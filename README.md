# ConverterTools.jp

https://convertertools.jp/ のソースです。開発やデータ作業でよく使う変換ツールを集めたサイトです。

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
- サーバーは HTML・CSS・JS を配るだけの **nginx**（`Dockerfile`）です。Render の Docker サービスで動いています。
- 共通のヘッダー・フッターは `public/_partials/` にあり、nginx の SSI で各ページに差し込みます。
- Excel の読み書きは [SheetJS](https://sheetjs.com/)、PDF の読み取りは [pdf.js](https://mozilla.github.io/pdf.js/) を `public/assets/vendor/` に置いて使っています。

```
public/
  index.html, date-tools.html, ...   各ページ
  _partials/                          共通の head・ヘッダー・フッター
  assets/css/site.css                 デザイン
  assets/js/*.js                      各ツールの処理
  assets/vendor/                      SheetJS・pdf.js
nginx/                                nginx の設定
tests/e2e.js                          ブラウザで全ツールを動かすテスト
```

## 手元で動かす

```bash
docker build -t convertertools . && docker run --rm -p 8080:10000 convertertools
```

http://localhost:8080 を開きます。

## テスト

サイトを起動した状態で：

```bash
npm i -D playwright && npx playwright install chromium
LANG=C.UTF-8 BASE_URL=http://localhost:8080 node tests/e2e.js
```

## 更新するとき

CSS や JS を書き換えたら、HTML と `_partials/head.html` の `?v=20261107` の数字を上げてください（ブラウザに新しいファイルを読ませるため）。
