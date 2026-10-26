# AI-eszközhasználat

Ez a mappa dokumentálja, hogyan készült az alkalmazás AI-eszközökkel ([#15](https://github.com/gchovanyecz/test/issues/15)). Az időrendi napló a [prompt-log.md](prompt-log.md) fájlban van, a session-átiratok a [sessions](sessions/) mappába kerülnek.

## Eszközök

| Eszköz | Mire |
|---|---|
| **Claude Code** (Claude desktop app, Code fül), **Claude Opus 5.5** | fő AI: tervezés, a backend nagy része, minden PR átnézése, végső ellenőrzés |
| **Claude Code segédek** (subagentek, saját git worktree-ben), **Claude Haiku 5.5** | egymástól független jegyek párhuzamosan: frontend (#9–#12), Docker (#13), README (#14), Swagger (#7), CI (#16) |
| **Context7** | friss library-dokumentáció (pl. springdoc verzió a Spring Boot 4-hez) |
| **`gh` CLI** | GitHub issue-k, PR-ok, CI-futások |
| **Claude beépített böngészője** | a kész alkalmazás végigkattintása a végső ellenőrzésnél |

## Munkafolyamat

1. **Feladat → jegyek.** A feladatleírásból az AI 16 GitHub issue-t és egy áttekintő jegyet (#17) készített. Mielőtt bármi elkészült volna, átnéztem őket, és a nyitott kérdéseket („Döntendő”) én döntöttem el.
2. **Jegyenként branch és PR.** Minden jegy külön branch-en (`feature/<szám>-<név>`) és külön pull requestben készült, `Closes #<szám>` hivatkozással. Összesen 16 PR.
3. **Párhuzamosítás.** A fő AI a jegyek függőségei alapján döntötte el, mi mehet párhuzamosan. A független jegyeket segédekre bízta, mindegyiket saját git worktree-ben, pontos fájlhatárokkal, előre kiosztott portokkal és a backend API rögzített formátumával.
4. **Ellenőrzés több szinten.**
   - A segédek munkáját a fő AI átnézte: a teszteket és a buildet maga is lefuttatta, a lényeges kódot átolvasta.
   - Minden PR-t én néztem át és engedtem be.
   - A végén a fő AI egy tiszta klónból elindította az egészet, és böngészőben végigkattintotta.
5. **Naplózás.** Minden lépés bekerült a [prompt-log.md](prompt-log.md) fájlba: mit kértem, mit csinált az AI, mit döntöttem, és mi ment félre.

## Mire használtam az AI-t, és mire nem

- **AI:** jegyek megfogalmazása, kód és tesztek írása, verziók ellenőrzése a dokumentációban, Docker- és CI-konfiguráció, dokumentáció, élő ellenőrzések.
- **Én:**
  - Az üzleti és technikai döntések:
    - foglalási szabályok (15 perces rács, 15 perc–8 óra);
    - az admin végpontok külön URL-en;
    - opcionális foglalási cím;
    - saját CSS komponenskönyvtár helyett;
    - Java 25, Node 24.
  - A munkamódszer: jegyenként külön PR, és minden PR átnézése a beolvasztás előtt.

## Hibák, amiket a folyamat elkapott

A napló ezeket részletesen leírja; itt csak a lényeg:

| Hiba | Hogyan derült ki | Javítás |
|---|---|---|
| Párhuzamos foglaláskor a vesztes kérések 500-at kaptak 409 helyett (PostgreSQL deadlock az exclusion constraint ellenőrzésekor) | az új párhuzamos teszt 3 futásból 2-szer elbukott | a tárgyaló sorának zárolása (`SELECT … FOR UPDATE`) – [#30](https://github.com/gchovanyecz/test/pull/30) |
| Egy teszt 404-et várt ott, ahol az alkalmazás valójában 405-öt ad | élő `curl`-ellenőrzés | a teszt a valódi viselkedést ellenőrzi |
| A segéd munkakönyvtára véletlenül bekerült egy commitba | a PR fájllistájának ellenőrzése | javító commit, `.claude/` a `.gitignore`-ban; a segédek ezután csak név szerint commitolhattak |
| Hibás JSON-ra angol hibaüzenet ment a felhasználónak | élő ellenőrzés | magyar üzenet |
| A legfrissebb Angular újabb Node-ot kért | a segéd jelezte, és nem kerülte meg | döntésem után Node 24 nvm-mel |

## Számok

- 17 GitHub issue, 16 pull request.
- **Backend:** 81 automata teszt; **frontend:** 77 teszt.
- A CI minden PR-nál és a `main`-en lefut.

## Session-átiratok

A Claude Code session teljes átiratát a Claude Code `/export` parancsával lehet menteni. Az exportált fájl a [sessions](sessions/) mappába kerül.
