/* JSON ⇄ CSV / TSV 変換 */
(function () {
  'use strict';
  const CT = (window.CT = window.CT || {});

  const isPlainObject = (v) => v !== null && typeof v === 'object' && !Array.isArray(v);

  /** {a:{b:1}} → {"a.b":1}。配列はそのまま JSON 文字列にする */
  function flatten(obj, prefix = '', out = {}) {
    for (const [k, v] of Object.entries(obj)) {
      const key = prefix ? prefix + '.' + k : k;
      if (isPlainObject(v) && Object.keys(v).length) flatten(v, key, out);
      else out[key] = v;
    }
    return out;
  }

  const cell = (v) => (v === null || v === undefined ? '' : typeof v === 'object' ? JSON.stringify(v) : String(v));

  /** JSON の値を表（2次元配列）にする */
  function jsonToRows(data, { flat = true } = {}) {
    if (isPlainObject(data)) data = [data];
    if (!Array.isArray(data)) throw new Error('JSON の配列（[ ... ]）かオブジェクト（{ ... }）を入力してください。');
    if (data.length === 0) return [];
    if (data.every(Array.isArray)) return data.map((r) => r.map(cell));
    if (!data.every(isPlainObject)) return [['value'], ...data.map((v) => [cell(v)])];

    const objects = flat ? data.map((o) => flatten(o)) : data;
    const headers = [];
    const seen = new Set();
    objects.forEach((o) => Object.keys(o).forEach((k) => seen.has(k) || (seen.add(k), headers.push(k))));
    return [headers, ...objects.map((o) => headers.map((h) => cell(o[h])))];
  }

  const NUMBER_RE = /^-?(0|[1-9]\d{0,14})(\.\d+)?([eE][+-]?\d+)?$/;

  function convertValue(s) {
    if (NUMBER_RE.test(s)) return Number(s);
    if (s === 'true') return true;
    if (s === 'false') return false;
    if (s === 'null') return null;
    return s;
  }

  /** 表（2次元配列）を JSON の値にする */
  function rowsToJson(rows, { header = true, types = true } = {}) {
    rows = CT.csv.dropEmptyRows(rows);
    const val = (s) => (types ? convertValue(s) : s);
    if (!header) return rows.map((r) => r.map(val));
    if (rows.length === 0) return [];
    const used = new Map();
    const headers = rows[0].map((h, i) => {
      let name = h.trim() || 'column' + (i + 1);
      const n = used.get(name) || 0;
      used.set(name, n + 1);
      return n ? `${name}_${n + 1}` : name;
    });
    return rows.slice(1).map((r) => {
      const o = {};
      const width = Math.max(headers.length, r.length);
      for (let i = 0; i < width; i++) o[headers[i] || 'column' + (i + 1)] = val(r[i] === undefined ? '' : r[i]);
      return o;
    });
  }

  function parseJson(text) {
    try {
      return JSON.parse(text);
    } catch (e) {
      const m = /position (\d+)/.exec(e.message);
      if (m) {
        const pos = +m[1];
        const before = text.slice(0, pos);
        const line = before.split('\n').length;
        const col = pos - before.lastIndexOf('\n');
        throw new Error(`JSON の書き方に誤りがあります（${line}行目・${col}文字目あたり）。`);
      }
      throw new Error('JSON の書き方に誤りがあります。カンマや引用符の閉じ忘れがないか確認してください。');
    }
  }

  CT.jsonCsv = { flatten, jsonToRows, rowsToJson, parseJson, convertValue };

  // =========================================================
  // 画面
  // =========================================================
  if (document.body.dataset.page !== 'json') return;
  const $ = CT.$;
  const input = $('#jc-in');
  const output = $('#jc-out');
  const msg = $('#jc-msg');
  const optJ2C = $('#opt-j2c');
  const optC2J = $('#opt-c2j');

  const mode = CT.toggleGroup($('#jc-mode'), (m) => {
    optJ2C.hidden = m !== 'j2c';
    optC2J.hidden = m !== 'c2j';
    $('#jc-in-label').textContent = m === 'j2c' ? 'JSON' : 'CSV / TSV';
    $('#jc-out-label').textContent = m === 'j2c' ? 'CSV / TSV' : 'JSON';
    input.placeholder = m === 'j2c' ? '[{"id": 1, "name": "山田", "tags": ["a", "b"]}]' : 'id,name\n1,山田';
    run();
  });

  const delimiterOf = (sel, text) => (sel.value === 'auto' ? CT.csv.detectDelimiter(text) : sel.value === 'tab' ? '\t' : sel.value);

  function run() {
    const text = input.value;
    CT.say(msg, '');
    if (!text.trim()) {
      output.value = '';
      $('#jc-stats').textContent = '';
      return;
    }
    try {
      if (mode.value === 'j2c') {
        const rows = jsonToRows(parseJson(text), { flat: $('#opt-flat').checked });
        output.value = CT.csv.stringify(rows, delimiterOf($('#opt-out-delim'), ''));
        $('#jc-stats').textContent = rows.length ? `${rows.length - 1} 行 × ${rows[0].length} 列` : '';
      } else {
        const d = delimiterOf($('#opt-in-delim'), text);
        const rows = CT.csv.parse(text, d);
        const json = rowsToJson(rows, { header: $('#opt-header').checked, types: $('#opt-types').checked });
        output.value = JSON.stringify(json, null, $('#opt-indent').value === '0' ? 0 : +$('#opt-indent').value);
        $('#jc-stats').textContent = `${json.length} 件` + ($('#opt-in-delim').value === 'auto' ? `（区切り：${d === '\t' ? 'タブ' : d}）` : '');
      }
    } catch (e) {
      output.value = '';
      $('#jc-stats').textContent = '';
      CT.say(msg, e.message);
    }
  }

  input.addEventListener('input', CT.debounce(run, 200));
  CT.$$('#opt-j2c input, #opt-j2c select, #opt-c2j input, #opt-c2j select').forEach((el) => el.addEventListener('change', run));

  CT.dropzone($('#jc-file'), async (file) => {
    const { text } = await CT.readText(file);
    input.value = text;
    if (/\.(csv|tsv|txt)$/i.test(file.name) && mode.value === 'j2c') mode.set('c2j');
    else if (/\.json$/i.test(file.name) && mode.value === 'c2j') mode.set('j2c');
    else run();
  });

  $('#jc-swap').addEventListener('click', () => {
    if (!output.value) return;
    input.value = output.value;
    mode.set(mode.value === 'j2c' ? 'c2j' : 'j2c');
  });

  $('#jc-clear').addEventListener('click', () => {
    input.value = '';
    run();
    input.focus();
  });

  $('#jc-download').addEventListener('click', () => {
    if (!output.value) return CT.toast('ダウンロードする内容がありません');
    if (mode.value === 'j2c') {
      const tsv = $('#opt-out-delim').value === 'tab';
      const bom = $('#opt-bom').checked ? '﻿' : '';
      CT.download(bom + output.value, tsv ? 'data.tsv' : 'data.csv', 'text/csv;charset=utf-8');
    } else {
      CT.download(output.value, 'data.json', 'application/json');
    }
  });
})();
