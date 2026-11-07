/* Excel ⇄ CSV 変換（SheetJS をブラウザ内で使う。ファイルはサーバーに送らない） */
(function () {
  'use strict';
  const CT = (window.CT = window.CT || {});
  if (document.body.dataset.page !== 'excel') return;
  const $ = CT.$;
  const XLSX_SRC = '/assets/vendor/xlsx-0.20.3.full.min.js';
  const loadXlsx = () => CT.loadScript(XLSX_SRC).then(() => window.XLSX);

  // 使いそうなときに先に読み込んでおく
  const warm = () => loadXlsx().catch(() => {});
  ['pointerenter', 'focusin', 'touchstart'].forEach((ev) => CT.$$('.drop').forEach((d) => d.addEventListener(ev, warm, { once: true })));

  function preview(table, rows, limit = 50) {
    const head = rows[0] || [];
    const width = Math.max(...rows.slice(0, limit + 1).map((r) => r.length), 0);
    const esc = (s) => String(s ?? '').replace(/[&<>]/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;' })[c]);
    let html = '<thead><tr><th>#</th>' + Array.from({ length: width }, (_, i) => `<th>${esc(head[i])}</th>`).join('') + '</tr></thead><tbody>';
    rows.slice(1, limit + 1).forEach((r, i) => {
      html += `<tr><th>${i + 2}</th>` + Array.from({ length: width }, (_, j) => `<td title="${esc(r[j])}">${esc(r[j])}</td>`).join('') + '</tr>';
    });
    table.innerHTML = html + '</tbody>';
  }

  const delim = (v) => (v === 'tab' ? '\t' : v);

  // ---------------- Excel → CSV ----------------
  const x2c = { wb: null, name: '' };
  const x2cMsg = $('#x2c-msg');

  function x2cRows() {
    const ws = x2c.wb.Sheets[$('#x2c-sheet').value];
    return window.XLSX.utils.sheet_to_json(ws, { header: 1, raw: false, defval: '', blankrows: false });
  }

  function x2cRender() {
    const rows = x2cRows();
    preview($('#x2c-table'), rows);
    $('#x2c-info').textContent = `${rows.length} 行 × ${Math.max(0, ...rows.map((r) => r.length))} 列` + (rows.length > 51 ? '（プレビューは先頭50行）' : '');
  }

  CT.dropzone($('#x2c-drop'), async (file) => {
    CT.say(x2cMsg, '読み込んでいます…', 'info');
    $('#x2c-result').hidden = true;
    try {
      const XLSX = await loadXlsx();
      const wb = XLSX.read(await file.arrayBuffer(), { type: 'array', cellDates: true, dense: true });
      x2c.wb = wb;
      x2c.name = CT.baseName(file.name);
      const sel = $('#x2c-sheet');
      sel.replaceChildren(...wb.SheetNames.map((n) => new Option(n, n)));
      $('#x2c-sheet-field').hidden = wb.SheetNames.length < 2;
      $('#x2c-file').textContent = `${file.name}（${CT.formatBytes(file.size)}・${wb.SheetNames.length} シート）`;
      x2cRender();
      $('#x2c-result').hidden = false;
      CT.say(x2cMsg, '');
    } catch (e) {
      CT.say(x2cMsg, 'ファイルを読み込めませんでした。Excel（.xlsx / .xls）や .ods のファイルか確認してください。' + (e && e.message && e.message.includes('password') ? '（パスワード付きのファイルは開けません）' : ''));
    }
  });
  $('#x2c-sheet').addEventListener('change', x2cRender);

  $('#x2c-download').addEventListener('click', () => {
    const d = delim($('#x2c-delim').value);
    const text = CT.csv.stringify(x2cRows(), d);
    const sheet = x2c.wb.SheetNames.length > 1 ? '_' + $('#x2c-sheet').value : '';
    const bom = $('#x2c-bom').checked ? '﻿' : '';
    CT.download(bom + text, `${x2c.name}${sheet}.${d === '\t' ? 'tsv' : 'csv'}`, 'text/csv;charset=utf-8');
  });

  // ---------------- CSV → Excel ----------------
  const c2x = { text: '', name: '', encoding: '' };
  const c2xMsg = $('#c2x-msg');
  const NUMBER_RE = /^-?(0|[1-9]\d{0,14})(\.\d+)?$/;

  function c2xRows() {
    const sel = $('#c2x-delim').value;
    const d = sel === 'auto' ? CT.csv.detectDelimiter(c2x.text) : delim(sel);
    return { rows: CT.csv.dropEmptyRows(CT.csv.parse(c2x.text, d)), d };
  }

  function c2xRender() {
    try {
      const { rows, d } = c2xRows();
      preview($('#c2x-table'), rows);
      $('#c2x-info').textContent = `${rows.length} 行・文字コード ${c2x.encoding}・区切り ${d === '\t' ? 'タブ' : d}` + (rows.length > 51 ? '（プレビューは先頭50行）' : '');
      $('#c2x-result').hidden = false;
      CT.say(c2xMsg, '');
    } catch (e) {
      $('#c2x-result').hidden = true;
      CT.say(c2xMsg, e.message);
    }
  }

  CT.dropzone($('#c2x-drop'), async (file) => {
    const { text, encoding } = await CT.readText(file);
    Object.assign(c2x, { text, encoding, name: CT.baseName(file.name) });
    $('#c2x-file').textContent = `${file.name}（${CT.formatBytes(file.size)}）`;
    c2xRender();
    warm();
  });
  $('#c2x-delim').addEventListener('change', c2xRender);

  $('#c2x-download').addEventListener('click', async () => {
    try {
      const XLSX = await loadXlsx();
      const { rows } = c2xRows();
      const asNumber = $('#c2x-numbers').checked;
      const data = rows.map((r) => r.map((v) => (asNumber && NUMBER_RE.test(v) ? Number(v) : v)));
      const ws = XLSX.utils.aoa_to_sheet(data);
      // 列の幅を中身に合わせる（最大 50 文字）
      const widths = [];
      data.slice(0, 500).forEach((r) => r.forEach((v, i) => (widths[i] = Math.max(widths[i] || 4, Math.min(50, String(v).length * 1.6)))));
      ws['!cols'] = widths.map((w) => ({ wch: w }));
      const wb = XLSX.utils.book_new();
      XLSX.utils.book_append_sheet(wb, ws, ($('#c2x-sheetname').value.trim() || 'Sheet1').slice(0, 31));
      XLSX.writeFile(wb, c2x.name + '.xlsx', { compression: true });
    } catch (e) {
      CT.say(c2xMsg, e.message || 'Excel ファイルを作れませんでした。');
    }
  });
})();
