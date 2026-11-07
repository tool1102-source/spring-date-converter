/* ConverterTools.jp 共通の小道具（コピー・ダウンロード・ファイル読み込みなど） */
(function () {
  'use strict';
  const CT = (window.CT = window.CT || {});

  CT.$ = (sel, root = document) => root.querySelector(sel);
  CT.$$ = (sel, root = document) => Array.from(root.querySelectorAll(sel));

  CT.debounce = (fn, ms = 150) => {
    let t;
    return (...args) => {
      clearTimeout(t);
      t = setTimeout(() => fn(...args), ms);
    };
  };

  let toastTimer;
  CT.toast = (msg) => {
    const el = document.getElementById('toast');
    if (!el) return;
    el.textContent = msg;
    el.classList.add('is-show');
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => el.classList.remove('is-show'), 1800);
  };

  CT.copy = async (text) => {
    if (!text) {
      CT.toast('コピーする内容がありません');
      return;
    }
    try {
      await navigator.clipboard.writeText(text);
    } catch (e) {
      const ta = document.createElement('textarea');
      ta.value = text;
      ta.setAttribute('readonly', '');
      ta.style.position = 'fixed';
      ta.style.opacity = '0';
      document.body.appendChild(ta);
      ta.select();
      document.execCommand('copy');
      ta.remove();
    }
    CT.toast('コピーしました');
  };

  CT.download = (content, filename, mime = 'text/plain;charset=utf-8') => {
    const blob = content instanceof Blob ? content : new Blob([content], { type: mime });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    a.remove();
    setTimeout(() => URL.revokeObjectURL(url), 2000);
  };

  CT.baseName = (name) => (name || 'file').replace(/\.[^.]+$/, '');

  CT.formatBytes = (n) => {
    if (n < 1024) return n + ' B';
    if (n < 1024 * 1024) return (n / 1024).toFixed(1) + ' KB';
    return (n / 1024 / 1024).toFixed(1) + ' MB';
  };

  /**
   * バイト列を文字列に。BOM → UTF-8 → Shift_JIS の順に判定する
   * （日本語版 Excel で保存した CSV は Shift_JIS のことが多い）。
   */
  CT.decodeText = (buffer) => {
    const bytes = new Uint8Array(buffer);
    if (bytes[0] === 0xef && bytes[1] === 0xbb && bytes[2] === 0xbf) {
      return { text: new TextDecoder('utf-8').decode(bytes.subarray(3)), encoding: 'UTF-8（BOM付き）' };
    }
    if (bytes[0] === 0xff && bytes[1] === 0xfe) {
      return { text: new TextDecoder('utf-16le').decode(bytes.subarray(2)), encoding: 'UTF-16' };
    }
    if (bytes[0] === 0xfe && bytes[1] === 0xff) {
      return { text: new TextDecoder('utf-16be').decode(bytes.subarray(2)), encoding: 'UTF-16' };
    }
    try {
      return { text: new TextDecoder('utf-8', { fatal: true }).decode(bytes), encoding: 'UTF-8' };
    } catch (e) {
      return { text: new TextDecoder('shift_jis').decode(bytes), encoding: 'Shift_JIS' };
    }
  };

  CT.readText = async (file) => CT.decodeText(await file.arrayBuffer());

  /** ファイル選択欄＋ドラッグ＆ドロップ。drop 要素の中に <input type="file"> を置く */
  CT.dropzone = (drop, onFile) => {
    const input = drop.querySelector('input[type="file"]');
    const pick = (file) => file && onFile(file);
    input.addEventListener('change', () => {
      pick(input.files[0]);
      input.value = '';
    });
    ['dragenter', 'dragover'].forEach((ev) =>
      drop.addEventListener(ev, (e) => {
        e.preventDefault();
        drop.classList.add('is-over');
      })
    );
    ['dragleave', 'drop'].forEach((ev) => drop.addEventListener(ev, () => drop.classList.remove('is-over')));
    drop.addEventListener('drop', (e) => {
      e.preventDefault();
      pick(e.dataTransfer.files[0]);
    });
  };

  const loaded = {};
  CT.loadScript = (src) =>
    (loaded[src] =
      loaded[src] ||
      new Promise((resolve, reject) => {
        const s = document.createElement('script');
        s.src = src;
        s.onload = resolve;
        s.onerror = () => {
          delete loaded[src];
          reject(new Error('部品の読み込みに失敗しました。通信状況を確認して、もう一度お試しください。'));
        };
        document.head.appendChild(s);
      }));

  /** メッセージ欄の表示（type: 'error' | 'info'） */
  CT.say = (el, text, type = 'error') => {
    el.textContent = text || '';
    el.className = 'message' + (text ? ' is-' + type : '');
  };

  /** セグメント／チップの切り替えボタン群。選ばれた data-value を返す */
  CT.toggleGroup = (group, onChange) => {
    const buttons = CT.$$('button[data-value]', group);
    const set = (value) => {
      buttons.forEach((b) => b.setAttribute('aria-pressed', String(b.dataset.value === value)));
      onChange && onChange(value);
    };
    buttons.forEach((b) => b.addEventListener('click', () => set(b.dataset.value)));
    return {
      get value() {
        const on = buttons.find((b) => b.getAttribute('aria-pressed') === 'true');
        return on ? on.dataset.value : buttons[0].dataset.value;
      },
      set,
    };
  };

  document.addEventListener('DOMContentLoaded', () => {
    const page = document.body.dataset.page;
    CT.$$('.nav a').forEach((a) => {
      if (a.dataset.page === page) a.setAttribute('aria-current', 'page');
    });
    const year = new Date().getFullYear();
    CT.$$('.footer-copy').forEach((el) => (el.textContent = '© 2025' + (year > 2025 ? '–' + year : '') + ' ConverterTools.jp'));
  });

  // <button data-copy="#id"> でその要素の中身をコピー
  document.addEventListener('click', (e) => {
    const btn = e.target.closest('[data-copy]');
    if (!btn) return;
    const el = document.querySelector(btn.dataset.copy);
    if (el) CT.copy('value' in el ? el.value : el.textContent);
  });
})();
