rm -rf electron-src/electron-vaadin
mkdir electron-src/electron-vaadin
cp -r build/install/electron-vaadin/* electron-src/electron-vaadin
cd electron-src
node_modules/.bin/electron .
cd ..
