# Development Roadmap

## Overview

This roadmap tracks practical improvements for `dre-kt` while preserving the library's core value: small API, pure reducers, explicit async operations, and thin platform integration.

## Current Baseline

| Area | Status | Notes |
| --- | --- | --- |
| Core API | Stable for 0.x | `Reducer`, `ReduceResult`, `DreStore`, markers, and test helpers exist. |
| Android integration | Stable for 0.x | `DreStoreViewModel` wraps `DreStore` with `viewModelScope`. |
| Sample app | Working | Counter sample builds, but can teach side-effect handling better. |
| Publishing | Working | Maven Central config exists for `dre-core` and `dre-android`. |
| Codex support | Available | Root guidance and project agents exist. |

## Priority 1: Adoption Docs And Examples

Status: Planned

Improve user onboarding without changing the public API.

- Add recipes for simple screen state, API loading, form validation, navigation/toast effects, and direct `DreStore` usage.
- Keep each recipe focused on one use case.
- Ensure examples compile against current public API.

## Priority 2: Skill And Scaffold Accuracy

Status: Planned

Fix friction in the dre-integrate guidance.

- Keep scaffold examples free of helpers that do not exist in the public API.
- Prefer additive helpers only if they make simple reducers meaningfully easier.
- Keep setup, feature, and migration guides aligned with Maven coordinates and current API.

## Priority 3: Sample Credibility

Status: Planned

Make the sample app a better executable explanation of DRE.

- Either handle `CounterEffect.ShowToast` through a real side-effect path or remove unused effect/toast code.
- Add focused sample tests if behavior becomes more than a visual counter demo.
- Keep the sample small enough to scan quickly.

## Priority 4: Release Hygiene

Status: Planned

Reduce release mistakes and future build friction.

- Avoid duplicated hardcoded versions between root build config and publishing coordinates.
- Refresh stale publishing plan docs.
- Investigate the Gradle 10 toolchain repository warning.
- Consider a release checklist before the next tag.

## Later: Platform Expansion

Status: Deferred

KMP or Compose Multiplatform support may expand use cases, but it should wait until the JVM/Android adoption story is stronger.

## Success Metrics

- A new Android developer can integrate dre-kt from README and recipes without guessing.
- Scaffold docs generate code that matches the real API.
- Core API remains small and understandable.
- Baseline tests and sample assemble stay green before releases.
