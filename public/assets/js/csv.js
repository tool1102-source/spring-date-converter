/* CSV / TSV の読み書き（RFC 4180 準拠：引用符・改行入りの値・"" のエスケープに対応） */
(function () {
  'use strict';
  const CT = (window.CT = window.CT || {});

  function parse(text, delimiter = ',') {
    const rows = [];
    let row = [];
    let field = '';
    let inQuotes = false;
    let quotedField = false;
    const n = text.length;

    for (let i = 0; i < n; i++) {
      const c = text[i];
      if (inQuotes) {
        if (c === '"') {
          if (text[i + 1] === '"') {
            field += '"';
            i++;
          } else {
            inQuotes = false;
          }
        } else {
          field += c;
        }
        continue;
      }
      if (c === '"' && field === '' && !quotedField) {
        inQuotes = true;
        quotedField = true;
      } else if (c === delimiter) {
        row.push(field);
        field = '';
        quotedField = false;
      } else if (c === '\r' || c === '\n') {
        if (c === '\r' && text[i + 1] === '\n') i++;
        row.push(field);
        rows.push(row);
        row = [];
        field = '';
        quotedField = false;
      } else {
        field += c;
      }
    }
    if (inQuotes) {
      throw new Error('引用符（"）が閉じられていない値があります。');
    }
    if (field !== '' || quotedField || row.length) {
      row.push(field);
      rows.push(row);
    }
    return rows;
  }

  function escape(value, delimiter) {
    if (value === null || value === undefined) return '';
    const s = String(value);
    if (s.includes(delimiter) || /["\r\n]/.test(s) || /^\s|\s$/.test(s)) {
      return '"' + s.replace(/"/g, '""') + '"';
    }
    return s;
  }

  function stringify(rows, delimiter = ',') {
    return rows.map((r) => r.map((v) => escape(v, delimiter)).join(delimiter)).join('\r\n');
  }

  /** 最初の数行を見て区切り文字を推定する */
  function detectDelimiter(text) {
    const sample = text.slice(0, 20000);
    const candidates = [',', '\t', ';', '|'];
    let best = ',';
    let bestScore = 0;
    for (const d of candidates) {
      let rows;
      try {
        rows = parse(sample, d).slice(0, 20);
      } catch (e) {
        continue;
      }
      if (rows.length === 0) continue;
      const counts = rows.map((r) => r.length);
      const first = counts[0];
      if (first < 2) continue;
      const consistent = counts.filter((c) => c === first).length;
      const score = first * consistent;
      if (score > bestScore) {
        bestScore = score;
        best = d;
      }
    }
    return best;
  }

  /** 末尾などの完全に空の行を取り除く */
  function dropEmptyRows(rows) {
    return rows.filter((r) => !(r.length === 1 && r[0] === ''));
  }

  CT.csv = { parse, stringify, escape, detectDelimiter, dropEmptyRows };
})();
