# Skill Registry

**Delegator use only.** Any agent that launches sub-agents reads this registry to resolve compact rules, then injects them directly into sub-agent prompts. Sub-agents do NOT read this registry or individual SKILL.md files.

See `_shared/skill-resolver.md` for the full resolution protocol.

Project: turnos-odontologia (Spring Boot, Gradle wrapper, JUnit 5, hexagonal architecture, JPA with H2 in tests and PostgreSQL as target, team of 5 working by pull request).
Scanned: 41 SKILL.md files (26 in ~/.claude/skills, 9 in ~/.config/opencode/skills, 6 in project .claude/skills). Skipped: skill-registry (self); no sdd-* or _shared found. The 9 opencode skills were duplicates of ~/.claude/skills (first found kept). 31 skills registered.

## User Skills

### Relevant to this stack (use these)

| Trigger | Skill | Path |
|---------|-------|------|
| Adding or changing features in an existing Spring Boot 3/4 project (controllers, validation, ProblemDetail, JPA, config) | java-springboot | C:\Users\zbook\.claude\skills\java-springboot\SKILL.md |
| Building or reviewing a Spring Boot backend: REST layer, services, data access, caching, async | springboot-patterns | C:\Users\zbook\.claude\skills\springboot-patterns\SKILL.md |
| TDD for Spring Boot: features, bug fixes, refactors (MockMvc, Mockito, Testcontainers, JaCoCo) | springboot-tdd | C:\Users\zbook\.claude\skills\springboot-tdd\SKILL.md |
| Adding, refactoring or debugging Java tests with JUnit 5, AssertJ, Mockito | java-junit | C:\Users\zbook\.claude\skills\java-junit\SKILL.md |
| Building features or fixing bugs test-first; red-green-refactor; integration tests | tdd | C:\Users\zbook\.claude\skills\tdd\SKILL.md |
| Designing JPA entities/relationships; fixing Hibernate query, transaction or N+1 problems | jpa-patterns | C:\Users\zbook\.claude\skills\jpa-patterns\SKILL.md |
| Creating repositories, entity relationships, @Query, pagination, auditing, transactions | spring-data-jpa | C:\Users\zbook\.claude\skills\spring-data-jpa\SKILL.md |
| Checking, auditing or fixing a Gradle build (dependencies, performance, structure) | gradle-best-practices | C:\Users\zbook\.claude\skills\gradle-best-practices\SKILL.md |
| PostgreSQL table design, indexes, data types, constraints, migrations | postgres | C:\Users\zbook\.claude\skills\postgres\SKILL.md |

### OpenSpec workflow (project-level)

| Trigger | Skill | Path |
|---------|-------|------|
| Implement tasks of an OpenSpec change ("opsx apply") | openspec-apply-change | .claude\skills\openspec-apply-change\SKILL.md |
| Archive a completed change ("opsx archive") | openspec-archive-change | .claude\skills\openspec-archive-change\SKILL.md |
| Explore ideas before/during a change ("opsx explore") | openspec-explore | .claude\skills\openspec-explore\SKILL.md |
| Propose a new change with all artifacts ("opsx propose") | openspec-propose | .claude\skills\openspec-propose\SKILL.md |
| Sync delta specs to main specs ("opsx sync") | openspec-sync-specs | .claude\skills\openspec-sync-specs\SKILL.md |
| Revise a change's planning artifacts ("opsx update") | openspec-update-change | .claude\skills\openspec-update-change\SKILL.md |

### Foundation / tooling (Active Stack)

| Trigger | Skill | Path |
|---------|-------|------|
| Start or resume project foundation | active-orchestrator | C:\Users\zbook\.claude\skills\active-orchestrator\SKILL.md |
| Generate AGENTS.md / CLAUDE.md from KB and roadmap | agent-instruction | C:\Users\zbook\.claude\skills\agent-instruction\SKILL.md |
| Discovery/market research before the KB | discovery-research | C:\Users\zbook\.claude\skills\discovery-research\SKILL.md |
| Find or install agent skills | find-skill | C:\Users\zbook\.claude\skills\find-skill\SKILL.md |
| Build the 10-file knowledge-base/ | kb-creator | C:\Users\zbook\.claude\skills\kb-creator\SKILL.md |
| Generate or update CHANGES.md | roadmap-generator | C:\Users\zbook\.claude\skills\roadmap-generator\SKILL.md |
| Create or improve skills | skill-creator | C:\Users\zbook\.claude\skills\skill-creator\SKILL.md |
| Research a competitor from a URL | web-scraper | C:\Users\zbook\.claude\skills\web-scraper\SKILL.md |

