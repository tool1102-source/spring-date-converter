/* PDF ⇄ テキスト（PDF の読み取りは pdf.js、PDF の作成はブラウザの「PDF に保存」を使う） */
(function () {
  'use strict';
  const CT = (window.CT = window.CT || {});
  if (document.body.dataset.page !== 'pdf') return;
  const $ = CT.$;
  const PDFJS = '/assets/vendor/pdfjs/pdf-3.11.174.min.js';
  const WORKER = '/assets/vendor/pdfjs/pdf-3.11.174.worker.min.js';

  async function loadPdfJs() {
    await CT.loadScript(PDFJS);
    window.pdfjsLib.GlobalWorkerOptions.workerSrc = WORKER;
    return window.pdfjsLib;
  }
  $('#p2t-drop').addEventListener('pointerenter', () => loadPdfJs().catch(() => {}), { once: true });

  // ---------------- PDF → テキスト ----------------
  const p2t = { name: 'document' };
  const out = $('#p2t-out');
  const msg = $('#p2t-msg');

  CT.dropzone($('#p2t-drop'), async (file) => {
    out.value = '';
    $('#p2t-result').hidden = true;
    CT.say(msg, '読み込んでいます…', 'info');
    try {
      const pdfjsLib = await loadPdfJs();
      const doc = await pdfjsLib.getDocument({ data: new Uint8Array(await file.arrayBuffer()) }).promise;
      p2t.name = CT.baseName(file.name);
      const pages = [];
      for (let i = 1; i <= doc.numPages; i++) {
        CT.say(msg, `文字を取り出しています… ${i} / ${doc.numPages} ページ`, 'info');
        const page = await doc.getPage(i);
        const content = await page.getTextContent();
        let text = '';
        for (const item of content.items) {
          text += item.str;
          if (item.hasEOL) text += '\n';
        }
        pages.push(text.replace(/[ \t]+\n/g, '\n').trim());
      }
      const sep = $('#p2t-sep').checked;
      out.value = pages.map((t, i) => (sep ? `--- ${i + 1} ページ ---\n` : '') + t).join(sep ? '\n\n' : '\n');
      $('#p2t-file').textContent = `${file.name}（${doc.numPages} ページ・${CT.formatBytes(file.size)}）`;
      $('#p2t-result').hidden = false;
      const total = pages.join('').replace(/\s/g, '').length;
      CT.say(msg, total ? '' : '文字の情報が入っていない PDF のようです（紙をスキャンした画像の PDF など）。画像から文字を読み取る機能（OCR）はありません。', 'info');
      p2t.pages = pages;
    } catch (e) {
      const pw = e && e.name === 'PasswordException';
      CT.say(msg, pw ? 'パスワードで保護された PDF は読み取れません。' : 'PDF を読み込めませんでした。壊れていないか確認してください。');
    }
  });

  $('#p2t-sep').addEventListener('change', () => {
    if (!p2t.pages) return;
    const sep = $('#p2t-sep').checked;
    out.value = p2t.pages.map((t, i) => (sep ? `--- ${i + 1} ページ ---\n` : '') + t).join(sep ? '\n\n' : '\n');
  });

  $('#p2t-download').addEventListener('click', () => CT.download(out.value, p2t.name + '.txt'));

  // ---------------- テキスト → PDF ----------------
  const t2pIn = $('#t2p-in');
  CT.dropzone($('#t2p-drop'), async (file) => {
    const { text } = await CT.readText(file);
    t2pIn.value = text;
    $('#t2p-title').value = $('#t2p-title').value || CT.baseName(file.name);
  });

  $('#t2p-make').addEventListener('click', () => {
    const text = t2pIn.value;
    if (!text.trim()) {
      CT.toast('文章を入力してください');
      return;
    }
    let area = document.getElementById('print-area');
    if (!area) {
      area = document.createElement('div');
      area.id = 'print-area';
      document.body.appendChild(area);
    }
    const size = $('#t2p-size').value;
    const orient = $('#t2p-orient').value;
    const margin = $('#t2p-margin').value;
    area.style.fontSize = size + 'pt';
    area.textContent = text.replace(/\r\n?/g, '\n');
    let style = document.getElementById('print-page-style');
    if (!style) {
      style = document.createElement('style');
      style.id = 'print-page-style';
      document.head.appendChild(style);
    }
    style.textContent = `@page { size: A4 ${orient}; margin: ${margin}mm; }`;
    const oldTitle = document.title;
    document.title = $('#t2p-title').value.trim() || 'document';
    document.body.classList.add('printing');
    const done = () => {
      document.body.classList.remove('printing');
      document.title = oldTitle;
      window.removeEventListener('afterprint', done);
    };
    window.addEventListener('afterprint', done);
    window.print();
    setTimeout(done, 1000);
  });
})();
