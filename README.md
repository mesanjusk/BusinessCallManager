# Business Call Manager — Monorepo

This monorepo contains both the Android app and the Node.js backend for the Business Call Manager (QuickLink Caller) project.

## Structure

```
BusinessCallManager/
├── android/      # Kotlin/Jetpack Compose Android app (QuickLink Caller)
└── backend/      # Node.js + Express + MongoDB REST API
```

---

## Automatic APK / AAB builds (CI/CD)

Every push to the **`main`** branch automatically:
1. Builds a signed release **APK** (direct Android install) and **AAB** (Google Play upload)
2. Publishes them as the **`latest`** GitHub Release

### Download the latest build
Go to **Releases → latest** on this repository page and download:
- `BusinessCallManager.apk` — install directly on any Android device
- `BusinessCallManager.aab` — upload to Google Play Console

### Required GitHub Secrets (one-time setup)

Go to **Settings → Secrets and variables → Actions** in the repository and add:

| Secret | Value |
|--------|-------|
| `KEYSTORE_BASE64` | Base64-encoded `.keystore` file (see below) |
| `KEYSTORE_PASSWORD` | Keystore store password |
| `KEY_ALIAS` | Key alias inside the keystore |
| `KEY_PASSWORD` | Key password |

> **If no secrets are configured** the workflow auto-generates a temporary keystore using the default credentials in `build.gradle.kts`. This is fine for testing but the keystore will differ on each run, making the AAB unsuitable for Google Play updates.

#### How to encode your keystore

```bash
# Generate keystore (skip if you already have one)
keytool -genkey -v \
  -keystore businesscallmanager.keystore \
  -alias businesscallmanager \
  -keyalg RSA -keysize 2048 -validity 10000

# Base64-encode it (copy the output into the KEYSTORE_BASE64 secret)
base64 -w 0 businesscallmanager.keystore
```

---

## Backend setup

```bash
cd backend
cp .env.example .env   # fill in your values
npm install
npm run dev
```

## Android local setup

Open the `android/` folder in Android Studio. Select the `Prod` build flavor and run/build from there.

## Notes
- Never commit `.env` or `.keystore` / `.jks` files to the repository
- The CI/CD pipeline is defined in `.github/workflows/build-apk.yml`
