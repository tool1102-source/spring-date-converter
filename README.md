# ConverterTools

https://web100tsukuru.com/tools/ のソースです。開発やデータ作業でよく使う変換ツールを集めたサイトで、「webサイト100個作る人」の No.001 です。

| ページ | できること |
| --- | --- |
| `/date-tools` | Epoch（UNIX時間）⇄ 日時、日付の書き方の一括変換 |
| `/json-csv` | JSON ⇄ CSV / TSV |
| `/excel-tools` | Excel ⇄ CSV（Shift_JIS 対応） |
| `/pdf-tools` | PDF → テキスト、テキスト → PDF |
| `/string-tools` | Base64、URL エンコード、ハッシュ、UUID |
| `/text-tools` | 全角・半角、カナ、行の整理、文字数カウント |

## しくみ

- 変換はすべて**ブラウザの中の JavaScript**で行います。サーバーにはデータを送りません。
- **web100tsukuru.com の `/tools/` の下**で公開しています。web100tsukuru の `Dockerfile` が、このリポジトリの main の中身を取ってきて `BASE_PATH=/tools node scripts/build.mjs` で書き出し、一緒に配ります。
  - **このリポジトリを更新したら、Render で web100tsukuru を作り直す（Manual Deploy）と反映されます。**
- `node scripts/build.mjs` は `public/` から `dist/` を作ります。`BASE_PATH` を付けると、サイト内のリンクの頭にその文字が付きます。
- 共通のヘッダー・フッターは `public/_partials/` にあり、各ページから SSI の書き方（`<!--# include ... -->`）で読み込んでいます。書き出しのときに埋め込まれます。
- Excel の読み書きは [SheetJS](https://sheetjs.com/)、PDF の読み取りは [pdf.js](https://mozilla.github.io/pdf.js/) を `public/assets/vendor/` に置いて使っています。

```
public/
  index.html, date-tools.html, ...   各ページ
  _partials/                          共通の head・ヘッダー・フッター
  assets/css/site.css                 デザイン
  assets/js/*.js                      各ツールの処理
  assets/vendor/                      SheetJS・pdf.js
scripts/build.mjs                     public/ → dist/ の書き出し
tests/e2e.js                          ブラウザで全ツールを動かすテスト
```

## 手元で動かす

```bash
node scripts/build.mjs && npx serve dist
```

## テスト

サイトを起動した状態で：

```bash
npm i -D playwright && npx playwright install chromium
LANG=C.UTF-8 BASE_URL=http://localhost:3000 node tests/e2e.js
```

## 更新するとき

CSS や JS を書き換えたら、HTML と `_partials/head.html` の `?v=20261107b` の数字を上げてください（ブラウザに新しいファイルを読ませるため）。

## 旧ドメイン（convertertools.jp）について

以前は convertertools.jp で、Render の Docker サービス（nginx）として動かしていました。ドメインは更新しないことにしたので、そのための `Dockerfile` と `nginx/` は 2026-11-07 に消しました（中身は git の履歴に残っています）。
