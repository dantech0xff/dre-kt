# Phase 2: Gradle Publishing Config

## Priority: High | Status: Not started

## Overview

Configure `maven-publish` + `signing` plugins for `dre-core` and `dre-android`.

## Key Insights

- `dre-core` is a plain Kotlin/JVM library → use `java` component
- `dre-android` is an Android library → use `release` component from AGP
- Both need POM metadata, sources jar, javadoc jar
- `sample` module is excluded from publishing
- Use convention plugin to avoid duplicating config

## Related Files

- `build.gradle.kts` (root) — add version + group
- `dre-core/build.gradle.kts` — add publishing
- `dre-android/build.gradle.kts` — add publishing
- `gradle/libs.versions.toml` — add vanniktech plugin (optional)

## Implementation Steps

### 1. Set group and version in root build

```kotlin
// build.gradle.kts (root)
allprojects {
    group = "dev.drekt"  // or io.github.dantech0xff
    version = "0.1.0"
}
```

### 2. Configure dre-core publishing

```kotlin
// dre-core/build.gradle.kts
plugins {
    alias(libs.plugins.kotlin.jvm)
    `maven-publish`
    signing
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("release") {
            from(components["java"])
            artifactId = "dre-core"
            pom { /* metadata */ }
        }
    }
    repositories {
        maven {
            name = "mavenCentral"
            url = uri("https://central.sonatype.com/api/v1/publisher/upload")
            credentials {
                username = findProperty("mavenCentralUsername") as? String
                password = findProperty("mavenCentralPassword") as? String
            }
        }
    }
}

signing {
    sign(publishing.publications["release"])
}
```

### 3. Configure dre-android publishing

```kotlin
// dre-android/build.gradle.kts
plugins {
    alias(libs.plugins.android.library)
    `maven-publish`
    signing
}

android {
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                artifactId = "dre-android"
                pom { /* metadata */ }
            }
        }
    }
}
```

### 4. POM Metadata (shared)

```kotlin
pom {
    name.set("dre-kt")
    description.set("Dispatch → Reduce → Effects state management for Kotlin & Android")
    url.set("https://github.com/dantech0xff/dre-kt")
    licenses {
        license {
            name.set("MIT License")
            url.set("https://opensource.org/licenses/MIT")
        }
    }
    developers {
        developer {
            id.set("dantech0xff")
            name.set("Dan")
            url.set("https://github.com/dantech0xff")
        }
    }
    scm {
        url.set("https://github.com/dantech0xff/dre-kt")
        connection.set("scm:git:git://github.com/dantech0xff/dre-kt.git")
        developerConnection.set("scm:git:ssh://github.com/dantech0xff/dre-kt.git")
    }
}
```

### 5. Verify locally

```bash
./gradlew :dre-core:publishToMavenLocal :dre-android:publishToMavenLocal
ls ~/.m2/repository/dev/drekt/dre-core/0.1.0/
ls ~/.m2/repository/dev/drekt/dre-android/0.1.0/
```

## Todo

- [ ] Add group + version to root build
- [ ] Add `maven-publish` + `signing` to dre-core
- [ ] Add `maven-publish` + `signing` to dre-android
- [ ] Add POM metadata (name, description, license, SCM, developer)
- [ ] Add sources jar + javadoc jar
- [ ] Configure Sonatype Central repository
- [ ] Test `publishToMavenLocal`
- [ ] Verify POM content in `~/.m2`

## Risk Assessment

- **POM validation**: Maven Central rejects incomplete POMs — must have name, description, URL, license, SCM, developer
- **Android variant**: Must use `afterEvaluate` for Android library publications
- **Signing**: Will fail if GPG not configured — Phase 1 must complete first

## Success Criteria

- `./gradlew publishToMavenLocal` succeeds for both modules
- `~/.m2` contains correct artifacts with valid POM, sources, javadoc
- Signing passes without errors
