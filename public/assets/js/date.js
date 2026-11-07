/* 日付・Epoch Time 変換 */
(function () {
  'use strict';
  const CT = (window.CT = window.CT || {});
  const DOW = ['日', '月', '火', '水', '木', '金', '土'];
  const EN_DOW = { Sun: 0, Mon: 1, Tue: 2, Wed: 3, Thu: 4, Fri: 5, Sat: 6 };
  const pad = (n, w = 2) => String(n).padStart(w, '0');

  // ---------- フォーマット文字列 ----------
  const TOKEN_RE = /'[^']*'|yyyy|yy|MM|M|dd|d|HH|H|mm|m|ss|s|SSS|E/g;

  function tokenize(fmt) {
    const out = [];
    let last = 0;
    fmt.replace(TOKEN_RE, (tok, idx) => {
      if (idx > last) out.push({ lit: fmt.slice(last, idx) });
      if (tok[0] === "'") out.push({ lit: tok.length === 2 ? "'" : tok.slice(1, -1) });
      else out.push({ tok });
      last = idx + tok.length;
      return tok;
    });
    if (last < fmt.length) out.push({ lit: fmt.slice(last) });
    return out;
  }

  function format(p, fmt) {
    return tokenize(fmt)
      .map((t) => {
        if (t.lit !== undefined) return t.lit;
        switch (t.tok) {
          case 'yyyy': return pad(p.year, 4);
          case 'yy': return pad(p.year % 100);
          case 'MM': return pad(p.month);
          case 'M': return String(p.month);
          case 'dd': return pad(p.day);
          case 'd': return String(p.day);
          case 'HH': return pad(p.hour);
          case 'H': return String(p.hour);
          case 'mm': return pad(p.minute);
          case 'm': return String(p.minute);
          case 'ss': return pad(p.second);
          case 's': return String(p.second);
          case 'SSS': return pad(p.ms, 3);
          case 'E': return DOW[p.dow];
        }
        return '';
      })
      .join('');
  }

  const TOKEN_PATTERN = {
    yyyy: '(\\d{4})', yy: '(\\d{2})', MM: '(\\d{2})', M: '(\\d{1,2})', dd: '(\\d{2})', d: '(\\d{1,2})',
    HH: '(\\d{2})', H: '(\\d{1,2})', mm: '(\\d{2})', m: '(\\d{1,2})', ss: '(\\d{2})', s: '(\\d{1,2})',
    SSS: '(\\d{3})', E: '([日月火水木金土])',
  };
  const escapeRe = (s) => s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');

  function parseWithFormat(str, fmt) {
    const tokens = tokenize(fmt);
    const keys = [];
    const re = new RegExp(
      '^' + tokens.map((t) => (t.lit !== undefined ? escapeRe(t.lit) : (keys.push(t.tok), TOKEN_PATTERN[t.tok]))).join('') + '$'
    );
    const m = normalize(str).match(re);
    if (!m) return null;
    const p = { year: 1970, month: 1, day: 1, hour: 0, minute: 0, second: 0, ms: 0 };
    keys.forEach((k, i) => {
      const v = m[i + 1];
      switch (k) {
        case 'yyyy': p.year = +v; break;
        case 'yy': p.year = 2000 + +v; break;
        case 'MM': case 'M': p.month = +v; break;
        case 'dd': case 'd': p.day = +v; break;
        case 'HH': case 'H': p.hour = +v; break;
        case 'mm': case 'm': p.minute = +v; break;
        case 'ss': case 's': p.second = +v; break;
        case 'SSS': p.ms = +v; break;
      }
    });
    return validate(p);
  }

  // ---------- 自動判別 ----------
  function normalize(str) {
    return str
      .trim()
      .replace(/[０-９：／－．，＋　]/g, (c) => (c === '　' ? ' ' : String.fromCharCode(c.charCodeAt(0) - 0xfee0)));
  }

  /** よくある書き方を自動で読む。{parts, offset(分) | null} を返す */
  function autoParse(str) {
    let s = normalize(str)
      .replace(/[（(][日月火水木金土][曜]?[日]?[）)]/g, ' ')
      .replace(/(\d)\s*年\s*/g, '$1/')
      .replace(/(\d)\s*月\s*/g, '$1/')
      .replace(/(\d)\s*日\s*/g, '$1 ')
      .replace(/(\d)\s*時\s*/g, '$1:')
      .replace(/(\d)\s*分\s*/g, '$1:')
      .replace(/(\d)\s*秒/g, '$1')
      .replace(/:\s*$/, '')
      .replace(/\s+/g, ' ')
      .trim();

    let m = s.match(
      /^(\d{4})[-/.](\d{1,2})[-/.](\d{1,2})(?:[T ](\d{1,2}):(\d{1,2})(?::(\d{1,2})(?:[.,](\d{1,9}))?)?)?\s*(Z|UTC|GMT|[+-]\d{2}:?\d{2})?$/i
    );
    if (!m) {
      const c = s.match(/^(\d{4})(\d{2})(\d{2})(?:[T ]?(\d{2})(\d{2})(\d{2})?)?$/);
      if (c) m = [c[0], c[1], c[2], c[3], c[4], c[5], c[6], undefined, undefined];
    }
    if (!m) return null;
    const parts = validate({
      year: +m[1], month: +m[2], day: +m[3],
      hour: +(m[4] || 0), minute: +(m[5] || 0), second: +(m[6] || 0),
      ms: m[7] ? +m[7].slice(0, 3).padEnd(3, '0') : 0,
    });
    if (!parts) return null;
    let offset = null;
    if (m[8]) {
      const z = m[8].toUpperCase();
      if (z === 'Z' || z === 'UTC' || z === 'GMT') offset = 0;
      else {
        const o = z.replace(':', '');
        offset = (o[0] === '-' ? -1 : 1) * (+o.slice(1, 3) * 60 + +o.slice(3, 5));
      }
    }
    return { parts, offset };
  }

  function utcMs(p) {
    const d = new Date(Date.UTC(2000, p.month - 1, p.day, p.hour, p.minute, p.second, p.ms));
    d.setUTCFullYear(p.year);
    return d.getTime();
  }

  function validate(p) {
    if (p.month < 1 || p.month > 12 || p.day < 1 || p.hour > 23 || p.minute > 59 || p.second > 59) return null;
    const d = new Date(utcMs(p));
    if (d.getUTCDate() !== p.day || d.getUTCMonth() !== p.month - 1) return null; // 2月30日など
    p.dow = d.getUTCDay();
    return p;
  }

  // ---------- Epoch ----------
  const UNITS = { s: '秒', ms: 'ミリ秒', us: 'マイクロ秒', ns: 'ナノ秒' };

  /** 数字の桁数から単位を推定し、ミリ秒に直す */
  function epochToMs(input, unit = 'auto') {
    const s = normalize(input).replace(/[,_\s]/g, '');
    if (!/^-?\d+(\.\d+)?$/.test(s)) throw new Error('数字だけを入力してください。');
    const [intPart, frac = ''] = s.split('.');
    const digits = intPart.replace('-', '').length;
    if (unit === 'auto') unit = digits <= 11 ? 's' : digits <= 14 ? 'ms' : digits <= 17 ? 'us' : 'ns';
    const big = BigInt(intPart);
    let ms;
    if (unit === 's') ms = Number(big) * 1000 + Math.round(Number('0.' + (frac || '0')) * 1000) * (s[0] === '-' ? -1 : 1);
    else if (unit === 'ms') ms = Number(big);
    else if (unit === 'us') ms = Number(big / 1000n);
    else ms = Number(big / 1000000n);
    if (!Number.isFinite(ms) || Math.abs(ms) > 8.64e15) throw new Error('値が大きすぎます（扱える範囲を超えています）。');
    return { ms, unit, digits };
  }

  /** あるタイムゾーンでの年月日などを取り出す（zone: 'Asia/Tokyo' | 'UTC' | undefined=端末） */
  function partsIn(ms, zone) {
    const f = new Intl.DateTimeFormat('en-US', {
      timeZone: zone, hourCycle: 'h23', year: 'numeric', month: '2-digit', day: '2-digit',
      hour: '2-digit', minute: '2-digit', second: '2-digit', weekday: 'short', era: 'short',
    });
    const o = {};
    f.formatToParts(new Date(ms)).forEach((x) => (o[x.type] = x.value));
    let year = +o.year;
    if (o.era === 'BC' || o.era === 'B') year = 1 - year;
    return {
      year, month: +o.month, day: +o.day, hour: +o.hour % 24, minute: +o.minute, second: +o.second,
      ms: ((ms % 1000) + 1000) % 1000, dow: EN_DOW[o.weekday],
    };
  }

  function offsetMinutes(ms, zone) {
    if (zone === 'UTC') return 0;
    if (zone === 'Asia/Tokyo') return 540;
    return -new Date(ms).getTimezoneOffset();
  }

  const offsetLabel = (min) =>
    min === 0 ? 'Z' : (min < 0 ? '-' : '+') + pad(Math.floor(Math.abs(min) / 60)) + ':' + pad(Math.abs(min) % 60);

  function iso(ms, zone) {
    const p = partsIn(ms, zone);
    return format(p, p.ms ? "yyyy-MM-dd'T'HH:mm:ss.SSS" : "yyyy-MM-dd'T'HH:mm:ss") + offsetLabel(offsetMinutes(ms, zone));
  }

  function toMs(parts, zone, offset) {
    const base = utcMs(parts);
    if (offset !== null && offset !== undefined) return base - offset * 60000;
    if (zone === 'UTC') return base;
    if (zone === 'Asia/Tokyo') return base - 540 * 60000;
    const d = new Date(2000, parts.month - 1, parts.day, parts.hour, parts.minute, parts.second, parts.ms);
    d.setFullYear(parts.year);
    return d.getTime();
  }

  function relative(ms, now = Date.now()) {
    const diff = (ms - now) / 1000;
    const abs = Math.abs(diff);
    const rtf = new Intl.RelativeTimeFormat('ja', { numeric: 'auto' });
    const steps = [[60, 'second', 1], [3600, 'minute', 60], [86400, 'hour', 3600], [86400 * 30, 'day', 86400], [86400 * 365, 'month', 86400 * 30], [Infinity, 'year', 86400 * 365]];
    for (const [limit, unit, div] of steps) {
      if (abs < limit) return rtf.format(Math.round(diff / div), unit);
    }
    return '';
  }

  const human = (p) => format(p, 'yyyy年M月d日（E） HH:mm:ss');

  CT.date = { tokenize, format, parseWithFormat, autoParse, epochToMs, partsIn, iso, toMs, relative, human, UNITS };

  // =========================================================
  // 画面
  // =========================================================
  if (document.body.dataset.page !== 'date') return;
  const $ = CT.$;
  const localZone = Intl.DateTimeFormat().resolvedOptions().timeZone;
  const localIsJst = new Date().getTimezoneOffset() === -540;

  const row = (label, value) => {
    const id = 'r' + Math.random().toString(36).slice(2, 9);
    return `<div class="result-row"><dt>${label}</dt><dd id="${id}">${escapeHtml(value)}</dd><button type="button" class="btn btn-ghost" data-copy="#${id}">コピー</button></div>`;
  };
  const escapeHtml = (s) => String(s).replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[c]);

  // ---- いまの時刻 ----
  function tick() {
    const now = Date.now();
    $('#now-s').textContent = Math.floor(now / 1000);
    $('#now-ms').textContent = now;
    $('#now-jst').textContent = format(partsIn(now, 'Asia/Tokyo'), 'yyyy/MM/dd HH:mm:ss');
    $('#now-utc').textContent = format(partsIn(now, 'UTC'), 'yyyy/MM/dd HH:mm:ss');
  }
  tick();
  setInterval(tick, 1000);

  // ---- Epoch → 日時 ----
  const epochIn = $('#epoch-in');
  const epochUnit = $('#epoch-unit');
  function renderEpoch() {
    const out = $('#epoch-out');
    const msg = $('#epoch-msg');
    const v = epochIn.value.trim();
    if (!v) {
      out.innerHTML = '';
      CT.say(msg, '');
      return;
    }
    try {
      const r = epochToMs(v, epochUnit.value);
      CT.say(msg, epochUnit.value === 'auto' ? `${r.digits}桁なので「${UNITS[r.unit]}」として変換しました。違う場合は単位を選んでください。` : '', 'info');
      const jst = partsIn(r.ms, 'Asia/Tokyo');
      let html = row('日本時間（JST）', format(jst, 'yyyy/MM/dd HH:mm:ss') + (r.ms % 1000 ? '.' + pad(jst.ms, 3) : ''))
        + row('UTC', format(partsIn(r.ms, 'UTC'), 'yyyy/MM/dd HH:mm:ss'))
        + row('ISO 8601（日本時間）', iso(r.ms, 'Asia/Tokyo'))
        + row('ISO 8601（UTC）', iso(r.ms, 'UTC'));
      if (!localIsJst) html += row(`端末の時刻（${localZone}）`, format(partsIn(r.ms), 'yyyy/MM/dd HH:mm:ss'));
      html += row('曜日・読みやすい形', human(jst)) + row('いまから見て', relative(r.ms));
      out.innerHTML = html;
    } catch (e) {
      out.innerHTML = '';
      CT.say(msg, e.message);
    }
  }
  epochIn.addEventListener('input', CT.debounce(renderEpoch, 100));
  epochUnit.addEventListener('change', renderEpoch);
  $('#epoch-now').addEventListener('click', () => {
    epochIn.value = Date.now();
    epochUnit.value = 'auto';
    renderEpoch();
  });

  // ---- 日時 → Epoch ----
  const dtIn = $('#dt-in');
  const dtZone = $('#dt-zone');
  if (!localIsJst) {
    const opt = dtZone.querySelector('option[value="local"]');
    if (opt) opt.textContent = `端末のタイムゾーン（${localZone}）`;
  }
  function renderDt() {
    const out = $('#dt-out');
    const msg = $('#dt-msg');
    const v = dtIn.value.trim();
    if (!v) {
      out.innerHTML = '';
      CT.say(msg, '');
      return;
    }
    const r = autoParse(v);
    if (!r) {
      out.innerHTML = '';
      CT.say(msg, '日時として読み取れませんでした。例：2024/05/20 10:30:00、2024-05-20T10:30:00+09:00、20240520');
      return;
    }
    const ms = toMs(r.parts, dtZone.value, r.offset);
    const zoneName = r.offset !== null ? `入力の ${offsetLabel(r.offset)}` : dtZone.options[dtZone.selectedIndex].text;
    CT.say(msg, `${human(r.parts)}（${zoneName}）として計算しました。`, 'info');
    out.innerHTML = row('Epoch 秒', Math.floor(ms / 1000)) + row('Epoch ミリ秒', ms) + row('ISO 8601（UTC）', iso(ms, 'UTC')) + row('ISO 8601（日本時間）', iso(ms, 'Asia/Tokyo'));
  }
  dtIn.addEventListener('input', CT.debounce(renderDt, 100));
  dtZone.addEventListener('change', renderDt);
  $('#dt-now').addEventListener('click', () => {
    dtIn.value = format(partsIn(Date.now(), 'Asia/Tokyo'), 'yyyy/MM/dd HH:mm:ss');
    dtZone.value = 'Asia/Tokyo';
    renderDt();
  });

  // ---- フォーマット変換（複数行） ----
  const fmtIn = $('#fmt-in');
  const fmtFrom = $('#fmt-from');
  const fmtTo = $('#fmt-to');
  const fmtPreset = $('#fmt-preset');
  function renderFmt() {
    const msg = $('#fmt-msg');
    const lines = fmtIn.value.split(/\r?\n/);
    const from = fmtFrom.value.trim();
    const to = fmtTo.value;
    let failed = 0;
    const result = lines.map((line) => {
      if (!line.trim()) return '';
      const p = from ? parseWithFormat(line, from) : (autoParse(line) || {}).parts;
      if (!p) {
        failed++;
        return '（変換できません）';
      }
      return format(p, to);
    });
    $('#fmt-out').value = fmtIn.value.trim() ? result.join('\n') : '';
    CT.say(msg, failed ? `${failed}行は読み取れませんでした。入力フォーマットを指定すると読める場合があります。` : '', 'error');
  }
  [fmtIn, fmtFrom, fmtTo].forEach((el) => el.addEventListener('input', CT.debounce(renderFmt, 120)));
  fmtPreset.addEventListener('change', () => {
    if (fmtPreset.value) fmtTo.value = fmtPreset.value;
    renderFmt();
  });
})();
