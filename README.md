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

Every push to **`main`** automatically:
1. Builds a signed **APK** (direct Android install) and **AAB** (Google Play upload)
2. Publishes them as the **`latest`** GitHub Release

> You can also trigger a build manually: **Actions → Build Release APK + AAB → Run workflow**

### Download the latest build
Go to **Releases → latest** on this repository and download:
- `BusinessCallManager.apk` — install directly on any Android device (enable "Install from unknown sources")
- `BusinessCallManager.aab` — upload to Google Play Console

---

### Required GitHub Secrets (one-time setup)

Go to **Settings → Secrets and variables → Actions → New repository secret** and add:

| Secret | Value |
|--------|-------|
| `KEYSTORE_BASE64` | Base64-encoded `.keystore` file (see below) |
| `KEYSTORE_PASSWORD` | Keystore store password |
| `KEY_ALIAS` | Key alias (default: `businesscallmanager`) |
| `KEY_PASSWORD` | Key password |

> **If no secrets are configured**, the workflow auto-generates a temporary keystore using the default credentials already set in `build.gradle.kts`. Builds succeed, but each run creates a different keystore — suitable for testing, not for Google Play updates.

#### How to create and encode your keystore (run once locally)

```bash
# 1. Generate the keystore
keytool -genkey -v \
  -keystore businesscallmanager.keystore \
  -alias businesscallmanager \
  -keyalg RSA -keysize 2048 -validity 10000

# 2. Base64-encode it — copy this output into the KEYSTORE_BASE64 secret
base64 -w 0 businesscallmanager.keystore
```

Keep the `.keystore` file safe — you need the same one every time you publish an update to Google Play.

---

## Backend setup

```bash
cd backend
cp .env.example .env   # fill in your values
npm install
npm run dev
```

## Android local setup

Open the `android/` folder in Android Studio. Select the `Prod` build flavor, then **Build → Generate Signed Bundle/APK** to build locally.

## Notes
- Never commit `.env` or `.keystore` / `.jks` files — they are in `.gitignore`
- CI/CD pipeline: `.github/workflows/build-apk.yml`
