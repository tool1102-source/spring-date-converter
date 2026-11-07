/*
 * ブラウザで全ツールを実際に動かして確かめるテスト。
 * 使い方：nginx などでサイトを起動してから
 *   LANG=C.UTF-8 BASE_URL=http://localhost:8090 node tests/e2e.js
 * （Playwright が必要：npm i -D playwright。
 *   LANG が UTF-8 でないと、Chromium が日本語のダウンロード名を "download" にしてしまう）
 */
const { chromium } = require('playwright');
const path = require('path');
const fs = require('fs');
const os = require('os');
const crypto = require('crypto');
const XLSX = require('../public/assets/vendor/xlsx-0.20.3.full.min.js');

const BASE = process.env.BASE_URL || 'http://localhost:8090';
const SHOTS = process.env.SHOTS_DIR || '';
const tmp = fs.mkdtempSync(path.join(os.tmpdir(), 'ct-e2e-'));

let failures = 0;
let passes = 0;
function check(name, cond, detail = '') {
  if (cond) {
    passes++;
    console.log('  ok   ' + name);
  } else {
    failures++;
    console.log('  FAIL ' + name + (detail ? '\n       ' + String(detail).slice(0, 400) : ''));
  }
}
const eq = (name, actual, expected) => check(name, actual === expected, `expected: ${JSON.stringify(expected)}\n       actual:   ${JSON.stringify(actual)}`);

async function typeInto(page, sel, value) {
  await page.fill(sel, value);
  await page.waitForTimeout(350);
}

