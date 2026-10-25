# Guía 6 - Entrega de desarrollo UniRoom

This package retains the original UniRoom project and adds a new Hexagonal / DDD bounded context for educational public data, two Socrata datasets, circuit breaker, exponential retries, two DuckDB SQL analyses and an HTML dashboard.

## Start
1. Have Java 21, Maven and the original MySQL environment available.
2. In the project root run `mvn test` (requires dependencies and MySQL as defined in CI).
3. Run `mvn spring-boot:run` using the original project database configuration.
4. Open `http://localhost:8080/datos-colombia.html`.
5. Endpoints: `/api/v1/datos-colombia/fuentes` and `/api/v1/datos-colombia/n5yy-8nav/estadisticas?limit=100`.

## Required remaining evidence
- Screenshots of working dashboard and API data, `mvn test`, and green GitHub Actions.
- Confirm source APIs respond to actual GET from the target computer and have expected schema.
- Make a real GitHub Copilot suggestion, save original suggestion and your correction.
- Word report with team names/codes, answers to four orientation questions, APA 7 citations, GitHub URL and YouTube URL.
- Real 1-3 minute English demo, upload to forum and reference it in the report.

## Honest scope
The existing reservation code is preserved, not fully refactored into the four layers. The new Colombian Open Data module follows the required ports-and-adapters separation. This should be presented as an incremental Strangler Fig refactor, not as a completed total migration of UniRoom.
