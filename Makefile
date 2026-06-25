# Makefile — Unified build/test/run contract for the TODO BusinessRepo (story folders-001).
# Tooling that may be absent locally (maven, playwright browsers) runs via Docker.
.PHONY: build run stop test web-install web-test backend-unit test test-integration \
        test-e2e test-contract test-all lint security-scan fmt containers-up containers-down \
        seed-test migrate test-features-sync test-features-scaffold \
        sdlc-report sdlc-rotate-logs sdlc-branch-protection help

COMPOSE := docker compose -f infra/docker-compose.yml
MVN := docker run --rm -v "$(CURDIR)/app/api":/work -v taskmaple-m2:/root/.m2 -w /work maven:3.9-eclipse-temurin-21 mvn -B
PLAYWRIGHT_IMAGE := mcr.microsoft.com/playwright:v1.49.0-jammy

## Build the production image (vite -> maven -> jre)
build:
	$(COMPOSE) build

## Run the app (SPA + API) at http://localhost:8080
run:
	$(COMPOSE) up --build

## Stop and remove the app container + volumes
stop:
	$(COMPOSE) down -v

## Install frontend deps once (idempotent)
web-install:
	@[ -d app/web/node_modules ] || npm --prefix app/web ci --no-audit --no-fund

## Frontend unit tests (vitest)
web-test: web-install
	npm --prefix app/web run test

## Backend domain + application unit tests (surefire, *Test)
backend-unit:
	$(MVN) -q test

## Unit tests (gate-friendly): backend unit + frontend unit
test: backend-unit web-test
	@echo "Unit tests passed."

## API integration tests (failsafe, *IT) against H2 — delete-reassignment, General-protection, validation
test-integration:
	$(MVN) -q test-compile failsafe:integration-test failsafe:verify

## E2E (Playwright) — boots the app via compose, runs against :8080, tears down
test-e2e:
	$(COMPOSE) up -d --build --wait
	@echo "App healthy — running Playwright e2e…"
	docker run --rm --network host -v "$(CURDIR)":/work -w /work $(PLAYWRIGHT_IMAGE) \
	  sh -lc "npm ci --no-audit --no-fund && npx playwright test --config=tests/playwright.config.ts"; \
	  status=$$?; \
	  $(COMPOSE) down -v; \
	  exit $$status

## Contract test — boots the app and asserts the live OpenAPI matches the documented operations
test-contract:
	$(MVN) -q test-compile failsafe:integration-test failsafe:verify -Dit.test=OpenApiContractIT

## Run all test suites (Phase 8 gate)
test-all: test test-integration test-e2e test-contract
	@echo "All test suites passed."

## Lint: frontend typecheck + backend compile
lint: web-install
	npm --prefix app/web run typecheck
	$(MVN) -q -DskipTests compile

## Security scanning (best-effort, non-blocking)
security-scan:
	-npm --prefix app/web audit --audit-level=high
	@echo "Backend dependencies are pinned via the Spring Boot BOM."

## Format / static checks
fmt: web-install
	-npm --prefix app/web run lint
	@echo "fmt complete."

## Start / stop the app container in the background
containers-up:
	$(COMPOSE) up -d --build --wait

containers-down:
	$(COMPOSE) down -v

## Seed test data (no-op: schema + sample data are created at app boot)
seed-test:
	@echo "Seeding is automatic at boot (todo.seed=true). Use POST /api/v1/test/reset to reset."

## Migrations (no-op: H2 in-memory schema is created by JPA at boot)
migrate:
	@echo "No migrations: H2 in-memory schema is created at boot (ddl-auto=create-drop)."

## Sync Gherkin from docs/stories/ -> tests/features/ (idempotent)
test-features-sync:
	@python3 scripts/sync-features.py

## Generate step definition stubs for scenarios not yet covered
test-features-scaffold:
	@echo "Acceptance scenarios are implemented as Playwright specs under tests/e2e/."
	@echo "Each spec maps 1:1 to a Gherkin scenario in tests/features/."

# ─── BEGIN MAPLE MANAGED — updated by `maple update`, do not hand-edit ────────

## Print per-story agent invocation counts and estimated costs
## Reads .claude/logs/skills.jsonl
sdlc-report:
	@if [ ! -f .claude/logs/skills.jsonl ]; then \
		echo "No skills log found. Run some agent workflows first."; exit 0; \
	fi
	@echo "=== SDLC Cost Report ==="
	@python3 -c " \
import json, collections; \
lines = [json.loads(l) for l in open('.claude/logs/skills.jsonl') if l.strip()]; \
by_story = collections.defaultdict(list); \
[by_story[l.get('story','unknown')].append(l) for l in lines]; \
print(f'Stories: {len(by_story)}  Total invocations: {len(lines)}'); \
[print(f'  {s}: {len(v)} invocations') for s,v in sorted(by_story.items())] \
"

## Rotate .claude/logs/ — keep last 5 compressed, delete older
sdlc-rotate-logs:
	@bash scripts/sdlc/rotate-logs.sh

## Apply branch protection rules to main (requires gh admin scope)
sdlc-branch-protection:
	@bash scripts/sdlc/branch-protection.sh

## Show available targets
help:
	@echo "Available make targets:"
	@echo "  build                  - Build the production image"
	@echo "  run                    - Run the app at http://localhost:8080"
	@echo "  stop                   - Stop and remove the app container"
	@echo "  test                   - Unit tests (backend + frontend)"
	@echo "  test-integration       - API integration tests (H2)"
	@echo "  test-e2e               - E2E tests with Playwright"
	@echo "  test-contract          - Live OpenAPI contract test"
	@echo "  test-all               - Run all test suites (Phase 8 gate)"
	@echo "  test-features-sync     - Extract Gherkin from stories -> tests/features/"
	@echo "  lint                   - Frontend typecheck + backend compile"
	@echo "  security-scan          - Dependency vulnerability scan"
	@echo "  fmt                    - Format / static checks"
	@echo "  containers-up          - Start the app container (detached, waits for health)"
	@echo "  containers-down        - Stop and remove the app container"
	@echo "  migrate                - No-op (H2 in-memory)"
	@echo "  sdlc-report            - Print per-story cost + invocation report"
	@echo "  sdlc-rotate-logs       - Rotate .claude/logs/ (keep last 5 compressed)"
	@echo "  sdlc-branch-protection - Apply branch protection rules to main"

# ─── END MAPLE MANAGED ────────────────────────────────────────────────────────
