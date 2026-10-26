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

---

## 5. #2 Adatmodell, migrációk és fix tárgyalólista (2026-10-26)

**Prompt:** „kész beengedtem a #18-at.” (A korábbi megállapodás szerint ezután jön a #2.)

**AI:**
- Három Flyway migrációt írt:
  - `room` tábla, egyedi névvel és pozitív kapacitással;
  - `booking` tábla, `start_time < end_time` ellenőrzéssel és **exclusion constraint**-tel (`btree_gist`, félig nyitott `tstzrange '[)'`);
  - seed 5 tárgyalóval.
- JPA entitásokat (`Room`, `Booking`) és repository-kat készített. A `Room` csak olvasható (nincs setter, nincs publikus konstruktor), mert a tárgyalólista fix.
- Integrációs teszteket írt valódi PostgreSQL-lel (Testcontainers), amelyek ellenőrzik:
  - a seed tárgyalók jelenlétét;
  - az entitás-leképezést;
  - hogy az átfedő foglalást közvetlen SQL insertnél is elutasítja az adatbázis;
  - hogy az egymás utáni foglalások és a más tárgyalóban lévő foglalások megengedettek;
  - a fordított időintervallum elutasítását.
- A tesztkonfigurációt publikussá tette, hogy más csomagok tesztjei is használhassák (ezt az első fordítási hiba mutatta meg).

**Eredmény:** `./mvnw verify` zöld, 7 teszt.

---

## 6. #6 Egységes hibakezelés, párhuzamosan a #9 frontend vázzal (2026-10-26)

**Prompt:** „kész beengedtem a #19-et.”

