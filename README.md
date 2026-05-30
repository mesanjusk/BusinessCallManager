# Business Call Manager — Monorepo

This monorepo contains both the Android app and the Node.js backend for the Business Call Manager (QuickLink Caller) project.

## Structure

```
BusinessCallManager/
├── android/      # Kotlin/Jetpack Compose Android app (QuickLink Caller)
└── backend/      # Node.js + Express + MongoDB REST API
```

## Backend setup

```bash
cd backend
cp .env.example .env   # fill in your values
npm install
npm run dev
```

## Android setup

Open the `android/` folder in Android Studio. Select the `Dev` or `Prod` build flavor and run.

## Notes
- Never commit `.env` or `.jks` keystore files
- Release APK/AAB files are excluded from this repo — build them locally via Android Studio
