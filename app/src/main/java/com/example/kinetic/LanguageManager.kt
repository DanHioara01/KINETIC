package com.example.kinetic
import android.content.Context
import android.content.SharedPreferences
import java.util.Locale
object LanguageManager {
    private const val PREF_NAME = "lang_prefs"
    private const val KEY_LANGUAGE = "current_language"
    private var currentLanguage: String = ""
    class Strings(m: Map<String, String>) {
        val dashboard: String = m["dashboard"] ?: ""
        val acasa: String = m["acasa"] ?: ""
        val workouts: String = m["workouts"] ?: ""
        val stats: String = m["stats"] ?: ""
        val waterIntake: String = m["waterIntake"] ?: ""
        val everyDay: String = m["everyDay"] ?: ""
        val reminder: String = m["reminder"] ?: ""
        val waterGoal: String = m["waterGoal"] ?: ""
        val addWater: String = m["addWater"] ?: ""
        val height: String = m["height"] ?: ""
        val personalInfo: String = m["personalInfo"] ?: ""
        val ml: String = m["ml"] ?: ""
        val weeklyHistory: String = m["weeklyHistory"] ?: ""
        val tips: String = m["tips"] ?: ""
        val customMl: String = m["customMl"] ?: ""
        val average: String = m["average"] ?: ""
        val target: String = m["target"] ?: ""
        val waterTip1: String = m["waterTip1"] ?: ""
        val waterTip2: String = m["waterTip2"] ?: ""
        val templates: String = m["templates"] ?: ""
        val recovery: String = m["recovery"] ?: ""
        val friends: String = m["friends"] ?: ""
        val leaderboard: String = m["leaderboard"] ?: ""
        val all: String = m["all"] ?: ""
        val language: String = m["language"] ?: ""
        val units: String = m["units"] ?: ""
        val logout: String = m["logout"] ?: ""
        val login: String = m["login"] ?: ""
        val signUp: String = m["signUp"] ?: ""
        val email: String = m["email"] ?: ""
        val password: String = m["password"] ?: ""
        val forgotPassword: String = m["forgotPassword"] ?: ""
        val loginAsGuest: String = m["loginAsGuest"] ?: ""
        val goalStrength: String = m["goalStrength"] ?: ""
        val goalMass: String = m["goalMass"] ?: ""
        val goalWeightLoss: String = m["goalWeightLoss"] ?: ""
        val goalMaintenance: String = m["goalMaintenance"] ?: ""
        val selectGoal: String = m["selectGoal"] ?: ""
        val stepOf: String = m["stepOf"] ?: ""
        val whatsYourAge: String = m["whatsYourAge"] ?: ""
        val whatsYourGender: String = m["whatsYourGender"] ?: ""
        val male: String = m["male"] ?: ""
        val female: String = m["female"] ?: ""
        val whatsYourActivityLevel: String = m["whatsYourActivityLevel"] ?: ""
        val sedentary: String = m["sedentary"] ?: ""
        val sedentaryDesc: String = m["sedentaryDesc"] ?: ""
        val active: String = m["active"] ?: ""
        val activeDesc: String = m["activeDesc"] ?: ""
        val veryActive: String = m["veryActive"] ?: ""
        val veryActiveDesc: String = m["veryActiveDesc"] ?: ""
        val remaining: String = m["remaining"] ?: ""
        val whatsYourExperience: String = m["whatsYourExperience"] ?: ""
        val beginnerLabel: String = m["beginnerLabel"] ?: ""
        val beginnerDesc: String = m["beginnerDesc"] ?: ""
        val intermediateLabel: String = m["intermediateLabel"] ?: ""
        val intermediateDesc: String = m["intermediateDesc"] ?: ""
        val advancedLabel: String = m["advancedLabel"] ?: ""
        val advancedDesc: String = m["advancedDesc"] ?: ""
        val whatEquipment: String = m["whatEquipment"] ?: ""
        val homeNoEquip: String = m["homeNoEquip"] ?: ""
        val homeNoEquipDesc: String = m["homeNoEquipDesc"] ?: ""
        val homeDumbbells: String = m["homeDumbbells"] ?: ""
        val homeDumbbellsDesc: String = m["homeDumbbellsDesc"] ?: ""
        val fullGym: String = m["fullGym"] ?: ""
        val fullGymDesc: String = m["fullGymDesc"] ?: ""
        val profileGoalLabel: String = m["profileGoalLabel"] ?: "Goal"
        val profileExperienceLabel: String = m["profileExperienceLabel"] ?: "Experience"
        val profileEquipmentLabel: String = m["profileEquipmentLabel"] ?: "Equipment"
        val trainingFrequency: String = m["trainingFrequency"] ?: ""
        val sessionsPerWeek: String = m["sessionsPerWeek"] ?: ""
        val selectTrainingDays: String = m["selectTrainingDays"] ?: ""
        val monday: String = m["monday"] ?: ""
        val tuesday: String = m["tuesday"] ?: ""
        val wednesday: String = m["wednesday"] ?: ""
        val thursday: String = m["thursday"] ?: ""
        val friday: String = m["friday"] ?: ""
        val saturday: String = m["saturday"] ?: ""
        val sunday: String = m["sunday"] ?: ""
        val physicalLimitations: String = m["physicalLimitations"] ?: ""
        val physicalLimitationsPlaceholder: String = m["physicalLimitationsPlaceholder"] ?: ""
        val whichMuscleGroups: String = m["whichMuscleGroups"] ?: ""
        val selectAtLeastOne: String = m["selectAtLeastOne"] ?: ""
        val next: String = m["next"] ?: ""
        val skip: String = m["skip"] ?: ""
        val finish: String = m["finish"] ?: ""
        val back: String = m["back"] ?: ""
        val profileSetup: String = m["profileSetup"] ?: ""
        val enterName: String = m["enterName"] ?: ""
        val pickPhoto: String = m["pickPhoto"] ?: ""
        val saveProfile: String = m["saveProfile"] ?: ""
        val saveExercise: String = m["saveExercise"] ?: ""
        val chest: String = m["chest"] ?: ""
        val shoulders: String = m["shoulders"] ?: ""
        val arms: String = m["arms"] ?: ""
        val biceps: String = m["biceps"] ?: ""
        val triceps: String = m["triceps"] ?: ""
        val legs: String = m["legs"] ?: ""
        val thighs: String = m["thighs"] ?: ""
        val glutes: String = m["glutes"] ?: ""
        val calves: String = m["calves"] ?: ""
        val core: String = m["core"] ?: ""
        val cardio: String = m["cardio"] ?: ""
        val fullBody: String = m["fullBody"] ?: ""
        val pleaseSelectOption: String = m["pleaseSelectOption"] ?: ""
        val sets: String = m["sets"] ?: ""
        val reps: String = m["reps"] ?: ""
        val weight: String = m["weight"] ?: ""
        val startWorkout: String = m["startWorkout"] ?: ""
        val nextExercise: String = m["nextExercise"] ?: ""
        val notes: String = m["notes"] ?: ""
        val cancel: String = m["cancel"] ?: ""
        val confirm: String = m["confirm"] ?: ""
        val delete: String = m["delete"] ?: ""
        val edit: String = m["edit"] ?: ""
        val search: String = m["search"] ?: ""
        val noDataYet: String = m["noDataYet"] ?: ""
        val sendRequest: String = m["sendRequest"] ?: ""
        val accept: String = m["accept"] ?: ""
        val reject: String = m["reject"] ?: ""
        val removeFriend: String = m["removeFriend"] ?: ""
        val noFriends: String = m["noFriends"] ?: ""
        val searchUsers: String = m["searchUsers"] ?: ""
        val searchByNameOrId: String = m["searchByNameOrId"] ?: ""
        val incomingRequests: String = m["incomingRequests"] ?: ""
        val yourFriends: String = m["yourFriends"] ?: ""
        val friendRequestSent: String = m["friendRequestSent"] ?: ""
        val feedEmpty: String = m["feedEmpty"] ?: ""
        val workoutCompleted: String = m["workoutCompleted"] ?: ""
        val streakLabel: String = m["streakLabel"] ?: ""
        val bestStreak: String = m["bestStreak"] ?: ""
        val badges: String = m["badges"] ?: ""
        val kg: String = m["kg"] ?: ""
        val xp: String = m["xp"] ?: ""
        val max: String = m["max"] ?: ""
        val ok: String = m["ok"] ?: ""
        val lv: String = m["lv"] ?: ""
        val lbs: String = m["lbs"] ?: ""
        val exportCsv: String = m["exportCsv"] ?: ""
        val importCsv: String = m["importCsv"] ?: ""
        val subscription: String = m["subscription"] ?: ""
        val premium: String = m["premium"] ?: ""
        val subscribe: String = m["subscribe"] ?: ""
        val error: String = m["error"] ?: ""
        val retry: String = m["retry"] ?: ""
        val recoveryInfo: String = m["recoveryInfo"] ?: ""
        val recommendedRecovery: String = m["recommendedRecovery"] ?: ""
        val progressChart: String = m["progressChart"] ?: ""
        val weightProgression: String = m["weightProgression"] ?: ""
        val calendarView: String = m["calendarView"] ?: ""
        val allGroups: String = m["allGroups"] ?: ""
        val friendRequestNotificationTitle: String = m["friendRequestNotificationTitle"] ?: ""
        val friendRequestNotificationText: String = m["friendRequestNotificationText"] ?: ""
        val selectLanguage: String = m["selectLanguage"] ?: ""
        val workoutsLabel: String = m["workoutsLabel"] ?: ""
        val currentStreakLabel: String = m["currentStreakLabel"] ?: ""
        val bestStreakLabel: String = m["bestStreakLabel"] ?: ""
        val days: String = m["days"] ?: ""
        val deleteAccount: String = m["deleteAccount"] ?: ""
        val exerciseHistory: String = m["exerciseHistory"] ?: ""
        val favorite: String = m["favorite"] ?: ""
        val savedExercises: String = m["savedExercises"] ?: ""
        val noFavorites: String = m["noFavorites"] ?: ""
        val tapStarToSave: String = m["tapStarToSave"] ?: ""
        val removeFavorite: String = m["removeFavorite"] ?: ""
        val howToGet: String = m["howToGet"] ?: ""
        val addSet: String = m["addSet"] ?: ""
        val estimatedOneRm: String = m["estimatedOneRm"] ?: ""
        val nextSetSuggestion: String = m["nextSetSuggestion"] ?: ""
        val setTypeWarmup: String = m["setTypeWarmup"] ?: ""
        val setTypeWorking: String = m["setTypeWorking"] ?: ""
        val setTypeDrop: String = m["setTypeDrop"] ?: ""
        val setTypeAmrap: String = m["setTypeAmrap"] ?: ""
        val setTypePaused: String = m["setTypePaused"] ?: ""
        val setTypeTempo: String = m["setTypeTempo"] ?: ""
        val rpeLabel: String = m["rpeLabel"] ?: ""
        val readinessTitle: String = m["readinessTitle"] ?: ""
        val messages: String = m["messages"] ?: "Messages"
        val readinessScore: String = m["readinessScore"] ?: ""
        val readinessHeavy: String = m["readinessHeavy"] ?: ""
        val readinessModerate: String = m["readinessModerate"] ?: ""
        val readinessLight: String = m["readinessLight"] ?: ""
        val readinessSleep: String = m["readinessSleep"] ?: ""
        val readinessSleepHours: String = m["readinessSleepHours"] ?: ""
        val readinessQuality: String = m["readinessQuality"] ?: ""
        val readinessSteps: String = m["readinessSteps"] ?: ""
        val readinessRecovery: String = m["readinessRecovery"] ?: ""
        val readinessVolume: String = m["readinessVolume"] ?: ""
        val readinessHint: String = m["readinessHint"] ?: ""
        val readinessHydration: String = m["readinessHydration"] ?: ""
        val readinessTrend: String = m["readinessTrend"] ?: ""
        val readinessActionSleep: String = m["readinessActionSleep"] ?: ""
        val readinessActionSteps: String = m["readinessActionSteps"] ?: ""
        val readinessActionRecovery: String = m["readinessActionRecovery"] ?: ""
        val readinessActionVolume: String = m["readinessActionVolume"] ?: ""
        val readinessActionHydration: String = m["readinessActionHydration"] ?: ""
        val readinessIntensityHeavy: String = m["readinessIntensityHeavy"] ?: ""
        val readinessIntensityModerate: String = m["readinessIntensityModerate"] ?: ""
        val readinessIntensityLight: String = m["readinessIntensityLight"] ?: ""
        val exerciseNotes: String = m["exerciseNotes"] ?: ""
        val saveNotes: String = m["saveNotes"] ?: ""
        val volume: String = m["volume"] ?: ""
        val maxWeight: String = m["maxWeight"] ?: ""
        val maxReps: String = m["maxReps"] ?: ""
        val maxSet: String = m["maxSet"] ?: ""
        val today: String = m["today"] ?: ""
        val thisWeek: String = m["thisWeek"] ?: ""
        val thisMonth: String = m["thisMonth"] ?: ""
        val totalVolumeLabel: String = m["totalVolumeLabel"] ?: ""
        val guest: String = m["guest"] ?: ""
        val loginWithGoogle: String = m["loginWithGoogle"] ?: ""
        val loginWithFacebook: String = m["loginWithFacebook"] ?: ""
        val close: String = m["close"] ?: ""
        val profile: String = m["profile"] ?: ""
        val appTagline: String = m["appTagline"] ?: ""
        val or: String = m["or"] ?: ""
        val dark: String = m["dark"] ?: ""
        val light: String = m["light"] ?: ""
        val system: String = m["system"] ?: ""
        val selectTheme: String = m["selectTheme"] ?: ""
        val settingsAndMore: String = m["settingsAndMore"] ?: ""
        val muscleGroups: String = m["muscleGroups"] ?: ""
        val features: String = m["features"] ?: ""
        val activity: String = m["activity"] ?: ""
        val tools: String = m["tools"] ?: ""
        val englishUS: String = m["englishUS"] ?: ""
        val romana: String = m["romana"] ?: ""
        val russkiy: String = m["russkiy"] ?: ""
        val ukrainska: String = m["ukrainska"] ?: ""
        val francais: String = m["francais"] ?: ""
        val deutsch: String = m["deutsch"] ?: ""
        val espanol: String = m["espanol"] ?: ""
        val italiano: String = m["italiano"] ?: ""
        val turkce: String = m["turkce"] ?: ""
        val portugues: String = m["portugues"] ?: ""
        val polski: String = m["polski"] ?: ""
        val add: String = m["add"] ?: ""
        val demoExercise: String = m["demoExercise"] ?: ""
        val prAndVolume: String = m["prAndVolume"] ?: ""
        val start: String = m["start"] ?: ""
        val noSavedSetsYet: String = m["noSavedSetsYet"] ?: ""
        val editSet: String = m["editSet"] ?: ""
        val exercises: String = m["exercises"] ?: ""
        val recovered: String = m["recovered"] ?: ""
        val almostRecovered: String = m["almostRecovered"] ?: ""
        val moderate: String = m["moderate"] ?: ""
        val tired: String = m["tired"] ?: ""
        val exhausted: String = m["exhausted"] ?: ""
        val fatigue: String = m["fatigue"] ?: ""
        val chooseMuscleGroup: String = m["chooseMuscleGroup"] ?: ""
        val noExercisesFound: String = m["noExercisesFound"] ?: ""
        val tryDifferentFilter: String = m["tryDifferentFilter"] ?: ""
        val voiceSearch: String = m["voiceSearch"] ?: ""
        val voiceSearchError: String = m["voiceSearchError"] ?: ""
        val monthlyProgress: String = m["monthlyProgress"] ?: ""
        val completeWorkoutsToSee: String = m["completeWorkoutsToSee"] ?: ""
        val jan: String = m["jan"] ?: ""
        val feb: String = m["feb"] ?: ""
        val mar: String = m["mar"] ?: ""
        val apr: String = m["apr"] ?: ""
        val may: String = m["may"] ?: ""
        val jun: String = m["jun"] ?: ""
        val jul: String = m["jul"] ?: ""
        val aug: String = m["aug"] ?: ""
        val sep: String = m["sep"] ?: ""
        val oct: String = m["oct"] ?: ""
        val nov: String = m["nov"] ?: ""
        val dec: String = m["dec"] ?: ""
        val mon: String = m["mon"] ?: ""
        val tue: String = m["tue"] ?: ""
        val wed: String = m["wed"] ?: ""
        val thu: String = m["thu"] ?: ""
        val fri: String = m["fri"] ?: ""
        val sat: String = m["sat"] ?: ""
        val sun: String = m["sun"] ?: ""
        val noWorkouts: String = m["noWorkouts"] ?: ""
        val monthlyDetails: String = m["monthlyDetails"] ?: ""
        val month: String = m["month"] ?: ""
        val subscribeNow: String = m["subscribeNow"] ?: ""
        val premiumFeature: String = m["premiumFeature"] ?: ""
        val subscribersOnly: String = m["subscribersOnly"] ?: ""
        val choosePlan: String = m["choosePlan"] ?: ""
        val youAreSubscribed: String = m["youAreSubscribed"] ?: ""
        val unlockPremiumTitle: String = m["unlockPremiumTitle"] ?: ""
        val unlockPremiumSubtitle: String = m["unlockPremiumSubtitle"] ?: ""
        val freePlan: String = m["freePlan"] ?: ""
        val permanentPlan: String = m["permanentPlan"] ?: ""
        val lifetimeAccess: String = m["lifetimeAccess"] ?: ""
        val restorePurchase: String = m["restorePurchase"] ?: ""
        val watchAdToUnlock: String = m["watchAdToUnlock"] ?: ""
        val unlockedForMinutes: String = m["unlockedForMinutes"] ?: ""
        val currentPlan: String = m["currentPlan"] ?: ""
        val bestValue: String = m["bestValue"] ?: ""
        val mostPopular: String = m["mostPopular"] ?: ""
        val perMonth: String = m["perMonth"] ?: ""
        val perYear: String = m["perYear"] ?: ""
        val oneTimePayment: String = m["oneTimePayment"] ?: ""
        val upgradeToUnlock: String = m["upgradeToUnlock"] ?: ""
        val buyNow: String = m["buyNow"] ?: ""
        val free: String = m["free"] ?: ""
        val purchaseSuccess: String = m["purchaseSuccess"] ?: ""
        val purchaseFailed: String = m["purchaseFailed"] ?: ""
        val purchaseCancelled: String = m["purchaseCancelled"] ?: ""
        val restoreSuccess: String = m["restoreSuccess"] ?: ""
        val noPurchasesToRestore: String = m["noPurchasesToRestore"] ?: ""
        val adUnlockSuccess: String = m["adUnlockSuccess"] ?: ""
        val adNotReady: String = m["adNotReady"] ?: ""
        val dailyAdLimitReached: String = m["dailyAdLimitReached"] ?: ""
        val cancelAnytime: String = m["cancelAnytime"] ?: ""
        val workoutAnalytics: String = m["workoutAnalytics"] ?: ""
        val muscleRecovery: String = m["muscleRecovery"] ?: ""
        val waterReminderTitle: String = m["waterReminderTitle"] ?: ""
        val waterReminderText: String = m["waterReminderText"] ?: ""
        val selectTime: String = m["selectTime"] ?: ""
        val forearms: String = m["forearms"] ?: ""
        val neckAndTraps: String = m["neckAndTraps"] ?: ""
        val welcome: String = m["welcome"] ?: ""
        val athlete: String = m["athlete"] ?: ""
        val biometricTracking: String = m["biometricTracking"] ?: ""
        val addMeasurement: String = m["addMeasurement"] ?: ""
        val bodyFat: String = m["bodyFat"] ?: ""
        val waistCirc: String = m["waistCirc"] ?: ""
        val hipsCirc: String = m["hipsCirc"] ?: ""
        val thighsCirc: String = m["thighsCirc"] ?: ""
        val chestCirc: String = m["chestCirc"] ?: ""
        val armsCirc: String = m["armsCirc"] ?: ""
        val noMeasurements: String = m["noMeasurements"] ?: ""
        val weeksAgo: String = m["weeksAgo"] ?: ""
        val cm: String = m["cm"] ?: ""
        val percent: String = m["percent"] ?: ""
        val deleteMeasurement: String = m["deleteMeasurement"] ?: ""
        val biometricHistory: String = m["biometricHistory"] ?: ""
        val weightChart: String = m["weightChart"] ?: ""
        val bodyFatChart: String = m["bodyFatChart"] ?: ""
        val circumferenceChart: String = m["circumferenceChart"] ?: ""
        val biometricReminderTitle: String = m["biometricReminderTitle"] ?: ""
        val biometricReminderText: String = m["biometricReminderText"] ?: ""
        val streakChannelName: String = m["streakChannelName"] ?: ""
        val streakReminderTitle: String = m["streakReminderTitle"] ?: ""
        val streakReminderText: String = m["streakReminderText"] ?: ""
        val welcomeSoundLabel: String = m["welcomeSoundLabel"] ?: ""
        val foodJournal: String = m["foodJournal"] ?: ""
        val dailyIntake: String = m["dailyIntake"] ?: ""
        val bio: String = m["bio"] ?: ""
        val currentPassword: String = m["currentPassword"] ?: ""
        val newPassword: String = m["newPassword"] ?: ""
        val confirmNewPassword: String = m["confirmNewPassword"] ?: ""
        val changeLabel: String = m["changeLabel"] ?: ""
        val currentPasswordRequired: String = m["currentPasswordRequired"] ?: ""
        val passwordTooShort: String = m["passwordTooShort"] ?: ""
        val enterPasswordToConfirm: String = m["enterPasswordToConfirm"] ?: ""
        val passwordRequiredToDelete: String = m["passwordRequiredToDelete"] ?: ""
        val lowLabel: String = m["lowLabel"] ?: ""
        val highLabel: String = m["highLabel"] ?: ""
        val newChat: String = m["newChat"] ?: ""
        val serverSettings: String = m["serverSettings"] ?: ""
        val deloadWhyTitle: String = m["deloadWhyTitle"] ?: ""
        val deloadWhyBody: String = m["deloadWhyBody"] ?: ""
        val deloadDuration: String = m["deloadDuration"] ?: ""
        val deload1Week: String = m["deload1Week"] ?: ""
        val deload2Weeks: String = m["deload2Weeks"] ?: ""
        val deloadReduction: String = m["deloadReduction"] ?: ""
        val deloadCompound: String = m["deloadCompound"] ?: ""
        val deloadNoHistory: String = m["deloadNoHistory"] ?: ""
        val editRestDay: String = m["editRestDay"] ?: ""
        val deloadDayOf: String = m["deloadDayOf"] ?: ""
        val gpsSearching: String = m["gpsSearching"] ?: ""
        val gpsError: String = m["gpsError"] ?: ""
        val running: String = m["running"] ?: ""
        val cycling: String = m["cycling"] ?: ""
        val walking: String = m["walking"] ?: ""
        val routePoints: String = m["routePoints"] ?: ""
        val paused: String = m["paused"] ?: ""
        val steps: String = m["steps"] ?: ""
        val timerFinished: String = m["timerFinished"] ?: ""
        val timeToStartNextSet: String = m["timeToStartNextSet"] ?: ""
        val scan: String = m["scan"] ?: ""
        val scanning: String = m["scanning"] ?: ""
        val scanBarcodeHelp: String = m["scanBarcodeHelp"] ?: ""
        val noFoodEntries: String = m["noFoodEntries"] ?: ""
        val todaysMacros: String = m["todaysMacros"] ?: ""
        val caloriesLabel: String = m["caloriesLabel"] ?: ""
        val stepsLabel: String = m["stepsLabel"] ?: ""
        val activeTimeLabel: String = m["activeTimeLabel"] ?: ""
        val proteinLabel: String = m["proteinLabel"] ?: ""
        val carbsLabel: String = m["carbsLabel"] ?: ""
        val fatLabel: String = m["fatLabel"] ?: ""
        val breakfast: String = m["breakfast"] ?: ""
        val lunch: String = m["lunch"] ?: ""
        val dinner: String = m["dinner"] ?: ""
        val snack: String = m["snack"] ?: ""
        val drinks: String = m["drinks"] ?: ""
        val selectMealType: String = m["selectMealType"] ?: ""
        val foodName: String = m["foodName"] ?: ""
        val brandLabel: String = m["brandLabel"] ?: ""
        val fiber: String = m["fiber"] ?: ""
        val searchFood: String = m["searchFood"] ?: ""
        val foodSearchHint: String = m["foodSearchHint"] ?: ""
        val quantity: String = m["quantity"] ?: ""
        val gramsShort: String = m["gramsShort"] ?: "g"
        val piecesShort: String = m["piecesShort"] ?: "pcs"
        val addToJournal: String = m["addToJournal"] ?: ""
        val manualEntryMode: String = m["manualEntryMode"] ?: ""
        val noFoodFound: String = m["noFoodFound"] ?: ""
        val enterManually: String = m["enterManually"] ?: ""
        val per100g: String = m["per100g"] ?: "per 100g"
        val perPiece: String = m["perPiece"] ?: "per piece"
        val aiTrainer: String = m["aiTrainer"] ?: ""
        val aiTrainerWelcome: String = m["aiTrainerWelcome"] ?: ""
        val aiTrainerHistory: String = m["aiTrainerHistory"] ?: ""
        val noHistoryYet: String = m["noHistoryYet"] ?: ""
        val current: String = m["current"] ?: ""
        val aiTrainerHint: String = m["aiTrainerHint"] ?: ""
        val askAiTrainer: String = m["askAiTrainer"] ?: ""
        val aiSuggestion1: String = m["aiSuggestion1"] ?: ""
        val aiSuggestion2: String = m["aiSuggestion2"] ?: ""
        val aiSuggestion3: String = m["aiSuggestion3"] ?: ""
        val aiSuggestion4: String = m["aiSuggestion4"] ?: ""
        val mottoMessages: List<String> = listOf(
            m["motto1"] ?: "", m["motto2"] ?: "", m["motto3"] ?: "", m["motto4"] ?: "",
            m["motto5"] ?: "", m["motto6"] ?: "", m["motto7"] ?: "", m["motto8"] ?: "",
            m["motto9"] ?: "", m["motto10"] ?: "", m["motto11"] ?: "", m["motto12"] ?: "",
            m["motto13"] ?: "", m["motto14"] ?: "", m["motto15"] ?: "", m["motto16"] ?: "",
            m["motto17"] ?: "", m["motto18"] ?: "", m["motto19"] ?: "", m["motto20"] ?: ""
        )
        val goodMorning: String = m["goodMorning"] ?: ""
        val goodAfternoon: String = m["goodAfternoon"] ?: ""
        val goodEvening: String = m["goodEvening"] ?: ""
        val daysConsecutive: String = m["daysConsecutive"] ?: ""
        val todaysWorkout: String = m["todaysWorkout"] ?: ""
        val todayYouRest: String = m["todayYouRest"] ?: ""
        val restDayMessage: String = m["restDayMessage"] ?: ""
        val restDayTip: String = m["restDayTip"] ?: ""
        val dayLabel: String = m["dayLabel"] ?: ""
        val ofCycle: String = m["ofCycle"] ?: ""
        val howDoYouFeel: String = m["howDoYouFeel"] ?: ""
        val tiredLabel: String = m["tiredLabel"] ?: ""
        val normalLabel: String = m["normalLabel"] ?: ""
        val energeticLabel: String = m["energeticLabel"] ?: ""
        val technicalTip: String = m["technicalTip"] ?: ""
        val weeklySummary: String = m["weeklySummary"] ?: "Weekly Summary"
        val lastWeekLabel: String = m["lastWeekLabel"] ?: "last week"
        val goalLabel: String = m["goalLabel"] ?: "Goal Tip"
        val volumeLabel: String = m["volumeLabel"] ?: "Volume"
        val topExerciseLabel: String = m["topExerciseLabel"] ?: "Top Exercise"
        val nutritionLabel: String = m["nutritionLabel"] ?: "Nutrition"
        val motivationLabel: String = m["motivationLabel"] ?: "Motivation"
        val gpsCardioMap: String = m["gpsCardioMap"] ?: ""
        val startTracking: String = m["startTracking"] ?: ""
        val pauseTracking: String = m["pauseTracking"] ?: ""
        val resumeTracking: String = m["resumeTracking"] ?: ""
        val distance: String = m["distance"] ?: ""
        val pace: String = m["pace"] ?: ""
        val speed: String = m["speed"] ?: ""
        val duration: String = m["duration"] ?: ""
        val savedRoutes: String = m["savedRoutes"] ?: ""
        val noSavedRoutes: String = m["noSavedRoutes"] ?: ""
        val routeName: String = m["routeName"] ?: ""
        val saveRoute: String = m["saveRoute"] ?: ""
        val deleteRoute: String = m["deleteRoute"] ?: ""
        val locationPermissionRequired: String = m["locationPermissionRequired"] ?: ""
        val gpsDisabledTitle: String = m["gpsDisabledTitle"] ?: ""
        val gpsDisabledMessage: String = m["gpsDisabledMessage"] ?: ""
        val openSettings: String = m["openSettings"] ?: ""
        val restDaysTitle: String = m["restDaysTitle"] ?: ""
        val restDaysSubtitle: String = m["restDaysSubtitle"] ?: ""
        val deloadWeek: String = m["deloadWeek"] ?: ""
        val recoverySchedule: String = m["recoverySchedule"] ?: ""
        val stretching: String = m["stretching"] ?: ""
        val lightYoga: String = m["lightYoga"] ?: ""
        val foamRolling: String = m["foamRolling"] ?: ""
        val nextRestDay: String = m["nextRestDay"] ?: ""
        val muscleNeedsRest: String = m["muscleNeedsRest"] ?: ""
        val deloadInfo: String = m["deloadInfo"] ?: ""
        val suggestedActivities: String = m["suggestedActivities"] ?: ""
        val activeRecovery: String = m["activeRecovery"] ?: ""
        val lightWalk: String = m["lightWalk"] ?: ""
        val save: String = m["save"] ?: ""
        val allGood: String = m["allGood"] ?: ""
        val alreadyHaveAccount: String = m["alreadyHaveAccount"] ?: ""
        val autoDeloadEnabled: String = m["autoDeloadEnabled"] ?: ""
        val avgRecovery: String = m["avgRecovery"] ?: ""
        val caloriesBurned: String = m["caloriesBurned"] ?: ""
        val confirmPassword: String = m["confirmPassword"] ?: ""
        val createAccountTitle: String = m["createAccountTitle"] ?: ""
        val deloadActive: String = m["deloadActive"] ?: ""
        val deloadHistory: String = m["deloadHistory"] ?: ""
        val deloadInterval: String = m["deloadInterval"] ?: ""
        val deloadNewValue: String = m["deloadNewValue"] ?: ""
        val deloadNormalValue: String = m["deloadNormalValue"] ?: ""
        val deloadPreview: String = m["deloadPreview"] ?: ""
        val deloadPreviewSubtitle: String = m["deloadPreviewSubtitle"] ?: ""
        val deloadActiveThisWeek: String = m["deloadActiveThisWeek"] ?: ""
        val recommendedForYou: String = m["recommendedForYou"] ?: ""
        val tapToSchedule: String = m["tapToSchedule"] ?: ""
        val dontHaveAccount: String = m["dontHaveAccount"] ?: ""
        val emailError: String = m["emailError"] ?: ""
        val endDeload: String = m["endDeload"] ?: ""
        val foamRollingDescription: String = m["foamRollingDescription"] ?: ""
        val heightCm: String = m["heightCm"] ?: ""
        val lissDescription: String = m["lissDescription"] ?: ""
        val loginInstead: String = m["loginInstead"] ?: ""
        val musclesTiredCount: String = m["musclesTiredCount"] ?: ""
        val nameError: String = m["nameError"] ?: ""
        val nameField: String = m["nameField"] ?: ""
        val optional: String = m["optional"] ?: ""
        val passwordError: String = m["passwordError"] ?: ""
        val passwordMismatch: String = m["passwordMismatch"] ?: ""
        val passwordStrengthMedium: String = m["passwordStrengthMedium"] ?: ""
        val passwordStrengthStrong: String = m["passwordStrengthStrong"] ?: ""
        val passwordStrengthWeak: String = m["passwordStrengthWeak"] ?: ""
        val privacyPolicyLink: String = m["privacyPolicyLink"] ?: ""
        val recoveryTargeted: String = m["recoveryTargeted"] ?: ""
        val startDeload: String = m["startDeload"] ?: ""
        val stretchingDescription: String = m["stretchingDescription"] ?: ""
        val warmupStretch: String = m["warmupStretch"] ?: ""
        val cooldownStretch: String = m["cooldownStretch"] ?: ""
        val termsAndConditions: String = m["termsAndConditions"] ?: ""
        val termsPrefix: String = m["termsPrefix"] ?: ""
        val timeForDeload: String = m["timeForDeload"] ?: ""
        val deloadReasonAuto: String = m["deloadReasonAuto"] ?: ""
        val deloadReasonGeneral: String = m["deloadReasonGeneral"] ?: ""
        val deloadDueBanner: String = m["deloadDueBanner"] ?: ""
        val weeks: String = m["weeks"] ?: ""
        val weeksSinceLastDeload: String = m["weeksSinceLastDeload"] ?: ""
        val weightKg: String = m["weightKg"] ?: ""
        val yogaDescription: String = m["yogaDescription"] ?: ""
        val mostTrained: String = m["mostTrained" ] ?: ""
        val allExercises: String = m["allExercises"] ?: ""
        val vsPrevious: String = m["vsPrevious"] ?: ""
        val sessions: String = m["sessions"] ?: ""
        val weightKgLabel: String = m["weightKgLabel"] ?: ""
        val repsLabel: String = m["repsLabel"] ?: ""
        val estimated1rm: String = m["estimated1rm"] ?: ""
        val intensityZones: String = m["intensityZones"] ?: ""
        val zoneStrength: String = m["zoneStrength"] ?: ""
        val zoneStrengthHypertrophy: String = m["zoneStrengthHypertrophy"] ?: ""
        val zoneHypertrophy: String = m["zoneHypertrophy"] ?: ""
        val zoneHypertrophyEndurance: String = m["zoneHypertrophyEndurance"] ?: ""
        val zoneEndurance: String = m["zoneEndurance"] ?: ""
        val weeklyTab: String = m["weeklyTab"] ?: ""
        val pbsTab: String = m["pbsTab"] ?: ""
        val keepTraining: String = m["keepTraining"] ?: ""
        val plateCalculatorTitle: String = m["plateCalculatorTitle"] ?: ""
        val menu: String = m["menu"] ?: ""
        val history: String = m["history"] ?: ""
        val deleteChat: String = m["deleteChat"] ?: ""
        val bodyAnatomy: String = m["bodyAnatomy"] ?: ""
        val leaveEmptyIfAuthDisabled: String = m["leaveEmptyIfAuthDisabled"] ?: ""
        val foodNamePlaceholder: String = m["foodNamePlaceholder"] ?: ""
        val brandPlaceholder: String = m["brandPlaceholder"] ?: ""
        val backendServerAddress: String = m["backendServerAddress"] ?: ""
        val aiApiKeyOptional: String = m["aiApiKeyOptional"] ?: ""
        val leaveEmptyForDefaultServer: String = m["leaveEmptyForDefaultServer"] ?: ""
        val kcal: String = m["kcal"] ?: ""
        val deleteAccountWarning: String = m["deleteAccountWarning"] ?: ""
        val setStepGoal: String = m["setStepGoal"] ?: ""
        val enterDailyStepGoal: String = m["enterDailyStepGoal"] ?: ""
        val floatingWindow: String = m["floatingWindow"] ?: ""
        val float: String = m["float"] ?: ""
        val clear: String = m["clear"] ?: ""
        val decrease: String = m["decrease"] ?: ""
        val increase: String = m["increase"] ?: ""
        val exercise: String = m["exercise"] ?: ""
        val top: String = m["top"] ?: ""
        val play: String = m["play"] ?: ""
        val reset: String = m["reset"] ?: ""
        val selected: String = m["selected"] ?: ""
        val loadingPlaylists: String = m["loadingPlaylists"] ?: ""
        val failedToLoadPlaylists: String = m["failedToLoadPlaylists"] ?: ""
        val noPlaylistsFound: String = m["noPlaylistsFound"] ?: ""
        val createPlaylistFirst: String = m["createPlaylistFirst"] ?: ""
        val authenticationFailed: String = m["authenticationFailed"] ?: ""
        val unknownError: String = m["unknownError"] ?: ""
        val selectPlaylist: String = m["selectPlaylist"] ?: ""
        val choosePlaylist: String = m["choosePlaylist"] ?: ""
        val tracks: String = m["tracks"] ?: ""
        val connectToSpotify: String = m["connectToSpotify"] ?: ""
        val spotifyAccessDescription: String = m["spotifyAccessDescription"] ?: ""
        val loginWithSpotify: String = m["loginWithSpotify"] ?: ""
        val spotifyRedirectInfo: String = m["spotifyRedirectInfo"] ?: ""
        val navy: String = m["navy"] ?: ""
        val bmi: String = m["bmi"] ?: ""
        val exportBackup: String = m["exportBackup"] ?: ""
        val targetWeightLabel: String = m["targetWeightLabel"] ?: ""
        val barWeightLabel: String = m["barWeightLabel"] ?: ""
        val lbsKg: String = m["lbsKg"] ?: ""
        val platesPerSide: String = m["platesPerSide"] ?: ""
        val plateUnit: String = m["plateUnit"] ?: ""
        val eachSide: String = m["eachSide"] ?: ""
        val total: String = m["total"] ?: ""
        val weightTooLight: String = m["weightTooLight"] ?: ""
        val plateCalcNote: String = m["plateCalcNote"] ?: ""
        val gender: String = m["gender"] ?: ""
        val age: String = m["age"] ?: ""
        val method: String = m["method"] ?: ""
        val waistCm: String = m["waistCm"] ?: ""
        val neckCm: String = m["neckCm"] ?: ""
        val hipsCm: String = m["hipsCm"] ?: ""
        val estimatedBodyFat: String = m["estimatedBodyFat"] ?: ""
        val navyMethodInfo: String = m["navyMethodInfo"] ?: ""
        val bodyFatCalculator: String = m["bodyFatCalculator"] ?: ""
        val newPRs: String = m["newPRs"] ?: ""
        val exerciseBreakdown: String = m["exerciseBreakdown"] ?: ""
        val done: String = m["done"] ?: ""
        val weightGoal: String = m["weightGoal"] ?: ""
        val currentWeight: String = m["currentWeight"] ?: ""
        val deadline: String = m["deadline"] ?: ""
        val goalDetails: String = m["goalDetails"] ?: ""
        val startWeight: String = m["startWeight"] ?: ""
        val targetWeight: String = m["targetWeight"] ?: ""
        val noActiveGoal: String = m["noActiveGoal"] ?: ""
        val setGoalToTrack: String = m["setGoalToTrack"] ?: ""
        val setGoal: String = m["setGoal"] ?: ""
        val pastGoals: String = m["pastGoals"] ?: ""
        val calculate: String = m["calculate"] ?: ""
        val weightEvolution: String = m["weightEvolution"] ?: ""
        val measurements: String = m["measurements"] ?: ""
        val startedOn: String = m["startedOn"] ?: ""
        val editGoal: String = m["editGoal"] ?: ""
        val equipDumbbells: String = m["equipDumbbells"] ?: ""
        val equipBarbell: String = m["equipBarbell"] ?: ""
        val equipMachine: String = m["equipMachine"] ?: ""
        val equipCable: String = m["equipCable"] ?: ""
        val equipBodyweight: String = m["equipBodyweight"] ?: ""
        val equipEZBar: String = m["equipEZBar"] ?: ""
        val equipSmithMachine: String = m["equipSmithMachine"] ?: ""
        val equipKettlebell: String = m["equipKettlebell"] ?: ""
        val equipStabilityBall: String = m["equipStabilityBall"] ?: ""
        val equipSledMachine: String = m["equipSledMachine"] ?: ""
        val equipBand: String = m["equipBand"] ?: ""
        val energizeLabel: String = m["energizeLabel"] ?: ""
        val performLabel: String = m["performLabel"] ?: ""
        val pushItLabel: String = m["pushItLabel"] ?: ""
        val openSpotifyLabel: String = m["openSpotifyLabel"] ?: ""
        val tapToPlayLabel: String = m["tapToPlayLabel"] ?: ""
        val startingWorkoutLabel: String = m["startingWorkoutLabel"] ?: ""
        val signUpSuccessMessage: String = m["signUpSuccessMessage"] ?: ""
        val trainingSectionLabel: String = m["trainingSectionLabel"] ?: "Training"
        val frequencyLabel: String = m["frequencyLabel"] ?: "Frequency"
        val xPerWeek: String = m["xPerWeek"] ?: "x / week"
        val editProfile: String = m["editProfile"] ?: "Edit Profile"
        val memberSince: String = m["memberSince"] ?: "Member since"
        val changePassword: String = m["changePassword"] ?: "Change Password"
        val updateTitle: String = m["updateTitle"] ?: "New version available"
        val updateMessage: String = m["updateMessage"] ?: "Kinetic %s has been released. You have v%s installed.\n\nTap to download the new APK."
        val updateDownload: String = m["updateDownload"] ?: "Download"
        val updateLater: String = m["updateLater"] ?: "Later"
        val stop: String = m["stop"] ?: ""
        val openApp: String = m["openApp"] ?: ""
        val goal: String = m["goal"] ?: ""
        val stepGoalChannel: String = m["stepGoalChannel"] ?: ""
        val stepGoalTitle: String = m["stepGoalTitle"] ?: ""
        val stepGoalText: String = m["stepGoalText"] ?: ""
        val stepGoalBig: String = m["stepGoalBig"] ?: ""
        val stepGoalKeepGoing: String = m["stepGoalKeepGoing"] ?: ""
        val gpsChannelName: String = m["gpsChannelName"] ?: ""
        val waterChannelName: String = m["waterChannelName"] ?: ""
        val biometricChannelName: String = m["biometricChannelName"] ?: ""
        val friendChannelName: String = m["friendChannelName"] ?: ""
        val oneRmCalculator: String = m["oneRmCalculator"] ?: ""
        val plusGoal: String = m["plusGoal"] ?: ""
        val tierFree: String = m["tierFree"] ?: ""
        val tierPro: String = m["tierPro"] ?: ""
        val tierProPlus: String = m["tierProPlus"] ?: ""
        val tierLifetime: String = m["tierLifetime"] ?: ""
        val goalComplete: String = m["goalComplete"] ?: ""
        val waterStreak: String = m["waterStreak"] ?: ""
        val ofGoal: String = m["ofGoal"] ?: ""
        val editWaterGoal: String = m["editWaterGoal"] ?: ""
        val newWaterGoal: String = m["newWaterGoal"] ?: ""
        val undo: String = m["undo"] ?: ""
        val workoutReminderTitle: String = m["workoutReminderTitle"] ?: ""
        val workoutReminderBody: String = m["workoutReminderBody"] ?: ""
        val workoutReminderText: String = m["workoutReminderText"] ?: ""
        val workoutChannelName: String = m["workoutChannelName"] ?: ""
        val weeklySummaryTitle: String = m["weeklySummaryTitle"] ?: ""
        val weeklySummaryText: String = m["weeklySummaryText"] ?: ""
        val weeklySummaryChannelName: String = m["weeklySummaryChannelName"] ?: ""
        val goalProgressTitle: String = m["goalProgressTitle"] ?: ""
        val goalProgressText: String = m["goalProgressText"] ?: ""
        val goalProgressChannelName: String = m["goalProgressChannelName"] ?: ""
        val achievementTitle: String = m["achievementTitle"] ?: ""
        val achievementText: String = m["achievementText"] ?: ""
        val achievementChannelName: String = m["achievementChannelName"] ?: ""
    }
    private val strings by lazy {
        mapOf(
            "ro" to createRo(),
            "en" to createEn(),
            "ru" to createRu(),
            "uk" to createUk(),
            "fr" to createFr(),
            "de" to createDe(),
            "es" to createEs(),
            "it" to createIt(),
            "tr" to createTr(),
            "pt" to createPt(),
            "pl" to createPl(),
        )
    }

    fun setLanguage(code: String) {
        currentLanguage = code
    }
    fun getLanguage(): String {
        return currentLanguage
    }

    private fun createRo() = Strings(enRaw() + mapOf(
         "stop" to "Oprire", "openApp" to "Deschide aplicația", "goal" to "Obiectiv", "stepGoalChannel" to "Obiectiv pași", "stepGoalTitle" to "🏆 Obiectiv de pași atins!", "stepGoalText" to "Felicitări! Ai atins %d pași!", "stepGoalBig" to "Felicitări! Ai atins obiectivul de %d pași!", "stepGoalKeepGoing" to "Continuă tot așa!", "gpsChannelName" to "Urmărire GPS", "waterChannelName" to "Memento apă", "biometricChannelName" to "Memento măsurători", "friendChannelName" to "Cereri de prietenie", "oneRmCalculator" to "Calculator 1RM", "plusGoal" to "+ Obiectiv", "tierFree" to "GRATUIT", "tierPro" to "PRO", "tierProPlus" to "PRO+", "tierLifetime" to "PERMANENT",
         "dashboard" to "Panou",  "acasa" to "Acasă", "workouts" to "Antrenamente", "stats" to "Statistici", "waterIntake" to "Apa", "waterGoal" to "Obiectiv apă", "addWater" to "Adaugă apă",  "height" to "Înălțime", "personalInfo" to "Informații personale",  "ml" to "ml", "templates" to "Șabloane", "recovery" to "Recuperare",
              "everyDay" to "În fiecare zi", "reminder" to "Memento",
            "weeklyHistory" to "Istoric săptămânal", "tips" to "Sfaturi", "customMl" to "ml personalizat", "average" to "Medie", "target" to "Țintă",
            "waterTip1" to "Bea 250ml la fiecare 30 min în timpul antrenamentului.", "waterTip2" to "Dimineața: 500ml la trezire pentru metabolism activ.", 
          "friends" to "Prieteni", "leaderboard" to "Clasament", "all" to "Toți",
         "language" to "Limbă", "units" to "Unități", "logout" to "Deconectare",
        "login" to "Autentificare", "signUp" to "Înregistrare", "email" to "Email", "password" to "Parolă",
        "forgotPassword" to "Ați uitat parola?", 
        "loginAsGuest" to "Conectare ca oaspete", 
         "goalStrength" to "Forță", "goalMass" to "Masă musculară",
        "goalWeightLoss" to "Slăbit", "goalMaintenance" to "Menținere",         "selectGoal" to "Selectați obiectivul",
        "stepOf" to "Pasul %d din 7", "whatsYourAge" to "Ce varsta ai?",
        "whatsYourGender" to "Ce gen ai?", "male" to "Barbat", "female" to "Femeie",
        "whatsYourActivityLevel" to "Ce nivel de activitate ai?", "sedentary" to "Sedentar",
        "sedentaryDesc" to "Lucru de birou, putina miscare", "active" to "Activ",
        "activeDesc" to "Antrenament + activitate zilnica", "very_active" to "Foarte activ",
        "veryActive" to "Foarte activ", "veryActiveDesc" to "Antrenament intens + munca fizica",
        "remaining" to "ramase", 
        "whatsYourExperience" to "Ce nivel de experienta ai?",
        "beginnerLabel" to "Începător", "beginnerDesc" to "0-1 ani de antrenament",
        "intermediateLabel" to "Intermediar", "intermediateDesc" to "1-3 ani de antrenament consecvent",
        "advancedLabel" to "Avansat", "advancedDesc" to "3+ ani de antrenament serios",
        "whatEquipment" to "Ce echipament ai?",
        "homeNoEquip" to "Acasă - Fără echipament", "homeNoEquipDesc" to "Exerciții doar cu greutatea corpului",
        "homeDumbbells" to "Acasă - Gantere/Benzi", "homeDumbbellsDesc" to "Echipament de bază pentru acasă",
        "fullGym" to "Sală completă", "fullGymDesc" to "Acces complet la sală",
        "profileGoalLabel" to "Obiectiv", "profileExperienceLabel" to "Experiență", "profileEquipmentLabel" to "Echipament",
        "trainingFrequency" to "Frecvența de antrenament", "sessionsPerWeek" to "Ședințe pe săptămână",
        "selectTrainingDays" to "Selectează zilele de antrenament",
        "monday" to "Luni", "tuesday" to "Marți", "wednesday" to "Miercuri", "thursday" to "Joi",
        "friday" to "Vineri", "saturday" to "Sâmbătă", "sunday" to "Duminică",
        "physicalLimitations" to "Limitări fizice sau accidentări?",
        "physicalLimitationsPlaceholder" to "ex. dureri de genunchi, probleme de spate (sau lasă gol)",
        "whichMuscleGroups" to "Ce grupe musculare?", "selectAtLeastOne" to "Pasul 7 din 7 - selecteaza cel putin una",
        "next" to "Următorul", "skip" to "Sari", "finish" to "Termină", "back" to "Spate",
        "profileSetup" to "Setare profil", "enterName" to "Introduceți numele", "pickPhoto" to "Alegeți o fotografie",
        "saveProfile" to "Salvați profilul", "saveExercise" to "Salvează exercițiul", "chest" to "Piept", "shoulders" to "Umeri", "arms" to "Brațe", "biceps" to "Biceps", "triceps" to "Triceps",
        "legs" to "Picioare", "thighs" to "Coapse", "glutes" to "Fese", "calves" to "Gâmbi", "core" to "Abdomen", "cardio" to "Cardio", "fullBody" to "Full Body", "pleaseSelectOption" to "Selectează cel puțin o opțiune", "sets" to "Serie", "reps" to "Repetări",
        "weight" to "Greutate",  
        "startWorkout" to "Începe", "nextExercise" to "Următorul exercițiu", "notes" to "Notițe", "cancel" to "Anulează", "confirm" to "Confirmă",
        "delete" to "Șterge", "edit" to "Editează", "search" to "Caută", "noDataYet" to "Nu există date încă",
         "sendRequest" to "Trimite cerere", "accept" to "Acceptă",
        "reject" to "Respinge", "removeFriend" to "Elimină prieten", "noFriends" to "Niciun prieten",
        "searchUsers" to "Caută utilizatori", 
        "searchByNameOrId" to "Caută după nume sau ID", "incomingRequests" to "Cereri primite",
         "yourFriends" to "Prietenii tăi",
         "friendRequestSent" to "Cererea a fost trimisă",
         "feedEmpty" to "Fluxul este gol", 
         "workoutCompleted" to "Antrenament completat!",
        "streakLabel" to "Șirul curent", "bestStreak" to "Cel mai bun șir", "badges" to "Distincții",
          "kg" to "kg", "lbs" to "lbs",
          "xp" to "XP",
          "max" to "Max", "ok" to "OK", "lv" to "NV",
         "exportCsv" to "Exportă CSV", "importCsv" to "Importă CSV",
        "subscription" to "Abonament", "premium" to "Premium", 
         "subscribe" to "Abonează-te", 
          "error" to "Eroare", "retry" to "Reîncearcă",
         "recoveryInfo" to "Informații recuperare",
        "recommendedRecovery" to "Recuperare recomandată", 
         "progressChart" to "Grafic progres",
         "weightProgression" to "Progresie greutate",
        "calendarView" to "Vezi calendar",  
        "allGroups" to "Toate", 
        "friendRequestNotificationTitle" to "Cerere de prietenie",
        "friendRequestNotificationText" to "v-a trimis o cerere de prietenie!",
        "selectLanguage" to "Selectați limba",  
        "workoutsLabel" to "Antrenamente", 
        "currentStreakLabel" to "Șir curent", "bestStreakLabel" to "Cel mai bun șir",
         "days" to "zile",  
          "add" to "Adaugă", "demoExercise" to "DEMO EXERCIȚIU",  "prAndVolume" to "PR-uri și volum", "start" to "Start",  "noSavedSetsYet" to "Nu există seturi salvate încă.", "editSet" to "Editează set",  "exercises" to "exerciții", "recovered" to "Recuperat", "almostRecovered" to "Aproape recuperat", "moderate" to "Moderat", "tired" to "Obosit", "exhausted" to "Epuizat", "fatigue" to "oboseală", "chooseMuscleGroup" to "Alege grupa musculară",  "noExercisesFound" to "Nu s-au găsit exerciții", "tryDifferentFilter" to "Încearcă un alt filtru sau caută după nume", "voiceSearch" to "Căutare vocală",  "voiceSearchError" to "Nu s-a putut recunoaște vocea",
        "monthlyProgress" to "Progres lunar", "completeWorkoutsToSee" to "Completează antrenamente pentru a vedea progresul", "jan" to "Ian", "feb" to "Feb", "mar" to "Mar", "apr" to "Apr", "may" to "Mai", "jun" to "Iun", "jul" to "Iul", "aug" to "Aug", "sep" to "Sep", "oct" to "Oct", "nov" to "Noi", "dec" to "Dec", "monthlyDetails" to "Detalii lunare", "month" to "Lună", "mon" to "Lu", "tue" to "Ma", "wed" to "Mi", "thu" to "Jo", "fri" to "Vi", "sat" to "Sâ", "sun" to "Du", "noWorkouts" to "Niciun antrenament în această zi", 
         "subscribeNow" to "Abonează-te acum", "premiumFeature" to "Funcție Premium", "subscribersOnly" to "\$feature este disponibil doar pentru abonați", "choosePlan" to "Alege un plan", "youAreSubscribed" to "Ești abonat!", "muscleRecovery" to "Recuperare musculară",  "waterReminderTitle" to "Timpul să bei apă!", "waterReminderText" to "Hidratează-te! Este timpul să bei un pahar cu apă.",   "selectTime" to "Selectează ora", "forearms" to "Antebrate", "neckAndTraps" to "Gât & Trapezi", "welcome" to "Bun venit", "athlete" to "Sportiv",
        "unlockPremiumTitle" to "Deblochează Kinetic Premium", "unlockPremiumSubtitle" to "Accesează toate funcțiile avansate", "freePlan" to "Gratuit",   "permanentPlan" to "Plan permanent", "lifetimeAccess" to "Acces permanent", "restorePurchase" to "Restaurează achiziția", "watchAdToUnlock" to "Vizionează o reclamă (deblocare 30 min)", "unlockedForMinutes" to "Deblocat: %s rămase",  "currentPlan" to "Plan curent", "bestValue" to "Cea mai bună valoare", "mostPopular" to "Cel mai popular", "perMonth" to "/lună", "perYear" to "/an", "oneTimePayment" to "plată unică", "upgradeToUnlock" to "Fă upgrade pentru deblocare", "buyNow" to "Cumpără", "free" to "Gratuit", "purchaseSuccess" to "Achiziție reușită! Bun venit în Premium.", "purchaseFailed" to "Achiziția a eșuat", "purchaseCancelled" to "Achiziție anulată", "restoreSuccess" to "Achiziții restaurate", "noPurchasesToRestore" to "Nicio achiziție de restaurat", "adUnlockSuccess" to "Funcție deblocată 30 de minute!", "adNotReady" to "Reclama nu este gata. Încearcă din nou.", "dailyAdLimitReached" to "Ai atins limita zilnică de deblocări", "cancelAnytime" to "Anulează oricând din Google Play", "workoutAnalytics" to "Analiză antrenamente",
        "biometricTracking" to "Monitorizare biometrică",  "addMeasurement" to "Adaugă măsurătoare", "bodyFat" to "Grăsime corporală", "waistCirc" to "Talie", "hipsCirc" to "Solduri", "thighsCirc" to "Coapse", "chestCirc" to "Piept", "armsCirc" to "Brațe",  "noMeasurements" to "Nicio măsurătoare încă",    "weeksAgo" to "săptămâni în urmă", "cm" to "cm", "percent" to "%", "deleteMeasurement" to "Șterge măsurătoarea", "biometricHistory" to "Istoric măsurători", "weightChart" to "Grafic greutate", "bodyFatChart" to "Grafic grăsime corporală", "circumferenceChart" to "Grafic circumferințe",   "biometricReminderTitle" to "Timpul pentru măsurători!", "biometricReminderText" to "Nu uita să îți înregistrezi măsurătorile corporale săptămânale.",  "streakChannelName" to "Memento serie", "streakReminderTitle" to "Nu uita să te antrenezi!", "streakReminderText" to "Antrenează-te astăzi pentru a-ți menține seria!",  "welcomeSoundLabel" to "Sunet de întâmpinare",
        "foodJournal" to "Jurnal alimentar",    "scan" to "Scanează", "scanning" to "Se scanează...", "scanBarcodeHelp" to "Asigură-te că Google Play Services este instalat și updatat", "noFoodEntries" to "Nicio intrare alimentară încă", "todaysMacros" to "Macronutrienții de azi", "stepsLabel" to "Pași", "activeTimeLabel" to "Timp activ", "caloriesLabel" to "Calorii", "proteinLabel" to "Proteine", "carbsLabel" to "Carbo", "fatLabel" to "Grăsimi", "breakfast" to "Mic dejun", "lunch" to "Prânz", "dinner" to "Cină", "snack" to "Gustare", "drinks" to "Băuturi", "selectMealType" to "Selectează tipul mesei",  "foodName" to "Nume aliment", "brandLabel" to "Marcă",     "fiber" to "Fibre", "searchFood" to "Caută un aliment", "foodSearchHint" to "Ex: ou, pui, orez", "quantity" to "Cantitate", "gramsShort" to "g", "piecesShort" to "buc", "addToJournal" to "Adaugă în jurnal", "manualEntryMode" to "Introdu manual", "noFoodFound" to "Alimentul nu e în listă", "enterManually" to "Introdu alimentul manual", "per100g" to "per 100g", "perPiece" to "per bucată",
        "aiTrainer" to "Antrenor AI", "aiTrainerWelcome" to "Salut! Sunt antrenorul tău AI", "aiTrainerHint" to "Întreabă-mă orice despre antrenamente, nutriție sau progres", "aiTrainerHistory" to "Istoric conversații", "noHistoryYet" to "Nu există istoric", "current" to "Actual", "askAiTrainer" to "Întreabă antrenorul...", "aiSuggestion1" to "Ce antrenament îmi recomanzi azi?", "aiSuggestion2" to "Cum pot să sporesc volumul?", "aiSuggestion3" to "Am nevoie de o zi de odihnă?", "aiSuggestion4" to "Cum să ies din platou?",
         "deleteAccount" to "Șterge contul",
        "exerciseHistory" to "Istoric exercițiu",  
        "favorite" to "Favorit",  "savedExercises" to "Exerciții Salvate", "noFavorites" to "Nu ai exerciții salvate încă", "tapStarToSave" to "Apasă steaua pentru a salva un exercițiu", "removeFavorite" to "Elimină de la favorite", 
        "addSet" to "Adaugă serie", "exerciseNotes" to "Notițe exercițiu", 
        "saveNotes" to "Salvează notițele", 
        "volume" to "Volum", "maxWeight" to "Greutate max", "maxReps" to "Rep max", "maxSet" to "Set max",
        "today" to "Astăzi", "thisWeek" to "Această săptămână", "thisMonth" to "Această lună",
        "totalVolumeLabel" to "Volum total",
        "guest" to "Oaspete", "loginWithGoogle" to "Conectare cu Google", "loginWithFacebook" to "Conectare cu Facebook",
        "close" to "Închide",  "profile" to "Profil",
        "appTagline" to "Antrenează-te. Progresează. Repetă.", "or" to "sau", "dark" to "Întunecat", "light" to "Luminos",
        "system" to "Sistem",  
        "selectTheme" to "Selectează tema", "settingsAndMore" to "Setări și mai multe",
        "muscleGroups" to "Grupe musculare",  "features" to "Funcții", "activity" to "Activitate", "tools" to "Instrumente", 
        "englishUS" to "Engleză", "romana" to "Română", "russkiy" to "Rusă", "ukrainska" to "Ucraineană",
        "francais" to "Franceză", "deutsch" to "Germană", "espanol" to "Spaniolă",
        "italiano" to "Italiană", "turkce" to "Turcă", "portugues" to "Portugheză", "polski" to "Poloneză",
        "motto1" to "Fiecare repetiție contează.", "motto2" to "Mai puternic decât ieri.",
        "motto3" to "Corpul tău, regulile tale.", "motto4" to "Împinge-ți limitele.",
        "motto5" to "Constanta învinge talentul.", "motto6" to "Disciplina este libertate.",
        "motto7" to "Fără scurtături.", "motto8" to "Câștigat, nu primit.",
        "motto9" to "Nu te opri când ești obosit. Oprește-te când ai terminat.", "motto10" to "Durerea de azi devine forța de mâine.",
        "motto11" to "Lucrurile mari nu vin niciodată din zona de confort.", "motto12" to "Nu devine niciodată mai ușor. Tu devii mai puternic.",
        "motto13" to "Nu trebuie să fii extrem, doar consecvent.", "motto14" to "Singura ta limită ești tu.",
        "motto15" to "Orice expert a fost cândva începător.", "motto16" to "Corpul atinge ceea ce mintea crede.",
        "motto17" to "Odihna face parte din proces, nu este dușmanul.", "motto18" to "Pașii mici din fiecare zi duc la rezultate mari.",
        "motto19" to "Antrenează-te ca și cum ți-e foame. Rămâi umil.", "motto20" to "Succesul este suma eforturilor mici repetate zilnic.",
        "goodMorning" to "Bună dimineața", "goodAfternoon" to "Bună ziua", "goodEvening" to "Bună seara",
        "daysConsecutive" to "zile consecutive", "todaysWorkout" to "Antrenamentul de azi",
        "todayYouRest" to "Azi te odihnești", "restDayMessage" to "Odihna este esențială pentru recuperarea musculară. Profită pentru a te reface și a te pregăti pentru următorul antrenament.",
        "restDayTip" to "Poți face întinderi ușoare sau o plimbare pentru a menține circulația sanguină.",
        "dayLabel" to "Ziua", "ofCycle" to "din ciclu",
        "howDoYouFeel" to "Cum te simți azi?", "tiredLabel" to "Obosit", "normalLabel" to "Normal", "energeticLabel" to "Energic",
         "technicalTip" to "Sfat tehnic",
        "weeklySummary" to "Rezumat săptămânal", "lastWeekLabel" to "săpt. trecută",
        "goalLabel" to "Sfat obiectiv", "volumeLabel" to "Volum", "topExerciseLabel" to "Top exercițiu",
        "nutritionLabel" to "Nutriție", "motivationLabel" to "Motivație",
        "gpsCardioMap" to "Cardio", "startTracking" to "Pornește urmărirea", 
        "pauseTracking" to "Pauză", "resumeTracking" to "Continuă",
        "distance" to "Distanță", "pace" to "Ritm", "speed" to "Viteză", "duration" to "Durată",
        "savedRoutes" to "Rute salvate", "noSavedRoutes" to "Nicio rută salvată încă",
        "routeName" to "Nume rută", "saveRoute" to "Salvează ruta", "deleteRoute" to "Șterge ruta",
        "locationPermissionRequired" to "Permisiunea de locație este necesară",
        "restDaysTitle" to "Zile de odihnă & Deload", "restDaysSubtitle" to "Recuperare programată, întinderi, yoga ușoară",
        "deloadWeek" to "Săptămâna de deload", "recoverySchedule" to "Program recuperare",
        "stretching" to "Întinderi", "lightYoga" to "Yoga ușoară", "foamRolling" to "Foam rolling",
         "nextRestDay" to "Următoarea zi de odihnă",
        "muscleNeedsRest" to "Mușchiul are nevoie de odihnă", 
        "deloadInfo" to "Informații deload", "suggestedActivities" to "Activități sugerate",
        "activeRecovery" to "Recuperare activă", "lightWalk" to "Plimbare ușoară",
        "save" to "Salvează",
        "allGood" to "Totul e bine", "alreadyHaveAccount" to "Ai deja cont?", "autoDeloadEnabled" to "Deload automat activat",
        "avgRecovery" to "Recuperare medie", "caloriesBurned" to "Calorii arse", "confirmPassword" to "Confirmă parola",
        "createAccountTitle" to "Creează cont", "deloadActive" to "Deload activ", "deloadHistory" to "Istoric deload",
        "deloadInterval" to "Interval deload", "deloadNewValue" to "Valoare nouă", "deloadNormalValue" to "Valoare normală",
        "deloadPreview" to "Previzualizare deload", "deloadPreviewSubtitle" to "Vezi planul redus pentru următorul deload", "deloadActiveThisWeek" to "Deload activ săptămâna aceasta",         "recommendedForYou" to "Recomandat pentru tine", "tapToSchedule" to "Apasă pentru a programa", "dontHaveAccount" to "Nu ai cont?", "emailError" to "Email invalid",
        "endDeload" to "Încheie deload", "foamRollingDescription" to "Eliberează tensiunea musculară cu role de spumă",
        "heightCm" to "Înălțime (cm)", "lissDescription" to "Cardio ușor pentru recuperare activă",
        "loginInstead" to "Conectează-te în schimb", "musclesTiredCount" to "mușchi obosiți",
        "nameError" to "Numele este obligatoriu", "nameField" to "Nume", "optional" to "Opțional",
        "passwordError" to "Parola trebuie să aibă minimum 6 caractere", "passwordMismatch" to "Parolele nu se potrivesc",
        "passwordStrengthMedium" to "Medie", "passwordStrengthStrong" to "Puternică", "passwordStrengthWeak" to "Slabă",
        "privacyPolicyLink" to "Politică de confidențialitate", "recoveryTargeted" to "Recuperare țintită",
         "startDeload" to "Începe deload", "stretchingDescription" to "Îmbunătățește flexibilitatea și mobilitatea", "warmupStretch" to "Întindere de încălzire", "cooldownStretch" to "Întindere de răgaz",
        "termsAndConditions" to "Termeni și Condiții", "termsPrefix" to "Prin continuare, ești de acord cu",
        "timeForDeload" to "E timpul pentru deload", "weeks" to "săptămâni",
        "deloadReasonAuto" to "E timpul pentru deload",
        "deloadReasonGeneral" to "Deload general",
        "deloadDueBanner" to "Redu volumul în această săptămână pentru recuperare completă",
        "dailyIntake" to "Aport zilnic",
        "bio" to "Bio",
        "currentPassword" to "Parola curentă",
        "newPassword" to "Parolă nouă",
        "confirmNewPassword" to "Confirmă parola nouă",
        "changeLabel" to "Schimbă",
        "currentPasswordRequired" to "Parola curentă este obligatorie",
        "passwordTooShort" to "Parola nouă trebuie să aibă cel puțin 6 caractere",
        "enterPasswordToConfirm" to "Introdu parola pentru a confirma",
        "passwordRequiredToDelete" to "Parola este obligatorie pentru ștergerea contului",
        "lowLabel" to "Scăzut",
        "highLabel" to "Ridicat",
        "newChat" to "Conversație nouă",
        "serverSettings" to "Setări server",
        "deloadWhyTitle" to "De ce deload?",
        "deloadWhyBody" to "Antrenamentul intens timp de %d săptămâni acumulează oboseală. O săptămână de deload reduce volumul, ca mușchii să se recupereze complet și să revii mai puternic.",
        "deloadDuration" to "Durată",
        "deload1Week" to "1 săptămână",
        "deload2Weeks" to "2 săptămâni",
        "deloadReduction" to "Reducere",
        "deloadCompound" to "CMP",
        "deloadNoHistory" to "Niciun deload încă. După ce completezi o săptămână de deload, aceasta va apărea aici cu progresul de recuperare.",
        "editRestDay" to "Editează ziua de odihnă",
        "deloadDayOf" to "Ziua %d din %d",
        "gpsSearching" to "Caut semnal GPS...",
        "gpsError" to "Eroare GPS",
        "running" to "Alergare",
        "cycling" to "Ciclism",
        "walking" to "Mers pe jos",
        "routePoints" to "Puncte",
        "paused" to "Pauză", "estimatedOneRm" to "1RM estimat", "nextSetSuggestion" to "Setul următor", "setTypeWarmup" to "Încălzire", "setTypeWorking" to "Lucru", "setTypeDrop" to "Drop", "setTypeAmrap" to "AMRAP", "setTypePaused" to "Pauză", "setTypeTempo" to "Tempo", "rpeLabel" to "RPE", "readinessTitle" to "Pregătire", "messages" to "Mesaje", "readinessScore" to "Pregătirea de azi", "readinessHeavy" to "GREU — dă tot", "readinessModerate" to "MODERAT — antrenează normal", "readinessLight" to "UȘOR — recuperare sau efort redus", "readinessSleep" to "Somn", "readinessSleepHours" to "Ore dormite aseară", "readinessQuality" to "Calitate", "readinessSteps" to "Pași azi", "readinessRecovery" to "Recuperare musculară", "readinessVolume" to "Volum azi", "readinessHint" to "Setează somnul ca să vezi recomandarea zilnică de antrenament.",
        "readinessHydration" to "Hidratare", "readinessTrend" to "Trend 7 zile",
        "readinessActionSleep" to "Dormi mai mult tonight pentru o recuperare mai buna",
        "readinessActionSteps" to "O plimbare de 20 min iti creste pregatirea",
        "readinessActionRecovery" to "Stretching azi - muschii au nevoie de odihna",
        "readinessActionVolume" to "Volum ridicat azi - redu intensitatea maine",
        "readinessActionHydration" to "Bea mai multa apa - deshidratarea scade performanta",
        "readinessIntensityHeavy" to "Antreneaza tare - intensitate maxima",
        "readinessIntensityModerate" to "Antreneaza normal - ramane la plan",
        "readinessIntensityLight" to "Ia-o usor - redu volumul cu 30-40%",
        "steps" to "pași",
        "timerFinished" to "Timpul a expirat!",
        "timeToStartNextSet" to "Gata pentru următoarea serie!",
        "weeksSinceLastDeload" to "Săptămâni de la ultimul deload", "weightKg" to "Greutate (kg)",
        "yogaDescription" to "Relaxare și mobilitate prin yoga ușoară",
        "mostTrained" to "Cel mai exersat",
        "allExercises" to "Toate exercițiile",
        "vsPrevious" to "vs anterioară",
        "sessions" to "ședințe",
        "gpsDisabledTitle" to "GPS dezactivat",
        "gpsDisabledMessage" to "Activează GPS-ul din setările telefonului pentru a putea urmări ruta în timp real.",
        "openSettings" to "Deschide setările",
         "weightKgLabel" to "Greutate (kg)", "repsLabel" to "Reps",
        "estimated1rm" to "1RM estimat", "intensityZones" to "Zone de intensitate",
        "zoneStrength" to "Forță (1-2 reps)", "zoneStrengthHypertrophy" to "Forță-Hipertrofie (3-5 reps)",
        "zoneHypertrophy" to "Hipertrofie (6-8 reps)", "zoneHypertrophyEndurance" to "Hipertrofie-Anduranță (10-12 reps)",
        "zoneEndurance" to "Anduranță (15+ reps)", 
        "totalVolumeLabel" to "Volum total", "muscleGroups" to "Grupe musculare", "weeklyTab" to "Săptămânal",
         "pbsTab" to "PB-uri",
        "keepTraining" to "Continuă antrenamentul pentru a-ți depăși recordurile!",
        "plateCalculatorTitle" to "Calculator discuri", "targetWeightLabel" to "Greutate țintă",
        "menu" to "Meniu",
        "foodNamePlaceholder" to "Ex: Piept de pui", "brandPlaceholder" to "Ex: Farm Foods", "backendServerAddress" to "Adresa serverului backend:", "aiApiKeyOptional" to "Cheie API AI Trainer (opțional):", "leaveEmptyForDefaultServer" to "Lasă gol pentru URL-ul implicit al serverului. Cheia API e necesară doar dacă serverul are autentificare activată.", "kcal" to "KCAL", "deleteAccountWarning" to "Această acțiune este permanentă și nu poate fi anulată. Toate datele tale vor fi șterse.", "setStepGoal" to "Setează obiectivul de pași", "enterDailyStepGoal" to "Introdu obiectivul tău zilnic de pași",
        "history" to "Istoric", "deleteChat" to "Șterge chatul", "bodyAnatomy" to "Anatomia corpului", "leaveEmptyIfAuthDisabled" to "Lasă gol dacă autentificarea este dezactivată", "floatingWindow" to "Fereastră plutitoare", "float" to "Plutitoare", "clear" to "Șterge", "decrease" to "Scade", "increase" to "Crește", "exercise" to "Exercițiu", "top" to "TOP", "play" to "Redare", "reset" to "Resetează", "selected" to "Selectat", "loadingPlaylists" to "Se încarcă playlisturile...", "failedToLoadPlaylists" to "Nu s-au putut încărca playlisturile", "noPlaylistsFound" to "Nu s-au găsit playlisturi", "createPlaylistFirst" to "Creează mai întâi un playlist pe Spotify", "authenticationFailed" to "Autentificare eșuată", "unknownError" to "Eroare necunoscută", "selectPlaylist" to "Selectează playlistul", "choosePlaylist" to "Alege un playlist pentru antrenamentul tău", "tracks" to "piese", "connectToSpotify" to "Conectează-te la Spotify", "spotifyAccessDescription" to "Accesează playlisturile tale și setează coloana sonoră perfectă pentru antrenament", "loginWithSpotify" to "Autentificare cu Spotify", "spotifyRedirectInfo" to "Vei fi redirecționat către Spotify pentru autorizare", "navy" to "Navy", "bmi" to "BMI", "exportBackup" to "Exportă backup",
        "barWeightLabel" to "Greutate bară", "lbsKg" to "lbs/kg",
        "platesPerSide" to "Discuri pe fiecare parte", "plateUnit" to "disc",
        "eachSide" to "pe fiecare parte", "total" to "Total",
        "weightTooLight" to "Greutatea este prea mică pentru discuri (doar bară:",
        "plateCalcNote" to "Calculatorul de discuri calculează automat ce discuri trebuie adăugate pe bară pentru a obține greutatea totală dorită.",
        "howToGet" to "Cum să obții:",
        "gender" to "Gen", "age" to "Varsta", "method" to "Metoda", "waistCm" to "Talie (cm)", "neckCm" to "Gat (cm)", "hipsCm" to "Solduri (cm)", "estimatedBodyFat" to "Grasime corporala estimata", "navyMethodInfo" to "Metoda Marina: foloseste un fleximetru pentru a masura circumferintele",  "bodyFatCalculator" to "Calculator grasime corporala", "newPRs" to "Noi Recorduri Personale", "exerciseBreakdown" to "Detalii Exercitii", "done" to "Gata", "weightGoal" to "Obiectiv Greutate", "currentWeight" to "Greutate Curenta", "target" to "Obiectiv", "deadline" to "Termen limita", "goalDetails" to "Detalii Obiectiv", "startWeight" to "Greutate Start", "targetWeight" to "Greutate Obiectiv",  "noActiveGoal" to "Niciun obiectiv activ", "setGoalToTrack" to "Seteaza un obiectiv pentru a urmari progresul",         "setGoal" to "Seteaza Obiectiv", "pastGoals" to "Obiective Anterioare", "calculate" to "Calculeaza",
        "weightEvolution" to "Evolutie Greutate", "measurements" to "masuratori", "startedOn" to "Inceput pe", "editGoal" to "Editeaza obiectiv",
        "equipDumbbells" to "Gantere", "equipBarbell" to "Bară", "equipMachine" to "Mașină", "equipCable" to "Cablu", "equipBodyweight" to "Greutate corp", "equipEZBar" to "Bară EZ", "equipSmithMachine" to "Mașină Smith", "equipKettlebell" to "Kettlebell", "equipStabilityBall" to "Bălans stabilitate", "equipSledMachine" to "Mașină sanie", "equipBand" to "Bandă",
        "energizeLabel" to "Energizează-te",
        "performLabel" to "Performanță",
        "pushItLabel" to "Impinge",
        "openSpotifyLabel" to "Deschide Spotify",
        "tapToPlayLabel" to "Apasă pentru a asculta pe Spotify",
        "startingWorkoutLabel" to "Pornim muzica de antrenament...",
        "signUpSuccessMessage" to "Cont creat cu succes! Te rugam sa te loghezi.",
        "trainingSectionLabel" to "Antrenament", "frequencyLabel" to "Frecvență", "xPerWeek" to "x / săpt", "editProfile" to "Editează profilul", "memberSince" to "Membru din", "changePassword" to "Schimbă parola",
        "updateTitle" to "Versiune nouă disponibilă", "updateMessage" to "Kinetic %s a fost lansată. Ai instalată versiunea v%s.\n\nApasă pentru a descărca noul APK.",         "updateDownload" to "Descarcă", "updateLater" to "Mai târziu",
        "goalComplete" to "Obiectiv atins!", "waterStreak" to "Șir de hidratare", "ofGoal" to "din obiectiv", "editWaterGoal" to "Editează obiectivul de apă", "newWaterGoal" to "Nou obiectiv (ml)",
        "undo" to "Anulează",
        "workoutReminderTitle" to "Antrenamentul de azi",
        "workoutReminderBody" to "E timpul să construiești forță. Concentrează-te pe __GROUPS__ astăzi. Depune efort maxim la fiecare serie și depășește-ți recordurile personale.",
        "workoutReminderText" to "Astăzi e ziua de antrenament! Pregătește-te!",
        "workoutChannelName" to "Remindere antrenament",
        "weeklySummaryTitle" to "Rezumatul săptămânii",
        "weeklySummaryText" to "Ai antrenat de __COUNT__ ori săptămâna aceasta! Continuă!",
        "weeklySummaryChannelName" to "Rezumat săptămânal",
        "streakReminderTitle" to "Nu întrerupe seria!",
        "streakReminderText" to "Antrenează-te azi ca să îți menții seria de __STREAK__ zile!",
        "streakChannelName" to "Remindere serie",
        "goalProgressTitle" to "Progresul pașilor",
        "goalProgressText" to "Ai atins __PERCENT__% din obiectivul de pași! (__CURRENT__/__GOAL__)",
        "goalProgressChannelName" to "Progres obiectiv",
        "achievementTitle" to "Realizare deblocată!",
        "achievementText" to "Felicitări! Ai deblocat un nou badge!",
        "achievementChannelName" to "Realizări"
    ))

    private fun createEn() = Strings(enRaw())

    private fun enRaw(): Map<String, String> = mapOf(
         "stop" to "Stop", "openApp" to "Open App", "goal" to "Goal", "stepGoalChannel" to "Step Goal", "stepGoalTitle" to "🏆 Step Goal Reached!", "stepGoalText" to "Congratulations! You reached %d steps!", "stepGoalBig" to "Congratulations! You reached your step goal of %d steps!", "stepGoalKeepGoing" to "Keep up the great work!", "gpsChannelName" to "GPS Tracking", "waterChannelName" to "Water Reminders", "biometricChannelName" to "Biometric Reminders", "friendChannelName" to "Friend Requests", "oneRmCalculator" to "1RM Calculator", "plusGoal" to "+ Goal", "tierFree" to "FREE", "tierPro" to "PRO", "tierProPlus" to "PRO+", "tierLifetime" to "LIFETIME",
         "dashboard" to "Dashboard",  "acasa" to "Home", "workouts" to "Workouts", "stats" to "Stats", "waterIntake" to "Water Intake", "waterGoal" to "Water Goal", "addWater" to "Add Water",  "height" to "Height", "personalInfo" to "Personal Info",  "ml" to "ml", "templates" to "Templates", "recovery" to "Recovery",
              "everyDay" to "Every day", "reminder" to "Reminder",
            "weeklyHistory" to "Weekly History", "tips" to "Tips", "customMl" to "Custom ml", "average" to "Average", "target" to "Target",
            "waterTip1" to "Drink 250ml every 30 min during workout.", "waterTip2" to "Morning: 500ml at wake up for active metabolism.", 
          "friends" to "Friends", "leaderboard" to "Leaderboard", "all" to "All",
         "language" to "Language", "units" to "Units", "logout" to "Logout",
        "login" to "Login", "signUp" to "Sign Up", "email" to "Email", "password" to "Password",
        "forgotPassword" to "Forgot password?", 
        "loginAsGuest" to "Login as guest", 
         "goalStrength" to "Strength", "goalMass" to "Muscle Mass",
        "goalWeightLoss" to "Weight Loss", "goalMaintenance" to "Maintenance",
        "selectGoal" to "Select your goal",
        "stepOf" to "Step %d of 7", "whatsYourAge" to "How old are you?",
        "whatsYourGender" to "What's your gender?", "male" to "Male", "female" to "Female",
        "whatsYourActivityLevel" to "What's your activity level?", "sedentary" to "Sedentary",
        "sedentaryDesc" to "Desk job, little movement", "active" to "Active",
        "activeDesc" to "Training + daily activity", "very_active" to "Very Active",
        "veryActive" to "Very Active", "veryActiveDesc" to "Intense training + physical work",
        "remaining" to "remaining", 
        "whatsYourExperience" to "What's your experience level?",
        "beginnerLabel" to "Beginner", "beginnerDesc" to "0-1 years of training",
        "intermediateLabel" to "Intermediate", "intermediateDesc" to "1-3 years of consistent training",
        "advancedLabel" to "Advanced", "advancedDesc" to "3+ years of serious training",
        "whatEquipment" to "What equipment do you have?",
        "homeNoEquip" to "Home - No Equipment", "homeNoEquipDesc" to "Bodyweight exercises only",
        "homeDumbbells" to "Home - Dumbbells/Bands", "homeDumbbellsDesc" to "Basic home equipment",
        "fullGym" to "Full Gym", "fullGymDesc" to "Complete gym access",
        "profileGoalLabel" to "Goal", "profileExperienceLabel" to "Experience", "profileEquipmentLabel" to "Equipment",
        "trainingFrequency" to "Training frequency", "sessionsPerWeek" to "Sessions per week",
        "selectTrainingDays" to "Select your training days",
        "monday" to "Monday", "tuesday" to "Tuesday", "wednesday" to "Wednesday", "thursday" to "Thursday",
        "friday" to "Friday", "saturday" to "Saturday", "sunday" to "Sunday",
        "physicalLimitations" to "Any physical limitations or injuries?",
        "physicalLimitationsPlaceholder" to "e.g. knee pain, back issues (or leave empty)",
        "whichMuscleGroups" to "Which muscle groups?", "selectAtLeastOne" to "Step 7 of 7 - select at least one",
        "next" to "Next", "skip" to "Skip", "finish" to "Finish",
        "back" to "Back", "profileSetup" to "Profile Setup", "enterName" to "Enter your name",
        "pickPhoto" to "Pick a photo", "saveProfile" to "Save Profile", "saveExercise" to "Save Exercise", "chest" to "Chest",
        "shoulders" to "Shoulders", "arms" to "Arms", "biceps" to "Biceps", "triceps" to "Triceps",
        "legs" to "Legs", "thighs" to "Thighs", "glutes" to "Glutes", "calves" to "Calves", "core" to "Core", "cardio" to "Cardio", "fullBody" to "Full Body",
        "pleaseSelectOption" to "Please select an option", "sets" to "Sets", "reps" to "Reps", "weight" to "Weight", 
         "startWorkout" to "Start Workout", "nextExercise" to "Next Exercise", "notes" to "Notes",
        "cancel" to "Cancel", "confirm" to "Confirm", "delete" to "Delete", "edit" to "Edit",
        "search" to "Search", "noDataYet" to "No data yet", 
        "sendRequest" to "Send Request", "accept" to "Accept", "reject" to "Reject",
        "removeFriend" to "Remove Friend", "noFriends" to "No friends yet", "searchUsers" to "Search Users",
         "searchByNameOrId" to "Search by name or ID",
        "incomingRequests" to "Incoming Requests", 
        "yourFriends" to "Your Friends", 
        "friendRequestSent" to "Request Sent",  "feedEmpty" to "Feed is empty",
           "workoutCompleted" to "Workout Complete!",
        "streakLabel" to "Current Streak", "bestStreak" to "Best Streak", "badges" to "Badges",
          "kg" to "kg", "lbs" to "lbs",
          "xp" to "XP",
          "max" to "Max", "ok" to "OK", "lv" to "LV",
         "exportCsv" to "Export CSV", "importCsv" to "Import CSV",
        "subscription" to "Subscription", "premium" to "Premium", 
         "subscribe" to "Subscribe", 
          "error" to "Error", "retry" to "Retry",
         "recoveryInfo" to "Recovery Info",
        "recommendedRecovery" to "Recommended Recovery",
        "progressChart" to "Progress Chart", 
        "weightProgression" to "Weight Progression", 
         "calendarView" to "See Calendar", 
         "allGroups" to "All", 
        "friendRequestNotificationTitle" to "Friend Request",
        "friendRequestNotificationText" to "sent you a friend request!",
        "selectLanguage" to "Select Language",  
        "workoutsLabel" to "Workouts", 
        "currentStreakLabel" to "Current Streak", "bestStreakLabel" to "Best Streak",
         "days" to "days",           "add" to "Add", "demoExercise" to "DEMO EXERCISE",  "prAndVolume" to "PRs and volume", "start" to "Start",  "noSavedSetsYet" to "No saved sets yet.", "editSet" to "Edit set",  "exercises" to "exercises", "recovered" to "Recovered", "almostRecovered" to "Almost recovered", "moderate" to "Moderate", "tired" to "Tired", "exhausted" to "Exhausted", "fatigue" to "fatigue", "chooseMuscleGroup" to "Choose muscle group",  "noExercisesFound" to "No exercises found", "tryDifferentFilter" to "Try a different filter or search by name", "voiceSearch" to "Voice search",  "voiceSearchError" to "Could not recognize voice", "estimatedOneRm" to "Est. 1RM", "nextSetSuggestion" to "Next set", "setTypeWarmup" to "Warm-up", "setTypeWorking" to "Working", "setTypeDrop" to "Drop", "setTypeAmrap" to "AMRAP", "setTypePaused" to "Paused", "setTypeTempo" to "Tempo", "rpeLabel" to "RPE",
           "readinessTitle" to "Readiness", "messages" to "Messages", "readinessScore" to "Today's readiness", "readinessHeavy" to "HEAVY — go hard", "readinessModerate" to "MODERATE — train normal", "readinessLight" to "LIGHT — recover or go easy", "readinessSleep" to "Sleep", "readinessSleepHours" to "Hours last night", "readinessQuality" to "Quality", "readinessSteps" to "Steps today", "readinessRecovery" to "Muscle recovery", "readinessVolume" to "Volume today", "readinessHint" to "Set your sleep to see your daily training recommendation.",
        "monthlyProgress" to "Monthly progress", "completeWorkoutsToSee" to "Complete workouts to see progress", "jan" to "Jan", "feb" to "Feb", "mar" to "Mar", "apr" to "Apr", "may" to "May", "jun" to "Jun", "jul" to "Jul", "aug" to "Aug", "sep" to "Sep", "oct" to "Oct", "nov" to "Nov", "dec" to "Dec", "monthlyDetails" to "Monthly details", "month" to "Month", "mon" to "Mon", "tue" to "Tue", "wed" to "Wed", "thu" to "Thu", "fri" to "Fri", "sat" to "Sat", "sun" to "Sun", "noWorkouts" to "No workouts on this day", 
         "subscribeNow" to "Subscribe Now", "premiumFeature" to "Premium Feature", "subscribersOnly" to "\$feature is available for subscribers only", "choosePlan" to "Choose a plan", "youAreSubscribed" to "You are subscribed!", "muscleRecovery" to "Muscle Recovery",  "waterReminderTitle" to "Time to drink water!", "waterReminderText" to "Stay hydrated! It's time to drink a glass of water.",   "selectTime" to "Select time", "forearms" to "Forearms", "neckAndTraps" to "Neck & Traps", "welcome" to "Welcome", "athlete" to "Athlete",
        "readinessHydration" to "Hydration", "readinessTrend" to "7-day trend",
        "readinessActionSleep" to "Sleep more tonight for better recovery",
        "readinessActionSteps" to "A 20-min walk will boost your readiness",
        "readinessActionRecovery" to "Stretching today - your muscles need rest",
        "readinessActionVolume" to "High volume today - consider lighter training tomorrow",
        "readinessActionHydration" to "Drink more water - dehydration reduces performance",
        "readinessIntensityHeavy" to "Train hard - full intensity, add volume if you feel good",
        "readinessIntensityModerate" to "Train normal - stick to the plan",
        "readinessIntensityLight" to "Go easy - reduce volume by 30-40%, focus on form",
        "unlockPremiumTitle" to "Unlock Kinetic Premium", "unlockPremiumSubtitle" to "Get access to all advanced features", "freePlan" to "Free",   "permanentPlan" to "Lifetime Plan", "lifetimeAccess" to "Lifetime access", "restorePurchase" to "Restore purchase", "watchAdToUnlock" to "Watch an ad (unlock 30 min)", "unlockedForMinutes" to "Unlocked: %s left",  "currentPlan" to "Current plan", "bestValue" to "Best value", "mostPopular" to "Most popular", "perMonth" to "/month", "perYear" to "/year", "oneTimePayment" to "one-time payment", "upgradeToUnlock" to "Upgrade to unlock", "buyNow" to "Buy", "free" to "Free", "purchaseSuccess" to "Purchase successful! Welcome to Premium.", "purchaseFailed" to "Purchase failed", "purchaseCancelled" to "Purchase cancelled", "restoreSuccess" to "Purchases restored", "noPurchasesToRestore" to "No purchases to restore", "adUnlockSuccess" to "Feature unlocked for 30 minutes!", "adNotReady" to "Ad not ready. Please try again.", "dailyAdLimitReached" to "You've reached the daily unlock limit", "cancelAnytime" to "Cancel anytime in Google Play", "workoutAnalytics" to "Workout Analytics",
        "biometricTracking" to "Biometric Tracking",  "addMeasurement" to "Add measurement", "bodyFat" to "Body fat", "waistCirc" to "Waist", "hipsCirc" to "Hips", "thighsCirc" to "Thighs", "chestCirc" to "Chest", "armsCirc" to "Arms",  "noMeasurements" to "No measurements yet",    "weeksAgo" to "weeks ago", "cm" to "cm", "percent" to "%", "deleteMeasurement" to "Delete measurement", "biometricHistory" to "Measurement history", "weightChart" to "Weight chart", "bodyFatChart" to "Body fat chart", "circumferenceChart" to "Circumference chart",   "biometricReminderTitle" to "Time for measurements!", "biometricReminderText" to "Don't forget to log your weekly body measurements.",  "streakChannelName" to "Streak Reminders", "streakReminderTitle" to "Don't break your streak!", "streakReminderText" to "Train today to keep your streak going!",  "welcomeSoundLabel" to "Welcome Sound",
        "foodJournal" to "Food Journal",    "scan" to "Scan", "scanning" to "Scanning...", "scanBarcodeHelp" to "Make sure Google Play Services is installed and updated", "noFoodEntries" to "No food entries yet", "todaysMacros" to "Today's Macros", "stepsLabel" to "Steps", "activeTimeLabel" to "Active time", "caloriesLabel" to "Calories", "proteinLabel" to "Protein", "carbsLabel" to "Carbs", "fatLabel" to "Fat", "breakfast" to "Breakfast", "lunch" to "Lunch", "dinner" to "Dinner", "snack" to "Snack", "drinks" to "Drinks", "selectMealType" to "Select meal type",  "foodName" to "Food name", "brandLabel" to "Brand",     "fiber" to "Fiber", "searchFood" to "Search a food", "foodSearchHint" to "Ex: egg, chicken, rice", "quantity" to "Quantity", "gramsShort" to "g", "piecesShort" to "pcs", "addToJournal" to "Add to journal", "manualEntryMode" to "Manual entry", "noFoodFound" to "Food not found in the list", "enterManually" to "Enter the food manually", "per100g" to "per 100g", "perPiece" to "per piece",
        "aiTrainer" to "AI Trainer", "aiTrainerWelcome" to "Hi! I'm your AI trainer", "aiTrainerHint" to "Ask me anything about workouts, nutrition or progress", "aiTrainerHistory" to "Chat history", "noHistoryYet" to "No history yet", "current" to "Current", "askAiTrainer" to "Ask the trainer...", "aiSuggestion1" to "What workout do you recommend today?", "aiSuggestion2" to "How can I increase volume?", "aiSuggestion3" to "Do I need a rest day?", "aiSuggestion4" to "How do I break through a plateau?",
         "deleteAccount" to "Delete Account",
        "exerciseHistory" to "Exercise History",  
        "favorite" to "Favorite",  "savedExercises" to "Saved Exercises", "noFavorites" to "No saved exercises yet", "tapStarToSave" to "Tap the star on any exercise to save it here", "removeFavorite" to "Remove from favorites", 
        "addSet" to "Add Set", "exerciseNotes" to "Exercise Notes", 
        "saveNotes" to "Save Notes", 
        "volume" to "Volume", "maxWeight" to "Max Weight", "maxReps" to "Max Reps", "maxSet" to "Max Set",
        "today" to "Today", "thisWeek" to "This Week", "thisMonth" to "This Month",
        "totalVolumeLabel" to "Total Volume",
        "guest" to "Guest", "loginWithGoogle" to "Sign in with Google", "loginWithFacebook" to "Sign in with Facebook",
        "close" to "Close",  "profile" to "Profile",
        "appTagline" to "Train. Progress. Repeat.", "or" to "or", "dark" to "Dark", "light" to "Light",
        "system" to "System",  
        "selectTheme" to "Select Theme", "settingsAndMore" to "Settings & More",
        "muscleGroups" to "Muscle Groups",  "features" to "Features", "activity" to "Activity", "tools" to "Tools", 
        "englishUS" to "English", "romana" to "Romanian", "russkiy" to "Russian", "ukrainska" to "Ukrainian",
        "francais" to "French", "deutsch" to "German", "espanol" to "Spanish",
        "italiano" to "Italian", "turkce" to "Turkish", "portugues" to "Portuguese", "polski" to "Polish",
        "motto1" to "Every rep counts.", "motto2" to "Stronger than yesterday.",
        "motto3" to "Your body, your rules.", "motto4" to "Push your limits.",
        "motto5" to "Consistency beats talent.", "motto6" to "Discipline is freedom.",
        "motto7" to "No shortcuts.", "motto8" to "Earned, not given.",
        "motto9" to "Don't stop when you're tired. Stop when you're done.", "motto10" to "The pain you feel today becomes the strength you feel tomorrow.",
        "motto11" to "Great things never come from comfort zones.", "motto12" to "It never gets easier. You just get stronger.",
        "motto13" to "You don't have to be extreme, just consistent.", "motto14" to "Your only limit is you.",
        "motto15" to "Every expert was once a beginner.", "motto16" to "The body achieves what the mind believes.",
        "motto17" to "Rest is part of the process, not the enemy.", "motto18" to "Small steps every day lead to big results.",
        "motto19" to "Train like you're hungry. Stay humble.", "motto20" to "Success is the sum of small efforts repeated daily.",
        "goodMorning" to "Good morning", "goodAfternoon" to "Good afternoon", "goodEvening" to "Good evening",
        "daysConsecutive" to "days consecutive", "todaysWorkout" to "Today's Workout",
        "todayYouRest" to "Today you rest", "restDayMessage" to "Rest is essential for muscle recovery. Take this time to recharge and prepare for your next workout.",
        "restDayTip" to "Light stretching or a walk can help maintain blood flow.",
        "dayLabel" to "Day", "ofCycle" to "of cycle",
        "howDoYouFeel" to "How do you feel?", "tiredLabel" to "Tired", "normalLabel" to "Normal", "energeticLabel" to "Energic",
         "technicalTip" to "Technical tip",
        "weeklySummary" to "Weekly Summary", "lastWeekLabel" to "last week",
        "goalLabel" to "Goal tip", "volumeLabel" to "Volume", "topExerciseLabel" to "Top exercise",
        "nutritionLabel" to "Nutrition", "motivationLabel" to "Motivation",
        "gpsCardioMap" to "Cardio", "startTracking" to "Start Tracking", 
        "pauseTracking" to "Pause", "resumeTracking" to "Resume",
        "distance" to "Distance", "pace" to "Pace", "speed" to "Speed", "duration" to "Duration",
        "savedRoutes" to "Saved Routes", "noSavedRoutes" to "No saved routes yet",
        "routeName" to "Route Name", "saveRoute" to "Save Route", "deleteRoute" to "Delete Route",
        "locationPermissionRequired" to "Location permission is required",
        "restDaysTitle" to "Rest Days & Deload", "restDaysSubtitle" to "Auto-schedule recovery, stretching, light yoga",
        "deloadWeek" to "Deload Week", "recoverySchedule" to "Recovery Schedule",
        "stretching" to "Stretching", "lightYoga" to "Light Yoga", "foamRolling" to "Foam Rolling",
         "nextRestDay" to "Next Rest Day",
        "muscleNeedsRest" to "Muscle needs rest", 
        "deloadInfo" to "Deload Info", "suggestedActivities" to "Suggested Activities",
        "activeRecovery" to "Active Recovery", "lightWalk" to "Light Walk",
        "save" to "Save",
        "allGood" to "All good", "alreadyHaveAccount" to "Already have an account?", "autoDeloadEnabled" to "Auto deload enabled",
        "avgRecovery" to "Average recovery", "caloriesBurned" to "Calories burned", "confirmPassword" to "Confirm password",
        "createAccountTitle" to "Create account", "deloadActive" to "Deload active", "deloadHistory" to "Deload history",
        "deloadInterval" to "Deload interval", "deloadNewValue" to "New value", "deloadNormalValue" to "Normal value",
        "deloadPreview" to "Deload preview", "deloadPreviewSubtitle" to "See reduced plan for next deload", "deloadActiveThisWeek" to "Deload active this week",         "recommendedForYou" to "Recommended for you", "tapToSchedule" to "Tap to schedule", "dontHaveAccount" to "Don't have an account?", "emailError" to "Invalid email",
        "endDeload" to "End deload", "foamRollingDescription" to "Release muscle tension with foam rolling",
        "heightCm" to "Height (cm)", "lissDescription" to "Light cardio for active recovery",
        "loginInstead" to "Log in instead", "musclesTiredCount" to "muscles tired",
        "nameError" to "Name is required", "nameField" to "Name", "optional" to "Optional",
        "passwordError" to "Password must be at least 6 characters", "passwordMismatch" to "Passwords don't match",
        "passwordStrengthMedium" to "Medium", "passwordStrengthStrong" to "Strong", "passwordStrengthWeak" to "Weak",
        "privacyPolicyLink" to "Privacy Policy", "recoveryTargeted" to "Targeted recovery",
        "startDeload" to "Start deload",          "stretchingDescription" to "Improve flexibility and mobility", "warmupStretch" to "Warm-up Stretch", "cooldownStretch" to "Cool-down Stretch",
        "termsAndConditions" to "Terms and Conditions", "termsPrefix" to "By continuing, you agree to our",
        "timeForDeload" to "Time for deload", "weeks" to "weeks",
        "deloadReasonAuto" to "Time for deload",
        "deloadReasonGeneral" to "General deload",
        "deloadDueBanner" to "Reduce your volume this week to recover fully",
        "dailyIntake" to "Daily Intake",
        "bio" to "Bio",
        "currentPassword" to "Current Password",
        "newPassword" to "New Password",
        "confirmNewPassword" to "Confirm New Password",
        "changeLabel" to "Change",
        "currentPasswordRequired" to "Current password is required",
        "passwordTooShort" to "New password must be at least 6 characters",
        "enterPasswordToConfirm" to "Enter your password to confirm",
        "passwordRequiredToDelete" to "Password is required to delete account",
        "lowLabel" to "Low",
        "highLabel" to "High",
        "newChat" to "New Chat",
        "serverSettings" to "Server Settings",
        "deloadWhyTitle" to "Why deload?",
        "deloadWhyBody" to "Training hard for %d weeks straight builds up fatigue. A deload week lowers the volume so your muscles recover fully and you come back stronger.",
        "deloadDuration" to "Duration",
        "deload1Week" to "1 week",
        "deload2Weeks" to "2 weeks",
        "deloadReduction" to "Reduction",
        "deloadCompound" to "CMP",
        "deloadNoHistory" to "No deloads yet. Once you complete a deload week, it will show up here with your recovery progress.",
        "editRestDay" to "Edit rest day",
        "deloadDayOf" to "Day %d of %d",
        "gpsSearching" to "Searching for GPS signal...",
        "gpsError" to "GPS error",
        "running" to "Running",
        "cycling" to "Cycling",
        "walking" to "Walking",
        "routePoints" to "Points",
        "paused" to "Paused", "estimatedOneRm" to "Est. 1RM", "nextSetSuggestion" to "Next set", "setTypeWarmup" to "Warm-up", "setTypeWorking" to "Working", "setTypeDrop" to "Drop", "setTypeAmrap" to "AMRAP", "setTypePaused" to "Paused", "setTypeTempo" to "Tempo", "rpeLabel" to "RPE",
        "readinessTitle" to "Readiness", "messages" to "Messages", "readinessScore" to "Today's readiness", "readinessHeavy" to "HEAVY — go hard", "readinessModerate" to "MODERATE — train normal", "readinessLight" to "LIGHT — recover or go easy", "readinessSleep" to "Sleep", "readinessSleepHours" to "Hours last night", "readinessQuality" to "Quality", "readinessSteps" to "Steps today", "readinessRecovery" to "Muscle recovery", "readinessVolume" to "Volume today", "readinessHint" to "Set your sleep to see your daily training recommendation.",
        "steps" to "Steps",
        "timerFinished" to "Time's up!",
        "timeToStartNextSet" to "Ready for your next set!",
        "readinessHydration" to "Hydration", "readinessTrend" to "7-day trend",
        "readinessActionSleep" to "Sleep more tonight for better recovery",
        "readinessActionSteps" to "A 20-min walk will boost your readiness",
        "readinessActionRecovery" to "Stretching today - your muscles need rest",
        "readinessActionVolume" to "High volume today - consider lighter training tomorrow",
        "readinessActionHydration" to "Drink more water - dehydration reduces performance",
        "readinessIntensityHeavy" to "Train hard - full intensity, add volume if you feel good",
        "readinessIntensityModerate" to "Train normal - stick to the plan",
        "readinessIntensityLight" to "Go easy - reduce volume by 30-40%, focus on form",
        "weeksSinceLastDeload" to "Weeks since last deload", "weightKg" to "Weight (kg)",
        "yogaDescription" to "Relax and improve mobility with light yoga",  
        "mostTrained" to "Most Trained", "allExercises" to "All Exercises",
        "vsPrevious" to "vs previous", 
        "sessions" to "sessions", 
        "gpsDisabledTitle" to "GPS disabled",
        "gpsDisabledMessage" to "Enable GPS in your phone settings to track your route in real time.",
        "openSettings" to "Open Settings",
         "weightKgLabel" to "Weight (kg)", "repsLabel" to "Reps",
        "estimated1rm" to "Estimated 1RM", "intensityZones" to "Intensity Zones",
        "zoneStrength" to "Strength (1-2 reps)", "zoneStrengthHypertrophy" to "Strength-Hypertrophy (3-5 reps)",
        "zoneHypertrophy" to "Hypertrophy (6-8 reps)", "zoneHypertrophyEndurance" to "Hypertrophy-Endurance (10-12 reps)",
        "zoneEndurance" to "Endurance (15+ reps)", 
        "totalVolumeLabel" to "Total Volume", "muscleGroups" to "Muscle Groups", "weeklyTab" to "Weekly",
         "pbsTab" to "PBs",
        "keepTraining" to "Keep training to beat your records!",
        "plateCalculatorTitle" to "Plate Calculator", "targetWeightLabel" to "Target Weight",
        "menu" to "Menu",
        "foodNamePlaceholder" to "Ex: Chicken Breast", "brandPlaceholder" to "Ex: Farm Foods", "backendServerAddress" to "Backend server address:", "aiApiKeyOptional" to "AI Trainer API Key (optional):", "leaveEmptyForDefaultServer" to "Leave empty for default server URL. API key only needed if server has auth enabled.", "kcal" to "KCAL", "deleteAccountWarning" to "This action is permanent and cannot be undone. All your data will be deleted.", "setStepGoal" to "Set step goal", "enterDailyStepGoal" to "Enter your daily step goal",
        "history" to "History", "deleteChat" to "Delete chat", "bodyAnatomy" to "Body anatomy", "leaveEmptyIfAuthDisabled" to "Leave empty if auth is disabled", "floatingWindow" to "Floating Window", "float" to "Float", "clear" to "Clear", "decrease" to "Decrease", "increase" to "Increase", "exercise" to "Exercise", "top" to "TOP", "play" to "Play", "reset" to "Reset", "selected" to "Selected", "loadingPlaylists" to "Loading playlists...", "failedToLoadPlaylists" to "Failed to load playlists", "noPlaylistsFound" to "No playlists found", "createPlaylistFirst" to "Create a playlist on Spotify first", "authenticationFailed" to "Authentication failed", "unknownError" to "Unknown error", "selectPlaylist" to "Select Playlist", "choosePlaylist" to "Choose a playlist for your workout", "tracks" to "tracks", "connectToSpotify" to "Connect to Spotify", "spotifyAccessDescription" to "Access your playlists and set the perfect workout soundtrack", "loginWithSpotify" to "Login with Spotify", "spotifyRedirectInfo" to "You'll be redirected to Spotify to authorize", "navy" to "Navy", "bmi" to "BMI", "exportBackup" to "Export backup",
        "barWeightLabel" to "Bar Weight", "lbsKg" to "lbs/kg",
        "platesPerSide" to "Plates per side", "plateUnit" to "plate",
        "eachSide" to "each side", "total" to "Total",
        "weightTooLight" to "Weight too light for plates (only bar:",
        "plateCalcNote" to "Plate Calculator automatically calculates which plates to add to the barbell to reach your target weight.",
        "howToGet" to "How to get:",
        "gender" to "Gender", "age" to "Age", "method" to "Method", "waistCm" to "Waist (cm)", "neckCm" to "Neck (cm)", "hipsCm" to "Hips (cm)", "estimatedBodyFat" to "Estimated Body Fat", "navyMethodInfo" to "Navy Method: uses a tape measure for circumferences",  "bodyFatCalculator" to "Body Fat Calculator", "newPRs" to "New PRs", "exerciseBreakdown" to "Exercise Breakdown", "done" to "Done", "weightGoal" to "Weight Goal", "currentWeight" to "Current Weight", "target" to "Target", "deadline" to "Deadline", "goalDetails" to "Goal Details", "startWeight" to "Start Weight", "targetWeight" to "Target Weight",  "noActiveGoal" to "No active goal", "setGoalToTrack" to "Set a goal to track your progress",         "setGoal" to "Set Goal", "pastGoals" to "Past Goals", "calculate" to "Calculate",
        "weightEvolution" to "Weight Evolution", "measurements" to "measurements", "startedOn" to "Started on", "editGoal" to "Edit goal",
        "equipDumbbells" to "Dumbbells", "equipBarbell" to "Barbell", "equipMachine" to "Machine", "equipCable" to "Cable", "equipBodyweight" to "Bodyweight", "equipEZBar" to "EZ Bar", "equipSmithMachine" to "Smith Machine", "equipKettlebell" to "Kettlebell", "equipStabilityBall" to "Stability Ball", "equipSledMachine" to "Sled Machine", "equipBand" to "Band",
        "energizeLabel" to "Energize",
        "performLabel" to "Perform",
        "pushItLabel" to "Push It",
        "openSpotifyLabel" to "Open Spotify",
        "tapToPlayLabel" to "Tap to play on Spotify",
        "startingWorkoutLabel" to "Starting workout music...",
        "signUpSuccessMessage" to "Account created! Please log in.",
        "trainingSectionLabel" to "Training", "frequencyLabel" to "Frequency", "xPerWeek" to "x / week", "editProfile" to "Edit Profile", "memberSince" to "Member since", "changePassword" to "Change Password",
        "updateTitle" to "New version available", "updateMessage" to "Kinetic %s has been released. You have v%s installed.\n\nTap to download the new APK.",         "updateDownload" to "Download", "updateLater" to "Later",
        "goalComplete" to "Goal reached!", "waterStreak" to "Hydration streak", "ofGoal" to "of goal", "editWaterGoal" to "Edit water goal", "newWaterGoal" to "New goal (ml)",
        "undo" to "Undo",
        "workoutReminderTitle" to "Today's Workout",
        "workoutReminderBody" to "Time to build strength. Focus on __GROUPS__ today. Give maximum effort on every set and beat your personal records.",
        "workoutReminderText" to "It's workout day! Get ready!",
        "workoutChannelName" to "Workout Reminders",
        "weeklySummaryTitle" to "Weekly Summary",
        "weeklySummaryText" to "You trained __COUNT__ times this week! Keep going!",
        "weeklySummaryChannelName" to "Weekly Summary",
        "streakReminderTitle" to "Don't break your streak!",
        "streakReminderText" to "Train today to maintain your __STREAK__-day streak!",
        "streakChannelName" to "Streak Reminders",
        "goalProgressTitle" to "Step Goal Progress",
        "goalProgressText" to "You've reached __PERCENT__% of your step goal! (__CURRENT__/__GOAL__)",
        "goalProgressChannelName" to "Goal Progress",
        "achievementTitle" to "Achievement Unlocked!",
        "achievementText" to "Congratulations! You've unlocked a new badge!",
        "achievementChannelName" to "Achievements"
    )

    private fun createRu() = Strings(enRaw() + mapOf(
         "stop" to "Стоп", "openApp" to "Открыть приложение", "goal" to "Цель", "stepGoalChannel" to "Цель по шагам", "stepGoalTitle" to "🏆 Цель по шагам достигнута!", "stepGoalText" to "Поздравляем! Вы прошли %d шагов!", "stepGoalBig" to "Поздравляем! Вы достигли своей цели в %d шагов!", "stepGoalKeepGoing" to "Продолжайте в том же духе!", "gpsChannelName" to "GPS-трекинг", "waterChannelName" to "Напоминания о воде", "biometricChannelName" to "Напоминания об измерениях", "friendChannelName" to "Запросы в друзья", "oneRmCalculator" to "Калькулятор 1ПМ", "plusGoal" to "+ Цель", "tierFree" to "БЕСПЛАТНО", "tierPro" to "ПРО", "tierProPlus" to "ПРО+", "tierLifetime" to "НАВСЕГДА",
         "dashboard" to "Панель",  "acasa" to "Главная", "workouts" to "Тренировки", "stats" to "Статистика", "waterIntake" to "Потребление воды", "waterGoal" to "Цель воды", "addWater" to "Добавить воду",  "height" to "Рост", "personalInfo" to "Личная информация",  "ml" to "мл", "templates" to "Шаблоны", "recovery" to "Восстановление",
              "everyDay" to "Каждый день", "reminder" to "Напоминание",
            "weeklyHistory" to "Недельная история", "tips" to "Советы", "customMl" to "мл вручную", "average" to "Среднее", "target" to "Цель",
            "waterTip1" to "Пейте 250мл каждые 30 мин во время тренировки.", "waterTip2" to "Утром: 500мл при пробуждении для активного метаболизма.", 
          "friends" to "Друзья", "leaderboard" to "Таблица лидеров", "all" to "Все",
         "language" to "Язык", "units" to "Единицы", "logout" to "Выход",
        "login" to "Вход", "signUp" to "Регистрация", "email" to "Эл. почта", "password" to "Пароль",
        "forgotPassword" to "Забыли пароль?", 
        "loginAsGuest" to "Войти как гость", 
         "goalStrength" to "Сила", "goalMass" to "Мышечная масса",
        "goalWeightLoss" to "Похудение", "goalMaintenance" to "Поддержание",
        "selectGoal" to "Выберите цель",
        "stepOf" to "Шаг %d из 5", "whatsYourExperience" to "Какой у вас уровень подготовки?",
        "beginnerLabel" to "Начинающий", "beginnerDesc" to "0-1 год тренировок",
        "intermediateLabel" to "Средний", "intermediateDesc" to "1-3 года регулярных тренировок",
        "advancedLabel" to "Продвинутый", "advancedDesc" to "3+ года серьёзных тренировок",
        "whatEquipment" to "Какое у вас оборудование?",
        "homeNoEquip" to "Дома - Без оборудования", "homeNoEquipDesc" to "Только упражнения с весом тела",
        "homeDumbbells" to "Дома - Гантели/Ленты", "homeDumbbellsDesc" to "Базовое домашнее оборудование",
        "fullGym" to "Полный зал", "fullGymDesc" to "Полный доступ к залу",
        "profileGoalLabel" to "Цель", "profileExperienceLabel" to "Опыт", "profileEquipmentLabel" to "Оборудование",
        "trainingFrequency" to "Частота тренировок", "sessionsPerWeek" to "Тренировок в неделю",
        "selectTrainingDays" to "Выберите дни тренировок",
        "monday" to "Понедельник", "tuesday" to "Вторник", "wednesday" to "Среда", "thursday" to "Четверг",
        "friday" to "Пятница", "saturday" to "Суббота", "sunday" to "Воскресенье",
        "physicalLimitations" to "Есть ли физические ограничения или травмы?",
        "physicalLimitationsPlaceholder" to "напр. боль в колене, проблемы со спиной (или оставьте пустым)",
        "whichMuscleGroups" to "Какие группы мышц?", "selectAtLeastOne" to "Шаг 7 из 7 - выберите хотя бы одну",
        "next" to "Далее", "skip" to "Пропустить", "finish" to "Готово",
        "back" to "Назад", "profileSetup" to "Настройка профиля", "enterName" to "Введите имя",
        "pickPhoto" to "Выберите фото", "saveProfile" to "Сохранить профиль", "chest" to "Грудь",
        "shoulders" to "Плечи", "arms" to "Руки", "biceps" to "Бицепс", "triceps" to "Трицепс",
        "legs" to "Ноги", "thighs" to "Бёдра", "glutes" to "Ягодичные", "calves" to "Икры", "core" to "Кор", "cardio" to "Кардио", "fullBody" to "Всё тело",
        "pleaseSelectOption" to "Пожалуйста, выберите опцию", "sets" to "Подходы", "reps" to "Повторы", "weight" to "Вес", 
         "startWorkout" to "Начать тренировку", "nextExercise" to "Следующее упражнение",
        "notes" to "Заметки", "cancel" to "Отмена", "confirm" to "Подтвердить", "delete" to "Удалить",
        "edit" to "Редактировать", "search" to "Поиск", "noDataYet" to "Данных пока нет",
         "sendRequest" to "Отправить заявку",
        "accept" to "Принять", "reject" to "Отклонить", "removeFriend" to "Удалить друга",
        "noFriends" to "Пока нет друзей", "searchUsers" to "Поиск пользователей",
         "searchByNameOrId" to "Поиск по имени или ID",
        "incomingRequests" to "Входящие заявки", 
        "yourFriends" to "Ваши друзья", 
        "friendRequestSent" to "Заявка отправлена",  "feedEmpty" to "Лента пуста",
         "workoutCompleted" to "Тренировка завершена!",
        "streakLabel" to "Текущая серия", "bestStreak" to "Лучшая серия", "badges" to "Значки",
          "kg" to "кг", "lbs" to "фунты",
          "xp" to "ОП",
          "max" to "Макс", "ok" to "ОК", "lv" to "УР",
         "exportCsv" to "Экспорт CSV",
        "importCsv" to "Импорт CSV", "subscription" to "Подписка", "premium" to "Премиум",
        "subscribe" to "Подписаться",  
         "error" to "Ошибка", "retry" to "Повторить", 
         "recoveryInfo" to "Информация о восстановлении",
        "recommendedRecovery" to "Рекомендуемое восстановление",
         "progressChart" to "График прогресса",
         "weightProgression" to "Прогрессия веса",
        "calendarView" to "Календарь",  
        "allGroups" to "Все", 
        "friendRequestNotificationTitle" to "Заявка в друзья",
        "friendRequestNotificationText" to "отправил(а) вам заявку в друзья!",
        "selectLanguage" to "Выберите язык",  
         "workoutsLabel" to "Тренировки", 
        "currentStreakLabel" to "Текущая серия", "bestStreakLabel" to "Лучшая серия",
         "days" to "дней",  
          "add" to "Добавить", "demoExercise" to "ДЕМО УПРАЖНЕНИЕ",  "prAndVolume" to "Личные рекорды и объём", "start" to "Старт",  "noSavedSetsYet" to "Нет сохранённых подходов.", "editSet" to "Редактировать подход",  "exercises" to "упражнений", "recovered" to "Восстановлен", "almostRecovered" to "Почти восстановлен", "moderate" to "Умеренно", "tired" to "Устал", "exhausted" to "Истощён", "fatigue" to "усталость", "chooseMuscleGroup" to "Выберите группу мышц",  "noExercisesFound" to "Упражнения не найдены", "tryDifferentFilter" to "Попробуйте другой фильтр или поиск по названию", "voiceSearch" to "Голосовой поиск",  "voiceSearchError" to "Не удалось распознать голос",
        "monthlyProgress" to "Прогресс за месяц", "completeWorkoutsToSee" to "Завершите тренировки чтобы увидеть прогресс", "jan" to "Янв", "feb" to "Фев", "mar" to "Мар", "apr" to "Апр", "may" to "Май", "jun" to "Июн", "jul" to "Июл", "aug" to "Авг", "sep" to "Сен", "oct" to "Окт", "nov" to "Ноя", "dec" to "Дек", "monthlyDetails" to "Детали месяца", "month" to "Месяц", "mon" to "Пн", "tue" to "Вт", "wed" to "Ср", "thu" to "Чт", "fri" to "Пт", "sat" to "Сб", "sun" to "Вс", "noWorkouts" to "Нет тренировок в этот день", 
         "subscribeNow" to "Подписаться", "premiumFeature" to "Премиум функция", "subscribersOnly" to "\$feature доступно только для подписчиков", "choosePlan" to "Выберите план", "youAreSubscribed" to "Вы подписаны!", "muscleRecovery" to "Восстановление мышц",  "waterReminderTitle" to "Время пить воду!", "waterReminderText" to "Пейте воду! Самое время выпить стакан воды.",   "selectTime" to "Выбрать время", "forearms" to "Предплечья", "neckAndTraps" to "Шея и Трапеции", "welcome" to "Добро пожаловать", "athlete" to "Спортсмен",
        "biometricTracking" to "Биометрический мониторинг",  "addMeasurement" to "Добавить измерение", "bodyFat" to "Жировая прослойка", "waistCirc" to "Талия", "hipsCirc" to "Бёдра", "thighsCirc" to "Бедра", "chestCirc" to "Грудь", "armsCirc" to "Руки",  "noMeasurements" to "Измерений пока нет",    "weeksAgo" to "недель назад", "cm" to "см", "percent" to "%", "deleteMeasurement" to "Удалить измерение", "biometricHistory" to "История измерений", "weightChart" to "График веса", "bodyFatChart" to "График жира", "circumferenceChart" to "График обхватов",   "biometricReminderTitle" to "Время для измерений!", "biometricReminderText" to "Не забудьте записать еженедельные измерения тела.",  "streakChannelName" to "Напоминания о серии", "streakReminderTitle" to "Не прерывайте серию!", "streakReminderText" to "Тренируйтесь сегодня, чтобы сохранить серию!",  "welcomeSoundLabel" to "Приветственный звук",
        "foodJournal" to "Дневник питания",    "scan" to "Сканировать", "scanning" to "Сканирование...", "scanBarcodeHelp" to "Убедитесь, что Google Play Services установлен и обновлен", "noFoodEntries" to "Записей о еде пока нет", "todaysMacros" to "Макронутриенты сегодня", "stepsLabel" to "Шаги", "activeTimeLabel" to "Активное время", "caloriesLabel" to "Калории", "proteinLabel" to "Белки", "carbsLabel" to "Углеводы", "fatLabel" to "Жиры", "breakfast" to "Завтрак", "lunch" to "Обед", "dinner" to "Ужин", "snack" to "Перекус", "drinks" to "Напитки", "selectMealType" to "Выберите тип приема пищи",  "foodName" to "Название продукта", "brandLabel" to "Бренд",     "fiber" to "Клетчатка", "searchFood" to "Поиск продукта", "foodSearchHint" to "Напр.: яйцо, курица, рис", "quantity" to "Количество", "gramsShort" to "г", "piecesShort" to "шт", "addToJournal" to "Добавить в журнал", "manualEntryMode" to "Ввод вручную", "noFoodFound" to "Продукт не найден в списке", "enterManually" to "Ввести продукт вручную", "per100g" to "на 100г", "perPiece" to "за штуку",
        "aiTrainer" to "ИИ Тренер", "aiTrainerWelcome" to "Привет! Я ваш ИИ тренер", "aiTrainerHint" to "Спросите меня о тренировках, питании или прогрессе", "aiTrainerHistory" to "История чатов", "noHistoryYet" to "История пуста", "current" to "Текущий", "askAiTrainer" to "Спросите тренера...", "aiSuggestion1" to "Какую тренировку вы рекомендуете сегодня?", "aiSuggestion2" to "Как увеличить объём?", "aiSuggestion3" to "Нужен ли мне день отдыха?", "aiSuggestion4" to "Как выйти из плато?",
         "deleteAccount" to "Удалить аккаунт",
        "exerciseHistory" to "История упражнения",  
        "favorite" to "Избранное",  "savedExercises" to "Сохранённые упражнения", "noFavorites" to "Нет сохранённых упражнений", "tapStarToSave" to "Нажмите звезду, чтобы сохранить упражнение", "removeFavorite" to "Удалить из избранного", 
        "addSet" to "Добавить подход", "exerciseNotes" to "Заметки", 
        "saveNotes" to "Сохранить", 
        "volume" to "Объём", "maxWeight" to "Макс вес", "maxReps" to "Макс повторы", "maxSet" to "Макс подход",
        "today" to "Сегодня", "thisWeek" to "Эта неделя", "thisMonth" to "Этот месяц",
        "totalVolumeLabel" to "Общий объём",
        "guest" to "Гость", "loginWithGoogle" to "Войти через Google", "loginWithFacebook" to "Войти через Facebook",
        "close" to "Закрыть",  "profile" to "Профиль",
        "appTagline" to "Тренируйся. Прогрессируй. Повторяй.", "or" to "или", "dark" to "Тёмная", "light" to "Светлая",
        "system" to "Системная",  
        "selectTheme" to "Выбрать тему", "settingsAndMore" to "Настройки и ещё",
        "muscleGroups" to "Мышечные группы",  "features" to "Функции", "activity" to "Активность", "tools" to "Инструменты", 
        "englishUS" to "Английский", "romana" to "Румынский", "russkiy" to "Русский", "ukrainska" to "Украинский",
        "francais" to "Французский", "deutsch" to "Немецкий", "espanol" to "Испанский",
        "italiano" to "Итальянский", "turkce" to "Турецкий", "portugues" to "Португальский", "polski" to "Польский",
        "motto1" to "Каждое повторение на счету.", "motto2" to "Сильнее, чем вчера.",
        "motto3" to "Твоё тело, твои правила.", "motto4" to "Превзойди свои пределы.",
        "motto5" to "Последовательность побеждает талант.", "motto6" to "Дисциплина — это свобода.",
        "motto7" to "Без коротких путей.", "motto8" to "Заработано, а не дано.",
        "motto9" to "Не останавливайся, когда устал. Останавливайся, когда закончил.", "motto10" to "Боль сегодня становится силой завтра.",
        "motto11" to "Великие вещи никогда не рождаются в зоне комфорта.", "motto12" to "Никогда не становится легче. Просто ты становишься сильнее.",
        "motto13" to "Не нужно быть экстремальным, просто будь последовательным.", "motto14" to "Твой единственный предел — это ты сам.",
        "motto15" to "Любой эксперт когда-то был новичком.", "motto16" to "Тело достигает того, во что верит разум.",
        "motto17" to "Отдых — часть процесса, а не враг.", "motto18" to "Маленькие шаги каждый день ведут к большим результатам.",
        "motto19" to "Тренируйся, как будто ты голоден. Оставайся скромным.", "motto20" to "Успех — это сумма маленьких усилий, повторяемых ежедневно.",
        "goodMorning" to "Доброе утро", "goodAfternoon" to "Добрый день", "goodEvening" to "Добрый вечер",
        "daysConsecutive" to "дней подряд", "todaysWorkout" to "Тренировка сегодня",
        "todayYouRest" to "Сегодня отдых", "restDayMessage" to "Отдых необходим для восстановления мышц. Используйте это время для восстановления и подготовки к следующей тренировке.",
        "restDayTip" to "Лёгкая растяжка или прогулка помогут поддержать кровообращение.",
        "dayLabel" to "День", "ofCycle" to "цикла",
        "howDoYouFeel" to "Как вы себя чувствуете?", "tiredLabel" to "Устал", "normalLabel" to "Нормально", "energeticLabel" to "Энергичен",
         "technicalTip" to "Технический совет",
        "weeklySummary" to "Итоги недели", "lastWeekLabel" to "прошл. неделя",
        "goalLabel" to "Совет по цели", "volumeLabel" to "Объём", "topExerciseLabel" to "Топ упражнение",
        "nutritionLabel" to "Питание", "motivationLabel" to "Мотивация",
        "gpsCardioMap" to "Cardio", "startTracking" to "Начать отслеживание", 
        "pauseTracking" to "Пауза", "resumeTracking" to "Продолжить",
        "distance" to "Дистанция", "pace" to "Темп", "speed" to "Скорость", "duration" to "Длительность",
        "savedRoutes" to "Сохранённые маршруты", "noSavedRoutes" to "Нет сохранённых маршрутов",
        "routeName" to "Название маршрута", "saveRoute" to "Сохранить маршрут", "deleteRoute" to "Удалить маршрут",
        "locationPermissionRequired" to "Требуется разрешение на местоположение",
        "restDaysTitle" to "Дни отдыха и разгрузка", "restDaysSubtitle" to "Автоматическое планирование восстановления, растяжки, лёгкой йоги",
        "deloadWeek" to "Неделя разгрузки", "recoverySchedule" to "График восстановления",
        "stretching" to "Растяжка", "lightYoga" to "Лёгкая йога", "foamRolling" to "Фоамроллинг",
         "nextRestDay" to "Следующий день отдыха",
        "muscleNeedsRest" to "Мышцам нужен отдых", 
        "deloadInfo" to "Информация о разгрузке", "suggestedActivities" to "Рекомендованные активности",
        "activeRecovery" to "Активное восстановление", "lightWalk" to "Лёгкая прогулка",
        "save" to "Сохранить",
        "allGood" to "Всё в порядке", "alreadyHaveAccount" to "Уже есть аккаунт?", "autoDeloadEnabled" to "Авторазгрузка включена",
        "avgRecovery" to "Среднее восстановление", "caloriesBurned" to "Сожжённые калории", "confirmPassword" to "Подтвердите пароль",
        "createAccountTitle" to "Создать аккаунт", "deloadActive" to "Разгрузка активна", "deloadHistory" to "История разгрузок",
        "deloadInterval" to "Интервал разгрузки", "deloadNewValue" to "Новое значение", "deloadNormalValue" to "Нормальное значение",
        "deloadPreview" to "Предпросмотр разгрузки", "deloadPreviewSubtitle" to "Посмотрите сокращённый план для следующей разгрузки", "deloadActiveThisWeek" to "Разгрузка активна на этой неделе",         "recommendedForYou" to "Рекомендовано для вас", "tapToSchedule" to "Нажмите для планирования", "dontHaveAccount" to "Нет аккаунта?", "emailError" to "Неверный email",
        "endDeload" to "Завершить разгрузку", "foamRollingDescription" to "Снимите напряжение мышц с помощью ролла",
        "heightCm" to "Рост (см)", "lissDescription" to "Лёгкая кардио для активного восстановления",
        "loginInstead" to "Войти вместо этого", "musclesTiredCount" to "мышц устали",
        "nameError" to "Имя обязательно", "nameField" to "Имя", "optional" to "Необязательно",
        "passwordError" to "Пароль должен содержать минимум 6 символов", "passwordMismatch" to "Пароли не совпадают",
        "passwordStrengthMedium" to "Средний", "passwordStrengthStrong" to "Сильный", "passwordStrengthWeak" to "Слабый",
        "privacyPolicyLink" to "Политика конфиденциальности", "recoveryTargeted" to "Целевое восстановление",
        "startDeload" to "Начать разгрузку", "stretchingDescription" to "Улучшите гибкость и мобильность", "warmupStretch" to "Разминка", "cooldownStretch" to "Заминка",
        "termsAndConditions" to "Условия использования", "termsPrefix" to "Продолжая, вы соглашаетесь с нашими",
        "timeForDeload" to "Время для разгрузки", "weeks" to "недель",
        "deloadReasonAuto" to "Время для разгрузки",
        "deloadReasonGeneral" to "Общая разгрузка",
        "deloadDueBanner" to "Снизьте объём на этой неделе для полного восстановления",
        "dailyIntake" to "Дневное потребление",
        "bio" to "О себе",
        "currentPassword" to "Текущий пароль",
        "newPassword" to "Новый пароль",
        "confirmNewPassword" to "Подтвердите новый пароль",
        "changeLabel" to "Изменить",
        "currentPasswordRequired" to "Требуется текущий пароль",
        "passwordTooShort" to "Новый пароль должен содержать минимум 6 символов",
        "enterPasswordToConfirm" to "Введите пароль для подтверждения",
        "passwordRequiredToDelete" to "Для удаления аккаунта требуется пароль",
        "lowLabel" to "Низкий",
        "highLabel" to "Высокий",
        "newChat" to "Новый чат",
        "serverSettings" to "Настройки сервера",
        "deloadWhyTitle" to "Зачем разгрузка?",
        "deloadWhyBody" to "Интенсивные тренировки %d недель подряд накапливают усталость. Разгрузочная неделя снижает объём, чтобы мышцы полностью восстановились, и вы вернулись сильнее.",
        "deloadDuration" to "Длительность",
        "deload1Week" to "1 неделя",
        "deload2Weeks" to "2 недели",
        "deloadReduction" to "Снижение",
        "deloadCompound" to "БАЗ",
        "deloadNoHistory" to "Разгрузок пока нет. После завершения разгрузочной недели она появится здесь с прогрессом восстановления.",
        "editRestDay" to "Изменить день отдыха",
        "deloadDayOf" to "День %d из %d",
        "gpsSearching" to "Поиск GPS-сигнала...",
        "gpsError" to "Ошибка GPS",
        "running" to "Бег",
        "cycling" to "Велосипед",
        "walking" to "Ходьба",
        "routePoints" to "Точки",
        "paused" to "Пауза", "estimatedOneRm" to "Расч. 1ПМ", "nextSetSuggestion" to "Следующий подход", "setTypeWarmup" to "Разминка", "setTypeWorking" to "Рабочий", "setTypeDrop" to "Дроп", "setTypeAmrap" to "AMRAP", "setTypePaused" to "Пауза", "setTypeTempo" to "Темпо", "rpeLabel" to "RPE",
        "steps" to "шагов",
        "timerFinished" to "Время вышло!",
        "timeToStartNextSet" to "Готовы к следующему подходу!",
        "weeksSinceLastDeload" to "Недель с последней разгрузки", "weightKg" to "Вес (кг)",
        "yogaDescription" to "Расслабьтесь и улучшите мобильность с помощью йоги",  
        "gpsDisabledTitle" to "GPS отключён",
        "gpsDisabledMessage" to "Включите GPS в настройках телефона для отслеживания маршрута в реальном времени.",
        "openSettings" to "Открыть настройки",
         "weightKgLabel" to "Вес (кг)", "repsLabel" to "Повторения",
        "estimated1rm" to "Расчётное 1ПМ", "intensityZones" to "Зоны интенсивности",
        "zoneStrength" to "Сила (1-2 повт.)", "zoneStrengthHypertrophy" to "Сила-Гипертрофия (3-5 повт.)",
        "zoneHypertrophy" to "Гипертрофия (6-8 повт.)", "zoneHypertrophyEndurance" to "Гипертрофия-Выносливость (10-12 повт.)",
        "zoneEndurance" to "Выносливость (15+ повт.)", 
        "totalVolumeLabel" to "Общий объём", "muscleGroups" to "Группы мышц", "weeklyTab" to "Неделя",
         "pbsTab" to "Рекорды",
        "keepTraining" to "Продолжайте тренироваться чтобы побить свои рекорды!",
        "plateCalculatorTitle" to "Калькулятор блинов", "targetWeightLabel" to "Целевой вес",
        "menu" to "Меню",
        "foodNamePlaceholder" to "Напр.: куриная грудка", "brandPlaceholder" to "Напр.: Farm Foods", "backendServerAddress" to "Адрес сервера:", "aiApiKeyOptional" to "API-ключ AI-тренера (необязательно):", "leaveEmptyForDefaultServer" to "Оставьте пустым для URL сервера по умолчанию. Ключ API нужен, только если на сервере включена аутентификация.", "kcal" to "ККАЛ", "deleteAccountWarning" to "Это действие необратимо. Все ваши данные будут удалены.", "setStepGoal" to "Установить цель по шагам", "enterDailyStepGoal" to "Введите вашу дневную цель по шагам",
        "history" to "История", "deleteChat" to "Удалить чат", "bodyAnatomy" to "Анатомия тела", "leaveEmptyIfAuthDisabled" to "Оставьте пустым, если аутентификация отключена", "floatingWindow" to "Плавающее окно", "float" to "Поверх", "clear" to "Очистить", "decrease" to "Уменьшить", "increase" to "Увеличить", "exercise" to "Упражнение", "top" to "ТОП", "play" to "Играть", "reset" to "Сбросить", "selected" to "Выбрано", "loadingPlaylists" to "Загрузка плейлистов...", "failedToLoadPlaylists" to "Не удалось загрузить плейлисты", "noPlaylistsFound" to "Плейлисты не найдены", "createPlaylistFirst" to "Сначала создайте плейлист в Spotify", "authenticationFailed" to "Ошибка аутентификации", "unknownError" to "Неизвестная ошибка", "selectPlaylist" to "Выберите плейлист", "choosePlaylist" to "Выберите плейлист для тренировки", "tracks" to "треков", "connectToSpotify" to "Подключиться к Spotify", "spotifyAccessDescription" to "Получите доступ к своим плейлистам и создайте идеальный саундтрек для тренировки", "loginWithSpotify" to "Войти через Spotify", "spotifyRedirectInfo" to "Вы будете перенаправлены в Spotify для авторизации", "navy" to "Navy", "bmi" to "ИМТ", "exportBackup" to "Экспорт резервной копии",
        "barWeightLabel" to "Вес штанги", "lbsKg" to "фунт/кг",
        "platesPerSide" to "Блины на каждую сторону", "plateUnit" to "блина",
        "eachSide" to "на каждую сторону", "total" to "Итого",
        "weightTooLight" to "Вес слишком мал для блинов (только штанга:",
        "plateCalcNote" to "Калькулятор блинов автоматически вычисляет какие блины нужно добавить на штангу для достижения целевого веса.",
        "howToGet" to "Как получить:",
        "gender" to "Пол", "age" to "Возраст", "method" to "Метод", "waistCm" to "Талия (см)", "neckCm" to "Шея (см)", "hipsCm" to "Бёдра (см)", "estimatedBodyFat" to "Расчётный % жира", "navyMethodInfo" to "Метод ВМС: использует измерение обхватов",  "bodyFatCalculator" to "Калькулятор жира", "newPRs" to "Новые рекорды", "exerciseBreakdown" to "Детали упражнений", "done" to "Готово", "weightGoal" to "Цель по весу", "currentWeight" to "Текущий вес", "target" to "Цель", "deadline" to "Срок", "goalDetails" to "Детали цели", "startWeight" to "Начальный вес", "targetWeight" to "Целевой вес",  "noActiveGoal" to "Нет активной цели", "setGoalToTrack" to "Установите цель для отслеживания прогресса",         "setGoal" to "Установить цель", "pastGoals" to "Прошлые цели", "calculate" to "Рассчитать",
        "weightEvolution" to "Динамика веса", "measurements" to "измерений", "startedOn" to "Начато", "editGoal" to "Изменить цель",
        "equipDumbbells" to "Гантели", "equipBarbell" to "Штанга", "equipMachine" to "Тренажёр", "equipCable" to "Блок", "equipBodyweight" to "Свой вес", "equipEZBar" to "EZ штанга", "equipSmithMachine" to "Смит", "equipKettlebell" to "Гиря", "equipStabilityBall" to "Фитбол", "equipSledMachine" to "Толкающая платформа", "equipBand" to "Лента",
        "energizeLabel" to "Энергия",
        "performLabel" to "Производительность",
        "pushItLabel" to "Давай",
        "openSpotifyLabel" to "Открыть Spotify",
        "tapToPlayLabel" to "Нажмите для воспроизведения в Spotify",
        "startingWorkoutLabel" to "Запускаем музыку для тренировки...",
        "signUpSuccessMessage" to "Аккаунт создан! Пожалуйста, войдите.",
        "trainingSectionLabel" to "Тренировки", "frequencyLabel" to "Частота", "xPerWeek" to "x / нед", "editProfile" to "Редактировать профиль", "memberSince" to "Участник с", "changePassword" to "Сменить пароль",
        "updateTitle" to "Доступна новая версия", "updateMessage" to "Kinetic %s выпущена. У вас установлена версия v%s.\n\nНажмите, чтобы скачать новый APK.", "updateDownload" to "Скачать", "updateLater" to "Позже",
        "activeDesc" to "Тренировки + ежедневная активность",
        "permanentPlan" to "Пожизненный план",
        "vsPrevious" to "по сравнению с прошлым",
        "free" to "Бесплатно",
        "lifetimeAccess" to "Пожизненный доступ",
        "male" to "Мужчина",
        "purchaseFailed" to "Ошибка покупки",
        "perMonth" to "/мес",
        "veryActiveDesc" to "Интенсивные тренировки + физическая работа",
        "restorePurchase" to "Восстановить покупки",
        "freePlan" to "Бесплатный",
        "veryActive" to "Очень активный",
        "whatsYourAge" to "Сколько вам лет?",
        "sedentary" to "Сидячий",
        "unlockedForMinutes" to "Разблокировано: осталось %s",
        "watchAdToUnlock" to "Посмотреть рекламу (разблокировать на 30 мин)",
        "remaining" to "осталось",
        "whatsYourGender" to "Ваш пол?",
        "restoreSuccess" to "Покупки восстановлены",
        "buyNow" to "Купить",
        "bestValue" to "Лучшая цена",
        "noPurchasesToRestore" to "Нет покупок для восстановления",
        "sessions" to "тренировки",
        "active" to "Активный",
        "allExercises" to "Все упражнения",
        "saveExercise" to "Сохранить упражнение",
        "perYear" to "/год",
        "mostPopular" to "Самый популярный",
        "unlockPremiumSubtitle" to "Получите доступ ко всем расширенным функциям",
        "female" to "Женщина",
        "purchaseSuccess" to "Покупка успешна! Добро пожаловать в Premium.",
        "upgradeToUnlock" to "Обновите тариф, чтобы разблокировать",
        "dailyAdLimitReached" to "Вы достигли дневного лимита разблокировок",
        "sedentaryDesc" to "Офисная работа, мало движений",
        "adUnlockSuccess" to "Функция разблокирована на 30 минут!",
        "cancelAnytime" to "Отмена в любое время в Google Play",
        "purchaseCancelled" to "Покупка отменена",
        "currentPlan" to "Текущий план",
        "adNotReady" to "Реклама не готова. Попробуйте еще раз.",
        "whatsYourActivityLevel" to "Каков ваш уровень активности?",
        "workoutAnalytics" to "Аналитика тренировок",
        "mostTrained" to "Самое тренируемое",
        "unlockPremiumTitle" to "Откройте Kinetic Premium",
        "oneTimePayment" to "разовый платеж",
        "goalComplete" to "Цель достигнута!", "waterStreak" to "Серия гидратации", "ofGoal" to "от цели", "editWaterGoal" to "Изменить цель воды", "newWaterGoal" to "Новая цель (мл)",
        "undo" to "Отменить",
        "workoutReminderTitle" to "Тренировка сегодня",
        "workoutReminderBody" to "Время наращивать силу. Сосредоточьтесь на __GROUPS__ сегодня. Выкладывайтесь на максимум на каждом подходе и побеждайте свои личные рекорды.",
        "workoutReminderText" to "Сегодня день тренировки! Приготовьтесь!",
        "workoutChannelName" to "Напоминания о тренировках",
        "weeklySummaryTitle" to "Итоги недели",
        "weeklySummaryText" to "Вы тренировались __COUNT__ раз за эту неделю! Продолжайте!",
        "weeklySummaryChannelName" to "Еженедельный отчёт",
        "streakReminderTitle" to "Не прерывайте серию!",
        "streakReminderText" to "Тренируйтесь сегодня, чтобы сохранить серию из __STREAK__ дней!",
        "streakChannelName" to "Напоминания о серии",
        "goalProgressTitle" to "Прогресс шагов",
        "goalProgressText" to "Вы достигли __PERCENT__% цели по шагам! (__CURRENT__/__GOAL__)",
        "goalProgressChannelName" to "Прогресс цели",
        "achievementTitle" to "Достижение открыто!",
        "achievementText" to "Поздравляем! Вы открыли новый значок!",
        "achievementChannelName" to "Достижения"
    ))

    private fun createUk() = Strings(enRaw() + mapOf(
         "stop" to "Стоп", "openApp" to "Відкрити застосунок", "goal" to "Ціль", "stepGoalChannel" to "Мета кроків", "stepGoalTitle" to "🏆 Мета кроків досягнута!", "stepGoalText" to "Вітаємо! Ви пройшли %d кроків!", "stepGoalBig" to "Вітаємо! Ви досягли своєї мети в %d кроків!", "stepGoalKeepGoing" to "Продовжуйте в тому ж дусі!", "gpsChannelName" to "GPS-відстеження", "waterChannelName" to "Нагадування про воду", "biometricChannelName" to "Нагадування про вимірювання", "friendChannelName" to "Запити в друзі", "oneRmCalculator" to "Калькулятор 1ПМ", "plusGoal" to "+ Мета", "tierFree" to "БЕЗКОШТОВНО", "tierPro" to "ПРО", "tierProPlus" to "ПРО+", "tierLifetime" to "НАЗАВЖДИ",
         "dashboard" to "Панель",  "acasa" to "Головна", "workouts" to "Тренування", "stats" to "Статистика", "waterIntake" to "Споживання води", "waterGoal" to "Мета води", "addWater" to "Додати воду",  "height" to "Зріст", "personalInfo" to "Особиста інформація",  "ml" to "мл", "templates" to "Шаблони",
              "everyDay" to "Щодня", "reminder" to "Нагадування",
            "weeklyHistory" to "Тижнева історія", "tips" to "Поради", "customMl" to "мл вручну", "average" to "Середня", "target" to "Мета",
            "waterTip1" to "Пийте 250мл кожні 30 хв під час тренування.", "waterTip2" to "Вранці: 500мл при пробудженні для активного метаболізму.", 
        "recovery" to "Відновлення",   "friends" to "Друзі",
        "leaderboard" to "Таблиця лідерів", "all" to "Усі",  "language" to "Мова",
        "units" to "Одиниці", "logout" to "Вихід", "login" to "Вхід", "signUp" to "Реєстрація",
        "email" to "Ел. пошта", "password" to "Пароль", "forgotPassword" to "Забули пароль?",
         "loginAsGuest" to "Увійти як гість",
        "goalStrength" to "Сила", "goalMass" to "М'язова маса", "goalWeightLoss" to "Схуднення",
        "goalMaintenance" to "Підтримання", "selectGoal" to "Оберіть ціль",
        "stepOf" to "Крок %d з 5", "whatsYourExperience" to "Який у вас рівень досвіду?",
        "beginnerLabel" to "Початківець", "beginnerDesc" to "0-1 рік тренувань",
        "intermediateLabel" to "Середній", "intermediateDesc" to "1-3 роки регулярних тренувань",
        "advancedLabel" to "Просунутий", "advancedDesc" to "3+ роки серйозних тренувань",
        "whatEquipment" to "Яке у вас обладнання?",
        "homeNoEquip" to "Вдома - Без обладнання", "homeNoEquipDesc" to "Тільки вправи з вагою тіла",
        "homeDumbbells" to "Вдома - Гантелі/Стрічки", "homeDumbbellsDesc" to "Базове домашнє обладнання",
        "fullGym" to "Повний зал", "fullGymDesc" to "Повний доступ до залу",
        "profileGoalLabel" to "Мета", "profileExperienceLabel" to "Досвід", "profileEquipmentLabel" to "Обладнання",
        "trainingFrequency" to "Частота тренувань", "sessionsPerWeek" to "Тренувань на тиждень",
        "selectTrainingDays" to "Оберіть дні тренувань",
        "monday" to "Понеділок", "tuesday" to "Вівторок", "wednesday" to "Середа", "thursday" to "Четвер",
        "friday" to "П'ятниця", "saturday" to "Субота", "sunday" to "Неділя",
        "physicalLimitations" to "Чи є фізичні обмеження або травми?",
        "physicalLimitationsPlaceholder" to "напр. біль у коліні, проблеми зі спиною (або залиште порожнім)",
        "whichMuscleGroups" to "Які групи м'язів?", "selectAtLeastOne" to "Крок 7 з 7 - оберіть хоча б одну",
        "next" to "Далі", "skip" to "Пропустити", "finish" to "Завершити",
        "skip" to "Пропустити", "finish" to "Готово", "back" to "Назад",
        "profileSetup" to "Налаштування профілю", "enterName" to "Введіть ім'я",
        "pickPhoto" to "Оберіть фото", "saveProfile" to "Зберегти профіль", "chest" to "Груди",
        "shoulders" to "Плечі", "arms" to "Руки", "biceps" to "Біцепс", "triceps" to "Трицепс",
        "legs" to "Ноги", "thighs" to "Стегна", "glutes" to "Сідничні", "calves" to "Гомілки", "core" to "Кор", "cardio" to "Кардіо", "fullBody" to "Все тіло",
        "pleaseSelectOption" to "Будь ласка, оберіть опцію", "sets" to "Підходи", "reps" to "Повтори", "weight" to "Вага", 
         "startWorkout" to "Почати тренування", "nextExercise" to "Наступна вправа",
        "notes" to "Нотатки", "cancel" to "Скасувати", "confirm" to "Підтвердити", "delete" to "Видалити",
        "edit" to "Редагувати", "search" to "Пошук", "noDataYet" to "Даних поки немає",
         "sendRequest" to "Надіслати запит",
        "accept" to "Прийняти", "reject" to "Відхилити", "removeFriend" to "Видалити друга",
        "noFriends" to "Поки немає друзів", "searchUsers" to "Пошук користувачів",
         "searchByNameOrId" to "Пошук за ім'ям або ID",
        "incomingRequests" to "Вхідні запити", 
        "yourFriends" to "Ваші друзі", 
        "friendRequestSent" to "Запит надіслано",  "feedEmpty" to "Стрічка порожня",
        "workoutCompleted" to "Тренування завершено!", "streakLabel" to "Поточна серія",
        "bestStreak" to "Найкраща серія", "badges" to "Значки", 
         "kg" to "кг", "lbs" to "фунти", 
         "xp" to "ОД",
         "max" to "Макс", "ok" to "ОК", "lv" to "РВ",
        "exportCsv" to "Експорт CSV", "importCsv" to "Імпорт CSV", "subscription" to "Підписка",
        "premium" to "Преміум",  
        "subscribe" to "Підписатися",  
         "error" to "Помилка", "retry" to "Повторити",
         "recoveryInfo" to "Інформація про відновлення",
        "recommendedRecovery" to "Рекомендоване відновлення",
         "progressChart" to "Графік прогресу",
         "weightProgression" to "Прогресія ваги",
        "calendarView" to "Календар",  
        "allGroups" to "Всі", 
        "friendRequestNotificationTitle" to "Запит в друзі",
        "friendRequestNotificationText" to "надіслав(а) вам запит в друзі!",
        "selectLanguage" to "Оберіть мову",  
         "workoutsLabel" to "Тренування", 
        "currentStreakLabel" to "Поточна серія", "bestStreakLabel" to "Найкраща серія",
         "days" to "днів",  
          "add" to "Додати", "demoExercise" to "ДЕМО ВПРАВА",  "prAndVolume" to "Особисті рекорди та обсяг", "start" to "Старт",  "noSavedSetsYet" to "Немає збережених підходів.", "editSet" to "Редагувати підхід",  "exercises" to "вправ", "recovered" to "Відновлено", "almostRecovered" to "Майже відновлено", "moderate" to "Помірно", "tired" to "Втомлений", "exhausted" to "Виснажений", "fatigue" to "втома", "chooseMuscleGroup" to "Оберіть групу м'язів",  "noExercisesFound" to "Вправи не знайдено", "tryDifferentFilter" to "Спробуйте інший фільтр або пошук за назвою", "voiceSearch" to "Голосовий пошук",  "voiceSearchError" to "Не вдалося розпізнати голос",
        "monthlyProgress" to "Прогрес за місяць", "completeWorkoutsToSee" to "Завершіть тренування щоб побачити прогрес", "jan" to "Січ", "feb" to "Лют", "mar" to "Бер", "apr" to "Кві", "may" to "Тра", "jun" to "Чер", "jul" to "Лип", "aug" to "Сер", "sep" to "Вер", "oct" to "Жов", "nov" to "Лис", "dec" to "Гру", "monthlyDetails" to "Деталі місяця", "month" to "Місяць", "mon" to "Пн", "tue" to "Вт", "wed" to "Ср", "thu" to "Чт", "fri" to "Пт", "sat" to "Сб", "sun" to "Нд", "noWorkouts" to "Немає тренувань в цей день", 
         "subscribeNow" to "Підписатися", "premiumFeature" to "Преміум функція", "subscribersOnly" to "\$feature доступне лише для підписників", "choosePlan" to "Оберіть план", "youAreSubscribed" to "Ви підписані!", "muscleRecovery" to "Відновлення м'язів",  "waterReminderTitle" to "Час пити воду!", "waterReminderText" to "Пийте воду! Настав час випити склянку води.",   "selectTime" to "Обрати час", "forearms" to "Передпліччя", "neckAndTraps" to "Шия і Трапеція", "welcome" to "Ласкаво просимо", "athlete" to "Спортсмен",
        "biometricTracking" to "Біометричний моніторинг",  "addMeasurement" to "Додати вимірювання", "bodyFat" to "Жирова прошарок", "waistCirc" to "Талія", "hipsCirc" to "Стегна", "thighsCirc" to "Бедра", "chestCirc" to "Груди", "armsCirc" to "Руки",  "noMeasurements" to "Вимірювань поки немає",    "weeksAgo" to "тижнів тому", "cm" to "см", "percent" to "%", "deleteMeasurement" to "Видалити вимірювання", "biometricHistory" to "Історія вимірювань", "weightChart" to "Графік ваги", "bodyFatChart" to "Графік жиру", "circumferenceChart" to "Графік обхватів",   "biometricReminderTitle" to "Час для вимірювань!", "biometricReminderText" to "Не забудьте записати тижневі вимірювання тіла.",  "streakChannelName" to "Нагадування про серію", "streakReminderTitle" to "Не переривайте серію!", "streakReminderText" to "Тренуйтесь сьогодні, щоб зберегти серію!",  "welcomeSoundLabel" to "Вітальний звук",
        "foodJournal" to "Щоденник харчування",    "scan" to "Сканувати", "scanning" to "Сканування...", "scanBarcodeHelp" to "Переконайтеся, що Google Play Services встановлено та оновлено", "noFoodEntries" to "Записів про їжу поки немає", "todaysMacros" to "Макронутрієнти сьогодні", "stepsLabel" to "Кроки", "activeTimeLabel" to "Активний час", "caloriesLabel" to "Калорії", "proteinLabel" to "Білки", "carbsLabel" to "Вуглеводи", "fatLabel" to "Жири", "breakfast" to "Сніданок", "lunch" to "Обід", "dinner" to "Вечеря", "snack" to "Перекус", "drinks" to "Напої", "selectMealType" to "Оберіть тип прийому їжі",  "foodName" to "Назва продукту", "brandLabel" to "Бренд",     "fiber" to "Клітковина", "searchFood" to "Пошук продукту", "foodSearchHint" to "Напр.: яйце, курка, рис", "quantity" to "Кількість", "gramsShort" to "г", "piecesShort" to "шт", "addToJournal" to "Додати до журналу", "manualEntryMode" to "Введення вручну", "noFoodFound" to "Продукт не знайдено в списку", "enterManually" to "Ввести продукт вручну", "per100g" to "на 100г", "perPiece" to "за штуку",
        "aiTrainer" to "ШІ Тренер", "aiTrainerWelcome" to "Привіт! Я ваш ШІ тренер", "aiTrainerHint" to "Запитайте мене про тренування, харчування або прогрес", "aiTrainerHistory" to "Історія чатів", "noHistoryYet" to "Історія порожня", "current" to "Поточний", "askAiTrainer" to "Запитайте тренера...", "aiSuggestion1" to "Яке тренування ви рекомендуєте сьогодні?", "aiSuggestion2" to "Як збільшити обсяг?", "aiSuggestion3" to "Чи потрібен мені день відпочинку?", "aiSuggestion4" to "Як вийти з плато?",
         "deleteAccount" to "Видалити акаунт",
        "exerciseHistory" to "Історія вправи",  
        "favorite" to "Обране",  "savedExercises" to "Збережені вправи", "noFavorites" to "Ще немає збережених вправ", "tapStarToSave" to "Натисніть зірку, щоб зберегти вправу", "removeFavorite" to "Видалити з обраних", 
        "addSet" to "Додати підхід", "exerciseNotes" to "Нотатки", 
        "saveNotes" to "Зберегти", 
        "volume" to "Об'єм", "maxWeight" to "Макс вага", "maxReps" to "Макс повтори", "maxSet" to "Макс підхід",
        "today" to "Сьогодні", "thisWeek" to "Цей тиждень", "thisMonth" to "Цей місяць",
        "totalVolumeLabel" to "Загальний об'єм",
        "guest" to "Гість", "loginWithGoogle" to "Увійти через Google", "loginWithFacebook" to "Увійти через Facebook",
        "close" to "Закрити",  "profile" to "Профіль",
        "appTagline" to "Тренуйся. Прогресуй. Повторюй.", "or" to "або", "dark" to "Темна", "light" to "Світла",
        "system" to "Системна",  
        "selectTheme" to "Обрати тему", "settingsAndMore" to "Налаштування та більше",
        "muscleGroups" to "М'язові групи",  "features" to "Функції", "activity" to "Активність", "tools" to "Інструменти", 
        "englishUS" to "Англійська", "romana" to "Румунська", "russkiy" to "Російська", "ukrainska" to "Українська",
        "francais" to "Французька", "deutsch" to "Німецька", "espanol" to "Іспанська",
        "italiano" to "Італійська", "turkce" to "Турецька", "portugues" to "Португальська", "polski" to "Польська",
        "motto1" to "Кожен повтор має значення.", "motto2" to "Сильніший, ніж учора.",
        "motto3" to "Твоє тіло, твої правила.", "motto4" to "Переверши свої межі.",
        "motto5" to "Послідовність перемагає талант.", "motto6" to "Дисципліна — це свобода.",
        "motto7" to "Без коротких шляхів.", "motto8" to "Зароблено, а не отримано.",
        "motto9" to "Не зупиняйся, коли втомився. Зупиняйся, коли закінчив.", "motto10" to "Біль сьогодні стає силою завтра.",
        "motto11" to "Великі речі ніколи не народжуються в зоні комфорту.", "motto12" to "Ніколи не стає легше. Просто ти стаєш сильнішим.",
        "motto13" to "Не потрібно бути екстремальним, просто будь послідовним.", "motto14" to "Твоя єдина межа — це ти сам.",
        "motto15" to "Кожен експерт колись був новачком.", "motto16" to "Тіло досягає того, у що вірить розум.",
        "motto17" to "Відпочинок — частина процесу, а не ворог.", "motto18" to "Маленькі кроки щодня ведуть до великих результатів.",
        "motto19" to "Тренуйся, ніби ти голодний. Залишайся скромним.", "motto20" to "Успіх — це сума маленьких зусиль, повторюваних щодня.",
        "goodMorning" to "Доброго ранку", "goodAfternoon" to "Доброго дня", "goodEvening" to "Доброго вечора",
        "daysConsecutive" to "днів поспіль", "todaysWorkout" to "Тренування сьогодні",
        "todayYouRest" to "Сьогодні відпочинок", "restDayMessage" to "Відпочинок необхідний для відновлення м'язів. Використайте цей час для відновлення та підготовки до наступного тренування.",
        "restDayTip" to "Легка розтяжка або прогулянка допоможуть підтримати кровообіг.",
        "dayLabel" to "День", "ofCycle" to "циклу",
        "howDoYouFeel" to "Як ви почуваєтесь?", "tiredLabel" to "Втомлений", "normalLabel" to "Нормально", "energeticLabel" to "Енергійний",
         "technicalTip" to "Технічна порада",
        "weeklySummary" to "Підсумки тижня", "lastWeekLabel" to "минул. тиждень",
        "goalLabel" to "Порада по цілі", "volumeLabel" to "Об'єм", "topExerciseLabel" to "Топ вправа",
        "nutritionLabel" to "Харчування", "motivationLabel" to "Мотивація",
        "gpsCardioMap" to "Cardio", "startTracking" to "Почати відстеження", 
        "pauseTracking" to "Пауза", "resumeTracking" to "Продовжити",
        "distance" to "Дистанція", "pace" to "Темп", "speed" to "Швидкість", "duration" to "Тривалість",
        "savedRoutes" to "Збережені маршрути", "noSavedRoutes" to "Немає збережених маршрутів",
        "routeName" to "Назва маршруту", "saveRoute" to "Зберегти маршрут", "deleteRoute" to "Видалити маршрут",
        "locationPermissionRequired" to "Потрібен дозвіл на місцезнаходження",
        "restDaysTitle" to "Дні відпочинку та розвантаження", "restDaysSubtitle" to "Автоматичне планування відновлення, розтяжки, легкої йоги",
        "deloadWeek" to "Тиждень розвантаження", "recoverySchedule" to "Графік відновлення",
        "stretching" to "Розтяжка", "lightYoga" to "Легка йога", "foamRolling" to "Фоамролінг",
         "nextRestDay" to "Наступний день відпочинку",
        "muscleNeedsRest" to "М'язам потрібен відпочинок", 
        "deloadInfo" to "Інформація про розвантаження", "suggestedActivities" to "Рекомендовані активності",
        "activeRecovery" to "Активне відновлення", "lightWalk" to "Легка прогулянка",
        "save" to "Зберегти",
        "allGood" to "Все добре", "alreadyHaveAccount" to "Вже є акаунт?", "autoDeloadEnabled" to "Авторозвантаження увімкнено",
        "avgRecovery" to "Середнє відновлення", "caloriesBurned" to "Спалені калорії", "confirmPassword" to "Підтвердіть пароль",
        "createAccountTitle" to "Створити акаунт", "deloadActive" to "Розвантаження активне", "deloadHistory" to "Історія розвантажень",
        "deloadInterval" to "Інтервал розвантаження", "deloadNewValue" to "Нове значення", "deloadNormalValue" to "Нормальне значення",
        "deloadPreview" to "Попередній перегляд розвантаження", "deloadPreviewSubtitle" to "Перегляньте скорочений план для наступного розвантаження", "deloadActiveThisWeek" to "Розвантаження активне цього тижня",         "recommendedForYou" to "Рекомендовано для вас", "tapToSchedule" to "Натисніть для планування", "dontHaveAccount" to "Немає акаунту?", "emailError" to "Невірний email",
        "endDeload" to "Завершити розвантаження", "foamRollingDescription" to "Зніміть напруження м'язів за допомогою рола",
        "heightCm" to "Зріст (см)", "lissDescription" to "Легке кардіо для активного відновлення",
        "loginInstead" to "Увійти натомість", "musclesTiredCount" to "м'язів втомились",
        "nameError" to "Ім'я обов'язкове", "nameField" to "Ім'я", "optional" to "Необов'язково",
        "passwordError" to "Пароль повинен містити щонайменше 6 символів", "passwordMismatch" to "Паролі не збігаються",
        "passwordStrengthMedium" to "Середній", "passwordStrengthStrong" to "Сильний", "passwordStrengthWeak" to "Слабкий",
        "privacyPolicyLink" to "Політика конфіденційності", "recoveryTargeted" to "Цільове відновлення",
        "startDeload" to "Почати розвантаження", "stretchingDescription" to "Покращіть гнучкість та мобільність", "warmupStretch" to "Розминка", "cooldownStretch" to "Застивання",
        "termsAndConditions" to "Умови використання", "termsPrefix" to "Продовжуючи, ви погоджуєтесь з нашими",
        "timeForDeload" to "Час для розвантаження", "weeks" to "тижнів",
        "deloadReasonAuto" to "Час для розвантаження",
        "deloadReasonGeneral" to "Загальне розвантаження",
        "deloadDueBanner" to "Зменште обсяг цього тижня для повного відновлення",
        "dailyIntake" to "Денне споживання",
        "bio" to "Про себе",
        "currentPassword" to "Поточний пароль",
        "newPassword" to "Новий пароль",
        "confirmNewPassword" to "Підтвердіть новий пароль",
        "changeLabel" to "Змінити",
        "currentPasswordRequired" to "Потрібен поточний пароль",
        "passwordTooShort" to "Новий пароль має містити щонайменше 6 символів",
        "enterPasswordToConfirm" to "Введіть пароль для підтвердження",
        "passwordRequiredToDelete" to "Для видалення акаунта потрібен пароль",
        "lowLabel" to "Низький",
        "highLabel" to "Високий",
        "newChat" to "Новий чат",
        "serverSettings" to "Налаштування сервера",
        "deloadWhyTitle" to "Навіщо розвантаження?",
        "deloadWhyBody" to "Інтенсивні тренування %d тижнів поспіль накопичують втому. Розвантажувальний тиждень знижує обсяг, щоб м'язи повністю відновилися, і ви повернулися сильнішими.",
        "deloadDuration" to "Тривалість",
        "deload1Week" to "1 тиждень",
        "deload2Weeks" to "2 тижні",
        "deloadReduction" to "Зниження",
        "deloadCompound" to "БАЗ",
        "deloadNoHistory" to "Розвантажень ще немає. Після завершення розвантажувального тижня він з'явиться тут із прогресом відновлення.",
        "editRestDay" to "Змінити день відпочинку",
        "deloadDayOf" to "День %d із %d",
        "gpsSearching" to "Пошук GPS-сигналу...",
        "gpsError" to "Помилка GPS",
        "running" to "Біг",
        "cycling" to "Велосипед",
        "walking" to "Ходьба",
        "routePoints" to "Точки",
        "paused" to "Пауза", "estimatedOneRm" to "Розрах. 1ПМ", "nextSetSuggestion" to "Наступний підхід", "setTypeWarmup" to "Розминка", "setTypeWorking" to "Робочий", "setTypeDrop" to "Дроп", "setTypeAmrap" to "AMRAP", "setTypePaused" to "Пауза", "setTypeTempo" to "Темпо", "rpeLabel" to "RPE",
        "steps" to "кроків",
        "timerFinished" to "Час вийшов!",
        "timeToStartNextSet" to "Готові до наступного підходу!",
        "weeksSinceLastDeload" to "Тижнів з останнього розвантаження", "weightKg" to "Вага (кг)",
        "yogaDescription" to "Розслабтесь та покращіть мобільність за допомогою йоги",  
        "gpsDisabledTitle" to "GPS вимкнено",
        "gpsDisabledMessage" to "Увімкніть GPS у налаштуваннях телефону для відстеження маршруту в реальному часі.",
        "openSettings" to "Відкрити налаштування",
         "weightKgLabel" to "Вага (кг)", "repsLabel" to "Повторення",
        "estimated1rm" to "Розрахункове 1ПМ", "intensityZones" to "Зони інтенсивності",
        "zoneStrength" to "Сила (1-2 повт.)", "zoneStrengthHypertrophy" to "Сила-Гіпертрофія (3-5 повт.)",
        "zoneHypertrophy" to "Гіпертрофія (6-8 повт.)", "zoneHypertrophyEndurance" to "Гіпертрофія-Витривалість (10-12 повт.)",
        "zoneEndurance" to "Витривалість (15+ повт.)", 
        "totalVolumeLabel" to "Загальний об'єм", "muscleGroups" to "Групи м'язів", "weeklyTab" to "Тиждень",
         "pbsTab" to "Рекорди",
        "keepTraining" to "Продовжуйте тренуватися щоб побити свої рекорди!",
        "plateCalculatorTitle" to "Калькулятор дисків", "targetWeightLabel" to "Цільова вага",
        "menu" to "Меню",
        "foodNamePlaceholder" to "Напр.: куряче філе", "brandPlaceholder" to "Напр.: Farm Foods", "backendServerAddress" to "Адреса сервера:", "aiApiKeyOptional" to "API-ключ AI-тренера (необов'язково):", "leaveEmptyForDefaultServer" to "Залиште порожнім для URL сервера за замовчуванням. Ключ API потрібен, лише якщо на сервері ввімкнено автентифікацію.", "kcal" to "ККАЛ", "deleteAccountWarning" to "Ця дія незворотна. Усі ваші дані буде видалено.", "setStepGoal" to "Встановити ціль кроків", "enterDailyStepGoal" to "Введіть вашу щоденну ціль кроків",
        "history" to "Історія", "deleteChat" to "Видалити чат", "bodyAnatomy" to "Анатомія тіла", "leaveEmptyIfAuthDisabled" to "Залиште порожнім, якщо автентифікацію вимкнено", "floatingWindow" to "Плаваюче вікно", "float" to "Плаваюче", "clear" to "Очистити", "decrease" to "Зменшити", "increase" to "Збільшити", "exercise" to "Вправа", "top" to "ТОП", "play" to "Відтворити", "reset" to "Скинути", "selected" to "Вибрано", "loadingPlaylists" to "Завантаження плейлистів...", "failedToLoadPlaylists" to "Не вдалося завантажити плейлисти", "noPlaylistsFound" to "Плейлисти не знайдено", "createPlaylistFirst" to "Спочатку створіть плейлист у Spotify", "authenticationFailed" to "Помилка автентифікації", "unknownError" to "Невідома помилка", "selectPlaylist" to "Виберіть плейлист", "choosePlaylist" to "Виберіть плейлист для тренування", "tracks" to "треків", "connectToSpotify" to "Підключитися до Spotify", "spotifyAccessDescription" to "Отримайте доступ до своїх плейлистів і створіть ідеальний саундтрек для тренування", "loginWithSpotify" to "Увійти через Spotify", "spotifyRedirectInfo" to "Вас буде перенаправлено до Spotify для авторизації", "navy" to "Navy", "bmi" to "ІМТ", "exportBackup" to "Експорт резервної копії",
        "barWeightLabel" to "Вага штанги", "lbsKg" to "фунт/кг",
        "platesPerSide" to "Диски на кожну сторону", "plateUnit" to "диск(ів)",
        "eachSide" to "на кожну сторону", "total" to "Загалом",
        "weightTooLight" to "Вага занадто мала для дисків (тільки штанга:",
        "plateCalcNote" to "Калькулятор дисків автоматично обчислює які диски потрібно додати на штангу для досягнення цільової ваги.",
        "howToGet" to "Як отримати:",
        "gender" to "Стать", "age" to "Вік", "method" to "Метод", "waistCm" to "Талія (см)", "neckCm" to "Шия (см)", "hipsCm" to "Стегна (см)", "estimatedBodyFat" to "Розрахований % жиру", "navyMethodInfo" to "Метод ВМС: використовує вимірювання обхватів",  "bodyFatCalculator" to "Калькулятор жиру", "newPRs" to "Нові рекорди", "exerciseBreakdown" to "Деталі вправ", "done" to "Готово", "weightGoal" to "Ціль по вазі", "currentWeight" to "Поточна вага", "target" to "Ціль", "deadline" to "Термін", "goalDetails" to "Деталі цілі", "startWeight" to "Початкова вага", "targetWeight" to "Цільова вага",  "noActiveGoal" to "Немає активної цілі", "setGoalToTrack" to "Встановіть ціль для відстеження прогресу",         "setGoal" to "Встановити ціль", "pastGoals" to "Попередні цілі", "calculate" to "Розрахувати",
        "weightEvolution" to "Динаміка ваги", "measurements" to "вимірювань", "startedOn" to "Розпочато", "editGoal" to "Змінити ціль",
        "equipDumbbells" to "Гантелі", "equipBarbell" to "Штанга", "equipMachine" to "Тренажер", "equipCable" to "Блок", "equipBodyweight" to "Вага тіла", "equipEZBar" to "EZ штанга", "equipSmithMachine" to "Сміт", "equipKettlebell" to "Гіра", "equipStabilityBall" to "Стабільний м'яч", "equipSledMachine" to "Сани", "equipBand" to "Стрічка",
        "energizeLabel" to "Енергія",
        "performLabel" to "Продуктивність",
        "pushItLabel" to "Давай",
        "openSpotifyLabel" to "Відкрити Spotify",
        "tapToPlayLabel" to "Натисніть для відтворення в Spotify",
        "startingWorkoutLabel" to "Запускаємо музику для тренування...",
        "signUpSuccessMessage" to "Обліковий запис створено! Будь ласка, увійдіть.",
        "trainingSectionLabel" to "Тренування", "frequencyLabel" to "Частота", "xPerWeek" to "x / тижд", "editProfile" to "Редагувати профіль", "memberSince" to "Учасник з", "changePassword" to "Змінити пароль",
        "updateTitle" to "Доступна нова версія", "updateMessage" to "Kinetic %s випущена. У вас встановлена версія v%s.\n\nНатисніть, щоб завантажити новий APK.", "updateDownload" to "Завантажити", "updateLater" to "Пізніше",
        "activeDesc" to "Тренування + щоденна активність",
        "permanentPlan" to "Довічний план",
        "vsPrevious" to "порівняно з минулим",
        "free" to "Безкоштовно",
        "lifetimeAccess" to "Довічний доступ",
        "male" to "Чоловік",
        "purchaseFailed" to "Помилка покупки",
        "perMonth" to "/міс",
        "veryActiveDesc" to "Інтенсивні тренування + фізична робота",
        "restorePurchase" to "Відновити покупки",
        "freePlan" to "Безкоштовний",
        "veryActive" to "Дуже активний",
        "whatsYourAge" to "Скільки вам років?",
        "sedentary" to "Сидячий",
        "unlockedForMinutes" to "Розблоковано: залишилось %s",
        "watchAdToUnlock" to "Переглянути рекламу (розблокувати на 30 хв)",
        "remaining" to "залишилось",
        "whatsYourGender" to "Ваша стать?",
        "restoreSuccess" to "Покупки відновлено",
        "buyNow" to "Купити",
        "bestValue" to "Найкраща ціна",
        "noPurchasesToRestore" to "Немає покупок для відновлення",
        "sessions" to "тренування",
        "active" to "Активний",
        "allExercises" to "Усі вправи",
        "saveExercise" to "Зберегти вправу",
        "perYear" to "/рік",
        "mostPopular" to "Найпопулярніший",
        "unlockPremiumSubtitle" to "Отримайте доступ до всіх розширених функцій",
        "female" to "Жінка",
        "purchaseSuccess" to "Покупку успішно завершено! Ласкаво просимо до Premium.",
        "upgradeToUnlock" to "Оновіть тариф, щоб розблокувати",
        "dailyAdLimitReached" to "Ви досягли денного ліміту розблокувань",
        "sedentaryDesc" to "Офісна робота, мало руху",
        "adUnlockSuccess" to "Функцію розблоковано на 30 хвилин!",
        "cancelAnytime" to "Скасування будь-коли в Google Play",
        "purchaseCancelled" to "Покупку скасовано",
        "currentPlan" to "Поточний план",
        "adNotReady" to "Реклама не готова. Спробуйте ще раз.",
        "whatsYourActivityLevel" to "Який ваш рівень активності?",
        "workoutAnalytics" to "Аналітика тренувань",
        "mostTrained" to "Найбільш тренована",
        "unlockPremiumTitle" to "Відкрийте Kinetic Premium",
        "oneTimePayment" to "разовий платіж",
        "goalComplete" to "Мету досягнуто!", "waterStreak" to "Серія гідратації", "ofGoal" to "від мети", "editWaterGoal" to "Змінити мету води", "newWaterGoal" to "Нова мета (мл)",
        "undo" to "Скасувати",
        "workoutReminderTitle" to "Тренування сьогодні",
        "workoutReminderBody" to "Час будувати силу. Сконцентруйтесь на __GROUPS__ сьогодні. Давайте максимум на кожному підході та побийте свої особисті рекорди.",
        "workoutReminderText" to "Сьогодні день тренування! Підготуйтеся!",
        "workoutChannelName" to "Нагадування про тренування",
        "weeklySummaryTitle" to "Підсумки тижня",
        "weeklySummaryText" to "Ви тренувалися __COUNT__ разів цього тижня! Продовжуйте!",
        "weeklySummaryChannelName" to "Щотижневий звіт",
        "streakReminderTitle" to "Не переривайте серію!",
        "streakReminderText" to "Тренуйтеся сьогодні, щоб зберегти серію з __STREAK__ днів!",
        "streakChannelName" to "Нагадування про серію",
        "goalProgressTitle" to "Прогрес кроків",
        "goalProgressText" to "Ви досягли __PERCENT__% мети по кроках! (__CURRENT__/__GOAL__)",
        "goalProgressChannelName" to "Прогрес мети",
        "achievementTitle" to "Досягнення відкрито!",
        "achievementText" to "Вітаємо! Ви відкрили новий значок!",
        "achievementChannelName" to "Досягнення"
    ))

    private fun createFr() = Strings(enRaw() + mapOf(
         "stop" to "Arrêter", "openApp" to "Ouvrir l'app", "goal" to "Objectif", "stepGoalChannel" to "Objectif de pas", "stepGoalTitle" to "🏆 Objectif de pas atteint !", "stepGoalText" to "Félicitations ! Vous avez atteint %d pas !", "stepGoalBig" to "Félicitations ! Vous avez atteint votre objectif de %d pas !", "stepGoalKeepGoing" to "Continuez comme ça !", "gpsChannelName" to "Suivi GPS", "waterChannelName" to "Rappels d'eau", "biometricChannelName" to "Rappels de mensurations", "friendChannelName" to "Demandes d'amis", "oneRmCalculator" to "Calculateur 1RM", "plusGoal" to "+ Objectif", "tierFree" to "GRATUIT", "tierPro" to "PRO", "tierProPlus" to "PRO+", "tierLifetime" to "À VIE",
         "dashboard" to "Tableau de bord",  "acasa" to "Accueil", "workouts" to "Entraînements", "stats" to "Stats", "waterIntake" to "Consommation d'eau", "waterGoal" to "Objectif eau", "addWater" to "Ajouter de l'eau",  "height" to "Taille", "personalInfo" to "Informations personnelles",  "ml" to "ml", "templates" to "Modèles",
              "everyDay" to "Chaque jour", "reminder" to "Rappel",
            "weeklyHistory" to "Historique hebdomadaire", "tips" to "Conseils", "customMl" to "ml personnalisé", "average" to "Moyenne", "target" to "Objectif",
            "waterTip1" to "Buvez 250ml toutes les 30 min pendant l'entraînement.", "waterTip2" to "Matin : 500ml au réveil pour un métabolisme actif.", 
        "recovery" to "Récupération",   "friends" to "Amis",
        "leaderboard" to "Classement", "all" to "Tous",  "language" to "Langue",
        "units" to "Unités", "logout" to "Déconnexion", "login" to "Connexion", "signUp" to "S'inscrire",
        "email" to "Email", "password" to "Mot de passe", "forgotPassword" to "Mot de passe oublié?",
         "loginAsGuest" to "Se connecter en tant qu'invité",
        "goalStrength" to "Force", "goalMass" to "Masse musculaire", "goalWeightLoss" to "Perte de poids",
        "goalMaintenance" to "Maintien", "selectGoal" to "Sélectionnez votre objectif",
        "stepOf" to "Étape %d sur 5", "whatsYourExperience" to "Quel est votre niveau d'expérience?",
        "beginnerLabel" to "Débutant", "beginnerDesc" to "0-1 an d'entraînement",
        "intermediateLabel" to "Intermédiaire", "intermediateDesc" to "1-3 ans d'entraînement régulier",
        "advancedLabel" to "Avancé", "advancedDesc" to "3+ ans d'entraînement sérieux",
        "whatEquipment" to "Quel équipement avez-vous?",
        "homeNoEquip" to "Maison - Sans équipement", "homeNoEquipDesc" to "Exercices avec poids du corps uniquement",
        "homeDumbbells" to "Maison - Haltères/Bandes", "homeDumbbellsDesc" to "Équipement de base à domicile",
        "fullGym" to "Salle complète", "fullGymDesc" to "Accès complet à la salle",
        "profileGoalLabel" to "Objectif", "profileExperienceLabel" to "Expérience", "profileEquipmentLabel" to "Équipement",
        "trainingFrequency" to "Fréquence d'entraînement", "sessionsPerWeek" to "Séances par semaine",
        "selectTrainingDays" to "Sélectionnez vos jours d'entraînement",
        "monday" to "Lundi", "tuesday" to "Mardi", "wednesday" to "Mercredi", "thursday" to "Jeudi",
        "friday" to "Vendredi", "saturday" to "Samedi", "sunday" to "Dimanche",
        "physicalLimitations" to "Des limitations physiques ou blessures?",
        "physicalLimitationsPlaceholder" to "ex. douleur au genou, problèmes de dos (ou laisser vide)",
        "whichMuscleGroups" to "Quels groupes musculaires?", "selectAtLeastOne" to "Étape 7 sur 7 - sélectionnez au moins un",
        "next" to "Suivant", "skip" to "Passer", "finish" to "Terminer", "back" to "Retour",
        "profileSetup" to "Configuration du profil", "enterName" to "Entrez votre nom",
        "pickPhoto" to "Choisir une photo", "saveProfile" to "Sauvegarder le profil",
        "chest" to "Pectoraux", "shoulders" to "Épaules",             "arms" to "Bras", "biceps" to "Biceps", "triceps" to "Triceps",
        "legs" to "Jambes", "thighs" to "Cuisses", "glutes" to "Fessiers", "calves" to "Mollets",
        "core" to "Abdominaux", "cardio" to "Cardio", "fullBody" to "Full Body", "pleaseSelectOption" to "Veuillez sélectionner une option", "sets" to "Séries", "reps" to "Répétitions",
        "weight" to "Poids", 
         "startWorkout" to "Commencer", "nextExercise" to "Exercice suivant",
        "notes" to "Notes", "cancel" to "Annuler", "confirm" to "Confirmer", "delete" to "Supprimer",
        "edit" to "Modifier", "search" to "Rechercher", "noDataYet" to "Pas encore de données",
         "sendRequest" to "Envoyer la demande",
        "accept" to "Accepter", "reject" to "Refuser", "removeFriend" to "Supprimer l'ami",
        "noFriends" to "Pas encore d'amis", "searchUsers" to "Rechercher des utilisateurs",
         "searchByNameOrId" to "Rechercher par nom ou ID",
        "incomingRequests" to "Demandes entrantes", 
        "yourFriends" to "Vos amis", 
        "friendRequestSent" to "Demande envoyée",  "feedEmpty" to "Le fil est vide",
        "workoutCompleted" to "Entraînement terminé!", "streakLabel" to "Série actuelle",
        "bestStreak" to "Meilleure série", "badges" to "Badges", 
         "kg" to "kg", "lbs" to "lbs", 
         "xp" to "XP",
         "max" to "Max", "ok" to "OK", "lv" to "NV",
        "exportCsv" to "Exporter CSV", "importCsv" to "Importer CSV",
        "subscription" to "Abonnement", "premium" to "Premium", 
         "subscribe" to "S'abonner", 
          "error" to "Erreur",
        "retry" to "Réessayer", 
         "recoveryInfo" to "Infos de récupération",
        "recommendedRecovery" to "Récupération recommandée",
         "progressChart" to "Graphique de progrès",
         "weightProgression" to "Progression du poids",
        "calendarView" to "Vue calendrier",  
        "allGroups" to "Tous", 
        "friendRequestNotificationTitle" to "Demande d'amitié",
        "friendRequestNotificationText" to "vous a envoyé une demande d'amitié!",
        "selectLanguage" to "Sélectionner la langue", 
          "workoutsLabel" to "Entraînements",
         "currentStreakLabel" to "Série actuelle",
        "bestStreakLabel" to "Meilleure série", 
        "days" to "jours",  
          "add" to "Ajouter", "demoExercise" to "EXERCICE DEMO",  "prAndVolume" to "Records et volume", "start" to "Démarrer",  "noSavedSetsYet" to "Aucune série sauvegardée.", "editSet" to "Modifier la série",  "exercises" to "exercices", "recovered" to "Récupéré", "almostRecovered" to "Presque récupéré", "moderate" to "Modéré", "tired" to "Fatigué", "exhausted" to "Épuisé", "fatigue" to "fatigue", "chooseMuscleGroup" to "Choisir le groupe musculaire",  "noExercisesFound" to "Aucun exercice trouvé", "tryDifferentFilter" to "Essayez un autre filtre ou recherchez par nom", "voiceSearch" to "Recherche vocale",  "voiceSearchError" to "Impossible de reconnaître la voix",
        "monthlyProgress" to "Progrès mensuel", "completeWorkoutsToSee" to "Complétez des entraînements pour voir les progrès", "jan" to "Janv", "feb" to "Févr", "mar" to "Mars", "apr" to "Avr", "may" to "Mai", "jun" to "Juin", "jul" to "Juil", "aug" to "Août", "sep" to "Sept", "oct" to "Oct", "nov" to "Nov", "dec" to "Déc", "monthlyDetails" to "Détails mensuels", "month" to "Mois", "mon" to "Lu", "tue" to "Ma", "wed" to "Me", "thu" to "Je", "fri" to "Ve", "sat" to "Sa", "sun" to "Di", "noWorkouts" to "Aucun entraînement ce jour", 
         "subscribeNow" to "S'abonner", "premiumFeature" to "Fonctionnalité Premium", "subscribersOnly" to "\$feature est disponible uniquement pour les abonnés", "choosePlan" to "Choisir un forfait", "youAreSubscribed" to "Vous êtes abonné!", "muscleRecovery" to "Récupération musculaire",  "waterReminderTitle" to "Il est temps de boire de l'eau!", "waterReminderText" to "Restez hydraté! Il est temps de boire un verre d'eau.",   "selectTime" to "Choisir l'heure", "forearms" to "Avant-bras", "neckAndTraps" to "Cou & Trapèzes", "welcome" to "Bienvenue", "athlete" to "Athlète",
        "biometricTracking" to "Suivi biométrique",  "addMeasurement" to "Ajouter une mesure", "bodyFat" to "Graisse corporelle", "waistCirc" to "Taille", "hipsCirc" to "Hanches", "thighsCirc" to "Cuisses", "chestCirc" to "Poitrine", "armsCirc" to "Bras",  "noMeasurements" to "Aucune mesure encore",    "weeksAgo" to "semaines", "cm" to "cm", "percent" to "%", "deleteMeasurement" to "Supprimer la mesure", "biometricHistory" to "Historique des mesures", "weightChart" to "Graphique du poids", "bodyFatChart" to "Graphique de la graisse", "circumferenceChart" to "Graphique des circonférences",   "biometricReminderTitle" to "C'est l'heure des mesures!", "biometricReminderText" to "N'oubliez pas d'enregistrer vos mesures corporelles hebdomadaires.",  "streakChannelName" to "Rappels de série", "streakReminderTitle" to "Ne brisez pas votre série !", "streakReminderText" to "Entraînez-vous aujourd'hui pour maintenir votre série !",  "welcomeSoundLabel" to "Son d'accueil",
        "foodJournal" to "Journal alimentaire",    "scan" to "Scanner", "scanning" to "Scan en cours...", "scanBarcodeHelp" to "Assurez-vous que Google Play Services est installé et mis à jour", "noFoodEntries" to "Aucune entrée alimentaire", "todaysMacros" to "Macronutriments du jour", "stepsLabel" to "Pas", "activeTimeLabel" to "Temps actif", "caloriesLabel" to "Calories", "proteinLabel" to "Protéines", "carbsLabel" to "Glucides", "fatLabel" to "Lipides", "breakfast" to "Petit-déjeuner", "lunch" to "Déjeuner", "dinner" to "Dîner", "snack" to "Collation", "drinks" to "Boissons", "selectMealType" to "Sélectionner le type de repas",  "foodName" to "Nom de l'aliment", "brandLabel" to "Marque",     "fiber" to "Fibres", "searchFood" to "Rechercher un aliment", "foodSearchHint" to "Ex : œuf, poulet, riz", "quantity" to "Quantité", "gramsShort" to "g", "piecesShort" to "pcs", "addToJournal" to "Ajouter au journal", "manualEntryMode" to "Saisie manuelle", "noFoodFound" to "Aliment introuvable dans la liste", "enterManually" to "Saisir l'aliment manuellement", "per100g" to "pour 100g", "perPiece" to "par pièce",
        "aiTrainer" to "Coach IA", "aiTrainerWelcome" to "Salut! Je suis votre coach IA", "aiTrainerHint" to "Demandez-moi tout sur l'entraînement, la nutrition ou les progrès", "aiTrainerHistory" to "Historique des chats", "noHistoryYet" to "Pas encore d'historique", "current" to "Actuel", "askAiTrainer" to "Demander au coach...", "aiSuggestion1" to "Quel entraînement recommandez-vous?", "aiSuggestion2" to "Comment augmenter le volume?", "aiSuggestion3" to "Ai-je besoin d'un jour de repos?", "aiSuggestion4" to "Comment sortir d'un plateau?",
        "deleteAccount" to "Supprimer le compte", 
        "exerciseHistory" to "Historique",  
        "favorite" to "Favori",  "savedExercises" to "Exercices sauvegardés", "noFavorites" to "Aucun exercice sauvegardé", "tapStarToSave" to "Appuyez sur l'étoile pour sauvegarder un exercice", "removeFavorite" to "Retirer des favoris", 
        "addSet" to "Ajouter set", "exerciseNotes" to "Notes exercice", 
        "saveNotes" to "Enregistrer", 
        "volume" to "Volume", "maxWeight" to "Poids max", "maxReps" to "Reps max", "maxSet" to "Série max",
        "today" to "Aujourd'hui", "thisWeek" to "Cette semaine", "thisMonth" to "Ce mois",
        "totalVolumeLabel" to "Volume total",
        "guest" to "Invité", "loginWithGoogle" to "Se connecter avec Google", "loginWithFacebook" to "Se connecter avec Facebook",
        "close" to "Fermer",  "profile" to "Profil",
        "appTagline" to "Entraîne-toi. Progresse. Répète.", "or" to "ou", "dark" to "Sombre", "light" to "Clair",
        "system" to "Système",  
        "selectTheme" to "Choisir le thème", "settingsAndMore" to "Paramètres et plus",
        "muscleGroups" to "Groupes musculaires",  "features" to "Fonctionnalités", "activity" to "Activité", "tools" to "Outils", 
        "englishUS" to "Anglais", "romana" to "Roumain", "russkiy" to "Russe", "ukrainska" to "Ukrainien",
        "francais" to "Français", "deutsch" to "Allemand", "espanol" to "Espagnol",
        "italiano" to "Italien", "turkce" to "Turc", "portugues" to "Portugais", "polski" to "Polonais",
        "motto1" to "Chaque répétition compte.", "motto2" to "Plus fort qu'hier.",
        "motto3" to "Ton corps, tes règles.", "motto4" to "Repousse tes limites.",
        "motto5" to "La constance bat le talent.", "motto6" to "La discipline, c'est la liberté.",
        "motto7" to "Pas de raccourcis.", "motto8" to "Gagné, pas donné.",
        "motto9" to "Ne t'arrête pas quand tu es fatigué. Arrête-toi quand tu as fini.", "motto10" to "La douleur d'aujourd'hui devient la force de demain.",
        "motto11" to "Les grandes choses ne naissent jamais dans la zone de confort.", "motto12" to "Ce n'est jamais plus facile. C'est toi qui deviens plus fort.",
        "motto13" to "Pas besoin d'être extrême, juste constant.", "motto14" to "Ta seule limite, c'est toi.",
        "motto15" to "Tout expert a été un jour débutant.", "motto16" to "Le corps atteint ce que l'esprit croit.",
        "motto17" to "Le repos fait partie du processus, ce n'est pas l'ennemi.", "motto18" to "Les petits pas de chaque jour mènent à de grands résultats.",
        "motto19" to "Entraîne-toi comme si tu avais faim. Reste humble.", "motto20" to "Le succès est la somme de petits efforts répétés chaque jour.",
        "goodMorning" to "Bonjour", "goodAfternoon" to "Bon après-midi", "goodEvening" to "Bonsoir",
        "daysConsecutive" to "jours consécutifs", "todaysWorkout" to "Entraînement du jour",
        "todayYouRest" to "Aujourd'hui vous vous reposez", "restDayMessage" to "Le repos est essentiel pour la récupération musculaire. Profitez de ce temps pour vous recharger et vous préparer à votre prochain entraînement.",
        "restDayTip" to "Un étirement léger ou une marche peuvent aider à maintenir la circulation sanguine.",
        "dayLabel" to "Jour", "ofCycle" to "du cycle",
        "howDoYouFeel" to "Comment vous sentez-vous?", "tiredLabel" to "Fatigué", "normalLabel" to "Normal", "energeticLabel" to "Énergique",
         "technicalTip" to "Conseil technique",
        "weeklySummary" to "Résumé de la semaine", "lastWeekLabel" to "semaine dernière",
        "goalLabel" to "Conseil objectif", "volumeLabel" to "Volume", "topExerciseLabel" to "Top exercice",
        "nutritionLabel" to "Nutrition", "motivationLabel" to "Motivation",
        "gpsCardioMap" to "Cardio", "startTracking" to "Commencer le suivi", 
        "pauseTracking" to "Pause", "resumeTracking" to "Reprendre",
        "distance" to "Distance", "pace" to "Rythme", "speed" to "Vitesse", "duration" to "Durée",
        "savedRoutes" to "Itinéraires sauvegardés", "noSavedRoutes" to "Aucun itinéraire sauvegardé",
        "routeName" to "Nom de l'itinéraire", "saveRoute" to "Sauvegarder l'itinéraire", "deleteRoute" to "Supprimer l'itinéraire",
        "locationPermissionRequired" to "La permission de localisation est requise",
        "restDaysTitle" to "Jours de repos et décharge", "restDaysSubtitle" to "Planification automatique récupération, étirements, yoga léger",
        "deloadWeek" to "Semaine de décharge", "recoverySchedule" to "Planning de récupération",
        "stretching" to "Étirements", "lightYoga" to "Yoga léger", "foamRolling" to "Rouleau de mousse",
         "nextRestDay" to "Prochain jour de repos",
        "muscleNeedsRest" to "Les muscles ont besoin de repos", 
        "deloadInfo" to "Infos décharge", "suggestedActivities" to "Activités suggérées",
        "activeRecovery" to "Récupération active", "lightWalk" to "Promenade légère",
        "save" to "Sauvegarder",
        "allGood" to "Tout va bien", "alreadyHaveAccount" to "Déjà un compte ?", "autoDeloadEnabled" to "Décharge automatique activée",
        "avgRecovery" to "Récupération moyenne", "caloriesBurned" to "Calories brûlées", "confirmPassword" to "Confirmer le mot de passe",
        "createAccountTitle" to "Créer un compte", "deloadActive" to "Décharge active", "deloadHistory" to "Historique des décharges",
        "deloadInterval" to "Intervalle de décharge", "deloadNewValue" to "Nouvelle valeur", "deloadNormalValue" to "Valeur normale",
        "deloadPreview" to "Aperçu de la décharge", "deloadPreviewSubtitle" to "Voir le plan réduit pour la prochaine décharge", "deloadActiveThisWeek" to "Décharge active cette semaine",         "recommendedForYou" to "Recommandé pour vous", "tapToSchedule" to "Appuyez pour planifier", "dontHaveAccount" to "Pas de compte ?", "emailError" to "Email invalide",
        "endDeload" to "Terminer la décharge", "foamRollingDescription" to "Libérez les tensions musculaires avec le rouleau de mousse",
        "heightCm" to "Taille (cm)", "lissDescription" to "Cardio léger pour récupération active",
        "loginInstead" to "Se connecter à la place", "musclesTiredCount" to "muscles fatigués",
        "nameError" to "Le nom est requis", "nameField" to "Nom", "optional" to "Optionnel",
        "passwordError" to "Le mot de passe doit contenir au moins 6 caractères", "passwordMismatch" to "Les mots de passe ne correspondent pas",
        "passwordStrengthMedium" to "Moyen", "passwordStrengthStrong" to "Fort", "passwordStrengthWeak" to "Faible",
        "privacyPolicyLink" to "Politique de confidentialité", "recoveryTargeted" to "Récupération ciblée",
        "startDeload" to "Commencer la décharge", "stretchingDescription" to "Améliorez la souplesse et la mobilité", "warmupStretch" to "Échauffement", "cooldownStretch" to "Étirement de récupération",
        "termsAndConditions" to "Conditions d'utilisation", "termsPrefix" to "En continuant, vous acceptez nos",
        "timeForDeload" to "C'est l'heure de la décharge", "weeks" to "semaines",
        "deloadReasonAuto" to "C'est l'heure de la décharge",
        "deloadReasonGeneral" to "Décharge générale",
        "deloadDueBanner" to "Réduisez votre volume cette semaine pour bien récupérer",
        "dailyIntake" to "Apport quotidien",
        "bio" to "Bio",
        "currentPassword" to "Mot de passe actuel",
        "newPassword" to "Nouveau mot de passe",
        "confirmNewPassword" to "Confirmer le nouveau mot de passe",
        "changeLabel" to "Changer",
        "currentPasswordRequired" to "Le mot de passe actuel est requis",
        "passwordTooShort" to "Le nouveau mot de passe doit contenir au moins 6 caractères",
        "enterPasswordToConfirm" to "Saisissez votre mot de passe pour confirmer",
        "passwordRequiredToDelete" to "Le mot de passe est requis pour supprimer le compte",
        "lowLabel" to "Faible",
        "highLabel" to "Élevé",
        "newChat" to "Nouveau chat",
        "serverSettings" to "Paramètres du serveur",
        "deloadWhyTitle" to "Pourquoi une décharge ?",
        "deloadWhyBody" to "S'entraîner dur %d semaines d'affilée accumule de la fatigue. Une semaine de décharge réduit le volume pour que vos muscles récupèrent complètement et que vous reveniez plus fort.",
        "deloadDuration" to "Durée",
        "deload1Week" to "1 semaine",
        "deload2Weeks" to "2 semaines",
        "deloadReduction" to "Réduction",
        "deloadCompound" to "CMP",
        "deloadNoHistory" to "Aucune décharge pour l'instant. Une fois votre semaine de décharge terminée, elle apparaîtra ici avec votre progression de récupération.",
        "editRestDay" to "Modifier le jour de repos",
        "deloadDayOf" to "Jour %d sur %d",
        "gpsSearching" to "Recherche du signal GPS...",
        "gpsError" to "Erreur GPS",
        "running" to "Course",
        "cycling" to "Vélo",
        "walking" to "Marche",
        "routePoints" to "Points",
        "paused" to "En pause", "estimatedOneRm" to "1RM estimé", "nextSetSuggestion" to "Prochaine série", "setTypeWarmup" to "Échauffement", "setTypeWorking" to "Travail", "setTypeDrop" to "Drop", "setTypeAmrap" to "AMRAP", "setTypePaused" to "Pause", "setTypeTempo" to "Tempo", "rpeLabel" to "RPE",
        "steps" to "pas",
        "timerFinished" to "Le temps est écoulé !",
        "timeToStartNextSet" to "Prêt pour votre prochaine série !",
        "weeksSinceLastDeload" to "Semaines depuis la dernière décharge", "weightKg" to "Poids (kg)",
        "yogaDescription" to "Détendez-vous et améliorez la mobilité avec le yoga léger",  
        "gpsDisabledTitle" to "GPS désactivé",
        "gpsDisabledMessage" to "Activez le GPS dans les paramètres de votre téléphone pour suivre votre itinéraire en temps réel.",
        "openSettings" to "Ouvrir les paramètres",
         "weightKgLabel" to "Poids (kg)", "repsLabel" to "Répétitions",
        "estimated1rm" to "1RM estimé", "intensityZones" to "Zones d'intensité",
        "zoneStrength" to "Force (1-2 rép.)", "zoneStrengthHypertrophy" to "Force-Hypertrophie (3-5 rép.)",
        "zoneHypertrophy" to "Hypertrophie (6-8 rép.)", "zoneHypertrophyEndurance" to "Hypertrophie-Endurance (10-12 rép.)",
        "zoneEndurance" to "Endurance (15+ rép.)", 
        "totalVolumeLabel" to "Volume total", "muscleGroups" to "Groupes musculaires", "weeklyTab" to "Hebdomadaire",
         "pbsTab" to "Records",
        "keepTraining" to "Continuez à vous entraîner pour battre vos records!",
        "plateCalculatorTitle" to "Calculateur de plaques", "targetWeightLabel" to "Poids cible",
        "menu" to "Menu",
        "foodNamePlaceholder" to "Ex : blanc de poulet", "brandPlaceholder" to "Ex : Farm Foods", "backendServerAddress" to "Adresse du serveur backend :", "aiApiKeyOptional" to "Clé API AI Trainer (optionnelle) :", "leaveEmptyForDefaultServer" to "Laissez vide pour l'URL du serveur par défaut. La clé API n'est nécessaire que si le serveur a l'authentification activée.", "kcal" to "KCAL", "deleteAccountWarning" to "Cette action est définitive et irréversible. Toutes vos données seront supprimées.", "setStepGoal" to "Définir l'objectif de pas", "enterDailyStepGoal" to "Entrez votre objectif de pas quotidien",
        "history" to "Historique", "deleteChat" to "Supprimer le chat", "bodyAnatomy" to "Anatomie du corps", "leaveEmptyIfAuthDisabled" to "Laisser vide si l'authentification est désactivée", "floatingWindow" to "Fenêtre flottante", "float" to "Flottant", "clear" to "Effacer", "decrease" to "Diminuer", "increase" to "Augmenter", "exercise" to "Exercice", "top" to "TOP", "play" to "Lecture", "reset" to "Réinitialiser", "selected" to "Sélectionné", "loadingPlaylists" to "Chargement des playlists...", "failedToLoadPlaylists" to "Échec du chargement des playlists", "noPlaylistsFound" to "Aucune playlist trouvée", "createPlaylistFirst" to "Créez d'abord une playlist sur Spotify", "authenticationFailed" to "Échec de l'authentification", "unknownError" to "Erreur inconnue", "selectPlaylist" to "Sélectionner la playlist", "choosePlaylist" to "Choisissez une playlist pour votre entraînement", "tracks" to "pistes", "connectToSpotify" to "Se connecter à Spotify", "spotifyAccessDescription" to "Accédez à vos playlists et créez la bande-son parfaite pour votre entraînement", "loginWithSpotify" to "Se connecter avec Spotify", "spotifyRedirectInfo" to "Vous serez redirigé vers Spotify pour autoriser", "navy" to "Navy", "bmi" to "IMC", "exportBackup" to "Exporter la sauvegarde",
        "barWeightLabel" to "Poids du barre", "lbsKg" to "lbs/kg",
        "platesPerSide" to "Plaques par côté", "plateUnit" to "plaque(s)",
        "eachSide" to "par côté", "total" to "Total",
        "weightTooLight" to "Poids trop léger pour les plaques (seulement la barre:",
        "plateCalcNote" to "Le calculateur de plaques calcule automatiquement quelles plaques ajouter à la barre pour atteindre le poids cible.",
        "howToGet" to "Comment obtenir :",
        "gender" to "Genre", "age" to "Âge", "method" to "Méthode", "waistCm" to "Taille (cm)", "neckCm" to "Cou (cm)", "hipsCm" to "Hanches (cm)", "estimatedBodyFat" to "Masse grasse estimée", "navyMethodInfo" to "Méthode Navy: utilise un mètre ruban pour les circonférences",  "bodyFatCalculator" to "Calculateur de masse grasse", "newPRs" to "Nouveaux Records", "exerciseBreakdown" to "Détails des exercices", "done" to "Terminé", "weightGoal" to "Objectif de poids", "currentWeight" to "Poids actuel", "target" to "Objectif", "deadline" to "Échéance", "goalDetails" to "Détails de l'objectif", "startWeight" to "Poids de départ", "targetWeight" to "Poids cible",  "noActiveGoal" to "Aucun objectif actif", "setGoalToTrack" to "Définissez un objectif pour suivre vos progrès",         "setGoal" to "Définir l'objectif", "pastGoals" to "Objectifs passés", "calculate" to "Calculer",
        "weightEvolution" to "Évolution du poids", "measurements" to "mesures", "startedOn" to "Commencé le", "editGoal" to "Modifier l'objectif",
        "equipDumbbells" to "Haltères", "equipBarbell" to "Barre", "equipMachine" to "Machine", "equipCable" to "Câble", "equipBodyweight" to "Poids du corps", "equipEZBar" to "Barre EZ", "equipSmithMachine" to "Smith Machine", "equipKettlebell" to "Kettlebell", "equipStabilityBall" to "Ballon stabilité", "equipSledMachine" to "Traîneau", "equipBand" to "Bande",
        "energizeLabel" to "Énergie",
        "performLabel" to "Performance",
        "pushItLabel" to "Pousse",
        "openSpotifyLabel" to "Ouvrir Spotify",
        "tapToPlayLabel" to "Appuyez pour écouter sur Spotify",
        "startingWorkoutLabel" to "Lancement de la musique d'entraînement...",
        "signUpSuccessMessage" to "Compte créé ! Veuillez vous connecter.",
        "trainingSectionLabel" to "Entraînement", "frequencyLabel" to "Fréquence", "xPerWeek" to "x / sem", "editProfile" to "Modifier le profil", "memberSince" to "Membre depuis", "changePassword" to "Changer le mot de passe",
        "updateTitle" to "Nouvelle version disponible", "updateMessage" to "Kinetic %s a été publiée. Vous avez installé la version v%s.\n\nAppuyez pour télécharger le nouvel APK.", "updateDownload" to "Télécharger", "updateLater" to "Plus tard",
        "activeDesc" to "Entraînement + activité quotidienne",
        "permanentPlan" to "Formule à vie",
        "vsPrevious" to "vs précédent",
        "free" to "Gratuit",
        "lifetimeAccess" to "Accès à vie",
        "male" to "Homme",
        "purchaseFailed" to "Échec de l'achat",
        "perMonth" to "/mois",
        "veryActiveDesc" to "Entraînement intensif + travail physique",
        "restorePurchase" to "Restaurer les achats",
        "freePlan" to "Gratuit",
        "veryActive" to "Très actif",
        "whatsYourAge" to "Quel âge avez-vous ?",
        "sedentary" to "Sédentaire",
        "unlockedForMinutes" to "Débloqué : %s restant",
        "watchAdToUnlock" to "Regarder une pub (débloquer 30 min)",
        "remaining" to "restant",
        "whatsYourGender" to "Quel est votre genre ?",
        "restoreSuccess" to "Achats restaurés",
        "buyNow" to "Acheter",
        "bestValue" to "Meilleur rapport qualité/prix",
        "noPurchasesToRestore" to "Aucun achat à restaurer",
        "sessions" to "séances",
        "active" to "Actif",
        "allExercises" to "Tous les exercices",
        "saveExercise" to "Enregistrer l'exercice",
        "perYear" to "/an",
        "mostPopular" to "Le plus populaire",
        "unlockPremiumSubtitle" to "Accédez à toutes les fonctionnalités avancées",
        "female" to "Femme",
        "purchaseSuccess" to "Achat réussi ! Bienvenue sur Premium.",
        "upgradeToUnlock" to "Passez au supérieur pour débloquer",
        "dailyAdLimitReached" to "Vous avez atteint la limite quotidienne de déblocages",
        "sedentaryDesc" to "Travail de bureau, peu de mouvement",
        "adUnlockSuccess" to "Fonction débloquée pour 30 minutes !",
        "cancelAnytime" to "Annulable à tout moment dans Google Play",
        "purchaseCancelled" to "Achat annulé",
        "currentPlan" to "Formule actuelle",
        "adNotReady" to "Publicité pas prête. Réessayez.",
        "whatsYourActivityLevel" to "Quel est votre niveau d'activité ?",
        "workoutAnalytics" to "Analyse d'entraînement",
        "mostTrained" to "Le plus pratiqué",
        "unlockPremiumTitle" to "Débloquez Kinetic Premium",
        "oneTimePayment" to "paiement unique",
        "goalComplete" to "Objectif atteint!", "waterStreak" to "Série d'hydratation", "ofGoal" to "de l'objectif", "editWaterGoal" to "Modifier l'objectif eau", "newWaterGoal" to "Nouvel objectif (ml)",
        "undo" to "Annuler",
        "workoutReminderTitle" to "Entraînement du jour",
        "workoutReminderBody" to "C'est l'heure de construire la force. Concentrez-vous sur __GROUPS__ aujourd'hui. Donnez le maximum à chaque série et battez vos records personnels.",
        "workoutReminderText" to "C'est jour d'entraînement ! Préparez-vous !",
        "workoutChannelName" to "Rappels d'entraînement",
        "weeklySummaryTitle" to "Résumé de la semaine",
        "weeklySummaryText" to "Vous vous êtes entraîné __COUNT__ fois cette semaine ! Continuez !",
        "weeklySummaryChannelName" to "Résumé hebdomadaire",
        "streakReminderTitle" to "Ne rompez pas votre série !",
        "streakReminderText" to "Entraînez-vous aujourd'hui pour maintenir votre série de __STREAK__ jours !",
        "streakChannelName" to "Rappels de série",
        "goalProgressTitle" to "Progrès des pas",
        "goalProgressText" to "Vous avez atteint __PERCENT__% de votre objectif de pas ! (__CURRENT__/__GOAL__)",
        "goalProgressChannelName" to "Progrès de l'objectif",
        "achievementTitle" to "Succès débloqué !",
        "achievementText" to "Félicitations ! Vous avez débloqué un nouveau badge !",
        "achievementChannelName" to "Succès"
    ))

    private fun createDe() = Strings(enRaw() + mapOf(
         "stop" to "Stopp", "openApp" to "App öffnen", "goal" to "Ziel", "stepGoalChannel" to "Schrittziel", "stepGoalTitle" to "🏆 Schrittziel erreicht!", "stepGoalText" to "Glückwunsch! Sie haben %d Schritte geschafft!", "stepGoalBig" to "Glückwunsch! Sie haben Ihr Ziel von %d Schritten erreicht!", "stepGoalKeepGoing" to "Weiter so!", "gpsChannelName" to "GPS-Tracking", "waterChannelName" to "Wasser-Erinnerungen", "biometricChannelName" to "Messerinnerungen", "friendChannelName" to "Freundschaftsanfragen", "oneRmCalculator" to "1RM-Rechner", "plusGoal" to "+ Ziel", "tierFree" to "KOSTENLOS", "tierPro" to "PRO", "tierProPlus" to "PRO+", "tierLifetime" to "LEBENSLANG",
         "dashboard" to "Dashboard",  "acasa" to "Startseite", "workouts" to "Trainings", "stats" to "Statistiken", "waterIntake" to "Wasseraufnahme", "waterGoal" to "Wasserziel", "addWater" to "Wasser hinzufügen",  "height" to "Größe", "personalInfo" to "Persönliche Infos",  "ml" to "ml", "templates" to "Vorlagen",
              "everyDay" to "Jeden Tag", "reminder" to "Erinnerung",
            "weeklyHistory" to "Wöchentliche Historie", "tips" to "Tipps", "customMl" to "Eigene ml", "average" to "Durchschnitt", "target" to "Ziel",
            "waterTip1" to "Trinken Sie 250ml alle 30 min während des Trainings.", "waterTip2" to "Morgen: 500ml beim Aufwachen für aktiven Stoffwechsel.", 
        "recovery" to "Erholung",   "friends" to "Freunde",
        "leaderboard" to "Bestenliste", "all" to "Alle",  "language" to "Sprache",
        "units" to "Einheiten", "logout" to "Abmelden", "login" to "Anmelden", "signUp" to "Registrieren",
        "email" to "E-Mail", "password" to "Passwort", "forgotPassword" to "Passwort vergessen?",
         "loginAsGuest" to "Als Gast anmelden",
        "goalStrength" to "Kraft", "goalMass" to "Muskelmasse", "goalWeightLoss" to "Gewichtsverlust",
        "goalMaintenance" to "Erhaltung", "selectGoal" to "Ziel auswählen",
        "stepOf" to "Schritt %d von 5", "whatsYourExperience" to "Wie ist dein Erfahrungsniveau?",
        "beginnerLabel" to "Anfänger", "beginnerDesc" to "0-1 Jahre Training",
        "intermediateLabel" to "Fortgeschritten", "intermediateDesc" to "1-3 Jahre konsequentes Training",
        "advancedLabel" to "Fortgeschritten", "advancedDesc" to "3+ Jahre ernsthaftes Training",
        "whatEquipment" to "Welches Equipment hast du?",
        "homeNoEquip" to "Zu Hause - Kein Equipment", "homeNoEquipDesc" to "Nur Körpergewichtübungen",
        "homeDumbbells" to "Zu Hause - Kurzhanteln/Bänder", "homeDumbbellsDesc" to "Basis-Ausrüstung für zu Hause",
        "fullGym" to "Volles Fitnessstudio", "fullGymDesc" to "Vollständiger Zugang zum Studio",
        "profileGoalLabel" to "Ziel", "profileExperienceLabel" to "Erfahrung", "profileEquipmentLabel" to "Ausrüstung",
        "trainingFrequency" to "Trainingsfrequenz", "sessionsPerWeek" to "Einheiten pro Woche",
        "selectTrainingDays" to "Wähle deine Trainingstage",
        "monday" to "Montag", "tuesday" to "Dienstag", "wednesday" to "Mittwoch", "thursday" to "Donnerstag",
        "friday" to "Freitag", "saturday" to "Samstag", "sunday" to "Sonntag",
        "physicalLimitations" to "Körperliche Einschränkungen oder Verletzungen?",
        "physicalLimitationsPlaceholder" to "z.B. Knieschmerzen, Rückenprobleme (oder leer lassen)",
        "whichMuscleGroups" to "Welche Muskelgruppen?", "selectAtLeastOne" to "Schritt 7 von 7 - mindestens eine auswählen",
        "next" to "Weiter", "skip" to "Überspringen", "finish" to "Fertig",
        "skip" to "Überspringen", "finish" to "Fertig", "back" to "Zurück",
        "profileSetup" to "Profil einrichten", "enterName" to "Name eingeben",
        "pickPhoto" to "Foto auswählen", "saveProfile" to "Profil speichern", "chest" to "Brust",
        "shoulders" to "Schultern",             "arms" to "Arme", "biceps" to "Bizeps", "triceps" to "Trizeps",
        "legs" to "Beine", "thighs" to "Oberschenkel", "glutes" to "Gesäß", "calves" to "Waden",
        "core" to "Rumpf",
        "cardio" to "Cardio", "fullBody" to "Ganzkörper", "pleaseSelectOption" to "Bitte wählen Sie eine Option", "sets" to "Sätze", "reps" to "Wiederholungen", "weight" to "Gewicht",
        "startWorkout" to "Training starten", "nextExercise" to "Nächste Übung", "notes" to "Notizen", "cancel" to "Abbrechen",
        "confirm" to "Bestätigen", "delete" to "Löschen", "edit" to "Bearbeiten", "search" to "Suchen",
        "noDataYet" to "Noch keine Daten", 
        "sendRequest" to "Anfrage senden", "accept" to "Akzeptieren", "reject" to "Ablehnen",
        "removeFriend" to "Freund entfernen", "noFriends" to "Noch keine Freunde",
        "searchUsers" to "Benutzer suchen", 
        "searchByNameOrId" to "Nach Name oder ID suchen", "incomingRequests" to "Eingehende Anfragen",
         "yourFriends" to "Deine Freunde",
         "friendRequestSent" to "Anfrage gesendet",
         "feedEmpty" to "Feed ist leer", 
          "workoutCompleted" to "Training abgeschlossen!",
        "streakLabel" to "Aktuelle Serie", "bestStreak" to "Beste Serie", "badges" to "Abzeichen",
          "kg" to "kg", "lbs" to "lbs",
          "xp" to "XP",
          "max" to "Max", "ok" to "OK", "lv" to "LV",
         "exportCsv" to "CSV exportieren",
        "importCsv" to "CSV importieren", "subscription" to "Abonnement", "premium" to "Premium",
        "subscribe" to "Abonnieren",  
         "error" to "Fehler", "retry" to "Erneut versuchen",
         "recoveryInfo" to "Erholungsinformationen",
        "recommendedRecovery" to "Empfohlene Erholung",
        "progressChart" to "Fortschrittsdiagramm", 
        "weightProgression" to "Gewichtsentwicklung", 
         "calendarView" to "Kalenderansicht",
        "allGroups" to "Alle", 
        "friendRequestNotificationTitle" to "Freundschaftsanfrage",
        "friendRequestNotificationText" to "hat dir eine Freundschaftsanfrage geschickt!",
        "selectLanguage" to "Sprache auswählen",  
         "workoutsLabel" to "Trainings", 
        "currentStreakLabel" to "Aktuelle Serie", "bestStreakLabel" to "Beste Serie",
         "days" to "Tage",  
          "add" to "Hinzufügen", "demoExercise" to "DEMO ÜBUNG",  "prAndVolume" to "Bestleistungen und Volumen", "start" to "Starten",  "noSavedSetsYet" to "Noch keine Sätze gespeichert.", "editSet" to "Satz bearbeiten",  "exercises" to "Übungen", "recovered" to "Erholt", "almostRecovered" to "Fast erholt", "moderate" to "Mäßig", "tired" to "Müde", "exhausted" to "Erschöpft", "fatigue" to "Müdigkeit", "chooseMuscleGroup" to "Muskelgruppe wählen",  "noExercisesFound" to "Keine Übungen gefunden", "tryDifferentFilter" to "Versuchen Sie einen anderen Filter oder suchen Sie nach Name", "voiceSearch" to "Sprachsuche",  "voiceSearchError" to "Stimme konnte nicht erkannt werden",
        "monthlyProgress" to "Monatlicher Fortschritt", "completeWorkoutsToSee" to "Schließen Sie Trainings ab um Fortschritt zu sehen", "jan" to "Jan", "feb" to "Feb", "mar" to "Mär", "apr" to "Apr", "may" to "Mai", "jun" to "Jun", "jul" to "Jul", "aug" to "Aug", "sep" to "Sep", "oct" to "Okt", "nov" to "Nov", "dec" to "Dez", "monthlyDetails" to "Monatliche Details", "month" to "Monat", "mon" to "Mo", "tue" to "Di", "wed" to "Mi", "thu" to "Do", "fri" to "Fr", "sat" to "Sa", "sun" to "So", "noWorkouts" to "Kein Training an diesem Tag", 
         "subscribeNow" to "Jetzt abonnieren", "premiumFeature" to "Premium-Funktion", "subscribersOnly" to "\$feature ist nur für Abonnenten verfügbar", "choosePlan" to "Plan wählen", "youAreSubscribed" to "Sie sind abonniert!", "muscleRecovery" to "Muskelerholung",  "waterReminderTitle" to "Zeit, Wasser zu trinken!", "waterReminderText" to "Bleiben Sie hydriert! Es ist Zeit, ein Glas Wasser zu trinken.",   "selectTime" to "Uhrzeit wählen", "forearms" to "Unterarme", "neckAndTraps" to "Hals & Trapezmuskel", "welcome" to "Willkommen", "athlete" to "Athlet",
        "biometricTracking" to "Biometrisches Tracking",  "addMeasurement" to "Messung hinzufügen", "bodyFat" to "Körperfett", "waistCirc" to "Taille", "hipsCirc" to "Hüfte", "thighsCirc" to "Oberschenkel", "chestCirc" to "Brust", "armsCirc" to "Arme",  "noMeasurements" to "Noch keine Messungen",    "weeksAgo" to "Wochen her", "cm" to "cm", "percent" to "%", "deleteMeasurement" to "Messung löschen", "biometricHistory" to "Messungsverlauf", "weightChart" to "Gewichtsdiagramm", "bodyFatChart" to "Körperfettdiagramm", "circumferenceChart" to "Umfangsdiagramm",   "biometricReminderTitle" to "Zeit für Messungen!", "biometricReminderText" to "Vergessen Sie nicht, Ihre wöchentlichen Körpermessungen zu protokollieren.",  "streakChannelName" to "Serien-Erinnerungen", "streakReminderTitle" to "Untbrechen Sie Ihre Serie nicht!", "streakReminderText" to "Trainieren Sie heute, um Ihre Serie aufrechtzuerhalten!",  "welcomeSoundLabel" to "Begrüßungston",
        "foodJournal" to "Ernährungstagebuch",    "scan" to "Scannen", "scanning" to "Scannen...", "scanBarcodeHelp" to "Stellen Sie sicher, dass Google Play Services installiert und aktuell ist", "noFoodEntries" to "Noch keine Einträge", "todaysMacros" to "Heutige Makros", "stepsLabel" to "Schritte", "activeTimeLabel" to "Aktive Zeit", "caloriesLabel" to "Kalorien", "proteinLabel" to "Eiweiß", "carbsLabel" to "Kohlenhydrate", "fatLabel" to "Fett", "breakfast" to "Frühstück", "lunch" to "Mittagessen", "dinner" to "Abendessen", "snack" to "Snack", "drinks" to "Getränke", "selectMealType" to "Mahlzeit auswählen",  "foodName" to "Produktname", "brandLabel" to "Marke",     "fiber" to "Ballaststoffe", "searchFood" to "Lebensmittel suchen", "foodSearchHint" to "z.B. Ei, Huhn, Reis", "quantity" to "Menge", "gramsShort" to "g", "piecesShort" to "Stk", "addToJournal" to "Zum Tagebuch hinzufügen", "manualEntryMode" to "Manuelle Eingabe", "noFoodFound" to "Lebensmittel nicht in der Liste", "enterManually" to "Lebensmittel manuell eingeben", "per100g" to "pro 100g", "perPiece" to "pro Stück",
        "aiTrainer" to "KI Trainer", "aiTrainerWelcome" to "Hallo! Ich bin Ihr KI-Trainer", "aiTrainerHint" to "Fragen Sie mich zu Training, Ernährung oder Fortschritt", "aiTrainerHistory" to "Chat-Verlauf", "noHistoryYet" to "Noch kein Verlauf", "current" to "Aktuell", "askAiTrainer" to "Trainer fragen...", "aiSuggestion1" to "Welches Training empfehlen Sie heute?", "aiSuggestion2" to "Wie kann ich das Volumen steigern?", "aiSuggestion3" to "Brauche ich einen Ruhetag?", "aiSuggestion4" to "Wie komme ich aus dem Plateau?",
         "deleteAccount" to "Konto löschen",
        "exerciseHistory" to "Übung Verlauf",  
        "favorite" to "Favorit",  "savedExercises" to "Gespeicherte Übungen", "noFavorites" to "Noch keine gespeicherten Übungen", "tapStarToSave" to "Tippen Sie auf den Stern, um eine Übung zu speichern", "removeFavorite" to "Aus Favoriten entfernen", 
        "addSet" to "Satz hinzufügen", "exerciseNotes" to "Übung Notizen", 
        "saveNotes" to "Speichern", 
        "volume" to "Volumen", "maxWeight" to "Max Gewicht", "maxReps" to "Max Wdh", "maxSet" to "Max Satz",
        "today" to "Heute", "thisWeek" to "Diese Woche", "thisMonth" to "Diesen Monat",
        "totalVolumeLabel" to "Gesamtvolumen",
        "guest" to "Gast", "loginWithGoogle" to "Mit Google anmelden", "loginWithFacebook" to "Mit Facebook anmelden",
        "close" to "Schließen",  "profile" to "Profil",
        "appTagline" to "Trainiere. Fortschritte. Wiederhole.", "or" to "oder", "dark" to "Dunkel", "light" to "Hell",
        "system" to "System",  
        "selectTheme" to "Thema auswählen", "settingsAndMore" to "Einstellungen & mehr",
        "muscleGroups" to "Muskelgruppen",  "features" to "Funktionen", "activity" to "Aktivität", "tools" to "Werkzeuge", 
        "englishUS" to "Englisch", "romana" to "Rumänisch", "russkiy" to "Russisch", "ukrainska" to "Ukrainisch",
        "francais" to "Französisch", "deutsch" to "Deutsch", "espanol" to "Spanisch",
        "italiano" to "Italienisch", "turkce" to "Türkisch", "portugues" to "Portugiesisch", "polski" to "Polnisch",
        "motto1" to "Jede Wiederholung zählt.", "motto2" to "Stärker als gestern.",
        "motto3" to "Dein Körper, deine Regeln.", "motto4" to "Überwinde deine Grenzen.",
        "motto5" to "Ausdauer schlägt Talent.", "motto6" to "Disziplin ist Freiheit.",
        "motto7" to "Keine Abkürzungen.", "motto8" to "Verdient, nicht bekommen.",
        "motto9" to "Hör nicht auf, wenn du müde bist. Hör auf, wenn du fertig bist.", "motto10" to "Der Schmerz von heute wird die Stärke von morgen.",
        "motto11" to "Große Dinge entstehen nie in der Komfortzone.", "motto12" to "Es wird nie leichter. Du wirst nur stärker.",
        "motto13" to "Du musst nicht extrem sein, nur konsequent.", "motto14" to "Deine einzige Grenze bist du selbst.",
        "motto15" to "Jeder Experte war einmal ein Anfänger.", "motto16" to "Der Körper erreicht, was der Geist glaubt.",
        "motto17" to "Ruhe ist Teil des Prozesses, nicht der Feind.", "motto18" to "Kleine Schritte jeden Tag führen zu großen Ergebnissen.",
        "motto19" to "Trainiere, als wärst du hungrig. Bleib bescheiden.", "motto20" to "Erfolg ist die Summe kleiner Anstrengungen, die täglich wiederholt werden.",
        "goodMorning" to "Guten Morgen", "goodAfternoon" to "Guten Tag", "goodEvening" to "Guten Abend",
        "daysConsecutive" to "Tage in Folge", "todaysWorkout" to "Training heute",
        "todayYouRest" to "Heute ruhst du dich aus", "restDayMessage" to "Erholung ist für die Muskelregeneration unerlässlich. Nutze die Zeit, um dich zu erholen und auf dein nächstes Training vorzubereiten.",
        "restDayTip" to "Leichte Dehnung oder ein Spaziergang helfen, die Durchblutung zu fördern.",
        "dayLabel" to "Tag", "ofCycle" to "des Zyklus",
        "howDoYouFeel" to "Wie fühlst du dich?", "tiredLabel" to "Müde", "normalLabel" to "Normal", "energeticLabel" to "Energisch",
         "technicalTip" to "Technischer Tipp",
        "weeklySummary" to "Wochenübersicht", "lastWeekLabel" to "letzte Woche",
        "goalLabel" to "Zieltipp", "volumeLabel" to "Volumen", "topExerciseLabel" to "Top Übung",
        "nutritionLabel" to "Ernährung", "motivationLabel" to "Motivation",
        "gpsCardioMap" to "Cardio", "startTracking" to "Tracking starten", 
        "pauseTracking" to "Pause", "resumeTracking" to "Fortsetzen",
        "distance" to "Distanz", "pace" to "Tempo", "speed" to "Geschwindigkeit", "duration" to "Dauer",
        "savedRoutes" to "Gespeicherte Routen", "noSavedRoutes" to "Keine gespeicherten Routen",
        "routeName" to "Routenname", "saveRoute" to "Route speichern", "deleteRoute" to "Route löschen",
        "locationPermissionRequired" to "Standortberechtigung erforderlich",
        "restDaysTitle" to "Ruhetage & Entlastung", "restDaysSubtitle" to "Automatische Planung Erholung, Dehnung, leichtes Yoga",
        "deloadWeek" to "Entlastungswoche", "recoverySchedule" to "Erholungsplan",
        "stretching" to "Dehnung", "lightYoga" to "Leichtes Yoga", "foamRolling" to "Faszienrolle",
         "nextRestDay" to "Nächster Ruhetag",
        "muscleNeedsRest" to "Muskeln brauchen Ruhe", 
        "deloadInfo" to "Entlastungsinfo", "suggestedActivities" to "Vorgeschlagene Aktivitäten",
        "activeRecovery" to "Aktive Erholung", "lightWalk" to "Lechter Spaziergang",
        "save" to "Speichern",
        "allGood" to "Alles gut", "alreadyHaveAccount" to "Bereits ein Konto?", "autoDeloadEnabled" to "Automatische Entlastung aktiviert",
        "avgRecovery" to "Durchschnittliche Erholung", "caloriesBurned" to "Verbrannte Kalorien", "confirmPassword" to "Passwort bestätigen",
        "createAccountTitle" to "Konto erstellen", "deloadActive" to "Entlastung aktiv", "deloadHistory" to "Entlastungshistorie",
        "deloadInterval" to "Entlastungsintervall", "deloadNewValue" to "Neuer Wert", "deloadNormalValue" to "Normaler Wert",
        "deloadPreview" to "Entlastungsvorschau", "deloadPreviewSubtitle" to "Reduzierten Plan für die nächste Entlastung ansehen", "deloadActiveThisWeek" to "Entlastung diese Woche aktiv",         "recommendedForYou" to "Für Sie empfohlen", "tapToSchedule" to "Tippen zum Planen", "dontHaveAccount" to "Kein Konto?", "emailError" to "Ungültige E-Mail",
        "endDeload" to "Entlastung beenden", "foamRollingDescription" to "Lösen Sie Muskelverspannungen mit der Faszienrolle",
        "heightCm" to "Größe (cm)", "lissDescription" to "Leichtes Cardio für aktive Erholung",
        "loginInstead" to "Stattdessen anmelden", "musclesTiredCount" to "Muskeln ermüdet",
        "nameError" to "Name ist erforderlich", "nameField" to "Name", "optional" to "Optional",
        "passwordError" to "Passwort muss mindestens 6 Zeichen haben", "passwordMismatch" to "Passwörter stimmen nicht überein",
        "passwordStrengthMedium" to "Mittel", "passwordStrengthStrong" to "Stark", "passwordStrengthWeak" to "Schwach",
        "privacyPolicyLink" to "Datenschutzrichtlinie", "recoveryTargeted" to "Gezielte Erholung",
        "startDeload" to "Entlastung starten", "stretchingDescription" to "Verbessern Sie Flexibilität und Mobilität", "warmupStretch" to "Aufwärmung", "cooldownStretch" to "Cool-down",
        "termsAndConditions" to "Nutzungsbedingungen", "termsPrefix" to "Durch Fortfahren stimmen Sie unseren",
        "timeForDeload" to "Zeit für Entlastung", "weeks" to "Wochen",
        "deloadReasonAuto" to "Zeit für Entlastung",
        "deloadReasonGeneral" to "Allgemeine Entlastung",
        "deloadDueBanner" to "Reduziere diese Woche dein Volumen für volle Erholung",
        "dailyIntake" to "Tägliche Zufuhr",
        "bio" to "Bio",
        "currentPassword" to "Aktuelles Passwort",
        "newPassword" to "Neues Passwort",
        "confirmNewPassword" to "Neues Passwort bestätigen",
        "changeLabel" to "Ändern",
        "currentPasswordRequired" to "Aktuelles Passwort ist erforderlich",
        "passwordTooShort" to "Das neue Passwort muss mindestens 6 Zeichen haben",
        "enterPasswordToConfirm" to "Gib dein Passwort zur Bestätigung ein",
        "passwordRequiredToDelete" to "Passwort erforderlich, um das Konto zu löschen",
        "lowLabel" to "Niedrig",
        "highLabel" to "Hoch",
        "newChat" to "Neuer Chat",
        "serverSettings" to "Servereinstellungen",
        "deloadWhyTitle" to "Warum Entlastung?",
        "deloadWhyBody" to "%d Wochen hartes Training in Folge baut Ermüdung auf. Eine Entlastungswoche senkt das Volumen, damit sich deine Muskeln vollständig erholen und du stärker zurückkommst.",
        "deloadDuration" to "Dauer",
        "deload1Week" to "1 Woche",
        "deload2Weeks" to "2 Wochen",
        "deloadReduction" to "Reduzierung",
        "deloadCompound" to "CMP",
        "deloadNoHistory" to "Noch keine Entlastung. Sobald du eine Entlastungswoche abschließt, erscheint sie hier mit deinem Erholungsfortschritt.",
        "editRestDay" to "Ruhetag bearbeiten",
        "deloadDayOf" to "Tag %d von %d",
        "gpsSearching" to "Suche GPS-Signal...",
        "gpsError" to "GPS-Fehler",
        "running" to "Laufen",
        "cycling" to "Radfahren",
        "walking" to "Gehen",
        "routePoints" to "Punkte",
        "paused" to "Pausiert", "estimatedOneRm" to "Gesch. 1RM", "nextSetSuggestion" to "Nächster Satz", "setTypeWarmup" to "Aufwärmen", "setTypeWorking" to "Arbeit", "setTypeDrop" to "Drop", "setTypeAmrap" to "AMRAP", "setTypePaused" to "Pause", "setTypeTempo" to "Tempo", "rpeLabel" to "RPE",
        "steps" to "Schritte",
        "timerFinished" to "Zeit abgelaufen!",
        "timeToStartNextSet" to "Bereit für den nächsten Satz!",
        "weeksSinceLastDeload" to "Wochen seit letzter Entlastung", "weightKg" to "Gewicht (kg)",
        "yogaDescription" to "Entspannen Sie sich und verbessern Sie die Mobilität mit leichtem Yoga",  
        "gpsDisabledTitle" to "GPS deaktiviert",
        "gpsDisabledMessage" to "Aktivieren Sie GPS in Ihren telefoneinstellungen, um Ihre Route in Echtzeit zu verfolgen.",
        "openSettings" to "Einstellungen öffnen",
         "weightKgLabel" to "Gewicht (kg)", "repsLabel" to "Wiederholungen",
        "estimated1rm" to "Geschätztes 1RM", "intensityZones" to "Intensitätszonen",
        "zoneStrength" to "Kraft (1-2 Wdh.)", "zoneStrengthHypertrophy" to "Kraft-Hypertrophie (3-5 Wdh.)",
        "zoneHypertrophy" to "Hypertrophie (6-8 Wdh.)", "zoneHypertrophyEndurance" to "Hypertrophie-Ausdauer (10-12 Wdh.)",
        "zoneEndurance" to "Ausdauer (15+ Wdh.)", 
        "totalVolumeLabel" to "Gesamtvolumen", "muscleGroups" to "Muskelgruppen", "weeklyTab" to "Wöchentlich",
         "pbsTab" to "Rekorde",
        "keepTraining" to "Trainiere weiter um deine Rekorde zu schlagen!",
        "plateCalculatorTitle" to "Scheibenrechner", "targetWeightLabel" to "Zielgewicht",
        "menu" to "Menü",
        "foodNamePlaceholder" to "z. B. Hähnchenbrust", "brandPlaceholder" to "z. B. Farm Foods", "backendServerAddress" to "Backend-Serveradresse:", "aiApiKeyOptional" to "AI-Trainer-API-Schlüssel (optional):", "leaveEmptyForDefaultServer" to "Für die Standard-Server-URL leer lassen. API-Schlüssel nur nötig, wenn der Server Authentifizierung aktiviert hat.", "kcal" to "KCAL", "deleteAccountWarning" to "Diese Aktion ist endgültig und kann nicht rückgängig gemacht werden. Alle deine Daten werden gelöscht.", "setStepGoal" to "Schrittziel festlegen", "enterDailyStepGoal" to "Gib dein tägliches Schrittziel ein",
        "history" to "Verlauf", "deleteChat" to "Chat löschen", "bodyAnatomy" to "Körperanatomie", "leaveEmptyIfAuthDisabled" to "Leer lassen, wenn die Authentifizierung deaktiviert ist", "floatingWindow" to "Schwebendes Fenster", "float" to "Schweben", "clear" to "Löschen", "decrease" to "Verringern", "increase" to "Erhöhen", "exercise" to "Übung", "top" to "TOP", "play" to "Abspielen", "reset" to "Zurücksetzen", "selected" to "Ausgewählt", "loadingPlaylists" to "Playlists werden geladen...", "failedToLoadPlaylists" to "Playlists konnten nicht geladen werden", "noPlaylistsFound" to "Keine Playlists gefunden", "createPlaylistFirst" to "Erstelle zuerst eine Playlist auf Spotify", "authenticationFailed" to "Authentifizierung fehlgeschlagen", "unknownError" to "Unbekannter Fehler", "selectPlaylist" to "Playlist auswählen", "choosePlaylist" to "Wähle eine Playlist für dein Training", "tracks" to "Titel", "connectToSpotify" to "Mit Spotify verbinden", "spotifyAccessDescription" to "Greife auf deine Playlists zu und erstelle den perfekten Trainings-Soundtrack", "loginWithSpotify" to "Mit Spotify anmelden", "spotifyRedirectInfo" to "Du wirst zur Autorisierung zu Spotify weitergeleitet", "navy" to "Navy", "bmi" to "BMI", "exportBackup" to "Backup exportieren",
        "barWeightLabel" to "Stangengewicht", "lbsKg" to "lbs/kg",
        "platesPerSide" to "Scheiben pro Seite", "plateUnit" to "Scheibe(n)",
        "eachSide" to "pro Seite", "total" to "Gesamt",
        "weightTooLight" to "Gewicht zu leicht für Scheiben (nur Stange:",
        "plateCalcNote" to "Der Scheibenrechner berechnet automatisch welche Scheiben auf die Stange müssen.",
        "howToGet" to "So bekommst du es:",
        "gender" to "Geschlecht", "age" to "Alter", "method" to "Methode", "waistCm" to "Taille (cm)", "neckCm" to "Hals (cm)", "hipsCm" to "Hüfte (cm)", "estimatedBodyFat" to "Geschätzter Körperfettanteil", "navyMethodInfo" to "Navy-Methode: verwendet Maßband für Umfänge",  "bodyFatCalculator" to "Körperfett-Rechner", "newPRs" to "Neue Bestleistungen", "exerciseBreakdown" to "Übungsdetails", "done" to "Fertig", "weightGoal" to "Gewichtsziel", "currentWeight" to "Aktuelles Gewicht", "target" to "Ziel", "deadline" to "Frist", "goalDetails" to "Zieldetails", "startWeight" to "Startgewicht", "targetWeight" to "Zielgewicht",  "noActiveGoal" to "Kein aktives Ziel", "setGoalToTrack" to "Setzen Sie ein Ziel, um Ihren Fortschritt zu verfolgen",         "setGoal" to "Ziel setzen", "pastGoals" to "Frühere Ziele", "calculate" to "Berechnen",
        "weightEvolution" to "Gewichtsentwicklung", "measurements" to "Messungen", "startedOn" to "Gestartet am", "editGoal" to "Ziel bearbeiten",
        "equipDumbbells" to "Kurzhanteln", "equipBarbell" to "Langhantel", "equipMachine" to "Maschine", "equipCable" to "Kabel", "equipBodyweight" to "Eigengewicht", "equipEZBar" to "SZ-Stange", "equipSmithMachine" to "Smith-Maschine", "equipKettlebell" to "Kettlebell", "equipStabilityBall" to "Gymnastikball", "equipSledMachine" to "Schlitten", "equipBand" to "Band",
        "energizeLabel" to "Energie",
        "performLabel" to "Leistung",
        "pushItLabel" to "Schieben",
        "openSpotifyLabel" to "Spotify öffnen",
        "tapToPlayLabel" to "Tippen um auf Spotify abzuspielen",
        "startingWorkoutLabel" to "Trainingsmusik starten...",
        "signUpSuccessMessage" to "Konto erstellt! Bitte einloggen.",
        "trainingSectionLabel" to "Training", "frequencyLabel" to "Frequenz", "xPerWeek" to "x / Wo", "editProfile" to "Profil bearbeiten", "memberSince" to "Mitglied seit", "changePassword" to "Passwort ändern",
        "updateTitle" to "Neue Version verfügbar", "updateMessage" to "Kinetic %s wurde veröffentlicht. Sie haben Version v%s installiert.\n\nTippen Sie, um das neue APK herunterzuladen.", "updateDownload" to "Herunterladen", "updateLater" to "Später",
        "activeDesc" to "Training + tägliche Aktivität",
        "permanentPlan" to "Lebenslanger Plan",
        "vsPrevious" to "vs. vorher",
        "free" to "Kostenlos",
        "lifetimeAccess" to "Lebenslanger Zugriff",
        "male" to "Männlich",
        "purchaseFailed" to "Kauf fehlgeschlagen",
        "perMonth" to "/Monat",
        "veryActiveDesc" to "Intensives Training + körperliche Arbeit",
        "restorePurchase" to "Käufe wiederherstellen",
        "freePlan" to "Kostenlos",
        "veryActive" to "Sehr aktiv",
        "whatsYourAge" to "Wie alt sind Sie?",
        "sedentary" to "Bewegungsarm",
        "unlockedForMinutes" to "Freigeschaltet: %s übrig",
        "watchAdToUnlock" to "Werbung ansehen (30 Min. freischalten)",
        "remaining" to "übrig",
        "whatsYourGender" to "Welches Geschlecht haben Sie?",
        "restoreSuccess" to "Käufe wiederhergestellt",
        "buyNow" to "Kaufen",
        "bestValue" to "Bestes Preis-Leistungs-Verhältnis",
        "noPurchasesToRestore" to "Keine Käufe zum Wiederherstellen",
        "sessions" to "Einheiten",
        "active" to "Aktiv",
        "allExercises" to "Alle Übungen",
        "saveExercise" to "Übung speichern",
        "perYear" to "/Jahr",
        "mostPopular" to "Am beliebtesten",
        "unlockPremiumSubtitle" to "Zugriff auf alle erweiterten Funktionen erhalten",
        "female" to "Weiblich",
        "purchaseSuccess" to "Kauf erfolgreich! Willkommen bei Premium.",
        "upgradeToUnlock" to "Upgrade zum Freischalten",
        "dailyAdLimitReached" to "Tägliches Freischaltlimit erreicht",
        "sedentaryDesc" to "Bürojob, wenig Bewegung",
        "adUnlockSuccess" to "Funktion für 30 Minuten freigeschaltet!",
        "cancelAnytime" to "Jederzeit in Google Play kündbar",
        "purchaseCancelled" to "Kauf abgebrochen",
        "currentPlan" to "Aktueller Plan",
        "adNotReady" to "Werbung nicht bereit. Bitte erneut versuchen.",
        "whatsYourActivityLevel" to "Wie ist Ihr Aktivitätsniveau?",
        "workoutAnalytics" to "Trainingsanalyse",
        "mostTrained" to "Am häufigsten trainiert",
        "unlockPremiumTitle" to "Kinetic Premium freischalten",
        "oneTimePayment" to "einmalige Zahlung",
        "goalComplete" to "Ziel erreicht!", "waterStreak" to "Hydratationsserie", "ofGoal" to "vom Ziel", "editWaterGoal" to "Wasserziel bearbeiten", "newWaterGoal" to "Neues Ziel (ml)",
        "undo" to "Rückgängig",
        "workoutReminderTitle" to "Heutiges Training",
        "workoutReminderBody" to "Zeit, Kraft aufzubauen. Konzentriere dich heute auf __GROUPS__. Gib bei jedem Satz alles und übertrumpfe deine persönlichen Bestleistungen.",
        "workoutReminderText" to "Heute ist Trainingstag! Mach dich bereit!",
        "workoutChannelName" to "Trainings-Erinnerungen",
        "weeklySummaryTitle" to "Wochenübersicht",
        "weeklySummaryText" to "Du hast diese Woche __COUNT__ mal trainiert! Weiter so!",
        "weeklySummaryChannelName" to "Wöchentliche Zusammenfassung",
        "streakReminderTitle" to "Untbrich deine Serie nicht!",
        "streakReminderText" to "Trainiere heute, um deine __STREAK__-tägige Serie aufrechtzuerhalten!",
        "streakChannelName" to "Serien-Erinnerungen",
        "goalProgressTitle" to "Schrittziel-Fortschritt",
        "goalProgressText" to "Du hast __PERCENT__% deines Schrittziels erreicht! (__CURRENT__/__GOAL__)",
        "goalProgressChannelName" to "Zielfortschritt",
        "achievementTitle" to "Erfolg freigeschaltet!",
        "achievementText" to "Herzlichen Glückwunsch! Du hast ein neues Badge freigeschaltet!",
        "achievementChannelName" to "Erfolge"
    ))

    private fun createEs() = Strings(enRaw() + mapOf(
         "stop" to "Detener", "openApp" to "Abrir la app", "goal" to "Objetivo", "stepGoalChannel" to "Objetivo de pasos", "stepGoalTitle" to "🏆 ¡Objetivo de pasos alcanzado!", "stepGoalText" to "¡Felicidades! ¡Has alcanzado %d pasos!", "stepGoalBig" to "¡Felicidades! Has alcanzado tu objetivo de %d pasos!", "stepGoalKeepGoing" to "¡Sigue así!", "gpsChannelName" to "Seguimiento GPS", "waterChannelName" to "Recordatorios de agua", "biometricChannelName" to "Recordatorios de mediciones", "friendChannelName" to "Solicitudes de amistad", "oneRmCalculator" to "Calculadora 1RM", "plusGoal" to "+ Objetivo", "tierFree" to "GRATIS", "tierPro" to "PRO", "tierProPlus" to "PRO+", "tierLifetime" to "DE POR VIDA",
         "dashboard" to "Panel",  "acasa" to "Inicio", "workouts" to "Entrenamientos", "stats" to "Estadísticas", "waterIntake" to "Consumo de agua", "waterGoal" to "Meta de agua", "addWater" to "Agregar agua",  "height" to "Altura", "personalInfo" to "Información personal",  "ml" to "ml", "templates" to "Plantillas",
              "everyDay" to "Cada día", "reminder" to "Recordatorio",
            "weeklyHistory" to "Historial semanal", "tips" to "Consejos", "customMl" to "ml personalizado", "average" to "Promedio", "target" to "Meta",
            "waterTip1" to "Bebe 250ml cada 30 min durante el entrenamiento.", "waterTip2" to "Mañana: 500ml al despertar para metabolismo activo.", 
        "recovery" to "Recuperación",   "friends" to "Amigos",
        "leaderboard" to "Clasificación", "all" to "Todos",  "language" to "Idioma",
        "units" to "Unidades", "logout" to "Cerrar sesión", "login" to "Iniciar sesión",
        "signUp" to "Registrarse", "email" to "Correo electrónico", "password" to "Contraseña",
        "forgotPassword" to "¿Olvidaste la contraseña?", 
        "loginAsGuest" to "Iniciar como invitado", 
         "goalStrength" to "Fuerza", "goalMass" to "Masa muscular",
        "goalWeightLoss" to "Pérdida de peso", "goalMaintenance" to "Mantenimiento",
        "selectGoal" to "Selecciona tu objetivo",
        "stepOf" to "Paso %d de 5", "whatsYourExperience" to "¿Cuál es tu nivel de experiencia?",
        "beginnerLabel" to "Principiante", "beginnerDesc" to "0-1 años de entrenamiento",
        "intermediateLabel" to "Intermedio", "intermediateDesc" to "1-3 años de entrenamiento constante",
        "advancedLabel" to "Avanzado", "advancedDesc" to "3+ años de entrenamiento serio",
        "whatEquipment" to "¿Qué equipamiento tienes?",
        "homeNoEquip" to "Casa - Sin equipamiento", "homeNoEquipDesc" to "Solo ejercicios con peso corporal",
        "homeDumbbells" to "Casa - Mancuernas/Bandas", "homeDumbbellsDesc" to "Equipamiento básico para casa",
        "fullGym" to "Gimnasio completo", "fullGymDesc" to "Acceso completo al gimnasio",
        "profileGoalLabel" to "Objetivo", "profileExperienceLabel" to "Experiencia", "profileEquipmentLabel" to "Equipo",
        "trainingFrequency" to "Frecuencia de entrenamiento", "sessionsPerWeek" to "Sesiones por semana",
        "selectTrainingDays" to "Selecciona tus días de entrenamiento",
        "monday" to "Lunes", "tuesday" to "Martes", "wednesday" to "Miércoles", "thursday" to "Jueves",
        "friday" to "Viernes", "saturday" to "Sábado", "sunday" to "Domingo",
        "physicalLimitations" to "¿Limitaciones físicas o lesiones?",
        "physicalLimitationsPlaceholder" to "ej. dolor de rodilla, problemas de espalda (o dejar vacío)",
        "whichMuscleGroups" to "¿Qué grupos musculares?", "selectAtLeastOne" to "Paso 7 de 7 - selecciona al menos uno",
        "next" to "Siguiente", "skip" to "Omitir",
        "finish" to "Finalizar", "back" to "Volver", "profileSetup" to "Configurar perfil",
        "enterName" to "Ingresa tu nombre", "pickPhoto" to "Elegir foto",
        "saveProfile" to "Guardar perfil", "chest" to "Pecho", "shoulders" to "Hombros",
        "arms" to "Brazos", "biceps" to "Bíceps", "triceps" to "Tríceps",
        "legs" to "Piernas", "thighs" to "Muslos", "glutes" to "Glúteos", "calves" to "Pantorrillas",
        "core" to "Core", "cardio" to "Cardio", "fullBody" to "Full Body",
        "pleaseSelectOption" to "Por favor, selecciona una opción", "sets" to "Series", "reps" to "Repeticiones", "weight" to "Peso",
        "startWorkout" to "Iniciar", "nextExercise" to "Siguiente ejercicio", "notes" to "Notas", "cancel" to "Cancelar",
        "confirm" to "Confirmar", "delete" to "Eliminar", "edit" to "Editar", "search" to "Buscar",
        "noDataYet" to "Aún no hay datos", 
        "sendRequest" to "Enviar solicitud", "accept" to "Aceptar", "reject" to "Rechazar",
        "removeFriend" to "Eliminar amigo", "noFriends" to "Aún no hay amigos",
        "searchUsers" to "Buscar usuarios", 
        "searchByNameOrId" to "Buscar por nombre o ID", "incomingRequests" to "Solicitudes entrantes",
         "yourFriends" to "Tus amigos",
         "friendRequestSent" to "Solicitud enviada",
         "feedEmpty" to "El feed está vacío",
        "workoutCompleted" to "¡Entrenamiento completado!", "streakLabel" to "Racha actual",
        "bestStreak" to "Mejor racha", "badges" to "Insignias", 
         "kg" to "kg", "lbs" to "lbs", 
         "xp" to "XP",
         "max" to "Máx", "ok" to "OK", "lv" to "NV",
        "exportCsv" to "Exportar CSV", "importCsv" to "Importar CSV",
        "subscription" to "Suscripción", "premium" to "Premium", 
         "subscribe" to "Suscribirse", 
          "error" to "Error",
        "retry" to "Reintentar", 
         "recoveryInfo" to "Información de recuperación",
        "recommendedRecovery" to "Recuperación recomendada",
         "progressChart" to "Gráfico de progreso",
        "weightProgression" to "Progresión de peso", 
         "calendarView" to "Vista de calendario",
        "allGroups" to "Todos", 
        "friendRequestNotificationTitle" to "Solicitud de amistad",
        "friendRequestNotificationText" to "¡te envió una solicitud de amistad!",
        "selectLanguage" to "Seleccionar idioma",  
        "workoutsLabel" to "Entrenamientos", 
        "currentStreakLabel" to "Racha actual", "bestStreakLabel" to "Mejor racha",
         "days" to "días",  
          "add" to "Agregar", "demoExercise" to "EJERCICIO DEMO",  "prAndVolume" to "Récords y volumen", "start" to "Iniciar",  "noSavedSetsYet" to "No hay series guardadas.", "editSet" to "Editar serie",  "exercises" to "ejercicios", "recovered" to "Recuperado", "almostRecovered" to "Casi recuperado", "moderate" to "Moderado", "tired" to "Cansado", "exhausted" to "Agotado", "fatigue" to "fatiga", "chooseMuscleGroup" to "Elegir grupo muscular",  "noExercisesFound" to "No se encontraron ejercicios", "tryDifferentFilter" to "Prueba con otro filtro o busca por nombre", "voiceSearch" to "Búsqueda por voz",  "voiceSearchError" to "No se pudo reconocer la voz",
        "monthlyProgress" to "Progreso mensual", "completeWorkoutsToSee" to "Completa entrenamientos para ver el progreso", "jan" to "Ene", "feb" to "Feb", "mar" to "Mar", "apr" to "Abr", "may" to "May", "jun" to "Jun", "jul" to "Jul", "aug" to "Ago", "sep" to "Sep", "oct" to "Oct", "nov" to "Nov", "dec" to "Dic", "monthlyDetails" to "Detalles mensuales", "month" to "Mes", "mon" to "Lu", "tue" to "Ma", "wed" to "Mi", "thu" to "Ju", "fri" to "Vi", "sat" to "Sa", "sun" to "Do", "noWorkouts" to "Sin entrenamiento este día", 
         "subscribeNow" to "Suscribirse", "premiumFeature" to "Función Premium", "subscribersOnly" to "\$feature solo está disponible para suscriptores", "choosePlan" to "Elegir plan", "youAreSubscribed" to "¡Estás suscrito!", "muscleRecovery" to "Recuperación muscular",  "waterReminderTitle" to "¡Es hora de beber agua!", "waterReminderText" to "¡Mantente hidratado! Es hora de beber un vaso de agua.",   "selectTime" to "Seleccionar hora", "forearms" to "Antebrazos", "neckAndTraps" to "Cuello & Trapecios", "welcome" to "Bienvenido", "athlete" to "Atleta",
        "biometricTracking" to "Seguimiento biométrico",  "addMeasurement" to "Añadir medición", "bodyFat" to "Grasa corporal", "waistCirc" to "Cintura", "hipsCirc" to "Caderas", "thighsCirc" to "Muslos", "chestCirc" to "Pecho", "armsCirc" to "Brazos",  "noMeasurements" to "Sin mediciones aún",    "weeksAgo" to "semanas atrás", "cm" to "cm", "percent" to "%", "deleteMeasurement" to "Eliminar medición", "biometricHistory" to "Historial de mediciones", "weightChart" to "Gráfico de peso", "bodyFatChart" to "Gráfico de grasa", "circumferenceChart" to "Gráfico de circunferencias",   "biometricReminderTitle" to "¡Es hora de las mediciones!", "biometricReminderText" to "No olvides registrar tus mediciones corporales semanales.",  "streakChannelName" to "Recordatorios de racha", "streakReminderTitle" to "¡No rompas tu racha!", "streakReminderText" to "¡Entrena hoy para mantener tu racha!",  "welcomeSoundLabel" to "Sonido de bienvenida",
        "foodJournal" to "Diario de alimentos",    "scan" to "Escanear", "scanning" to "Escaneando...", "scanBarcodeHelp" to "Asegúrate de que Google Play Services esté instalado y actualizado", "noFoodEntries" to "Sin entradas de alimentos aún", "todaysMacros" to "Macros de hoy", "stepsLabel" to "Pasos", "activeTimeLabel" to "Tiempo activo", "caloriesLabel" to "Calorías", "proteinLabel" to "Proteínas", "carbsLabel" to "Carbos", "fatLabel" to "Grasas", "breakfast" to "Desayuno", "lunch" to "Almuerzo", "dinner" to "Cena", "snack" to "Snack", "drinks" to "Bebidas", "selectMealType" to "Seleccionar tipo de comida",  "foodName" to "Nombre del alimento", "brandLabel" to "Marca",     "fiber" to "Fibra", "searchFood" to "Buscar alimento", "foodSearchHint" to "Ej: huevo, pollo, arroz", "quantity" to "Cantidad", "gramsShort" to "g", "piecesShort" to "uds", "addToJournal" to "Añadir al diario", "manualEntryMode" to "Entrada manual", "noFoodFound" to "Alimento no encontrado en la lista", "enterManually" to "Introducir alimento manualmente", "per100g" to "por 100g", "perPiece" to "por pieza",
        "aiTrainer" to "Entrenador IA", "aiTrainerWelcome" to "¡Hola! Soy tu entrenador IA", "aiTrainerHint" to "Pregúntame sobre entrenamiento, nutrición o progreso", "aiTrainerHistory" to "Historial de chats", "noHistoryYet" to "Sin historial aún", "current" to "Actual", "askAiTrainer" to "Preguntar al entrenador...", "aiSuggestion1" to "¿Qué entrenamiento me recomiendas hoy?", "aiSuggestion2" to "¿Cómo puedo aumentar el volumen?", "aiSuggestion3" to "¿Necesito un día de descanso?", "aiSuggestion4" to "¿Cómo salgo de un estancamiento?",
         "deleteAccount" to "Eliminar cuenta",
        "exerciseHistory" to "Historial",  
        "favorite" to "Favorito",  "savedExercises" to "Ejercicios guardados", "noFavorites" to "Aún no hay ejercicios guardados", "tapStarToSave" to "Toca la estrella para guardar un ejercicio", "removeFavorite" to "Eliminar de favoritos", 
        "addSet" to "Agregar serie", "exerciseNotes" to "Notas ejercicio", 
        "saveNotes" to "Guardar", 
        "volume" to "Volumen", "maxWeight" to "Peso máx", "maxReps" to "Reps máx", "maxSet" to "Serie máx",
        "today" to "Hoy", "thisWeek" to "Esta semana", "thisMonth" to "Este mes",
        "totalVolumeLabel" to "Volumen total",
        "guest" to "Invitado", "loginWithGoogle" to "Iniciar con Google", "loginWithFacebook" to "Iniciar con Facebook",
        "close" to "Cerrar",  "profile" to "Perfil",
        "appTagline" to "Entrena. Progresa. Repite.", "or" to "o", "dark" to "Oscuro", "light" to "Claro",
        "system" to "Sistema",  
        "selectTheme" to "Seleccionar tema", "settingsAndMore" to "Configuración y más",
        "muscleGroups" to "Grupos musculares",  "features" to "Funciones", "activity" to "Actividad", "tools" to "Herramientas", 
        "englishUS" to "Inglés", "romana" to "Rumano", "russkiy" to "Ruso", "ukrainska" to "Ucraniano",
        "francais" to "Francés", "deutsch" to "Alemán", "espanol" to "Español",
        "italiano" to "Italiano", "turkce" to "Turco", "portugues" to "Portugués", "polski" to "Polaco",
        "motto1" to "Cada repetición cuenta.", "motto2" to "Más fuerte que ayer.",
        "motto3" to "Tu cuerpo, tus reglas.", "motto4" to "Supera tus límites.",
        "motto5" to "La constancia vence al talento.", "motto6" to "La disciplina es libertad.",
        "motto7" to "Sin atajos.", "motto8" to "Ganado, no dado.",
        "motto9" to "No te detengas cuando estés cansado. Detente cuando termines.", "motto10" to "El dolor de hoy se convierte en la fuerza de mañana.",
        "motto11" to "Las grandes cosas nunca nacen en la zona de confort.", "motto12" to "Nunca se vuelve más fácil. Tú te vuelves más fuerte.",
        "motto13" to "No tienes que ser extremo, solo constante.", "motto14" to "Tu único límite eres tú.",
        "motto15" to "Todo experto fue alguna vez un principiante.", "motto16" to "El cuerpo logra lo que la mente cree.",
        "motto17" to "El descanso es parte del proceso, no el enemigo.", "motto18" to "Los pequeños pasos de cada día llevan a grandes resultados.",
        "motto19" to "Entrena como si tuvieras hambre. Mantente humilde.", "motto20" to "El éxito es la suma de pequeños esfuerzos repetidos a diario.",
        "goodMorning" to "Buenos días", "goodAfternoon" to "Buenas tardes", "goodEvening" to "Buenas noches",
        "daysConsecutive" to "días consecutivos", "todaysWorkout" to "Entrenamiento de hoy",
        "todayYouRest" to "Hoy descansas", "restDayMessage" to "El descanso es esencial para la recuperación muscular. Aprovecha para recargar energías y prepararte para tu próximo entrenamiento.",
        "restDayTip" to "Un estiramiento ligero o una caminata pueden ayudar a mantener la circulación.",
        "dayLabel" to "Día", "ofCycle" to "del ciclo",
        "howDoYouFeel" to "Cómo te sientes?", "tiredLabel" to "Cansado", "normalLabel" to "Normal", "energeticLabel" to "Energético",
         "technicalTip" to "Consejo técnico",
        "weeklySummary" to "Resumen semanal", "lastWeekLabel" to "semana pasada",
        "goalLabel" to "Consejo de objetivo", "volumeLabel" to "Volumen", "topExerciseLabel" to "Top ejercicio",
        "nutritionLabel" to "Nutrición", "motivationLabel" to "Motivación",
        "gpsCardioMap" to "Cardio", "startTracking" to "Iniciar seguimiento", 
        "pauseTracking" to "Pausar", "resumeTracking" to "Reanudar",
        "distance" to "Distancia", "pace" to "Ritmo", "speed" to "Velocidad", "duration" to "Duración",
        "savedRoutes" to "Rutas guardadas", "noSavedRoutes" to "No hay rutas guardadas",
        "routeName" to "Nombre de ruta", "saveRoute" to "Guardar ruta", "deleteRoute" to "Eliminar ruta",
        "locationPermissionRequired" to "Se requiere permiso de ubicación",
        "restDaysTitle" to "Días de descanso y descarga", "restDaysSubtitle" to "Programación automática recuperación, estiramientos, yoga suave",
        "deloadWeek" to "Semana de descarga", "recoverySchedule" to "Calendario de recuperación",
        "stretching" to "Estiramientos", "lightYoga" to "Yoga suave", "foamRolling" to "Rodillo de espuma",
         "nextRestDay" to "Próximo día de descanso",
        "muscleNeedsRest" to "Los músculos necesitan descanso", 
        "deloadInfo" to "Info de descarga", "suggestedActivities" to "Actividades sugeridas",
        "activeRecovery" to "Recuperación activa", "lightWalk" to "Caminata ligera",
        "save" to "Guardar",
        "allGood" to "Todo bien", "alreadyHaveAccount" to "¿Ya tienes una cuenta?", "autoDeloadEnabled" to "Descarga automática activada",
        "avgRecovery" to "Recuperación promedio", "caloriesBurned" to "Calorías quemadas", "confirmPassword" to "Confirmar contraseña",
        "createAccountTitle" to "Crear cuenta", "deloadActive" to "Descarga activa", "deloadHistory" to "Historial de descargas",
        "deloadInterval" to "Intervalo de descarga", "deloadNewValue" to "Nuevo valor", "deloadNormalValue" to "Valor normal",
        "deloadPreview" to "Vista previa de descarga", "deloadPreviewSubtitle" to "Ver plan reducido para la próxima descarga", "deloadActiveThisWeek" to "Descarga activa esta semana",         "recommendedForYou" to "Recomendado para ti", "tapToSchedule" to "Toca para programar", "dontHaveAccount" to "¿No tienes cuenta?", "emailError" to "Email inválido",
        "endDeload" to "Terminar descarga", "foamRollingDescription" to "Libera la tensión muscular con foam rolling",
        "heightCm" to "Altura (cm)", "lissDescription" to "Cardio ligero para recuperación activa",
        "loginInstead" to "Iniciar sesión en su lugar", "musclesTiredCount" to "músculos cansados",
        "nameError" to "El nombre es obligatorio", "nameField" to "Nombre", "optional" to "Opcional",
        "passwordError" to "La contraseña debe tener al menos 6 caracteres", "passwordMismatch" to "Las contraseñas no coinciden",
        "passwordStrengthMedium" to "Media", "passwordStrengthStrong" to "Fuerte", "passwordStrengthWeak" to "Débil",
        "privacyPolicyLink" to "Política de privacidad", "recoveryTargeted" to "Recuperación dirigida",
        "startDeload" to "Iniciar descarga", "stretchingDescription" to "Mejora la flexibilidad y movilidad", "warmupStretch" to "Calentamiento", "cooldownStretch" to "Estiramiento final",
        "termsAndConditions" to "Términos y Condiciones", "termsPrefix" to "Al continuar, aceptas nuestros",
        "timeForDeload" to "Es hora de la descarga", "weeks" to "semanas",
        "deloadReasonAuto" to "Es hora de la descarga",
        "deloadReasonGeneral" to "Descarga general",
        "deloadDueBanner" to "Reduce tu volumen esta semana para recuperarte por completo",
        "dailyIntake" to "Ingesta diaria",
        "bio" to "Bio",
        "currentPassword" to "Contraseña actual",
        "newPassword" to "Nueva contraseña",
        "confirmNewPassword" to "Confirmar nueva contraseña",
        "changeLabel" to "Cambiar",
        "currentPasswordRequired" to "Se requiere la contraseña actual",
        "passwordTooShort" to "La nueva contraseña debe tener al menos 6 caracteres",
        "enterPasswordToConfirm" to "Introduce tu contraseña para confirmar",
        "passwordRequiredToDelete" to "Se requiere contraseña para eliminar la cuenta",
        "lowLabel" to "Bajo",
        "highLabel" to "Alto",
        "newChat" to "Nuevo chat",
        "serverSettings" to "Configuración del servidor",
        "deloadWhyTitle" to "¿Por qué la descarga?",
        "deloadWhyBody" to "Entrenar duro %d semanas seguidas acumula fatiga. Una semana de descarga reduce el volumen para que tus músculos se recuperen por completo y vuelvas más fuerte.",
        "deloadDuration" to "Duración",
        "deload1Week" to "1 semana",
        "deload2Weeks" to "2 semanas",
        "deloadReduction" to "Reducción",
        "deloadCompound" to "CMP",
        "deloadNoHistory" to "Aún no hay descargas. Cuando completes una semana de descarga, aparecerá aquí con tu progreso de recuperación.",
        "editRestDay" to "Editar día de descanso",
        "deloadDayOf" to "Día %d de %d",
        "gpsSearching" to "Buscando señal GPS...",
        "gpsError" to "Error de GPS",
        "running" to "Correr",
        "cycling" to "Ciclismo",
        "walking" to "Caminar",
        "routePoints" to "Puntos",
        "paused" to "En pausa", "estimatedOneRm" to "1RM estimado", "nextSetSuggestion" to "Siguiente serie", "setTypeWarmup" to "Calentamiento", "setTypeWorking" to "Trabajo", "setTypeDrop" to "Drop", "setTypeAmrap" to "AMRAP", "setTypePaused" to "Pausa", "setTypeTempo" to "Tempo", "rpeLabel" to "RPE",
        "steps" to "pasos",
        "timerFinished" to "¡Tiempo agotado!",
        "timeToStartNextSet" to "¡Listo para tu siguiente serie!",
        "weeksSinceLastDeload" to "Semanas desde la última descarga", "weightKg" to "Peso (kg)",
        "yogaDescription" to "Relájate y mejora la movilidad con yoga suave",  
        "gpsDisabledTitle" to "GPS desactivado",
        "gpsDisabledMessage" to "Activa el GPS en la configuración de tu teléfono para rastrear tu ruta en tiempo real.",
        "openSettings" to "Abrir configuración",
         "weightKgLabel" to "Peso (kg)", "repsLabel" to "Repeticiones",
        "estimated1rm" to "1RM estimado", "intensityZones" to "Zonas de intensidad",
        "zoneStrength" to "Fuerza (1-2 reps)", "zoneStrengthHypertrophy" to "Fuerza-Hipertrofia (3-5 reps)",
        "zoneHypertrophy" to "Hipertrofia (6-8 reps)", "zoneHypertrophyEndurance" to "Hipertrofia-Resistencia (10-12 reps)",
        "zoneEndurance" to "Resistencia (15+ reps)", 
        "totalVolumeLabel" to "Volumen total", "muscleGroups" to "Grupos musculares", "weeklyTab" to "Semanal",
         "pbsTab" to "Récords",
        "keepTraining" to "Sigue entrenando para superar tus récords!",
        "plateCalculatorTitle" to "Calculadora de discos", "targetWeightLabel" to "Peso objetivo",
        "menu" to "Menú",
        "foodNamePlaceholder" to "Ej.: pechuga de pollo", "brandPlaceholder" to "Ej.: Farm Foods", "backendServerAddress" to "Dirección del servidor backend:", "aiApiKeyOptional" to "Clave API del AI Trainer (opcional):", "leaveEmptyForDefaultServer" to "Déjalo vacío para la URL del servidor por defecto. La clave API solo se necesita si el servidor tiene autenticación habilitada.", "kcal" to "KCAL", "deleteAccountWarning" to "Esta acción es permanente e irreversible. Todos tus datos serán eliminados.", "setStepGoal" to "Establecer objetivo de pasos", "enterDailyStepGoal" to "Ingresa tu objetivo diario de pasos",
        "history" to "Historial", "deleteChat" to "Eliminar chat", "bodyAnatomy" to "Anatomía del cuerpo", "leaveEmptyIfAuthDisabled" to "Déjalo vacío si la autenticación está desactivada", "floatingWindow" to "Ventana flotante", "float" to "Flotante", "clear" to "Borrar", "decrease" to "Disminuir", "increase" to "Aumentar", "exercise" to "Ejercicio", "top" to "TOP", "play" to "Reproducir", "reset" to "Restablecer", "selected" to "Seleccionado", "loadingPlaylists" to "Cargando listas de reproducción...", "failedToLoadPlaylists" to "No se pudieron cargar las listas de reproducción", "noPlaylistsFound" to "No se encontraron listas de reproducción", "createPlaylistFirst" to "Crea primero una lista de reproducción en Spotify", "authenticationFailed" to "Error de autenticación", "unknownError" to "Error desconocido", "selectPlaylist" to "Seleccionar lista de reproducción", "choosePlaylist" to "Elige una lista de reproducción para tu entrenamiento", "tracks" to "canciones", "connectToSpotify" to "Conectar con Spotify", "spotifyAccessDescription" to "Accede a tus listas de reproducción y crea la banda sonora perfecta para tu entrenamiento", "loginWithSpotify" to "Iniciar sesión con Spotify", "spotifyRedirectInfo" to "Serás redirigido a Spotify para autorizar", "navy" to "Navy", "bmi" to "IMC", "exportBackup" to "Exportar copia de seguridad",
        "barWeightLabel" to "Peso de la barra", "lbsKg" to "lbs/kg",
        "platesPerSide" to "Discos por lado", "plateUnit" to "disco(s)",
        "eachSide" to "por lado", "total" to "Total",
        "weightTooLight" to "Peso demasiado ligero para discos (solo barra:",
        "plateCalcNote" to "La calculadora de discos calcula automáticamente qué discos agregar a la barra.",
        "howToGet" to "Cómo obtenerlo:",
        "gender" to "Género", "age" to "Edad", "method" to "Método", "waistCm" to "Cintura (cm)", "neckCm" to "Cuello (cm)", "hipsCm" to "Caderas (cm)", "estimatedBodyFat" to "Grasa corporal estimada", "navyMethodInfo" to "Método Navy: usa cinta métrica para circunferencias",  "bodyFatCalculator" to "Calculadora de grasa corporal", "newPRs" to "Nuevos Récords", "exerciseBreakdown" to "Detalles de ejercicios", "done" to "Listo", "weightGoal" to "Objetivo de peso", "currentWeight" to "Peso actual", "target" to "Objetivo", "deadline" to "Fecha límite", "goalDetails" to "Detalles del objetivo", "startWeight" to "Peso inicial", "targetWeight" to "Peso objetivo",  "noActiveGoal" to "Sin objetivo activo", "setGoalToTrack" to "Establece un objetivo para seguir tu progreso",         "setGoal" to "Establecer objetivo", "pastGoals" to "Objetivos anteriores", "calculate" to "Calcular",
        "weightEvolution" to "Evolución del peso", "measurements" to "mediciones", "startedOn" to "Iniciado el", "editGoal" to "Editar objetivo",
        "equipDumbbells" to "Mancuernas", "equipBarbell" to "Barra", "equipMachine" to "Máquina", "equipCable" to "Cable", "equipBodyweight" to "Peso corporal", "equipEZBar" to "Barra EZ", "equipSmithMachine" to "Máquina Smith", "equipKettlebell" to "Pesa rusa", "equipStabilityBall" to "Balón de estabilidad", "equipSledMachine" to "Trineo", "equipBand" to "Banda",
        "energizeLabel" to "Energía",
        "performLabel" to "Rendimiento",
        "pushItLabel" to "Empuja",
        "openSpotifyLabel" to "Abrir Spotify",
        "tapToPlayLabel" to "Toca para reproducir en Spotify",
        "startingWorkoutLabel" to "Iniciando música de entrenamiento...",
        "signUpSuccessMessage" to "¡Cuenta creada! Por favor, inicia sesión.",
        "trainingSectionLabel" to "Entrenamiento", "frequencyLabel" to "Frecuencia", "xPerWeek" to "x / sem", "editProfile" to "Editar perfil", "memberSince" to "Miembro desde", "changePassword" to "Cambiar contraseña",
        "updateTitle" to "Nueva versión disponible", "updateMessage" to "Kinetic %s se ha publicado. Tienes instalada la versión v%s.\n\nToca para descargar el nuevo APK.", "updateDownload" to "Descargar", "updateLater" to "Más tarde",
        "activeDesc" to "Entrenamiento + actividad diaria",
        "permanentPlan" to "Plan vitalicio",
        "vsPrevious" to "vs anterior",
        "free" to "Gratis",
        "lifetimeAccess" to "Acceso de por vida",
        "male" to "Hombre",
        "purchaseFailed" to "Error en la compra",
        "perMonth" to "/mes",
        "veryActiveDesc" to "Entrenamiento intenso + trabajo físico",
        "restorePurchase" to "Restaurar compras",
        "freePlan" to "Gratis",
        "veryActive" to "Muy activo",
        "whatsYourAge" to "¿Cuántos años tienes?",
        "sedentary" to "Sedentario",
        "unlockedForMinutes" to "Desbloqueado: %s restante",
        "watchAdToUnlock" to "Ver anuncio (desbloquear 30 min)",
        "remaining" to "restante",
        "whatsYourGender" to "¿Cuál es tu género?",
        "restoreSuccess" to "Compras restauradas",
        "buyNow" to "Comprar",
        "bestValue" to "Mejor valor",
        "noPurchasesToRestore" to "No hay compras que restaurar",
        "sessions" to "sesiones",
        "active" to "Activo",
        "allExercises" to "Todos los ejercicios",
        "saveExercise" to "Guardar ejercicio",
        "perYear" to "/año",
        "mostPopular" to "El más popular",
        "unlockPremiumSubtitle" to "Obtén acceso a todas las funciones avanzadas",
        "female" to "Mujer",
        "purchaseSuccess" to "¡Compra realizada! Bienvenido a Premium.",
        "upgradeToUnlock" to "Mejora tu plan para desbloquear",
        "dailyAdLimitReached" to "Has alcanzado el límite diario de desbloqueos",
        "sedentaryDesc" to "Trabajo de oficina, poco movimiento",
        "adUnlockSuccess" to "¡Función desbloqueada durante 30 minutos!",
        "cancelAnytime" to "Cancela cuando quieras en Google Play",
        "purchaseCancelled" to "Compra cancelada",
        "currentPlan" to "Plan actual",
        "adNotReady" to "El anuncio no está listo. Inténtalo de nuevo.",
        "whatsYourActivityLevel" to "¿Cuál es tu nivel de actividad?",
        "workoutAnalytics" to "Análisis de entrenamiento",
        "mostTrained" to "El más entrenado",
        "unlockPremiumTitle" to "Desbloquea Kinetic Premium",
        "oneTimePayment" to "pago único",
        "goalComplete" to "Meta alcanzada!", "waterStreak" to "Serie de hidratación", "ofGoal" to "de la meta", "editWaterGoal" to "Editar meta de agua", "newWaterGoal" to "Nueva meta (ml)",
        "undo" to "Deshacer",
        "workoutReminderTitle" to "Entrenamiento de hoy",
        "workoutReminderBody" to "Es hora de construir fuerza. Concéntrate en __GROUPS__ hoy. Da lo máximo en cada serie y supera tus récords personales.",
        "workoutReminderText" to "¡Es día de entrenamiento! ¡Prepárate!",
        "workoutChannelName" to "Recordatorios de entrenamiento",
        "weeklySummaryTitle" to "Resumen semanal",
        "weeklySummaryText" to "¡Entrenaste __COUNT__ veces esta semana! ¡Sigue así!",
        "weeklySummaryChannelName" to "Resumen semanal",
        "streakReminderTitle" to "¡No rompas tu racha!",
        "streakReminderText" to "¡Entrena hoy para mantener tu racha de __STREAK__ días!",
        "streakChannelName" to "Recordatorios de racha",
        "goalProgressTitle" to "Progreso de pasos",
        "goalProgressText" to "¡Has alcanzado el __PERCENT__% de tu objetivo de pasos! (__CURRENT__/__GOAL__)",
        "goalProgressChannelName" to "Progreso de objetivos",
        "achievementTitle" to "¡Logro desbloqueado!",
        "achievementText" to "¡Felicitaciones! ¡Has desbloqueado una nueva insignia!",
        "achievementChannelName" to "Logros"
    ))

    private fun createIt() = Strings(enRaw() + mapOf(
         "stop" to "Ferma", "openApp" to "Apri l'app", "goal" to "Obiettivo", "stepGoalChannel" to "Obiettivo passi", "stepGoalTitle" to "🏆 Obiettivo passi raggiunto!", "stepGoalText" to "Congratulazioni! Hai raggiunto %d passi!", "stepGoalBig" to "Congratulazioni! Hai raggiunto il tuo obiettivo di %d passi!", "stepGoalKeepGoing" to "Continua così!", "gpsChannelName" to "Tracciamento GPS", "waterChannelName" to "Promemoria acqua", "biometricChannelName" to "Promemoria misure", "friendChannelName" to "Richieste di amicizia", "oneRmCalculator" to "Calcolatore 1RM", "plusGoal" to "+ Obiettivo", "tierFree" to "GRATIS", "tierPro" to "PRO", "tierProPlus" to "PRO+", "tierLifetime" to "A VITA",
         "dashboard" to "Pannello",  "acasa" to "Home", "workouts" to "Allenamenti", "stats" to "Statistiche", "waterIntake" to "Assunzione di acqua", "waterGoal" to "Obiettivo acqua", "addWater" to "Aggiungi acqua",  "height" to "Altezza", "personalInfo" to "Info personali",  "ml" to "ml", "templates" to "Modelli",
              "everyDay" to "Ogni giorno", "reminder" to "Promemoria",
            "weeklyHistory" to "Cronologia settimanale", "tips" to "Consigli", "customMl" to "ml personalizzato", "average" to "Media", "target" to "Obiettivo",
            "waterTip1" to "Bevi 250ml ogni 30 min durante l'allenamento.", "waterTip2" to "Mattina: 500ml al risveglio per un metabolismo attivo.", 
        "recovery" to "Recupero",   "friends" to "Amici",
        "leaderboard" to "Classifica", "all" to "Tutti",  "language" to "Lingua",
        "units" to "Unità", "logout" to "Esci", "login" to "Accedi", "signUp" to "Registrati",
        "email" to "Email", "password" to "Password", "forgotPassword" to "Password dimenticata?",
         "loginAsGuest" to "Accedi come ospite",
        "goalStrength" to "Forza", "goalMass" to "Massa muscolare", "goalWeightLoss" to "Perdita di peso",
        "goalMaintenance" to "Mantenimento", "selectGoal" to "Seleziona il tuo obiettivo",
        "stepOf" to "Passo %d di 5", "whatsYourExperience" to "Qual è il tuo livello di esperienza?",
        "beginnerLabel" to "Principiante", "beginnerDesc" to "0-1 anni di allenamento",
        "intermediateLabel" to "Intermedio", "intermediateDesc" to "1-3 anni di allenamento costante",
        "advancedLabel" to "Avanzato", "advancedDesc" to "3+ anni di allenamento serio",
        "whatEquipment" to "Che attrezzatura hai?",
        "homeNoEquip" to "Casa - Senza attrezzatura", "homeNoEquipDesc" to "Esercizi con peso corporeo solo",
        "homeDumbbells" to "Casa - Manubri/Elastici", "homeDumbbellsDesc" to "Attrezzatura base per casa",
        "fullGym" to "Palestra completa", "fullGymDesc" to "Accesso completo alla palestra",
        "profileGoalLabel" to "Obiettivo", "profileExperienceLabel" to "Esperienza", "profileEquipmentLabel" to "Attrezzatura",
        "trainingFrequency" to "Frequenza di allenamento", "sessionsPerWeek" to "Sessioni a settimana",
        "selectTrainingDays" to "Seleziona i tuoi giorni di allenamento",
        "monday" to "Lunedì", "tuesday" to "Martedì", "wednesday" to "Mercoledì", "thursday" to "Giovedì",
        "friday" to "Venerdì", "saturday" to "Sabato", "sunday" to "Domenica",
        "physicalLimitations" to "Limitazioni fisiche o infortuni?",
        "physicalLimitationsPlaceholder" to "es. dolore al ginocchio, problemi alla schiena (o lascia vuoto)",
        "whichMuscleGroups" to "Quali gruppi muscolari?", "selectAtLeastOne" to "Passo 7 di 7 - seleziona almeno uno",
        "next" to "Avanti", "skip" to "Salta",
        "next" to "Avanti", "skip" to "Salta", "finish" to "Fine", "back" to "Indietro",
        "profileSetup" to "Configura profilo", "enterName" to "Inserisci il tuo nome",
        "pickPhoto" to "Scegli una foto", "saveProfile" to "Salva profilo", "chest" to "Petto",
        "shoulders" to "Spalle",             "arms" to "Braccia", "biceps" to "Bicipite", "triceps" to "Tricipite",
        "legs" to "Gambe", "thighs" to "Cosce", "glutes" to "Glutei", "calves" to "Polpacci",
        "core" to "Core",
        "cardio" to "Cardio", "fullBody" to "Full Body", "pleaseSelectOption" to "Seleziona un'opzione", "sets" to "Serie", "reps" to "Ripetizioni", "weight" to "Peso",
        "startWorkout" to "Inizia allenamento", "nextExercise" to "Esercizio successivo", "notes" to "Note", "cancel" to "Annulla",
        "confirm" to "Conferma", "delete" to "Elimina", "edit" to "Modifica", "search" to "Cerca",
        "noDataYet" to "Nessun dato ancora", 
        "sendRequest" to "Invia richiesta", "accept" to "Accetta", "reject" to "Rifiuta",
        "removeFriend" to "Rimuovi amico", "noFriends" to "Nessun amico ancora",
        "searchUsers" to "Cerca utenti", 
        "searchByNameOrId" to "Cerca per nome o ID", "incomingRequests" to "Richieste in arrivo",
         "yourFriends" to "I tuoi amici",
         "friendRequestSent" to "Richiesta inviata",
         "feedEmpty" to "Il feed è vuoto", 
         "workoutCompleted" to "Allenamento completato!",
        "streakLabel" to "Serie attuale", "bestStreak" to "Miglior serie", "badges" to "Badge",
          "kg" to "kg", "lbs" to "lbs",
          "xp" to "XP",
          "max" to "Max", "ok" to "OK", "lv" to "LV",
         "exportCsv" to "Esporta CSV", "importCsv" to "Importa CSV",
        "subscription" to "Abbonamento", "premium" to "Premium", 
         "subscribe" to "Abbonati", 
          "error" to "Errore",
        "retry" to "Riprova", 
         "recoveryInfo" to "Info recupero",
        "recommendedRecovery" to "Recupero consigliato",
        "progressChart" to "Grafico progressi", 
        "weightProgression" to "Progressione peso", 
         "calendarView" to "Vista calendario",
        "allGroups" to "Tutti", 
        "friendRequestNotificationTitle" to "Richiesta di amicizia",
        "friendRequestNotificationText" to "ti ha inviato una richiesta di amicizia!",
        "selectLanguage" to "Seleziona lingua",  
        "workoutsLabel" to "Allenamenti", 
        "currentStreakLabel" to "Serie attuale", "bestStreakLabel" to "Miglior serie",
         "days" to "giorni",  
          "add" to "Aggiungi", "demoExercise" to "ESERCIZIO DEMO",  "prAndVolume" to "Record e volume", "start" to "Avvia",  "noSavedSetsYet" to "Nessuna serie salvata.", "editSet" to "Modifica serie",  "exercises" to "esercizi", "recovered" to "Recuperato", "almostRecovered" to "Quasi recuperato", "moderate" to "Moderato", "tired" to "Stanco", "exhausted" to "Esausto", "fatigue" to "affaticamento", "chooseMuscleGroup" to "Scegli gruppo muscolare",  "noExercisesFound" to "Nessun esercizio trovato", "tryDifferentFilter" to "Prova un altro filtro o cerca per nome", "voiceSearch" to "Ricerca vocale",  "voiceSearchError" to "Impossibile riconoscere la voce",
        "monthlyProgress" to "Progresso mensile", "completeWorkoutsToSee" to "Completa gli allenamenti per vedere i progressi", "jan" to "Gen", "feb" to "Feb", "mar" to "Mar", "apr" to "Apr", "may" to "Mag", "jun" to "Giu", "jul" to "Lug", "aug" to "Ago", "sep" to "Set", "oct" to "Ott", "nov" to "Nov", "dec" to "Dic", "monthlyDetails" to "Dettagli mensili", "month" to "Mese", "mon" to "Lu", "tue" to "Ma", "wed" to "Me", "thu" to "Gi", "fri" to "Ve", "sat" to "Sa", "sun" to "Do", "noWorkouts" to "Nessun allenamento in questo giorno", 
         "subscribeNow" to "Abbonati ora", "premiumFeature" to "Funzionalità Premium", "subscribersOnly" to "\$feature è disponibile solo per gli abbonati", "choosePlan" to "Scegli un piano", "youAreSubscribed" to "Sei abbonato!", "muscleRecovery" to "Recupero muscolare",  "waterReminderTitle" to "È ora di bere acqua!", "waterReminderText" to "Resta idratato! È ora di bere un bicchiere d'acqua.",   "selectTime" to "Seleziona ora", "forearms" to "Avambracci", "neckAndTraps" to "Collo & Trapezi", "welcome" to "Benvenuto", "athlete" to "Atleta",
        "biometricTracking" to "Monitoraggio biometrico",  "addMeasurement" to "Aggiungi misurazione", "bodyFat" to "Grasso corporeo", "waistCirc" to "Vita", "hipsCirc" to "Fianchi", "thighsCirc" to "Cosce", "chestCirc" to "Petto", "armsCirc" to "Braccia",  "noMeasurements" to "Nessuna misurazione ancora",    "weeksAgo" to "settimane fa", "cm" to "cm", "percent" to "%", "deleteMeasurement" to "Elimina misurazione", "biometricHistory" to "Cronologia misurazioni", "weightChart" to "Grafico del peso", "bodyFatChart" to "Grafico del grasso", "circumferenceChart" to "Grafico delle circonferenze",   "biometricReminderTitle" to "È ora delle misurazioni!", "biometricReminderText" to "Non dimenticare di registrare le tue misurazioni corporee settimanali.",  "streakChannelName" to "Promemoria serie", "streakReminderTitle" to "Non interrompere la tua serie!", "streakReminderText" to "Allenati oggi per mantenere la tua serie!",  "welcomeSoundLabel" to "Suono di benvenuto",
        "foodJournal" to "Diario alimentare",    "scan" to "Scansiona", "scanning" to "Scansione in corso...", "scanBarcodeHelp" to "Assicurati che Google Play Services sia installato e aggiornato", "noFoodEntries" to "Nessuna voce alimentare ancora", "todaysMacros" to "Macronutrienti di oggi", "stepsLabel" to "Passi", "activeTimeLabel" to "Tempo attivo", "caloriesLabel" to "Calorie", "proteinLabel" to "Proteine", "carbsLabel" to "Carboidrati", "fatLabel" to "Grassi", "breakfast" to "Colazione", "lunch" to "Pranzo", "dinner" to "Cena", "snack" to "Spuntino", "drinks" to "Bevande", "selectMealType" to "Seleziona tipo di pasto",  "foodName" to "Nome alimento", "brandLabel" to "Marca",     "fiber" to "Fibre", "searchFood" to "Cerca alimento", "foodSearchHint" to "Es: uovo, pollo, riso", "quantity" to "Quantità", "gramsShort" to "g", "piecesShort" to "pz", "addToJournal" to "Aggiungi al diario", "manualEntryMode" to "Inserimento manuale", "noFoodFound" to "Alimento non trovato nell'elenco", "enterManually" to "Inserisci alimento manualmente", "per100g" to "per 100g", "perPiece" to "per pezzo",
        "aiTrainer" to "Allenatore IA", "aiTrainerWelcome" to "Ciao! Sono il tuo allenatore IA", "aiTrainerHint" to "Chiedimi di allenamento, nutrizione o progressi", "aiTrainerHistory" to "Cronologia chat", "noHistoryYet" to "Nessuna cronologia", "current" to "Attuale", "askAiTrainer" to "Chiedi all'allenatore...", "aiSuggestion1" to "Che allenamento mi consigli oggi?", "aiSuggestion2" to "Come posso aumentare il volume?", "aiSuggestion3" to "Ho bisogno di un giorno di riposo?", "aiSuggestion4" to "Come supero un plateau?",
         "deleteAccount" to "Elimina account",
        "exerciseHistory" to "Cronologia",  
        "favorite" to "Preferito",  "savedExercises" to "Esercizi salvati", "noFavorites" to "Nessun esercizio salvato", "tapStarToSave" to "Tocca la stella per salvare un esercizio", "removeFavorite" to "Rimuovi dai preferiti", 
        "addSet" to "Aggiungi serie", "exerciseNotes" to "Note esercizio", 
        "saveNotes" to "Salva", 
        "volume" to "Volume", "maxWeight" to "Peso máx", "maxReps" to "Rep máx", "maxSet" to "Serie máx",
        "today" to "Oggi", "thisWeek" to "Questa settimana", "thisMonth" to "Questo mese",
        "totalVolumeLabel" to "Volume totale",
        "guest" to "Ospite", "loginWithGoogle" to "Accedi con Google", "loginWithFacebook" to "Accedi con Facebook",
        "close" to "Chiudi",  "profile" to "Profilo",
        "appTagline" to "Allena. Progredisce. Ripeti.", "or" to "o", "dark" to "Scuro", "light" to "Chiaro",
        "system" to "Sistema",  
        "selectTheme" to "Seleziona tema", "settingsAndMore" to "Impostazioni e altro",
        "muscleGroups" to "Gruppi muscolari",  "features" to "Funzionalità", "activity" to "Attività", "tools" to "Strumenti", 
        "englishUS" to "Inglese", "romana" to "Rumeno", "russkiy" to "Russo", "ukrainska" to "Ucraino",
        "francais" to "Francese", "deutsch" to "Tedesco", "espanol" to "Spagnolo",
        "italiano" to "Italiano", "turkce" to "Turco", "portugues" to "Portoghese", "polski" to "Polacco",
        "motto1" to "Ogni ripetizione conta.", "motto2" to "Più forte di ieri.",
        "motto3" to "Il tuo corpo, le tue regole.", "motto4" to "Supera i tuoi limiti.",
        "motto5" to "La costanza batte il talento.", "motto6" to "La disciplina è libertà.",
        "motto7" to "Nessuna scorciatoia.", "motto8" to "Guadagnato, non dato.",
        "motto9" to "Non fermarti quando sei stanco. Fermati quando hai finito.", "motto10" to "Il dolore di oggi diventa la forza di domani.",
        "motto11" to "Le grandi cose non nascono mai nella zona di comfort.", "motto12" to "Non diventa mai più facile. Sei tu che diventi più forte.",
        "motto13" to "Non devi essere estremo, solo costante.", "motto14" to "Il tuo unico limite sei tu.",
        "motto15" to "Ogni esperto è stato un principiante.", "motto16" to "Il corpo raggiunge ciò in cui la mente crede.",
        "motto17" to "Il riposo fa parte del processo, non è il nemico.", "motto18" to "I piccoli passi di ogni giorno portano a grandi risultati.",
        "motto19" to "Allenati come se avessi fame. Resta umile.", "motto20" to "Il successo è la somma di piccoli sforzi ripetuti ogni giorno.",
        "goodMorning" to "Buongiorno", "goodAfternoon" to "Buon pomeriggio", "goodEvening" to "Buonasera",
        "daysConsecutive" to "giorni consecutivi", "todaysWorkout" to "Allenamento di oggi",
        "todayYouRest" to "Oggi riposi", "restDayMessage" to "Il riposo è essenziale per il recupero muscolare. Approfitta per ricaricarti e prepararti al prossimo allenamento.",
        "restDayTip" to "Un leggero stretching o una passeggiata possono aiutare a mantenere la circolazione.",
        "dayLabel" to "Giorno", "ofCycle" to "del ciclo",
        "howDoYouFeel" to "Come ti senti?", "tiredLabel" to "Stanco", "normalLabel" to "Normale", "energeticLabel" to "Energico",
         "technicalTip" to "Consiglio tecnico",
        "weeklySummary" to "Riepilogo settimanale", "lastWeekLabel" to "scorsa settimana",
        "goalLabel" to "Consiglio obiettivo", "volumeLabel" to "Volume", "topExerciseLabel" to "Top esercizio",
        "nutritionLabel" to "Nutrizione", "motivationLabel" to "Motivazione",
        "gpsCardioMap" to "Cardio", "startTracking" to "Inizia tracciamento", 
        "pauseTracking" to "Pausa", "resumeTracking" to "Riprendi",
        "distance" to "Distanza", "pace" to "Ritmo", "speed" to "Velocità", "duration" to "Durata",
        "savedRoutes" to "Percorsi salvati", "noSavedRoutes" to "Nessun percorso salvato",
        "routeName" to "Nome percorso", "saveRoute" to "Salva percorso", "deleteRoute" to "Elimina percorso",
        "locationPermissionRequired" to "Autorizzazione posizione necessaria",
        "restDaysTitle" to "Giorni di riposo e scarico", "restDaysSubtitle" to "Pianificazione automatica recupero, stretching, yoga leggero",
        "deloadWeek" to "Settimana di scarico", "recoverySchedule" to "Programma recupero",
        "stretching" to "Stretching", "lightYoga" to "Yoga leggero", "foamRolling" to "Rullo schiuma",
         "nextRestDay" to "Prossimo giorno di riposo",
        "muscleNeedsRest" to "I muscoli hanno bisogno di riposo", 
        "deloadInfo" to "Info scarico", "suggestedActivities" to "Attività suggerite",
        "activeRecovery" to "Recupero attivo", "lightWalk" to "Camminata leggera",
        "save" to "Salva",
        "allGood" to "Tutto bene", "alreadyHaveAccount" to "Hai già un account?", "autoDeloadEnabled" to "Scarico automatico attivato",
        "avgRecovery" to "Recupero medio", "caloriesBurned" to "Calorie bruciate", "confirmPassword" to "Conferma password",
        "createAccountTitle" to "Crea account", "deloadActive" to "Scarico attivo", "deloadHistory" to "Cronologia scarichi",
        "deloadInterval" to "Intervallo scarico", "deloadNewValue" to "Nuovo valore", "deloadNormalValue" to "Valore normale",
        "deloadPreview" to "Anteprima scarico", "deloadPreviewSubtitle" to "Vedi il piano ridotto per il prossimo scarico", "deloadActiveThisWeek" to "Scarico attivo questa settimana",         "recommendedForYou" to "Consigliato per te", "tapToSchedule" to "Tocca per pianificare", "dontHaveAccount" to "Non hai un account?", "emailError" to "Email non valida",
        "endDeload" to "Termina scarico", "foamRollingDescription" to "Rilassa la tensione muscolare con il rullino",
        "heightCm" to "Altezza (cm)", "lissDescription" to "Cardio leggero per recupero attivo",
        "loginInstead" to "Accedi invece", "musclesTiredCount" to "muscoli stanchi",
        "nameError" to "Il nome è obbligatorio", "nameField" to "Nome", "optional" to "Opzionale",
        "passwordError" to "La password deve avere almeno 6 caratteri", "passwordMismatch" to "Le password non corrispondono",
        "passwordStrengthMedium" to "Media", "passwordStrengthStrong" to "Forte", "passwordStrengthWeak" to "Debole",
        "privacyPolicyLink" to "Informativa sulla privacy", "recoveryTargeted" to "Recupero mirato",
        "startDeload" to "Inizia scarico", "stretchingDescription" to "Migliora flessibilità e mobilità", "warmupStretch" to "Riscaldamento", "cooldownStretch" to "Defaticamento",
        "termsAndConditions" to "Termini e Condizioni", "termsPrefix" to "Continuando, accetti i nostri",
        "timeForDeload" to "È ora dello scarico", "weeks" to "settimane",
        "deloadReasonAuto" to "È ora dello scarico",
        "deloadReasonGeneral" to "Scarico generale",
        "deloadDueBanner" to "Riduci il volume questa settimana per recuperare al meglio",
        "dailyIntake" to "Apporto giornaliero",
        "bio" to "Bio",
        "currentPassword" to "Password attuale",
        "newPassword" to "Nuova password",
        "confirmNewPassword" to "Conferma nuova password",
        "changeLabel" to "Modifica",
        "currentPasswordRequired" to "La password attuale è obbligatoria",
        "passwordTooShort" to "La nuova password deve avere almeno 6 caratteri",
        "enterPasswordToConfirm" to "Inserisci la password per confermare",
        "passwordRequiredToDelete" to "La password è obbligatoria per eliminare l'account",
        "lowLabel" to "Basso",
        "highLabel" to "Alto",
        "newChat" to "Nuova chat",
        "serverSettings" to "Impostazioni server",
        "deloadWhyTitle" to "Perché lo scarico?",
        "deloadWhyBody" to "Allenarsi duramente per %d settimane di fila accumula fatica. Una settimana di scarico riduce il volume così i muscoli recuperano completamente e torni più forte.",
        "deloadDuration" to "Durata",
        "deload1Week" to "1 settimana",
        "deload2Weeks" to "2 settimane",
        "deloadReduction" to "Riduzione",
        "deloadCompound" to "CMP",
        "deloadNoHistory" to "Nessuno scarico per ora. Quando completi una settimana di scarico, apparirà qui con i tuoi progressi di recupero.",
        "editRestDay" to "Modifica giorno di riposo",
        "deloadDayOf" to "Giorno %d di %d",
        "gpsSearching" to "Ricerca segnale GPS...",
        "gpsError" to "Errore GPS",
        "running" to "Corsa",
        "cycling" to "Ciclismo",
        "walking" to "Camminata",
        "routePoints" to "Punti",
        "paused" to "In pausa", "estimatedOneRm" to "1RM stimato", "nextSetSuggestion" to "Prossima serie", "setTypeWarmup" to "Riscaldamento", "setTypeWorking" to "Lavoro", "setTypeDrop" to "Drop", "setTypeAmrap" to "AMRAP", "setTypePaused" to "Pausa", "setTypeTempo" to "Tempo", "rpeLabel" to "RPE",
        "steps" to "passi",
        "timerFinished" to "Tempo scaduto!",
        "timeToStartNextSet" to "Pronto per la prossima serie!",
        "weeksSinceLastDeload" to "Settimane dall'ultimo scarico", "weightKg" to "Peso (kg)",
        "yogaDescription" to "Rilassati e migliora la mobilità con yoga leggero",  
        "gpsDisabledTitle" to "GPS disattivato",
        "gpsDisabledMessage" to "Attiva il GPS nelle impostazioni del telefono per tracciare il percorso in tempo reale.",
        "openSettings" to "Apri impostazioni",
         "weightKgLabel" to "Peso (kg)", "repsLabel" to "Ripetizioni",
        "estimated1rm" to "1RM stimato", "intensityZones" to "Zone di intensità",
        "zoneStrength" to "Forza (1-2 rip.)", "zoneStrengthHypertrophy" to "Forza-Ipertrofia (3-5 rip.)",
        "zoneHypertrophy" to "Ipertrofia (6-8 rip.)", "zoneHypertrophyEndurance" to "Ipertrofia-Resistenza (10-12 rip.)",
        "zoneEndurance" to "Resistenza (15+ rip.)", 
        "totalVolumeLabel" to "Volume totale", "muscleGroups" to "Gruppi muscolari", "weeklyTab" to "Settimanale",
         "pbsTab" to "Record",
        "keepTraining" to "Continua ad allenarti per battere i tuoi record!",
        "plateCalculatorTitle" to "Calcolatore dischi", "targetWeightLabel" to "Peso obiettivo",
        "menu" to "Menu",
        "foodNamePlaceholder" to "Es.: petto di pollo", "brandPlaceholder" to "Es.: Farm Foods", "backendServerAddress" to "Indirizzo del server backend:", "aiApiKeyOptional" to "Chiave API AI Trainer (opzionale):", "leaveEmptyForDefaultServer" to "Lascia vuoto per l'URL del server predefinito. La chiave API serve solo se il server ha l'autenticazione abilitata.", "kcal" to "KCAL", "deleteAccountWarning" to "Questa azione è permanente e irreversibile. Tutti i tuoi dati verranno eliminati.", "setStepGoal" to "Imposta obiettivo passi", "enterDailyStepGoal" to "Inserisci il tuo obiettivo giornaliero di passi",
        "history" to "Cronologia", "deleteChat" to "Elimina chat", "bodyAnatomy" to "Anatomia del corpo", "leaveEmptyIfAuthDisabled" to "Lascia vuoto se l'autenticazione è disattivata", "floatingWindow" to "Finestra flottante", "float" to "Fluttuante", "clear" to "Cancella", "decrease" to "Diminuisci", "increase" to "Aumenta", "exercise" to "Esercizio", "top" to "TOP", "play" to "Riproduci", "reset" to "Reimposta", "selected" to "Selezionato", "loadingPlaylists" to "Caricamento playlist...", "failedToLoadPlaylists" to "Impossibile caricare le playlist", "noPlaylistsFound" to "Nessuna playlist trovata", "createPlaylistFirst" to "Crea prima una playlist su Spotify", "authenticationFailed" to "Autenticazione non riuscita", "unknownError" to "Errore sconosciuto", "selectPlaylist" to "Seleziona playlist", "choosePlaylist" to "Scegli una playlist per il tuo allenamento", "tracks" to "brani", "connectToSpotify" to "Connettiti a Spotify", "spotifyAccessDescription" to "Accedi alle tue playlist e crea la colonna sonora perfetta per il tuo allenamento", "loginWithSpotify" to "Accedi con Spotify", "spotifyRedirectInfo" to "Verrai reindirizzato a Spotify per autorizzare", "navy" to "Navy", "bmi" to "IMC", "exportBackup" to "Esporta backup",
        "barWeightLabel" to "Peso bilanciere", "lbsKg" to "lbs/kg",
        "platesPerSide" to "Dischi per lato", "plateUnit" to "disco(i)",
        "eachSide" to "per lato", "total" to "Totale",
        "weightTooLight" to "Peso troppo leggero per i dischi (solo bilanciere:",
        "plateCalcNote" to "Il calcolatore dischi calcola automaticamente quali dischi aggiungere al bilanciere.",
        "howToGet" to "Come ottenerlo:",
        "gender" to "Genere", "age" to "Età", "method" to "Metodo", "waistCm" to "Vita (cm)", "neckCm" to "Collo (cm)", "hipsCm" to "Fianchi (cm)", "estimatedBodyFat" to "Grasso corporeo stimato", "navyMethodInfo" to "Metodo Navy: usa un metro a nastro per le circonferenze",  "bodyFatCalculator" to "Calcolatore grasso corporeo", "newPRs" to "Nuovi Record", "exerciseBreakdown" to "Dettagli esercizi", "done" to "Fatto", "weightGoal" to "Obiettivo peso", "currentWeight" to "Peso attuale", "target" to "Obiettivo", "deadline" to "Scadenza", "goalDetails" to "Dettagli obiettivo", "startWeight" to "Peso iniziale", "targetWeight" to "Peso obiettivo",  "noActiveGoal" to "Nessun obiettivo attivo", "setGoalToTrack" to "Imposta un obiettivo per monitorare i tuoi progressi",         "setGoal" to "Imposta obiettivo", "pastGoals" to "Obiettivi precedenti", "calculate" to "Calcola",
        "weightEvolution" to "Evoluzione del peso", "measurements" to "misurazioni", "startedOn" to "Iniziato il", "editGoal" to "Modifica obiettivo",
        "equipDumbbells" to "Manubri", "equipBarbell" to "Bilanciere", "equipMachine" to "Macchina", "equipCable" to "Cavo", "equipBodyweight" to "Peso corporeo", "equipEZBar" to "Bilanciere EZ", "equipSmithMachine" to "Macchina Smith", "equipKettlebell" to "Kettlebell", "equipStabilityBall" to "Palla stabilità", "equipSledMachine" to "Slitta", "equipBand" to "Banda",
        "energizeLabel" to "Energia",
        "performLabel" to "Performance",
        "pushItLabel" to "Spingi",
        "openSpotifyLabel" to "Apri Spotify",
        "tapToPlayLabel" to "Tocca per ascoltare su Spotify",
        "startingWorkoutLabel" to "Avvio musica da allenamento...",
        "signUpSuccessMessage" to "Account creato! Effettua il login.",
        "trainingSectionLabel" to "Allenamento", "frequencyLabel" to "Frequenza", "xPerWeek" to "x / sett", "editProfile" to "Modifica profilo", "memberSince" to "Membro dal", "changePassword" to "Cambia password",
        "updateTitle" to "Nuova versione disponibile", "updateMessage" to "Kinetic %s è stata rilasciata. Hai installata la versione v%s.\n\nTocca per scaricare il nuovo APK.", "updateDownload" to "Scarica", "updateLater" to "Più tardi",
        "activeDesc" to "Allenamento + attività quotidiana",
        "permanentPlan" to "Piano a vita",
        "vsPrevious" to "vs precedente",
        "free" to "Gratis",
        "lifetimeAccess" to "Accesso a vita",
        "male" to "Uomo",
        "purchaseFailed" to "Acquisto non riuscito",
        "perMonth" to "/mese",
        "veryActiveDesc" to "Allenamento intenso + lavoro fisico",
        "restorePurchase" to "Ripristina acquisti",
        "freePlan" to "Gratis",
        "veryActive" to "Molto attivo",
        "whatsYourAge" to "Quanti anni hai?",
        "sedentary" to "Sedentario",
        "unlockedForMinutes" to "Sbloccato: %s rimanente",
        "watchAdToUnlock" to "Guarda un annuncio (sblocca 30 min)",
        "remaining" to "rimanenti",
        "whatsYourGender" to "Qual è il tuo genere?",
        "restoreSuccess" to "Acquisti ripristinati",
        "buyNow" to "Acquista",
        "bestValue" to "Miglior rapporto qualità/prezzo",
        "noPurchasesToRestore" to "Nessun acquisto da ripristinare",
        "sessions" to "sessioni",
        "active" to "Attivo",
        "allExercises" to "Tutti gli esercizi",
        "saveExercise" to "Salva esercizio",
        "perYear" to "/anno",
        "mostPopular" to "Il più popolare",
        "unlockPremiumSubtitle" to "Accedi a tutte le funzioni avanzate",
        "female" to "Donna",
        "purchaseSuccess" to "Acquisto riuscito! Benvenuto su Premium.",
        "upgradeToUnlock" to "Passa al piano superiore per sbloccare",
        "dailyAdLimitReached" to "Hai raggiunto il limite giornaliero di sblocchi",
        "sedentaryDesc" to "Lavoro d'ufficio, poca attività",
        "adUnlockSuccess" to "Funzione sbloccata per 30 minuti!",
        "cancelAnytime" to "Annulla quando vuoi su Google Play",
        "purchaseCancelled" to "Acquisto annullato",
        "currentPlan" to "Piano attuale",
        "adNotReady" to "Annuncio non pronto. Riprova.",
        "whatsYourActivityLevel" to "Qual è il tuo livello di attività?",
        "workoutAnalytics" to "Analisi allenamento",
        "mostTrained" to "Il più allenato",
        "unlockPremiumTitle" to "Sblocca Kinetic Premium",
        "oneTimePayment" to "pagamento una tantum",
        "goalComplete" to "Obiettivo raggiunto!", "waterStreak" to "Serie di idratazione", "ofGoal" to "dell'obiettivo", "editWaterGoal" to "Modifica obiettivo acqua", "newWaterGoal" to "Nuovo obiettivo (ml)",
        "undo" to "Annulla",
        "workoutReminderTitle" to "Allenamento di oggi",
        "workoutReminderBody" to "È ora di costruire la forza. Concentrati su __GROUPS__ oggi. Dai il massimo in ogni serie e batti i tuoi record personali.",
        "workoutReminderText" to "Oggi è giorno di allenamento! Preparati!",
        "workoutChannelName" to "Promemoria allenamento",
        "weeklySummaryTitle" to "Riepilogo settimanale",
        "weeklySummaryText" to "Ti sei allenato __COUNT__ volte questa settimana! Continua!",
        "weeklySummaryChannelName" to "Riepilogo settimanale",
        "streakReminderTitle" to "Non interrompere la serie!",
        "streakReminderText" to "Allenati oggi per mantenere la tua serie di __STREAK__ giorni!",
        "streakChannelName" to "Promemoria serie",
        "goalProgressTitle" to "Progresso passi",
        "goalProgressText" to "Hai raggiunto il __PERCENT__% del tuo obiettivo passi! (__CURRENT__/__GOAL__)",
        "goalProgressChannelName" to "Progresso obiettivi",
        "achievementTitle" to "Achievement sbloccato!",
        "achievementText" to "Congratulazioni! Hai sbloccato un nuovo badge!",
        "achievementChannelName" to "Achievement"
    ))

    private fun createTr() = Strings(enRaw() + mapOf(
         "stop" to "Durdur", "openApp" to "Uygulamayı aç", "goal" to "Hedef", "stepGoalChannel" to "Adım hedefi", "stepGoalTitle" to "🏆 Adım hedefine ulaştın!", "stepGoalText" to "Tebrikler! %d adıma ulaştın!", "stepGoalBig" to "Tebrikler! %d adımlık hedefine ulaştın!", "stepGoalKeepGoing" to "Böyle devam et!", "gpsChannelName" to "GPS takibi", "waterChannelName" to "Su hatırlatıcıları", "biometricChannelName" to "Ölçüm hatırlatıcıları", "friendChannelName" to "Arkadaşlık istekleri", "oneRmCalculator" to "1RM Hesaplayıcı", "plusGoal" to "+ Hedef", "tierFree" to "ÜCRETSİZ", "tierPro" to "PRO", "tierProPlus" to "PRO+", "tierLifetime" to "ÖMÜR BOYU",
         "dashboard" to "Gösterge Paneli",  "acasa" to "Ana Sayfa", "workouts" to "Egzersizler", "stats" to "İstatistikler", "waterIntake" to "Su Tüketimi", "waterGoal" to "Su Hedefi", "addWater" to "Su Ekle",  "height" to "Boy", "personalInfo" to "Kişisel Bilgi",  "ml" to "ml", "templates" to "Şablonlar",
              "everyDay" to "Her gün", "reminder" to "Hatırlatıcı",
            "weeklyHistory" to "Haftalık geçmiş", "tips" to "İpuçları", "customMl" to "Özel ml", "average" to "Ortalama", "target" to "Hedef",
            "waterTip1" to "Antrenmanda her 30 dakikada 250ml için.", "waterTip2" to "Sabah: Uyanınca 500ml aktif metabolizma için.", 
        "recovery" to "İyileşme",   "friends" to "Arkadaşlar",
        "leaderboard" to "Sıralama", "all" to "Tümü",  "language" to "Dil",
        "units" to "Birimler", "logout" to "Çıkış", "login" to "Giriş", "signUp" to "Kayıt Ol",
        "email" to "E-posta", "password" to "Şifre", "forgotPassword" to "Şifreni mi unuttun?",
         "loginAsGuest" to "Misafir olarak giriş yap",
        "goalStrength" to "Güç", "goalMass" to "Kas Kütlesi", "goalWeightLoss" to "Kilo Verme",
        "goalMaintenance" to "Koruma", "selectGoal" to "Hedefinizi seçin",
        "stepOf" to "Adım %d / 5", "whatsYourExperience" to "Deneyim seviyeniz nedir?",
        "beginnerLabel" to "Başlangıç", "beginnerDesc" to "0-1 yıl antrenman",
        "intermediateLabel" to "Orta", "intermediateDesc" to "1-3 yıl düzenli antrenman",
        "advancedLabel" to "İleri", "advancedDesc" to "3+ yıl ciddi antrenman",
        "whatEquipment" to "Hangi ekipmanınız var?",
        "homeNoEquip" to "Ev - Ekipmansız", "homeNoEquipDesc" to "Sadece vücut ağırlığı egzersizleri",
        "homeDumbbells" to "Ev - Dambıl/Bantlar", "homeDumbbellsDesc" to "Temel ev ekipmanı",
        "fullGym" to "Tam spor salonu", "fullGymDesc" to "Spor salonuna tam erişim",
        "profileGoalLabel" to "Hedef", "profileExperienceLabel" to "Deneyim", "profileEquipmentLabel" to "Ekipman",
        "trainingFrequency" to "Antrenman sıklığı", "sessionsPerWeek" to "Haftalık seanslar",
        "selectTrainingDays" to "Antrenman günlerinizi seçin",
        "monday" to "Pazartesi", "tuesday" to "Salı", "wednesday" to "Çarşamba", "thursday" to "Perşembe",
        "friday" to "Cuma", "saturday" to "Cumartesi", "sunday" to "Pazar",
        "physicalLimitations" to "Fiziksel sınırlamalar veya sakatlıklar?",
        "physicalLimitationsPlaceholder" to "ör. diz ağrısı, sırt sorunları (veya boş bırakın)",
        "whichMuscleGroups" to "Hangi kas grupları?", "selectAtLeastOne" to "Adım 7 / 7 - en az birini seçin",
        "next" to "İleri", "skip" to "Atla",
        "skip" to "Atla", "finish" to "Bitir", "back" to "Geri", "profileSetup" to "Profil Ayarları",
        "enterName" to "Adınızı girin", "pickPhoto" to "Fotoğraf seç", "saveProfile" to "Profili Kaydet",
        "chest" to "Göğüs", "shoulders" to "Omuzlar",             "arms" to "Kollar", "biceps" to "Biceps", "triceps" to "Triceps",
        "legs" to "Bacaklar", "thighs" to "Uyluk", "glutes" to "Kalça", "calves" to "Baldırlar",
        "core" to "Core", "cardio" to "Kardio", "fullBody" to "Tüm Vücut", "sets" to "Set", "reps" to "Tekrarlar", "weight" to "Ağırlık",
        "startWorkout" to "Antrenmanı Başlat", "nextExercise" to "Sonraki egzersiz", "notes" to "Notlar", "cancel" to "İptal",
        "confirm" to "Onayla", "delete" to "Sil", "edit" to "Düzenle", "search" to "Ara",
        "noDataYet" to "Henüz veri yok", 
        "sendRequest" to "İstek Gönder", "accept" to "Kabul Et", "reject" to "Reddet",
        "removeFriend" to "Arkadaşı Kaldır", "noFriends" to "Henüz arkadaş yok",
        "searchUsers" to "Kullanıcı Ara", 
        "searchByNameOrId" to "İsim veya ID ile ara", "incomingRequests" to "Gelen İstekler",
         "yourFriends" to "Arkadaşların",
         "friendRequestSent" to "İstek Gönderildi",
         "feedEmpty" to "Akış boş", 
         "workoutCompleted" to "Antrenman Tamamlandı!",
        "streakLabel" to "Mevcut Seri", "bestStreak" to "En İyi Seri", "badges" to "Rozetler",
          "kg" to "kg", "lbs" to "lbs",
          "xp" to "XP",
          "max" to "Maks", "ok" to "Tamam", "lv" to "SV",
         "exportCsv" to "CSV Dışa Aktar",
        "importCsv" to "CSV İçe Aktar", "subscription" to "Abonelik", "premium" to "Premium",
        "subscribe" to "Abone Ol",  
         "error" to "Hata", "retry" to "Yeniden Dene",
         "recoveryInfo" to "İyileşme Bilgisi",
        "recommendedRecovery" to "Önerilen İyileşme",
         "progressChart" to "İlerleme Grafiği",
         "weightProgression" to "Ağırlık İlerlemesi",
        "calendarView" to "Takvim Görünümü", 
         "allGroups" to "Tümü",
        "friendRequestNotificationTitle" to "Arkadaşlık İsteği",
        "friendRequestNotificationText" to "size arkadaşlık isteği gönderdi!",
         "selectLanguage" to "Dil Seçin",
        "workoutsLabel" to "Antrenmanlar", 
        "currentStreakLabel" to "Mevcut Seri", "bestStreakLabel" to "En İyi Seri",
         "days" to "gün",  
          "add" to "Ekle", "demoExercise" to "DEMO EGZERSİZ",  "prAndVolume" to "Kişisel rekorlar ve hacim", "start" to "Başlat",  "noSavedSetsYet" to "Henüz kayıtlı set yok.", "editSet" to "Seti düzenle",  "exercises" to "egzersiz", "recovered" to "İyileşti", "almostRecovered" to "Neredeyse iyileşti", "moderate" to "Orta", "tired" to "Yorgun", "exhausted" to "Tükenmiş", "fatigue" to "yorgunluk", "chooseMuscleGroup" to "Kas grubu seçin",  "noExercisesFound" to "Egzersiz bulunamadı", "tryDifferentFilter" to "Farklı bir filtre deneyin veya isme göre arayın", "voiceSearch" to "Sesli arama",  "voiceSearchError" to "Ses tanınamadı",
        "monthlyProgress" to "Aylık ilerleme", "completeWorkoutsToSee" to "İlerlemeyi görmek için antrenmanları tamamlayın", "jan" to "Oca", "feb" to "Şub", "mar" to "Mar", "apr" to "Nis", "may" to "May", "jun" to "Haz", "jul" to "Tem", "aug" to "Ağu", "sep" to "Eyl", "oct" to "Eki", "nov" to "Kas", "dec" to "Ara", "monthlyDetails" to "Aylık detaylar", "month" to "Ay", "mon" to "Pzt", "tue" to "Sal", "wed" to "Çar", "thu" to "Per", "fri" to "Cum", "sat" to "Cmt", "sun" to "Paz", "noWorkouts" to "Bu gün antrenman yok", 
         "subscribeNow" to "Şimdi abone ol", "premiumFeature" to "Premium Özellik", "subscribersOnly" to "\$feature sadece aboneler için mevcut", "choosePlan" to "Bir plan seçin", "youAreSubscribed" to "Abone oldunuz!", "muscleRecovery" to "Kas İyileşmesi",  "waterReminderTitle" to "Su içme zamanı!", "waterReminderText" to "Su için! Bir bardak su içme zamanı.",   "selectTime" to "Saat seç", "forearms" to "Ön kollar", "neckAndTraps" to "Boyun & Trapez kasları", "welcome" to "Hoş geldin", "athlete" to "Sporcu",
        "biometricTracking" to "Biyometrik Takip",  "addMeasurement" to "Ölçüm ekle", "bodyFat" to "Vücut yağı", "waistCirc" to "Bel", "hipsCirc" to "Kalça", "thighsCirc" to "Uyluk", "chestCirc" to "Göğüs", "armsCirc" to "Kollar",  "noMeasurements" to "Henüz ölçüm yok",    "weeksAgo" to "hafta önce", "cm" to "cm", "percent" to "%", "deleteMeasurement" to "Ölçümü sil", "biometricHistory" to "Ölçüm geçmişi", "weightChart" to "Ağırlık grafiği", "bodyFatChart" to "Yağ grafiği", "circumferenceChart" to "Çevre grafiği",   "biometricReminderTitle" to "Ölçüm zamanı!", "biometricReminderText" to "Haftalık vücut ölçümlerinizi kaydetmeyi unutmayın.",  "streakChannelName" to "Seri Hatırlatıcıları", "streakReminderTitle" to "Serini bozma!", "streakReminderText" to "Sürdürmek için bugün antrenman yap!",  "welcomeSoundLabel" to "Karşılama sesi",
        "foodJournal" to "Besin Günlüğü",    "scan" to "Tara", "scanning" to "Taranıyor...", "scanBarcodeHelp" to "Google Play Services'in yüklü ve güncel olduğundan emin olun", "noFoodEntries" to "Henüz besin girişi yok", "todaysMacros" to "Bugünün Makroları", "stepsLabel" to "Adımlar", "activeTimeLabel" to "Aktif süre", "caloriesLabel" to "Kalori", "proteinLabel" to "Protein", "carbsLabel" to "Karb", "fatLabel" to "Yağ", "breakfast" to "Kahvaltı", "lunch" to "Öğle yemeği", "dinner" to "Akşam yemeği", "snack" to "Atıştırmalık", "drinks" to "İçecekler", "selectMealType" to "Öğün türü seçin",  "foodName" to "Besin adı", "brandLabel" to "Marka",     "fiber" to "Lif", "searchFood" to "Besin ara", "foodSearchHint" to "Örn: yumurta, tavuk, pirinç", "quantity" to "Miktar", "gramsShort" to "g", "piecesShort" to "adet", "addToJournal" to "Günlüğe ekle", "manualEntryMode" to "Manuel giriş", "noFoodFound" to "Besin listede bulunamadı", "enterManually" to "Besini manuel girin", "per100g" to "100g başına", "perPiece" to "adet başına",
        "aiTrainer" to "AI Antrenör", "aiTrainerWelcome" to "Merhaba! Ben yapay zeka antrenörünüz", "aiTrainerHint" to "Antrenman, beslenme veya ilerleme hakkında sorun", "aiTrainerHistory" to "Sohbet geçmişi", "noHistoryYet" to "Henüz geçmiş yok", "current" to "Mevcut", "askAiTrainer" to "Antrenöre sor...", "aiSuggestion1" to "Bugun ne onerirsiniz?", "aiSuggestion2" to "Hacmi nasil artirabilirim?", "aiSuggestion3" to "Dinlenme gunune ihtiyacim var mi?", "aiSuggestion4" to "Platodan nasil cikarim?",
         "deleteAccount" to "Hesabı Sil",
        "exerciseHistory" to "Geçmiş",  
        "favorite" to "Favori",  "savedExercises" to "Kaydedilen egzersizler", "noFavorites" to "Henüz kaydedilen egzersiz yok", "tapStarToSave" to "Bir egzersizi kaydetmek için yıldıza dokunun", "removeFavorite" to "Favorilerden kaldır", 
        "addSet" to "Set ekle", "exerciseNotes" to "Notlar", 
        "saveNotes" to "Kaydet", 
        "volume" to "Hacim", "maxWeight" to "Max ağırlık", "maxReps" to "Max tekrar", "maxSet" to "Max set",
        "today" to "Bugün", "thisWeek" to "Bu hafta", "thisMonth" to "Bu ay",
        "totalVolumeLabel" to "Toplam hacim",
        "guest" to "Misafir", "loginWithGoogle" to "Google ile giriş", "loginWithFacebook" to "Facebook ile giriş",
        "close" to "Kapat",  "profile" to "Profil",
        "appTagline" to "Antrenman yap. Geliş. Tekrarla.", "or" to "veya", "dark" to "Karanlık", "light" to "Aydınlık",
        "system" to "Sistem",  
        "selectTheme" to "Tema seç", "settingsAndMore" to "Ayarlar ve daha fazlası",
        "muscleGroups" to "Kas grupları",  "features" to "Özellikler", "activity" to "Aktivite", "tools" to "Araçlar", 
        "englishUS" to "İngilizce", "romana" to "Rumence", "russkiy" to "Rusça", "ukrainska" to "Ukraynaca",
        "francais" to "Fransızca", "deutsch" to "Almanca", "espanol" to "İspanyolca",
        "italiano" to "İtalyanca", "turkce" to "Türkçe", "portugues" to "Portekizce", "polski" to "Lehçe",
        "motto1" to "Her tekrar sayılır.", "motto2" to "Dünden daha güçlü.",
        "motto3" to "Senin bedenin, senin kuralların.", "motto4" to "Limitlerini zorla.",
        "motto5" to "Tutarlılık yeteneği yener.", "motto6" to "Disiplin özgürlüktür.",
        "motto7" to "Kısa yol yok.", "motto8" to "Kazanılmış, verilmiş değil.",
        "motto9" to "Yorgun olduğunda durma. Bitirdiğinde dur.", "motto10" to "Bugünkü acı yarının gücü olur.",
        "motto11" to "Büyük şeyler asla konfor alanından çıkmaz.", "motto12" to "Asla kolaylaşmaz. Sadece sen güçlenirsin.",
        "motto13" to "Aşırı olmana gerek yok, sadece tutarlı ol.", "motto14" to "Tek sınırın sensin.",
        "motto15" to "Her uzman bir zamanlar acemiydi.", "motto16" to "Beden, zihnin inandığını başarır.",
        "motto17" to "Dinlenme sürecin parçasıdır, düşman değil.", "motto18" to "Her gün küçük adımlar büyük sonuçlara götürür.",
        "motto19" to "Açmış gibi antrenman yap. Mütevazı kal.", "motto20" to "Başarı, her gün tekrarlanan küçük çabaların toplamıdır.",
        "goodMorning" to "Günaydın", "goodAfternoon" to "İyi günler", "goodEvening" to "İyi akşamlar",
        "daysConsecutive" to "art arda gün", "todaysWorkout" to "Bugünkü antrenman",
        "todayYouRest" to "Bugün dinleniyorsun", "restDayMessage" to "Dinlenme kas iyileşmesi için gereklidir. Bu zamanı yenilenmek ve bir sonraki antrenmana hazırlanmak için kullan.",
        "restDayTip" to "Hafif esneme veya yürüyüş dolaşımı desteklemeye yardımcı olabilir.",
        "dayLabel" to "Gün", "ofCycle" to "döngü",
        "howDoYouFeel" to "Nasıl hissediyorsun?", "tiredLabel" to "Yorgun", "normalLabel" to "Normal", "energeticLabel" to "Enerjik",
         "technicalTip" to "Teknik ipucu",
        "weeklySummary" to "Haftalık özet", "lastWeekLabel" to "geçen hafta",
        "goalLabel" to "Hedef ipucu", "volumeLabel" to "Hacim", "topExerciseLabel" to "En iyi egzersiz",
        "nutritionLabel" to "Beslenme", "motivationLabel" to "Motivasyon",
        "gpsCardioMap" to "Cardio", "startTracking" to "Takibi başlat", 
        "pauseTracking" to "Duraklat", "resumeTracking" to "Devam et",
        "distance" to "Mesafe", "pace" to "Tempo", "speed" to "Hız", "duration" to "Süre",
        "savedRoutes" to "Kayıtlı rotalar", "noSavedRoutes" to "Henüz kayıtlı rota yok",
        "routeName" to "Rota adı", "saveRoute" to "Rotayı kaydet", "deleteRoute" to "Rotayı sil",
        "locationPermissionRequired" to "Konum izni gerekli",
        "restDaysTitle" to "Dinlenme günleri & Deşarj", "restDaysSubtitle" to "Otomatik iyileşme, esneme, hafif yoga planlama",
        "deloadWeek" to "Deşarj haftası", "recoverySchedule" to "İyileşme programı",
        "stretching" to "Esneme", "lightYoga" to "Hafif yoga", "foamRolling" to "Köpük rulo",
         "nextRestDay" to "Sonraki dinlenme günü",
        "muscleNeedsRest" to "Kasların dinlenmeye ihtiyacı var", 
        "deloadInfo" to "Deşarj bilgisi", "suggestedActivities" to "Önerilen aktiviteler",
        "activeRecovery" to "Aktif iyileşme", "lightWalk" to "Hafif yürüyüş",
        "save" to "Kaydet",
        "allGood" to "Her şey yolunda", "alreadyHaveAccount" to "Zaten hesabınız var mı?", "autoDeloadEnabled" to "Otomatik deşarj etkin",
        "avgRecovery" to "Ortalama iyileşme", "caloriesBurned" to "Yakılan kalori", "confirmPassword" to "Şifreyi onayla",
        "createAccountTitle" to "Hesap oluştur", "deloadActive" to "Deşarj aktif", "deloadHistory" to "Deşarj geçmişi",
        "deloadInterval" to "Deşarj aralığı", "deloadNewValue" to "Yeni değer", "deloadNormalValue" to "Normal değer",
        "deloadPreview" to "Deşarj önizleme", "deloadPreviewSubtitle" to "Sonraki deşarj için azaltılmış planı görüntüle", "deloadActiveThisWeek" to "Bu hafta deşarj aktif",         "recommendedForYou" to "Sizin için önerilen", "tapToSchedule" to "Zamanlamak için dokunun", "dontHaveAccount" to "Hesabınız yok mu?", "emailError" to "Geçersiz e-posta",
        "endDeload" to "Deşarji bitir", "foamRollingDescription" to "Kas gerginliğini köpük rulo ile giderin",
        "heightCm" to "Boy (cm)", "lissDescription" to "Aktif iyileşme için hafif kardiyo",
        "loginInstead" to "Bunun yerine giriş yap", "musclesTiredCount" to "kas yorgun",
        "nameError" to "İsim zorunludur", "nameField" to "İsim", "optional" to "İsteğe bağlı",
        "passwordError" to "Şifre en az 6 karakter olmalıdır", "passwordMismatch" to "Şifreler eşleşmiyor",
        "passwordStrengthMedium" to "Orta", "passwordStrengthStrong" to "Güçlü", "passwordStrengthWeak" to "Zayıf",
        "privacyPolicyLink" to "Gizlilik Politikası", "recoveryTargeted" to "Hedefli iyileşme",
        "startDeload" to "Deşarja başla", "stretchingDescription" to "Esneklik ve hareketliliği artırın", "warmupStretch" to "Isınma", "cooldownStretch" to "Soğuma",
        "termsAndConditions" to "Kullanım Koşulları", "termsPrefix" to "Devam ederek şunları kabul etmiş olursunuz",
        "timeForDeload" to "Deşarj zamanı", "weeks" to "hafta",
        "deloadReasonAuto" to "Deşarj zamanı",
        "deloadReasonGeneral" to "Genel deşarj",
        "deloadDueBanner" to "Bu hafta tam iyileşme için hacmini azalt",
        "dailyIntake" to "Günlük alım",
        "bio" to "Bio",
        "currentPassword" to "Mevcut şifre",
        "newPassword" to "Yeni şifre",
        "confirmNewPassword" to "Yeni şifreyi onayla",
        "changeLabel" to "Değiştir",
        "currentPasswordRequired" to "Mevcut şifre gerekli",
        "passwordTooShort" to "Yeni şifre en az 6 karakter olmalı",
        "enterPasswordToConfirm" to "Onaylamak için şifrenizi girin",
        "passwordRequiredToDelete" to "Hesabı silmek için şifre gerekli",
        "lowLabel" to "Düşük",
        "highLabel" to "Yüksek",
        "newChat" to "Yeni sohbet",
        "serverSettings" to "Sunucu ayarları",
        "deloadWhyTitle" to "Neden deşarj?",
        "deloadWhyBody" to "%d hafta boyunca yoğun antrenman yorgunluk biriktirir. Deşarj haftası hacmi azaltır, böylece kaslar tamamen toparlanır ve daha güçlü dönersiniz.",
        "deloadDuration" to "Süre",
        "deload1Week" to "1 hafta",
        "deload2Weeks" to "2 hafta",
        "deloadReduction" to "Azaltma",
        "deloadCompound" to "CMP",
        "deloadNoHistory" to "Henüz deşarj yok. Bir deşarj haftasını tamamladığınızda, iyileşme ilerlemenizle birlikte burada görünecektir.",
        "editRestDay" to "Dinlenme gününü düzenle",
        "deloadDayOf" to "%d. gün / %d",
        "gpsSearching" to "GPS sinyali aranıyor...",
        "gpsError" to "GPS hatası",
        "running" to "Koşu",
        "cycling" to "Bisiklet",
        "walking" to "Yürüyüş",
        "routePoints" to "Noktalar",
        "paused" to "Duraklatıldı", "estimatedOneRm" to "Tahmini 1RM", "nextSetSuggestion" to "Sonraki set", "setTypeWarmup" to "Isınma", "setTypeWorking" to "Çalışma", "setTypeDrop" to "Drop", "setTypeAmrap" to "AMRAP", "setTypePaused" to "Duraklat", "setTypeTempo" to "Tempo", "rpeLabel" to "RPE",
        "steps" to "adım",
        "timerFinished" to "Süre doldu!",
        "timeToStartNextSet" to "Sıradaki set için hazırsınız!",
        "weeksSinceLastDeload" to "Son deşarjdan bu yana hafta", "weightKg" to "Ağırlık (kg)",
        "yogaDescription" to "Hafif yoga ile rahatlayın ve hareketliliği artırın",  
        "gpsDisabledTitle" to "GPS devre dışı",
        "gpsDisabledMessage" to "Rotalarınızı gerçek zamanlı olarak takip etmek için telefon ayarlarından GPS'i etkinleştirin.",
        "openSettings" to "Ayarları aç",
         "weightKgLabel" to "Ağırlık (kg)", "repsLabel" to "Tekrarlar",
        "estimated1rm" to "Tahmini 1RM", "intensityZones" to "Yoğunluk bölgeleri",
        "zoneStrength" to "Güç (1-2 tekrar)", "zoneStrengthHypertrophy" to "Güç-Hipertrofi (3-5 tekrar)",
        "zoneHypertrophy" to "Hipertrofi (6-8 tekrar)", "zoneHypertrophyEndurance" to "Hipertrofi-Dayanıklılık (10-12 tekrar)",
        "zoneEndurance" to "Dayanıklılık (15+ tekrar)", 
        "totalVolumeLabel" to "Toplam hacim", "muscleGroups" to "Kas grupları", "weeklyTab" to "Haftalık",
         "pbsTab" to "Rekorlar",
        "keepTraining" to "Rekorlarını kırmak için antrenmana devam et!",
        "plateCalculatorTitle" to "Disk hesaplayıcı", "targetWeightLabel" to "Hedef ağırlık",
        "menu" to "Menü",
        "foodNamePlaceholder" to "Örn: Tavuk göğsü", "brandPlaceholder" to "Örn: Farm Foods", "backendServerAddress" to "Backend sunucu adresi:", "aiApiKeyOptional" to "AI Trainer API Anahtarı (isteğe bağlı):", "leaveEmptyForDefaultServer" to "Varsayılan sunucu URL'si için boş bırakın. API anahtarı yalnızca sunucuda kimlik doğrulama etkinse gerekir.", "kcal" to "KCAL", "deleteAccountWarning" to "Bu işlem kalıcıdır ve geri alınamaz. Tüm verileriniz silinecektir.", "setStepGoal" to "Adım hedefini belirle", "enterDailyStepGoal" to "Günlük adım hedefinizi girin",
        "history" to "Geçmiş", "deleteChat" to "Sohbeti sil", "bodyAnatomy" to "Vücut anatomisi", "leaveEmptyIfAuthDisabled" to "Kimlik doğrulama kapalıysa boş bırakın", "floatingWindow" to "Yüzen pencere", "float" to "Yüzen", "clear" to "Temizle", "decrease" to "Azalt", "increase" to "Artır", "exercise" to "Egzersiz", "top" to "TOP", "play" to "Oynat", "reset" to "Sıfırla", "selected" to "Seçildi", "loadingPlaylists" to "Çalma listeleri yükleniyor...", "failedToLoadPlaylists" to "Çalma listeleri yüklenemedi", "noPlaylistsFound" to "Çalma listesi bulunamadı", "createPlaylistFirst" to "Önce Spotify'da bir çalma listesi oluşturun", "authenticationFailed" to "Kimlik doğrulama başarısız", "unknownError" to "Bilinmeyen hata", "selectPlaylist" to "Çalma listesi seç", "choosePlaylist" to "Antrenmanın için bir çalma listesi seç", "tracks" to "parça", "connectToSpotify" to "Spotify'a bağlan", "spotifyAccessDescription" to "Çalma listelerine eriş ve antrenmanın için mükemmel müziği ayarla", "loginWithSpotify" to "Spotify ile giriş yap", "spotifyRedirectInfo" to "Yetkilendirme için Spotify'a yönlendirileceksiniz", "navy" to "Navy", "bmi" to "BMI", "exportBackup" to "Yedekleme dışa aktar",
        "barWeightLabel" to "Bar ağırlığı", "lbsKg" to "lbs/kg",
        "platesPerSide" to "Taraf başına disk", "plateUnit" to "disk",
        "eachSide" to "taraf başına", "total" to "Toplam",
        "weightTooLight" to "Diskler için ağırlık çok hafif (sadece bar:",
        "plateCalcNote" to "Disk hesaplayıcı, hedef ağırlığa ulaşmak için baraya hangi disklerin eklenmesi gerektiğini otomatik hesaplar.",
        "howToGet" to "Nasıl alınır:",
        "gender" to "Cinsiyet", "age" to "Yaş", "method" to "Yöntem", "waistCm" to "Bel (cm)", "neckCm" to "Boyun (cm)", "hipsCm" to "Kalça (cm)", "estimatedBodyFat" to "Tahmini vücut yağı", "navyMethodInfo" to "Navy Yöntemi: çevre ölçümleri için mezura kullanır",  "bodyFatCalculator" to "Vücut Yağı Hesaplayıcı", "newPRs" to "Yeni Rekorlar", "exerciseBreakdown" to "Egzersiz Detayları", "done" to "Bitti", "weightGoal" to "Kilo Hedefi", "currentWeight" to "Mevcut Kilo", "target" to "Hedef", "deadline" to "Son Tarih", "goalDetails" to "Hedef Detayları", "startWeight" to "Başlangıç Kilosu", "targetWeight" to "Hedef Kilo",  "noActiveGoal" to "Aktif hedef yok", "setGoalToTrack" to "İlerlemenizi takip etmek için bir hedef belirleyin",         "setGoal" to "Hedef Belirle", "pastGoals" to "Geçmiş Hedefler", "calculate" to "Hesapla",
        "weightEvolution" to "Kilo Değişimi", "measurements" to "ölçüm", "startedOn" to "Başlangıç", "editGoal" to "Hedefi düzenle",
        "equipDumbbells" to "Dumbbells", "equipBarbell" to "Barbell", "equipMachine" to "Makine", "equipCable" to "Kablo", "equipBodyweight" to "Vücut ağırlığı", "equipEZBar" to "EZ Bar", "equipSmithMachine" to "Smith Makinesi", "equipKettlebell" to "Kettlebell", "equipStabilityBall" to "Denge topu", "equipSledMachine" to "Kızak makinesi", "equipBand" to "Direnç bandı",
        "energizeLabel" to "Enerji",
        "performLabel" to "Performans",
        "pushItLabel" to "Hadi",
        "openSpotifyLabel" to "Spotify'ı Aç",
        "tapToPlayLabel" to "Spotify'da çalmak için dokunun",
        "startingWorkoutLabel" to "Antrenman müziği başlatılıyor...",
        "signUpSuccessMessage" to "Hesap oluşturuldu! Lütfen giriş yapın.",
        "trainingSectionLabel" to "Antrenman", "frequencyLabel" to "Sıklık", "xPerWeek" to "x / hafta", "editProfile" to "Profili Düzenle", "memberSince" to "Üyelik tarihi", "changePassword" to "Şifreyi Değiştir",
        "updateTitle" to "Yeni sürüm mevcut", "updateMessage" to "Kinetic %s yayınlandı. v%s sürümünü yüklediniz.\n\nYeni APK'yı indirmek için dokunun.", "updateDownload" to "İndir", "updateLater" to "Daha sonra",
        "activeDesc" to "Antrenman + günlük aktivite",
        "permanentPlan" to "Ömür Boyu Plan",
        "vsPrevious" to "öncekine göre",
        "free" to "Ücretsiz",
        "lifetimeAccess" to "Ömür boyu erişim",
        "male" to "Erkek",
        "purchaseFailed" to "Satın alma başarısız oldu",
        "perMonth" to "/ay",
        "veryActiveDesc" to "Yoğun antrenman + fiziksel iş",
        "restorePurchase" to "Satın alımları geri yükle",
        "freePlan" to "Ücretsiz",
        "veryActive" to "Çok Aktif",
        "whatsYourAge" to "Kaç yaşındasınız?",
        "sedentary" to "Hareketsiz",
        "unlockedForMinutes" to "Açıldı: %s kaldı",
        "watchAdToUnlock" to "Reklam izle (30 dk aç)",
        "remaining" to "kalan",
        "whatsYourGender" to "Cinsiyetiniz nedir?",
        "restoreSuccess" to "Satın alımlar geri yüklendi",
        "buyNow" to "Satın Al",
        "bestValue" to "En iyi değer",
        "noPurchasesToRestore" to "Geri yüklenecek satın alma yok",
        "sessions" to "seans",
        "active" to "Aktif",
        "allExercises" to "Tüm Egzersizler",
        "saveExercise" to "Egzersizi Kaydet",
        "perYear" to "/yıl",
        "mostPopular" to "En popüler",
        "unlockPremiumSubtitle" to "Tüm gelişmiş özelliklere erişin",
        "female" to "Kadın",
        "purchaseSuccess" to "Satın alma başarılı! Premium'a hoş geldiniz.",
        "upgradeToUnlock" to "Açmak için yükseltin",
        "pleaseSelectOption" to "Lütfen bir seçenek seçin",
        "dailyAdLimitReached" to "Günlük açma limitine ulaştınız",
        "sedentaryDesc" to "Masa başı iş, az hareket",
        "adUnlockSuccess" to "Özellik 30 dakikalığına açıldı!",
        "cancelAnytime" to "Google Play'de istediğiniz zaman iptal edin",
        "purchaseCancelled" to "Satın alma iptal edildi",
        "currentPlan" to "Mevcut plan",
        "adNotReady" to "Reklam hazır değil. Lütfen tekrar deneyin.",
        "whatsYourActivityLevel" to "Aktivite seviyeniz nedir?",
        "workoutAnalytics" to "Antrenman Analizi",
        "mostTrained" to "En çok çalışılan",
        "unlockPremiumTitle" to "Kinetic Premium'u Aç",
        "oneTimePayment" to "tek seferlik ödeme",
        "goalComplete" to "Hedefe ulaşıldı!", "waterStreak" to "Hidrasyon serisi", "ofGoal" to "hedefin", "editWaterGoal" to "Su hedefini düzenle", "newWaterGoal" to "Yeni hedef (ml)",
        "undo" to "Geri Al",
        "workoutReminderTitle" to "Bugünkü Antrenman",
        "workoutReminderBody" to "Güç inşa etme zamanı. Bugün __GROUPS__ üzerine yoğunlaş. Her sette maksimum efor göster ve kişisel rekorlarını kır.",
        "workoutReminderText" to "Bugün antrenman günü! Hazırlan!",
        "workoutChannelName" to "Antrenman Hatırlatıcıları",
        "weeklySummaryTitle" to "Haftalık Özet",
        "weeklySummaryText" to "Bu hafta __COUNT__ kez antrenman yaptın! Devam et!",
        "weeklySummaryChannelName" to "Haftalık Özet",
        "streakReminderTitle" to "Seriyi bozma!",
        "streakReminderText" to "__STREAK__ günlük serini sürdürmek için bugün antrenman yap!",
        "streakChannelName" to "Seri Hatırlatıcıları",
        "goalProgressTitle" to "Adım Hedefi İlerlemesi",
        "goalProgressText" to "Adım hedefinin __PERCENT__%'ine ulaştın! (__CURRENT__/__GOAL__)",
        "goalProgressChannelName" to "Hedef İlerlemesi",
        "achievementTitle" to "Başarı Kilidi Açıldı!",
        "achievementText" to "Tebrikler! Yeni bir rozet açtın!",
        "achievementChannelName" to "Başarılar"
    ))

    private fun createPt() = Strings(enRaw() + mapOf(
         "stop" to "Parar", "openApp" to "Abrir o app", "goal" to "Meta", "stepGoalChannel" to "Meta de passos", "stepGoalTitle" to "🏆 Meta de passos atingida!", "stepGoalText" to "Parabéns! Você atingiu %d passos!", "stepGoalBig" to "Parabéns! Você atingiu sua meta de %d passos!", "stepGoalKeepGoing" to "Continue assim!", "gpsChannelName" to "Rastreamento GPS", "waterChannelName" to "Lembretes de água", "biometricChannelName" to "Lembretes de medidas", "friendChannelName" to "Pedidos de amizade", "oneRmCalculator" to "Calculadora 1RM", "plusGoal" to "+ Meta", "tierFree" to "GRÁTIS", "tierPro" to "PRO", "tierProPlus" to "PRO+", "tierLifetime" to "VITALÍCIO",
         "dashboard" to "Painel",  "acasa" to "Início", "workouts" to "Treinos", "stats" to "Estatísticas", "waterIntake" to "Consumo de água", "waterGoal" to "Meta de água", "addWater" to "Adicionar água",  "height" to "Altura", "personalInfo" to "Informações pessoais",  "ml" to "ml", "templates" to "Modelos",
              "everyDay" to "Todos os dias", "reminder" to "Lembrete",
            "weeklyHistory" to "Histórico semanal", "tips" to "Dicas", "customMl" to "ml personalizado", "average" to "Média", "target" to "Meta",
            "waterTip1" to "Beba 250ml a cada 30 min durante o treino.", "waterTip2" to "Manhã: 500ml ao acordar para metabolismo ativo.", 
        "recovery" to "Recuperação",   "friends" to "Amigos",
        "leaderboard" to "Leaderboard", "all" to "Todos",  "language" to "Idioma",
        "units" to "Unidades", "logout" to "Sair", "login" to "Entrar", "signUp" to "Cadastrar-se",
        "email" to "E-mail", "password" to "Senha", "forgotPassword" to "Esqueceu a senha?",
         "loginAsGuest" to "Entrar como convidado",
        "goalStrength" to "Força", "goalMass" to "Massa Muscular", "goalWeightLoss" to "Perda de Peso",
        "goalMaintenance" to "Manutenção", "selectGoal" to "Selecione seu objetivo",
        "stepOf" to "Passo %d de 5", "whatsYourExperience" to "Qual é o seu nível de experiência?",
        "beginnerLabel" to "Iniciante", "beginnerDesc" to "0-1 ano de treino",
        "intermediateLabel" to "Intermediário", "intermediateDesc" to "1-3 anos de treino consistente",
        "advancedLabel" to "Avançado", "advancedDesc" to "3+ anos de treino sério",
        "whatEquipment" to "Que equipamento você tem?",
        "homeNoEquip" to "Casa - Sem equipamento", "homeNoEquipDesc" to "Exercícios com peso corporal apenas",
        "homeDumbbells" to "Casa - Halteres/Bandas", "homeDumbbellsDesc" to "Equipamento básico para casa",
        "fullGym" to "Academia completa", "fullGymDesc" to "Acesso completo à academia",
        "profileGoalLabel" to "Objetivo", "profileExperienceLabel" to "Experiência", "profileEquipmentLabel" to "Equipamento",
        "trainingFrequency" to "Frequência de treino", "sessionsPerWeek" to "Sessões por semana",
        "selectTrainingDays" to "Selecione seus dias de treino",
        "monday" to "Segunda-feira", "tuesday" to "Terça-feira", "wednesday" to "Quarta-feira", "thursday" to "Quinta-feira",
        "friday" to "Sexta-feira", "saturday" to "Sábado", "sunday" to "Domingo",
        "physicalLimitations" to "Limitações físicas ou lesões?",
        "physicalLimitationsPlaceholder" to "ex. dor no joelho, problemas nas costas (ou deixe vazio)",
        "whichMuscleGroups" to "Quais grupos musculares?", "selectAtLeastOne" to "Passo 7 de 7 - selecione pelo menos um",
        "next" to "Próximo", "skip" to "Pular",
        "next" to "Próximo", "skip" to "Pular", "finish" to "Finalizar", "back" to "Voltar",
        "profileSetup" to "Configurar Perfil", "enterName" to "Digite seu nome",
        "pickPhoto" to "Escolher foto", "saveProfile" to "Salvar Perfil", "chest" to "Peito",
        "shoulders" to "Ombros",             "arms" to "Braços", "biceps" to "Bíceps", "triceps" to "Tríceps",
        "legs" to "Pernas", "thighs" to "Coxas", "glutes" to "Glúteos", "calves" to "Panturrilhas",
        "core" to "Core",
        "cardio" to "Cardio", "fullBody" to "Full Body", "pleaseSelectOption" to "Por favor, selecione uma opção", "sets" to "Séries", "reps" to "Repetições", "weight" to "Peso",
        "startWorkout" to "Iniciar Treino", "nextExercise" to "Próximo exercício", "notes" to "Notas", "cancel" to "Cancelar",
        "confirm" to "Confirmar", "delete" to "Excluir", "edit" to "Editar", "search" to "Pesquisar",
        "noDataYet" to "Ainda sem dados", 
        "sendRequest" to "Enviar Solicitação", "accept" to "Aceitar", "reject" to "Rejeitar",
        "removeFriend" to "Remover Amigo", "noFriends" to "Ainda sem amigos",
        "searchUsers" to "Pesquisar Usuários", 
        "searchByNameOrId" to "Pesquisar por nome ou ID",
        "incomingRequests" to "Solicitações Recebidas", 
        "yourFriends" to "Seus Amigos", 
        "friendRequestSent" to "Solicitação Enviada", 
        "feedEmpty" to "Feed está vazio", 
         "workoutCompleted" to "Treino Concluído!",
        "streakLabel" to "Sequência Atual", "bestStreak" to "Melhor Sequência",
        "badges" to "Distintivos",  
        "kg" to "kg", "lbs" to "lbs", 
        "xp" to "XP",
        "max" to "Máx", "ok" to "OK", "lv" to "NV",
        "exportCsv" to "Exportar CSV", "importCsv" to "Importar CSV",
        "subscription" to "Assinatura", "premium" to "Premium", 
         "subscribe" to "Assinar", 
          "error" to "Erro",
        "retry" to "Tentar Novamente", 
         "recoveryInfo" to "Informações de Recuperação",
        "recommendedRecovery" to "Recuperação Recomendada",
         "progressChart" to "Gráfico de Progresso",
        "weightProgression" to "Progressão de Peso", 
         "calendarView" to "Visão de Calendário",
        "allGroups" to "Todos", 
        "friendRequestNotificationTitle" to "Solicitação de Amizade",
        "friendRequestNotificationText" to "enviou uma solicitação de amizade!",
         "selectLanguage" to "Selecionar Idioma",
        "workoutsLabel" to "Treinos", 
        "currentStreakLabel" to "Sequência Atual", "bestStreakLabel" to "Melhor Sequência",
         "days" to "dias",  
          "add" to "Adicionar", "demoExercise" to "EXERCÍCIO DEMO",  "prAndVolume" to "Recordes e volume", "start" to "Iniciar",  "noSavedSetsYet" to "Nenhuma série salva ainda.", "editSet" to "Editar série",  "exercises" to "exercícios", "recovered" to "Recuperado", "almostRecovered" to "Quase recuperado", "moderate" to "Moderado", "tired" to "Cansado", "exhausted" to "Exausto", "fatigue" to "fadiga", "chooseMuscleGroup" to "Escolher grupo muscular",  "noExercisesFound" to "Nenhum exercício encontrado", "tryDifferentFilter" to "Tente outro filtro ou pesquise por nome", "voiceSearch" to "Pesquisa por voz",  "voiceSearchError" to "Não foi possível reconhecer a voz",
        "monthlyProgress" to "Progresso mensal", "completeWorkoutsToSee" to "Complete treinos para ver o progresso", "jan" to "Jan", "feb" to "Fev", "mar" to "Mar", "apr" to "Abr", "may" to "Mai", "jun" to "Jun", "jul" to "Jul", "aug" to "Ago", "sep" to "Set", "oct" to "Out", "nov" to "Nov", "dec" to "Dez", "monthlyDetails" to "Detalhes mensais", "month" to "Mês", "mon" to "Seg", "tue" to "Ter", "wed" to "Qua", "thu" to "Qui", "fri" to "Sex", "sat" to "Sáb", "sun" to "Dom", "noWorkouts" to "Nenhum treino neste dia", 
         "subscribeNow" to "Assinar agora", "premiumFeature" to "Recurso Premium", "subscribersOnly" to "\$feature está disponível apenas para assinantes", "choosePlan" to "Escolher plano", "youAreSubscribed" to "Você está assinado!", "muscleRecovery" to "Recuperação muscular",  "waterReminderTitle" to "Hora de beber água!", "waterReminderText" to "Mantenha-se hidratado! É hora de beber um copo de água.",   "selectTime" to "Selecionar hora", "forearms" to "Antebraços", "neckAndTraps" to "Pescoço & Trapézios", "welcome" to "Bem-vindo", "athlete" to "Atleta",
        "biometricTracking" to "Rastreamento Biométrico",  "addMeasurement" to "Adicionar medição", "bodyFat" to "Gordura corporal", "waistCirc" to "Cintura", "hipsCirc" to "Quadril", "thighsCirc" to "Coxas", "chestCirc" to "Peito", "armsCirc" to "Braços",  "noMeasurements" to "Sem medições ainda",    "weeksAgo" to "semanas atrás", "cm" to "cm", "percent" to "%", "deleteMeasurement" to "Excluir medição", "biometricHistory" to "Histórico de medições", "weightChart" to "Gráfico de peso", "bodyFatChart" to "Gráfico de gordura", "circumferenceChart" to "Gráfico de circunferências",   "biometricReminderTitle" to "Hora das medições!", "biometricReminderText" to "Não esqueça de registrar suas medições corporais semanais.",  "streakChannelName" to "Lembretes de sequência", "streakReminderTitle" to "Não quebre sua sequência!", "streakReminderText" to "Treine hoje para manter sua sequência!",  "welcomeSoundLabel" to "Som de boas-vindas",
        "foodJournal" to "Diário Alimentar",    "scan" to "Escanear", "scanning" to "Escaneando...", "scanBarcodeHelp" to "Certifique-se de que o Google Play Services está instalado e atualizado", "noFoodEntries" to "Nenhuma entrada de alimentos ainda", "todaysMacros" to "Macros de Hoje", "stepsLabel" to "Passos", "activeTimeLabel" to "Tempo ativo", "caloriesLabel" to "Calorias", "proteinLabel" to "Proteínas", "carbsLabel" to "Carbos", "fatLabel" to "Gorduras", "breakfast" to "Café da manhã", "lunch" to "Almoço", "dinner" to "Jantar", "snack" to "Lanche", "drinks" to "Bebidas", "selectMealType" to "Selecionar tipo de refeição",  "foodName" to "Nome do alimento", "brandLabel" to "Marca",     "fiber" to "Fibras", "searchFood" to "Pesquisar alimento", "foodSearchHint" to "Ex: ovo, frango, arroz", "quantity" to "Quantidade", "gramsShort" to "g", "piecesShort" to "uni", "addToJournal" to "Adicionar ao diário", "manualEntryMode" to "Entrada manual", "noFoodFound" to "Alimento não encontrado na lista", "enterManually" to "Inserir alimento manualmente", "per100g" to "por 100g", "perPiece" to "por unidade",
        "aiTrainer" to "Treinador IA", "aiTrainerWelcome" to "Olá! Sou seu treinador IA", "aiTrainerHint" to "Pergunte sobre treino, nutrição ou progresso", "aiTrainerHistory" to "Histórico de chats", "noHistoryYet" to "Sem histórico ainda", "current" to "Atual", "askAiTrainer" to "Perguntar ao treinador...", "aiSuggestion1" to "Que treino você recomenda hoje?", "aiSuggestion2" to "Como posso aumentar o volume?", "aiSuggestion3" to "Preciso de um dia de descanso?", "aiSuggestion4" to "Como sair de um platô?",
         "deleteAccount" to "Excluir Conta",
        "exerciseHistory" to "Histórico",  
        "favorite" to "Favorito",  "savedExercises" to "Exercícios salvos", "noFavorites" to "Nenhum exercício salvo ainda", "tapStarToSave" to "Toque na estrela para salvar um exercício", "removeFavorite" to "Remover dos favoritos", 
        "addSet" to "Adicionar série", "exerciseNotes" to "Notas", 
        "saveNotes" to "Salvar", 
        "volume" to "Volume", "maxWeight" to "Peso máx", "maxReps" to "Reps máx", "maxSet" to "Série máx",
        "today" to "Hoje", "thisWeek" to "Esta semana", "thisMonth" to "Este mês",
        "totalVolumeLabel" to "Volume total",
        "guest" to "Convidado", "loginWithGoogle" to "Entrar com Google", "loginWithFacebook" to "Entrar com Facebook",
        "close" to "Fechar",  "profile" to "Perfil",
        "appTagline" to "Treine. Progrida. Repita.", "or" to "ou", "dark" to "Escuro", "light" to "Claro",
        "system" to "Sistema",  
        "selectTheme" to "Selecionar tema", "settingsAndMore" to "Configurações e mais",
        "muscleGroups" to "Grupos musculares",  "features" to "Funcionalidades", "activity" to "Atividade", "tools" to "Ferramentas", 
        "englishUS" to "Inglês", "romana" to "Romeno", "russkiy" to "Russo", "ukrainska" to "Ucraniano",
        "francais" to "Francês", "deutsch" to "Alemão", "espanol" to "Espanhol",
        "italiano" to "Italiano", "turkce" to "Turco", "portugues" to "Português", "polski" to "Polonês",
        "motto1" to "Cada repetição conta.", "motto2" to "Mais forte que ontem.",
        "motto3" to "Seu corpo, suas regras.", "motto4" to "Supere seus limites.",
        "motto5" to "Consistência supera talento.", "motto6" to "Disciplina é liberdade.",
        "motto7" to "Sem atalhos.", "motto8" to "Ganho, não dado.",
        "motto9" to "Não pare quando estiver cansado. Pare quando terminar.", "motto10" to "A dor de hoje torna-se a força de amanhã.",
        "motto11" to "Grandes coisas nunca nascem na zona de conforto.", "motto12" to "Nunca fica mais fácil. Você é que fica mais forte.",
        "motto13" to "Não precisa ser extremo, apenas consistente.", "motto14" to "Seu único limite é você.",
        "motto15" to "Todo especialista já foi um iniciante.", "motto16" to "O corpo alcança o que a mente acredita.",
        "motto17" to "O descanso faz parte do processo, não é o inimigo.", "motto18" to "Pequenos passos todos os dias levam a grandes resultados.",
        "motto19" to "Treine como se estivesse com fome. Mantenha-se humilde.", "motto20" to "O sucesso é a soma de pequenos esforços repetidos diariamente.",
        "goodMorning" to "Bom dia", "goodAfternoon" to "Boa tarde", "goodEvening" to "Boa noite",
        "daysConsecutive" to "dias consecutivos", "todaysWorkout" to "Treino de hoje",
        "todayYouRest" to "Hoje descansas", "restDayMessage" to "O descanso é essencial para a recuperação muscular. Aproveita para recarregar e te preparares para o próximo treino.",
        "restDayTip" to "Um leve alongamento ou caminhada podem ajudar a manter a circulação.",
        "dayLabel" to "Dia", "ofCycle" to "do ciclo",
        "howDoYouFeel" to "Como se sente?", "tiredLabel" to "Cansado", "normalLabel" to "Normal", "energeticLabel" to "Energético",
         "technicalTip" to "Dica técnica",
        "weeklySummary" to "Resumo semanal", "lastWeekLabel" to "semana passada",
        "goalLabel" to "Dica de objetivo", "volumeLabel" to "Volume", "topExerciseLabel" to "Top exercício",
        "nutritionLabel" to "Nutrição", "motivationLabel" to "Motivação",
        "gpsCardioMap" to "Cardio", "startTracking" to "Iniciar rastreamento", 
        "pauseTracking" to "Pausar", "resumeTracking" to "Retomar",
        "distance" to "Distância", "pace" to "Ritmo", "speed" to "Velocidade", "duration" to "Duração",
        "savedRoutes" to "Rotas salvas", "noSavedRoutes" to "Nenhuma rota salva",
        "routeName" to "Nome da rota", "saveRoute" to "Salvar rota", "deleteRoute" to "Excluir rota",
        "locationPermissionRequired" to "Permissão de localização necessária",
        "restDaysTitle" to "Dias de descanso e descarga", "restDaysSubtitle" to "Agendamento automático recuperação, alongamento, yoga leve",
        "deloadWeek" to "Semana de descarga", "recoverySchedule" to "Programa de recuperação",
        "stretching" to "Alongamento", "lightYoga" to "Yoga leve", "foamRolling" to "Rolo de espuma",
         "nextRestDay" to "Próximo dia de descanso",
        "muscleNeedsRest" to "Os músculos precisam de descanso", 
        "deloadInfo" to "Info de descarga", "suggestedActivities" to "Atividades sugeridas",
        "activeRecovery" to "Recuperação ativa", "lightWalk" to "Caminhada leve",
        "save" to "Salvar",
        "allGood" to "Tudo bem", "alreadyHaveAccount" to "Já tem uma conta?", "autoDeloadEnabled" to "Descarga automática ativada",
        "avgRecovery" to "Recuperação média", "caloriesBurned" to "Calorias queimadas", "confirmPassword" to "Confirmar senha",
        "createAccountTitle" to "Criar conta", "deloadActive" to "Descarga ativa", "deloadHistory" to "Histórico de descargas",
        "deloadInterval" to "Intervalo de descarga", "deloadNewValue" to "Novo valor", "deloadNormalValue" to "Valor normal",
        "deloadPreview" to "Pré-visualização da descarga", "deloadPreviewSubtitle" to "Ver plano reduzido para a próxima descarga", "deloadActiveThisWeek" to "Descarga ativa esta semana",         "recommendedForYou" to "Recomendado para você", "tapToSchedule" to "Toque para agendar", "dontHaveAccount" to "Não tem conta?", "emailError" to "Email inválido",
        "endDeload" to "Finalizar descarga", "foamRollingDescription" to "Liberte a tensão muscular com rolo de espuma",
        "heightCm" to "Altura (cm)", "lissDescription" to "Cardio leve para recuperação ativa",
        "loginInstead" to "Entrar em vez disso", "musclesTiredCount" to "músculos cansados",
        "nameError" to "O nome é obrigatório", "nameField" to "Nome", "optional" to "Opcional",
        "passwordError" to "A senha deve ter pelo menos 6 caracteres", "passwordMismatch" to "As senhas não coincidem",
        "passwordStrengthMedium" to "Média", "passwordStrengthStrong" to "Forte", "passwordStrengthWeak" to "Fraca",
        "privacyPolicyLink" to "Política de Privacidade", "recoveryTargeted" to "Recuperação direcionada",
         "startDeload" to "Iniciar descarga", "stretchingDescription" to "Melhore a flexibilidade e mobilidade", "warmupStretch" to "Aquecimento", "cooldownStretch" to "Resfriamento",
        "termsAndConditions" to "Termos e Condições", "termsPrefix" to "Ao continuar, você concorda com nossos",
        "timeForDeload" to "Hora da descarga", "weeks" to "semanas",
        "deloadReasonAuto" to "Hora da descarga",
        "deloadReasonGeneral" to "Descarga geral",
        "deloadDueBanner" to "Reduza seu volume esta semana para se recuperar totalmente",
        "dailyIntake" to "Ingestão diária",
        "bio" to "Bio",
        "currentPassword" to "Senha atual",
        "newPassword" to "Nova senha",
        "confirmNewPassword" to "Confirmar nova senha",
        "changeLabel" to "Alterar",
        "currentPasswordRequired" to "A senha atual é obrigatória",
        "passwordTooShort" to "A nova senha deve ter pelo menos 6 caracteres",
        "enterPasswordToConfirm" to "Digite sua senha para confirmar",
        "passwordRequiredToDelete" to "A senha é obrigatória para excluir a conta",
        "lowLabel" to "Baixo",
        "highLabel" to "Alto",
        "newChat" to "Novo chat",
        "serverSettings" to "Configurações do servidor",
        "deloadWhyTitle" to "Por que a descarga?",
        "deloadWhyBody" to "Treinar pesado por %d semanas seguidas acumula fadiga. Uma semana de descarga reduz o volume para que os músculos se recuperem totalmente e você volte mais forte.",
        "deloadDuration" to "Duração",
        "deload1Week" to "1 semana",
        "deload2Weeks" to "2 semanas",
        "deloadReduction" to "Redução",
        "deloadCompound" to "CMP",
        "deloadNoHistory" to "Ainda não há descargas. Quando você completar uma semana de descarga, ela aparecerá aqui com seu progresso de recuperação.",
        "editRestDay" to "Editar dia de descanso",
        "deloadDayOf" to "Dia %d de %d",
        "gpsSearching" to "Procurando sinal GPS...",
        "gpsError" to "Erro de GPS",
        "running" to "Corrida",
        "cycling" to "Ciclismo",
        "walking" to "Caminhada",
        "routePoints" to "Pontos",
        "paused" to "Em pausa", "estimatedOneRm" to "1RM estimado", "nextSetSuggestion" to "Próxima série", "setTypeWarmup" to "Aquecimento", "setTypeWorking" to "Trabalho", "setTypeDrop" to "Drop", "setTypeAmrap" to "AMRAP", "setTypePaused" to "Pausa", "setTypeTempo" to "Tempo", "rpeLabel" to "RPE",
        "steps" to "passos",
        "timerFinished" to "Tempo esgotado!",
        "timeToStartNextSet" to "Pronto para a próxima série!",
        "weeksSinceLastDeload" to "Semanas desde a última descarga", "weightKg" to "Peso (kg)",
        "yogaDescription" to "Relaxe e melhore a mobilidade com yoga leve",  
        "gpsDisabledTitle" to "GPS desativado",
        "gpsDisabledMessage" to "Ative o GPS nas configurações do seu telefone para rastrear sua rota em tempo real.",
        "openSettings" to "Abrir configurações",
         "weightKgLabel" to "Peso (kg)", "repsLabel" to "Repetições",
        "estimated1rm" to "1RM estimado", "intensityZones" to "Zonas de intensidade",
        "zoneStrength" to "Força (1-2 reps)", "zoneStrengthHypertrophy" to "Força-Hipertrofia (3-5 reps)",
        "zoneHypertrophy" to "Hipertrofia (6-8 reps)", "zoneHypertrophyEndurance" to "Hipertrofia-Resistência (10-12 reps)",
        "zoneEndurance" to "Resistência (15+ reps)", 
        "totalVolumeLabel" to "Volume total", "muscleGroups" to "Grupos musculares", "weeklyTab" to "Semanal",
         "pbsTab" to "Recordes",
        "keepTraining" to "Continue treinando para superar seus recordes!",
        "plateCalculatorTitle" to "Calculadora de discos", "targetWeightLabel" to "Peso alvo",
        "menu" to "Menu",
        "foodNamePlaceholder" to "Ex.: peito de frango", "brandPlaceholder" to "Ex.: Farm Foods", "backendServerAddress" to "Endereço do servidor backend:", "aiApiKeyOptional" to "Chave da API do AI Trainer (opcional):", "leaveEmptyForDefaultServer" to "Deixe vazio para o URL padrão do servidor. A chave da API só é necessária se o servidor tiver autenticação ativada.", "kcal" to "KCAL", "deleteAccountWarning" to "Esta ação é permanente e não pode ser desfeita. Todos os seus dados serão excluídos.", "setStepGoal" to "Definir meta de passos", "enterDailyStepGoal" to "Insira sua meta diária de passos",
        "history" to "Histórico", "deleteChat" to "Excluir chat", "bodyAnatomy" to "Anatomia do corpo", "leaveEmptyIfAuthDisabled" to "Deixe vazio se a autenticação estiver desativada", "floatingWindow" to "Janela flutuante", "float" to "Flutuante", "clear" to "Limpar", "decrease" to "Diminuir", "increase" to "Aumentar", "exercise" to "Exercício", "top" to "TOP", "play" to "Reproduzir", "reset" to "Redefinir", "selected" to "Selecionado", "loadingPlaylists" to "Carregando playlists...", "failedToLoadPlaylists" to "Falha ao carregar playlists", "noPlaylistsFound" to "Nenhuma playlist encontrada", "createPlaylistFirst" to "Crie primeiro uma playlist no Spotify", "authenticationFailed" to "Falha na autenticação", "unknownError" to "Erro desconhecido", "selectPlaylist" to "Selecionar playlist", "choosePlaylist" to "Escolha uma playlist para o seu treino", "tracks" to "faixas", "connectToSpotify" to "Conectar ao Spotify", "spotifyAccessDescription" to "Acesse suas playlists e crie a trilha sonora perfeita para o seu treino", "loginWithSpotify" to "Entrar com Spotify", "spotifyRedirectInfo" to "Você será redirecionado ao Spotify para autorizar", "navy" to "Navy", "bmi" to "IMC", "exportBackup" to "Exportar backup",
        "barWeightLabel" to "Peso da barra", "lbsKg" to "lbs/kg",
        "platesPerSide" to "Discos por lado", "plateUnit" to "disco(s)",
        "eachSide" to "por lado", "total" to "Total",
        "weightTooLight" to "Peso muito leve para discos (só a barra:",
        "plateCalcNote" to "A calculadora de discos calcula automaticamente quais discos adicionar à barra.",
        "howToGet" to "Como obter:",
        "gender" to "Gênero", "age" to "Idade", "method" to "Método", "waistCm" to "Cintura (cm)", "neckCm" to "Pescoço (cm)", "hipsCm" to "Quadris (cm)", "estimatedBodyFat" to "Gordura corporal estimada", "navyMethodInfo" to "Método Navy: usa fita métrica para circunferências",  "bodyFatCalculator" to "Calculadora de gordura corporal", "newPRs" to "Novos Recordes", "exerciseBreakdown" to "Detalhes dos exercícios", "done" to "Pronto", "weightGoal" to "Meta de peso", "currentWeight" to "Peso atual", "target" to "Meta", "deadline" to "Prazo", "goalDetails" to "Detalhes da meta", "startWeight" to "Peso inicial", "targetWeight" to "Peso alvo",  "noActiveGoal" to "Nenhuma meta ativa", "setGoalToTrack" to "Defina uma meta para acompanhar seu progresso",         "setGoal" to "Definir meta", "pastGoals" to "Metas anteriores", "calculate" to "Calcular",
        "weightEvolution" to "Evolução do peso", "measurements" to "medições", "startedOn" to "Iniciado em", "editGoal" to "Editar meta",
        "equipDumbbells" to "Halteres", "equipBarbell" to "Barra", "equipMachine" to "Máquina", "equipCable" to "Cabo", "equipBodyweight" to "Peso corporal", "equipEZBar" to "Barra EZ", "equipSmithMachine" to "Máquina Smith", "equipKettlebell" to "Kettlebell", "equipStabilityBall" to "Bola de estabilidade", "equipSledMachine" to "Trenó", "equipBand" to "Faixa",
        "energizeLabel" to "Energizar",
        "performLabel" to "Desempenho",
        "pushItLabel" to "Vai",
        "openSpotifyLabel" to "Abrir Spotify",
        "tapToPlayLabel" to "Toque para tocar no Spotify",
        "startingWorkoutLabel" to "Iniciando música de treino...",
        "signUpSuccessMessage" to "Conta criada! Por favor, faça login.",
        "trainingSectionLabel" to "Treino", "frequencyLabel" to "Frequência", "xPerWeek" to "x / sem", "editProfile" to "Editar perfil", "memberSince" to "Membro desde", "changePassword" to "Alterar senha",
        "updateTitle" to "Nova versão disponível", "updateMessage" to "Kinetic %s foi lançada. Você tem instalada a versão v%s.\n\nToque para baixar o novo APK.", "updateDownload" to "Baixar", "updateLater" to "Mais tarde",
        "activeDesc" to "Treino + atividade diária",
        "permanentPlan" to "Plano vitalício",
        "vsPrevious" to "vs anterior",
        "free" to "Grátis",
        "lifetimeAccess" to "Acesso vitalício",
        "male" to "Masculino",
        "purchaseFailed" to "Falha na compra",
        "perMonth" to "/mês",
        "veryActiveDesc" to "Treino intenso + trabalho físico",
        "restorePurchase" to "Restaurar compras",
        "freePlan" to "Grátis",
        "veryActive" to "Muito ativo",
        "whatsYourAge" to "Quantos anos você tem?",
        "sedentary" to "Sedentário",
        "unlockedForMinutes" to "Desbloqueado: %s restante",
        "watchAdToUnlock" to "Assista a um anúncio (desbloqueia 30 min)",
        "remaining" to "restante",
        "whatsYourGender" to "Qual é o seu gênero?",
        "restoreSuccess" to "Compras restauradas",
        "buyNow" to "Comprar",
        "bestValue" to "Melhor valor",
        "noPurchasesToRestore" to "Nenhuma compra para restaurar",
        "sessions" to "sessões",
        "active" to "Ativo",
        "allExercises" to "Todos os exercícios",
        "saveExercise" to "Salvar exercício",
        "perYear" to "/ano",
        "mostPopular" to "Mais popular",
        "unlockPremiumSubtitle" to "Tenha acesso a todos os recursos avançados",
        "female" to "Feminino",
        "purchaseSuccess" to "Compra realizada! Bem-vindo ao Premium.",
        "upgradeToUnlock" to "Atualize para desbloquear",
        "dailyAdLimitReached" to "Você atingiu o limite diário de desbloqueios",
        "sedentaryDesc" to "Trabalho de escritório, pouca atividade",
        "adUnlockSuccess" to "Recurso desbloqueado por 30 minutos!",
        "cancelAnytime" to "Cancele quando quiser no Google Play",
        "purchaseCancelled" to "Compra cancelada",
        "currentPlan" to "Plano atual",
        "adNotReady" to "Anúncio não está pronto. Tente novamente.",
        "whatsYourActivityLevel" to "Qual é o seu nível de atividade?",
        "workoutAnalytics" to "Análise de treino",
        "mostTrained" to "Mais treinado",
        "unlockPremiumTitle" to "Desbloqueie o Kinetic Premium",
        "oneTimePayment" to "pagamento único",
        "goalComplete" to "Meta atingida!", "waterStreak" to "Série de hidratação", "ofGoal" to "da meta", "editWaterGoal" to "Editar meta de água", "newWaterGoal" to "Nova meta (ml)",
        "undo" to "Desfazer",
        "workoutReminderTitle" to "Treino de Hoje",
        "workoutReminderBody" to "Hora de construir força. Concentre-se em __GROUPS__ hoje. Dê o máximo em cada série e supere seus recordes pessoais.",
        "workoutReminderText" to "É dia de treino! Se prepare!",
        "workoutChannelName" to "Lembretes de Treino",
        "weeklySummaryTitle" to "Resumo Semanal",
        "weeklySummaryText" to "Você treinou __COUNT__ vezes esta semana! Continue!",
        "weeklySummaryChannelName" to "Resumo Semanal",
        "streakReminderTitle" to "Não quebre sua sequência!",
        "streakReminderText" to "Treine hoje para manter sua sequência de __STREAK__ dias!",
        "streakChannelName" to "Lembretes de Sequência",
        "goalProgressTitle" to "Progresso da Meta de Passos",
        "goalProgressText" to "Você atingiu __PERCENT__% da sua meta de passos! (__CURRENT__/__GOAL__)",
        "goalProgressChannelName" to "Progresso de Metas",
        "achievementTitle" to "Conquista Desbloqueada!",
        "achievementText" to "Parabéns! Você desbloqueou uma nova insígnia!",
        "achievementChannelName" to "Conquistas"
    ))

    private fun createPl() = Strings(enRaw() + mapOf(
         "stop" to "Zatrzymaj", "openApp" to "Otwórz aplikację", "goal" to "Cel", "stepGoalChannel" to "Cel kroków", "stepGoalTitle" to "🏆 Cel kroków osiągnięty!", "stepGoalText" to "Gratulacje! Osiągnąłeś %d kroków!", "stepGoalBig" to "Gratulacje! Osiągnąłeś swój cel %d kroków!", "stepGoalKeepGoing" to "Tak trzymaj!", "gpsChannelName" to "Śledzenie GPS", "waterChannelName" to "Przypomnienia o wodzie", "biometricChannelName" to "Przypomnienia o pomiarach", "friendChannelName" to "Prośby o znajomość", "oneRmCalculator" to "Kalkulator 1RM", "plusGoal" to "+ Cel", "tierFree" to "DARMOWE", "tierPro" to "PRO", "tierProPlus" to "PRO+", "tierLifetime" to "DOŻYWOTNI",
         "dashboard" to "Panel",  "acasa" to "Strona Główna", "workouts" to "Treningi", "stats" to "Statystyki", "waterIntake" to "Spożycie wody", "waterGoal" to "Cel wody", "addWater" to "Dodaj wodę",  "height" to "Wzrost", "personalInfo" to "Informacje osobiste",  "ml" to "ml", "templates" to "Szablony",
              "everyDay" to "Codziennie", "reminder" to "Przypomnienie",
            "weeklyHistory" to "Historia tygodniowa", "tips" to "Wskazówki", "customMl" to "Własne ml", "average" to "Średnia", "target" to "Cel",
            "waterTip1" to "Pij 250ml co 30 min podczas treningu.", "waterTip2" to "Rano: 500ml po przebudzeniu dla aktywnego metabolizmu.", 
        "recovery" to "Regeneracja",   "friends" to "Znajomi",
        "leaderboard" to "Ranking", "all" to "Wszyscy",  "language" to "Język",
        "units" to "Jednostki", "logout" to "Wyloguj", "login" to "Zaloguj się",
        "signUp" to "Zarejestruj się", "email" to "E-mail", "password" to "Hasło",
        "forgotPassword" to "Zapomniałeś hasła?", 
        "loginAsGuest" to "Zaloguj jako gość", 
         "goalStrength" to "Siła", "goalMass" to "Masa mięśniowa",
        "goalWeightLoss" to "Redukcja", "goalMaintenance" to "Utrzymanie",
        "selectGoal" to "Wybierz swój cel",
        "stepOf" to "Krok %d z 5", "whatsYourExperience" to "Jaki jest Twój poziom doświadczenia?",
        "beginnerLabel" to "Początkujący", "beginnerDesc" to "0-1 lat treningu",
        "intermediateLabel" to "Średniozaawansowany", "intermediateDesc" to "1-3 lata regularnego treningu",
        "advancedLabel" to "Zaawansowany", "advancedDesc" to "3+ lat poważnego treningu",
        "whatEquipment" to "Jakie masz wyposażenie?",
        "homeNoEquip" to "W domu - Bez wyposażenia", "homeNoEquipDesc" to "Tylko ćwiczenia z ciężarem ciała",
        "homeDumbbells" to "W domu - Hantle/Pasma", "homeDumbbellsDesc" to "Podstawowe wyposażenie domowe",
        "fullGym" to "Pełna siłownia", "fullGymDesc" to "Pełny dostęp do siłowni",
        "profileGoalLabel" to "Cel", "profileExperienceLabel" to "Doświadczenie", "profileEquipmentLabel" to "Sprzęt",
        "trainingFrequency" to "Częstotliwość treningów", "sessionsPerWeek" to "Sesji tygodniowo",
        "selectTrainingDays" to "Wybierz dni treningowe",
        "monday" to "Poniedziałek", "tuesday" to "Wtorek", "wednesday" to "Środa", "thursday" to "Czwartek",
        "friday" to "Piątek", "saturday" to "Sobota", "sunday" to "Niedziela",
        "physicalLimitations" to "Ograniczenia fizyczne lub kontuzje?",
        "physicalLimitationsPlaceholder" to "np. ból kolana, problemy z plecami (lub zostaw puste)",
        "whichMuscleGroups" to "Jakie grupy mięśniowe?", "selectAtLeastOne" to "Krok 7 z 7 - wybierz przynajmniej jedną",
        "next" to "Dalej", "skip" to "Pomiń", "finish" to "Zakończ",
        "back" to "Wstecz", "profileSetup" to "Ustawienia profilu", "enterName" to "Wprowadź imię",
        "pickPhoto" to "Wybierz zdjęcie", "saveProfile" to "Zapisz profil", "chest" to "Klatka piersiowa",
        "shoulders" to "Barki",             "arms" to "Ramiona", "biceps" to "Bicepsy", "triceps" to "Tricepsy",
        "legs" to "Nogi", "thighs" to "Uda", "glutes" to "Pośladki", "calves" to "Łydki",
        "core" to "Core",
        "cardio" to "Cardio", "fullBody" to "Full Body", "pleaseSelectOption" to "Proszę wybrać opcję", "sets" to "Serie", "reps" to "Powtórzenia", "weight" to "Ciężar",
        "startWorkout" to "Rozpocznij trening", "nextExercise" to "Następne ćwiczenie", "notes" to "Notatki", "cancel" to "Anuluj",
        "confirm" to "Potwierdź", "delete" to "Usuń", "edit" to "Edytuj", "search" to "Szukaj",
        "noDataYet" to "Brak danych", 
        "sendRequest" to "Wyślij zaproszenie", "accept" to "Akceptuj", "reject" to "Odrzuć",
        "removeFriend" to "Usuń znajomego", "noFriends" to "Brak znajomych",
        "searchUsers" to "Szukaj użytkowników", 
        "searchByNameOrId" to "Szukaj po nazwie lub ID",
        "incomingRequests" to "Otrzymane zaproszenia",
         "yourFriends" to "Twoi znajomi",
        "friendRequestSent" to "Zaproszenie wysłane", 
        "feedEmpty" to "Feed jest pusty", 
         "workoutCompleted" to "Trening ukończony!",
        "streakLabel" to "Aktualna seria", "bestStreak" to "Najlepsza seria", "badges" to "Odznaki",
          "kg" to "kg", "lbs" to "lbs",
          "xp" to "PD",
          "max" to "Maks", "ok" to "OK", "lv" to "PO",
         "exportCsv" to "Eksportuj CSV",
        "importCsv" to "Importuj CSV", "subscription" to "Subskrypcja", "premium" to "Premium",
        "subscribe" to "Subskrybuj", 
         "error" to "Błąd", "retry" to "Ponów",
         "recoveryInfo" to "Informacje o regeneracji",
        "recommendedRecovery" to "Zalecana regeneracja",
         "progressChart" to "Wykres postępu",
         "weightProgression" to "Progresja ciężaru",
        "calendarView" to "Widok kalendarza", 
         "allGroups" to "Wszystkie",
        "friendRequestNotificationTitle" to "Zaproszenie do znajomych",
        "friendRequestNotificationText" to "wysłał(a) Ci zaproszenie do znajomych!",
         "selectLanguage" to "Wybierz język",
        "workoutsLabel" to "Treningi", 
        "currentStreakLabel" to "Aktualna seria", "bestStreakLabel" to "Najlepsza seria",
         "days" to "dni",  
          "add" to "Dodaj", "demoExercise" to "ĆWICZENIE DEMO",  "prAndVolume" to "Rekordy i objętość", "start" to "Start",  "noSavedSetsYet" to "Brak zapisanych serii.", "editSet" to "Edytuj serię",  "exercises" to "ćwiczeń", "recovered" to "Wypoczęty", "almostRecovered" to "Prawie wypoczęty", "moderate" to "Umiarkowany", "tired" to "Zmęczony", "exhausted" to "Wyczerpany", "fatigue" to "zmęczenie", "chooseMuscleGroup" to "Wybierz grupę mięśniową",  "noExercisesFound" to "Nie znaleziono ćwiczeń", "tryDifferentFilter" to "Spróbuj innego filtru lub wyszukaj po nazwie", "voiceSearch" to "Wyszukiwanie głosowe",  "voiceSearchError" to "Nie rozpoznano głosu",
        "monthlyProgress" to "Postęp miesięczny", "completeWorkoutsToSee" to "Ukończ treningi aby zobaczyć postępy", "jan" to "Sty", "feb" to "Lut", "mar" to "Mar", "apr" to "Kwi", "may" to "Maj", "jun" to "Cze", "jul" to "Lip", "aug" to "Sie", "sep" to "Wrz", "oct" to "Paź", "nov" to "Lis", "dec" to "Gru", "monthlyDetails" to "Szczegóły miesięczne", "month" to "Miesiąc", "mon" to "Pon", "tue" to "Wt", "wed" to "Śr", "thu" to "Czw", "fri" to "Pt", "sat" to "Sob", "sun" to "Nd", "noWorkouts" to "Brak treningów w tym dniu", 
         "subscribeNow" to "Subskrybuj teraz", "premiumFeature" to "Funkcja Premium", "subscribersOnly" to "\$feature jest dostępne tylko dla subskrybentów", "choosePlan" to "Wybierz plan", "youAreSubscribed" to "Jesteś subskrybentem!", "muscleRecovery" to "Regeneracja mięśni",  "waterReminderTitle" to "Czas pić wodę!", "waterReminderText" to "Pij wodę! Czas napić się szklanki wody.",   "selectTime" to "Wybierz godzinę", "forearms" to "Przedramiona", "neckAndTraps" to "Szyja & Czworoboczny", "welcome" to "Witaj", "athlete" to "Sportowiec",
        "biometricTracking" to "Monitorowanie biometryczne",  "addMeasurement" to "Dodaj pomiary", "bodyFat" to "Tkanka tłuszczowa", "waistCirc" to "Talia", "hipsCirc" to "Biodra", "thighsCirc" to "Uda", "chestCirc" to "Klatka piersiowa", "armsCirc" to "Ramiona",  "noMeasurements" to "Brak pomiarów",    "weeksAgo" to "tygodnie temu", "cm" to "cm", "percent" to "%", "deleteMeasurement" to "Usuń pomiary", "biometricHistory" to "Historia pomiarów", "weightChart" to "Wykres wagi", "bodyFatChart" to "Wykres tłuszczu", "circumferenceChart" to "Wykres obwodów",   "biometricReminderTitle" to "Czas na pomiary!", "biometricReminderText" to "Nie zapomnij zapisać tygodniowych pomiarów ciała.",  "streakChannelName" to "Przypomnienia o serii", "streakReminderTitle" to "Nie przerywaj serii!", "streakReminderText" to "Trenuj dzisiaj, aby utrzymać serię!",  "welcomeSoundLabel" to "Dźwięk powitania",
        "foodJournal" to "Dziennik żywieniowy",    "scan" to "Skanuj", "scanning" to "Skanowanie...", "scanBarcodeHelp" to "Upewnij się, że Google Play Services jest zainstalowany i zaktualizowany", "noFoodEntries" to "Brak wpisów żywieniowych", "todaysMacros" to "Makro na dziś", "stepsLabel" to "Kroki", "activeTimeLabel" to "Czas aktywności", "caloriesLabel" to "Kalorie", "proteinLabel" to "Białko", "carbsLabel" to "Węgle", "fatLabel" to "Tłuszcze", "breakfast" to "Śniadanie", "lunch" to "Obiad", "dinner" to "Kolacja", "snack" to "Przekąska", "drinks" to "Napoje", "selectMealType" to "Wybierz typ posiłku",  "foodName" to "Nazwa produktu", "brandLabel" to "Marka",     "fiber" to "Błonnik", "searchFood" to "Szukaj produktu", "foodSearchHint" to "Np.: jajko, kurczak, ryż", "quantity" to "Ilość", "gramsShort" to "g", "piecesShort" to "szt", "addToJournal" to "Dodaj do dziennika", "manualEntryMode" to "Wpis ręczny", "noFoodFound" to "Nie znaleziono produktu na liście", "enterManually" to "Wprowadź produkt ręcznie", "per100g" to "na 100g", "perPiece" to "za sztukę",
        "aiTrainer" to "Trener AI", "aiTrainerWelcome" to "Cześć! Jestem twoim trenerem AI", "aiTrainerHint" to "Zapytaj o trening, dietę lub postępy", "aiTrainerHistory" to "Historia czatów", "noHistoryYet" to "Brak historii", "current" to "Aktualny", "askAiTrainer" to "Zapytaj trenera...", "aiSuggestion1" to "Jaki trening polecasz dzisiaj?", "aiSuggestion2" to "Jak zwiększyć objętość?", "aiSuggestion3" to "Czy potrzebuję dnia odpoczynku?", "aiSuggestion4" to "Jak wyjść z plateau?",
         "deleteAccount" to "Usuń konto",
        "exerciseHistory" to "Historia",  
        "favorite" to "Ulubione",  "savedExercises" to "Zapisane ćwiczenia", "noFavorites" to "Brak zapisanych ćwiczeń", "tapStarToSave" to "Dotknij gwiazdki, aby zapisać ćwiczenie", "removeFavorite" to "Usuń z ulubionych", 
        "addSet" to "Dodaj serię", "exerciseNotes" to "Notatki", 
        "saveNotes" to "Zapisz", 
        "volume" to "Objętość", "maxWeight" to "Maks waga", "maxReps" to "Maks powtórzenia", "maxSet" to "Maks seria",
        "today" to "Dziś", "thisWeek" to "Ten tydzień", "thisMonth" to "Ten miesiąc",
        "totalVolumeLabel" to "Łączna objętość",
        "guest" to "Gość", "loginWithGoogle" to "Zaloguj z Google", "loginWithFacebook" to "Zaloguj z Facebook",
        "close" to "Zamknij",  "profile" to "Profil",
        "appTagline" to "Trenuj. Postępuj. Powtarzaj.", "or" to "lub", "dark" to "Ciemny", "light" to "Jasny",
        "system" to "System",  
        "selectTheme" to "Wybierz motyw", "settingsAndMore" to "Ustawienia i więcej",
        "muscleGroups" to "Grupy mięśniowe",  "features" to "Funkcje", "activity" to "Aktywność", "tools" to "Narzędzia", 
        "englishUS" to "Angielski", "romana" to "Rumuński", "russkiy" to "Rosyjski", "ukrainska" to "Ukraiński",
        "francais" to "Francuski", "deutsch" to "Niemiecki", "espanol" to "Hiszpański",
        "italiano" to "Włoski", "turkce" to "Turecki", "portugues" to "Portugalski", "polski" to "Polski",
        "motto1" to "Każde powtórzenie się liczy.", "motto2" to "Silniejszy niż wczoraj.",
        "motto3" to "Twoje ciało, twoje zasady.", "motto4" to "Przekrocz swoje granice.",
        "motto5" to "Wytrwałość pokonuje talent.", "motto6" to "Dyscyplina to wolność.",
        "motto7" to "Bez skrótów.", "motto8" to "Zarobione, nie dane.",
        "motto9" to "Nie przestawaj, gdy jesteś zmęczony. Przestań, gdy skończysz.", "motto10" to "Dzisiejszy ból staje się siłą jutra.",
        "motto11" to "Wielkie rzeczy nigdy nie rodzą się w strefie komfortu.", "motto12" to "Nigdy nie jest łatwiej. To ty stajesz się silniejszy.",
        "motto13" to "Nie musisz być ekstremalny, po prostu konsekwentny.", "motto14" to "Twoim jedynym ograniczeniem jesteś ty.",
        "motto15" to "Każdy ekspert kiedyś był początkującym.", "motto16" to "Ciało osiąga to, w co wierzy umysł.",
        "motto17" to "Odpoczynek jest częścią procesu, a nie wrogiem.", "motto18" to "Małe kroki każdego dnia prowadzą do wielkich rezultatów.",
        "motto19" to "Trenuj, jakbyś był głodny. Pozostań pokorny.", "motto20" to "Sukces to suma małych wysiłków powtarzanych codziennie.",
        "goodMorning" to "Dzień dobry", "goodAfternoon" to "Dzień dobry", "goodEvening" to "Dobry wieczór",
        "daysConsecutive" to "dni z rzędu", "todaysWorkout" to "Dzisiejszy trening",
        "todayYouRest" to "Dzisiaj odpoczywasz", "restDayMessage" to "Odpoczynek jest niezbędny do regeneracji mięśni. Wykorzystaj ten czas na naładowanie baterii i przygotowanie do następnego treningu.",
        "restDayTip" to "Lekki rozciąganie lub spacer mogą pomóc w utrzymaniu krążenia krwi.",
        "dayLabel" to "Dzień", "ofCycle" to "cyklu",
        "howDoYouFeel" to "Jak się czujesz?", "tiredLabel" to "Zmęczony", "normalLabel" to "Normalnie", "energeticLabel" to "Energetyczny",
         "technicalTip" to "Wskazówka techniczna",
        "weeklySummary" to "Podsumowanie tygodnia", "lastWeekLabel" to "poprz. tydzień",
        "goalLabel" to "Wskazówka celu", "volumeLabel" to "Objętość", "topExerciseLabel" to "Top ćwiczenie",
        "nutritionLabel" to "Odżywianie", "motivationLabel" to "Motywacja",
        "gpsCardioMap" to "Cardio", "startTracking" to "Rozpocznij śledzenie", 
        "pauseTracking" to "Pauza", "resumeTracking" to "Wznów",
        "distance" to "Dystans", "pace" to "Tempo", "speed" to "Prędkość", "duration" to "Czas trwania",
        "savedRoutes" to "Zapisane trasy", "noSavedRoutes" to "Brak zapisanych tras",
        "routeName" to "Nazwa trasy", "saveRoute" to "Zapisz trasę", "deleteRoute" to "Usuń trasę",
        "locationPermissionRequired" to "Wymagana zgoda na lokalizację",
        "restDaysTitle" to "Dni odpoczynku i deload", "restDaysSubtitle" to "Automatyczne planowanie regeneracji, rozciągania, jogi",
        "deloadWeek" to "Tydzień deload", "recoverySchedule" to "Harmonogram regeneracji",
        "stretching" to "Rozciąganie", "lightYoga" to "Łagodna joga", "foamRolling" to "Rolowanie",
         "nextRestDay" to "Następny dzień odpoczynku",
        "muscleNeedsRest" to "Mięśnie potrzebują odpoczynku", 
        "deloadInfo" to "Informacje o deload", "suggestedActivities" to "Sugerowane aktywności",
        "activeRecovery" to "Aktywna regeneracja", "lightWalk" to "Lekki spacer",
        "save" to "Zapisz",
        "allGood" to "Wszystko w porządku", "alreadyHaveAccount" to "Masz już konto?", "autoDeloadEnabled" to "Automatyczny deload włączony",
        "avgRecovery" to "Średnia regeneracja", "caloriesBurned" to "Spalone kalorie", "confirmPassword" to "Potwierdź hasło",
        "createAccountTitle" to "Utwórz konto", "deloadActive" to "Deload aktywny", "deloadHistory" to "Historia deloadów",
        "deloadInterval" to "Interwał deloadu", "deloadNewValue" to "Nowa wartość", "deloadNormalValue" to "Wartość normalna",
        "deloadPreview" to "Podgląd deloadu", "deloadPreviewSubtitle" to "Zobacz zmniejszony plan na następny deload", "deloadActiveThisWeek" to "Deload aktywny w tym tygodniu",         "recommendedForYou" to "Polecane dla Ciebie", "tapToSchedule" to "Dotknij, aby zaplanować", "dontHaveAccount" to "Nie masz konta?", "emailError" to "Nieprawidłowy email",
        "endDeload" to "Zakończ deload", "foamRollingDescription" to "Zmień napięcie mięśni za pomocą rollera piankowego",
        "heightCm" to "Wzrost (cm)", "lissDescription" to "Lekkie cardio do aktywnej regeneracji",
        "loginInstead" to "Zaloguj się zamiast tego", "musclesTiredCount" to "mięśni zmęczonych",
        "nameError" to "Imię jest wymagane", "nameField" to "Imię", "optional" to "Opcjonalnie",
        "passwordError" to "Hasło musi mieć co najmniej 6 znaków", "passwordMismatch" to "Hasła nie są zgodne",
        "passwordStrengthMedium" to "Średnie", "passwordStrengthStrong" to "Silne", "passwordStrengthWeak" to "Słabe",
        "privacyPolicyLink" to "Polityka Prywatności", "recoveryTargeted" to "Regeneracja ukierunkowana",
        "startDeload" to "Rozpocznij deload", "stretchingDescription" to "Popraw elastyczność i mobilność", "warmupStretch" to "Rozgrzewka", "cooldownStretch" to "Schładzanie",
        "termsAndConditions" to "Warunki Korzystania", "termsPrefix" to "Kontynuując, zgadzasz się z naszymi",
        "timeForDeload" to "Czas na deload", "weeks" to "tygodni",
        "deloadReasonAuto" to "Czas na deload",
        "deloadReasonGeneral" to "Ogólny deload",
        "deloadDueBanner" to "Zmniejsz objętość w tym tygodniu, aby w pełni się zregenerować",
        "dailyIntake" to "Dzienne spożycie",
        "bio" to "Bio",
        "currentPassword" to "Obecne hasło",
        "newPassword" to "Nowe hasło",
        "confirmNewPassword" to "Potwierdź nowe hasło",
        "changeLabel" to "Zmień",
        "currentPasswordRequired" to "Obecne hasło jest wymagane",
        "passwordTooShort" to "Nowe hasło musi mieć co najmniej 6 znaków",
        "enterPasswordToConfirm" to "Wpisz hasło, aby potwierdzić",
        "passwordRequiredToDelete" to "Hasło jest wymagane do usunięcia konta",
        "lowLabel" to "Niski",
        "highLabel" to "Wysoki",
        "newChat" to "Nowy czat",
        "serverSettings" to "Ustawienia serwera",
        "deloadWhyTitle" to "Po co deload?",
        "deloadWhyBody" to "%d tygodni ciężkich treningów z rzędu kumuluje zmęczenie. Tydzień deloadu obniża objętość, aby mięśnie w pełni się zregenerowały, a ty wróciłeś silniejszy.",
        "deloadDuration" to "Czas trwania",
        "deload1Week" to "1 tydzień",
        "deload2Weeks" to "2 tygodnie",
        "deloadReduction" to "Redukcja",
        "deloadCompound" to "CMP",
        "deloadNoHistory" to "Brak deloadów. Po ukończeniu tygodnia deloadu pojawi się tutaj z postępem regeneracji.",
        "editRestDay" to "Edytuj dzień odpoczynku",
        "deloadDayOf" to "Dzień %d z %d",
        "gpsSearching" to "Szukanie sygnału GPS...",
        "gpsError" to "Błąd GPS",
        "running" to "Bieganie",
        "cycling" to "Jazda na rowerze",
        "walking" to "Chodzenie",
        "routePoints" to "Punkty",
        "paused" to "Wstrzymano", "estimatedOneRm" to "Szac. 1RM", "nextSetSuggestion" to "Następna seria", "setTypeWarmup" to "Rozgrzewka", "setTypeWorking" to "Roboczy", "setTypeDrop" to "Drop", "setTypeAmrap" to "AMRAP", "setTypePaused" to "Pauza", "setTypeTempo" to "Tempo", "rpeLabel" to "RPE",
        "steps" to "kroków",
        "timerFinished" to "Czas minął!",
        "timeToStartNextSet" to "Gotowy na następną serię!",
        "weeksSinceLastDeload" to "Tygodni od ostatniego deloadu", "weightKg" to "Waga (kg)",
        "yogaDescription" to "Zrelaksuj się i popraw mobilność dzięki lekkiej jodze",  
        "gpsDisabledTitle" to "GPS wyłączony",
        "gpsDisabledMessage" to "Włącz GPS w ustawieniach telefonu, aby śledzić trasę w czasie rzeczywistym.",
        "openSettings" to "Otwórz ustawienia",
         "weightKgLabel" to "Ciężar (kg)", "repsLabel" to "Powtórzenia",
        "estimated1rm" to "Szacowane 1RM", "intensityZones" to "Strefy intensywności",
        "zoneStrength" to "Siła (1-2 powt.)", "zoneStrengthHypertrophy" to "Siła-Hipertrofia (3-5 powt.)",
        "zoneHypertrophy" to "Hipertrofia (6-8 powt.)", "zoneHypertrophyEndurance" to "Hipertrofia-Wytrzymałość (10-12 powt.)",
        "zoneEndurance" to "Wytrzymałość (15+ powt.)", 
        "totalVolumeLabel" to "Łączny wolumen", "muscleGroups" to "Grupy mięśniowe", "weeklyTab" to "Tygodniowo",
         "pbsTab" to "Rekordy",
        "keepTraining" to "Kontynuuj trening aby pobić swoje rekordy!",
        "plateCalculatorTitle" to "Kalkulator talerzy", "targetWeightLabel" to "Waga docelowa",
        "menu" to "Menu",
        "foodNamePlaceholder" to "Np. pierś z kurczaka", "brandPlaceholder" to "Np. Farm Foods", "backendServerAddress" to "Adres serwera backend:", "aiApiKeyOptional" to "Klucz API AI Trainer (opcjonalnie):", "leaveEmptyForDefaultServer" to "Zostaw puste, aby użyć domyślnego adresu serwera. Klucz API potrzebny tylko, gdy serwer ma włączone uwierzytelnianie.", "kcal" to "KCAL", "deleteAccountWarning" to "Ta czynność jest trwała i nieodwracalna. Wszystkie Twoje dane zostaną usunięte.", "setStepGoal" to "Ustaw cel kroków", "enterDailyStepGoal" to "Wpisz swój dzienny cel kroków",
        "history" to "Historia", "deleteChat" to "Usuń czat", "bodyAnatomy" to "Anatomia ciała", "leaveEmptyIfAuthDisabled" to "Zostaw puste, jeśli uwierzytelnianie jest wyłączone", "floatingWindow" to "Pływające okno", "float" to "Pływające", "clear" to "Wyczyść", "decrease" to "Zmniejsz", "increase" to "Zwiększ", "exercise" to "Ćwiczenie", "top" to "TOP", "play" to "Odtwórz", "reset" to "Zresetuj", "selected" to "Wybrano", "loadingPlaylists" to "Ładowanie playlist...", "failedToLoadPlaylists" to "Nie udało się załadować playlist", "noPlaylistsFound" to "Nie znaleziono playlist", "createPlaylistFirst" to "Najpierw utwórz playlistę w Spotify", "authenticationFailed" to "Uwierzytelnianie nie powiodło się", "unknownError" to "Nieznany błąd", "selectPlaylist" to "Wybierz playlistę", "choosePlaylist" to "Wybierz playlistę do treningu", "tracks" to "utworów", "connectToSpotify" to "Połącz z Spotify", "spotifyAccessDescription" to "Uzyskaj dostęp do swoich playlist i stwórz idealną ścieżkę dźwiękową do treningu", "loginWithSpotify" to "Zaloguj się przez Spotify", "spotifyRedirectInfo" to "Zostaniesz przekierowany do Spotify w celu autoryzacji", "navy" to "Navy", "bmi" to "BMI", "exportBackup" to "Eksportuj kopię zapasową",
        "barWeightLabel" to "Waga sztangi", "lbsKg" to "lbs/kg",
        "platesPerSide" to "Talerze na stronę", "plateUnit" to "talerz(e)",
        "eachSide" to "na stronę", "total" to "Razem",
        "weightTooLight" to "Waga zbyt mała na talerze (tylko sztanga:",
        "plateCalcNote" to "Kalkulator talerzy automatycznie oblicza jakie talerze dodać do sztangi.",
        "howToGet" to "Jak zdobyć:",
        "gender" to "Płeć", "age" to "Wiek", "method" to "Metoda", "waistCm" to "Talia (cm)", "neckCm" to "Szyja (cm)", "hipsCm" to "Biodra (cm)", "estimatedBodyFat" to "Szacowany tłuszcz ciała", "navyMethodInfo" to "Metoda Navy: używa miary krawieckiej do obwodów",  "bodyFatCalculator" to "Kalkulator tłuszczu ciała", "newPRs" to "Nowe Rekordy", "exerciseBreakdown" to "Szczegóły ćwiczeń", "done" to "Gotowe", "weightGoal" to "Cel wagowy", "currentWeight" to "Obecna waga", "target" to "Cel", "deadline" to "Termin", "goalDetails" to "Szczegóły celu", "startWeight" to "Waga startowa", "targetWeight" to "Waga docelowa",  "noActiveGoal" to "Brak aktywnego celu", "setGoalToTrack" to "Ustaw cel, aby śledzić postępy",         "setGoal" to "Ustaw cel", "pastGoals" to "Poprzednie cele", "calculate" to "Oblicz",
        "weightEvolution" to "Ewolucja wagi", "measurements" to "pomiarów", "startedOn" to "Rozpoczęto", "editGoal" to "Edytuj cel",
        "equipDumbbells" to "Hantle", "equipBarbell" to "Sztanga", "equipMachine" to "Maszyna", "equipCable" to "Kabel", "equipBodyweight" to "Własne ciało", "equipEZBar" to "Sztanga EZ", "equipSmithMachine" to "Maszyna Smith", "equipKettlebell" to "Kettlebell", "equipStabilityBall" to "Piłka stabilna", "equipSledMachine" to "Maszyna sanki", "equipBand" to "Opaska",
        "energizeLabel" to "Energia",
        "performLabel" to "Wydajność",
        "pushItLabel" to "Dajesz",
        "openSpotifyLabel" to "Otwórz Spotify",
        "tapToPlayLabel" to "Dotknij aby odtworzyć na Spotify",
        "startingWorkoutLabel" to "Uruchamianie muzyki treningowej...",
        "signUpSuccessMessage" to "Konto utworzone! Proszę się zalogować.",
        "trainingSectionLabel" to "Trening", "frequencyLabel" to "Częstotliwość", "xPerWeek" to "x / tydz", "editProfile" to "Edytuj profil", "memberSince" to "Członek od", "changePassword" to "Zmień hasło",
        "updateTitle" to "Dostępna nowa wersja", "updateMessage" to "Kinetic %s została wydana. Masz zainstalowaną wersję v%s.\n\nKliknij, aby pobrać nowy APK.", "updateDownload" to "Pobierz", "updateLater" to "Później",
        "activeDesc" to "Trening + codzienna aktywność",
        "permanentPlan" to "Plan dożywotni",
        "vsPrevious" to "vs poprzednio",
        "free" to "Darmowy",
        "lifetimeAccess" to "Dostęp dożywotni",
        "male" to "Mężczyzna",
        "purchaseFailed" to "Nie udało się dokonać zakupu",
        "perMonth" to "/mies",
        "veryActiveDesc" to "Intensywny trening + praca fizyczna",
        "restorePurchase" to "Przywróć zakupy",
        "freePlan" to "Darmowy",
        "veryActive" to "Bardzo aktywny",
        "whatsYourAge" to "Ile masz lat?",
        "sedentary" to "Siedzący",
        "unlockedForMinutes" to "Odblokowano: %s pozostało",
        "watchAdToUnlock" to "Obejrzyj reklamę (odblokuj 30 min)",
        "remaining" to "pozostało",
        "whatsYourGender" to "Jaka jest Twoja płeć?",
        "restoreSuccess" to "Zakupy przywrócone",
        "buyNow" to "Kup",
        "bestValue" to "Najlepsza wartość",
        "noPurchasesToRestore" to "Brak zakupów do przywrócenia",
        "sessions" to "sesje",
        "active" to "Aktywny",
        "allExercises" to "Wszystkie ćwiczenia",
        "saveExercise" to "Zapisz ćwiczenie",
        "perYear" to "/rok",
        "mostPopular" to "Najpopularniejszy",
        "unlockPremiumSubtitle" to "Uzyskaj dostęp do wszystkich zaawansowanych funkcji",
        "female" to "Kobieta",
        "purchaseSuccess" to "Zakup udany! Witamy w Premium.",
        "upgradeToUnlock" to "Przejdź na wyższy plan, aby odblokować",
        "dailyAdLimitReached" to "Osiągnąłeś dzienny limit odblokowań",
        "sedentaryDesc" to "Praca biurowa, mało ruchu",
        "adUnlockSuccess" to "Funkcja odblokowana na 30 minut!",
        "cancelAnytime" to "Anuluj w dowolnym momencie w Google Play",
        "purchaseCancelled" to "Zakup anulowany",
        "currentPlan" to "Obecny plan",
        "adNotReady" to "Reklama nie jest gotowa. Spróbuj ponownie.",
        "whatsYourActivityLevel" to "Jaki jest Twój poziom aktywności?",
        "workoutAnalytics" to "Analiza treningów",
        "mostTrained" to "Najczęściej trenowany",
        "unlockPremiumTitle" to "Odblokuj Kinetic Premium",
        "oneTimePayment" to "płatność jednorazowa",
        "goalComplete" to "Cel osiągnięty!", "waterStreak" to "Seria nawodnienia", "ofGoal" to "celu", "editWaterGoal" to "Edytuj cel wody", "newWaterGoal" to "Nowy cel (ml)",
        "undo" to "Cofnij",
        "workoutReminderTitle" to "Dzisiejszy trening",
        "workoutReminderBody" to "Czas budować siłę. Skup się na __GROUPS__ dzisiaj. Daj z siebie maximum na każdym powtórzeniu i pobij swoje rekordy osobiste.",
        "workoutReminderText" to "Dzień treningowy! Przygotuj się!",
        "workoutChannelName" to "Przypomnienia o treningu",
        "weeklySummaryTitle" to "Podsumowanie tygodnia",
        "weeklySummaryText" to "Trenowałeś __COUNT__ razy w tym tygodniu! Tak trzymaj!",
        "weeklySummaryChannelName" to "Tygodniowe podsumowanie",
        "streakReminderTitle" to "Nie przerywaj serii!",
        "streakReminderText" to "Trenuj dziś, aby utrzymać serię __STREAK__ dni!",
        "streakChannelName" to "Przypomnienia o serii",
        "goalProgressTitle" to "Postęp kroków",
        "goalProgressText" to "Osiągnąłeś __PERCENT__% celu kroków! (__CURRENT__/__GOAL__)",
        "goalProgressChannelName" to "Postęp celów",
        "achievementTitle" to "Osiągnięcie odblokowane!",
        "achievementText" to "Gratulacje! Odblokowałeś nową odznakę!",
        "achievementChannelName" to "Osiągnięcia"
    ))

    fun getStrings(context: Context): Strings {
        val lang = if (currentLanguage.isNotEmpty()) {
            currentLanguage
        } else {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val saved = prefs.getString(KEY_LANGUAGE, null)
            if (!saved.isNullOrEmpty()) {
                currentLanguage = saved
                saved
            } else {
                currentLanguage = "en"
                "en"
            }
        }
        return strings[lang] ?: strings["en"]!!
    }
    fun saveLanguage(context: Context, code: String) {
        currentLanguage = code
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_LANGUAGE, code).apply()
    }
    fun loadSavedLanguage(context: Context): String {
        val saved = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, null)
        if (!saved.isNullOrEmpty()) {
            currentLanguage = saved
        }
        return currentLanguage
    }

    fun translateMuscleGroup(group: String, strings: Strings): String {
        return when (group) {
            "Piept", "Chest" -> strings.chest
            "Spate", "Back" -> strings.back
            "Umeri", "Shoulders" -> strings.shoulders
            "Biceps" -> strings.biceps
            "Triceps" -> strings.triceps
            "Abdomen", "Core" -> strings.core
            "Picioare", "Thighs" -> strings.thighs
            "Fese", "Glutes" -> strings.glutes
            "Gambe", "Calves" -> strings.calves
            "Cardio" -> strings.cardio
            "Antebrate", "Forearms" -> strings.forearms
            "Gat & Trapezi", "Neck & Traps" -> strings.neckAndTraps
            else -> group
        }
    }

    fun translateEquipment(equipment: String, strings: Strings): String {
        return when (equipment) {
            "Dumbbells" -> strings.equipDumbbells
            "Barbell" -> strings.equipBarbell
            "Machine" -> strings.equipMachine
            "Cable" -> strings.equipCable
            "Bodyweight" -> strings.equipBodyweight
            "EZ Bar" -> strings.equipEZBar
            "Smith Machine" -> strings.equipSmithMachine
            "Kettlebell" -> strings.equipKettlebell
            "Stability Ball" -> strings.equipStabilityBall
            "Sled Machine" -> strings.equipSledMachine
            "Band" -> strings.equipBand
            else -> equipment
        }
    }
    data class TranslatedBadge(val title: String, val description: String, val hint: String)

    fun getTranslatedBadge(badgeKey: String): TranslatedBadge {
        val lang = getLanguage()
        return when (lang) {
            "ro" -> when (badgeKey) {
                "first_workout" -> TranslatedBadge("Primul Antrenament", "Ai completat primul antrenament", "Loghează primul tău antrenament")
                "7day_streak" -> TranslatedBadge("Șir de 7 Zile", "Te-ai antrenat 7 zile consecutiv", "Antrenează-te 7 zile consecutive")
                "30day_streak" -> TranslatedBadge("Șir de 30 Zile", "Te-ai antrenat 30 zile consecutiv", "Antrenează-te 30 zile consecutive")
                "pr_machine" -> TranslatedBadge("Mașinărie de PR", "Ai stabilit 10 recorduri personale", "Setează 10 recorduri personale")
                "century_club" -> TranslatedBadge("Clubul sutelor", "Ai completat 100 de antrenamente", "Loghează 100 de antrenamente")
                "social_butterfly" -> TranslatedBadge("Fluture Social", "Ai adăugat 10 prieteni", "Adaugă 10 prieteni")
                "helping_hand" -> TranslatedBadge("Mâna de Ajutor", "Ai comentat la 10 postări", "Comentează la 10 postări")
                "1000kg_club" -> TranslatedBadge("Clubul 1000kg", "Ai ridicat 1000kg total într-o sesiune", "Ridică 1000kg total într-o sesiune")
                else -> TranslatedBadge(badgeKey, "", "")
            }
            "ru" -> when (badgeKey) {
                "first_workout" -> TranslatedBadge("Первая тренировка", "Вы завершили первую тренировку", "Проведите первую тренировку")
                "7day_streak" -> TranslatedBadge("Серия 7 дней", "Вы тренировались 7 дней подряд", "Тренируйтесь 7 дней подряд")
                "30day_streak" -> TranslatedBadge("Серия 30 дней", "Вы тренировались 30 дней подряд", "Тренируйтесь 30 дней подряд")
                "pr_machine" -> TranslatedBadge("Машина рекордов", "Вы установили 10 личных рекордов", "Установите 10 личных рекордов")
                "century_club" -> TranslatedBadge("Клуб сотни", "Вы провели 100 тренировок", "Проведите 100 тренировок")
                "social_butterfly" -> TranslatedBadge("Социальная бабочка", "Вы добавили 10 друзей", "Добавьте 10 друзей")
                "helping_hand" -> TranslatedBadge("Рука помощи", "Вы прокомментировали 10 постов", "Прокомментируйте 10 постов")
                "1000kg_club" -> TranslatedBadge("Клуб 1000кг", "Вы подняли 1000кг за одну тренировку", "Поднимите 1000кг за одну тренировку")
                else -> TranslatedBadge(badgeKey, "", "")
            }
            "uk" -> when (badgeKey) {
                "first_workout" -> TranslatedBadge("Перше тренування", "Ви завершили перше тренування", "Проведіть перше тренування")
                "7day_streak" -> TranslatedBadge("Серія 7 днів", "Ви тренувались 7 днів поспіль", "Тренуйтесь 7 днів поспіль")
                "30day_streak" -> TranslatedBadge("Серія 30 днів", "Ви тренувались 30 днів поспіль", "Тренуйтесь 30 днів поспіль")
                "pr_machine" -> TranslatedBadge("Машина рекордів", "Ви встановили 10 особистих рекордів", "Встановіть 10 особистих рекордів")
                "century_club" -> TranslatedBadge("Клуб сотні", "Ви провели 100 тренувань", "Проведіть 100 тренувань")
                "social_butterfly" -> TranslatedBadge("Соціальний метелик", "Ви додали 10 друзів", "Додайте 10 друзів")
                "helping_hand" -> TranslatedBadge("Рука допомоги", "Ви прокоментували 10 дописів", "Прокоментуйте 10 дописів")
                "1000kg_club" -> TranslatedBadge("Клуб 1000кг", "Ви підняли 1000кг за одне тренування", "Підніміть 1000кг за одне тренування")
                else -> TranslatedBadge(badgeKey, "", "")
            }
            "fr" -> when (badgeKey) {
                "first_workout" -> TranslatedBadge("Première séance", "Vous avez terminé votre première séance", "Effectuez votre première séance")
                "7day_streak" -> TranslatedBadge("Série de 7 jours", "Vous vous êtes entraîné 7 jours d'affilée", "Entraînez-vous 7 jours d'affilée")
                "30day_streak" -> TranslatedBadge("Série de 30 jours", "Vous vous êtes entraîné 30 jours d'affilée", "Entraînez-vous 30 jours d'affilée")
                "pr_machine" -> TranslatedBadge("Machine à records", "Vous avez établi 10 records personnels", "Établissez 10 records personnels")
                "century_club" -> TranslatedBadge("Club du centenaire", "Vous avez enregistré 100 séances", "Enregistrez 100 séances")
                "social_butterfly" -> TranslatedBadge("Papillon social", "Vous avez ajouté 10 amis", "Ajoutez 10 amis")
                "helping_hand" -> TranslatedBadge("Main secourable", "Vous avez commenté 10 publications", "Commentez 10 publications")
                "1000kg_club" -> TranslatedBadge("Club des 1000kg", "Vous avez soulevé 1000kg en une séance", "Soulevez 1000kg en une séance")
                else -> TranslatedBadge(badgeKey, "", "")
            }
            "de" -> when (badgeKey) {
                "first_workout" -> TranslatedBadge("Erstes Training", "Du hast dein erstes Training abgeschlossen", "Schließe dein erstes Training ab")
                "7day_streak" -> TranslatedBadge("7-Tage-Serie", "Du hast 7 Tage hintereinander trainiert", "Trainiere 7 Tage hintereinander")
                "30day_streak" -> TranslatedBadge("30-Tage-Serie", "Du hast 30 Tage hintereinander trainiert", "Trainiere 30 Tage hintereinander")
                "pr_machine" -> TranslatedBadge("PR-Maschine", "Du hast 10 persönliche Rekorde aufgestellt", "Stelle 10 persönliche Rekorde auf")
                "century_club" -> TranslatedBadge("Jahrhundert-Club", "Du hast 100 Trainings absolviert", "Absolviere 100 Trainings")
                "social_butterfly" -> TranslatedBadge("Sozialer Schmetterling", "Du hast 10 Freunde hinzugefügt", "Füge 10 Freunde hinzu")
                "helping_hand" -> TranslatedBadge("Helfende Hand", "Du hast 10 Beiträge kommentiert", "Kommentiere 10 Beiträge")
                "1000kg_club" -> TranslatedBadge("1000kg-Club", "Du hast 1000kg in einer Einheit gehoben", "Hebe 1000kg in einer Einheit")
                else -> TranslatedBadge(badgeKey, "", "")
            }
            "es" -> when (badgeKey) {
                "first_workout" -> TranslatedBadge("Primer entrenamiento", "Completaste tu primer entrenamiento", "Completa tu primer entrenamiento")
                "7day_streak" -> TranslatedBadge("Racha de 7 días", "Entrenaste 7 días seguidos", "Entrena 7 días seguidos")
                "30day_streak" -> TranslatedBadge("Racha de 30 días", "Entrenaste 30 días seguidos", "Entrena 30 días seguidos")
                "pr_machine" -> TranslatedBadge("Máquina de récords", "Estableciste 10 récords personales", "Establece 10 récords personales")
                "century_club" -> TranslatedBadge("Club del centenario", "Registraste 100 entrenamientos", "Registra 100 entrenamientos")
                "social_butterfly" -> TranslatedBadge("Mariposa social", "Agregaste 10 amigos", "Agrega 10 amigos")
                "helping_hand" -> TranslatedBadge("Mano helpful", "Comentaste 10 publicaciones", "Comenta 10 publicaciones")
                "1000kg_club" -> TranslatedBadge("Club de 1000kg", "Levantaste 1000kg en una sesión", "Levanta 1000kg en una sesión")
                else -> TranslatedBadge(badgeKey, "", "")
            }
            "it" -> when (badgeKey) {
                "first_workout" -> TranslatedBadge("Primo allenamento", "Hai completato il primo allenamento", "Completa il primo allenamento")
                "7day_streak" -> TranslatedBadge("Serie di 7 giorni", "Ti sei allenato 7 giorni consecutivi", "Allénati 7 giorni consecutivi")
                "30day_streak" -> TranslatedBadge("Serie di 30 giorni", "Ti sei allenato 30 giorni consecutivi", "Allénati 30 giorni consecutivi")
                "pr_machine" -> TranslatedBadge("Macchina dei PR", "Hai stabilito 10 record personali", "Stabilisci 10 record personali")
                "century_club" -> TranslatedBadge("Club del secolo", "Hai registrato 100 allenamenti", "Registrа 100 allenamenti")
                "social_butterfly" -> TranslatedBadge("Farfalla sociale", "Hai aggiunto 10 amici", "Aggiungi 10 amici")
                "helping_hand" -> TranslatedBadge("Mano aiutante", "Hai commentato 10 post", "Commenta 10 post")
                "1000kg_club" -> TranslatedBadge("Club dei 1000kg", "Hai sollevato 1000kg in una sessione", "Sollevа 1000kg in una sessione")
                else -> TranslatedBadge(badgeKey, "", "")
            }
            "tr" -> when (badgeKey) {
                "first_workout" -> TranslatedBadge("İlk antrenman", "İlk antrenmanını tamamladın", "İlk antrenmanını tamamla")
                "7day_streak" -> TranslatedBadge("7 günlük seri", "7 gün üst üste antrenman yaptın", "7 gün üst üste antrenman yap")
                "30day_streak" -> TranslatedBadge("30 günlük seri", "30 gün üst üste antrenman yaptın", "30 gün üst üste antrenman yap")
                "pr_machine" -> TranslatedBadge("PR makinesi", "10 kişisel rekor kırdın", "10 kişisel rekor kır")
                "century_club" -> TranslatedBadge("Yüzler kulübü", "100 antrenman kaydettin", "100 antrenman kaydet")
                "social_butterfly" -> TranslatedBadge("Sosyal kelebek", "10 arkadaş ekledin", "10 arkadaş ekle")
                "ndering_hand" -> TranslatedBadge("Yardımsever el", "10 gönderiye yorum yaptın", "10 gönderiye yorum yap")
                "1000kg_club" -> TranslatedBadge("1000kg kulübü", "Bir seansta 1000kg kaldırdın", "Bir seansta 1000kg kaldır")
                else -> TranslatedBadge(badgeKey, "", "")
            }
            "pt" -> when (badgeKey) {
                "first_workout" -> TranslatedBadge("Primeiro treino", "Você completou seu primeiro treino", "Complete seu primeiro treino")
                "7day_streak" -> TranslatedBadge("Sequência de 7 dias", "Você treinou 7 dias seguidos", "Treine 7 dias seguidos")
                "30day_streak" -> TranslatedBadge("Sequência de 30 dias", "Você treinou 30 dias seguidos", "Treine 30 dias seguidos")
                "pr_machine" -> TranslatedBadge("Máquina de recordes", "Você estabeleceu 10 recordes pessoais", "Estabeleça 10 recordes pessoais")
                "century_club" -> TranslatedBadge("Clube do centenário", "Você registrou 100 treinos", "Registre 100 treinos")
                "social_butterfly" -> TranslatedBadge("Borboleta social", "Você adicionou 10 amigos", "Adicione 10 amigos")
                "helping_hand" -> TranslatedBadge("Mão solidária", "Você comentou em 10 publicações", "Comente em 10 publicações")
                "1000kg_club" -> TranslatedBadge("Clube dos 1000kg", "Você levantou 1000kg em uma sessão", "Levante 1000kg em uma sessão")
                else -> TranslatedBadge(badgeKey, "", "")
            }
            "pl" -> when (badgeKey) {
                "first_workout" -> TranslatedBadge("Pierwszy trening", "Ukończyłeś pierwszy trening", "Ukończ swój pierwszy trening")
                "7day_streak" -> TranslatedBadge("Seria 7 dni", "Trenowałeś 7 dni z rzędu", "Trenuj 7 dni z rzędu")
                "30day_streak" -> TranslatedBadge("Seria 30 dni", "Trenowałeś 30 dni z rzędu", "Trenuj 30 dni z rzędu")
                "pr_machine" -> TranslatedBadge("Maszyna do rekordów", "Ustanowiłeś 10 rekordów osobistych", "Ustanów 10 rekordów osobistych")
                "century_club" -> TranslatedBadge("Klub setki", "Zarejestrowałeś 100 treningów", "Zarejestruj 100 treningów")
                "social_butterfly" -> TranslatedBadge("Towarzyski motyl", "Dodałeś 10 znajomych", "Dodaj 10 znajomych")
                "helping_hand" -> TranslatedBadge("Pomocna dłoń", "Skomentowałeś 10 postów", "Skomentuj 10 postów")
                "1000kg_club" -> TranslatedBadge("Klub 1000kg", "Podniosłeś 1000kg w jednej sesji", "Podnieś 1000kg w jednej sesji")
                else -> TranslatedBadge(badgeKey, "", "")
            }
            else -> when (badgeKey) {
                "first_workout" -> TranslatedBadge("First Workout", "Completed your first workout", "Log your first workout")
                "7day_streak" -> TranslatedBadge("7-Day Streak", "Trained 7 days in a row", "Train 7 days in a row")
                "30day_streak" -> TranslatedBadge("30-Day Streak", "Trained 30 days in a row", "Train 30 days in a row")
                "pr_machine" -> TranslatedBadge("PR Machine", "Set 10 personal records", "Set 10 personal records")
                "century_club" -> TranslatedBadge("Century Club", "Logged 100 workouts", "Log 100 workouts")
                "social_butterfly" -> TranslatedBadge("Social Butterfly", "Added 10 friends", "Add 10 friends")
                "helping_hand" -> TranslatedBadge("Helping Hand", "Commented on 10 posts", "Comment on 10 posts")
                "1000kg_club" -> TranslatedBadge("1000kg Club", "Lifted 1000kg total in one session", "Lift 1000kg total in one session")
                else -> TranslatedBadge(badgeKey, "", "")
            }
        }
    }
}