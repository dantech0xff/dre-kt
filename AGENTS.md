# AGENTS.md

Guidance for Codex and project-scoped agents working in this repository.

## Project

`dre-kt` is a small Kotlin state-management library built around Dispatch -> Reduce -> Effects.

Core value: keep state transitions explicit, reducers pure, async work represented as data, and platform integrations thin.

## Module Map

- `dre-core`: platform-agnostic library API. Owns `DreStore`, `Reducer`, `ReduceResult`, marker interfaces, side-effect handling, and reducer test helpers.
- `dre-android`: Android `ViewModel` wrapper around `DreStore`. Keep it lifecycle-focused and thin.
- `sample`: Compose counter app used as executable documentation.
- `docs`: durable project documentation.
- `.codex/agents`: project-scoped Codex agent instructions.
- `.claude/skills/dre-integrate`: distributable dre-kt integration skill source.

## Build And Test

Use these commands before claiming implementation is complete:

```bash
./gradlew :dre-core:test
./gradlew :dre-android:testDebugUnitTest
./gradlew :sample:assembleDebug
```

Useful combined baseline:

```bash
./gradlew :dre-core:test :dre-android:testDebugUnitTest
./gradlew :sample:assembleDebug
```

For installer-only changes:

```bash
bash -n install-skill.sh
```

## DRE Invariants

- Reducers must be pure and deterministic: no I/O, no coroutines, no side effects.
- Reducers return `ReduceResult(state, sideEffects, asyncOp)`.
- Side effects are fire-and-forget and must not dispatch result actions.
- Async operations are data returned by the reducer; async handlers dispatch result actions when done.
- `DreStore` dispatch serialization is part of the API contract. Do not change ordering or state update semantics casually.
- `dre-android` should remain a lifecycle wrapper, not a framework layer.

## Public API Compatibility

This project is pre-1.0 but should still be conservative.

Allowed by default:
- Additive helpers, docs, tests, samples, and overloads.
- Bug fixes that preserve existing behavior for valid callers.

Avoid unless explicitly requested:
- Renaming public types or packages.
- Removing marker interfaces.
- Changing `DreStore.dispatch()` ordering.
- Adding middleware, DI, navigation, persistence, or networking abstractions to the core.
- Moving Android dependencies into `dre-core`.

## Implementation Rules

- Follow YAGNI, KISS, and DRY.
- Keep Kotlin source files around 200 lines or less.
- Use existing package structure and conventions before adding new patterns.
- Prefer clear Kotlin types over generic extensibility points.
- Keep samples honest: if a sample emits an effect, it should demonstrate how that effect is handled.
- Do not commit secrets or local machine config. `local.properties` stays local.

## Docs Rules

Update docs when behavior, public API, release process, or project priorities change:

- `docs/project-overview-pdr.md`: product goals and non-goals.
- `docs/system-architecture.md`: data flow and module boundaries.
- `docs/code-standards.md`: coding and testing conventions.
- `docs/codebase-summary.md`: module/file overview.
- `docs/development-roadmap.md`: planned improvements and priorities.
- `docs/project-changelog.md`: notable changes.

## Agent Routing

Prefer project-scoped Codex agents when available:

- `dre-library-planner`: roadmap, API shape, adoption-first planning.
- `dre-api-reviewer`: public API and DRE invariant review.
- `dre-test-agent`: reducer/store/ViewModel test coverage.
- `dre-docs-manager`: docs, changelog, roadmap, and skill docs.

For implementation work, keep subagent prompts scoped to exact files and include:

- Work context: repository root.
- Reports path: `plans/reports/` or the active plan's `reports/`.
- Plans path: `plans/`.

## Current Priorities

1. Improve adoption docs and examples.
2. Fix skill/scaffold friction without breaking the API.
3. Make the sample demonstrate real DRE usage cleanly.
4. Clean up release hygiene and Gradle warnings.
5. Defer KMP or larger platform expansion until the JVM/Android story is polished.
