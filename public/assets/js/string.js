/* 文字列ツール：Base64・URL エンコード・ハッシュ・UUID */
(function () {
  'use strict';
  const CT = (window.CT = window.CT || {});
  const enc = new TextEncoder();

  // ---------- Base64 ----------
  function bytesToBase64(bytes) {
    let bin = '';
    for (let i = 0; i < bytes.length; i += 0x8000) bin += String.fromCharCode.apply(null, bytes.subarray(i, i + 0x8000));
    return btoa(bin);
  }
  function base64Encode(text, urlSafe = false) {
    const b = bytesToBase64(enc.encode(text));
    return urlSafe ? b.replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '') : b;
  }
  function base64Decode(b64) {
    let s = b64.replace(/\s+/g, '').replace(/-/g, '+').replace(/_/g, '/');
    if (/[^A-Za-z0-9+/=]/.test(s)) throw new Error('Base64 に使えない文字が含まれています。');
    while (s.length % 4) s += '=';
    let bin;
    try {
      bin = atob(s);
    } catch (e) {
      throw new Error('Base64 として正しくありません。');
    }
    const bytes = Uint8Array.from(bin, (c) => c.charCodeAt(0));
    try {
      return new TextDecoder('utf-8', { fatal: true }).decode(bytes);
    } catch (e) {
      throw new Error('文字として読めないデータです（画像などのバイナリデータかもしれません）。');
    }
  }

  // ---------- URL ----------
  function urlEncode(text, mode) {
    return mode === 'uri' ? encodeURI(text) : encodeURIComponent(text);
  }
  function urlDecode(text, plusAsSpace) {
    try {
      return decodeURIComponent(plusAsSpace ? text.replace(/\+/g, ' ') : text);
    } catch (e) {
      throw new Error('「%」のあとの書き方が正しくない部分があります。');
    }
  }

  // ---------- MD5（Web Crypto に無いので自前で計算） ----------
  const MD5_K = Array.from({ length: 64 }, (_, i) => Math.floor(Math.abs(Math.sin(i + 1)) * 4294967296) >>> 0);
  const MD5_S = [7, 12, 17, 22, 5, 9, 14, 20, 4, 11, 16, 23, 6, 10, 15, 21];
  function md5(bytes) {
    const len = bytes.length;
    const blocks = ((len + 8) >>> 6) + 1;
    const w = new Uint32Array(blocks * 16);
    for (let i = 0; i < len; i++) w[i >> 2] |= bytes[i] << ((i % 4) * 8);
    w[len >> 2] |= 0x80 << ((len % 4) * 8);
    w[blocks * 16 - 2] = (len * 8) >>> 0;
    w[blocks * 16 - 1] = Math.floor((len * 8) / 4294967296);
    let a0 = 0x67452301, b0 = 0xefcdab89, c0 = 0x98badcfe, d0 = 0x10325476;
    for (let b = 0; b < blocks; b++) {
      let A = a0, B = b0, C = c0, D = d0;
      for (let i = 0; i < 64; i++) {
        let F, g;
        if (i < 16) { F = (B & C) | (~B & D); g = i; }
        else if (i < 32) { F = (D & B) | (~D & C); g = (5 * i + 1) % 16; }
        else if (i < 48) { F = B ^ C ^ D; g = (3 * i + 5) % 16; }
        else { F = C ^ (B | ~D); g = (7 * i) % 16; }
        F = (F + A + MD5_K[i] + w[b * 16 + g]) >>> 0;
        const s = MD5_S[(i >> 4) * 4 + (i % 4)];
        A = D; D = C; C = B;
        B = (B + ((F << s) | (F >>> (32 - s)))) >>> 0;
      }
      a0 = (a0 + A) >>> 0; b0 = (b0 + B) >>> 0; c0 = (c0 + C) >>> 0; d0 = (d0 + D) >>> 0;
    }
    return [a0, b0, c0, d0].map((x) => [0, 8, 16, 24].map((s) => ((x >>> s) & 255).toString(16).padStart(2, '0')).join('')).join('');
  }

  async function hashes(text) {
    const bytes = enc.encode(text);
    const hex = (buf) => Array.from(new Uint8Array(buf), (b) => b.toString(16).padStart(2, '0')).join('');
    const out = { MD5: md5(bytes) };
    for (const alg of ['SHA-1', 'SHA-256', 'SHA-384', 'SHA-512']) out[alg] = hex(await crypto.subtle.digest(alg, bytes));
    return out;
  }

  function uuid() {
    if (crypto.randomUUID) return crypto.randomUUID();
    const b = crypto.getRandomValues(new Uint8Array(16));
    b[6] = (b[6] & 0x0f) | 0x40;
    b[8] = (b[8] & 0x3f) | 0x80;
    const h = Array.from(b, (x) => x.toString(16).padStart(2, '0')).join('');
    return `${h.slice(0, 8)}-${h.slice(8, 12)}-${h.slice(12, 16)}-${h.slice(16, 20)}-${h.slice(20)}`;
  }

  CT.str = { base64Encode, base64Decode, bytesToBase64, urlEncode, urlDecode, md5, hashes, uuid };

  // =========================================================
  // 画面
  // =========================================================
  if (document.body.dataset.page !== 'string') return;
  const $ = CT.$;

  // Base64
  const b64In = $('#b64-in');
  const b64Mode = CT.toggleGroup($('#b64-mode'), () => runB64());
  function runB64() {
    const msg = $('#b64-msg');
    CT.say(msg, '');
    if (!b64In.value) return ($('#b64-out').value = '');
    try {
      $('#b64-out').value = b64Mode.value === 'enc' ? base64Encode(b64In.value, $('#b64-url').checked) : base64Decode(b64In.value);
    } catch (e) {
      $('#b64-out').value = '';
      CT.say(msg, e.message);
    }
  }
  b64In.addEventListener('input', CT.debounce(runB64, 100));
  $('#b64-url').addEventListener('change', runB64);
  CT.dropzone($('#b64-drop'), async (file) => {
    if (file.size > 20 * 1024 * 1024) return CT.say($('#b64-msg'), '20MB より大きいファイルは扱えません。');
    const b = bytesToBase64(new Uint8Array(await file.arrayBuffer()));
    b64Mode.set('enc');
    b64In.value = '';
    $('#b64-out').value = $('#b64-dataurl').checked ? `data:${file.type || 'application/octet-stream'};base64,${b}` : b;
    CT.say($('#b64-msg'), `${file.name}（${CT.formatBytes(file.size)}）を Base64 にしました。`, 'info');
  });

  // URL
  const urlIn = $('#url-in');
  const urlMode = CT.toggleGroup($('#url-mode'), () => runUrl());
  function runUrl() {
    const msg = $('#url-msg');
    CT.say(msg, '');
    const m = urlMode.value;
    $('#url-plus-wrap').hidden = m !== 'dec';
    if (!urlIn.value) return ($('#url-out').value = '');
    try {
      $('#url-out').value = m === 'dec' ? urlDecode(urlIn.value, $('#url-plus').checked) : urlEncode(urlIn.value, m);
    } catch (e) {
      $('#url-out').value = '';
      CT.say(msg, e.message);
    }
  }
  urlIn.addEventListener('input', CT.debounce(runUrl, 100));
  $('#url-plus').addEventListener('change', runUrl);
  runUrl();

  // ハッシュ
  const hashIn = $('#hash-in');
  const runHash = CT.debounce(async () => {
    const res = await hashes(hashIn.value);
    const upper = $('#hash-upper').checked;
    $('#hash-out').innerHTML = Object.entries(res)
      .map(([k, v]) => `<div class="result-row"><dt>${k}</dt><dd id="h-${k}">${upper ? v.toUpperCase() : v}</dd><button type="button" class="btn btn-ghost" data-copy="#h-${k}">コピー</button></div>`)
      .join('');
  }, 120);
  hashIn.addEventListener('input', runHash);
  $('#hash-upper').addEventListener('change', runHash);
  runHash();

  // UUID
  function runUuid() {
    const n = Math.min(1000, Math.max(1, parseInt($('#uuid-count').value, 10) || 1));
    const upper = $('#uuid-upper').checked;
    const hyphen = $('#uuid-hyphen').checked;
    $('#uuid-out').value = Array.from({ length: n }, () => {
      let u = uuid();
      if (!hyphen) u = u.replace(/-/g, '');
      return upper ? u.toUpperCase() : u;
    }).join('\n');
  }
  $('#uuid-make').addEventListener('click', runUuid);
  ['#uuid-upper', '#uuid-hyphen'].forEach((s) => $(s).addEventListener('change', runUuid));
  runUuid();
})();