(async () => {
  const browser = await chromium.launch();
  const ctx = await browser.newContext({ acceptDownloads: true, timezoneId: 'America/New_York', locale: 'ja-JP' });
  const page = await ctx.newPage();
  const errors = [];
  page.on('pageerror', (e) => errors.push(e.message));
  page.on('console', (m) => {
    if (m.type() !== 'error') return;
    const url = m.location().url || '';
    if (/googlesyndication/.test(url) || url.endsWith('/nope') || url.includes('/_partials/')) return; // 広告の遮断と、わざと開いた 404
    errors.push(`${m.text()} (${url})`);
  });
  // 広告スクリプトはテストでは読まない
  await ctx.route(/googlesyndication|doubleclick|google-analytics/, (r) => r.abort());

  const pages = ['/', '/date-tools', '/json-csv', '/excel-tools', '/pdf-tools', '/string-tools', '/text-tools'];

  console.log('\n[全ページ]');
  for (const p of pages) {
    const res = await page.goto(BASE + p);
    eq(`${p} が表示される`, res.status(), 200);
    const title = await page.title();
    check(`${p} のタイトル`, title.endsWith('| ConverterTools') && title.length > 20, title);
    const canonical = await page.getAttribute('link[rel=canonical]', 'href');
    eq(`${p} の canonical`, canonical, 'https://tools.web100tsukuru.com' + p);
  }
  eq('存在しないページは 404', (await page.goto(BASE + '/nope')).status(), 404);
  check('404 画面に案内がある', (await page.textContent('h1')).includes('見つかりません'));
  const r301 = await page.goto(BASE + '/date-tools.html');
  check('.html 付きは付かない URL へ転送', r301.request().redirectedFrom() !== null && new URL(page.url()).pathname === '/date-tools', page.url());
  eq('部品ファイルは直接見えない', (await page.goto(BASE + '/_partials/head.html')).status(), 404);
  await page.goto(BASE + '/ads.txt');
  check('ads.txt', (await page.textContent('body')).includes('pub-4550031417169253'));

  // ---------------- 日付 ----------------
  console.log('\n[日付・Epoch]');
  await page.goto(BASE + '/date-tools');
  await typeInto(page, '#epoch-in', '1716168600');
  check('秒（10桁）を日本時間に', (await page.textContent('#epoch-out')).includes('2024/05/20 10:30:00'), await page.textContent('#epoch-out'));
  check('単位を自動判別した旨を表示', (await page.textContent('#epoch-msg')).includes('秒'));
  await typeInto(page, '#epoch-in', '1716168600123');
  check('ミリ秒（13桁）', (await page.textContent('#epoch-out')).includes('2024/05/20 10:30:00.123'));
  check('ISO 8601（UTC）', (await page.textContent('#epoch-out')).includes('2024-05-20T01:30:00.123Z'));
  check('端末が日本以外なら端末の時刻も出す', (await page.textContent('#epoch-out')).includes('America/New_York'));
  await typeInto(page, '#epoch-in', 'abc');
  check('数字以外はエラー', (await page.textContent('#epoch-msg')).includes('数字'));

  await typeInto(page, '#dt-in', '2024/05/20 10:30:00');
  check('日本時間の日時 → Epoch 秒', (await page.textContent('#dt-out')).includes('1716168600'), await page.textContent('#dt-out'));
  await typeInto(page, '#dt-in', '2024-05-20T01:30:00Z');
  check('Z 付きは UTC として計算', (await page.textContent('#dt-out')).includes('1716168600000'));
  await typeInto(page, '#dt-in', '2024年5月20日 10時30分');
  check('日本語の日時も読める', (await page.textContent('#dt-out')).includes('1716168600'));
  await page.selectOption('#dt-zone', 'UTC');
  await typeInto(page, '#dt-in', '2024/05/20 01:30:00');
  check('UTC を選んだとき', (await page.textContent('#dt-out')).includes('1716168600'));
  await typeInto(page, '#dt-in', '2024/02/30');
  check('ありえない日付はエラー', (await page.textContent('#dt-msg')).includes('読み取れません'));

  await page.fill('#fmt-to', 'yyyy年M月d日（E）');
  await typeInto(page, '#fmt-in', '2024/5/20 9:05\n2025-01-01\nだめな行');
  eq('フォーマット変換（自動判別・複数行）', await page.inputValue('#fmt-out'), '2024年5月20日（月）\n2025年1月1日（水）\n（変換できません）');
  await page.fill('#fmt-from', 'dd/MM/yyyy');
  await page.fill('#fmt-to', 'yyyy-MM-dd');
  await typeInto(page, '#fmt-in', '20/05/2024');
  eq('入力の書き方を指定した変換', await page.inputValue('#fmt-out'), '2024-05-20');

  // ---------------- JSON ⇄ CSV ----------------
  console.log('\n[JSON ⇄ CSV]');
  await page.goto(BASE + '/json-csv');
  await typeInto(page, '#jc-in', JSON.stringify([{ id: 1, name: '山田, 太郎', user: { age: 30 }, tags: ['a', 'b'] }, { id: 2, memo: '改行\nあり', ok: true }]));
  eq('JSON → CSV（展開・引用符・改行）', await page.inputValue('#jc-out'), 'id,name,user.age,tags,memo,ok\n1,"山田, 太郎",30,"[""a"",""b""]",,\n2,,,,"改行\nあり",true');
  await typeInto(page, '#jc-in', '[{"a": 1,}]');
  check('壊れた JSON は場所つきでエラー', /行目/.test(await page.textContent('#jc-msg')), await page.textContent('#jc-msg'));

  await page.click('#jc-mode button[data-value="c2j"]');
  await typeInto(page, '#jc-in', 'id,name,zip,note\n1,"山田, 太郎",0012,"1行目\n2行目"\n2,佐藤,1000000,"引用符""あり"""');
  const parsed = JSON.parse(await page.inputValue('#jc-out'));
  eq('CSV → JSON 件数', parsed.length, 2);
  eq('引用符内のカンマ', parsed[0].name, '山田, 太郎');
  eq('先頭 0 は文字のまま', parsed[0].zip, '0012');
  eq('数値に変換', parsed[1].zip, 1000000);
  eq('引用符内の改行', parsed[0].note, '1行目\n2行目');
  eq('"" のエスケープ', parsed[1].note, '引用符"あり"');
  await typeInto(page, '#jc-in', 'a\tb\n1\t2');
  eq('TSV を自動判別', JSON.parse(await page.inputValue('#jc-out'))[0].b, 2);

  const dl = page.waitForEvent('download');
  await page.click('#jc-mode button[data-value="j2c"]');
  await typeInto(page, '#jc-in', '[{"名前":"テスト"}]');
  await page.click('#jc-download');
  const csvBuf = fs.readFileSync(await (await dl).path());
  check('CSV ダウンロードに BOM が付く', csvBuf[0] === 0xef && csvBuf[1] === 0xbb && csvBuf[2] === 0xbf);

  // ---------------- Excel ----------------
  console.log('\n[Excel ⇄ CSV]');
  const wb = XLSX.utils.book_new();
  const ws = XLSX.utils.aoa_to_sheet([
    ['名前', '', '日付', '計'],
    ['山田', '', new Date(Date.UTC(2024, 4, 20)), null],
    ['', 'B列だけ', null, null],
  ], { cellDates: true });
  ws['C2'].z = 'yyyy/mm/dd';
  ws['D2'] = { t: 'n', f: '1+2', v: 3 };
  ws['!ref'] = 'A1:D3';
  XLSX.utils.book_append_sheet(wb, ws, '売上');
  XLSX.utils.book_append_sheet(wb, XLSX.utils.aoa_to_sheet([['x']]), '別シート');
  // 日本語のファイル名でも動くか（パスではなく名前で渡す）
  const xlsxFile = { name: 'テスト.xlsx', mimeType: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', buffer: Buffer.from(XLSX.write(wb, { type: 'array', bookType: 'xlsx' })) };

  await page.goto(BASE + '/excel-tools');
  await page.setInputFiles('#x2c-drop input', xlsxFile);
  await page.waitForSelector('#x2c-result:not([hidden])', { timeout: 15000 });
  check('シートを選べる', (await page.$$eval('#x2c-sheet option', (o) => o.map((x) => x.value))).join() === '売上,別シート');
  const dl2 = page.waitForEvent('download');
  await page.click('#x2c-download');
  const d2 = await dl2;
  eq('CSV のファイル名', d2.suggestedFilename(), 'テスト_売上.csv');
  const csvText = fs.readFileSync(await d2.path(), 'utf8').replace(/^﻿/, '');
  eq('Excel → CSV（空セルで列がずれない・数式は結果・日付は表示どおり）', csvText, '名前,,日付,計\r\n山田,,2024/05/20,3\r\n,B列だけ,,');

  const sjis = Buffer.from(require('child_process').execSync(`python3 -c "import sys; sys.stdout.buffer.write('社員番号,氏名\\r\\n00123,山田\\r\\n456,佐藤\\r\\n'.encode('shift_jis'))"`));
  await page.setInputFiles('#c2x-drop input', { name: '社員.csv', mimeType: 'text/csv', buffer: sjis });
  await page.waitForSelector('#c2x-result:not([hidden])');
  check('Shift_JIS を判別', (await page.textContent('#c2x-info')).includes('Shift_JIS'), await page.textContent('#c2x-info'));
  check('文字化けしない', (await page.textContent('#c2x-table')).includes('山田'));
  const dl3 = page.waitForEvent('download');
  await page.click('#c2x-download');
  const outWb = XLSX.read(fs.readFileSync(await (await dl3).path()));
  const outWs = outWb.Sheets[outWb.SheetNames[0]];
  eq('先頭 0 の値は文字のまま', outWs['A2'].v, '00123');
  eq('数字は数値として入る', outWs['A3'].v, 456);
  eq('日本語の値', outWs['B2'].v, '山田');

  // ---------------- PDF ----------------
  console.log('\n[PDF]');
  const pdfMaker = await ctx.newPage();
  await pdfMaker.setContent('<html><body style="font-family:sans-serif"><p>1ページ目の日本語テキスト</p><p style="page-break-before:always">2ページ目の最後の行</p></body></html>');
  const pdfPath = path.join(tmp, 'sample.pdf');
  await pdfMaker.pdf({ path: pdfPath, format: 'A4' });
  await pdfMaker.close();
  await page.goto(BASE + '/pdf-tools');
  await page.setInputFiles('#p2t-drop input', pdfPath);
  await page.waitForSelector('#p2t-result:not([hidden])', { timeout: 20000 });
  const pdfText = await page.inputValue('#p2t-out');
  check('PDF の 1ページ目の文字', pdfText.includes('1ページ目の日本語テキスト'), pdfText);
  check('PDF の 2ページ目の文字（全ページ読む）', pdfText.includes('2ページ目の最後の行'), pdfText);
  check('ページ数を表示', (await page.textContent('#p2t-file')).includes('2 ページ'));

  await page.evaluate(() => (window.print = () => (window.__printed = document.getElementById('print-area').textContent)));
  const longText = Array.from({ length: 300 }, (_, i) => `${i + 1}行目 長い文章のテスト`).join('\n');
  await page.fill('#t2p-in', longText);
  await page.click('#t2p-make');
  eq('テキスト → PDF：全文を印刷領域に入れる（1ページで切れない）', await page.evaluate(() => window.__printed), longText);

  // ---------------- 文字列 ----------------
  console.log('\n[文字列]');
  await page.goto(BASE + '/string-tools');
  await typeInto(page, '#b64-in', 'こんにちは');
  eq('Base64 エンコード（UTF-8）', await page.inputValue('#b64-out'), '44GT44KT44Gr44Gh44Gv');
  await page.click('#b64-mode button[data-value="dec"]');
  await typeInto(page, '#b64-in', '44GT44KT44Gr44Gh44Gv');
  eq('Base64 デコード', await page.inputValue('#b64-out'), 'こんにちは');
  await typeInto(page, '#b64-in', '%%%');
  check('壊れた Base64 はエラー', (await page.textContent('#b64-msg')).length > 0);
  await typeInto(page, '#url-in', '東京 タワー&page=1');
  eq('URL エンコード', await page.inputValue('#url-out'), encodeURIComponent('東京 タワー&page=1'));
  for (const s of ['', 'password', '日本語のテキスト', 'a'.repeat(1000)]) {
    await typeInto(page, '#hash-in', s);
    await page.waitForTimeout(150);
    eq(`MD5（${s.slice(0, 8) || '空'}）`, await page.textContent('#h-MD5'), crypto.createHash('md5').update(s).digest('hex'));
    eq(`SHA-256（${s.slice(0, 8) || '空'}）`, await page.textContent('#h-SHA-256'), crypto.createHash('sha256').update(s).digest('hex'));
  }
  const uuids = (await page.inputValue('#uuid-out')).split('\n');
  check('UUID を5個', uuids.length === 5 && uuids.every((u) => /^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/.test(u)), uuids.join(' '));

  // ---------------- テキスト ----------------
  console.log('\n[テキスト]');
  await page.goto(BASE + '/text-tools');
  const t = (mode, input) => page.evaluate(([m, s]) => window.CT.text.MODES[m][1](s), [mode, input]);
  eq('全角 → 半角', await t('half', 'ＡＢＣ１２３　！'), 'ABC123 !');
  eq('半角 → 全角', await t('full', 'ABC 123'), 'ＡＢＣ　１２３');
  eq('半角カナ → 全角（濁点・半濁点）', await t('kanaFull', 'ｶﾞｷﾞｸﾞ ﾊﾟﾋﾟ ｳﾞｧｲｵﾘﾝ ｱｲｳ'), 'ガギグ パピ ヴァイオリン アイウ');
  eq('全角カナ → 半角', await t('kanaHalf', 'ガッコウ、パン'), 'ｶﾞｯｺｳ､ﾊﾟﾝ');
  eq('カタカナ → ひらがな', await t('hira', 'カタカナ'), 'かたかな');
  eq('ひらがな → カタカナ', await t('kata', 'ひらがな'), 'ヒラガナ');
  eq('HTML エスケープ', await t('esc', '<a href="x">&</a>'), '&lt;a href=&quot;x&quot;&gt;&amp;&lt;/a&gt;');
  eq('HTML アンエスケープ', await t('unesc', '&lt;b&gt; &amp; &#x3042; &copy;'), '<b> & あ ©');
  eq('重複行を削除', await t('unique', 'a\nb\na'), 'a\nb');
  await typeInto(page, '#tx-in', '👨‍👩‍👧あ\nい');
  eq('絵文字の組み合わせも1文字として数える', await page.textContent('#cnt-chars'), '4');
  eq('行数', await page.textContent('#cnt-lines'), '2');
  await page.click('#tx-modes button[data-value="kata"]');
  check('ボタンで変換の種類を切り替え', (await page.inputValue('#tx-out')).includes('ア'));

  // ---------------- 見た目（スマホ幅で横にはみ出さない） ----------------
  console.log('\n[スマホ表示]');
  const phone = await browser.newContext({ viewport: { width: 375, height: 800 }, deviceScaleFactor: 1 });
  await phone.route(/googlesyndication/, (r) => r.abort());
  const pp = await phone.newPage();
  for (const p of pages) {
    await pp.goto(BASE + p);
    const over = await pp.evaluate(() => document.documentElement.scrollWidth - window.innerWidth);
    check(`${p} がスマホ幅からはみ出さない`, over <= 0, `はみ出し ${over}px`);
  }

  if (SHOTS) {
    fs.mkdirSync(SHOTS, { recursive: true });
    for (const [name, opts] of [['pc', { viewport: { width: 1280, height: 860 } }], ['sp', { viewport: { width: 390, height: 844 } }], ['dark', { viewport: { width: 1280, height: 860 }, colorScheme: 'dark' }]]) {
      const c = await browser.newContext({ ...opts, deviceScaleFactor: 1 });
      await c.route(/googlesyndication/, (r) => r.abort());
      const sp = await c.newPage();
      for (const p of pages) {
        await sp.goto(BASE + p);
        if (p === '/date-tools') {
          await sp.fill('#epoch-in', '1716168600');
          await sp.fill('#fmt-in', '2024/5/20 9:05\n2025-01-01');
        }
        if (p === '/json-csv') await sp.fill('#jc-in', '[{"id":1,"name":"山田","user":{"age":30}},{"id":2,"name":"佐藤"}]');
        if (p === '/text-tools') await sp.fill('#tx-in', 'ＡＢＣ１２３　ｶﾀｶﾅ');
        await sp.waitForTimeout(400);
        await sp.screenshot({ path: path.join(SHOTS, `${name}${p === '/' ? '-home' : p.replace('/', '-')}.png`), fullPage: true });
      }
      await c.close();
    }
  }

  check('ブラウザのエラーが出ていない', errors.length === 0, errors.join('\n'));
  await browser.close();
  console.log(`\n${passes} 件成功 / ${failures} 件失敗`);
  process.exit(failures ? 1 : 0);
})().catch((e) => {
  console.error(e);
  process.exit(1);
});
