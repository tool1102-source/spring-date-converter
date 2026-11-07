# ConverterTools は HTML・CSS・JavaScript だけで動く静的サイト。
# 変換処理はすべて訪問者のブラウザ内で行うので、サーバーはファイルを配るだけの nginx で十分。
FROM nginx:1.27-alpine

# Render は PORT 環境変数（既定 10000）で待ち受けポートを伝えてくる
ENV PORT=10000

# 起動時に ${PORT} を埋め込んで設定ファイルを作る（nginx 公式イメージの仕組み）
COPY nginx/default.conf.template /etc/nginx/templates/default.conf.template
COPY nginx/headers.conf /etc/nginx/headers.conf
RUN rm -f /etc/nginx/conf.d/default.conf

COPY public/ /usr/share/nginx/html/

EXPOSE 10000
