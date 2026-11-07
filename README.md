# ConverterTools

https://tools.web100tsukuru.com/ のソースです。開発やデータ作業でよく使う変換ツールを集めたサイトで、「webサイト100個作る人」の No.001 です。

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
- **Render の Static Site** で公開しています。`node scripts/build.mjs` で `public/` から `dist/` を作り、それを配ります。
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
Dockerfile, nginx/                    旧ドメイン convertertools.jp 用（下記）
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

以前は convertertools.jp で、Render の Docker サービス（`Dockerfile` の nginx）として動かしていました。ドメインは更新しないので、期限が切れたら `Dockerfile` と `nginx/` と Render の旧サービスは消してかまいません。
