---
status: planned
created: 2026-04-19
slug: maven-central-publishing
blockedBy: []
blocks: []
---

# Maven Central Publishing for dre-kt

## Overview

Publish `dre-core` and `dre-android` to Maven Central so users can consume via:

```kotlin
dependencies {
    implementation("dev.drekt:dre-core:0.1.0")
    implementation("dev.drekt:dre-android:0.1.0")
}
```

**Priority:** High
**Effort:** Medium (mostly config, no code changes)

## Phases

| # | Phase | Status | File |
|---|-------|--------|------|
| 1 | Prerequisites & Accounts | Not started | [phase-01](phase-01-prerequisites.md) |
| 2 | Gradle Publishing Config | Not started | [phase-02](phase-02-gradle-config.md) |
| 3 | CI/CD Automation | Not started | [phase-03](phase-03-ci-cd.md) |

## Key Decisions

- **Group ID:** `dev.drekt` (matches package structure)
- **Artifact IDs:** `dre-core`, `dre-android`
- **Initial version:** `0.1.0`
- **Publishing plugin:** `maven-publish` + `signing` (Gradle built-in)
- **Portal:** Maven Central via Sonatype Central Portal (new portal, replaces OSSRH)
- **`sample` module excluded** — not published

## Dependencies

- Sonatype Central Portal account
- GPG key for signing
- Domain verification for `dev.drekt` (or use `io.github.dantech0xff`)
