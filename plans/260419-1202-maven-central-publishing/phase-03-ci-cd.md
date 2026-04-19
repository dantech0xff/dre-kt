# Phase 3: CI/CD Automation

## Priority: Medium | Status: Not started

## Overview

GitHub Actions workflow to auto-publish on version tag push.

## Implementation Steps

### 1. Add GitHub Secrets

In repo Settings → Secrets → Actions:

- `MAVEN_CENTRAL_USERNAME` — Sonatype portal username
- `MAVEN_CENTRAL_PASSWORD` — Sonatype portal password/token
- `GPG_PRIVATE_KEY` — ASCII-armored private key (`gpg --export-secret-keys --armor`)
- `GPG_PASSPHRASE` — GPG key passphrase

### 2. Create Release Workflow

`.github/workflows/publish.yml`:

```yaml
name: Publish to Maven Central

on:
  push:
    tags:
      - 'v*'

jobs:
  publish:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 17

      - name: Import GPG key
        run: echo "${{ secrets.GPG_PRIVATE_KEY }}" | gpg --batch --import

      - name: Publish dre-core
        run: ./gradlew :dre-core:publish
        env:
          ORG_GRADLE_PROJECT_mavenCentralUsername: ${{ secrets.MAVEN_CENTRAL_USERNAME }}
          ORG_GRADLE_PROJECT_mavenCentralPassword: ${{ secrets.MAVEN_CENTRAL_PASSWORD }}
          ORG_GRADLE_PROJECT_signingKeyId: ${{ secrets.GPG_KEY_ID }}
          ORG_GRADLE_PROJECT_signingPassword: ${{ secrets.GPG_PASSPHRASE }}

      - name: Publish dre-android
        run: ./gradlew :dre-android:publish
        env:
          ORG_GRADLE_PROJECT_mavenCentralUsername: ${{ secrets.MAVEN_CENTRAL_USERNAME }}
          ORG_GRADLE_PROJECT_mavenCentralPassword: ${{ secrets.MAVEN_CENTRAL_PASSWORD }}
          ORG_GRADLE_PROJECT_signingKeyId: ${{ secrets.GPG_KEY_ID }}
          ORG_GRADLE_PROJECT_signingPassword: ${{ secrets.GPG_PASSPHRASE }}
```

### 3. Add CI Test Workflow

`.github/workflows/ci.yml`:

```yaml
name: CI

on:
  push:
    branches: [main, master]
  pull_request:
    branches: [main, master]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 17
      - run: ./gradlew :dre-core:test :dre-android:testDebugUnitTest
```

### 4. Release Process

```bash
# 1. Update version in build.gradle.kts
# 2. Commit
git commit -am "release: v0.1.0"
# 3. Tag
git tag v0.1.0
# 4. Push
git push origin main --tags
# 5. GitHub Actions publishes automatically
# 6. Verify on https://central.sonatype.com
```

## Todo

- [ ] Add GitHub secrets (4 secrets)
- [ ] Create `.github/workflows/publish.yml`
- [ ] Create `.github/workflows/ci.yml`
- [ ] Test with first release tag `v0.1.0`
- [ ] Verify artifacts on Maven Central portal

## Success Criteria

- Tag push triggers publish workflow
- Artifacts appear on Maven Central within ~30 min
- CI runs tests on every push/PR
