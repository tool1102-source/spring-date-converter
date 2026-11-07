// public/ を、そのまま配れる完成品として dist/ に書き出す。
//
// 各ページは共通部品（public/_partials/）を nginx の SSI の書き方で読み込んでいる。
//   <!--# set var="title" value="..." -->
//   <!--# include virtual="/_partials/head.html" -->
//   <!--# echo var="title" -->
// 静的サイトのホスティング（Render の Static Site）では SSI が動かないので、
// ここで埋め込んでおく。外部のパッケージは使わない（Node.js だけで動く）。
//
// 使い方： node scripts/build.mjs
//         BASE_PATH=/tools node scripts/build.mjs   … /tools/ の下に置くとき
//
// BASE_PATH を付けると、ページの中の "/date-tools" や "/assets/..." のような
// サイト内のリンク・部品の場所の頭に、その文字を足して書き出す。
// （web100tsukuru.com/tools/ に置くときは、web100tsukuru の Dockerfile がこれを使う）

import { cp, mkdir, readFile, readdir, rm, writeFile } from 'node:fs/promises';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const src = path.join(root, 'public');
const out = path.join(root, 'dist');

const BASE = (process.env.BASE_PATH || '').replace(/\/+$/, '');
if (BASE && !/^(\/[a-z0-9_-]+)+$/i.test(BASE)) throw new Error(`BASE_PATH の書き方が正しくありません：${BASE}（例：/tools）`);

// href="/..." src="/..." の頭に BASE を足す（"//" で始まる外部の URL や https:// は対象外）
const withBaseHtml = (html) => (BASE ? html.replace(/(\s(?:href|src|action)=")\/(?!\/)/g, `$1${BASE}/`) : html);
// JS の中の '/assets/...' の頭に BASE を足す
const withBaseJs = (js) => (BASE ? js.replace(/(['"`])\/assets\//g, `$1${BASE}/assets/`) : js);

const DIRECTIVE = /<!--#\s*(\w+)((?:\s+\w+="[^"]*")*)\s*-->/g;
const escapeHtml = (s) => s.replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[c]);

function params(text) {
  const p = {};
  for (const m of text.matchAll(/(\w+)="([^"]*)"/g)) p[m[1]] = m[2];
  return p;
}

async function render(html, vars, depth = 0) {
  if (depth > 5) throw new Error('部品の読み込みが深すぎます（循環していませんか）');
  let result = '';
  let last = 0;
  for (const m of html.matchAll(DIRECTIVE)) {
    result += html.slice(last, m.index);
    last = m.index + m[0].length;
    const [, command, rest] = m;
    const p = params(rest);
    if (command === 'set') {
      vars[p.var] = p.value;
    } else if (command === 'echo') {
      if (!(p.var in vars)) throw new Error(`変数 ${p.var} が設定されていません`);
      result += escapeHtml(vars[p.var]);
    } else if (command === 'include') {
      const file = path.join(src, p.virtual || p.file);
      result += await render(await readFile(file, 'utf8'), vars, depth + 1);
    } else {
      throw new Error(`使えない命令です：${command}`);
    }
  }
  return result + html.slice(last);
}

await rm(out, { recursive: true, force: true });
await mkdir(out, { recursive: true });

const entries = await readdir(src, { withFileTypes: true });
let pages = 0;
for (const e of entries) {
  if (e.name === '_partials') continue;
  const from = path.join(src, e.name);
  const to = path.join(out, e.name);
  if (e.isFile() && e.name.endsWith('.html')) {
    const html = await render(await readFile(from, 'utf8'), {});
    if (html.includes('<!--#')) throw new Error(`${e.name} に処理されていない命令が残っています`);
    await writeFile(to, withBaseHtml(html));
    pages++;
  } else {
    await cp(from, to, { recursive: true });
  }
}

// 自分で書いた JS（ライブラリ以外）の中の部品の場所を直す
const jsDir = path.join(out, 'assets', 'js');
for (const name of await readdir(jsDir)) {
  if (!name.endsWith('.js')) continue;
  const file = path.join(jsDir, name);
  await writeFile(file, withBaseJs(await readFile(file, 'utf8')));
}
console.log(`dist/ に書き出しました（ページ ${pages} 件${BASE ? `、置き場所 ${BASE}/` : ''}）`);
