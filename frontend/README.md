# Tárgyalófoglaló – frontend (`room-booking-ui`)

Angular alkalmazás (standalone komponensek, signals, routing), a backend (`../backend`) REST API-ját használja.

## Előfeltétel: Node 24

A projekt **Node 24**-et igényel (az Angular CLI 22 engine-követelménye: `^22.22.3 || ^24.15.0 || >=26.0.0`; a `.nvmrc` a `24`-es ágat rögzíti).

```bash
cd frontend
nvm use        # a .nvmrc alapján Node 24-re vált (nvm install 24, ha még nincs telepítve)
npm install
```

## Fejlesztői futtatás (proxy-val)

A `/api` hívásokat a dev server a `proxy.conf.json` szerint a backendre (`http://localhost:8080`) továbbítja, így a böngészőben nincs CORS-probléma. A backendet előbb el kell indítani (lásd a repo gyökér README-jét).

```bash
npm start      # ng serve, http://localhost:4200
```

## Tesztek

```bash
npm test          # egyszeri futtatás, CI mód (--watch=false)
npm run test:watch # watch mód fejlesztéshez
```

## Build

```bash
npm run build     # production build a dist/ mappába
```