**AI:**
- A függőségek alapján a #6-ot vette előre, mert a #3–#5 erre épül.
- A frontend vázat (#9) egy külön AI-segédre (subagent) bízta, saját munkapéldányban. A segéd megállt, mert a legfrissebb Angular (22.2.4) Node 22.22.3+ verziót kér, a gépen pedig 22.15.1 van. Az utasítás szerint nem telepített és nem kerülte meg a korlátot, hanem jelentett. A döntés az enyém.
- `GlobalExceptionHandler` (`@RestControllerAdvice`, a `ResponseEntityExceptionHandler` kiterjesztése):
  - minden hiba RFC 9457 `application/problem+json` formában jön vissza;
  - 404: nem létező tárgyaló vagy foglalás (közös `ResourceNotFoundException` ős);
  - 409: ütköző foglalás, a válaszban az ütköző foglalás azonosítójával és idejével;
  - 409: az adatbázis `booking_no_overlap` szabályának sértése (párhuzamos kérések esetére);
  - 400: validációs hiba mezőnkénti `errors` listával; hibás JSON;
  - 500: váratlan hiba, belső részletek nélkül, a részletek a logba kerülnek.
- A hibaüzenetek magyarok, mert a frontend ezeket mutatja a felhasználónak.
- Tesztek: standalone MockMvc egy csak tesztben létező controllerrel. Az első futásnál mind a 7 teszt 404-et kapott, mert a teszt-controllerről lehagyta a `@RestController` annotációt (attól tartott, hogy más tesztek is felszednék). Kiderült, hogy a Spring Boot a tesztosztályok belső osztályait eleve kizárja a szkennelésből, ezért visszatette.

**Eredmény:** `./mvnw verify` zöld, 14 teszt.

---

## 7. #3 Tárgyalók végpont, Node 24 a frontendhez (2026-10-26)

**Prompt:** „átnéztem a #6-ot, és beengedtem. telepítheted a 24-es node verziót, de nvm-et használok, azzal telepítsd.”

**AI:**
- Node 24.22.1-et telepített nvm-mel. Az nvm alapértelmezett verziója (22.15.1) változatlan maradt.
- A frontend segédet (#9) a Node 24-gyel és a legfrissebb Angularral (22.2.4) indította újra. A segéd `frontend/.nvmrc` fájlt is kapott feladatul.
- Közben a #3-at csinálta:
  - `GET /api/rooms` (név szerint rendezve) és `GET /api/rooms/{id}` (404, ha nincs ilyen);
  - `RoomResponse` DTO, `RoomService`, `RoomController` OpenAPI annotációkkal.
- Tesztek: `@WebMvcTest` (200, 404, nem numerikus ID → 400) és integrációs teszt a seed adatokkal.
- Élő ellenőrzés: elindította az alkalmazást, és meghívta a végpontokat. A `/v3/api-docs`-ban megjelenik a két útvonal a `Rooms` tag alatt.

**Eredmény:** `./mvnw verify` zöld, 20 teszt.

**Hiba és javítás:** az AI `git add -A`-val véletlenül commitolta a frontend segéd munkakönyvtárát (`.claude/worktrees/...`, beágyazott git repóként). A commit átírását (force push) a Claude Code jogosultságkezelése nem engedte, ezért egy külön javító commit vette ki a fájlt, és a `.claude/` mappa bekerült a `.gitignore`-ba. Tanulság: a commitok előtt a `git status` kimenetét ellenőrizni kell.

---

## 8. #9 Frontend váz, AI-segéddel (2026-10-26)

**AI (segéd):**
- Angular 22.2.4-gyel, Node 24-en elkészítette a frontend vázat:
  - API service-ek, modellek, dev proxy;
  - navigáció három oldallal;
  - globális hibaértesítés (interceptor és toast);
  - saját CSS változókkal;
  - 10 unit teszt.
- A fő AI átnézte: a teszteket és a buildet maga is lefuttatta, és átnézte a kódot.
- **Megjegyzés:** a segéd egy kisebb modellen (Haiku 5.5) futott, mégis „Claude Opus 5.5” `Co-Authored-By` jelöléssel commitolt, mert a fő AI így kérte. A jelölés ezért pontatlan lehet. Javítani csak force push-sal lehetne, ami nem engedélyezett.
- Nyitott pont a #11-hez: a globális hibaértesítés a 400-as validációs hibáknál is megjelenik, ezt az űrlapnál ki kell kapcsolni, hogy ne legyen dupla üzenet.

---

## 9. #4 Foglalás leadása, párhuzamosan a #10 és #13 segédekkel (2026-10-26)

**Prompt:** „mindkét PR-t beengedtem.”

**AI:**
- Két független jegyet segédekre bízott, mindegyiket saját munkapéldányban:
  - **#13 Docker:** csak új fájlokat érint;
  - **#10 frontend listák:** csak a frontendet érinti.
- A segédek pontos utasítást kaptak, hogy mely fájlokhoz nem nyúlhatnak, és hogy csak a szándékolt fájlokat commitolják (a #3-nál történt véletlen commit tanulsága).
- A #4-et maga csinálta:
  - **`BookingTimeRules`:** az időszabályokat a service ellenőrzi:
    - nem kezdődhet a múltban;
    - 15 perces időrácson kell lennie;
    - a kezdés a befejezés előtt van;
    - legalább 15 perc, legfeljebb 8 óra.
    Minden megszegett szabályt egyszerre jelez, mezőnként. A határértékek az `application.yml`-ből jönnek (`booking.rules.*`), az aktuális időt egy injektált `Clock` adja.
  - **Ütközés:** a `findOverlapping` lekérdezés feltétele `start < új.vége ÉS vége > új.kezdete`. Ütközés esetén 409, az ütköző foglalás adataival. A mentés azonnal flush-ol, hogy a DB exclusion constraint sértése is 409-ként jöjjön vissza.
  - **Hibaüzenet:** a hibás JSON-ra adott válasz angol volt („Failed to read request”). Ezt magyarra cserélte, mert a frontend ezt mutatja a felhasználónak.
- Tesztek:
  - a szabályok rögzített órával (10);
  - a service Mockitóval (5);
  - a controller (7);
  - integrációs tesztek valódi PostgreSQL-lel (3), köztük annak ellenőrzése, hogy a DB-szabály sértésekor a hibaüzenet tartalmazza a constraint nevét.
- Az első futásnál 2 teszt elbukott (Mockito: mock létrehozása egy másik stubbing belsejében), ezért valódi `Room` objektumokra cserélte a mockot.
- Élő ellenőrzés `curl`-lel:
  - sikeres foglalás: 201 és `Location` fejléc;
  - ütközés: 409;
  - egymás utáni foglalás: 201;
  - rácson kívüli időpont: 400;
  - múltbeli időpont: 400;
  - nem létező tárgyaló: 404;
  - hibás dátum: 400.

**Eredmény:** `./mvnw verify` zöld, 45 teszt.

---

## 10. #5 Foglalások listázása és admin törlés, párhuzamosan a #11 és #12 segédekkel (2026-10-26)

**Prompt:** „beengedtem a két PR-t”

**AI:**
- A két frontend jegyet (#11 űrlap, #12 admin nézet) két segédre bízta, pontos fájlhatárokkal, hogy ne ütközzenek. A #11 kapta az interceptor módosítását: egy kapcsolót, amellyel az űrlap kikapcsolhatja a globális hibaértesítést, és a hibákat a mezők alatt mutathatja.
- A #5-öt maga csinálta:
  - `GET /api/bookings`:
    - kezdési idő szerint rendezve;
    - opcionális `roomId`, `from` és `to` szűrés, a [from, to) időszakkal átfedő foglalásokra;
    - nem létező tárgyaló → 404, üres időszak → 400.
  - `GET /api/bookings/{id}`: 404, ha nincs ilyen foglalás.
  - `DELETE /api/admin/bookings/{id}`: külön `AdminBookingController`, `Admin` taggel. 204, illetve 404.
- A szűrés Spring Data Specificationökkel működik.
  - A Spring Data JPA 4 nem garantálja a `null` specification kezelését, ezért az AI a lefordított könyvtárból (`javap`) ellenőrizte, hogy van-e `Specification.unrestricted()`, és azt használta.
  - Az `@EntityGraph` egy lekérdezéssel tölti be a tárgyalót is, így a listázás nem indít foglalásonként külön lekérdezést (N+1).
- Élő ellenőrzés: a 8080-as portot a Docker segéd compose-a foglalta, ezért az alkalmazás a 18080-as porton futott. Így derült ki, hogy a nyilvános útvonalon a `DELETE` valójában 405-öt ad, a teszt viszont 404-et várt. A teszt hamisan ment át, mert csak az admin controller töltődött be. Javítva.

**Eredmény:** `./mvnw verify` zöld, 62 teszt.