### Not applicable to this stack (listed for completeness)

| Trigger | Skill | Path |
|---------|-------|------|
| WCAG 2.2 accessibility audit | accessibility | C:\Users\zbook\.claude\skills\accessibility\SKILL.md |
| WCAG 2.1 AA design/page review | accessibility-review | C:\Users\zbook\.claude\skills\accessibility-review\SKILL.md |
| Android Kotlin idioms | android-kotlin-core | C:\Users\zbook\.claude\skills\android-kotlin-core\SKILL.md |
| OWASP API Top 10 review of REST/GraphQL/gRPC | api-security-review | C:\Users\zbook\.claude\skills\api-security-review\SKILL.md |
| FastAPI / Pydantic work | fastapi | C:\Users\zbook\.claude\skills\fastapi\SKILL.md |
| PostGIS spatial SQL | postgis | C:\Users\zbook\.claude\skills\postgis\SKILL.md |
| pytest coverage | pytest-coverage | C:\Users\zbook\.claude\skills\pytest-coverage\SKILL.md |
| systemd services and timers | systemd-services | C:\Users\zbook\.claude\skills\systemd-services\SKILL.md |

## Compact Rules

Pre-digested rules per skill. Delegators copy matching blocks into sub-agent prompts as `## Project Standards (auto-resolved)`.

### java-springboot
- Detect before writing: read build.gradle for Boot version, Java version, starters, persistence, test setup, package layout; match existing conventions. Never upgrade Boot/Java/deps implicitly.
- Boot 3 uses `spring-boot-starter-web` and Jackson 2 (`com.fasterxml.jackson.databind`); Boot 4 uses `starter-webmvc` and Jackson 3 (`tools.jackson.databind`).
- Ask before adding infrastructure (broker, cache provider, new DB, scheduler); BOM-managed artifacts get no explicit version, never `latest`.
- Package by feature; constructor injection only; controllers return DTOs/records, never entities; the service owns the transaction.
- Bean Validation on request records with `@Valid`; ONE `@RestControllerAdvice` returning `ProblemDetail`.
- Put `@EnableJpaAuditing`, `@EnableCaching`, `@EnableAsync`, `@EnableScheduling` on dedicated `@Configuration` classes, NOT on the `@SpringBootApplication` class, or `@DataJpaTest` slices fail to start. Slices do not scan @Configuration: `@Import` it.
- `@Transactional`/`@Cacheable`/`@Async` work via proxies: no effect on self-invocation or private/final methods; test through the injected bean.
- Config via `@ConfigurationProperties`; secrets never in code, properties or logs.
- Verify every change with `./gradlew build` (wrapper).

### springboot-patterns
- Layering controller -> service -> repository; constructor injection; controllers thin, logic in services.
- Request/response as records; `@Valid @RequestBody` with Bean Validation (`@NotBlank`, `@Size`, `@NotNull`); map entity to response with a static `from(...)`.
- POST returns 201 via `ResponseEntity.status(HttpStatus.CREATED)`; list endpoints take `page`/`size` and return a `Page` mapped to DTO.
- `@Transactional` on service write methods; map persistence entity to domain object before returning.
- Central `@ControllerAdvice`: validation -> 400 with field messages, access denied -> 403, generic -> 500 with a generic message (log the stack trace, never leak it).
- `@Cacheable`/`@Async` need `@EnableCaching`/`@EnableAsync` on a configuration class.
- SLF4J logging with structured key=value messages (`log.info("event key={}", v)`); log errors with the exception as last argument.
- Use `OncePerRequestFilter` for cross-cutting request logging.

