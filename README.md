<div align="center">

# 🏋️ Kinetic

### Your all-in-one fitness companion — workouts, nutrition, and community in a single app.

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202024.04-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Android](https://img.shields.io/badge/Android-min%20SDK%2024%20·%20target%2035-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

[Features](#-features) · [Screenshots](#-screenshots) · [Tech Stack](#-tech-stack) · [Architecture](#-architecture) · [Getting Started](#-getting-started) · [Build](#-build) · [API](#-api) · [Contributing](#-contributing)

</div>

---

## ✨ Features

### 💪 Training
- **Weekly split planner** — build your own training schedule with muscle-group focus
- **Workout logging** — log sets, reps, and weights in seconds; auto-computed volume
- **Exercise library** — 100+ exercises with GIF demos, equipment filters, and muscle-group maps
- **1RM & Plate calculators** — estimate your one-rep max and load the barbell correctly
- **Muscle recovery tracker** — per-group recovery bars so you never overtrain
- **Rest days & deloads** — scheduled recovery with guidance
- **Custom workouts & templates** — save and reuse your favorite sessions

### 🥗 Nutrition
- **Food journal** — log meals by breakfast, lunch, dinner, and snacks
- **Macro tracking** — live calories, protein, carbs, and fat vs. your daily targets
- **Smart search** — searchable food database (local + OpenFoodFacts) or manual entry
- **Barcode scanner** — scan product barcodes to log instantly
- **Nutrition calculator** — targets computed from your profile and goal (lose / maintain / gain)
- **Water tracking** — daily hydration with reminders

### 🗺️ GPS Cardio
- Real-time GPS route tracking with satellite map
- Session recording, distance / pace / calories, and full history
- Foreground service with pause/resume/cancel and live stats

### 🤖 AI & Community
- **AI Trainer** — chat assistant that answers training questions and plans your workouts
- **Social feed** — share workouts, like, and comment
- **Friends system** — requests, accept/reject, and friend lists
- **Leaderboards** — compete on volume, streaks, and achievements
- **Badges & streaks** — stay motivated with gamification

### 📊 Tracking & Insights
- **Biometrics** — weight, body fat %, and charts over time
- **Workout analytics** — monthly progress, PRs, and volume trends
- **Injury risk assessment** — screen based on your training load
- **Home screen widgets** — steps ring and water intake (Jetpack Glance)

### 🌍 Platform
- **11 languages** — full i18n (Romanian, English, and more)
- **Light & dark theme** — premium frosted-glass UI in both modes
- **Offline-first sync** — Room database with background cloud sync
- **Google, Facebook & email sign-in** — Firebase Auth
- **Premium subscriptions** — RevenueCat paywall, AdMob rewarded ads for unlocks

---

## 📸 Screenshots

| Dashboard | Workout Logging | Food Journal |
| :---: | :---: | :---: |
| *(add your screenshots here)* | *(add your screenshots here)* | *(add your screenshots here)* |

---

## 🛠 Tech Stack

### Android App
| Layer | Technology |
|---|---|
| Language | **Kotlin 2.0.21** |
| UI | **Jetpack Compose** (BOM 2024.04), Material 3, Haze (frosted glass) |
| Architecture | **MVVM** — ViewModels + StateFlow + Repository pattern |
| Database | **Room 2.6.1** (KSP) with offline sync |
| Networking | **Retrofit + OkHttp**, **Coil** (images & GIFs) |
| Backend | **Firebase** (Auth, Firestore, Storage, Messaging) |
| Navigation | **Navigation Compose** |
| Background | **WorkManager**, **Glance** app widgets, GPS foreground service |
| Monetization | **RevenueCat** (billing + paywall), **AdMob** (rewarded) |
| Maps | **OSMDroid** satellite tiles |
| Minimum SDK | **24** (with core library desugaring) · Target SDK **35** |

### Backend Services
| Service | Stack | Purpose |
|---|---|---|
| `backend/` | **Node.js + Express + SQLite** (`better-sqlite3`) | Social API — friends, feed, leaderboard, badges, sync |
| `functions/` | **Firebase Cloud Functions** (TypeScript) | Trigger-based logic (e.g., friend-request notifications) |
| `ai_server/` | **FastAPI** (Python) | AI Trainer chat assistant |

---

## 🏗 Architecture

```
┌─────────────────────────────────────────────────────────┐
│                     Android App (Compose)               │
│  ┌──────────┐  ┌──────────┐  ┌──────────────────────┐  │
│  │  Screens │→│ ViewModel │→│   Repository (Room)   │  │
│  │ (85+ UI) │  │  (State) │  │  + Sync + Network    │  │
│  └──────────┘  └──────────┘  └──────────┬───────────┘  │
└─────────────────────────────────────────┼───────────────┘
                                          │
              ┌──────────────┬────────────┼───────────────┐
              ▼              ▼            ▼               ▼
        ┌───────────┐  ┌───────────┐ ┌───────────┐  ┌───────────┐
        │  Firebase │  │  backend/ │ │ ai_server │  │ Firestore │
        │    Auth   │  │  (Node)   │ │ (FastAPI) │  │   + FCM   │
        └───────────┘  └───────────┘ └───────────┘  └───────────┘
```

The app is **offline-first**: all data lives in a local Room database and syncs to the cloud in the background, so it works even without a connection. User data is **isolated per account** — switching accounts never leaks data between profiles.

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio** (latest stable) with **JDK 21**
- Android SDK 35 + Build Tools
- A Firebase project (for Auth, Firestore, Storage, FCM)
- Node.js 18+ and Python 3.10+ (for backend services, optional)

### 1. Clone

```bash
git clone https://github.com/DanHioara01/KINETIC.git
cd KINETIC
```

### 2. Firebase setup

1. Create a project at the [Firebase Console](https://console.firebase.google.com).
2. Add an **Android app** with package name `com.example.kinetic`.
3. Download `google-services.json` and place it in `app/src/main/google-services.json`.
4. *(Optional)* Enable **Email/Password**, **Google**, and **Facebook** sign-in providers.

> ⚠️ Never commit `serviceAccountKey.json` or other Firebase service-account keys. They are git-ignored.

### 3. Backend services (optional, for social features)

```bash
# Social API — Node.js + SQLite
cd backend && npm install && npm start      # listens on http://localhost:4242

# Firebase Cloud Functions
cd functions && npm install && npm run deploy

# AI Trainer — Python
cd ai_server && pip install -r requirements.txt && uvicorn server:app
```

---

## 🔨 Build

```bash
# Debug APK (installs on any device)
./gradlew :app:assembleDebug

# Release APK (unsigned — sign with your own keystore)
./gradlew :app:assembleRelease

# Run unit tests
./gradlew test
```

The signed APK output lands in `app/build/outputs/apk/`. Latest release builds are published on the **[Releases](https://github.com/DanHioara01/KINETIC/releases)** page.

---

## 📡 API Overview

The `backend/` Express server exposes a REST API with Firebase Auth verification:

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/health` | Health check |
| `POST` | `/users` | Create user profile |
| `GET` | `/users/search?q=` | Search users |
| `GET` | `/users/:id` | Get user profile |
| `POST` | `/friends/request` · `/accept` · `/reject` · `/remove` | Friend management |
| `GET` | `/friends/:userId` | List friends |
| `POST` | `/posts` · `/comments` | Create posts & comments |
| `GET` | `/feed?limit=&offset=` | Paginated social feed |
| `POST` | `/posts/:postId/like` · `DELETE` | Like / unlike |
| `GET` | `/leaderboard` | Rankings |
| `POST` | `/workouts/log` | Log workout (auto-awards badges) |
| `GET` | `/badges` · `/badges/user/:userId` | Badge catalog & user badges |
| `GET` | `/streaks/:userId` | Streak data |
| `POST` | `/sync/:table/bulk` | Bulk offline sync |

---

## 📁 Project Structure

```
KINETIC/
├── app/                          # Android app
│   ├── src/main/java/com/example/kinetic/
│   │   ├── MainActivity.kt       # Entry point & navigation host
│   │   ├── MainViewModel.kt      # Global ViewModel
│   │   ├── AppDatabase.kt        # Room database (migrations, v17)
│   │   ├── Models.kt             # Data models
│   │   ├── *Screen.kt            # 85+ UI screens
│   │   ├── *Manager.kt           # Feature managers (auth, food, AI, ads…)
│   │   └── ui/theme/             # Design system (palette, type, colors)
│   └── src/main/res/             # Resources (drawables, fonts, strings)
├── backend/                      # Node.js social API (Express + SQLite)
├── functions/                    # Firebase Cloud Functions (TypeScript)
├── ai_server/                    # Python AI Trainer (FastAPI)
├── scripts/                      # Dev tooling scripts
├── gradle/                       # Version catalog & wrapper
└── build.gradle.kts              # Project build config
```

---

## 🤝 Contributing

1. Fork the repository.
2. Create a feature branch: `git checkout -b feat/my-feature`
3. Commit your changes: `git commit -m "feat: add my feature"`
4. Push: `git push origin feat/my-feature`
5. Open a Pull Request.

Please keep changes focused, write clear commit messages, and make sure the project builds (`./gradlew assembleDebug`) before submitting.

---

## 📄 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

---

<div align="center">
  Made with ❤️ for people who train hard.
</div>
