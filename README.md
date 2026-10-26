# Tárgyalófoglaló

Iroda tárgyalóinak foglalását kezelő alkalmazás. A munkatársak megnézik a tárgyalók foglaltságát és új foglalást adnak le, az adminisztrátor pedig törölhet foglalást.

- **Backend:** Spring Boot REST API, PostgreSQL, Flyway migrációk
- **Frontend:** Angular alkalmazás, saját CSS-sel
- **Futtatás:** Docker Compose, egyetlen paranccsal

A feladatok és a döntések a [GitHub issue-kban](https://github.com/gchovanyecz/test/issues/17) követhetők.

## Funkciók

- Tárgyalók listázása és egy tárgyaló lekérdezése (a tárgyalólista fix, öt tárgyaló van)
- Foglalások listázása, szűrés tárgyalóra és időszakra
- Foglalás leadása űrlapon, opcionális címmel; az időtartam, az időrács, a múltbeli kezdés és az ütközés ellenőrzése
- Foglalás törlése az admin oldalon, megerősítő párbeszédablakkal
- Egységes hibaformátum ([RFC 9457](https://www.rfc-editor.org/rfc/rfc9457) `application/problem+json`)
- OpenAPI dokumentáció és Swagger UI

## Technológiák

| Terület | Technológia | Verzió |
|---|---|---|
| Backend nyelv | Java | 25 |
| Backend keretrendszer | Spring Boot (Web MVC, Data JPA, Validation, Actuator) | 4.1.2 |
| API-dokumentáció | springdoc-openapi | 3.2.0 |
| Adatbázis-migráció | Flyway | 12.4.0 |
| Adatbázis | PostgreSQL (Docker image) | 18 |
| Backend tesztelés | JUnit 5, Testcontainers (PostgreSQL) | 2.0.5 (Testcontainers) |
| Frontend keretrendszer | Angular (`@angular/core`, CLI és build) | 22.2.3, 22.2.4 |
| Frontend nyelv | TypeScript | 6.0 |
| Frontend tesztelés | Vitest (`@angular/build:unit-test`) | 5.0.5 |
| Frontend futtatókörnyezet | Node.js | 24 |
| Webszerver (frontend konténer) | nginx | 1.29 |

## Indítás két paranccsal

**Előfeltételek**

- Git
- Docker Compose v2 vagy újabb (Docker Desktop vagy Docker Engine + Compose plugin). Ellenőrzés: `docker compose version`

```bash
git clone git@github.com:gchovanyecz/test.git
cd test
docker compose up --build
```

Az első build a Maven- és npm-függőségek letöltése miatt több percet is igénybe vehet, a későbbi indítások a Docker-rétegek gyorsítótárából jóval gyorsabbak. Amikor a konténerek elindultak, a böngészőben nyisd meg a [http://localhost:4200](http://localhost:4200) címet.

**Leállítás**

```bash
docker compose down      # a konténerek és a hálózat törlődik, az adatbázis megmarad
docker compose down -v   # törli az adatbázis volume-ot is: minden foglalás törlődik
```

Ha a 4200 vagy a 8080 port már foglalt, a `docker compose up` hibát jelez; ilyenkor azt a szolgáltatást kell leállítani, amelyik a portot használja.

## Elérhetőség

| Mi | Cím |
|---|---|
| Frontend | http://localhost:4200 |
| Swagger UI | http://localhost:8080/swagger-ui.html (átirányít: `/swagger-ui/index.html`) |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| Health check | http://localhost:8080/actuator/health (válasz: `{"groups":["liveness","readiness"],"status":"UP"}`) |

Az adatbázis (5432-es port) nincs kiajánlva a host gépre, csak a backend éri el a Docker-hálózaton belül.

## API végpontok

Az alap útvonal `/api`. Az adminisztrációs végpontok az `/api/admin` alatt vannak.

| Metódus | Útvonal | Leírás | Főbb státuszkódok |
|---|---|---|---|
| GET | `/api/rooms` | Tárgyalók listája, név szerint rendezve | 200 |
| GET | `/api/rooms/{roomId}` | Egy tárgyaló | 200, 404 |
| GET | `/api/bookings?roomId&from&to` | Foglalások kezdési idő szerint. Mindhárom paraméter opcionális; `from`/`to` esetén a `[from, to)` időszakkal átfedő foglalásokat adja vissza | 200, 400 (`to` nem `from` után van), 404 (nincs ilyen tárgyaló) |
| GET | `/api/bookings/{bookingId}` | Egy foglalás (a foglaláskor visszaadott `Location` fejléc erre mutat) | 200, 404 |
| POST | `/api/bookings` | Foglalás leadása | 201, 400, 404, 409 |
| DELETE | `/api/admin/bookings/{bookingId}` | **Admin:** foglalás törlése | 204, 404 |

**Nincs authentikáció, szándékosan.** Authentikáció nélkül nem tudjuk, kié egy foglalás, ezért a törlés adminisztrátori művelet, és az `/api/admin` alá került. A listázás publikus marad, mert foglalás előtt látni kell a tárgyalók foglaltságát. Éles környezetben az admin végpontokat védeni kell (pl. Spring Security); ez a projektben nincs megvalósítva.

### Példa: foglalás leadása

```bash
curl -i -X POST http://localhost:8080/api/bookings \
  -H 'Content-Type: application/json' \
  -d '{"roomId":1,"title":"Sprint planning","startTime":"2026-11-03T09:00:00+01:00","endTime":"2026-11-03T10:00:00+01:00"}'
```

A példában szereplő időpont a jövőben van; a múltbeli kezdés 400-as hibát ad.

**201 Created** (a valós válasz, a `Location` fejléc a létrejött erőforrásra mutat):

```http
HTTP/1.1 201
Location: /api/bookings/1
Content-Type: application/json

{"id":1,"roomId":1,"roomName":"Budapest","title":"Sprint planning","startTime":"2026-11-03T08:00:00Z","endTime":"2026-11-03T09:00:00Z","createdAt":"2026-10-26T23:18:57.859078Z"}
```

**409 Conflict**, ha a tárgyaló ebben az idősávban már foglalt (a 09:30–10:30-as kérés átfed az előző, 09:00–10:00-s foglalással):

```http
HTTP/1.1 409
Content-Type: application/problem+json

{"detail":"A tárgyaló ebben az idősávban már foglalt.","instance":"/api/bookings","status":409,"title":"Conflict","conflictingBookingId":1,"conflictingStartTime":"2026-11-03T08:00:00Z","conflictingEndTime":"2026-11-03T09:00:00Z"}
```

**400 Bad Request**, ha a kezdés nem esik a 15 perces időrácsra (a `startTime` 09:07):

```http
HTTP/1.1 400
Content-Type: application/problem+json

{"detail":"A foglalás nem felel meg a foglalási szabályoknak.","instance":"/api/bookings","status":400,"title":"Bad Request","errors":[{"field":"startTime","message":"Az időpontnak 15 perces időrácsra kell esnie (pl. 9:00, 9:15)."}]}
```

**404 Not Found** egy nem létező tárgyalóra (`roomId: 999`):

```json
{"detail":"Nem létezik tárgyaló ezzel az azonosítóval: 999","instance":"/api/bookings","status":404,"title":"Not Found"}
```

### Hibaformátum

Minden hiba `application/problem+json` (RFC 9457). A mezők: `status`, `title`, `detail`, `instance`. Kiegészítő mezők:

- **400:** `errors` tömb, elemei `{field, message}`. Több megszegett szabály esetén mindegyik szerepel.
- **409:** `conflictingBookingId`, `conflictingStartTime`, `conflictingEndTime` az ütköző foglalásról.

## Foglalási szabályok

| Szabály | Részletek |
|---|---|
| Időtartam | legalább 15 perc, legfeljebb 8 óra (`booking.rules.min-duration`, `max-duration`) |
| Időrács | a kezdés és a befejezés 15 percenként esik (:00, :15, :30, :45), másodperc nélkül |
| Nem múltbeli | a kezdés nem lehet a múltban; az aktuális időt egy injektált `Clock` adja |
| Nincs ütközés | ugyanabban a tárgyalóban két foglalás nem fedheti át egymást |
| Félig nyitott intervallum | a foglalás `[kezdés, befejezés)`: a befejezés pillanatát már nem foglalja. A 09:00–10:00 foglalás után a 10:00–11:00 foglalás leadható (a valós tesztben 201-et kapott) |
| Más tárgyaló | más tárgyalóban lévő foglalás nem számít ütközésnek |
| Cím | opcionális, legfeljebb 100 karakter |
| Időformátum | ISO-8601, időzóna-eltolással (pl. `+01:00`) vagy UTC-ben. A válaszban az időpontok UTC-ben, `Z` jelöléssel szerepelnek |

Két foglalás ütközik, ha `létező.kezdés < új.befejezés` **és** `létező.befejezés > új.kezdés`.

## Architektúra

### Backend rétegek

A backend funkció szerinti csomagokra oszlik (`room`, `booking`), mindkettő ugyanazt a rétegzést követi:

| Réteg | Feladat |
|---|---|
| `controller` | HTTP-végpontok; csak DTO-t fogad és ad vissza, sosem entitást |
| `service` | üzleti logika: időszabályok (`BookingTimeRules`), ütközésvizsgálat, tranzakciók |
| `repository` | Spring Data JPA; a foglalások szűrése a `BookingSpecifications`-szel |
| `domain` | JPA-entitások (`Room`, `Booking`); nem hagyják el a backendet |
| `dto` | a kérés- és válaszrekordok |

A `common` csomag a közös részeket tartalmazza: a `config` a `Clock`-ot, az `exception` és a `web` a `GlobalExceptionHandler`-t, amely a kivételeket ProblemDetail válasszá alakítja. A séma Flyway migrációkban van (`db/migration`): tárgyalók, foglalások exclusion constraint-tel, és öt kezdő tárgyaló.

### Miért kétszer védett az ütközés?

1. **Service-szintű ellenőrzés** (`BookingService.create`): érthető 409-et ad, a válaszban az ütköző foglalás adataival.
2. **Adatbázis-szintű exclusion constraint** (`booking_no_overlap`, `EXCLUDE USING gist`, `tstzrange(..., '[)')`, a `btree_gist` extensionnel): párhuzamos kérések esetén is garantálja, hogy nem jön létre átfedő foglalás.

Az egyik nem elég: két kérés egyszerre mehet át a service-ellenőrzésen, és csak az adatbázis állítja meg a másodikat. Az adatbázis hibáját a `GlobalExceptionHandler` ugyanúgy 409-ként adja vissza, csak általános üzenettel, mert az ütköző foglalás adatai ekkor már nem ismertek.

### Frontend

Standalone Angular komponensek, signal-ok és routing. Az útvonalak:

| Útvonal | Oldal |
|---|---|
| `/rooms` | tárgyalók listája |
| `/bookings` | foglalások listája szűrővel, foglalási űrlap (alapértelmezett oldal) |
| `/admin` | foglalások törlése megerősítéssel |

Mappastruktúra (`frontend/src/app`): `core/` az API-szolgáltatások (`HttpClient`), a modellek, a hibainterceptor és a formázó segédfüggvények; `features/` az oldalak; `shared/` az újrahasznosítható elemek (értesítés, betöltési állapot). A stílus saját CSS, komponenskönyvtár nincs.

### Docker topológia

```text
böngésző ──► frontend (nginx, a konténer 80-as portja → host 4200)
                ├── statikus Angular fájlok
                └── /api/* ──► backend (Spring Boot, 8080 → host 8080)
                                  └──► db (PostgreSQL 18, csak belső hálózat)
```

A böngésző csak a 4200-as portot éri el, az nginx továbbítja az `/api` hívásokat a backendnek. Mivel a böngésző és az API ugyanazon a származáson (origin) van, nincs szükség CORS-beállításra. Az nginx a backend címét futásidőben oldja fel, így a backend konténer újraindítása után is működik a proxy.

## Helyi fejlesztés Docker Compose nélkül

A backend futtatásához JDK 25 kell. A `JAVA_HOME` legyen a 25-ös JDK-ra állítva.

**Backend**

```bash
cd backend
./mvnw spring-boot:test-run   # ideiglenes PostgreSQL (Testcontainers) és az alkalmazás a 8080-as porton
./mvnw verify                 # build és tesztek
```

A `spring-boot:test-run` Testcontainers-t használ, ezért működő Docker-daemont igényel. Ha saját PostgreSQL 18 példányt használnál, állítsd be a `DB_URL`, `DB_USERNAME` és `DB_PASSWORD` környezeti változókat (alapértelmezés: `jdbc:postgresql://localhost:5432/roombooking`, `roombooking` / `roombooking`), majd `./mvnw spring-boot:run`.

**Frontend**

```bash
cd frontend
nvm use     # a .nvmrc alapján Node 24
npm ci
npm start   # http://localhost:4200, az /api hívások a 8080-as backendre mennek (proxy)
```

A frontend részletei a [frontend/README.md](frontend/README.md) fájlban vannak.

## Tesztek

A számok a `main` jelenlegi állapotán mért értékek; a tesztek bővülhetnek.

**Backend** (JUnit 5, Spring Boot test, Testcontainers PostgreSQL): **62 teszt, mind zöld** (`./mvnw verify`)

| Terület | Teszt osztály | Mit ellenőriz |
|---|---|---|
| Időszabályok | `BookingTimeRulesTests` | időtartam, időrács, múltbeli kezdés, több megszegett szabály egyszerre, időzóna-eltolás |
| Ütközés | `BookingServiceIntegrationTests` | átfedés elutasítása, egymás utáni foglalás engedélyezése, az adatbázis-constraint neve a hibakezelőnek |
| Adatbázis | `BookingSchemaTests` | átfedő insert közvetlen SQL-lel hibát dob, más tárgyalóban engedélyezett, `start < end` constraint |
| Lekérdezés | `BookingQueryIntegrationTests` | rendezés, szűrés tárgyalóra és időszakra, ismeretlen tárgyaló, törlés után felszabadul az idősáv |
| API | `BookingControllerTests`, `AdminBookingControllerTests`, `RoomControllerTests` | HTTP-státuszok és válaszformátum |
| Hibakezelés | `GlobalExceptionHandlerTests` | ProblemDetail-válaszok |
| Alkalmazás | `RoomBookingApplicationTests` | a Spring-kontextus felépül |

**Frontend** (Vitest): **77 teszt, 11 fájl, mind zöld** (`npm test`). Az API-szolgáltatások, a hibainterceptor, a formázó függvények, a foglalási űrlap validációja és az oldalkomponensek (tárgyalók, foglalások, admin) tesztjei futnak.

Egyszeri futtatás CI-módban: `npm test`. Fejlesztés közben: `npm run test:watch`.

## Projektstruktúra

```text
.
├── backend/                 Spring Boot API (Maven, Java 25)
│   ├── src/main/java/hu/gchovanyecz/roombooking/
│   │   ├── room/            tárgyalók
│   │   ├── booking/         foglalások
│   │   └── common/          konfiguráció, kivételkezelés, hibaválaszok
│   └── src/main/resources/db/migration/   Flyway migrációk
├── frontend/                Angular alkalmazás (és a saját README-je)
├── docs/ai/                 AI-használat dokumentációja és prompt-napló
├── docker-compose.yml       db, backend, frontend
└── README.md
```

## AI-használat

Az alkalmazás AI-eszközökkel (Claude Code) készült. A használat és a munkafolyamat leírása a [docs/ai/README.md](docs/ai/README.md) fájlban, az időrendi prompt-napló a [docs/ai/prompt-log.md](docs/ai/prompt-log.md) fájlban található.
