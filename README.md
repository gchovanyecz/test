# Tárgyalófoglaló

Iroda tárgyalóinak foglalását kezelő alkalmazás (Spring Boot backend, Angular frontend, PostgreSQL).

A fejlesztés folyamatban van, a feladatok a [GitHub issue-kban](https://github.com/gchovanyecz/test/issues/17) követhetők.

## Backend fejlesztői futtatás

Előfeltétel: JDK 25 és Docker.

```bash
cd backend
./mvnw spring-boot:test-run   # elindítja az alkalmazást egy ideiglenes PostgreSQL konténerrel (Testcontainers)
./mvnw verify                 # build és tesztek
```

Health check: http://localhost:8080/actuator/health
