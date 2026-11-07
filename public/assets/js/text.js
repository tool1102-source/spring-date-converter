/* テキストの表記変換と文字数カウント */
(function () {
  'use strict';
  const CT = (window.CT = window.CT || {});

  // ---------- 全角 ⇄ 半角（英数字・記号・スペース） ----------
  const toHalf = (s) => s.replace(/[！-～]/g, (c) => String.fromCharCode(c.charCodeAt(0) - 0xfee0)).replace(/　/g, ' ');
  const toFull = (s) => s.replace(/[!-~]/g, (c) => String.fromCharCode(c.charCodeAt(0) + 0xfee0)).replace(/ /g, '　');

  // ---------- カタカナ 全角 ⇄ 半角 ----------
  const FULL = 'アイウエオカキクケコサシスセソタチツテトナニヌネノハヒフヘホマミムメモヤユヨラリルレロワヲンァィゥェォッャュョー。「」、・゛゜';
  const HALF = 'ｱｲｳｴｵｶｷｸｹｺｻｼｽｾｿﾀﾁﾂﾃﾄﾅﾆﾇﾈﾉﾊﾋﾌﾍﾎﾏﾐﾑﾒﾓﾔﾕﾖﾗﾘﾙﾚﾛﾜｦﾝｧｨｩｪｫｯｬｭｮｰ｡｢｣､･ﾞﾟ';
  const f2h = new Map();
  const h2f = new Map();
  [...FULL].forEach((c, i) => {
    f2h.set(c, HALF[i]);
    h2f.set(HALF[i], c);
  });
  [...'カキクケコサシスセソタチツテトハヒフヘホ'].forEach((c) => {
    const voiced = String.fromCharCode(c.charCodeAt(0) + 1);
    f2h.set(voiced, f2h.get(c) + 'ﾞ');
    h2f.set(f2h.get(c) + 'ﾞ', voiced);
  });
  [...'ハヒフヘホ'].forEach((c) => {
    const semi = String.fromCharCode(c.charCodeAt(0) + 2);
    f2h.set(semi, f2h.get(c) + 'ﾟ');
    h2f.set(f2h.get(c) + 'ﾟ', semi);
  });
  f2h.set('ヴ', 'ｳﾞ');
  h2f.set('ｳﾞ', 'ヴ');
  const kanaToHalf = (s) => [...s].map((c) => f2h.get(c) || c).join('');
  const kanaToFull = (s) => s.replace(/[｡-ﾟ][ﾞﾟ]?/g, (m) => h2f.get(m) || [...m].map((c) => h2f.get(c) || c).join(''));

  // ---------- ひらがな ⇄ カタカナ ----------
  const hiraToKata = (s) => s.replace(/[ぁ-ゖゝゞ]/g, (c) => String.fromCharCode(c.charCodeAt(0) + 0x60));
  const kataToHira = (s) => s.replace(/[ァ-ヶヽヾ]/g, (c) => String.fromCharCode(c.charCodeAt(0) - 0x60));

  // ---------- HTML ----------
  const escapeHtml = (s) => s.replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[c]);
  const unescapeHtml = (s) => new DOMParser().parseFromString('<!doctype html><body><textarea>' + s.replace(/</g, '&lt;') + '</textarea>', 'text/html').querySelector('textarea').value;

  // ---------- 行の整理 ----------
  const trimLines = (s) => s.split('\n').map((l) => l.trim()).join('\n');
  const removeEmptyLines = (s) => s.split('\n').filter((l) => l.trim()).join('\n');
  const uniqueLines = (s) => [...new Set(s.split('\n'))].join('\n');
  const sortLines = (s) => s.split('\n').sort((a, b) => a.localeCompare(b, 'ja')).join('\n');
  const joinLines = (s) => s.replace(/\r?\n/g, '');

  const MODES = {
    half: ['全角英数 → 半角', toHalf],
    full: ['半角英数 → 全角', toFull],
    kanaHalf: ['全角カナ → 半角カナ', kanaToHalf],
    kanaFull: ['半角カナ → 全角カナ', kanaToFull],
    hira: ['カタカナ → ひらがな', kataToHira],
    kata: ['ひらがな → カタカナ', hiraToKata],
    lower: ['大文字 → 小文字', (s) => s.toLowerCase()],
    upper: ['小文字 → 大文字', (s) => s.toUpperCase()],
    esc: ['HTML エスケープ', escapeHtml],
    unesc: ['HTML アンエスケープ', unescapeHtml],
    trim: ['各行の前後の空白を削除', trimLines],
    noEmpty: ['空の行を削除', removeEmptyLines],
    unique: ['重複した行を削除', uniqueLines],
    sort: ['行を並べ替え', sortLines],
    join: ['改行を削除', joinLines],
  };

  function count(s) {
    const graphemes = typeof Intl.Segmenter === 'function' ? [...new Intl.Segmenter('ja', { granularity: 'grapheme' }).segment(s)].length : [...s].length;
    return {
      chars: graphemes,
      noSpace: typeof Intl.Segmenter === 'function' ? [...new Intl.Segmenter('ja', { granularity: 'grapheme' }).segment(s.replace(/\s/g, ''))].length : [...s.replace(/\s/g, '')].length,
      lines: s ? s.split(/\r\n|\r|\n/).length : 0,
      bytes: new TextEncoder().encode(s).length,
      manuscript: Math.ceil(s.split(/\r\n|\r|\n/).reduce((n, l) => n + Math.max(1, Math.ceil([...l].length / 20)), 0) / 20) || 0,
    };
  }

  CT.text = { toHalf, toFull, kanaToHalf, kanaToFull, hiraToKata, kataToHira, escapeHtml, unescapeHtml, MODES, count };

  // =========================================================
  // 画面
  // =========================================================
  if (document.body.dataset.page !== 'text') return;
  const $ = CT.$;
  const input = $('#tx-in');
  const output = $('#tx-out');
  const chips = $('#tx-modes');
  chips.innerHTML = Object.entries(MODES)
    .map(([k, [label]], i) => `<button type="button" data-value="${k}" aria-pressed="${i === 0}">${label}</button>`)
    .join('');
  const mode = CT.toggleGroup(chips, () => run());

  function run() {
    const s = input.value.replace(/\r\n?/g, '\n');
    output.value = s ? MODES[mode.value][1](s) : '';
    const c = count(input.value);
    $('#cnt-chars').textContent = c.chars.toLocaleString();
    $('#cnt-nospace').textContent = c.noSpace.toLocaleString();
    $('#cnt-lines').textContent = c.lines.toLocaleString();
    $('#cnt-bytes').textContent = c.bytes.toLocaleString();
    $('#cnt-paper').textContent = c.manuscript.toLocaleString();
  }
  input.addEventListener('input', CT.debounce(run, 80));
  $('#tx-apply').addEventListener('click', () => {
    input.value = output.value;
    run();
    CT.toast('結果を入力に戻しました');
  });
  $('#tx-clear').addEventListener('click', () => {
    input.value = '';
    run();
    input.focus();
  });
  run();
})();