### springboot-tdd
- Workflow: write failing test first, minimal code to pass, refactor green, enforce coverage (JaCoCo, 80%+ target).
- Unit: `@ExtendWith(MockitoExtension.class)` with `@Mock`/`@InjectMocks`; Arrange-Act-Assert; no partial mocks; `@ParameterizedTest` for variants.
- Web layer: `@WebMvcTest(Controller.class)` + `MockMvc`; mock the service (`@MockBean`, or `@MockitoBean` on Boot 3.4+); assert with `jsonPath`.
- Integration: `@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")`.
- Persistence: `@DataJpaTest`; this project uses H2 in tests (PostgreSQL is only the target), so keep the embedded DB; use Testcontainers/Postgres only if a test needs real Postgres behavior.
- Assertions with AssertJ (`assertThat`, `assertThatThrownBy`); test data builders for domain objects.
- Tests fast, isolated, deterministic; test behavior, not implementation.
- Gradle: `./gradlew test jacocoTestReport`.

### java-junit
- Detect first: copy conventions in `src/test/java`; do not add AssertJ/Mockito/JaCoCo to a project that lacks them without asking; never bump the JUnit major implicitly.
- JUnit 5 Jupiter (`org.junit.jupiter.api.*`); Gradle needs `testRuntimeOnly("org.junit.platform:junit-platform-launcher")` and `tasks.test { useJUnitPlatform() }`.
- Assert outcomes/state, not interaction counts; no bare `assertNotNull`; never catch AssertionError to pass.
- Mock only collaborators across a boundary (repository, HTTP client, clock, sender); never mock records, value objects, collections, or the class under test. Prefer a hand-written fake for stateful interaction.
- Strict stubs: an unused stub fails the test; fix the test, do not go lenient without justification. Avoid `mockStatic`/`mockConstruction`.
- Determinism: inject `java.time.Clock` (`Clock.fixed`) and seeded Random; no dependence on locale, time zone, network, sleeps.
- Naming: unit `*Test` (Gradle `test`), integration `*IT` (separate `integrationTest` task or `@Tag`).
- NEVER delete, @Disabled, weaken or skip a failing test; never use `-x test`. If the fix changes production behavior, stop and ask.
- Run narrow first: `./gradlew test --tests 'pkg.OrdersTest'`, then `./gradlew check`.
- Flaky test: remove the nondeterminism (time, order, shared state); no retries.

### tdd
- Red before green: failing test first, then only enough code to pass; no speculative features. One slice at a time (one test -> one implementation), never all tests first.
- Test behavior through public interfaces (seams), not implementation; a good test survives refactors. Test names read like specs ("user can checkout with valid cart").
- Agree the seams under test with the user before writing tests; no test at an unconfirmed seam.
- Anti-patterns: implementation-coupled tests (mocking internal collaborators, private methods, verifying via DB side channel); tautological tests (assertion recomputes the expected value the way the code does). Expected values come from literals/spec.
- Refactoring belongs to review, not the red-green loop. Use GLOSSARY.md vocabulary if it exists.
- Project is on Strict TDD Mode: also keep a safety-net baseline run and triangulate with 2+ cases per behavior.

### jpa-patterns
- Entities: explicit `@Table`/`@Column`; `@Enumerated(EnumType.STRING)`; `@Id @GeneratedValue(IDENTITY)`; auditing with `@EntityListeners(AuditingEntityListener.class)` + `@CreatedDate`/`@LastModifiedDate` (`@EnableJpaAuditing` on its own config class).
- Default LAZY; never EAGER on collections; use `JOIN FETCH` or DTO projections on read paths to prevent N+1.
- `@OneToMany(mappedBy=..., cascade=ALL, orphanRemoval=true)` only for true aggregate children; keep both sides in sync.
- `@Transactional` on service methods, `readOnly = true` for reads; keep transactions short.
- Pagination with `PageRequest.of(page, size, Sort.by(...))`; projections (interfaces/records) for lightweight reads; avoid `select *`.
- Index filters and FKs (`@Index` / migration); composite indexes matching query patterns.
- Migrations with Flyway/Liquibase; never rely on Hibernate auto DDL in production (`ddl-auto=validate`/`none`); H2 auto DDL only in tests.
- Test with `@DataJpaTest`; log SQL via `logging.level.org.hibernate.SQL=DEBUG` to assert efficiency.
- Hexagonal: keep JPA entities in the persistence adapter and map to domain models.

