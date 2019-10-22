FROM nginx:alpine

COPY nginx.conf /etc/nginx/nginx.conf
COPY dist /usr/share/nginx/html/dist
COPY index.html /usr/share/nginx/html
COPY sitemap.json /usr/share/nginx/html/data/
COPY favicon.ico /usr/share/nginx/html