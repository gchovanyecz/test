# AI-eszközhasználat

Ez a mappa dokumentálja, hogyan készült az alkalmazás AI-eszközökkel (lásd [#15](https://github.com/gchovanyecz/test/issues/15)).

## Eszközök

- **Claude Code** (Claude desktop app, Code fül), modell: Claude Opus 5.5
- Kiegészítő eszközök a Claude Code-on belül: Context7 (friss library-dokumentáció), `gh` CLI (GitHub issue-k, PR-ok)

## Munkafolyamat

1. **Feladat → jegyek:** a feladatleírásból az AI GitHub issue-kat készített, ezeket én néztem át, a nyitott kérdéseket én döntöttem el.
2. **Jegyenként fejlesztés:** minden jegy külön branch-en (`feature/<szám>-<név>`) és külön pull requestben készül, `Closes #<szám>` hivatkozással.
3. **Review:** minden PR-t átnézek, mielőtt a `main`-be kerül.
4. **Naplózás:** minden lépés bekerül a [prompt-log.md](prompt-log.md) fájlba: mit kértem, mit javasolt az AI, mit fogadtam el és mit változtattam.

## Mire használtam az AI-t, és mire nem

- **Igen:** jegyek megfogalmazása, projektváz generálása, kód és tesztek írása, verziók ellenőrzése a dokumentációban.
- **Nem:** az üzleti döntéseket (időrács, admin végpontok, foglalás címe, UI-könyvtár) én hoztam meg, az AI csak javaslatot tett.

## Session-átiratok

A Claude Code session-átiratokat a `/export` paranccsal exportálom a [sessions](sessions/) mappába.