### spring-data-jpa
- Repositories extend `JpaRepository<T, ID>`; derived queries for simple conditions (`findByEmail`), `@Query` + `@Param` for complex ones; `Optional<T>` for single results.
- `@Modifying` for update/delete queries; `Pageable` for pagination (needs indexed sort columns).
- `@Transactional(readOnly = true)` on reads; explicit boundaries on writes; specify rollback rules when needed.
- Prevent N+1 with `@EntityGraph` or `JOIN FETCH`; never EAGER collections.
- NEVER expose JPA entities in REST APIs; always DTOs (records).
- Constructor injection only; prefer immutable fields; explicit `@Table`/`@Column`.
- Careful with `CascadeType.REMOVE` on large collections; test cascades on a small dataset first.
- UUID PKs fragment indexes: use sequential UUIDs or Long IDs unless required.
- Verify slow queries with `EXPLAIN ANALYZE`; add index then re-verify.
- Auditing: `@CreatedDate` with `updatable = false`, `@LastModifiedDate`.

### gradle-best-practices
- Audit mode reports findings; Apply mode fixes. Read only the catalog layers it names (references/<category>.md); never fetch from the network.
- Use the `plugins {}` block, never `apply plugin:`; prefer Kotlin DSL when the project uses it; name the root project in settings.
- Always use the wrapper (`gradlew`), prefer the `-bin` distribution; keep build flags in root `gradle.properties` (not in subprojects).
- Dependencies: single GAV string, centralize versions in `gradle/libs.versions.toml`, declare repositories in `settings.gradle(.kts)` only, no duplicate declarations.
- Performance: enable build cache and configuration cache; no expensive computation or `.get()` at configuration time; avoid `afterEvaluate`.
- Do not use Gradle internal APIs; do not hardcode task names; avoid `dependsOn` between tasks when an artifact flows (wire outputs instead).
- Use UTF-8 file encoding. Do not depend on plugin application order.
- Never bump the Gradle version implicitly.

### postgres
- NOTE: `references/design-postgres-tables.md` is a dangling 37-byte pointer in this install, so the rules below are standard PostgreSQL practice, not extracted from it.
- PKs: `bigint generated always as identity` (or UUID v7); `text` over `varchar(n)`; `timestamptz` over `timestamp`; `numeric` for money, never float.
- Declare constraints in the DB: `NOT NULL`, `UNIQUE`, `CHECK`, FKs; model overlaps with exclusion constraints (`EXCLUDE USING gist` with `tstzrange`), useful for appointment slots.
- PostgreSQL does not auto-index FK columns: index them plus common filter/sort columns.
- Prefer `CHECK`/lookup tables for enum-like values; JSONB only for truly semi-structured data.
- Migrations: additive and reversible; avoid long locks (`CREATE INDEX CONCURRENTLY`, add nullable column then backfill); set `lock_timeout`.
- H2 in tests must stay Postgres-compatible (`MODE=PostgreSQL`); avoid Postgres-only SQL on code paths tested with H2, or gate it.

### openspec-apply-change
- For "opsx apply": get state via `openspec status` and `openspec instructions apply --change <name> --json`; implement tasks from tasks.md and mark each `[x]` when done.
- Read the change's proposal/design/specs before coding; pause and report on ambiguity or blockers instead of guessing.
- Follow the project's Strict TDD cycle when applying tasks.

### openspec-archive-change
- Only when all tasks are complete; sync delta specs then archive to `openspec/changes/archive/YYYY-MM-DD-<name>/`.
- Check `openspec status` first; warn on incomplete artifacts/tasks before archiving.

