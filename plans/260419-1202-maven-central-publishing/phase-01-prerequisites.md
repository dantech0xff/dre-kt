# Phase 1: Prerequisites & Accounts

## Priority: High | Status: Not started

## Overview

Set up accounts, keys, and domain verification needed before publishing.

## Steps

### 1. Create Sonatype Central Portal Account

- Go to https://central.sonatype.com
- Sign in with GitHub account (dantech0xff)
- This is the **new** portal (replaces legacy OSSRH/Nexus)

### 2. Namespace (Group ID) Verification

Two options:

**Option A: `io.github.dantech0xff`** (easier)
- Sonatype auto-verifies GitHub-based namespaces
- Artifact coords: `io.github.dantech0xff:dre-core:0.1.0`

**Option B: `dev.drekt`** (cleaner, matches package)
- Requires DNS TXT record on `drekt.dev` domain
- Add TXT record with verification code from Sonatype portal
- Artifact coords: `dev.drekt:dre-core:0.1.0`

### 3. Generate GPG Key

```bash
# Generate key (RSA 4096)
gpg --full-generate-key

# List keys — note the key ID (last 8 chars of fingerprint)
gpg --list-keys --keyid-format short

# Export private key (for CI)
gpg --export-secret-keys --armor YOUR_KEY_ID > private-key.gpg

# Publish public key to keyserver (required by Maven Central)
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
```

### 4. Store Credentials

Add to `~/.gradle/gradle.properties` (local, never commit):

```properties
# Sonatype Central Portal
mavenCentralUsername=<sonatype-username>
mavenCentralPassword=<sonatype-password>

# GPG Signing
signing.keyId=<last-8-chars>
signing.password=<gpg-passphrase>
signing.secretKeyRingFile=/path/to/private-key.gpg
```

## Todo

- [ ] Create Sonatype Central Portal account
- [ ] Choose namespace: `dev.drekt` or `io.github.dantech0xff`
- [ ] Verify namespace (DNS or GitHub)
- [ ] Generate GPG key pair
- [ ] Publish public key to keyserver
- [ ] Store credentials in `~/.gradle/gradle.properties`

## Success Criteria

- Sonatype portal account active
- Namespace verified and approved
- GPG key generated, public key on keyserver
- Credentials stored locally (not in repo)
