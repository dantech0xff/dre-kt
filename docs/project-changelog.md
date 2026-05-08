# Project Changelog

All notable project changes are recorded here. Dates use `YYYY-MM-DD`.

## Unreleased

### Added

- Root Codex guidance through `AGENTS.md`.
- Project-scoped Codex agents for dre-kt planning, API review, testing, and docs maintenance.
- Development roadmap seeded with adoption-first priorities.
- Project changelog for release and maintenance tracking.
- Implementation plan for Codex project enablement and roadmap work.

### Changed

- `install-skill.sh` supports installing the `dre-integrate` skill for Claude, Codex, or both.
- `dre-integrate` scaffold examples avoid non-existent reducer adapter helpers.

### Verified

- Verified during this work: `./gradlew :dre-core:test :dre-android:testDebugUnitTest` passed.
- Verified during this work: `./gradlew :sample:assembleDebug` passed.

## 0.1.3

### Added

- Maven Central publishing configuration for `dre-core` and `dre-android`.
- Core DRE APIs: marker interfaces, reducer contract, reduce result, store, side-effect handler, and reducer test helpers.
- Android `DreStoreViewModel` integration.
- Counter sample app.

### Documentation

- README quick start and installation guidance.
- Project overview, architecture, code standards, and codebase summary docs.