### openspec-explore
- Thinking-partner mode: investigate and clarify; do NOT implement code. Create/update OpenSpec artifacts only if asked.

### openspec-propose
- Creates change + proposal, design, tasks (and delta specs) in one step via `openspec new change "<name>"` and `openspec instructions <artifact> --change`; kebab-case change names.
- Never create openspec/ structure manually; always use the CLI.

### openspec-sync-specs
- Merges delta specs of a change into main specs at `openspec/specs/<capability>/spec.md` without archiving; intelligent merge, not blind overwrite.

### openspec-update-change
- Revises existing planning artifacts and keeps them coherent with each other; never edits code. For the `openspec update` CLI (refresh generated files) run the command instead.

### active-orchestrator
- Reads project state and triggers only the missing foundation step (openspec init, kb-creator, roadmap-generator, find-skill, agent-instruction); idempotent, never re-runs what exists.

### agent-instruction
- Generates the canonical AGENTS.md / CLAUDE.md from KB + roadmap; confirms hard rules with the user (stack-aware); emits only project-specific content, not global client instructions.

### discovery-research
- Runs the Discovery Q&A checklist (problem, users, use cases, competitors, features, business rules, integrations, constraints, risks, open questions) before the KB; does not build the KB.

### find-skill
- Search/install agent skills when the user asks "is there a skill for X".

### kb-creator
- Builds the 10 canonical files in `knowledge-base/`; silent mode from docs/, or interactive from scratch.

### roadmap-generator
- Generates CHANGES.md (dependency tree, parallelism gates, critical path, per-change scope, governance level, "Leer antes" pointers); no questions asked.

### skill-creator
- Create/modify skills and evals; only when the user asks to author skills.

### web-scraper
- Competitor research from a URL via WebFetch/WebSearch into fixed-schema notes; no mass crawling or JS-heavy sites.

### accessibility
- WCAG 2.2 web UI audits; not applicable to this backend.

### accessibility-review
- WCAG 2.1 AA review of designs/pages; not applicable to this backend.

### android-kotlin-core
- Kotlin/Android idioms; not applicable.

### api-security-review
- OWASP API Top 10 (2023) review: BOLA/IDOR, broken auth, mass assignment, rate limiting, SSRF. Optionally useful when reviewing this project's REST API.

### fastapi
- Python FastAPI; not applicable.

### postgis
- PostGIS spatial SQL; not applicable (no spatial data).

### pytest-coverage
- Python pytest coverage; not applicable.

### systemd-services
- Linux systemd units/timers; not applicable.

## Project Conventions

| File | Path | Notes |
|------|------|-------|
| knowledge-base/ | knowledge-base/01_vision_y_objetivos.md | Source of truth for the domain (10 files): vision and goals |
| | knowledge-base/02_descripcion_general.md | General description |
| | knowledge-base/03_actores_y_roles.md | Actors and roles |
| | knowledge-base/04_modelo_de_datos.md | Data model |
| | knowledge-base/05_reglas_de_negocio.md | Business rules |
| | knowledge-base/06_funcionalidades.md | Features |
| | knowledge-base/07_flujos_principales.md | Main flows |
| | knowledge-base/08_arquitectura_propuesta.md | Proposed (hexagonal) architecture |
| | knowledge-base/09_decisiones_y_supuestos.md | Decisions and assumptions |
| | knowledge-base/10_preguntas_abiertas.md | Open questions |
| CHANGES.md | CHANGES.md | Implementation roadmap (changes, dependencies, governance levels) |
| Discovery report | docs/discovery/informe-discovery.md | Discovery that grounds the product |
| Discovery notes | docs/discovery/discovery.md | Supporting discovery notes |
| OpenSpec | openspec/ | Specs and changes (managed via the openspec CLI) |
| AGENTS.md | (pending) | Does not exist yet; generated by agent-instruction in the next foundation step |
| CLAUDE.md | (pending) | Does not exist yet; generated by agent-instruction in the next foundation step |

Read the convention files listed above for project-specific patterns and rules. AGENTS.md and CLAUDE.md are pending; re-run skill-registry after they exist to pick up the paths they reference.
