# MIGRATION NOTES — Per-Account Data Isolation

## Overview

All user data in the app is now isolated per account (`userId`). Previously, SharedPreferences and Room DB stored data with fixed keys / no user filter, so any account logged in on the same device saw the same data.

---

## Files Modified

### Room Database (`AppDatabase.kt`)
- **`MuscleRecoveryEntity`** — Added `userId: String` field; changed primary key to composite `("grupaMusculara", "userId")`.
- **`ExerciseMetadataEntity`** — Added `userId: String` field; changed primary key to composite `("exerciseName", "userId")`.
- **`ExercitiuDao`** — 3 queries that had hardcoded `'simple'` userId now accept `:userId` as parameter.
- **`MuscleRecoveryDao`** — All queries (`getByGroup`, `getAll`, `delete`, `upsert`) now filter by `userId`.
- **`ExerciseMetadataDao`** — All queries (`getByName`, `getByGroup`, `getAll`, `setFavorite`, `delete`, `upsert`) now filter by `userId`.
- **`MIGRATION_16_17`** — New migration: recreates `muscle_recovery` and `exercise_metadata` tables with composite primary keys. Existing rows are assigned `userId = 'simple'` (the old default).
- **DB version** bumped from 16 → 17.

### Repository (`AntrenamentRepository.kt`)
- 11 methods now accept `userId: String` as first parameter: `getExercitiiPentruGrupaSimple`, `adaugaExercitiuCustom`, `setFavoriteSimple`, `getIstoricExercitiu`, `getStatisticiExercitiu`, `getVolumeSummary`, `getRecuperareMusculara`, `getToateRecuperarile`, `updateMuscleRecovery`, `incarcaUltimulAntrenament`, `getProgresLunar`.
- All entity construction (`MuscleRecoveryEntity`, `ExerciseMetadataEntity`) now includes the `userId` field.

### ViewModel (`MainViewModel.kt`)
- All callback-style methods now accept `userId` as first parameter and forward it to the repository.
- Added reactive collection of `CurrentUserProvider.currentUserId` — automatically reloads workouts, exercises, volumes, and PRs when the account changes.

### UI — Main Activity (`MainActivity.kt`)
- Removed all 10 references to `AppConstants.DEFAULT_USER_ID`; replaced with the actual `userId` resolved from `UserProfileManager`.
- Added `val userId = remember { UserProfileManager(context).getOwnUserId() }` in 5 Composable functions: `ExerciseListScreen`, `ExerciseInputScreen`, `RecoveryBarForGroup`, `MuscleRecoveryScreen`, `CalendarScreen`.
- Dashboard `LaunchedEffect` now includes `userId` as a key, triggering a re-query on account switch.
- CSV export and import now use the actual `userId`.

### Reactive User Provider (`CurrentUserProvider.kt`) — NEW FILE
- Singleton `CurrentUserProvider` with `StateFlow<String>` exposing the active userId.
- `refresh()` called after every login (`LoginHandler.completeLogin()`) and every logout (both logout handlers in `MainActivity`).
- `MainViewModel` collects this flow to auto-reload data on account switch.

### Login Handler (`LoginHandler.kt`)
- `completeLogin()` now calls `CurrentUserProvider.getInstance().refresh()` after setting session state.

### Sync (`SyncRepository.kt`, `SyncWorker.kt`, `GymLogApplication.kt`)
- `SyncRepository` — `saveMuscleRecovery` and `saveExerciseMetadata` now use `withUuid.userId` instead of `DEFAULT_USER_ID`.
- `SyncWorker` and `GymLogApplication.backgroundSync` — Changed login state read from `"theme_prefs"` to `"session_prefs"` (the correct file).

### Other Screens
- **`StatsScreen.kt`** — Removed `DEFAULT_USER_ID` default; now requires explicit `userId` parameter.
- **`CalendarWorkoutScreen.kt`** — Same: removed default, requires explicit `userId`.
- **`AiTrainerManager.kt`** — `getToateRecuperarile()` now passes `userId`.
- **`RestDayScreen.kt`** — Two calls to `getHistoryForExerciseSimple` now pass `userId`.
- **`WaterReminderReceiver.kt`** — Removed unused stale `"theme_prefs"` read.

---

## SharedPreferences Isolation (Already Done Previously)

| File | Purpose |
|------|---------|
| `session_prefs` | Login state (`logged_in`, `login_method`, `guest_key`) — shared, not per-user |
| `user_data_$userId` | Per-account: water intake, weight, onboarding flags, theme, units |
| `user_profiles` | Known profiles directory (shared by design) |

---

## Room DB Migration Strategy (v16 → v17)

Existing rows in `muscle_recovery` and `exercise_metadata` are assigned `userId = 'simple'` during migration. This is the old hardcoded default. Users who had data before this update will see their existing data when logged in with a userId of `'simple'`. All other accounts will start with empty tables for these entities.

**No destructive migration** — all workout history (`antrenamente`, `exercitii`) tables already had `userId` and are unaffected.

---

## Regression Test Checklist

For EACH data category, perform:
1. Login Account A → enter distinct data, note values.
2. Logout → Login Account B → verify empty/default data, enter distinct data for B.
3. Logout → Login Account A again → verify EXACTLY the data from step 1.
4. Repeat with Guest as one of the accounts.

| Data Category | Where to Check | Isolated? |
|---------------|----------------|-----------|
| Water intake | WaterTrackingScreen | ✅ (SharedPreferences per-user) |
| Body weight / height | BiometricInputScreen | ✅ (SharedPreferences per-user) |
| Onboarding flags | WelcomeScreen / ProfileSetupScreen | ✅ (SharedPreferences per-user) |
| Workouts (antrenamente) | Dashboard, CalendarWorkoutScreen | ✅ (Room DB with userId filter) |
| Exercise history (exerciții) | ExerciseInputScreen, StatsScreen | ✅ (Room DB with userId filter) |
| Muscle recovery | MuscleRecoveryScreen | ✅ (Room DB with userId + composite PK) |
| Exercise metadata (favorites, custom) | ExerciseListScreen | ✅ (Room DB with userId + composite PK) |
| Personal records | StatsScreen | ✅ (Room DB with userId filter) |
| Biometric data | BiometricChartScreen | ✅ (Room DB with userId filter) |
| Food journal | FoodJournalScreen | ✅ (Room DB with userId filter) |
| Social posts / friends | FriendsScreen, Feed | ✅ (Room DB with userId filter) |
| Theme / units | App-wide | ✅ (SharedPreferences per-user) |
| Language | App-wide | ❌ Shared (intentional — app-level setting) |
| GPS cardio | GpsCardioScreen | ✅ (uses userId from Composable) |
| Streaks / badges | Dashboard | ✅ (Room DB with userId) |
