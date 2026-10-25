# ADR-006: Hexagonal boundary for Colombian open data

**Status:** Accepted for the new Open Data context; gradual migration of legacy modules pending.  
**Date:** 2026-10-24

## Context
UniRoom currently mixes web controllers and persistence access in several pre-existing modules. A full migration at once risks breaking reservation and waiting-list flows. The assignment also needs Colombian open data integrations and analytical queries.

## Decision
We introduce a bounded context named **Colombian Educational Open Data** with domain ports (`OpenDataPort`, `AnalyticsPort`), application use cases (`OpenDataService`, `AnalyticsService`), REST driving adapter (`DatosColombiaController`), and infrastructure adapters (`SocrataHttpAdapter`, `DuckDbAnalyticsAdapter`). Constructor-based dependency injection is used in both use cases. Spring configuration composes the adapters. Existing reservation and waitlist flows remain unchanged as a separate **Reservations** context to be migrated incrementally.

Two public Colombian datasets are obtained from the official `datos.gov.co` Socrata API: national higher education institutions (`n5yy-8nav`) and university enrolment by municipality (`y9ga-zwzy`). They are separate official public datasets, hosted by the same API platform; the implementation does **not** claim two independent providers.

The circuit breaker has CLOSED, OPEN and HALF_OPEN states, with a three-failure threshold and 30-second reset; HTTP retries run up to three times with exponential delay (250ms, 500ms). DuckDB executes two queries: descriptive column-completeness statistics and grouping by geographic/institution categories.

## Consequences
**Benefits:** testable application ports, independent remote adapters, clearer dependency direction, failures isolated from core reservations, embedded analytical SQL and an accessible UI.  
**Trade-offs:** data labels vary by dataset; external availability and API field schema must be checked, and retries can increase response latency. The circuit breaker currently observes one failure per exhausted retry group. In-memory circuit state resets at restart.  
**Not yet completed:** migrating all historical reservation files into the new context, running CI in the remote GitHub account, and obtaining live screenshots/evidence.
