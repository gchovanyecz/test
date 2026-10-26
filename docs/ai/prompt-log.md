# Prompt-napló

Időrendi napló az AI-val végzett munkáról. Minden bejegyzés: mit kértem, mit csinált vagy javasolt az AI, és mit döntöttem.

---

## 1. GitHub-hozzáférés beállítása (2026-10-25)

**Prompt:** „csak ehhez a projekthez állítsuk be a github-omat.”

**AI:**
- Git repót inicializált, és a repo saját beállításában adta meg a nevet, az e-mailt és az SSH kulcsot. A globális (céges) beállításokhoz nem nyúlt.
- Először új SSH kulcsot generált. Miután megmutattam a GitHubon lévő kulcs ujjlenyomatát, megtalálta a hozzá tartozó helyi kulcsot, átállt rá, és törölte a fölöslegeset.

**Döntésem:** név `gchovanyecz`, e-mail `gchovanyecz@gmail.com`, privát `test` repo (ezt a GitHub webes felületén hoztam létre).

---

## 2. Jegyek készítése a feladatleírásból (2026-10-26)

**Prompt:** „Kaptam egy ilyen feladatot, készíts belőle jegyeket github-on, végig szeretném nézni a jegyeket és majd csak utána nekiállni a fejlesztésnek.” Ezt követte a teljes feladatleírás.

**AI:**
- 16 jegyet írt (backend, frontend, Docker, README, AI-dokumentáció, opcionális CI), címkékkel és függőségekkel, valamint egy áttekintő jegyet (#17).
- Szakmai javaslatai:
  - az ütközést félig nyitott intervallumként `[start, end)` kezeli;
  - a párhuzamos kérések ellen PostgreSQL exclusion constraint véd;
  - az admin végpontok `/api/admin` alá kerülnek;
  - az indítás két parancsból áll.
- A nyitott kérdéseket „Döntendő” szakaszokba gyűjtötte.

---

## 3. Jegyek átnézése és döntések (2026-10-26)

**Prompt:** „nézd át a jegyeket, megvan minden ahhoz hogy elkezdhesd a fejlesztést?”

**AI:** az átnézés során hiányokat talált:
- nincs `GET /api/bookings/{id}` végpont, pedig a `Location` fejléc erre mutat;
- az időrács szabálya nincs pontosan leírva;
- a „nem lehet múltbeli” szabályt csak cserélhető `Clock`-kal lehet megbízhatóan tesztelni;
- hiányoznak tesztesetek;
- a frontend portja nincs eldöntve.

**Döntéseim** (részben a GitHub issue-kban, kommentként):
- #2: a foglalásnak legyen opcionális címe
- #4: legalább 15 perc, legfeljebb 8 óra, 15 perces időrács
- #5: a törlés admin funkció, a listázás publikus
- #9: saját CSS, komponenskönyvtár nélkül
- Git: jegyenként külön branch és PR

Az AI ezek alapján frissítette a jegyeket és az áttekintő jegyet.

---

## 4. #1 Backend projektváz (2026-10-26)

**Prompt:** „akkor induljon a fejlesztés”

**AI:**
- Ellenőrizte a gépen lévő eszközöket. Csak Java 17 volt telepítve, ezért megkérdezte, melyik verziót használjuk.
- A projektvázat a start.spring.io-val generálta: Spring Boot 4.1.2, Java 25, Maven.
- A springdoc verzióját a Context7 dokumentációból és a Maven Centralból ellenőrizte. A Boot 4-hez a 3.x ág kell, a legfrissebb a 3.2.0.
- A teszteléshez rögzített Postgres image-et állított be (`postgres:18-alpine`) a `latest` helyett, hogy a build megismételhető legyen.
- Ellenőrizte, hogy a build és a teszt lefut, az alkalmazás elindul, és a `/actuator/health` UP választ ad.

**Döntésem:** Java 25 (legfrissebb LTS), brew-val telepítve, csak ehhez a projekthez.
