# Kinetic - Android Gym Tracker App

## Project Structure
- **`app/src/main/java/com/example/kinetic/`** - Main Kotlin source code (85+ files)
- **`backend/`** - Node.js Express server (PostgreSQL + Firebase Auth)
- **`functions/`** - Firebase Cloud Functions (TypeScript)
- **`ai_server/`** - Python AI server (at Desktop level, not in this folder)
- **`gradle/`** - Gradle wrapper and version catalog

## Key Source Files (app/src/main/java/com/example/kinetic/)
- `MainActivity.kt` - Main entry point and UI (very large file, ~6000+ lines)
- `MainViewModel.kt` - Main ViewModel
- `AppDatabase.kt` - Room database definition
- `Models.kt` - Data models
- `Navigation.kt` - Navigation routes
- `WorkoutNavHost.kt` - Workout navigation graph
- `LanguageManager.kt` - Multi-language support
- `PreferencesManager.kt` - SharedPreferences manager
- `UserProfileManager.kt` - User profile management
- `RevenueCatManager.kt` - In-app purchases
- `FeatureAccessManager.kt` - Premium feature gating
- `SyncRepository.kt` - Offline sync
- `ui/theme/` - Theme colors, typography

## Build System
- Kotlin 2.0.21, Compose BOM 2024.04.01
- Room 2.6.1 with KSP
- Firebase BOM 34.14.0
- Gradle 9.6.1, AGP 8.7.0
- Package: `com.example.kinetic`
- Min SDK 24, Target SDK 35

## Do NOT read/search these directories
- `backend/node_modules/`
- `functions/node_modules/`
- `build/`, `app/build/`
- `.gradle/`, `.kotlin/`, `.idea/`

## Testing
- Unit tests: `app/src/test/`
- Instrumented tests: `app/src/androidTest/`
