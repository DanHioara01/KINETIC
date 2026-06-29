package com.example.gymlog2

import android.content.Context
import android.content.SharedPreferences
import java.util.Locale

object LanguageManager {

    private const val PREF_NAME = "lang_prefs"
    private const val KEY_LANGUAGE = "current_language"

    private var currentLanguage: String = ""

    class Strings(m: Map<String, String>) {
        val appName: String = m["appName"] ?: ""
        val dashboard: String = m["dashboard"] ?: ""
        val overview: String = m["overview"] ?: ""
        val acasa: String = m["acasa"] ?: ""
        val workouts: String = m["workouts"] ?: ""
        val stats: String = m["stats"] ?: ""
        val waterIntake: String = m["waterIntake"] ?: ""
        val waterHistory: String = m["waterHistory"] ?: ""
        val last7Days: String = m["last7Days"] ?: ""
        val everyDay: String = m["everyDay"] ?: ""
        val reminder: String = m["reminder"] ?: ""
        val waterGoal: String = m["waterGoal"] ?: ""
        val addWater: String = m["addWater"] ?: ""
        val dailyWater: String = m["dailyWater"] ?: ""
        val height: String = m["height"] ?: ""
        val personalInfo: String = m["personalInfo"] ?: ""
        val waterAutoCalc: String = m["waterAutoCalc"] ?: ""
        val ml: String = m["ml"] ?: ""
        val templates: String = m["templates"] ?: ""
        val recovery: String = m["recovery"] ?: ""
        val progress: String = m["progress"] ?: ""
        val feed: String = m["feed"] ?: ""
        val friends: String = m["friends"] ?: ""
        val leaderboard: String = m["leaderboard"] ?: ""
        val all: String = m["all"] ?: ""
        val settings: String = m["settings"] ?: ""
        val language: String = m["language"] ?: ""
        val units: String = m["units"] ?: ""
        val logout: String = m["logout"] ?: ""
        val login: String = m["login"] ?: ""
        val signUp: String = m["signUp"] ?: ""
        val email: String = m["email"] ?: ""
        val password: String = m["password"] ?: ""
        val forgotPassword: String = m["forgotPassword"] ?: ""
        val orContinueWith: String = m["orContinueWith"] ?: ""
        val loginAsGuest: String = m["loginAsGuest"] ?: ""
        val welcomeBack: String = m["welcomeBack"] ?: ""
        val createAccount: String = m["createAccount"] ?: ""
        val goalStrength: String = m["goalStrength"] ?: ""
        val goalMass: String = m["goalMass"] ?: ""
        val goalWeightLoss: String = m["goalWeightLoss"] ?: ""
        val goalMaintenance: String = m["goalMaintenance"] ?: ""
        val selectGoal: String = m["selectGoal"] ?: ""
        val next: String = m["next"] ?: ""
        val skip: String = m["skip"] ?: ""
        val finish: String = m["finish"] ?: ""
        val back: String = m["back"] ?: ""
        val profileSetup: String = m["profileSetup"] ?: ""
        val enterName: String = m["enterName"] ?: ""
        val pickPhoto: String = m["pickPhoto"] ?: ""
        val saveProfile: String = m["saveProfile"] ?: ""
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
        val sets: String = m["sets"] ?: ""
        val reps: String = m["reps"] ?: ""
        val weight: String = m["weight"] ?: ""
        val addExercise: String = m["addExercise"] ?: ""
        val saveWorkout: String = m["saveWorkout"] ?: ""
        val startWorkout: String = m["startWorkout"] ?: ""
        val nextExercise: String = m["nextExercise"] ?: ""
        val notes: String = m["notes"] ?: ""
        val cancel: String = m["cancel"] ?: ""
        val confirm: String = m["confirm"] ?: ""
        val delete: String = m["delete"] ?: ""
        val edit: String = m["edit"] ?: ""
        val search: String = m["search"] ?: ""
        val noDataYet: String = m["noDataYet"] ?: ""
        val friendRequests: String = m["friendRequests"] ?: ""
        val sendRequest: String = m["sendRequest"] ?: ""
        val accept: String = m["accept"] ?: ""
        val reject: String = m["reject"] ?: ""
        val removeFriend: String = m["removeFriend"] ?: ""
        val noFriends: String = m["noFriends"] ?: ""
        val searchUsers: String = m["searchUsers"] ?: ""
        val userId: String = m["userId"] ?: ""
        val searchByNameOrId: String = m["searchByNameOrId"] ?: ""
        val incomingRequests: String = m["incomingRequests"] ?: ""
        val noIncomingRequests: String = m["noIncomingRequests"] ?: ""
        val yourFriends: String = m["yourFriends"] ?: ""
        val sendFriendRequest: String = m["sendFriendRequest"] ?: ""
        val friendRequestSent: String = m["friendRequestSent"] ?: ""
        val byId: String = m["byId"] ?: ""
        val feedEmpty: String = m["feedEmpty"] ?: ""
        val postPlaceholder: String = m["postPlaceholder"] ?: ""
        val post: String = m["post"] ?: ""
        val comments: String = m["comments"] ?: ""
        val like: String = m["like"] ?: ""
        val likes: String = m["likes"] ?: ""
        val share: String = m["share"] ?: ""
        val workoutCompleted: String = m["workoutCompleted"] ?: ""
        val streakLabel: String = m["streakLabel"] ?: ""
        val bestStreak: String = m["bestStreak"] ?: ""
        val badges: String = m["badges"] ?: ""
        val noBadges: String = m["noBadges"] ?: ""
        val rank: String = m["rank"] ?: ""
        val kg: String = m["kg"] ?: ""
        val lbs: String = m["lbs"] ?: ""
        val kgLbsToggle: String = m["kgLbsToggle"] ?: ""
        val exportCsv: String = m["exportCsv"] ?: ""
        val importCsv: String = m["importCsv"] ?: ""
        val subscription: String = m["subscription"] ?: ""
        val premium: String = m["premium"] ?: ""
        val monthlyPlan: String = m["monthlyPlan"] ?: ""
        val yearlyPlan: String = m["yearlyPlan"] ?: ""
        val subscribe: String = m["subscribe"] ?: ""
        val subscribed: String = m["subscribed"] ?: ""
        val notSubscribed: String = m["notSubscribed"] ?: ""
        val darkMode: String = m["darkMode"] ?: ""
        val lightMode: String = m["lightMode"] ?: ""
        val systemDefault: String = m["systemDefault"] ?: ""
        val about: String = m["about"] ?: ""
        val version: String = m["version"] ?: ""
        val totalWorkouts: String = m["totalWorkouts"] ?: ""
        val totalWeight: String = m["totalWeight"] ?: ""
        val personalRecords: String = m["personalRecords"] ?: ""
        val recentWorkouts: String = m["recentWorkouts"] ?: ""
        val viewAll: String = m["viewAll"] ?: ""
        val loading: String = m["loading"] ?: ""
        val error: String = m["error"] ?: ""
        val retry: String = m["retry"] ?: ""
        val success: String = m["success"] ?: ""
        val friendRequestAccepted: String = m["friendRequestAccepted"] ?: ""
        val friendRequestRejected: String = m["friendRequestRejected"] ?: ""
        val profileUpdated: String = m["profileUpdated"] ?: ""
        val workoutSaved: String = m["workoutSaved"] ?: ""
        val workoutDeleted: String = m["workoutDeleted"] ?: ""
        val noExercises: String = m["noExercises"] ?: ""
        val selectExercises: String = m["selectExercises"] ?: ""
        val exerciseList: String = m["exerciseList"] ?: ""
        val customExercises: String = m["customExercises"] ?: ""
        val defaultExercises: String = m["defaultExercises"] ?: ""
        val addCustomExercise: String = m["addCustomExercise"] ?: ""
        val enterExerciseName: String = m["enterExerciseName"] ?: ""
        val selectGroup: String = m["selectGroup"] ?: ""
        val addTemplate: String = m["addTemplate"] ?: ""
        val templateName: String = m["templateName"] ?: ""
        val templateSaved: String = m["templateSaved"] ?: ""
        val templateDeleted: String = m["templateDeleted"] ?: ""
        val noTemplates: String = m["noTemplates"] ?: ""
        val createFirstTemplate: String = m["createFirstTemplate"] ?: ""
        val selectTemplate: String = m["selectTemplate"] ?: ""
        val useTemplate: String = m["useTemplate"] ?: ""
        val deleteTemplate: String = m["deleteTemplate"] ?: ""
        val recoveryInfo: String = m["recoveryInfo"] ?: ""
        val lastWorkout: String = m["lastWorkout"] ?: ""
        val daysSince: String = m["daysSince"] ?: ""
        val recommendedRecovery: String = m["recommendedRecovery"] ?: ""
        val muscleGroupRecovery: String = m["muscleGroupRecovery"] ?: ""
        val readyToTrain: String = m["readyToTrain"] ?: ""
        val needsMoreRest: String = m["needsMoreRest"] ?: ""
        val todayIsRestDay: String = m["todayIsRestDay"] ?: ""
        val progressChart: String = m["progressChart"] ?: ""
        val volumeOverTime: String = m["volumeOverTime"] ?: ""
        val weightProgression: String = m["weightProgression"] ?: ""
        val frequencyChart: String = m["frequencyChart"] ?: ""
        val noChartData: String = m["noChartData"] ?: ""
        val calendarView: String = m["calendarView"] ?: ""
        val listView: String = m["listView"] ?: ""
        val sortBy: String = m["sortBy"] ?: ""
        val sortByDate: String = m["sortByDate"] ?: ""
        val sortByGroup: String = m["sortByGroup"] ?: ""
        val filterByGroup: String = m["filterByGroup"] ?: ""
        val allGroups: String = m["allGroups"] ?: ""
        val welcomeTitle: String = m["welcomeTitle"] ?: ""
        val welcomeSubtitle: String = m["welcomeSubtitle"] ?: ""
        val featureSocial: String = m["featureSocial"] ?: ""
        val featureGamification: String = m["featureGamification"] ?: ""
        val featureCharts: String = m["featureCharts"] ?: ""
        val featureExport: String = m["featureExport"] ?: ""
        val featureTemplates: String = m["featureTemplates"] ?: ""
        val featureMultiLang: String = m["featureMultiLang"] ?: ""
        val notifications: String = m["notifications"] ?: ""
        val enableNotifications: String = m["enableNotifications"] ?: ""
        val notificationPermissionRequired: String = m["notificationPermissionRequired"] ?: ""
        val friendRequestNotificationTitle: String = m["friendRequestNotificationTitle"] ?: ""
        val friendRequestNotificationText: String = m["friendRequestNotificationText"] ?: ""
        val profilePhotoUpdated: String = m["profilePhotoUpdated"] ?: ""
        val nameRequired: String = m["nameRequired"] ?: ""
        val settingsSaved: String = m["settingsSaved"] ?: ""
        val darkTheme: String = m["darkTheme"] ?: ""
        val lightTheme: String = m["lightTheme"] ?: ""
        val systemTheme: String = m["systemTheme"] ?: ""
        val selectLanguage: String = m["selectLanguage"] ?: ""
        val english: String = m["english"] ?: ""
        val romanian: String = m["romanian"] ?: ""
        val russian: String = m["russian"] ?: ""
        val ukrainian: String = m["ukrainian"] ?: ""
        val french: String = m["french"] ?: ""
        val german: String = m["german"] ?: ""
        val spanish: String = m["spanish"] ?: ""
        val italian: String = m["italian"] ?: ""
        val turkish: String = m["turkish"] ?: ""
        val portuguese: String = m["portuguese"] ?: ""
        val polish: String = m["polish"] ?: ""
        val leaderLabel: String = m["leaderLabel"] ?: ""
        val workoutsLabel: String = m["workoutsLabel"] ?: ""
        val totalVolume: String = m["totalVolume"] ?: ""
        val currentStreakLabel: String = m["currentStreakLabel"] ?: ""
        val bestStreakLabel: String = m["bestStreakLabel"] ?: ""
        val badgesEarned: String = m["badgesEarned"] ?: ""
        val days: String = m["days"] ?: ""
        val badge: String = m["badge"] ?: ""
        val lastPR: String = m["lastPR"] ?: ""
        val viewProfile: String = m["viewProfile"] ?: ""
        val accountSettings: String = m["accountSettings"] ?: ""
        val deleteAccount: String = m["deleteAccount"] ?: ""
        val privacyPolicy: String = m["privacyPolicy"] ?: ""
        val termsOfService: String = m["termsOfService"] ?: ""
        val back_: String = m["back_"] ?: ""
        val restTimer: String = m["restTimer"] ?: ""
        val startTimer: String = m["startTimer"] ?: ""
        val customTimer: String = m["customTimer"] ?: ""
        val seconds: String = m["seconds"] ?: ""
        val custom: String = m["custom"] ?: ""
        val exerciseHistory: String = m["exerciseHistory"] ?: ""
        val bestSet: String = m["bestSet"] ?: ""
        val lastSets: String = m["lastSets"] ?: ""
        val favorite: String = m["favorite"] ?: ""
        val favorites: String = m["favorites"] ?: ""
        val usageCount: String = m["usageCount"] ?: ""
        val addSet: String = m["addSet"] ?: ""
        val exerciseNotes: String = m["exerciseNotes"] ?: ""
        val workoutNotes: String = m["workoutNotes"] ?: ""
        val saveNotes: String = m["saveNotes"] ?: ""
        val editWorkout: String = m["editWorkout"] ?: ""
        val volume: String = m["volume"] ?: ""
        val maxWeight: String = m["maxWeight"] ?: ""
        val maxReps: String = m["maxReps"] ?: ""
        val maxSet: String = m["maxSet"] ?: ""
        val today: String = m["today"] ?: ""
        val thisWeek: String = m["thisWeek"] ?: ""
        val thisMonth: String = m["thisMonth"] ?: ""
        val totalVolumeLabel: String = m["totalVolumeLabel"] ?: ""
        val languageChanged: String = m["languageChanged"] ?: ""
        val themeChanged: String = m["themeChanged"] ?: ""
        val guest: String = m["guest"] ?: ""
        val loginWithGoogle: String = m["loginWithGoogle"] ?: ""
        val loginWithFacebook: String = m["loginWithFacebook"] ?: ""
        val close: String = m["close"] ?: ""
        val menu: String = m["menu"] ?: ""
        val profile: String = m["profile"] ?: ""
        val appTagline: String = m["appTagline"] ?: ""
        val or: String = m["or"] ?: ""
        val dark: String = m["dark"] ?: ""
        val light: String = m["light"] ?: ""
        val system: String = m["system"] ?: ""
        val languageTitle: String = m["languageTitle"] ?: ""
        val themeTitle: String = m["themeTitle"] ?: ""
        val selectTheme: String = m["selectTheme"] ?: ""
        val settingsAndMore: String = m["settingsAndMore"] ?: ""
        val muscleGroups: String = m["muscleGroups"] ?: ""
        val startHere: String = m["startHere"] ?: ""
        val back__: String = m["back__"] ?: ""
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
        val newExercise: String = m["newExercise"] ?: ""
        val exerciseNameLabel: String = m["exerciseNameLabel"] ?: ""
        val add: String = m["add"] ?: ""
        val demoExercise: String = m["demoExercise"] ?: ""
        val setLabel: String = m["setLabel"] ?: ""
        val prAndVolume: String = m["prAndVolume"] ?: ""
        val start: String = m["start"] ?: ""
        val stop: String = m["stop"] ?: ""
        val noSavedSetsYet: String = m["noSavedSetsYet"] ?: ""
        val editSet: String = m["editSet"] ?: ""
        val chooseTemplate: String = m["chooseTemplate"] ?: ""
        val exercises: String = m["exercises"] ?: ""
        val recovered: String = m["recovered"] ?: ""
        val almostRecovered: String = m["almostRecovered"] ?: ""
        val moderate: String = m["moderate"] ?: ""
        val tired: String = m["tired"] ?: ""
        val exhausted: String = m["exhausted"] ?: ""
        val fatigue: String = m["fatigue"] ?: ""
        val chooseMuscleGroup: String = m["chooseMuscleGroup"] ?: ""
        val changeExercise: String = m["changeExercise"] ?: ""
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
        val workoutDistribution: String = m["workoutDistribution"] ?: ""
        val monthlyDetails: String = m["monthlyDetails"] ?: ""
        val month: String = m["month"] ?: ""
        val notNow: String = m["notNow"] ?: ""
        val subscribeNow: String = m["subscribeNow"] ?: ""
        val premiumFeature: String = m["premiumFeature"] ?: ""
        val subscribersOnly: String = m["subscribersOnly"] ?: ""
        val choosePlan: String = m["choosePlan"] ?: ""
        val youAreSubscribed: String = m["youAreSubscribed"] ?: ""
        val muscleRecovery: String = m["muscleRecovery"] ?: ""
        val waterReminder: String = m["waterReminder"] ?: ""
        val waterReminderTitle: String = m["waterReminderTitle"] ?: ""
        val waterReminderText: String = m["waterReminderText"] ?: ""
        val waterReminderEnabled: String = m["waterReminderEnabled"] ?: ""
        val waterReminderDisabled: String = m["waterReminderDisabled"] ?: ""
        val selectTime: String = m["selectTime"] ?: ""
        val forearms: String = m["forearms"] ?: ""
        val neckAndTraps: String = m["neckAndTraps"] ?: ""
        val welcome: String = m["welcome"] ?: ""
        val athlete: String = m["athlete"] ?: ""
        val biometricTracking: String = m["biometricTracking"] ?: ""
        val biometricSubtitle: String = m["biometricSubtitle"] ?: ""
        val addMeasurement: String = m["addMeasurement"] ?: ""
        val bodyFat: String = m["bodyFat"] ?: ""
        val waistCirc: String = m["waistCirc"] ?: ""
        val hipsCirc: String = m["hipsCirc"] ?: ""
        val thighsCirc: String = m["thighsCirc"] ?: ""
        val chestCirc: String = m["chestCirc"] ?: ""
        val armsCirc: String = m["armsCirc"] ?: ""
        val lastMeasurement: String = m["lastMeasurement"] ?: ""
        val noMeasurements: String = m["noMeasurements"] ?: ""
        val viewCharts: String = m["viewCharts"] ?: ""
        val saveMeasurement: String = m["saveMeasurement"] ?: ""
        val measurementSaved: String = m["measurementSaved"] ?: ""
        val weeksAgo: String = m["weeksAgo"] ?: ""
        val cm: String = m["cm"] ?: ""
        val percent: String = m["percent"] ?: ""
        val deleteMeasurement: String = m["deleteMeasurement"] ?: ""
        val biometricHistory: String = m["biometricHistory"] ?: ""
        val weightChart: String = m["weightChart"] ?: ""
        val bodyFatChart: String = m["bodyFatChart"] ?: ""
        val circumferenceChart: String = m["circumferenceChart"] ?: ""
        val date: String = m["date"] ?: ""
        val biometricReminder: String = m["biometricReminder"] ?: ""
        val biometricReminderTitle: String = m["biometricReminderTitle"] ?: ""
        val biometricReminderText: String = m["biometricReminderText"] ?: ""
        val biometricReminderEnabled: String = m["biometricReminderEnabled"] ?: ""
        val biometricReminderDisabled: String = m["biometricReminderDisabled"] ?: ""
        val foodJournal: String = m["foodJournal"] ?: ""
        val scanBarcode: String = m["scanBarcode"] ?: ""
        val scanBarcodeHint: String = m["scanBarcodeHint"] ?: ""
        val cameraPermissionRequired: String = m["cameraPermissionRequired"] ?: ""
        val scan: String = m["scan"] ?: ""
        val scanning: String = m["scanning"] ?: ""
        val scanBarcodeHelp: String = m["scanBarcodeHelp"] ?: ""
        val noFoodEntries: String = m["noFoodEntries"] ?: ""
        val todaysMacros: String = m["todaysMacros"] ?: ""
        val caloriesLabel: String = m["caloriesLabel"] ?: ""
        val proteinLabel: String = m["proteinLabel"] ?: ""
        val carbsLabel: String = m["carbsLabel"] ?: ""
        val fatLabel: String = m["fatLabel"] ?: ""
        val breakfast: String = m["breakfast"] ?: ""
        val lunch: String = m["lunch"] ?: ""
        val dinner: String = m["dinner"] ?: ""
        val snack: String = m["snack"] ?: ""
        val drinks: String = m["drinks"] ?: ""
        val selectMealType: String = m["selectMealType"] ?: ""
        val manualFoodEntry: String = m["manualFoodEntry"] ?: ""
        val foodName: String = m["foodName"] ?: ""
        val brandLabel: String = m["brandLabel"] ?: ""
        val calories: String = m["calories"] ?: ""
        val protein: String = m["protein"] ?: ""
        val carbs: String = m["carbs"] ?: ""
        val fat: String = m["fat"] ?: ""
        val fiber: String = m["fiber"] ?: ""
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
            m["motto5"] ?: "", m["motto6"] ?: "", m["motto7"] ?: "", m["motto8"] ?: ""
        )
        val goodMorning: String = m["goodMorning"] ?: ""
        val goodAfternoon: String = m["goodAfternoon"] ?: ""
        val goodEvening: String = m["goodEvening"] ?: ""
        val daysConsecutive: String = m["daysConsecutive"] ?: ""
        val todaysWorkout: String = m["todaysWorkout"] ?: ""
        val howDoYouFeel: String = m["howDoYouFeel"] ?: ""
        val tiredLabel: String = m["tiredLabel"] ?: ""
        val normalLabel: String = m["normalLabel"] ?: ""
        val energeticLabel: String = m["energeticLabel"] ?: ""
        val plusToday: String = m["plusToday"] ?: ""
        val technicalTip: String = m["technicalTip"] ?: ""
        val tomorrowLabel: String = m["tomorrowLabel"] ?: ""
        val setWorkoutTime: String = m["setWorkoutTime"] ?: ""
        val daysSinceLastWorkout: String = m["daysSinceLastWorkout"] ?: ""
        val groupsFullyRecovered: String = m["groupsFullyRecovered"] ?: ""
        val recoveryOnGroups: String = m["recoveryOnGroups"] ?: ""
        val weeklySummary: String = m["weeklySummary"] ?: "Weekly Summary"
        val lastWeekLabel: String = m["lastWeekLabel"] ?: "last week"
        val goalLabel: String = m["goalLabel"] ?: "Goal Tip"
        val volumeLabel: String = m["volumeLabel"] ?: "Volume"
        val topExerciseLabel: String = m["topExerciseLabel"] ?: "Top Exercise"
        val workoutsLabel_: String = m["workoutsLabel"] ?: "Workouts"
        val bestStreakLabel_: String = m["bestStreakLabel"] ?: "Best Streak"
        val nutritionLabel: String = m["nutritionLabel"] ?: "Nutrition"
        val motivationLabel: String = m["motivationLabel"] ?: "Motivation"
        val gpsCardioMap: String = m["gpsCardioMap"] ?: ""
        val startTracking: String = m["startTracking"] ?: ""
        val stopTracking: String = m["stopTracking"] ?: ""
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
        val currentLocation: String = m["currentLocation"] ?: ""
        val trackingActive: String = m["trackingActive"] ?: ""
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
        val restDayRecommendation: String = m["restDayRecommendation"] ?: ""
        val nextRestDay: String = m["nextRestDay"] ?: ""
        val muscleNeedsRest: String = m["muscleNeedsRest"] ?: ""
        val recoveryComplete: String = m["recoveryComplete"] ?: ""
        val deloadInfo: String = m["deloadInfo"] ?: ""
        val suggestedActivities: String = m["suggestedActivities"] ?: ""
        val activeRecovery: String = m["activeRecovery"] ?: ""
        val lightWalk: String = m["lightWalk"] ?: ""
        val swimming: String = m["swimming"] ?: ""
        val mobilityWork: String = m["mobilityWork"] ?: ""
        val noRestDays: String = m["noRestDays"] ?: ""
        val selectDay: String = m["selectDay"] ?: ""
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
        val termsAndConditions: String = m["termsAndConditions"] ?: ""
        val termsPrefix: String = m["termsPrefix"] ?: ""
        val timeForDeload: String = m["timeForDeload"] ?: ""
        val weeks: String = m["weeks"] ?: ""
        val weeksSinceLastDeload: String = m["weeksSinceLastDeload"] ?: ""
        val weightKg: String = m["weightKg"] ?: ""
        val yogaDescription: String = m["yogaDescription"] ?: ""
        val tapToEdit: String = m["tapToEdit"] ?: ""
        val weeklyReminder: String = m["weeklyReminder"] ?: ""
        val mostTrained: String = m["mostTrained" ] ?: ""
        val allExercises: String = m["allExercises"] ?: ""
        val vsPrevious: String = m["vsPrevious"] ?: ""
        val previousPR: String = m["previousPR"] ?: ""
        val sessions: String = m["sessions"] ?: ""
        val noDataShort: String = m["noDataShort"] ?: ""
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


    private fun createRo() = Strings(mapOf(
        "appName" to "Kinetic", "dashboard" to "Panou", "overview" to "Rezumat", "acasa" to "Acasă", "workouts" to "Antrenamente", "stats" to "Statistici", "waterIntake" to "Apa", "waterGoal" to "Obiectiv apă", "addWater" to "Adaugă apă", "dailyWater" to "Apa zilnică", "height" to "Înălțime", "personalInfo" to "Informații personale", "waterAutoCalc" to "Calcul automat apă", "ml" to "ml", "templates" to "Șabloane", "recovery" to "Recuperare",
            "waterHistory" to "Istoric hidratare", "last7Days" to "Ultimele 7 zile", "everyDay" to "În fiecare zi", "reminder" to "Memento",
        "progress" to "Progres", "feed" to "Flux", "friends" to "Prieteni", "leaderboard" to "Clasament", "all" to "Toți",
        "settings" to "Setări", "language" to "Limbă", "units" to "Unități", "logout" to "Deconectare",
        "login" to "Autentificare", "signUp" to "Înregistrare", "email" to "Email", "password" to "Parolă",
        "forgotPassword" to "Ați uitat parola?", "orContinueWith" to "Sau continuați cu",
        "loginAsGuest" to "Conectare ca oaspete", "welcomeBack" to "Bine ați revenit!",
        "createAccount" to "Creați cont", "goalStrength" to "Forță", "goalMass" to "Masă musculară",
        "goalWeightLoss" to "Slăbit", "goalMaintenance" to "Menținere", "selectGoal" to "Selectați obiectivul",
        "next" to "Următorul", "skip" to "Sari", "finish" to "Termină", "back" to "Spate",
        "profileSetup" to "Setare profil", "enterName" to "Introduceți numele", "pickPhoto" to "Alegeți o fotografie",
        "saveProfile" to "Salvați profilul", "chest" to "Piept", "shoulders" to "Umeri", "arms" to "Brațe", "biceps" to "Biceps", "triceps" to "Triceps",
        "legs" to "Picioare", "thighs" to "Coapse", "glutes" to "Fese", "calves" to "Gâmbi", "core" to "Abdomen", "cardio" to "Cardio", "sets" to "Serie", "reps" to "Repetări",
        "weight" to "Greutate", "addExercise" to "Adaugă exercițiu", "saveWorkout" to "Salvează antrenament",
        "startWorkout" to "Începe antrenamentul", "nextExercise" to "Următorul exercițiu", "notes" to "Notițe", "cancel" to "Anulează", "confirm" to "Confirmă",
        "delete" to "Șterge", "edit" to "Editează", "search" to "Caută", "noDataYet" to "Nu există date încă",
        "friendRequests" to "Cereri de prietenie", "sendRequest" to "Trimite cerere", "accept" to "Acceptă",
        "reject" to "Respinge", "removeFriend" to "Elimină prieten", "noFriends" to "Niciun prieten",
        "searchUsers" to "Caută utilizatori", "userId" to "ID utilizator",
        "searchByNameOrId" to "Caută după nume sau ID", "incomingRequests" to "Cereri primite",
        "noIncomingRequests" to "Nicio cerere primită", "yourFriends" to "Prietenii tăi",
        "sendFriendRequest" to "Trimite cerere de prietenie", "friendRequestSent" to "Cererea a fost trimisă",
        "byId" to "După ID", "feedEmpty" to "Fluxul este gol", "postPlaceholder" to "Scrieți ceva...",
        "post" to "Postează", "comments" to "Comentarii", "like" to "Apreciază", "likes" to "Aprecieri",
        "share" to "Distribuie", "workoutCompleted" to "Antrenament completat!",
        "streakLabel" to "Șirul curent", "bestStreak" to "Cel mai bun șir", "badges" to "Distincții",
        "noBadges" to "Nicio distincție încă", "rank" to "Rang", "kg" to "kg", "lbs" to "lbs",
        "kgLbsToggle" to "Comută kg/lbs", "exportCsv" to "Exportă CSV", "importCsv" to "Importă CSV",
        "subscription" to "Abonament", "premium" to "Premium", "monthlyPlan" to "Plan lunar",
        "yearlyPlan" to "Plan anual", "subscribe" to "Abonează-te", "subscribed" to "Abonat",
        "notSubscribed" to "Neabonat", "darkMode" to "Mod întunecat", "lightMode" to "Mod luminos",
        "systemDefault" to "Sistem", "about" to "Despre", "version" to "Versiune",
        "totalWorkouts" to "Total antrenamente", "totalWeight" to "Total greutate",
        "personalRecords" to "Recorduri personale", "recentWorkouts" to "Antrenamente recente",
        "viewAll" to "Vezi toate", "loading" to "Se încarcă...", "error" to "Eroare", "retry" to "Reîncearcă",
        "success" to "Succes", "friendRequestAccepted" to "Cererea acceptată",
        "friendRequestRejected" to "Cererea respinsă", "profileUpdated" to "Profil actualizat",
        "workoutSaved" to "Antrenament salvat", "workoutDeleted" to "Antrenament șters",
        "noExercises" to "Niciun exercițiu", "selectExercises" to "Selectați exerciții",
        "exerciseList" to "Lista de exerciții", "customExercises" to "Exerciții personalizate",
        "defaultExercises" to "Exerciții implicite", "addCustomExercise" to "Adaugă exercițiu personalizat",
        "enterExerciseName" to "Introduceți numele exercițiului", "selectGroup" to "Selectați grupa",
        "addTemplate" to "Adaugă șablon", "templateName" to "Numele șablonului",
        "templateSaved" to "Șablon salvat", "templateDeleted" to "Șablon șters",
        "noTemplates" to "Niciun șablon", "createFirstTemplate" to "Creați primul șablon",
        "selectTemplate" to "Selectați șablonul", "useTemplate" to "Folosește șablonul",
        "deleteTemplate" to "Șterge șablonul", "recoveryInfo" to "Informații recuperare",
        "lastWorkout" to "Ultimul antrenament", "daysSince" to "Zile de la",
        "recommendedRecovery" to "Recuperare recomandată", "muscleGroupRecovery" to "Recuperare grupe musculare",
        "readyToTrain" to "Gata de antrenament!", "needsMoreRest" to "Mai are nevoie de odihnă",
        "todayIsRestDay" to "Astăzi e zi de odihnă", "progressChart" to "Grafic progres",
        "volumeOverTime" to "Volum în timp", "weightProgression" to "Progresie greutate",
        "frequencyChart" to "Grafic frecvență", "noChartData" to "Niciun date pentru grafic",
        "calendarView" to "Vezi calendar", "listView" to "Vedere listă", "sortBy" to "Sortează după",
        "sortByDate" to "După dată", "sortByGroup" to "După grupă", "filterByGroup" to "Filtrează după grupă",
        "allGroups" to "Toate", "welcomeTitle" to "Bine ați venit!",
        "welcomeSubtitle" to "Începeți călătoria dvs. fitness", "featureSocial" to "Social",
        "featureGamification" to "Jocuri", "featureCharts" to "Grafice", "featureExport" to "Export",
        "featureTemplates" to "Șabloane", "featureMultiLang" to "Multi-limbă", "notifications" to "Notificări",
        "enableNotifications" to "Activați notificările",
        "notificationPermissionRequired" to "Permisiunea de notificare este necesară",
        "friendRequestNotificationTitle" to "Cerere de prietenie",
        "friendRequestNotificationText" to "v-a trimis o cerere de prietenie!",
        "profilePhotoUpdated" to "Fotografia profilului actualizată", "nameRequired" to "Numele este obligatoriu",
        "settingsSaved" to "Setări salvate", "darkTheme" to "Temă întunecată",
        "lightTheme" to "Temă luminoasă", "systemTheme" to "Temă sistem",
        "selectLanguage" to "Selectați limba", "english" to "Engleză", "romanian" to "Română",
        "russian" to "Rusă", "ukrainian" to "Ucraineană", "french" to "Franceză", "german" to "Germană",
        "spanish" to "Spaniolă", "italian" to "Italiană", "turkish" to "Turcă",
        "portuguese" to "Portugheză", "polish" to "Poloneză", "leaderLabel" to "Lider",
        "workoutsLabel" to "Antrenamente", "totalVolume" to "Volum total",
        "currentStreakLabel" to "Șir curent", "bestStreakLabel" to "Cel mai bun șir",
        "badgesEarned" to "Distincții obținute", "days" to "zile", "badge" to "Distincție", "lastPR" to "Ultimul PR",
        "newExercise" to "Exercițiu nou", "exerciseNameLabel" to "Nume exercițiu", "add" to "Adaugă", "demoExercise" to "DEMO EXERCIȚIU", "setLabel" to "SET", "prAndVolume" to "PR-uri și volum", "start" to "Start", "stop" to "Stop", "noSavedSetsYet" to "Nu există seturi salvate încă.", "editSet" to "Editează set", "chooseTemplate" to "Alege un template de antrenament", "exercises" to "exerciții", "recovered" to "Recuperat", "almostRecovered" to "Aproape recuperat", "moderate" to "Moderat", "tired" to "Obosit", "exhausted" to "Epuizat", "fatigue" to "oboseală", "chooseMuscleGroup" to "Alege grupa musculară", "changeExercise" to "Schimbă exercițiul",
        "monthlyProgress" to "Progres lunar", "completeWorkoutsToSee" to "Completează antrenamente pentru a vedea progresul", "jan" to "Ian", "feb" to "Feb", "mar" to "Mar", "apr" to "Apr", "may" to "Mai", "jun" to "Iun", "jul" to "Iul", "aug" to "Aug", "sep" to "Sep", "oct" to "Oct", "nov" to "Noi", "dec" to "Dec", "monthlyDetails" to "Detalii lunare", "month" to "Lună", "mon" to "Lu", "tue" to "Ma", "wed" to "Mi", "thu" to "Jo", "fri" to "Vi", "sat" to "Sâ", "sun" to "Du", "noWorkouts" to "Niciun antrenament în această zi", "workoutDistribution" to "Distribuție antrenamente",
        "notNow" to "Mai târziu", "subscribeNow" to "Abonează-te acum", "premiumFeature" to "Funcție Premium", "subscribersOnly" to "\$feature este disponibil doar pentru abonați", "choosePlan" to "Alege un plan", "youAreSubscribed" to "Ești abonat!", "muscleRecovery" to "Recuperare musculară", "waterReminder" to "Memento apă", "waterReminderTitle" to "Timpul să bei apă!", "waterReminderText" to "Hidratează-te! Este timpul să bei un pahar cu apă.", "waterReminderEnabled" to "Activat", "waterReminderDisabled" to "Dezactivat", "selectTime" to "Selectează ora", "forearms" to "Antebrate", "neckAndTraps" to "Gât & Trapezi", "welcome" to "Bun venit", "athlete" to "Sportiv",
        "biometricTracking" to "Monitorizare biometrică", "biometricSubtitle" to "Greutate, circumferințe, grăsime corporală", "addMeasurement" to "Adaugă măsurătoare", "bodyFat" to "Grăsime corporală", "waistCirc" to "Talie", "hipsCirc" to "Solduri", "thighsCirc" to "Coapse", "chestCirc" to "Piept", "armsCirc" to "Brațe", "lastMeasurement" to "Ultima măsurătoare", "noMeasurements" to "Nicio măsurătoare încă", "viewCharts" to "Vezi grafice", "saveMeasurement" to "Salvează măsurătoarea", "measurementSaved" to "Măsurătoarea salvată", "weeksAgo" to "săptămâni în urmă", "cm" to "cm", "percent" to "%", "deleteMeasurement" to "Șterge măsurătoarea", "biometricHistory" to "Istoric măsurători", "weightChart" to "Grafic greutate", "bodyFatChart" to "Grafic grăsime corporală", "circumferenceChart" to "Grafic circumferințe", "date" to "Data", "biometricReminder" to "Memento biometric", "biometricReminderTitle" to "Timpul pentru măsurători!", "biometricReminderText" to "Nu uita să îți înregistrezi măsurătorile corporale săptămânale.", "biometricReminderEnabled" to "Activat", "biometricReminderDisabled" to "Dezactivat",
        "foodJournal" to "Jurnal alimentar", "scanBarcode" to "Scanează cod de bare", "scanBarcodeHint" to "Plasează codul de bare în cadru pentru a scana produsul", "cameraPermissionRequired" to "Permisiunea camerei este necesară pentru scanare", "scan" to "Scanează", "scanning" to "Se scanează...", "scanBarcodeHelp" to "Asigură-te că Google Play Services este instalat și updatat", "noFoodEntries" to "Nicio intrare alimentară încă", "todaysMacros" to "Macronutrienții de azi", "caloriesLabel" to "Calorii", "proteinLabel" to "Proteine", "carbsLabel" to "Carbo", "fatLabel" to "Grăsimi", "breakfast" to "Mic dejun", "lunch" to "Prânz", "dinner" to "Cină", "snack" to "Gustare", "drinks" to "Băuturi", "selectMealType" to "Selectează tipul mesei", "manualFoodEntry" to "Intrare manuală", "foodName" to "Nume aliment", "brandLabel" to "Marcă", "calories" to "Calorii", "protein" to "Proteine", "carbs" to "Carbohidrați", "fat" to "Grăsimi", "fiber" to "Fibre",
        "aiTrainer" to "Antrenor AI", "aiTrainerWelcome" to "Salut! Sunt antrenorul tău AI", "aiTrainerHint" to "Întreabă-mă orice despre antrenamente, nutriție sau progres", "aiTrainerHistory" to "Istoric conversații", "noHistoryYet" to "Nu există istoric", "current" to "Actual", "askAiTrainer" to "Întreabă antrenorul...", "aiSuggestion1" to "Ce antrenament îmi recomanzi azi?", "aiSuggestion2" to "Cum pot să sporesc volumul?", "aiSuggestion3" to "Am nevoie de o zi de odihnă?", "aiSuggestion4" to "Cum să ies din platou?",
        "viewProfile" to "Vezi profilul",
        "accountSettings" to "Setări cont", "deleteAccount" to "Șterge contul",
        "privacyPolicy" to "Politica de confidențialitate",
        "termsOfService" to "Condiții de utilizare", "back_" to "Înapoi",
        "restTimer" to "Timer pauză", "startTimer" to "Porneste timer", "customTimer" to "Timer personalizat",
        "seconds" to "Secunde", "custom" to "Personalizat",
        "exerciseHistory" to "Istoric exercițiu", "bestSet" to "Cel mai bun set", "lastSets" to "Ultimele serii",
        "favorite" to "Favorit", "favorites" to "Favoriți", "usageCount" to "Folosit de",
        "addSet" to "Adaugă serie", "exerciseNotes" to "Notițe exercițiu", "workoutNotes" to "Notițe antrenament",
        "saveNotes" to "Salvează notițele", "editWorkout" to "Editează antrenament",
        "volume" to "Volum", "maxWeight" to "Greutate max", "maxReps" to "Rep max", "maxSet" to "Set max",
        "today" to "Astăzi", "thisWeek" to "Această săptămână", "thisMonth" to "Această lună",
        "totalVolumeLabel" to "Volum total",
        "languageChanged" to "Limba a fost schimbată", "themeChanged" to "Tema a fost schimbată",
        "guest" to "Oaspete", "loginWithGoogle" to "Conectare cu Google", "loginWithFacebook" to "Conectare cu Facebook",
        "close" to "Închide", "menu" to "Meniu", "profile" to "Profil",
        "appTagline" to "Antrenează-te. Progresează. Repetă.", "or" to "sau", "dark" to "Întunecat", "light" to "Luminos",
        "system" to "Sistem", "languageTitle" to "Limbă", "themeTitle" to "Temă",
        "selectTheme" to "Selectează tema", "settingsAndMore" to "Setări și mai multe",
        "muscleGroups" to "Grupe musculare", "startHere" to "Începe aici", "back__" to "Înapoi",
        "englishUS" to "Engleză", "romana" to "Română", "russkiy" to "Rusă", "ukrainska" to "Ucraineană",
        "francais" to "Franceză", "deutsch" to "Germană", "espanol" to "Spaniolă",
        "italiano" to "Italiană", "turkce" to "Turcă", "portugues" to "Portugheză", "polski" to "Poloneză",
        "motto1" to "Fiecare repetiție contează.", "motto2" to "Mai puternic decât ieri.",
        "motto3" to "Corpul tău, regulile tale.", "motto4" to "Împinge-ți limitele.",
        "motto5" to "Constanta învinge talentul.", "motto6" to "Disciplina este libertate.",
        "motto7" to "Fără scurtături.", "motto8" to "Câștigat, nu primit.",
        "goodMorning" to "Bună dimineața", "goodAfternoon" to "Bună ziua", "goodEvening" to "Bună seara",
        "daysConsecutive" to "zile consecutive", "todaysWorkout" to "Antrenamentul de azi",
        "howDoYouFeel" to "Cum te simți azi?", "tiredLabel" to "Obosit", "normalLabel" to "Normal", "energeticLabel" to "Energic",
        "plusToday" to "Azi în plus", "technicalTip" to "Sfat tehnic",
        "tomorrowLabel" to "Mâine", "setWorkoutTime" to "Setează ora de antrenament",
        "daysSinceLastWorkout" to "Zile de la ultimul workout", "groupsFullyRecovered" to "Grupe complet recuperate",
        "recoveryOnGroups" to "Recuperare pe grupe",
        "weeklySummary" to "Rezumat săptămânal", "lastWeekLabel" to "săpt. trecută",
        "goalLabel" to "Sfat obiectiv", "volumeLabel" to "Volum", "topExerciseLabel" to "Top exercițiu",
        "nutritionLabel" to "Nutriție", "motivationLabel" to "Motivație",
        "gpsCardioMap" to "Cardio", "startTracking" to "Pornește urmărirea", "stopTracking" to "Oprește urmărirea",
        "pauseTracking" to "Pauză", "resumeTracking" to "Continuă",
        "distance" to "Distanță", "pace" to "Ritm", "speed" to "Viteză", "duration" to "Durată",
        "savedRoutes" to "Rute salvate", "noSavedRoutes" to "Nicio rută salvată încă",
        "routeName" to "Nume rută", "saveRoute" to "Salvează ruta", "deleteRoute" to "Șterge ruta",
        "currentLocation" to "Locația curentă", "trackingActive" to "Urmărire activă",
        "locationPermissionRequired" to "Permisiunea de locație este necesară",
        "restDaysTitle" to "Zile de odihnă & Deload", "restDaysSubtitle" to "Recuperare programată, întinderi, yoga ușoară",
        "deloadWeek" to "Săptămâna de deload", "recoverySchedule" to "Program recuperare",
        "stretching" to "Întinderi", "lightYoga" to "Yoga ușoară", "foamRolling" to "Foam rolling",
        "restDayRecommendation" to "Recomandare zi de odihnă", "nextRestDay" to "Următoarea zi de odihnă",
        "muscleNeedsRest" to "Mușchiul are nevoie de odihnă", "recoveryComplete" to "Recuperare completă",
        "deloadInfo" to "Informații deload", "suggestedActivities" to "Activități sugerate",
        "activeRecovery" to "Recuperare activă", "lightWalk" to "Plimbare ușoară",
        "swimming" to "Înot", "mobilityWork" to "Lucru de mobilitate",
        "noRestDays" to "Nicio zi de odihnă programată", "selectDay" to "Selectează ziua",
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
        "startDeload" to "Începe deload", "stretchingDescription" to "Îmbunătățește flexibilitatea și mobilitatea",
        "termsAndConditions" to "Termeni și Condiții", "termsPrefix" to "Prin continuare, ești de acord cu",
        "timeForDeload" to "E timpul pentru deload", "weeks" to "săptămâni",
        "weeksSinceLastDeload" to "Săptămâni de la ultimul deload", "weightKg" to "Greutate (kg)",
        "yogaDescription" to "Relaxare și mobilitate prin yoga ușoară",
        "tapToEdit" to "Apasă pentru a edita",
        "weeklyReminder" to "Memento săptămânal",
        "mostTrained" to "Cel mai exersat",
        "allExercises" to "Toate exercițiile",
        "vsPrevious" to "vs anterioară",
        "previousPR" to "PR anterior",
        "sessions" to "ședințe",
        "noDataShort" to "fără date"
    ))

    private fun createEn() = Strings(mapOf(
        "appName" to "Kinetic", "dashboard" to "Dashboard", "overview" to "Overview", "acasa" to "Home", "workouts" to "Workouts", "stats" to "Stats", "waterIntake" to "Water Intake", "waterGoal" to "Water Goal", "addWater" to "Add Water", "dailyWater" to "Daily Water", "height" to "Height", "personalInfo" to "Personal Info", "waterAutoCalc" to "Auto Water Calc", "ml" to "ml", "templates" to "Templates", "recovery" to "Recovery",
            "waterHistory" to "Water History", "last7Days" to "Last 7 days", "everyDay" to "Every day", "reminder" to "Reminder",
        "progress" to "Progress", "feed" to "Feed", "friends" to "Friends", "leaderboard" to "Leaderboard", "all" to "All",
        "settings" to "Settings", "language" to "Language", "units" to "Units", "logout" to "Logout",
        "login" to "Login", "signUp" to "Sign Up", "email" to "Email", "password" to "Password",
        "forgotPassword" to "Forgot password?", "orContinueWith" to "Or continue with",
        "loginAsGuest" to "Login as guest", "welcomeBack" to "Welcome back!",
        "createAccount" to "Create Account", "goalStrength" to "Strength", "goalMass" to "Muscle Mass",
        "goalWeightLoss" to "Weight Loss", "goalMaintenance" to "Maintenance",
        "selectGoal" to "Select your goal", "next" to "Next", "skip" to "Skip", "finish" to "Finish",
        "back" to "Back", "profileSetup" to "Profile Setup", "enterName" to "Enter your name",
        "pickPhoto" to "Pick a photo", "saveProfile" to "Save Profile", "chest" to "Chest",
        "shoulders" to "Shoulders", "arms" to "Arms", "biceps" to "Biceps", "triceps" to "Triceps",
        "legs" to "Legs", "thighs" to "Thighs", "glutes" to "Glutes", "calves" to "Calves", "core" to "Core", "cardio" to "Cardio",
        "sets" to "Sets", "reps" to "Reps", "weight" to "Weight", "addExercise" to "Add Exercise",
        "saveWorkout" to "Save Workout", "startWorkout" to "Start Workout", "nextExercise" to "Next Exercise", "notes" to "Notes",
        "cancel" to "Cancel", "confirm" to "Confirm", "delete" to "Delete", "edit" to "Edit",
        "search" to "Search", "noDataYet" to "No data yet", "friendRequests" to "Friend Requests",
        "sendRequest" to "Send Request", "accept" to "Accept", "reject" to "Reject",
        "removeFriend" to "Remove Friend", "noFriends" to "No friends yet", "searchUsers" to "Search Users",
        "userId" to "User ID", "searchByNameOrId" to "Search by name or ID",
        "incomingRequests" to "Incoming Requests", "noIncomingRequests" to "No incoming requests",
        "yourFriends" to "Your Friends", "sendFriendRequest" to "Send Friend Request",
        "friendRequestSent" to "Request Sent", "byId" to "By ID", "feedEmpty" to "Feed is empty",
        "postPlaceholder" to "Write something...", "post" to "Post", "comments" to "Comments",
        "like" to "Like", "likes" to "Likes", "share" to "Share", "workoutCompleted" to "Workout Complete!",
        "streakLabel" to "Current Streak", "bestStreak" to "Best Streak", "badges" to "Badges",
        "noBadges" to "No badges yet", "rank" to "Rank", "kg" to "kg", "lbs" to "lbs",
        "kgLbsToggle" to "Toggle kg/lbs", "exportCsv" to "Export CSV", "importCsv" to "Import CSV",
        "subscription" to "Subscription", "premium" to "Premium", "monthlyPlan" to "Monthly Plan",
        "yearlyPlan" to "Yearly Plan", "subscribe" to "Subscribe", "subscribed" to "Subscribed",
        "notSubscribed" to "Not Subscribed", "darkMode" to "Dark Mode", "lightMode" to "Light Mode",
        "systemDefault" to "System", "about" to "About", "version" to "Version",
        "totalWorkouts" to "Total Workouts", "totalWeight" to "Total Weight",
        "personalRecords" to "Personal Records", "recentWorkouts" to "Recent Workouts",
        "viewAll" to "View All", "loading" to "Loading...", "error" to "Error", "retry" to "Retry",
        "success" to "Success", "friendRequestAccepted" to "Friend request accepted",
        "friendRequestRejected" to "Friend request rejected", "profileUpdated" to "Profile Updated",
        "workoutSaved" to "Workout Saved", "workoutDeleted" to "Workout Deleted",
        "noExercises" to "No exercises", "selectExercises" to "Select Exercises",
        "exerciseList" to "Exercise List", "customExercises" to "Custom Exercises",
        "defaultExercises" to "Default Exercises", "addCustomExercise" to "Add Custom Exercise",
        "enterExerciseName" to "Enter exercise name", "selectGroup" to "Select Group",
        "addTemplate" to "Add Template", "templateName" to "Template Name",
        "templateSaved" to "Template Saved", "templateDeleted" to "Template Deleted",
        "noTemplates" to "No Templates", "createFirstTemplate" to "Create your first template",
        "selectTemplate" to "Select Template", "useTemplate" to "Use Template",
        "deleteTemplate" to "Delete Template", "recoveryInfo" to "Recovery Info",
        "lastWorkout" to "Last Workout", "daysSince" to "Days since",
        "recommendedRecovery" to "Recommended Recovery",
        "muscleGroupRecovery" to "Muscle Group Recovery", "readyToTrain" to "Ready to train!",
        "needsMoreRest" to "Needs more rest", "todayIsRestDay" to "Today is rest day",
        "progressChart" to "Progress Chart", "volumeOverTime" to "Volume Over Time",
        "weightProgression" to "Weight Progression", "frequencyChart" to "Frequency Chart",
        "noChartData" to "No chart data", "calendarView" to "See Calendar", "listView" to "List View",
        "sortBy" to "Sort By", "sortByDate" to "By Date", "sortByGroup" to "By Group",
        "filterByGroup" to "Filter by Group", "allGroups" to "All", "welcomeTitle" to "Welcome!",
        "welcomeSubtitle" to "Start your fitness journey", "featureSocial" to "Social",
        "featureGamification" to "Gamification", "featureCharts" to "Charts", "featureExport" to "Export",
        "featureTemplates" to "Templates", "featureMultiLang" to "Multi-language",
        "notifications" to "Notifications", "enableNotifications" to "Enable Notifications",
        "notificationPermissionRequired" to "Notification permission required",
        "friendRequestNotificationTitle" to "Friend Request",
        "friendRequestNotificationText" to "sent you a friend request!",
        "profilePhotoUpdated" to "Profile photo updated", "nameRequired" to "Name is required",
        "settingsSaved" to "Settings Saved", "darkTheme" to "Dark Theme",
        "lightTheme" to "Light Theme", "systemTheme" to "System Theme",
        "selectLanguage" to "Select Language", "english" to "English", "romanian" to "Romanian",
        "russian" to "Russian", "ukrainian" to "Ukrainian", "french" to "French", "german" to "German",
        "spanish" to "Spanish", "italian" to "Italian", "turkish" to "Turkish",
        "portuguese" to "Portuguese", "polish" to "Polish", "leaderLabel" to "Leader",
        "workoutsLabel" to "Workouts", "totalVolume" to "Total Volume",
        "currentStreakLabel" to "Current Streak", "bestStreakLabel" to "Best Streak",
        "badgesEarned" to "Badges Earned", "days" to "days", "badge" to "Badge", "lastPR" to "Last PR",
        "newExercise" to "New exercise", "exerciseNameLabel" to "Exercise name", "add" to "Add", "demoExercise" to "DEMO EXERCISE", "setLabel" to "SET", "prAndVolume" to "PRs and volume", "start" to "Start", "stop" to "Stop", "noSavedSetsYet" to "No saved sets yet.", "editSet" to "Edit set", "chooseTemplate" to "Choose a workout template", "exercises" to "exercises", "recovered" to "Recovered", "almostRecovered" to "Almost recovered", "moderate" to "Moderate", "tired" to "Tired", "exhausted" to "Exhausted", "fatigue" to "fatigue", "chooseMuscleGroup" to "Choose muscle group", "changeExercise" to "Change exercise",
        "monthlyProgress" to "Monthly progress", "completeWorkoutsToSee" to "Complete workouts to see progress", "jan" to "Jan", "feb" to "Feb", "mar" to "Mar", "apr" to "Apr", "may" to "May", "jun" to "Jun", "jul" to "Jul", "aug" to "Aug", "sep" to "Sep", "oct" to "Oct", "nov" to "Nov", "dec" to "Dec", "monthlyDetails" to "Monthly details", "month" to "Month", "mon" to "Mon", "tue" to "Tue", "wed" to "Wed", "thu" to "Thu", "fri" to "Fri", "sat" to "Sat", "sun" to "Sun", "noWorkouts" to "No workouts on this day", "workoutDistribution" to "Workout Distribution",
        "notNow" to "Not now", "subscribeNow" to "Subscribe Now", "premiumFeature" to "Premium Feature", "subscribersOnly" to "\$feature is available for subscribers only", "choosePlan" to "Choose a plan", "youAreSubscribed" to "You are subscribed!", "muscleRecovery" to "Muscle Recovery", "waterReminder" to "Water Reminder", "waterReminderTitle" to "Time to drink water!", "waterReminderText" to "Stay hydrated! It's time to drink a glass of water.", "waterReminderEnabled" to "Enabled", "waterReminderDisabled" to "Disabled", "selectTime" to "Select time", "forearms" to "Forearms", "neckAndTraps" to "Neck & Traps", "welcome" to "Welcome", "athlete" to "Athlete",
        "biometricTracking" to "Biometric Tracking", "biometricSubtitle" to "Weight, circumferences, body fat", "addMeasurement" to "Add measurement", "bodyFat" to "Body fat", "waistCirc" to "Waist", "hipsCirc" to "Hips", "thighsCirc" to "Thighs", "chestCirc" to "Chest", "armsCirc" to "Arms", "lastMeasurement" to "Last measurement", "noMeasurements" to "No measurements yet", "viewCharts" to "View charts", "saveMeasurement" to "Save measurement", "measurementSaved" to "Measurement saved", "weeksAgo" to "weeks ago", "cm" to "cm", "percent" to "%", "deleteMeasurement" to "Delete measurement", "biometricHistory" to "Measurement history", "weightChart" to "Weight chart", "bodyFatChart" to "Body fat chart", "circumferenceChart" to "Circumference chart", "date" to "Date", "biometricReminder" to "Biometric Reminder", "biometricReminderTitle" to "Time for measurements!", "biometricReminderText" to "Don't forget to log your weekly body measurements.", "biometricReminderEnabled" to "Enabled", "biometricReminderDisabled" to "Disabled",
        "foodJournal" to "Food Journal", "scanBarcode" to "Scan Barcode", "scanBarcodeHint" to "Place the barcode in frame to scan the product", "cameraPermissionRequired" to "Camera permission is required for scanning", "scan" to "Scan", "scanning" to "Scanning...", "scanBarcodeHelp" to "Make sure Google Play Services is installed and updated", "noFoodEntries" to "No food entries yet", "todaysMacros" to "Today's Macros", "caloriesLabel" to "Calories", "proteinLabel" to "Protein", "carbsLabel" to "Carbs", "fatLabel" to "Fat", "breakfast" to "Breakfast", "lunch" to "Lunch", "dinner" to "Dinner", "snack" to "Snack", "drinks" to "Drinks", "selectMealType" to "Select meal type", "manualFoodEntry" to "Manual Entry", "foodName" to "Food name", "brandLabel" to "Brand", "calories" to "Calories", "protein" to "Protein", "carbs" to "Carbs", "fat" to "Fat", "fiber" to "Fiber",
        "aiTrainer" to "AI Trainer", "aiTrainerWelcome" to "Hi! I'm your AI trainer", "aiTrainerHint" to "Ask me anything about workouts, nutrition or progress", "aiTrainerHistory" to "Chat history", "noHistoryYet" to "No history yet", "current" to "Current", "askAiTrainer" to "Ask the trainer...", "aiSuggestion1" to "What workout do you recommend today?", "aiSuggestion2" to "How can I increase volume?", "aiSuggestion3" to "Do I need a rest day?", "aiSuggestion4" to "How do I break through a plateau?",
        "viewProfile" to "View Profile",
        "accountSettings" to "Account Settings", "deleteAccount" to "Delete Account",
        "privacyPolicy" to "Privacy Policy", "termsOfService" to "Terms of Service",
        "back_" to "Back",
        "restTimer" to "Rest Timer", "startTimer" to "Start Timer", "customTimer" to "Custom Timer",
        "seconds" to "Seconds", "custom" to "Custom",
        "exerciseHistory" to "Exercise History", "bestSet" to "Best Set", "lastSets" to "Last Sets",
        "favorite" to "Favorite", "favorites" to "Favorites", "usageCount" to "Used",
        "addSet" to "Add Set", "exerciseNotes" to "Exercise Notes", "workoutNotes" to "Workout Notes",
        "saveNotes" to "Save Notes", "editWorkout" to "Edit Workout",
        "volume" to "Volume", "maxWeight" to "Max Weight", "maxReps" to "Max Reps", "maxSet" to "Max Set",
        "today" to "Today", "thisWeek" to "This Week", "thisMonth" to "This Month",
        "totalVolumeLabel" to "Total Volume",
        "languageChanged" to "Language changed", "themeChanged" to "Theme changed",
        "guest" to "Guest", "loginWithGoogle" to "Sign in with Google", "loginWithFacebook" to "Sign in with Facebook",
        "close" to "Close", "menu" to "Menu", "profile" to "Profile",
        "appTagline" to "Train. Progress. Repeat.", "or" to "or", "dark" to "Dark", "light" to "Light",
        "system" to "System", "languageTitle" to "Language", "themeTitle" to "Theme",
        "selectTheme" to "Select Theme", "settingsAndMore" to "Settings & More",
        "muscleGroups" to "Muscle Groups", "startHere" to "Start Here", "back__" to "Back",
        "englishUS" to "English", "romana" to "Romanian", "russkiy" to "Russian", "ukrainska" to "Ukrainian",
        "francais" to "French", "deutsch" to "German", "espanol" to "Spanish",
        "italiano" to "Italian", "turkce" to "Turkish", "portugues" to "Portuguese", "polski" to "Polish",
        "motto1" to "Every rep counts.", "motto2" to "Stronger than yesterday.",
        "motto3" to "Your body, your rules.", "motto4" to "Push your limits.",
        "motto5" to "Consistency beats talent.", "motto6" to "Discipline is freedom.",
        "motto7" to "No shortcuts.", "motto8" to "Earned, not given.",
        "goodMorning" to "Good morning", "goodAfternoon" to "Good afternoon", "goodEvening" to "Good evening",
        "daysConsecutive" to "days consecutive", "todaysWorkout" to "Today's Workout",
        "howDoYouFeel" to "How do you feel?", "tiredLabel" to "Tired", "normalLabel" to "Normal", "energeticLabel" to "Energic",
        "plusToday" to "Plus today", "technicalTip" to "Technical tip",
        "tomorrowLabel" to "Tomorrow", "setWorkoutTime" to "Set workout time",
        "daysSinceLastWorkout" to "Days since last workout", "groupsFullyRecovered" to "Groups fully recovered",
        "recoveryOnGroups" to "Recovery on groups",
        "weeklySummary" to "Weekly Summary", "lastWeekLabel" to "last week",
        "goalLabel" to "Goal tip", "volumeLabel" to "Volume", "topExerciseLabel" to "Top exercise",
        "nutritionLabel" to "Nutrition", "motivationLabel" to "Motivation",
        "gpsCardioMap" to "Cardio", "startTracking" to "Start Tracking", "stopTracking" to "Stop Tracking",
        "pauseTracking" to "Pause", "resumeTracking" to "Resume",
        "distance" to "Distance", "pace" to "Pace", "speed" to "Speed", "duration" to "Duration",
        "savedRoutes" to "Saved Routes", "noSavedRoutes" to "No saved routes yet",
        "routeName" to "Route Name", "saveRoute" to "Save Route", "deleteRoute" to "Delete Route",
        "currentLocation" to "Current Location", "trackingActive" to "Tracking Active",
        "locationPermissionRequired" to "Location permission is required",
        "restDaysTitle" to "Rest Days & Deload", "restDaysSubtitle" to "Auto-schedule recovery, stretching, light yoga",
        "deloadWeek" to "Deload Week", "recoverySchedule" to "Recovery Schedule",
        "stretching" to "Stretching", "lightYoga" to "Light Yoga", "foamRolling" to "Foam Rolling",
        "restDayRecommendation" to "Rest Day Recommendation", "nextRestDay" to "Next Rest Day",
        "muscleNeedsRest" to "Muscle needs rest", "recoveryComplete" to "Recovery Complete",
        "deloadInfo" to "Deload Info", "suggestedActivities" to "Suggested Activities",
        "activeRecovery" to "Active Recovery", "lightWalk" to "Light Walk",
        "swimming" to "Swimming", "mobilityWork" to "Mobility Work",
        "noRestDays" to "No rest days scheduled", "selectDay" to "Select day",
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
        "startDeload" to "Start deload", "stretchingDescription" to "Improve flexibility and mobility",
        "termsAndConditions" to "Terms and Conditions", "termsPrefix" to "By continuing, you agree to our",
        "timeForDeload" to "Time for deload", "weeks" to "weeks",
        "weeksSinceLastDeload" to "Weeks since last deload", "weightKg" to "Weight (kg)",
        "yogaDescription" to "Relax and improve mobility with light yoga", "tapToEdit" to "Tap to edit", "weeklyReminder" to "Weekly reminder",
        "mostTrained" to "Most Trained", "allExercises" to "All Exercises",
        "vsPrevious" to "vs previous", "previousPR" to "Previous PR",
        "sessions" to "sessions", "noDataShort" to "no data"
    ))

    private fun createRu() = Strings(mapOf(
        "appName" to "Kinetic", "dashboard" to "Панель", "overview" to "Обзор", "acasa" to "Главная", "workouts" to "Тренировки", "stats" to "Статистика", "waterIntake" to "Потребление воды", "waterGoal" to "Цель воды", "addWater" to "Добавить воду", "dailyWater" to "Вода за день", "height" to "Рост", "personalInfo" to "Личная информация", "waterAutoCalc" to "Авторасчет воды", "ml" to "мл", "templates" to "Шаблоны", "recovery" to "Восстановление",
            "waterHistory" to "История гидратации", "last7Days" to "Последние 7 дней", "everyDay" to "Каждый день", "reminder" to "Напоминание",
        "progress" to "Прогресс", "feed" to "Лента", "friends" to "Друзья", "leaderboard" to "Таблица лидеров", "all" to "Все",
        "settings" to "Настройки", "language" to "Язык", "units" to "Единицы", "logout" to "Выход",
        "login" to "Вход", "signUp" to "Регистрация", "email" to "Эл. почта", "password" to "Пароль",
        "forgotPassword" to "Забыли пароль?", "orContinueWith" to "Или продолжить с",
        "loginAsGuest" to "Войти как гость", "welcomeBack" to "С возвращением!",
        "createAccount" to "Создать аккаунт", "goalStrength" to "Сила", "goalMass" to "Мышечная масса",
        "goalWeightLoss" to "Похудение", "goalMaintenance" to "Поддержание",
        "selectGoal" to "Выберите цель", "next" to "Далее", "skip" to "Пропустить", "finish" to "Готово",
        "back" to "Назад", "profileSetup" to "Настройка профиля", "enterName" to "Введите имя",
        "pickPhoto" to "Выберите фото", "saveProfile" to "Сохранить профиль", "chest" to "Грудь",
        "shoulders" to "Плечи", "arms" to "Руки", "biceps" to "Бицепс", "triceps" to "Трицепс",
        "legs" to "Ноги", "thighs" to "Бёдра", "glutes" to "Ягодичные", "calves" to "Икры", "core" to "Кор", "cardio" to "Кардио",
        "sets" to "Подходы", "reps" to "Повторы", "weight" to "Вес", "addExercise" to "Добавить упражнение",
        "saveWorkout" to "Сохранить тренировку", "startWorkout" to "Начать тренировку", "nextExercise" to "Следующее упражнение",
        "notes" to "Заметки", "cancel" to "Отмена", "confirm" to "Подтвердить", "delete" to "Удалить",
        "edit" to "Редактировать", "search" to "Поиск", "noDataYet" to "Данных пока нет",
        "friendRequests" to "Заявки в друзья", "sendRequest" to "Отправить заявку",
        "accept" to "Принять", "reject" to "Отклонить", "removeFriend" to "Удалить друга",
        "noFriends" to "Пока нет друзей", "searchUsers" to "Поиск пользователей",
        "userId" to "ID пользователя", "searchByNameOrId" to "Поиск по имени или ID",
        "incomingRequests" to "Входящие заявки", "noIncomingRequests" to "Нет входящих заявок",
        "yourFriends" to "Ваши друзья", "sendFriendRequest" to "Отправить заявку в друзья",
        "friendRequestSent" to "Заявка отправлена", "byId" to "По ID", "feedEmpty" to "Лента пуста",
        "postPlaceholder" to "Напишите что-нибудь...", "post" to "Опубликовать",
        "comments" to "Комментарии", "like" to "Нравится", "likes" to "Нравится",
        "share" to "Поделиться", "workoutCompleted" to "Тренировка завершена!",
        "streakLabel" to "Текущая серия", "bestStreak" to "Лучшая серия", "badges" to "Значки",
        "noBadges" to "Пока нет значков", "rank" to "Ранг", "kg" to "кг", "lbs" to "фунты",
        "kgLbsToggle" to "Переключить кг/фунты", "exportCsv" to "Экспорт CSV",
        "importCsv" to "Импорт CSV", "subscription" to "Подписка", "premium" to "Премиум",
        "monthlyPlan" to "Месячный план", "yearlyPlan" to "Годовой план",
        "subscribe" to "Подписаться", "subscribed" to "Подписан", "notSubscribed" to "Не подписан",
        "darkMode" to "Тёмная тема", "lightMode" to "Светлая тема", "systemDefault" to "Системная",
        "about" to "О приложении", "version" to "Версия", "totalWorkouts" to "Всего тренировок",
        "totalWeight" to "Общий вес", "personalRecords" to "Личные рекорды",
        "recentWorkouts" to "Недавние тренировки", "viewAll" to "Показать все",
        "loading" to "Загрузка...", "error" to "Ошибка", "retry" to "Повторить", "success" to "Успешно",
        "friendRequestAccepted" to "Заявка принята", "friendRequestRejected" to "Заявка отклонена",
        "profileUpdated" to "Профиль обновлён", "workoutSaved" to "Тренировка сохранена",
        "workoutDeleted" to "Тренировка удалена", "noExercises" to "Нет упражнений",
        "selectExercises" to "Выберите упражнения", "exerciseList" to "Список упражнений",
        "customExercises" to "Пользовательские упражнения",
        "defaultExercises" to "Упражнения по умолчанию",
        "addCustomExercise" to "Добавить упражнение",
        "enterExerciseName" to "Введите название упражнения", "selectGroup" to "Выберите группу",
        "addTemplate" to "Добавить шаблон", "templateName" to "Название шаблона",
        "templateSaved" to "Шаблон сохранён", "templateDeleted" to "Шаблон удалён",
        "noTemplates" to "Нет шаблонов", "createFirstTemplate" to "Создайте первый шаблон",
        "selectTemplate" to "Выберите шаблон", "useTemplate" to "Использовать шаблон",
        "deleteTemplate" to "Удалить шаблон", "recoveryInfo" to "Информация о восстановлении",
        "lastWorkout" to "Последняя тренировка", "daysSince" to "Дней с",
        "recommendedRecovery" to "Рекомендуемое восстановление",
        "muscleGroupRecovery" to "Восстановление мышечных групп",
        "readyToTrain" to "Готовы к тренировке!", "needsMoreRest" to "Нужен отдых",
        "todayIsRestDay" to "Сегодня день отдыха", "progressChart" to "График прогресса",
        "volumeOverTime" to "Объём во времени", "weightProgression" to "Прогрессия веса",
        "frequencyChart" to "График частоты", "noChartData" to "Нет данных для графика",
        "calendarView" to "Календарь", "listView" to "Список", "sortBy" to "Сортировка",
        "sortByDate" to "По дате", "sortByGroup" to "По группе", "filterByGroup" to "Фильтр по группе",
        "allGroups" to "Все", "welcomeTitle" to "Добро пожаловать!",
        "welcomeSubtitle" to "Начните ваш путь к fitness", "featureSocial" to "Социальное",
        "featureGamification" to "Игры", "featureCharts" to "Графики", "featureExport" to "Экспорт",
        "featureTemplates" to "Шаблоны", "featureMultiLang" to "Мультиязычный",
        "notifications" to "Уведомления", "enableNotifications" to "Включить уведомления",
        "notificationPermissionRequired" to "Требуется разрешение на уведомления",
        "friendRequestNotificationTitle" to "Заявка в друзья",
        "friendRequestNotificationText" to "отправил(а) вам заявку в друзья!",
        "profilePhotoUpdated" to "Фото профиля обновлено", "nameRequired" to "Имя обязательно",
        "settingsSaved" to "Настройки сохранены", "darkTheme" to "Тёмная тема",
        "lightTheme" to "Светлая тема", "systemTheme" to "Системная тема",
        "selectLanguage" to "Выберите язык", "english" to "Английский", "romanian" to "Румынский",
        "russian" to "Русский", "ukrainian" to "Украинский", "french" to "Французский",
        "german" to "Немецкий", "spanish" to "Испанский", "italian" to "Итальянский",
        "turkish" to "Турецкий", "portuguese" to "Португальский", "polish" to "Польский",
        "leaderLabel" to "Лидер", "workoutsLabel" to "Тренировки", "totalVolume" to "Общий объём",
        "currentStreakLabel" to "Текущая серия", "bestStreakLabel" to "Лучшая серия",
        "badgesEarned" to "Заработанные значки", "days" to "дней", "badge" to "Значок", "lastPR" to "Последний PR",
        "newExercise" to "Новое упражнение", "exerciseNameLabel" to "Название упражнения", "add" to "Добавить", "demoExercise" to "ДЕМО УПРАЖНЕНИЕ", "setLabel" to "ПОДХОД", "prAndVolume" to "Личные рекорды и объём", "start" to "Старт", "stop" to "Стоп", "noSavedSetsYet" to "Нет сохранённых подходов.", "editSet" to "Редактировать подход", "chooseTemplate" to "Выберите шаблон тренировки", "exercises" to "упражнений", "recovered" to "Восстановлен", "almostRecovered" to "Почти восстановлен", "moderate" to "Умеренно", "tired" to "Устал", "exhausted" to "Истощён", "fatigue" to "усталость", "chooseMuscleGroup" to "Выберите группу мышц", "changeExercise" to "Заменить упражнение",
        "monthlyProgress" to "Прогресс за месяц", "completeWorkoutsToSee" to "Завершите тренировки чтобы увидеть прогресс", "jan" to "Янв", "feb" to "Фев", "mar" to "Мар", "apr" to "Апр", "may" to "Май", "jun" to "Июн", "jul" to "Июл", "aug" to "Авг", "sep" to "Сен", "oct" to "Окт", "nov" to "Ноя", "dec" to "Дек", "monthlyDetails" to "Детали месяца", "month" to "Месяц", "mon" to "Пн", "tue" to "Вт", "wed" to "Ср", "thu" to "Чт", "fri" to "Пт", "sat" to "Сб", "sun" to "Вс", "noWorkouts" to "Нет тренировок в этот день", "workoutDistribution" to "Распределение тренировок",
        "notNow" to "Не сейчас", "subscribeNow" to "Подписаться", "premiumFeature" to "Премиум функция", "subscribersOnly" to "\$feature доступно только для подписчиков", "choosePlan" to "Выберите план", "youAreSubscribed" to "Вы подписаны!", "muscleRecovery" to "Восстановление мышц", "waterReminder" to "Напоминание о воде", "waterReminderTitle" to "Время пить воду!", "waterReminderText" to "Пейте воду! Самое время выпить стакан воды.", "waterReminderEnabled" to "Включено", "waterReminderDisabled" to "Выключено", "selectTime" to "Выбрать время", "forearms" to "Предплечья", "neckAndTraps" to "Шея и Трапеции", "welcome" to "Добро пожаловать", "athlete" to "Спортсмен",
        "biometricTracking" to "Биометрический мониторинг", "biometricSubtitle" to "Вес, обхваты, жировая прослойка", "addMeasurement" to "Добавить измерение", "bodyFat" to "Жировая прослойка", "waistCirc" to "Талия", "hipsCirc" to "Бёдра", "thighsCirc" to "Бедра", "chestCirc" to "Грудь", "armsCirc" to "Руки", "lastMeasurement" to "Последнее измерение", "noMeasurements" to "Измерений пока нет", "viewCharts" to "Посмотреть графики", "saveMeasurement" to "Сохранить измерение", "measurementSaved" to "Измерение сохранено", "weeksAgo" to "недель назад", "cm" to "см", "percent" to "%", "deleteMeasurement" to "Удалить измерение", "biometricHistory" to "История измерений", "weightChart" to "График веса", "bodyFatChart" to "График жира", "circumferenceChart" to "График обхватов", "date" to "Дата", "biometricReminder" to "Биометрическое напоминание", "biometricReminderTitle" to "Время для измерений!", "biometricReminderText" to "Не забудьте записать еженедельные измерения тела.", "biometricReminderEnabled" to "Включено", "biometricReminderDisabled" to "Выключено",
        "foodJournal" to "Дневник питания", "scanBarcode" to "Сканировать штрих-код", "scanBarcodeHint" to "Поместите штрих-код в кадр для сканирования продукта", "cameraPermissionRequired" to "Для сканирования необходим доступ к камере", "scan" to "Сканировать", "scanning" to "Сканирование...", "scanBarcodeHelp" to "Убедитесь, что Google Play Services установлен и обновлен", "noFoodEntries" to "Записей о еде пока нет", "todaysMacros" to "Макронутриенты сегодня", "caloriesLabel" to "Калории", "proteinLabel" to "Белки", "carbsLabel" to "Углеводы", "fatLabel" to "Жиры", "breakfast" to "Завтрак", "lunch" to "Обед", "dinner" to "Ужин", "snack" to "Перекус", "drinks" to "Напитки", "selectMealType" to "Выберите тип приема пищи", "manualFoodEntry" to "Ручной ввод", "foodName" to "Название продукта", "brandLabel" to "Бренд", "calories" to "Калории", "protein" to "Белки", "carbs" to "Углеводы", "fat" to "Жиры", "fiber" to "Клетчатка",
        "aiTrainer" to "ИИ Тренер", "aiTrainerWelcome" to "Привет! Я ваш ИИ тренер", "aiTrainerHint" to "Спросите меня о тренировках, питании или прогрессе", "aiTrainerHistory" to "История чатов", "noHistoryYet" to "История пуста", "current" to "Текущий", "askAiTrainer" to "Спросите тренера...", "aiSuggestion1" to "Какую тренировку вы рекомендуете сегодня?", "aiSuggestion2" to "Как увеличить объём?", "aiSuggestion3" to "Нужен ли мне день отдыха?", "aiSuggestion4" to "Как выйти из плато?",
        "viewProfile" to "Посмотреть профиль",
        "accountSettings" to "Настройки аккаунта", "deleteAccount" to "Удалить аккаунт",
        "privacyPolicy" to "Политика конфиденциальности",
        "termsOfService" to "Условия использования", "back_" to "Назад",
        "restTimer" to "Таймер отдыха", "startTimer" to "Старт таймер", "customTimer" to "Свой таймер",
        "seconds" to "Секунды", "custom" to "Свой",
        "exerciseHistory" to "История упражнения", "bestSet" to "Лучший подход", "lastSets" to "Последние подходы",
        "favorite" to "Избранное", "favorites" to "Избранные", "usageCount" to "Использован",
        "addSet" to "Добавить подход", "exerciseNotes" to "Заметки", "workoutNotes" to "Заметки тренировки",
        "saveNotes" to "Сохранить", "editWorkout" to "Редактировать",
        "volume" to "Объём", "maxWeight" to "Макс вес", "maxReps" to "Макс повторы", "maxSet" to "Макс подход",
        "today" to "Сегодня", "thisWeek" to "Эта неделя", "thisMonth" to "Этот месяц",
        "totalVolumeLabel" to "Общий объём",
        "languageChanged" to "Язык изменён", "themeChanged" to "Тема изменена",
        "guest" to "Гость", "loginWithGoogle" to "Войти через Google", "loginWithFacebook" to "Войти через Facebook",
        "close" to "Закрыть", "menu" to "Меню", "profile" to "Профиль",
        "appTagline" to "Тренируйся. Прогрессируй. Повторяй.", "or" to "или", "dark" to "Тёмная", "light" to "Светлая",
        "system" to "Системная", "languageTitle" to "Язык", "themeTitle" to "Тема",
        "selectTheme" to "Выбрать тему", "settingsAndMore" to "Настройки и ещё",
        "muscleGroups" to "Мышечные группы", "startHere" to "Начать здесь", "back__" to "Назад",
        "englishUS" to "Английский", "romana" to "Румынский", "russkiy" to "Русский", "ukrainska" to "Украинский",
        "francais" to "Французский", "deutsch" to "Немецкий", "espanol" to "Испанский",
        "italiano" to "Итальянский", "turkce" to "Турецкий", "portugues" to "Португальский", "polski" to "Польский",
        "motto1" to "Каждое повторение на счету.", "motto2" to "Сильнее, чем вчера.",
        "motto3" to "Твоё тело, твои правила.", "motto4" to "Превзойди свои пределы.",
        "motto5" to "Последовательность побеждает талант.", "motto6" to "Дисциплина — это свобода.",
        "motto7" to "Без коротких путей.", "motto8" to "Заработано, а не дано.",
        "goodMorning" to "Доброе утро", "goodAfternoon" to "Добрый день", "goodEvening" to "Добрый вечер",
        "daysConsecutive" to "дней подряд", "todaysWorkout" to "Тренировка сегодня",
        "howDoYouFeel" to "Как вы себя чувствуете?", "tiredLabel" to "Устал", "normalLabel" to "Нормально", "energeticLabel" to "Энергичен",
        "plusToday" to "Ещё сегодня", "technicalTip" to "Технический совет",
        "tomorrowLabel" to "Завтра", "setWorkoutTime" to "Установить время тренировки",
        "daysSinceLastWorkout" to "Дней с последней тренировки", "groupsFullyRecovered" to "Группы полностью восстановлены",
        "recoveryOnGroups" to "Восстановление групп",
        "weeklySummary" to "Итоги недели", "lastWeekLabel" to "прошл. неделя",
        "goalLabel" to "Совет по цели", "volumeLabel" to "Объём", "topExerciseLabel" to "Топ упражнение",
        "nutritionLabel" to "Питание", "motivationLabel" to "Мотивация",
        "gpsCardioMap" to "Cardio", "startTracking" to "Начать отслеживание", "stopTracking" to "Остановить отслеживание",
        "pauseTracking" to "Пауза", "resumeTracking" to "Продолжить",
        "distance" to "Дистанция", "pace" to "Темп", "speed" to "Скорость", "duration" to "Длительность",
        "savedRoutes" to "Сохранённые маршруты", "noSavedRoutes" to "Нет сохранённых маршрутов",
        "routeName" to "Название маршрута", "saveRoute" to "Сохранить маршрут", "deleteRoute" to "Удалить маршрут",
        "currentLocation" to "Текущее местоположение", "trackingActive" to "Отслеживание активно",
        "locationPermissionRequired" to "Требуется разрешение на местоположение",
        "restDaysTitle" to "Дни отдыха и разгрузка", "restDaysSubtitle" to "Автоматическое планирование восстановления, растяжки, лёгкой йоги",
        "deloadWeek" to "Неделя разгрузки", "recoverySchedule" to "График восстановления",
        "stretching" to "Растяжка", "lightYoga" to "Лёгкая йога", "foamRolling" to "Фоамроллинг",
        "restDayRecommendation" to "Рекомендация дня отдыха", "nextRestDay" to "Следующий день отдыха",
        "muscleNeedsRest" to "Мышцам нужен отдых", "recoveryComplete" to "Восстановление завершено",
        "deloadInfo" to "Информация о разгрузке", "suggestedActivities" to "Рекомендованные активности",
        "activeRecovery" to "Активное восстановление", "lightWalk" to "Лёгкая прогулка",
        "swimming" to "Плавание", "mobilityWork" to "Упражнения на мобильность",
        "noRestDays" to "Нет запланированных дней отдыха", "selectDay" to "Выберите день",
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
        "startDeload" to "Начать разгрузку", "stretchingDescription" to "Улучшите гибкость и мобильность",
        "termsAndConditions" to "Условия использования", "termsPrefix" to "Продолжая, вы соглашаетесь с нашими",
        "timeForDeload" to "Время для разгрузки", "weeks" to "недель",
        "weeksSinceLastDeload" to "Недель с последней разгрузки", "weightKg" to "Вес (кг)",
        "yogaDescription" to "Расслабьтесь и улучшите мобильность с помощью йоги", "tapToEdit" to "Нажмите для редактирования", "weeklyReminder" to "Еженедельное напоминание"
    ))

    private fun createUk() = Strings(mapOf(
        "appName" to "Kinetic", "dashboard" to "Панель", "overview" to "Огляд", "acasa" to "Головна", "workouts" to "Тренування", "stats" to "Статистика", "waterIntake" to "Споживання води", "waterGoal" to "Мета води", "addWater" to "Додати воду", "dailyWater" to "Вода за день", "height" to "Зріст", "personalInfo" to "Особиста інформація", "waterAutoCalc" to "Авторозрахунок води", "ml" to "мл", "templates" to "Шаблони",
            "waterHistory" to "Історія гідратації", "last7Days" to "Останні 7 днів", "everyDay" to "Щодня", "reminder" to "Нагадування",
        "recovery" to "Відновлення", "progress" to "Прогрес", "feed" to "Стрічка", "friends" to "Друзі",
        "leaderboard" to "Таблиця лідерів", "all" to "Усі", "settings" to "Налаштування", "language" to "Мова",
        "units" to "Одиниці", "logout" to "Вихід", "login" to "Вхід", "signUp" to "Реєстрація",
        "email" to "Ел. пошта", "password" to "Пароль", "forgotPassword" to "Забули пароль?",
        "orContinueWith" to "Або продовжити з", "loginAsGuest" to "Увійти як гість",
        "welcomeBack" to "З поверненням!", "createAccount" to "Створити акаунт",
        "goalStrength" to "Сила", "goalMass" to "М'язова маса", "goalWeightLoss" to "Схуднення",
        "goalMaintenance" to "Підтримання", "selectGoal" to "Оберіть ціль", "next" to "Далі",
        "skip" to "Пропустити", "finish" to "Готово", "back" to "Назад",
        "profileSetup" to "Налаштування профілю", "enterName" to "Введіть ім'я",
        "pickPhoto" to "Оберіть фото", "saveProfile" to "Зберегти профіль", "chest" to "Груди",
        "shoulders" to "Плечі", "arms" to "Руки", "biceps" to "Біцепс", "triceps" to "Трицепс",
        "legs" to "Ноги", "thighs" to "Стегна", "glutes" to "Сідничні", "calves" to "Гомілки", "core" to "Кор", "cardio" to "Кардіо",
        "sets" to "Підходи", "reps" to "Повтори", "weight" to "Вага", "addExercise" to "Додати вправу",
        "saveWorkout" to "Зберегти тренування", "startWorkout" to "Почати тренування", "nextExercise" to "Наступна вправа",
        "notes" to "Нотатки", "cancel" to "Скасувати", "confirm" to "Підтвердити", "delete" to "Видалити",
        "edit" to "Редагувати", "search" to "Пошук", "noDataYet" to "Даних поки немає",
        "friendRequests" to "Запити в друзі", "sendRequest" to "Надіслати запит",
        "accept" to "Прийняти", "reject" to "Відхилити", "removeFriend" to "Видалити друга",
        "noFriends" to "Поки немає друзів", "searchUsers" to "Пошук користувачів",
        "userId" to "ID користувача", "searchByNameOrId" to "Пошук за ім'ям або ID",
        "incomingRequests" to "Вхідні запити", "noIncomingRequests" to "Немає вхідних запитів",
        "yourFriends" to "Ваші друзі", "sendFriendRequest" to "Надіслати запит в друзі",
        "friendRequestSent" to "Запит надіслано", "byId" to "За ID", "feedEmpty" to "Стрічка порожня",
        "postPlaceholder" to "Напишіть щось...", "post" to "Опублікувати", "comments" to "Коментарі",
        "like" to "Подобається", "likes" to "Подобається", "share" to "Поділитися",
        "workoutCompleted" to "Тренування завершено!", "streakLabel" to "Поточна серія",
        "bestStreak" to "Найкраща серія", "badges" to "Значки", "noBadges" to "Поки немає значків",
        "rank" to "Ранг", "kg" to "кг", "lbs" to "фунти", "kgLbsToggle" to "Переключити кг/фунти",
        "exportCsv" to "Експорт CSV", "importCsv" to "Імпорт CSV", "subscription" to "Підписка",
        "premium" to "Преміум", "monthlyPlan" to "Місячний план", "yearlyPlan" to "Річний план",
        "subscribe" to "Підписатися", "subscribed" to "Підписано", "notSubscribed" to "Не підписано",
        "darkMode" to "Темна тема", "lightMode" to "Світла тема", "systemDefault" to "Системна",
        "about" to "Про додаток", "version" to "Версія", "totalWorkouts" to "Всього тренувань",
        "totalWeight" to "Загальна вага", "personalRecords" to "Особисті рекорди",
        "recentWorkouts" to "Нещодавні тренування", "viewAll" to "Показати все",
        "loading" to "Завантаження...", "error" to "Помилка", "retry" to "Повторити",
        "success" to "Успішно", "friendRequestAccepted" to "Запит прийнято",
        "friendRequestRejected" to "Запит відхилено", "profileUpdated" to "Профіль оновлено",
        "workoutSaved" to "Тренування збережено", "workoutDeleted" to "Тренування видалено",
        "noExercises" to "Немає вправ", "selectExercises" to "Оберіть вправи",
        "exerciseList" to "Список вправ", "customExercises" to "Власні вправи",
        "defaultExercises" to "Вправи за замовчуванням", "addCustomExercise" to "Додати вправу",
        "enterExerciseName" to "Введіть назву вправи", "selectGroup" to "Оберіть групу",
        "addTemplate" to "Додати шаблон", "templateName" to "Назва шаблону",
        "templateSaved" to "Шаблон збережено", "templateDeleted" to "Шаблон видалено",
        "noTemplates" to "Немає шаблонів", "createFirstTemplate" to "Створіть перший шаблон",
        "selectTemplate" to "Оберіть шаблон", "useTemplate" to "Використати шаблон",
        "deleteTemplate" to "Видалити шаблон", "recoveryInfo" to "Інформація про відновлення",
        "lastWorkout" to "Останнє тренування", "daysSince" to "Днів з",
        "recommendedRecovery" to "Рекомендоване відновлення",
        "muscleGroupRecovery" to "Відновлення м'язових груп",
        "readyToTrain" to "Готові до тренування!", "needsMoreRest" to "Потрібен відпочинок",
        "todayIsRestDay" to "Сьогодні день відпочинку", "progressChart" to "Графік прогресу",
        "volumeOverTime" to "Об'єм у часі", "weightProgression" to "Прогресія ваги",
        "frequencyChart" to "Графік частоти", "noChartData" to "Немає даних для графіка",
        "calendarView" to "Календар", "listView" to "Список", "sortBy" to "Сортувати за",
        "sortByDate" to "За датою", "sortByGroup" to "За групою", "filterByGroup" to "Фільтр за групою",
        "allGroups" to "Всі", "welcomeTitle" to "Ласкаво просимо!",
        "welcomeSubtitle" to "Почніть ваш шлях до фітнесу", "featureSocial" to "Соціальне",
        "featureGamification" to "Ігри", "featureCharts" to "Графики", "featureExport" to "Експорт",
        "featureTemplates" to "Шаблони", "featureMultiLang" to "Багатомовний",
        "notifications" to "Сповіщення", "enableNotifications" to "Увімкнути сповіщення",
        "notificationPermissionRequired" to "Потрібен дозвіл на сповіщення",
        "friendRequestNotificationTitle" to "Запит в друзі",
        "friendRequestNotificationText" to "надіслав(а) вам запит в друзі!",
        "profilePhotoUpdated" to "Фото профілю оновлено", "nameRequired" to "Ім'я обов'язкове",
        "settingsSaved" to "Налаштування збережено", "darkTheme" to "Темна тема",
        "lightTheme" to "Світла тема", "systemTheme" to "Системна тема",
        "selectLanguage" to "Оберіть мову", "english" to "Англійська", "romanian" to "Румунська",
        "russian" to "Російська", "ukrainian" to "Українська", "french" to "Французька",
        "german" to "Німецька", "spanish" to "Іспанська", "italian" to "Італійська",
        "turkish" to "Турецька", "portuguese" to "Португальська", "polish" to "Польська",
        "leaderLabel" to "Лідер", "workoutsLabel" to "Тренування", "totalVolume" to "Загальний об'єм",
        "currentStreakLabel" to "Поточна серія", "bestStreakLabel" to "Найкраща серія",
        "badgesEarned" to "Зароблені значки", "days" to "днів", "badge" to "Значок", "lastPR" to "Останній PR",
        "newExercise" to "Нова вправа", "exerciseNameLabel" to "Назва вправи", "add" to "Додати", "demoExercise" to "ДЕМО ВПРАВА", "setLabel" to "ПІДХІД", "prAndVolume" to "Особисті рекорди та обсяг", "start" to "Старт", "stop" to "Стоп", "noSavedSetsYet" to "Немає збережених підходів.", "editSet" to "Редагувати підхід", "chooseTemplate" to "Оберіть шаблон тренування", "exercises" to "вправ", "recovered" to "Відновлено", "almostRecovered" to "Майже відновлено", "moderate" to "Помірно", "tired" to "Втомлений", "exhausted" to "Виснажений", "fatigue" to "втома", "chooseMuscleGroup" to "Оберіть групу м'язів", "changeExercise" to "Замінити вправу",
        "monthlyProgress" to "Прогрес за місяць", "completeWorkoutsToSee" to "Завершіть тренування щоб побачити прогрес", "jan" to "Січ", "feb" to "Лют", "mar" to "Бер", "apr" to "Кві", "may" to "Тра", "jun" to "Чер", "jul" to "Лип", "aug" to "Сер", "sep" to "Вер", "oct" to "Жов", "nov" to "Лис", "dec" to "Гру", "monthlyDetails" to "Деталі місяця", "month" to "Місяць", "mon" to "Пн", "tue" to "Вт", "wed" to "Ср", "thu" to "Чт", "fri" to "Пт", "sat" to "Сб", "sun" to "Нд", "noWorkouts" to "Немає тренувань в цей день", "workoutDistribution" to "Розподіл тренувань",
        "notNow" to "Не зараз", "subscribeNow" to "Підписатися", "premiumFeature" to "Преміум функція", "subscribersOnly" to "\$feature доступне лише для підписників", "choosePlan" to "Оберіть план", "youAreSubscribed" to "Ви підписані!", "muscleRecovery" to "Відновлення м'язів", "waterReminder" to "Нагадування про воду", "waterReminderTitle" to "Час пити воду!", "waterReminderText" to "Пийте воду! Настав час випити склянку води.", "waterReminderEnabled" to "Увімкнено", "waterReminderDisabled" to "Вимкнено", "selectTime" to "Обрати час", "forearms" to "Передпліччя", "neckAndTraps" to "Шия і Трапеція", "welcome" to "Ласкаво просимо", "athlete" to "Спортсмен",
        "biometricTracking" to "Біометричний моніторинг", "biometricSubtitle" to "Вага, обхвати, жирова прошарок", "addMeasurement" to "Додати вимірювання", "bodyFat" to "Жирова прошарок", "waistCirc" to "Талія", "hipsCirc" to "Стегна", "thighsCirc" to "Бедра", "chestCirc" to "Груди", "armsCirc" to "Руки", "lastMeasurement" to "Останнє вимірювання", "noMeasurements" to "Вимірювань поки немає", "viewCharts" to "Переглянути графіки", "saveMeasurement" to "Зберегти вимірювання", "measurementSaved" to "Вимірювання збережено", "weeksAgo" to "тижнів тому", "cm" to "см", "percent" to "%", "deleteMeasurement" to "Видалити вимірювання", "biometricHistory" to "Історія вимірювань", "weightChart" to "Графік ваги", "bodyFatChart" to "Графік жиру", "circumferenceChart" to "Графік обхватів", "date" to "Дата", "biometricReminder" to "Біометричне нагадування", "biometricReminderTitle" to "Час для вимірювань!", "biometricReminderText" to "Не забудьте записати тижневі вимірювання тіла.", "biometricReminderEnabled" to "Увімкнено", "biometricReminderDisabled" to "Вимкнено",
        "foodJournal" to "Щоденник харчування", "scanBarcode" to "Сканувати штрих-код", "scanBarcodeHint" to "Помістіть штрих-код у кадр для сканування продукту", "cameraPermissionRequired" to "Для сканування потрібен доступ до камери", "scan" to "Сканувати", "scanning" to "Сканування...", "scanBarcodeHelp" to "Переконайтеся, що Google Play Services встановлено та оновлено", "noFoodEntries" to "Записів про їжу поки немає", "todaysMacros" to "Макронутрієнти сьогодні", "caloriesLabel" to "Калорії", "proteinLabel" to "Білки", "carbsLabel" to "Вуглеводи", "fatLabel" to "Жири", "breakfast" to "Сніданок", "lunch" to "Обід", "dinner" to "Вечеря", "snack" to "Перекус", "drinks" to "Напої", "selectMealType" to "Оберіть тип прийому їжі", "manualFoodEntry" to "Ручний ввід", "foodName" to "Назва продукту", "brandLabel" to "Бренд", "calories" to "Калорії", "protein" to "Білки", "carbs" to "Вуглеводи", "fat" to "Жири", "fiber" to "Клітковина",
        "aiTrainer" to "ШІ Тренер", "aiTrainerWelcome" to "Привіт! Я ваш ШІ тренер", "aiTrainerHint" to "Запитайте мене про тренування, харчування або прогрес", "aiTrainerHistory" to "Історія чатів", "noHistoryYet" to "Історія порожня", "current" to "Поточний", "askAiTrainer" to "Запитайте тренера...", "aiSuggestion1" to "Яке тренування ви рекомендуєте сьогодні?", "aiSuggestion2" to "Як збільшити обсяг?", "aiSuggestion3" to "Чи потрібен мені день відпочинку?", "aiSuggestion4" to "Як вийти з плато?",
        "viewProfile" to "Переглянути профіль",
        "accountSettings" to "Налаштування акаунту", "deleteAccount" to "Видалити акаунт",
        "privacyPolicy" to "Політика конфіденційності",
        "termsOfService" to "Умови використання", "back_" to "Назад",
        "restTimer" to "Таймер відпочинку", "startTimer" to "Старт таймер", "customTimer" to "Свій таймер",
        "seconds" to "Секунди", "custom" to "Свій",
        "exerciseHistory" to "Історія вправи", "bestSet" to "Найкращий підхід", "lastSets" to "Останні підходи",
        "favorite" to "Обране", "favorites" to "Обрані", "usageCount" to "Використано",
        "addSet" to "Додати підхід", "exerciseNotes" to "Нотатки", "workoutNotes" to "Нотатки тренування",
        "saveNotes" to "Зберегти", "editWorkout" to "Редагувати",
        "volume" to "Об'єм", "maxWeight" to "Макс вага", "maxReps" to "Макс повтори", "maxSet" to "Макс підхід",
        "today" to "Сьогодні", "thisWeek" to "Цей тиждень", "thisMonth" to "Цей місяць",
        "totalVolumeLabel" to "Загальний об'єм",
        "languageChanged" to "Мову змінено", "themeChanged" to "Тему змінено",
        "guest" to "Гість", "loginWithGoogle" to "Увійти через Google", "loginWithFacebook" to "Увійти через Facebook",
        "close" to "Закрити", "menu" to "Меню", "profile" to "Профіль",
        "appTagline" to "Тренуйся. Прогресуй. Повторюй.", "or" to "або", "dark" to "Темна", "light" to "Світла",
        "system" to "Системна", "languageTitle" to "Мова", "themeTitle" to "Тема",
        "selectTheme" to "Обрати тему", "settingsAndMore" to "Налаштування та більше",
        "muscleGroups" to "М'язові групи", "startHere" to "Почати тут", "back__" to "Назад",
        "englishUS" to "Англійська", "romana" to "Румунська", "russkiy" to "Російська", "ukrainska" to "Українська",
        "francais" to "Французька", "deutsch" to "Німецька", "espanol" to "Іспанська",
        "italiano" to "Італійська", "turkce" to "Турецька", "portugues" to "Португальська", "polski" to "Польська",
        "motto1" to "Кожен повтор має значення.", "motto2" to "Сильніший, ніж учора.",
        "motto3" to "Твоє тіло, твої правила.", "motto4" to "Переверши свої межі.",
        "motto5" to "Послідовність перемагає талант.", "motto6" to "Дисципліна — це свобода.",
        "motto7" to "Без коротких шляхів.", "motto8" to "Зароблено, а не отримано.",
        "goodMorning" to "Доброго ранку", "goodAfternoon" to "Доброго дня", "goodEvening" to "Доброго вечора",
        "daysConsecutive" to "днів поспіль", "todaysWorkout" to "Тренування сьогодні",
        "howDoYouFeel" to "Як ви почуваєтесь?", "tiredLabel" to "Втомлений", "normalLabel" to "Нормально", "energeticLabel" to "Енергійний",
        "plusToday" to "Ще сьогодні", "technicalTip" to "Технічна порада",
        "tomorrowLabel" to "Завтра", "setWorkoutTime" to "Встановити час тренування",
        "daysSinceLastWorkout" to "Днів з останнього тренування", "groupsFullyRecovered" to "Групи повністю відновлені",
        "recoveryOnGroups" to "Відновлення груп",
        "weeklySummary" to "Підсумки тижня", "lastWeekLabel" to "минул. тиждень",
        "goalLabel" to "Порада по цілі", "volumeLabel" to "Об'єм", "topExerciseLabel" to "Топ вправа",
        "nutritionLabel" to "Харчування", "motivationLabel" to "Мотивація",
        "gpsCardioMap" to "Cardio", "startTracking" to "Почати відстеження", "stopTracking" to "Зупинити відстеження",
        "pauseTracking" to "Пауза", "resumeTracking" to "Продовжити",
        "distance" to "Дистанція", "pace" to "Темп", "speed" to "Швидкість", "duration" to "Тривалість",
        "savedRoutes" to "Збережені маршрути", "noSavedRoutes" to "Немає збережених маршрутів",
        "routeName" to "Назва маршруту", "saveRoute" to "Зберегти маршрут", "deleteRoute" to "Видалити маршрут",
        "currentLocation" to "Поточне місцезнаходження", "trackingActive" to "Відстеження активне",
        "locationPermissionRequired" to "Потрібен дозвіл на місцезнаходження",
        "restDaysTitle" to "Дні відпочинку та розвантаження", "restDaysSubtitle" to "Автоматичне планування відновлення, розтяжки, легкої йоги",
        "deloadWeek" to "Тиждень розвантаження", "recoverySchedule" to "Графік відновлення",
        "stretching" to "Розтяжка", "lightYoga" to "Легка йога", "foamRolling" to "Фоамролінг",
        "restDayRecommendation" to "Рекомендація дня відпочинку", "nextRestDay" to "Наступний день відпочинку",
        "muscleNeedsRest" to "М'язам потрібен відпочинок", "recoveryComplete" to "Відновлення завершено",
        "deloadInfo" to "Інформація про розвантаження", "suggestedActivities" to "Рекомендовані активності",
        "activeRecovery" to "Активне відновлення", "lightWalk" to "Легка прогулянка",
        "swimming" to "Плавання", "mobilityWork" to "Вправи на мобільність",
        "noRestDays" to "Немає запланованих днів відпочинку", "selectDay" to "Оберіть день",
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
        "startDeload" to "Почати розвантаження", "stretchingDescription" to "Покращіть гнучкість та мобільність",
        "termsAndConditions" to "Умови використання", "termsPrefix" to "Продовжуючи, ви погоджуєтесь з нашими",
        "timeForDeload" to "Час для розвантаження", "weeks" to "тижнів",
        "weeksSinceLastDeload" to "Тижнів з останнього розвантаження", "weightKg" to "Вага (кг)",
        "yogaDescription" to "Розслабтесь та покращіть мобільність за допомогою йоги", "tapToEdit" to "Натисніть для редагування", "weeklyReminder" to "Щотижневе нагадування"
    ))

    private fun createFr() = Strings(mapOf(
        "appName" to "Kinetic", "dashboard" to "Tableau de bord", "overview" to "Aperçu", "acasa" to "Accueil", "workouts" to "Entraînements", "stats" to "Stats", "waterIntake" to "Consommation d'eau", "waterGoal" to "Objectif eau", "addWater" to "Ajouter de l'eau", "dailyWater" to "Eau quotidienne", "height" to "Taille", "personalInfo" to "Informations personnelles", "waterAutoCalc" to "Calcul auto eau", "ml" to "ml", "templates" to "Modèles",
            "waterHistory" to "Historique d'hydratation", "last7Days" to "7 derniers jours", "everyDay" to "Chaque jour", "reminder" to "Rappel",
        "recovery" to "Récupération", "progress" to "Progrès", "feed" to "Fil", "friends" to "Amis",
        "leaderboard" to "Classement", "all" to "Tous", "settings" to "Paramètres", "language" to "Langue",
        "units" to "Unités", "logout" to "Déconnexion", "login" to "Connexion", "signUp" to "S'inscrire",
        "email" to "Email", "password" to "Mot de passe", "forgotPassword" to "Mot de passe oublié?",
        "orContinueWith" to "Ou continuer avec", "loginAsGuest" to "Se connecter en tant qu'invité",
        "welcomeBack" to "Bon retour!", "createAccount" to "Créer un compte",
        "goalStrength" to "Force", "goalMass" to "Masse musculaire", "goalWeightLoss" to "Perte de poids",
        "goalMaintenance" to "Maintien", "selectGoal" to "Sélectionnez votre objectif",
        "next" to "Suivant", "skip" to "Passer", "finish" to "Terminer", "back" to "Retour",
        "profileSetup" to "Configuration du profil", "enterName" to "Entrez votre nom",
        "pickPhoto" to "Choisir une photo", "saveProfile" to "Sauvegarder le profil",
        "chest" to "Pectoraux", "shoulders" to "Épaules",             "arms" to "Bras", "biceps" to "Biceps", "triceps" to "Triceps",
        "legs" to "Jambes", "thighs" to "Cuisses", "glutes" to "Fessiers", "calves" to "Mollets",
        "core" to "Abdominaux", "cardio" to "Cardio", "sets" to "Séries", "reps" to "Répétitions",
        "weight" to "Poids", "addExercise" to "Ajouter un exercice",
        "saveWorkout" to "Sauvegarder l'entraînement", "startWorkout" to "Commencer l'entraînement", "nextExercise" to "Exercice suivant",
        "notes" to "Notes", "cancel" to "Annuler", "confirm" to "Confirmer", "delete" to "Supprimer",
        "edit" to "Modifier", "search" to "Rechercher", "noDataYet" to "Pas encore de données",
        "friendRequests" to "Demandes d'amis", "sendRequest" to "Envoyer la demande",
        "accept" to "Accepter", "reject" to "Refuser", "removeFriend" to "Supprimer l'ami",
        "noFriends" to "Pas encore d'amis", "searchUsers" to "Rechercher des utilisateurs",
        "userId" to "ID utilisateur", "searchByNameOrId" to "Rechercher par nom ou ID",
        "incomingRequests" to "Demandes entrantes", "noIncomingRequests" to "Aucune demande entrante",
        "yourFriends" to "Vos amis", "sendFriendRequest" to "Envoyer une demande d'amitié",
        "friendRequestSent" to "Demande envoyée", "byId" to "Par ID", "feedEmpty" to "Le fil est vide",
        "postPlaceholder" to "Écrivez quelque chose...", "post" to "Publier", "comments" to "Commentaires",
        "like" to "J'aime", "likes" to "J'aimes", "share" to "Partager",
        "workoutCompleted" to "Entraînement terminé!", "streakLabel" to "Série actuelle",
        "bestStreak" to "Meilleure série", "badges" to "Badges", "noBadges" to "Pas encore de badges",
        "rank" to "Rang", "kg" to "kg", "lbs" to "lbs", "kgLbsToggle" to "Basculer kg/lbs",
        "exportCsv" to "Exporter CSV", "importCsv" to "Importer CSV",
        "subscription" to "Abonnement", "premium" to "Premium", "monthlyPlan" to "Forfait mensuel",
        "yearlyPlan" to "Forfait annuel", "subscribe" to "S'abonner", "subscribed" to "Abonné",
        "notSubscribed" to "Non abonné", "darkMode" to "Mode sombre", "lightMode" to "Mode clair",
        "systemDefault" to "Système", "about" to "À propos", "version" to "Version",
        "totalWorkouts" to "Total des entraînements", "totalWeight" to "Poids total",
        "personalRecords" to "Records personnels", "recentWorkouts" to "Entraînements récents",
        "viewAll" to "Voir tout", "loading" to "Chargement...", "error" to "Erreur",
        "retry" to "Réessayer", "success" to "Succès",
        "friendRequestAccepted" to "Demande d'amitié acceptée",
        "friendRequestRejected" to "Demande d'amitié refusée",
        "profileUpdated" to "Profil mis à jour", "workoutSaved" to "Entraînement sauvegardé",
        "workoutDeleted" to "Entraînement supprimé", "noExercises" to "Pas d'exercices",
        "selectExercises" to "Sélectionner des exercices", "exerciseList" to "Liste des exercices",
        "customExercises" to "Exercices personnalisés",
        "defaultExercises" to "Exercices par défaut",
        "addCustomExercise" to "Ajouter un exercice personnalisé",
        "enterExerciseName" to "Entrez le nom de l'exercice", "selectGroup" to "Sélectionner le groupe",
        "addTemplate" to "Ajouter un modèle", "templateName" to "Nom du modèle",
        "templateSaved" to "Modèle sauvegardé", "templateDeleted" to "Modèle supprimé",
        "noTemplates" to "Pas de modèles", "createFirstTemplate" to "Créez votre premier modèle",
        "selectTemplate" to "Sélectionner le modèle", "useTemplate" to "Utiliser le modèle",
        "deleteTemplate" to "Supprimer le modèle", "recoveryInfo" to "Infos de récupération",
        "lastWorkout" to "Dernier entraînement", "daysSince" to "Jours depuis",
        "recommendedRecovery" to "Récupération recommandée",
        "muscleGroupRecovery" to "Récupération des groupes musculaires",
        "readyToTrain" to "Prêt à s'entraîner!", "needsMoreRest" to "Besoin de plus de repos",
        "todayIsRestDay" to "Aujourd'hui est jour de repos", "progressChart" to "Graphique de progrès",
        "volumeOverTime" to "Volume au fil du temps", "weightProgression" to "Progression du poids",
        "frequencyChart" to "Graphique de fréquence", "noChartData" to "Pas de données pour le graphique",
        "calendarView" to "Vue calendrier", "listView" to "Vue liste", "sortBy" to "Trier par",
        "sortByDate" to "Par date", "sortByGroup" to "Par groupe", "filterByGroup" to "Filtrer par groupe",
        "allGroups" to "Tous", "welcomeTitle" to "Bienvenue!",
        "welcomeSubtitle" to "Commencez votre parcours fitness", "featureSocial" to "Social",
        "featureGamification" to "Gamification", "featureCharts" to "Graphiques",
        "featureExport" to "Export", "featureTemplates" to "Modèles",
        "featureMultiLang" to "Multi-langue", "notifications" to "Notifications",
        "enableNotifications" to "Activer les notifications",
        "notificationPermissionRequired" to "Permission de notification requise",
        "friendRequestNotificationTitle" to "Demande d'amitié",
        "friendRequestNotificationText" to "vous a envoyé une demande d'amitié!",
        "profilePhotoUpdated" to "Photo de profil mise à jour", "nameRequired" to "Le nom est requis",
        "settingsSaved" to "Paramètres sauvegardés", "darkTheme" to "Thème sombre",
        "lightTheme" to "Thème clair", "systemTheme" to "Thème système",
        "selectLanguage" to "Sélectionner la langue", "english" to "Anglais",
        "romanian" to "Roumain", "russian" to "Russe", "ukrainian" to "Ukrainien",
        "french" to "Français", "german" to "Allemand", "spanish" to "Espagnol",
        "italian" to "Italien", "turkish" to "Turc", "portuguese" to "Portugais",
        "polish" to "Polonais", "leaderLabel" to "Leader", "workoutsLabel" to "Entraînements",
        "totalVolume" to "Volume total", "currentStreakLabel" to "Série actuelle",
        "bestStreakLabel" to "Meilleure série", "badgesEarned" to "Badges obtenus",
        "days" to "jours", "badge" to "Badge", "lastPR" to "Dernier PR",
        "newExercise" to "Nouvel exercice", "exerciseNameLabel" to "Nom de l'exercice", "add" to "Ajouter", "demoExercise" to "EXERCICE DEMO", "setLabel" to "SÉRIE", "prAndVolume" to "Records et volume", "start" to "Démarrer", "stop" to "Arrêter", "noSavedSetsYet" to "Aucune série sauvegardée.", "editSet" to "Modifier la série", "chooseTemplate" to "Choisir un modèle d'entraînement", "exercises" to "exercices", "recovered" to "Récupéré", "almostRecovered" to "Presque récupéré", "moderate" to "Modéré", "tired" to "Fatigué", "exhausted" to "Épuisé", "fatigue" to "fatigue", "chooseMuscleGroup" to "Choisir le groupe musculaire", "changeExercise" to "Changer d'exercice",
        "monthlyProgress" to "Progrès mensuel", "completeWorkoutsToSee" to "Complétez des entraînements pour voir les progrès", "jan" to "Janv", "feb" to "Févr", "mar" to "Mars", "apr" to "Avr", "may" to "Mai", "jun" to "Juin", "jul" to "Juil", "aug" to "Août", "sep" to "Sept", "oct" to "Oct", "nov" to "Nov", "dec" to "Déc", "monthlyDetails" to "Détails mensuels", "month" to "Mois", "mon" to "Lu", "tue" to "Ma", "wed" to "Me", "thu" to "Je", "fri" to "Ve", "sat" to "Sa", "sun" to "Di", "noWorkouts" to "Aucun entraînement ce jour", "workoutDistribution" to "Distribution des entraînements",
        "notNow" to "Pas maintenant", "subscribeNow" to "S'abonner", "premiumFeature" to "Fonctionnalité Premium", "subscribersOnly" to "\$feature est disponible uniquement pour les abonnés", "choosePlan" to "Choisir un forfait", "youAreSubscribed" to "Vous êtes abonné!", "muscleRecovery" to "Récupération musculaire", "waterReminder" to "Rappel d'hydratation", "waterReminderTitle" to "Il est temps de boire de l'eau!", "waterReminderText" to "Restez hydraté! Il est temps de boire un verre d'eau.", "waterReminderEnabled" to "Activé", "waterReminderDisabled" to "Désactivé", "selectTime" to "Choisir l'heure", "forearms" to "Avant-bras", "neckAndTraps" to "Cou & Trapèzes", "welcome" to "Bienvenue", "athlete" to "Athlète",
        "biometricTracking" to "Suivi biométrique", "biometricSubtitle" to "Poids, circonférences, graisse corporelle", "addMeasurement" to "Ajouter une mesure", "bodyFat" to "Graisse corporelle", "waistCirc" to "Taille", "hipsCirc" to "Hanches", "thighsCirc" to "Cuisses", "chestCirc" to "Poitrine", "armsCirc" to "Bras", "lastMeasurement" to "Dernière mesure", "noMeasurements" to "Aucune mesure encore", "viewCharts" to "Voir les graphiques", "saveMeasurement" to "Enregistrer la mesure", "measurementSaved" to "Mesure enregistrée", "weeksAgo" to "semaines", "cm" to "cm", "percent" to "%", "deleteMeasurement" to "Supprimer la mesure", "biometricHistory" to "Historique des mesures", "weightChart" to "Graphique du poids", "bodyFatChart" to "Graphique de la graisse", "circumferenceChart" to "Graphique des circonférences", "date" to "Date", "biometricReminder" to "Rappel biométrique", "biometricReminderTitle" to "C'est l'heure des mesures!", "biometricReminderText" to "N'oubliez pas d'enregistrer vos mesures corporelles hebdomadaires.", "biometricReminderEnabled" to "Activé", "biometricReminderDisabled" to "Désactivé",
        "foodJournal" to "Journal alimentaire", "scanBarcode" to "Scanner le code-barres", "scanBarcodeHint" to "Placez le code-barres dans le cadre pour scanner le produit", "cameraPermissionRequired" to "L'accès à la caméra est nécessaire pour scanner", "scan" to "Scanner", "scanning" to "Scan en cours...", "scanBarcodeHelp" to "Assurez-vous que Google Play Services est installé et mis à jour", "noFoodEntries" to "Aucune entrée alimentaire", "todaysMacros" to "Macronutriments du jour", "caloriesLabel" to "Calories", "proteinLabel" to "Protéines", "carbsLabel" to "Glucides", "fatLabel" to "Lipides", "breakfast" to "Petit-déjeuner", "lunch" to "Déjeuner", "dinner" to "Dîner", "snack" to "Collation", "drinks" to "Boissons", "selectMealType" to "Sélectionner le type de repas", "manualFoodEntry" to "Saisie manuelle", "foodName" to "Nom de l'aliment", "brandLabel" to "Marque", "calories" to "Calories", "protein" to "Protéines", "carbs" to "Glucides", "fat" to "Lipides", "fiber" to "Fibres",
        "aiTrainer" to "Coach IA", "aiTrainerWelcome" to "Salut! Je suis votre coach IA", "aiTrainerHint" to "Demandez-moi tout sur l'entraînement, la nutrition ou les progrès", "aiTrainerHistory" to "Historique des chats", "noHistoryYet" to "Pas encore d'historique", "current" to "Actuel", "askAiTrainer" to "Demander au coach...", "aiSuggestion1" to "Quel entraînement recommandez-vous?", "aiSuggestion2" to "Comment augmenter le volume?", "aiSuggestion3" to "Ai-je besoin d'un jour de repos?", "aiSuggestion4" to "Comment sortir d'un plateau?",
        "viewProfile" to "Voir le profil", "accountSettings" to "Paramètres du compte",
        "deleteAccount" to "Supprimer le compte", "privacyPolicy" to "Politique de confidentialité",
        "termsOfService" to "Conditions d'utilisation", "back_" to "Retour",
        "restTimer" to "Timer de repos", "startTimer" to "Démarrer", "customTimer" to "Timer custom",
        "seconds" to "Secondes", "custom" to "Custom",
        "exerciseHistory" to "Historique", "bestSet" to "Meilleur set", "lastSets" to "Derniers sets",
        "favorite" to "Favori", "favorites" to "Favoris", "usageCount" to "Utilisé",
        "addSet" to "Ajouter set", "exerciseNotes" to "Notes exercice", "workoutNotes" to "Notes séance",
        "saveNotes" to "Enregistrer", "editWorkout" to "Modifier",
        "volume" to "Volume", "maxWeight" to "Poids max", "maxReps" to "Reps max", "maxSet" to "Série max",
        "today" to "Aujourd'hui", "thisWeek" to "Cette semaine", "thisMonth" to "Ce mois",
        "totalVolumeLabel" to "Volume total",
        "languageChanged" to "Langue modifiée", "themeChanged" to "Thème modifié",
        "guest" to "Invité", "loginWithGoogle" to "Se connecter avec Google", "loginWithFacebook" to "Se connecter avec Facebook",
        "close" to "Fermer", "menu" to "Menu", "profile" to "Profil",
        "appTagline" to "Entraîne-toi. Progresse. Répète.", "or" to "ou", "dark" to "Sombre", "light" to "Clair",
        "system" to "Système", "languageTitle" to "Langue", "themeTitle" to "Thème",
        "selectTheme" to "Choisir le thème", "settingsAndMore" to "Paramètres et plus",
        "muscleGroups" to "Groupes musculaires", "startHere" to "Commencer ici", "back__" to "Retour",
        "englishUS" to "Anglais", "romana" to "Roumain", "russkiy" to "Russe", "ukrainska" to "Ukrainien",
        "francais" to "Français", "deutsch" to "Allemand", "espanol" to "Espagnol",
        "italiano" to "Italien", "turkce" to "Turc", "portugues" to "Portugais", "polski" to "Polonais",
        "motto1" to "Chaque répétition compte.", "motto2" to "Plus fort qu'hier.",
        "motto3" to "Ton corps, tes règles.", "motto4" to "Repousse tes limites.",
        "motto5" to "La constance bat le talent.", "motto6" to "La discipline, c'est la liberté.",
        "motto7" to "Pas de raccourcis.", "motto8" to "Gagné, pas donné.",
        "goodMorning" to "Bonjour", "goodAfternoon" to "Bon après-midi", "goodEvening" to "Bonsoir",
        "daysConsecutive" to "jours consécutifs", "todaysWorkout" to "Entraînement du jour",
        "howDoYouFeel" to "Comment vous sentez-vous?", "tiredLabel" to "Fatigué", "normalLabel" to "Normal", "energeticLabel" to "Énergique",
        "plusToday" to "Encore aujourd'hui", "technicalTip" to "Conseil technique",
        "tomorrowLabel" to "Demain", "setWorkoutTime" to "Régler l'heure d'entraînement",
        "daysSinceLastWorkout" to "Jours depuis le dernier entraînement", "groupsFullyRecovered" to "Groupes complètement récupérés",
        "recoveryOnGroups" to "Récupération des groupes",
        "weeklySummary" to "Résumé de la semaine", "lastWeekLabel" to "semaine dernière",
        "goalLabel" to "Conseil objectif", "volumeLabel" to "Volume", "topExerciseLabel" to "Top exercice",
        "nutritionLabel" to "Nutrition", "motivationLabel" to "Motivation",
        "gpsCardioMap" to "Cardio", "startTracking" to "Commencer le suivi", "stopTracking" to "Arrêter le suivi",
        "pauseTracking" to "Pause", "resumeTracking" to "Reprendre",
        "distance" to "Distance", "pace" to "Rythme", "speed" to "Vitesse", "duration" to "Durée",
        "savedRoutes" to "Itinéraires sauvegardés", "noSavedRoutes" to "Aucun itinéraire sauvegardé",
        "routeName" to "Nom de l'itinéraire", "saveRoute" to "Sauvegarder l'itinéraire", "deleteRoute" to "Supprimer l'itinéraire",
        "currentLocation" to "Position actuelle", "trackingActive" to "Suivi actif",
        "locationPermissionRequired" to "La permission de localisation est requise",
        "restDaysTitle" to "Jours de repos et décharge", "restDaysSubtitle" to "Planification automatique récupération, étirements, yoga léger",
        "deloadWeek" to "Semaine de décharge", "recoverySchedule" to "Planning de récupération",
        "stretching" to "Étirements", "lightYoga" to "Yoga léger", "foamRolling" to "Rouleau de mousse",
        "restDayRecommendation" to "Recommandation jour de repos", "nextRestDay" to "Prochain jour de repos",
        "muscleNeedsRest" to "Les muscles ont besoin de repos", "recoveryComplete" to "Récupération terminée",
        "deloadInfo" to "Infos décharge", "suggestedActivities" to "Activités suggérées",
        "activeRecovery" to "Récupération active", "lightWalk" to "Promenade légère",
        "swimming" to "Natation", "mobilityWork" to "Travail de mobilité",
        "noRestDays" to "Aucun jour de repos planifié", "selectDay" to "Sélectionner le jour",
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
        "startDeload" to "Commencer la décharge", "stretchingDescription" to "Améliorez la souplesse et la mobilité",
        "termsAndConditions" to "Conditions d'utilisation", "termsPrefix" to "En continuant, vous acceptez nos",
        "timeForDeload" to "C'est l'heure de la décharge", "weeks" to "semaines",
        "weeksSinceLastDeload" to "Semaines depuis la dernière décharge", "weightKg" to "Poids (kg)",
        "yogaDescription" to "Détendez-vous et améliorez la mobilité avec le yoga léger", "tapToEdit" to "Appuyez pour modifier", "weeklyReminder" to "Rappel hebdomadaire"
    ))

    private fun createDe() = Strings(mapOf(
        "appName" to "Kinetic", "dashboard" to "Dashboard", "overview" to "Übersicht", "acasa" to "Startseite", "workouts" to "Trainings", "stats" to "Statistiken", "waterIntake" to "Wasseraufnahme", "waterGoal" to "Wasserziel", "addWater" to "Wasser hinzufügen", "dailyWater" to "Tägliches Wasser", "height" to "Größe", "personalInfo" to "Persönliche Infos", "waterAutoCalc" to "Auto Wasserberechnung", "ml" to "ml", "templates" to "Vorlagen",
            "waterHistory" to "Hydratationsverlauf", "last7Days" to "Letzte 7 Tage", "everyDay" to "Jeden Tag", "reminder" to "Erinnerung",
        "recovery" to "Erholung", "progress" to "Fortschritt", "feed" to "Feed", "friends" to "Freunde",
        "leaderboard" to "Bestenliste", "all" to "Alle", "settings" to "Einstellungen", "language" to "Sprache",
        "units" to "Einheiten", "logout" to "Abmelden", "login" to "Anmelden", "signUp" to "Registrieren",
        "email" to "E-Mail", "password" to "Passwort", "forgotPassword" to "Passwort vergessen?",
        "orContinueWith" to "Oder weiter mit", "loginAsGuest" to "Als Gast anmelden",
        "welcomeBack" to "Willkommen zurück!", "createAccount" to "Konto erstellen",
        "goalStrength" to "Kraft", "goalMass" to "Muskelmasse", "goalWeightLoss" to "Gewichtsverlust",
        "goalMaintenance" to "Erhaltung", "selectGoal" to "Ziel auswählen", "next" to "Weiter",
        "skip" to "Überspringen", "finish" to "Fertig", "back" to "Zurück",
        "profileSetup" to "Profil einrichten", "enterName" to "Name eingeben",
        "pickPhoto" to "Foto auswählen", "saveProfile" to "Profil speichern", "chest" to "Brust",
        "shoulders" to "Schultern",             "arms" to "Arme", "biceps" to "Bizeps", "triceps" to "Trizeps",
        "legs" to "Beine", "thighs" to "Oberschenkel", "glutes" to "Gesäß", "calves" to "Waden",
        "core" to "Rumpf",
        "cardio" to "Cardio", "sets" to "Sätze", "reps" to "Wiederholungen", "weight" to "Gewicht",
        "addExercise" to "Übung hinzufügen", "saveWorkout" to "Training speichern",
        "startWorkout" to "Training starten", "nextExercise" to "Nächste Übung", "notes" to "Notizen", "cancel" to "Abbrechen",
        "confirm" to "Bestätigen", "delete" to "Löschen", "edit" to "Bearbeiten", "search" to "Suchen",
        "noDataYet" to "Noch keine Daten", "friendRequests" to "Freundschaftsanfragen",
        "sendRequest" to "Anfrage senden", "accept" to "Akzeptieren", "reject" to "Ablehnen",
        "removeFriend" to "Freund entfernen", "noFriends" to "Noch keine Freunde",
        "searchUsers" to "Benutzer suchen", "userId" to "Benutzer-ID",
        "searchByNameOrId" to "Nach Name oder ID suchen", "incomingRequests" to "Eingehende Anfragen",
        "noIncomingRequests" to "Keine eingehenden Anfragen", "yourFriends" to "Deine Freunde",
        "sendFriendRequest" to "Freundschaftsanfrage senden", "friendRequestSent" to "Anfrage gesendet",
        "byId" to "Nach ID", "feedEmpty" to "Feed ist leer", "postPlaceholder" to "Schreib etwas...",
        "post" to "Posten", "comments" to "Kommentare", "like" to "Gefällt mir",
        "likes" to "Gefällt mir", "share" to "Teilen", "workoutCompleted" to "Training abgeschlossen!",
        "streakLabel" to "Aktuelle Serie", "bestStreak" to "Beste Serie", "badges" to "Abzeichen",
        "noBadges" to "Noch keine Abzeichen", "rank" to "Rang", "kg" to "kg", "lbs" to "lbs",
        "kgLbsToggle" to "kg/lbs umschalten", "exportCsv" to "CSV exportieren",
        "importCsv" to "CSV importieren", "subscription" to "Abonnement", "premium" to "Premium",
        "monthlyPlan" to "Monatsplan", "yearlyPlan" to "Jahresplan",
        "subscribe" to "Abonnieren", "subscribed" to "Abonniert", "notSubscribed" to "Nicht abonniert",
        "darkMode" to "Dunkler Modus", "lightMode" to "Heller Modus", "systemDefault" to "System",
        "about" to "Über", "version" to "Version", "totalWorkouts" to "Gesamte Trainings",
        "totalWeight" to "Gesamtgewicht", "personalRecords" to "Persönliche Rekorde",
        "recentWorkouts" to "Letzte Trainings", "viewAll" to "Alle anzeigen",
        "loading" to "Laden...", "error" to "Fehler", "retry" to "Erneut versuchen",
        "success" to "Erfolg", "friendRequestAccepted" to "Freundschaftsanfrage angenommen",
        "friendRequestRejected" to "Freundschaftsanfrage abgelehnt",
        "profileUpdated" to "Profil aktualisiert", "workoutSaved" to "Training gespeichert",
        "workoutDeleted" to "Training gelöscht", "noExercises" to "Keine Übungen",
        "selectExercises" to "Übungen auswählen", "exerciseList" to "Übungsliste",
        "customExercises" to "Eigene Übungen", "defaultExercises" to "Standardübungen",
        "addCustomExercise" to "Eigene Übung hinzufügen",
        "enterExerciseName" to "Übungsname eingeben", "selectGroup" to "Gruppe auswählen",
        "addTemplate" to "Vorlage hinzufügen", "templateName" to "Vorlagenname",
        "templateSaved" to "Vorlage gespeichert", "templateDeleted" to "Vorlage gelöscht",
        "noTemplates" to "Keine Vorlagen", "createFirstTemplate" to "Erstelle deine erste Vorlage",
        "selectTemplate" to "Vorlage auswählen", "useTemplate" to "Vorlage verwenden",
        "deleteTemplate" to "Vorlage löschen", "recoveryInfo" to "Erholungsinformationen",
        "lastWorkout" to "Letztes Training", "daysSince" to "Tage seit",
        "recommendedRecovery" to "Empfohlene Erholung",
        "muscleGroupRecovery" to "Erholung der Muskelgruppen", "readyToTrain" to "Bereit zum Trainieren!",
        "needsMoreRest" to "Braucht mehr Ruhe", "todayIsRestDay" to "Heute ist Ruhetag",
        "progressChart" to "Fortschrittsdiagramm", "volumeOverTime" to "Volumen im Zeitverlauf",
        "weightProgression" to "Gewichtsentwicklung", "frequencyChart" to "Häufigkeitsdiagramm",
        "noChartData" to "Keine Diagrammdaten", "calendarView" to "Kalenderansicht",
        "listView" to "Listenansicht", "sortBy" to "Sortieren nach", "sortByDate" to "Nach Datum",
        "sortByGroup" to "Nach Gruppe", "filterByGroup" to "Nach Gruppe filtern",
        "allGroups" to "Alle", "welcomeTitle" to "Willkommen!",
        "welcomeSubtitle" to "Beginne deine Fitnessreise", "featureSocial" to "Sozial",
        "featureGamification" to "Spielification", "featureCharts" to "Diagramme",
        "featureExport" to "Export", "featureTemplates" to "Vorlagen",
        "featureMultiLang" to "Mehrsprachig", "notifications" to "Benachrichtigungen",
        "enableNotifications" to "Benachrichtigungen aktivieren",
        "notificationPermissionRequired" to "Benachrichtigungsberechtigung erforderlich",
        "friendRequestNotificationTitle" to "Freundschaftsanfrage",
        "friendRequestNotificationText" to "hat dir eine Freundschaftsanfrage geschickt!",
        "profilePhotoUpdated" to "Profilfoto aktualisiert", "nameRequired" to "Name ist erforderlich",
        "settingsSaved" to "Einstellungen gespeichert", "darkTheme" to "Dunkles Thema",
        "lightTheme" to "Helles Thema", "systemTheme" to "Systemthema",
        "selectLanguage" to "Sprache auswählen", "english" to "Englisch", "romanian" to "Rumänisch",
        "russian" to "Russisch", "ukrainian" to "Ukrainisch", "french" to "Französisch",
        "german" to "Deutsch", "spanish" to "Spanisch", "italian" to "Italienisch",
        "turkish" to "Türkisch", "portuguese" to "Portugiesisch", "polish" to "Polnisch",
        "leaderLabel" to "Anführer", "workoutsLabel" to "Trainings", "totalVolume" to "Gesamtvolumen",
        "currentStreakLabel" to "Aktuelle Serie", "bestStreakLabel" to "Beste Serie",
        "badgesEarned" to "Verdiente Abzeichen", "days" to "Tage", "badge" to "Abzeichen", "lastPR" to "Letzter PR",
        "newExercise" to "Neue Übung", "exerciseNameLabel" to "Übungsname", "add" to "Hinzufügen", "demoExercise" to "DEMO ÜBUNG", "setLabel" to "SATZ", "prAndVolume" to "Bestleistungen und Volumen", "start" to "Starten", "stop" to "Stopp", "noSavedSetsYet" to "Noch keine Sätze gespeichert.", "editSet" to "Satz bearbeiten", "chooseTemplate" to "Trainingsvorlage wählen", "exercises" to "Übungen", "recovered" to "Erholt", "almostRecovered" to "Fast erholt", "moderate" to "Mäßig", "tired" to "Müde", "exhausted" to "Erschöpft", "fatigue" to "Müdigkeit", "chooseMuscleGroup" to "Muskelgruppe wählen", "changeExercise" to "Übung wechseln",
        "monthlyProgress" to "Monatlicher Fortschritt", "completeWorkoutsToSee" to "Schließen Sie Trainings ab um Fortschritt zu sehen", "jan" to "Jan", "feb" to "Feb", "mar" to "Mär", "apr" to "Apr", "may" to "Mai", "jun" to "Jun", "jul" to "Jul", "aug" to "Aug", "sep" to "Sep", "oct" to "Okt", "nov" to "Nov", "dec" to "Dez", "monthlyDetails" to "Monatliche Details", "month" to "Monat", "mon" to "Mo", "tue" to "Di", "wed" to "Mi", "thu" to "Do", "fri" to "Fr", "sat" to "Sa", "sun" to "So", "noWorkouts" to "Kein Training an diesem Tag", "workoutDistribution" to "Trainingsverteilung",
        "notNow" to "Nicht jetzt", "subscribeNow" to "Jetzt abonnieren", "premiumFeature" to "Premium-Funktion", "subscribersOnly" to "\$feature ist nur für Abonnenten verfügbar", "choosePlan" to "Plan wählen", "youAreSubscribed" to "Sie sind abonniert!", "muscleRecovery" to "Muskelerholung", "waterReminder" to "Wasser-Erinnerung", "waterReminderTitle" to "Zeit, Wasser zu trinken!", "waterReminderText" to "Bleiben Sie hydriert! Es ist Zeit, ein Glas Wasser zu trinken.", "waterReminderEnabled" to "Aktiviert", "waterReminderDisabled" to "Deaktiviert", "selectTime" to "Uhrzeit wählen", "forearms" to "Unterarme", "neckAndTraps" to "Hals & Trapezmuskel", "welcome" to "Willkommen", "athlete" to "Athlet",
        "biometricTracking" to "Biometrisches Tracking", "biometricSubtitle" to "Gewicht, Umfänge, Körperfett", "addMeasurement" to "Messung hinzufügen", "bodyFat" to "Körperfett", "waistCirc" to "Taille", "hipsCirc" to "Hüfte", "thighsCirc" to "Oberschenkel", "chestCirc" to "Brust", "armsCirc" to "Arme", "lastMeasurement" to "Letzte Messung", "noMeasurements" to "Noch keine Messungen", "viewCharts" to "Diagramme anzeigen", "saveMeasurement" to "Messung speichern", "measurementSaved" to "Messung gespeichert", "weeksAgo" to "Wochen her", "cm" to "cm", "percent" to "%", "deleteMeasurement" to "Messung löschen", "biometricHistory" to "Messungsverlauf", "weightChart" to "Gewichtsdiagramm", "bodyFatChart" to "Körperfettdiagramm", "circumferenceChart" to "Umfangsdiagramm", "date" to "Datum", "biometricReminder" to "Biometrische Erinnerung", "biometricReminderTitle" to "Zeit für Messungen!", "biometricReminderText" to "Vergessen Sie nicht, Ihre wöchentlichen Körpermessungen zu protokollieren.", "biometricReminderEnabled" to "Aktiviert", "biometricReminderDisabled" to "Deaktiviert",
        "foodJournal" to "Ernährungstagebuch", "scanBarcode" to "Barcode scannen", "scanBarcodeHint" to "Platzieren Sie den Barcode im Rahmen, um das Produkt zu scannen", "cameraPermissionRequired" to "Kamerazugang ist zum Scannen erforderlich", "scan" to "Scannen", "scanning" to "Scannen...", "scanBarcodeHelp" to "Stellen Sie sicher, dass Google Play Services installiert und aktuell ist", "noFoodEntries" to "Noch keine Einträge", "todaysMacros" to "Heutige Makros", "caloriesLabel" to "Kalorien", "proteinLabel" to "Eiweiß", "carbsLabel" to "Kohlenhydrate", "fatLabel" to "Fett", "breakfast" to "Frühstück", "lunch" to "Mittagessen", "dinner" to "Abendessen", "snack" to "Snack", "drinks" to "Getränke", "selectMealType" to "Mahlzeit auswählen", "manualFoodEntry" to "Manuelle Eingabe", "foodName" to "Produktname", "brandLabel" to "Marke", "calories" to "Kalorien", "protein" to "Eiweiß", "carbs" to "Kohlenhydrate", "fat" to "Fett", "fiber" to "Ballaststoffe",
        "aiTrainer" to "KI Trainer", "aiTrainerWelcome" to "Hallo! Ich bin Ihr KI-Trainer", "aiTrainerHint" to "Fragen Sie mich zu Training, Ernährung oder Fortschritt", "aiTrainerHistory" to "Chat-Verlauf", "noHistoryYet" to "Noch kein Verlauf", "current" to "Aktuell", "askAiTrainer" to "Trainer fragen...", "aiSuggestion1" to "Welches Training empfehlen Sie heute?", "aiSuggestion2" to "Wie kann ich das Volumen steigern?", "aiSuggestion3" to "Brauche ich einen Ruhetag?", "aiSuggestion4" to "Wie komme ich aus dem Plateau?",
        "viewProfile" to "Profil anzeigen",
        "accountSettings" to "Kontoeinstellungen", "deleteAccount" to "Konto löschen",
        "privacyPolicy" to "Datenschutzrichtlinie",
        "termsOfService" to "Nutzungsbedingungen", "back_" to "Zurück",
        "restTimer" to "Pause Timer", "startTimer" to "Timer starten", "customTimer" to "Eigener Timer",
        "seconds" to "Sekunden", "custom" to "Eigener",
        "exerciseHistory" to "Übung Verlauf", "bestSet" to "Bester Satz", "lastSets" to "Letzte Sätze",
        "favorite" to "Favorit", "favorites" to "Favoriten", "usageCount" to "Benutzt",
        "addSet" to "Satz hinzufügen", "exerciseNotes" to "Übung Notizen", "workoutNotes" to "Training Notizen",
        "saveNotes" to "Speichern", "editWorkout" to "Bearbeiten",
        "volume" to "Volumen", "maxWeight" to "Max Gewicht", "maxReps" to "Max Wdh", "maxSet" to "Max Satz",
        "today" to "Heute", "thisWeek" to "Diese Woche", "thisMonth" to "Diesen Monat",
        "totalVolumeLabel" to "Gesamtvolumen",
        "languageChanged" to "Sprache geändert", "themeChanged" to "Thema geändert",
        "guest" to "Gast", "loginWithGoogle" to "Mit Google anmelden", "loginWithFacebook" to "Mit Facebook anmelden",
        "close" to "Schließen", "menu" to "Menü", "profile" to "Profil",
        "appTagline" to "Trainiere. Fortschritte. Wiederhole.", "or" to "oder", "dark" to "Dunkel", "light" to "Hell",
        "system" to "System", "languageTitle" to "Sprache", "themeTitle" to "Thema",
        "selectTheme" to "Thema auswählen", "settingsAndMore" to "Einstellungen & mehr",
        "muscleGroups" to "Muskelgruppen", "startHere" to "Hier starten", "back__" to "Zurück",
        "englishUS" to "Englisch", "romana" to "Rumänisch", "russkiy" to "Russisch", "ukrainska" to "Ukrainisch",
        "francais" to "Französisch", "deutsch" to "Deutsch", "espanol" to "Spanisch",
        "italiano" to "Italienisch", "turkce" to "Türkisch", "portugues" to "Portugiesisch", "polski" to "Polnisch",
        "motto1" to "Jede Wiederholung zählt.", "motto2" to "Stärker als gestern.",
        "motto3" to "Dein Körper, deine Regeln.", "motto4" to "Überwinde deine Grenzen.",
        "motto5" to "Ausdauer schlägt Talent.", "motto6" to "Disziplin ist Freiheit.",
        "motto7" to "Keine Abkürzungen.", "motto8" to "Verdient, nicht bekommen.",
        "goodMorning" to "Guten Morgen", "goodAfternoon" to "Guten Tag", "goodEvening" to "Guten Abend",
        "daysConsecutive" to "Tage in Folge", "todaysWorkout" to "Training heute",
        "howDoYouFeel" to "Wie fühlst du dich?", "tiredLabel" to "Müde", "normalLabel" to "Normal", "energeticLabel" to "Energisch",
        "plusToday" to "Noch heute", "technicalTip" to "Technischer Tipp",
        "tomorrowLabel" to "Morgen", "setWorkoutTime" to "Trainingszeit einstellen",
        "daysSinceLastWorkout" to "Tage seit dem letzten Training", "groupsFullyRecovered" to "Gruppen vollständig erholt",
        "recoveryOnGroups" to "Erholung der Gruppen",
        "weeklySummary" to "Wochenübersicht", "lastWeekLabel" to "letzte Woche",
        "goalLabel" to "Zieltipp", "volumeLabel" to "Volumen", "topExerciseLabel" to "Top Übung",
        "nutritionLabel" to "Ernährung", "motivationLabel" to "Motivation",
        "gpsCardioMap" to "Cardio", "startTracking" to "Tracking starten", "stopTracking" to "Tracking stoppen",
        "pauseTracking" to "Pause", "resumeTracking" to "Fortsetzen",
        "distance" to "Distanz", "pace" to "Tempo", "speed" to "Geschwindigkeit", "duration" to "Dauer",
        "savedRoutes" to "Gespeicherte Routen", "noSavedRoutes" to "Keine gespeicherten Routen",
        "routeName" to "Routenname", "saveRoute" to "Route speichern", "deleteRoute" to "Route löschen",
        "currentLocation" to "Aktueller Standort", "trackingActive" to "Tracking aktiv",
        "locationPermissionRequired" to "Standortberechtigung erforderlich",
        "restDaysTitle" to "Ruhetage & Entlastung", "restDaysSubtitle" to "Automatische Planung Erholung, Dehnung, leichtes Yoga",
        "deloadWeek" to "Entlastungswoche", "recoverySchedule" to "Erholungsplan",
        "stretching" to "Dehnung", "lightYoga" to "Leichtes Yoga", "foamRolling" to "Faszienrolle",
        "restDayRecommendation" to "Ruhetag-Empfehlung", "nextRestDay" to "Nächster Ruhetag",
        "muscleNeedsRest" to "Muskeln brauchen Ruhe", "recoveryComplete" to "Erholung abgeschlossen",
        "deloadInfo" to "Entlastungsinfo", "suggestedActivities" to "Vorgeschlagene Aktivitäten",
        "activeRecovery" to "Aktive Erholung", "lightWalk" to "Lechter Spaziergang",
        "swimming" to "Schwimmen", "mobilityWork" to "Mobilitätstraining",
        "noRestDays" to "Keine Ruhetage geplant", "selectDay" to "Tag auswählen",
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
        "startDeload" to "Entlastung starten", "stretchingDescription" to "Verbessern Sie Flexibilität und Mobilität",
        "termsAndConditions" to "Nutzungsbedingungen", "termsPrefix" to "Durch Fortfahren stimmen Sie unseren",
        "timeForDeload" to "Zeit für Entlastung", "weeks" to "Wochen",
        "weeksSinceLastDeload" to "Wochen seit letzter Entlastung", "weightKg" to "Gewicht (kg)",
        "yogaDescription" to "Entspannen Sie sich und verbessern Sie die Mobilität mit leichtem Yoga", "tapToEdit" to "Tippen zum Bearbeiten", "weeklyReminder" to "Wöchentliche Erinnerung"
    ))

    private fun createEs() = Strings(mapOf(
        "appName" to "Kinetic", "dashboard" to "Panel", "overview" to "Resumen", "acasa" to "Inicio", "workouts" to "Entrenamientos", "stats" to "Estadísticas", "waterIntake" to "Consumo de agua", "waterGoal" to "Meta de agua", "addWater" to "Agregar agua", "dailyWater" to "Agua diaria", "height" to "Altura", "personalInfo" to "Información personal", "waterAutoCalc" to "Cálculo auto de agua", "ml" to "ml", "templates" to "Plantillas",
            "waterHistory" to "Historial de hidratación", "last7Days" to "Últimos 7 días", "everyDay" to "Cada día", "reminder" to "Recordatorio",
        "recovery" to "Recuperación", "progress" to "Progreso", "feed" to "Feed", "friends" to "Amigos",
        "leaderboard" to "Clasificación", "all" to "Todos", "settings" to "Configuración", "language" to "Idioma",
        "units" to "Unidades", "logout" to "Cerrar sesión", "login" to "Iniciar sesión",
        "signUp" to "Registrarse", "email" to "Correo electrónico", "password" to "Contraseña",
        "forgotPassword" to "¿Olvidaste la contraseña?", "orContinueWith" to "O continuar con",
        "loginAsGuest" to "Iniciar como invitado", "welcomeBack" to "¡Bienvenido de nuevo!",
        "createAccount" to "Crear cuenta", "goalStrength" to "Fuerza", "goalMass" to "Masa muscular",
        "goalWeightLoss" to "Pérdida de peso", "goalMaintenance" to "Mantenimiento",
        "selectGoal" to "Selecciona tu objetivo", "next" to "Siguiente", "skip" to "Omitir",
        "finish" to "Finalizar", "back" to "Volver", "profileSetup" to "Configurar perfil",
        "enterName" to "Ingresa tu nombre", "pickPhoto" to "Elegir foto",
        "saveProfile" to "Guardar perfil", "chest" to "Pecho", "shoulders" to "Hombros",
        "arms" to "Brazos", "biceps" to "Bíceps", "triceps" to "Tríceps",
        "legs" to "Piernas", "thighs" to "Muslos", "glutes" to "Glúteos", "calves" to "Pantorrillas",
        "core" to "Core", "cardio" to "Cardio",
        "sets" to "Series", "reps" to "Repeticiones", "weight" to "Peso",
        "addExercise" to "Agregar ejercicio", "saveWorkout" to "Guardar entrenamiento",
        "startWorkout" to "Iniciar entrenamiento", "nextExercise" to "Siguiente ejercicio", "notes" to "Notas", "cancel" to "Cancelar",
        "confirm" to "Confirmar", "delete" to "Eliminar", "edit" to "Editar", "search" to "Buscar",
        "noDataYet" to "Aún no hay datos", "friendRequests" to "Solicitudes de amistad",
        "sendRequest" to "Enviar solicitud", "accept" to "Aceptar", "reject" to "Rechazar",
        "removeFriend" to "Eliminar amigo", "noFriends" to "Aún no hay amigos",
        "searchUsers" to "Buscar usuarios", "userId" to "ID de usuario",
        "searchByNameOrId" to "Buscar por nombre o ID", "incomingRequests" to "Solicitudes entrantes",
        "noIncomingRequests" to "No hay solicitudes entrantes", "yourFriends" to "Tus amigos",
        "sendFriendRequest" to "Enviar solicitud de amistad", "friendRequestSent" to "Solicitud enviada",
        "byId" to "Por ID", "feedEmpty" to "El feed está vacío",
        "postPlaceholder" to "Escribe algo...", "post" to "Publicar", "comments" to "Comentarios",
        "like" to "Me gusta", "likes" to "Me gusta", "share" to "Compartir",
        "workoutCompleted" to "¡Entrenamiento completado!", "streakLabel" to "Racha actual",
        "bestStreak" to "Mejor racha", "badges" to "Insignias", "noBadges" to "Aún no hay insignias",
        "rank" to "Rango", "kg" to "kg", "lbs" to "lbs", "kgLbsToggle" to "Cambiar kg/lbs",
        "exportCsv" to "Exportar CSV", "importCsv" to "Importar CSV",
        "subscription" to "Suscripción", "premium" to "Premium", "monthlyPlan" to "Plan mensual",
        "yearlyPlan" to "Plan anual", "subscribe" to "Suscribirse", "subscribed" to "Suscrito",
        "notSubscribed" to "No suscrito", "darkMode" to "Modo oscuro", "lightMode" to "Modo claro",
        "systemDefault" to "Sistema", "about" to "Acerca de", "version" to "Versión",
        "totalWorkouts" to "Total de entrenamientos", "totalWeight" to "Peso total",
        "personalRecords" to "Récords personales", "recentWorkouts" to "Entrenamientos recientes",
        "viewAll" to "Ver todo", "loading" to "Cargando...", "error" to "Error",
        "retry" to "Reintentar", "success" to "Éxito",
        "friendRequestAccepted" to "Solicitud de amistad aceptada",
        "friendRequestRejected" to "Solicitud de amistad rechazada",
        "profileUpdated" to "Perfil actualizado", "workoutSaved" to "Entrenamiento guardado",
        "workoutDeleted" to "Entrenamiento eliminado", "noExercises" to "No hay ejercicios",
        "selectExercises" to "Seleccionar ejercicios", "exerciseList" to "Lista de ejercicios",
        "customExercises" to "Ejercicios personalizados",
        "defaultExercises" to "Ejercicios predeterminados",
        "addCustomExercise" to "Agregar ejercicio personalizado",
        "enterExerciseName" to "Ingresa el nombre del ejercicio", "selectGroup" to "Seleccionar grupo",
        "addTemplate" to "Agregar plantilla", "templateName" to "Nombre de la plantilla",
        "templateSaved" to "Plantilla guardada", "templateDeleted" to "Plantilla eliminada",
        "noTemplates" to "No hay plantillas", "createFirstTemplate" to "Crea tu primera plantilla",
        "selectTemplate" to "Seleccionar plantilla", "useTemplate" to "Usar plantilla",
        "deleteTemplate" to "Eliminar plantilla", "recoveryInfo" to "Información de recuperación",
        "lastWorkout" to "Último entrenamiento", "daysSince" to "Días desde",
        "recommendedRecovery" to "Recuperación recomendada",
        "muscleGroupRecovery" to "Recuperación del grupo muscular",
        "readyToTrain" to "¡Listo para entrenar!", "needsMoreRest" to "Necesita más descanso",
        "todayIsRestDay" to "Hoy es día de descanso", "progressChart" to "Gráfico de progreso",
        "volumeOverTime" to "Volumen a lo largo del tiempo",
        "weightProgression" to "Progresión de peso", "frequencyChart" to "Gráfico de frecuencia",
        "noChartData" to "Sin datos del gráfico", "calendarView" to "Vista de calendario",
        "listView" to "Vista de lista", "sortBy" to "Ordenar por", "sortByDate" to "Por fecha",
        "sortByGroup" to "Por grupo", "filterByGroup" to "Filtrar por grupo",
        "allGroups" to "Todos", "welcomeTitle" to "¡Bienvenido!",
        "welcomeSubtitle" to "Comienza tu camino fitness", "featureSocial" to "Social",
        "featureGamification" to "Gamificación", "featureCharts" to "Gráficos",
        "featureExport" to "Exportar", "featureTemplates" to "Plantillas",
        "featureMultiLang" to "Multi-idioma", "notifications" to "Notificaciones",
        "enableNotifications" to "Activar notificaciones",
        "notificationPermissionRequired" to "Se requiere permiso de notificación",
        "friendRequestNotificationTitle" to "Solicitud de amistad",
        "friendRequestNotificationText" to "¡te envió una solicitud de amistad!",
        "profilePhotoUpdated" to "Foto de perfil actualizada",
        "nameRequired" to "El nombre es obligatorio", "settingsSaved" to "Configuración guardada",
        "darkTheme" to "Tema oscuro", "lightTheme" to "Tema claro", "systemTheme" to "Tema del sistema",
        "selectLanguage" to "Seleccionar idioma", "english" to "Inglés", "romanian" to "Rumano",
        "russian" to "Ruso", "ukrainian" to "Ucraniano", "french" to "Francés", "german" to "Alemán",
        "spanish" to "Español", "italian" to "Italiano", "turkish" to "Turco",
        "portuguese" to "Portugués", "polish" to "Polaco", "leaderLabel" to "Líder",
        "workoutsLabel" to "Entrenamientos", "totalVolume" to "Volumen total",
        "currentStreakLabel" to "Racha actual", "bestStreakLabel" to "Mejor racha",
        "badgesEarned" to "Insignias obtenidas", "days" to "días", "badge" to "Insignia", "lastPR" to "Último PR",
        "newExercise" to "Nuevo ejercicio", "exerciseNameLabel" to "Nombre del ejercicio", "add" to "Agregar", "demoExercise" to "EJERCICIO DEMO", "setLabel" to "SERIE", "prAndVolume" to "Récords y volumen", "start" to "Iniciar", "stop" to "Detener", "noSavedSetsYet" to "No hay series guardadas.", "editSet" to "Editar serie", "chooseTemplate" to "Elegir plantilla de entrenamiento", "exercises" to "ejercicios", "recovered" to "Recuperado", "almostRecovered" to "Casi recuperado", "moderate" to "Moderado", "tired" to "Cansado", "exhausted" to "Agotado", "fatigue" to "fatiga", "chooseMuscleGroup" to "Elegir grupo muscular", "changeExercise" to "Cambiar ejercicio",
        "monthlyProgress" to "Progreso mensual", "completeWorkoutsToSee" to "Completa entrenamientos para ver el progreso", "jan" to "Ene", "feb" to "Feb", "mar" to "Mar", "apr" to "Abr", "may" to "May", "jun" to "Jun", "jul" to "Jul", "aug" to "Ago", "sep" to "Sep", "oct" to "Oct", "nov" to "Nov", "dec" to "Dic", "monthlyDetails" to "Detalles mensuales", "month" to "Mes", "mon" to "Lu", "tue" to "Ma", "wed" to "Mi", "thu" to "Ju", "fri" to "Vi", "sat" to "Sa", "sun" to "Do", "noWorkouts" to "Sin entrenamiento este día", "workoutDistribution" to "Distribución de entrenamientos",
        "notNow" to "Ahora no", "subscribeNow" to "Suscribirse", "premiumFeature" to "Función Premium", "subscribersOnly" to "\$feature solo está disponible para suscriptores", "choosePlan" to "Elegir plan", "youAreSubscribed" to "¡Estás suscrito!", "muscleRecovery" to "Recuperación muscular", "waterReminder" to "Recordatorio de agua", "waterReminderTitle" to "¡Es hora de beber agua!", "waterReminderText" to "¡Mantente hidratado! Es hora de beber un vaso de agua.", "waterReminderEnabled" to "Activado", "waterReminderDisabled" to "Desactivado", "selectTime" to "Seleccionar hora", "forearms" to "Antebrazos", "neckAndTraps" to "Cuello & Trapecios", "welcome" to "Bienvenido", "athlete" to "Atleta",
        "biometricTracking" to "Seguimiento biométrico", "biometricSubtitle" to "Peso, circunferencias, grasa corporal", "addMeasurement" to "Añadir medición", "bodyFat" to "Grasa corporal", "waistCirc" to "Cintura", "hipsCirc" to "Caderas", "thighsCirc" to "Muslos", "chestCirc" to "Pecho", "armsCirc" to "Brazos", "lastMeasurement" to "Última medición", "noMeasurements" to "Sin mediciones aún", "viewCharts" to "Ver gráficos", "saveMeasurement" to "Guardar medición", "measurementSaved" to "Medición guardada", "weeksAgo" to "semanas atrás", "cm" to "cm", "percent" to "%", "deleteMeasurement" to "Eliminar medición", "biometricHistory" to "Historial de mediciones", "weightChart" to "Gráfico de peso", "bodyFatChart" to "Gráfico de grasa", "circumferenceChart" to "Gráfico de circunferencias", "date" to "Fecha", "biometricReminder" to "Recordatorio biométrico", "biometricReminderTitle" to "¡Es hora de las mediciones!", "biometricReminderText" to "No olvides registrar tus mediciones corporales semanales.", "biometricReminderEnabled" to "Activado", "biometricReminderDisabled" to "Desactivado",
        "foodJournal" to "Diario de alimentos", "scanBarcode" to "Escanear código de barras", "scanBarcodeHint" to "Coloca el código de barras en el marco para escanear el producto", "cameraPermissionRequired" to "Se requiere acceso a la cámara para escanear", "scan" to "Escanear", "scanning" to "Escaneando...", "scanBarcodeHelp" to "Asegúrate de que Google Play Services esté instalado y actualizado", "noFoodEntries" to "Sin entradas de alimentos aún", "todaysMacros" to "Macros de hoy", "caloriesLabel" to "Calorías", "proteinLabel" to "Proteínas", "carbsLabel" to "Carbos", "fatLabel" to "Grasas", "breakfast" to "Desayuno", "lunch" to "Almuerzo", "dinner" to "Cena", "snack" to "Snack", "drinks" to "Bebidas", "selectMealType" to "Seleccionar tipo de comida", "manualFoodEntry" to "Entrada manual", "foodName" to "Nombre del alimento", "brandLabel" to "Marca", "calories" to "Calorías", "protein" to "Proteínas", "carbs" to "Carbohidratos", "fat" to "Grasas", "fiber" to "Fibra",
        "aiTrainer" to "Entrenador IA", "aiTrainerWelcome" to "¡Hola! Soy tu entrenador IA", "aiTrainerHint" to "Pregúntame sobre entrenamiento, nutrición o progreso", "aiTrainerHistory" to "Historial de chats", "noHistoryYet" to "Sin historial aún", "current" to "Actual", "askAiTrainer" to "Preguntar al entrenador...", "aiSuggestion1" to "¿Qué entrenamiento me recomiendas hoy?", "aiSuggestion2" to "¿Cómo puedo aumentar el volumen?", "aiSuggestion3" to "¿Necesito un día de descanso?", "aiSuggestion4" to "¿Cómo salgo de un estancamiento?",
        "viewProfile" to "Ver perfil",
        "accountSettings" to "Configuración de cuenta", "deleteAccount" to "Eliminar cuenta",
        "privacyPolicy" to "Política de privacidad",
        "termsOfService" to "Términos de servicio", "back_" to "Volver",
        "restTimer" to "Temporizador", "startTimer" to "Iniciar", "customTimer" to "Personalizado",
        "seconds" to "Segundos", "custom" to "Personalizado",
        "exerciseHistory" to "Historial", "bestSet" to "Mejor serie", "lastSets" to "Últimas series",
        "favorite" to "Favorito", "favorites" to "Favoritos", "usageCount" to "Usado",
        "addSet" to "Agregar serie", "exerciseNotes" to "Notas ejercicio", "workoutNotes" to "Notas sesión",
        "saveNotes" to "Guardar", "editWorkout" to "Editar",
        "volume" to "Volumen", "maxWeight" to "Peso máx", "maxReps" to "Reps máx", "maxSet" to "Serie máx",
        "today" to "Hoy", "thisWeek" to "Esta semana", "thisMonth" to "Este mes",
        "totalVolumeLabel" to "Volumen total",
        "languageChanged" to "Idioma cambiado", "themeChanged" to "Tema cambiado",
        "guest" to "Invitado", "loginWithGoogle" to "Iniciar con Google", "loginWithFacebook" to "Iniciar con Facebook",
        "close" to "Cerrar", "menu" to "Menú", "profile" to "Perfil",
        "appTagline" to "Entrena. Progresa. Repite.", "or" to "o", "dark" to "Oscuro", "light" to "Claro",
        "system" to "Sistema", "languageTitle" to "Idioma", "themeTitle" to "Tema",
        "selectTheme" to "Seleccionar tema", "settingsAndMore" to "Configuración y más",
        "muscleGroups" to "Grupos musculares", "startHere" to "Empezar aquí", "back__" to "Volver",
        "englishUS" to "Inglés", "romana" to "Rumano", "russkiy" to "Ruso", "ukrainska" to "Ucraniano",
        "francais" to "Francés", "deutsch" to "Alemán", "espanol" to "Español",
        "italiano" to "Italiano", "turkce" to "Turco", "portugues" to "Portugués", "polski" to "Polaco",
        "motto1" to "Cada repetición cuenta.", "motto2" to "Más fuerte que ayer.",
        "motto3" to "Tu cuerpo, tus reglas.", "motto4" to "Supera tus límites.",
        "motto5" to "La constancia vence al talento.", "motto6" to "La disciplina es libertad.",
        "motto7" to "Sin atajos.", "motto8" to "Ganado, no dado.",
        "goodMorning" to "Buenos días", "goodAfternoon" to "Buenas tardes", "goodEvening" to "Buenas noches",
        "daysConsecutive" to "días consecutivos", "todaysWorkout" to "Entrenamiento de hoy",
        "howDoYouFeel" to "Cómo te sientes?", "tiredLabel" to "Cansado", "normalLabel" to "Normal", "energeticLabel" to "Energético",
        "plusToday" to "Aún hoy", "technicalTip" to "Consejo técnico",
        "tomorrowLabel" to "Mañana", "setWorkoutTime" to "Establecer hora de entrenamiento",
        "daysSinceLastWorkout" to "Días desde el último entrenamiento", "groupsFullyRecovered" to "Grupos completamente recuperados",
        "recoveryOnGroups" to "Recuperación de grupos",
        "weeklySummary" to "Resumen semanal", "lastWeekLabel" to "semana pasada",
        "goalLabel" to "Consejo de objetivo", "volumeLabel" to "Volumen", "topExerciseLabel" to "Top ejercicio",
        "nutritionLabel" to "Nutrición", "motivationLabel" to "Motivación",
        "gpsCardioMap" to "Cardio", "startTracking" to "Iniciar seguimiento", "stopTracking" to "Detener seguimiento",
        "pauseTracking" to "Pausar", "resumeTracking" to "Reanudar",
        "distance" to "Distancia", "pace" to "Ritmo", "speed" to "Velocidad", "duration" to "Duración",
        "savedRoutes" to "Rutas guardadas", "noSavedRoutes" to "No hay rutas guardadas",
        "routeName" to "Nombre de ruta", "saveRoute" to "Guardar ruta", "deleteRoute" to "Eliminar ruta",
        "currentLocation" to "Ubicación actual", "trackingActive" to "Seguimiento activo",
        "locationPermissionRequired" to "Se requiere permiso de ubicación",
        "restDaysTitle" to "Días de descanso y descarga", "restDaysSubtitle" to "Programación automática recuperación, estiramientos, yoga suave",
        "deloadWeek" to "Semana de descarga", "recoverySchedule" to "Calendario de recuperación",
        "stretching" to "Estiramientos", "lightYoga" to "Yoga suave", "foamRolling" to "Rodillo de espuma",
        "restDayRecommendation" to "Recomendación día de descanso", "nextRestDay" to "Próximo día de descanso",
        "muscleNeedsRest" to "Los músculos necesitan descanso", "recoveryComplete" to "Recuperación completa",
        "deloadInfo" to "Info de descarga", "suggestedActivities" to "Actividades sugeridas",
        "activeRecovery" to "Recuperación activa", "lightWalk" to "Caminata ligera",
        "swimming" to "Natación", "mobilityWork" to "Trabajo de movilidad",
        "noRestDays" to "No hay días de descanso programados", "selectDay" to "Seleccionar día",
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
        "startDeload" to "Iniciar descarga", "stretchingDescription" to "Mejora la flexibilidad y movilidad",
        "termsAndConditions" to "Términos y Condiciones", "termsPrefix" to "Al continuar, aceptas nuestros",
        "timeForDeload" to "Es hora de la descarga", "weeks" to "semanas",
        "weeksSinceLastDeload" to "Semanas desde la última descarga", "weightKg" to "Peso (kg)",
        "yogaDescription" to "Relájate y mejora la movilidad con yoga suave", "tapToEdit" to "Toca para editar", "weeklyReminder" to "Recordatorio semanal"
    ))

    private fun createIt() = Strings(mapOf(
        "appName" to "Kinetic", "dashboard" to "Pannello", "overview" to "Panoramica", "acasa" to "Home", "workouts" to "Allenamenti", "stats" to "Statistiche", "waterIntake" to "Assunzione di acqua", "waterGoal" to "Obiettivo acqua", "addWater" to "Aggiungi acqua", "dailyWater" to "Acqua giornaliera", "height" to "Altezza", "personalInfo" to "Info personali", "waterAutoCalc" to "Calcolo auto acqua", "ml" to "ml", "templates" to "Modelli",
            "waterHistory" to "Cronologia idratazione", "last7Days" to "Ultimi 7 giorni", "everyDay" to "Ogni giorno", "reminder" to "Promemoria",
        "recovery" to "Recupero", "progress" to "Progressi", "feed" to "Feed", "friends" to "Amici",
        "leaderboard" to "Classifica", "all" to "Tutti", "settings" to "Impostazioni", "language" to "Lingua",
        "units" to "Unità", "logout" to "Esci", "login" to "Accedi", "signUp" to "Registrati",
        "email" to "Email", "password" to "Password", "forgotPassword" to "Password dimenticata?",
        "orContinueWith" to "Oppure continua con", "loginAsGuest" to "Accedi come ospite",
        "welcomeBack" to "Bentornato!", "createAccount" to "Crea account",
        "goalStrength" to "Forza", "goalMass" to "Massa muscolare", "goalWeightLoss" to "Perdita di peso",
        "goalMaintenance" to "Mantenimento", "selectGoal" to "Seleziona il tuo obiettivo",
        "next" to "Avanti", "skip" to "Salta", "finish" to "Fine", "back" to "Indietro",
        "profileSetup" to "Configura profilo", "enterName" to "Inserisci il tuo nome",
        "pickPhoto" to "Scegli una foto", "saveProfile" to "Salva profilo", "chest" to "Petto",
        "shoulders" to "Spalle",             "arms" to "Braccia", "biceps" to "Bicipite", "triceps" to "Tricipite",
        "legs" to "Gambe", "thighs" to "Cosce", "glutes" to "Glutei", "calves" to "Polpacci",
        "core" to "Core",
        "cardio" to "Cardio", "sets" to "Serie", "reps" to "Ripetizioni", "weight" to "Peso",
        "addExercise" to "Aggiungi esercizio", "saveWorkout" to "Salva allenamento",
        "startWorkout" to "Inizia allenamento", "nextExercise" to "Esercizio successivo", "notes" to "Note", "cancel" to "Annulla",
        "confirm" to "Conferma", "delete" to "Elimina", "edit" to "Modifica", "search" to "Cerca",
        "noDataYet" to "Nessun dato ancora", "friendRequests" to "Richieste di amicizia",
        "sendRequest" to "Invia richiesta", "accept" to "Accetta", "reject" to "Rifiuta",
        "removeFriend" to "Rimuovi amico", "noFriends" to "Nessun amico ancora",
        "searchUsers" to "Cerca utenti", "userId" to "ID utente",
        "searchByNameOrId" to "Cerca per nome o ID", "incomingRequests" to "Richieste in arrivo",
        "noIncomingRequests" to "Nessuna richiesta in arrivo", "yourFriends" to "I tuoi amici",
        "sendFriendRequest" to "Invia richiesta di amicizia", "friendRequestSent" to "Richiesta inviata",
        "byId" to "Per ID", "feedEmpty" to "Il feed è vuoto", "postPlaceholder" to "Scrivi qualcosa...",
        "post" to "Pubblica", "comments" to "Commenti", "like" to "Mi piace", "likes" to "Mi piace",
        "share" to "Condividi", "workoutCompleted" to "Allenamento completato!",
        "streakLabel" to "Serie attuale", "bestStreak" to "Miglior serie", "badges" to "Badge",
        "noBadges" to "Nessun badge ancora", "rank" to "Grado", "kg" to "kg", "lbs" to "lbs",
        "kgLbsToggle" to "Cambia kg/lbs", "exportCsv" to "Esporta CSV", "importCsv" to "Importa CSV",
        "subscription" to "Abbonamento", "premium" to "Premium", "monthlyPlan" to "Piano mensile",
        "yearlyPlan" to "Piano annuale", "subscribe" to "Abbonati", "subscribed" to "Abbonato",
        "notSubscribed" to "Non abbonato", "darkMode" to "Modalità scura",
        "lightMode" to "Modalità chiara", "systemDefault" to "Sistema", "about" to "Informazioni",
        "version" to "Versione", "totalWorkouts" to "Totale allenamenti", "totalWeight" to "Peso totale",
        "personalRecords" to "Record personali", "recentWorkouts" to "Allenamenti recenti",
        "viewAll" to "Vedi tutto", "loading" to "Caricamento...", "error" to "Errore",
        "retry" to "Riprova", "success" to "Successo",
        "friendRequestAccepted" to "Richiesta di amicizia accettata",
        "friendRequestRejected" to "Richiesta di amicizia rifiutata",
        "profileUpdated" to "Profilo aggiornato", "workoutSaved" to "Allenamento salvato",
        "workoutDeleted" to "Allenamento eliminato", "noExercises" to "Nessun esercizio",
        "selectExercises" to "Seleziona esercizi", "exerciseList" to "Lista esercizi",
        "customExercises" to "Esercizi personalizzati",
        "defaultExercises" to "Esercizi predefiniti",
        "addCustomExercise" to "Aggiungi esercizio personalizzato",
        "enterExerciseName" to "Inserisci il nome dell'esercizio", "selectGroup" to "Seleziona gruppo",
        "addTemplate" to "Aggiungi modello", "templateName" to "Nome del modello",
        "templateSaved" to "Modello salvato", "templateDeleted" to "Modello eliminato",
        "noTemplates" to "Nessun modello", "createFirstTemplate" to "Crea il tuo primo modello",
        "selectTemplate" to "Seleziona modello", "useTemplate" to "Usa modello",
        "deleteTemplate" to "Elimina modello", "recoveryInfo" to "Info recupero",
        "lastWorkout" to "Ultimo allenamento", "daysSince" to "Giorni da",
        "recommendedRecovery" to "Recupero consigliato",
        "muscleGroupRecovery" to "Recupero gruppo muscolare", "readyToTrain" to "Pronto per allenarti!",
        "needsMoreRest" to "Ha bisogno di più riposo", "todayIsRestDay" to "Oggi è giorno di riposo",
        "progressChart" to "Grafico progressi", "volumeOverTime" to "Volume nel tempo",
        "weightProgression" to "Progressione peso", "frequencyChart" to "Grafico frequenza",
        "noChartData" to "Nessun dato grafico", "calendarView" to "Vista calendario",
        "listView" to "Vista elenco", "sortBy" to "Ordina per", "sortByDate" to "Per data",
        "sortByGroup" to "Per gruppo", "filterByGroup" to "Filtra per gruppo",
        "allGroups" to "Tutti", "welcomeTitle" to "Benvenuto!",
        "welcomeSubtitle" to "Inizia il tuo percorso fitness", "featureSocial" to "Sociale",
        "featureGamification" to "Gamification", "featureCharts" to "Grafici",
        "featureExport" to "Esporta", "featureTemplates" to "Modelli",
        "featureMultiLang" to "Multi-lingua", "notifications" to "Notifiche",
        "enableNotifications" to "Attiva notifiche",
        "notificationPermissionRequired" to "Autorizzazione notifiche richiesta",
        "friendRequestNotificationTitle" to "Richiesta di amicizia",
        "friendRequestNotificationText" to "ti ha inviato una richiesta di amicizia!",
        "profilePhotoUpdated" to "Foto profilo aggiornata", "nameRequired" to "Il nome è obbligatorio",
        "settingsSaved" to "Impostazioni salvate", "darkTheme" to "Tema scuro",
        "lightTheme" to "Tema chiaro", "systemTheme" to "Tema di sistema",
        "selectLanguage" to "Seleziona lingua", "english" to "Inglese", "romanian" to "Rumeno",
        "russian" to "Russo", "ukrainian" to "Ucraino", "french" to "Francese", "german" to "Tedesco",
        "spanish" to "Spagnolo", "italian" to "Italiano", "turkish" to "Turco",
        "portuguese" to "Portoghese", "polish" to "Polacco", "leaderLabel" to "Leader",
        "workoutsLabel" to "Allenamenti", "totalVolume" to "Volume totale",
        "currentStreakLabel" to "Serie attuale", "bestStreakLabel" to "Miglior serie",
        "badgesEarned" to "Badge ottenuti", "days" to "giorni", "badge" to "Badge", "lastPR" to "Ultimo PR",
        "newExercise" to "Nuovo esercizio", "exerciseNameLabel" to "Nome esercizio", "add" to "Aggiungi", "demoExercise" to "ESERCIZIO DEMO", "setLabel" to "SERIE", "prAndVolume" to "Record e volume", "start" to "Avvia", "stop" to "Ferma", "noSavedSetsYet" to "Nessuna serie salvata.", "editSet" to "Modifica serie", "chooseTemplate" to "Scegli modello di allenamento", "exercises" to "esercizi", "recovered" to "Recuperato", "almostRecovered" to "Quasi recuperato", "moderate" to "Moderato", "tired" to "Stanco", "exhausted" to "Esausto", "fatigue" to "affaticamento", "chooseMuscleGroup" to "Scegli gruppo muscolare", "changeExercise" to "Cambia esercizio",
        "monthlyProgress" to "Progresso mensile", "completeWorkoutsToSee" to "Completa gli allenamenti per vedere i progressi", "jan" to "Gen", "feb" to "Feb", "mar" to "Mar", "apr" to "Apr", "may" to "Mag", "jun" to "Giu", "jul" to "Lug", "aug" to "Ago", "sep" to "Set", "oct" to "Ott", "nov" to "Nov", "dec" to "Dic", "monthlyDetails" to "Dettagli mensili", "month" to "Mese", "mon" to "Lu", "tue" to "Ma", "wed" to "Me", "thu" to "Gi", "fri" to "Ve", "sat" to "Sa", "sun" to "Do", "noWorkouts" to "Nessun allenamento in questo giorno", "workoutDistribution" to "Distribuzione allenamenti",
        "notNow" to "Non ora", "subscribeNow" to "Abbonati ora", "premiumFeature" to "Funzionalità Premium", "subscribersOnly" to "\$feature è disponibile solo per gli abbonati", "choosePlan" to "Scegli un piano", "youAreSubscribed" to "Sei abbonato!", "muscleRecovery" to "Recupero muscolare", "waterReminder" to "Promemoria acqua", "waterReminderTitle" to "È ora di bere acqua!", "waterReminderText" to "Resta idratato! È ora di bere un bicchiere d'acqua.", "waterReminderEnabled" to "Attivato", "waterReminderDisabled" to "Disattivato", "selectTime" to "Seleziona ora", "forearms" to "Avambracci", "neckAndTraps" to "Collo & Trapezi", "welcome" to "Benvenuto", "athlete" to "Atleta",
        "biometricTracking" to "Monitoraggio biometrico", "biometricSubtitle" to "Peso, circonferenze, grasso corporeo", "addMeasurement" to "Aggiungi misurazione", "bodyFat" to "Grasso corporeo", "waistCirc" to "Vita", "hipsCirc" to "Fianchi", "thighsCirc" to "Cosce", "chestCirc" to "Petto", "armsCirc" to "Braccia", "lastMeasurement" to "Ultima misurazione", "noMeasurements" to "Nessuna misurazione ancora", "viewCharts" to "Vedi grafici", "saveMeasurement" to "Salva misurazione", "measurementSaved" to "Misurazione salvata", "weeksAgo" to "settimane fa", "cm" to "cm", "percent" to "%", "deleteMeasurement" to "Elimina misurazione", "biometricHistory" to "Cronologia misurazioni", "weightChart" to "Grafico del peso", "bodyFatChart" to "Grafico del grasso", "circumferenceChart" to "Grafico delle circonferenze", "date" to "Data", "biometricReminder" to "Promemoria biometrico", "biometricReminderTitle" to "È ora delle misurazioni!", "biometricReminderText" to "Non dimenticare di registrare le tue misurazioni corporee settimanali.", "biometricReminderEnabled" to "Attivato", "biometricReminderDisabled" to "Disattivato",
        "foodJournal" to "Diario alimentare", "scanBarcode" to "Scansiona codice a barre", "scanBarcodeHint" to "Posiziona il codice a barre nell'inquadramento per scansionare il prodotto", "cameraPermissionRequired" to "L'accesso alla fotocamera è necessario per la scansione", "scan" to "Scansiona", "scanning" to "Scansione in corso...", "scanBarcodeHelp" to "Assicurati che Google Play Services sia installato e aggiornato", "noFoodEntries" to "Nessuna voce alimentare ancora", "todaysMacros" to "Macronutrienti di oggi", "caloriesLabel" to "Calorie", "proteinLabel" to "Proteine", "carbsLabel" to "Carboidrati", "fatLabel" to "Grassi", "breakfast" to "Colazione", "lunch" to "Pranzo", "dinner" to "Cena", "snack" to "Spuntino", "drinks" to "Bevande", "selectMealType" to "Seleziona tipo di pasto", "manualFoodEntry" to "Inserimento manuale", "foodName" to "Nome alimento", "brandLabel" to "Marca", "calories" to "Calorie", "protein" to "Proteine", "carbs" to "Carboidrati", "fat" to "Grassi", "fiber" to "Fibre",
        "aiTrainer" to "Allenatore IA", "aiTrainerWelcome" to "Ciao! Sono il tuo allenatore IA", "aiTrainerHint" to "Chiedimi di allenamento, nutrizione o progressi", "aiTrainerHistory" to "Cronologia chat", "noHistoryYet" to "Nessuna cronologia", "current" to "Attuale", "askAiTrainer" to "Chiedi all'allenatore...", "aiSuggestion1" to "Che allenamento mi consigli oggi?", "aiSuggestion2" to "Come posso aumentare il volume?", "aiSuggestion3" to "Ho bisogno di un giorno di riposo?", "aiSuggestion4" to "Come supero un plateau?",
        "viewProfile" to "Vedi profilo",
        "accountSettings" to "Impostazioni account", "deleteAccount" to "Elimina account",
        "privacyPolicy" to "Informativa sulla privacy",
        "termsOfService" to "Termini di servizio", "back_" to "Indietro",
        "restTimer" to "Timer pausa", "startTimer" to "Avvia timer", "customTimer" to "Timer custom",
        "seconds" to "Secondi", "custom" to "Custom",
        "exerciseHistory" to "Cronologia", "bestSet" to "Miglior serie", "lastSets" to "Ultime serie",
        "favorite" to "Preferito", "favorites" to "Preferiti", "usageCount" to "Usato",
        "addSet" to "Aggiungi serie", "exerciseNotes" to "Note esercizio", "workoutNotes" to "Note allenamento",
        "saveNotes" to "Salva", "editWorkout" to "Modifica",
        "volume" to "Volume", "maxWeight" to "Peso máx", "maxReps" to "Rep máx", "maxSet" to "Serie máx",
        "today" to "Oggi", "thisWeek" to "Questa settimana", "thisMonth" to "Questo mese",
        "totalVolumeLabel" to "Volume totale",
        "languageChanged" to "Lingua cambiata", "themeChanged" to "Tema cambiato",
        "guest" to "Ospite", "loginWithGoogle" to "Accedi con Google", "loginWithFacebook" to "Accedi con Facebook",
        "close" to "Chiudi", "menu" to "Menu", "profile" to "Profilo",
        "appTagline" to "Allena. Progredisce. Ripeti.", "or" to "o", "dark" to "Scuro", "light" to "Chiaro",
        "system" to "Sistema", "languageTitle" to "Lingua", "themeTitle" to "Tema",
        "selectTheme" to "Seleziona tema", "settingsAndMore" to "Impostazioni e altro",
        "muscleGroups" to "Gruppi muscolari", "startHere" to "Inizia qui", "back__" to "Indietro",
        "englishUS" to "Inglese", "romana" to "Rumeno", "russkiy" to "Russo", "ukrainska" to "Ucraino",
        "francais" to "Francese", "deutsch" to "Tedesco", "espanol" to "Spagnolo",
        "italiano" to "Italiano", "turkce" to "Turco", "portugues" to "Portoghese", "polski" to "Polacco",
        "motto1" to "Ogni ripetizione conta.", "motto2" to "Più forte di ieri.",
        "motto3" to "Il tuo corpo, le tue regole.", "motto4" to "Supera i tuoi limiti.",
        "motto5" to "La costanza batte il talento.", "motto6" to "La disciplina è libertà.",
        "motto7" to "Nessuna scorciatoia.", "motto8" to "Guadagnato, non dato.",
        "goodMorning" to "Buongiorno", "goodAfternoon" to "Buon pomeriggio", "goodEvening" to "Buonasera",
        "daysConsecutive" to "giorni consecutivi", "todaysWorkout" to "Allenamento di oggi",
        "howDoYouFeel" to "Come ti senti?", "tiredLabel" to "Stanco", "normalLabel" to "Normale", "energeticLabel" to "Energico",
        "plusToday" to "Ancora oggi", "technicalTip" to "Consiglio tecnico",
        "tomorrowLabel" to "Domani", "setWorkoutTime" to "Impostare l'orario di allenamento",
        "daysSinceLastWorkout" to "Giorni dall'ultimo allenamento", "groupsFullyRecovered" to "Gruppi completamente recuperati",
        "recoveryOnGroups" to "Recupero dei gruppi",
        "weeklySummary" to "Riepilogo settimanale", "lastWeekLabel" to "scorsa settimana",
        "goalLabel" to "Consiglio obiettivo", "volumeLabel" to "Volume", "topExerciseLabel" to "Top esercizio",
        "nutritionLabel" to "Nutrizione", "motivationLabel" to "Motivazione",
        "gpsCardioMap" to "Cardio", "startTracking" to "Inizia tracciamento", "stopTracking" to "Ferma tracciamento",
        "pauseTracking" to "Pausa", "resumeTracking" to "Riprendi",
        "distance" to "Distanza", "pace" to "Ritmo", "speed" to "Velocità", "duration" to "Durata",
        "savedRoutes" to "Percorsi salvati", "noSavedRoutes" to "Nessun percorso salvato",
        "routeName" to "Nome percorso", "saveRoute" to "Salva percorso", "deleteRoute" to "Elimina percorso",
        "currentLocation" to "Posizione attuale", "trackingActive" to "Tracciamento attivo",
        "locationPermissionRequired" to "Autorizzazione posizione necessaria",
        "restDaysTitle" to "Giorni di riposo e scarico", "restDaysSubtitle" to "Pianificazione automatica recupero, stretching, yoga leggero",
        "deloadWeek" to "Settimana di scarico", "recoverySchedule" to "Programma recupero",
        "stretching" to "Stretching", "lightYoga" to "Yoga leggero", "foamRolling" to "Rullo schiuma",
        "restDayRecommendation" to "Raccomandazione giorno di riposo", "nextRestDay" to "Prossimo giorno di riposo",
        "muscleNeedsRest" to "I muscoli hanno bisogno di riposo", "recoveryComplete" to "Recupero completato",
        "deloadInfo" to "Info scarico", "suggestedActivities" to "Attività suggerite",
        "activeRecovery" to "Recupero attivo", "lightWalk" to "Camminata leggera",
        "swimming" to "Nuoto", "mobilityWork" to "Lavoro di mobilità",
        "noRestDays" to "Nessun giorno di riposo programmato", "selectDay" to "Seleziona giorno",
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
        "startDeload" to "Inizia scarico", "stretchingDescription" to "Migliora flessibilità e mobilità",
        "termsAndConditions" to "Termini e Condizioni", "termsPrefix" to "Continuando, accetti i nostri",
        "timeForDeload" to "È ora dello scarico", "weeks" to "settimane",
        "weeksSinceLastDeload" to "Settimane dall'ultimo scarico", "weightKg" to "Peso (kg)",
        "yogaDescription" to "Rilassati e migliora la mobilità con yoga leggero", "tapToEdit" to "Tocca per modificare", "weeklyReminder" to "Promemoria settimanale"
    ))

    private fun createTr() = Strings(mapOf(
        "appName" to "Kinetic", "dashboard" to "Gösterge Paneli", "overview" to "Genel Bakış", "acasa" to "Ana Sayfa", "workouts" to "Egzersizler", "stats" to "İstatistikler", "waterIntake" to "Su Tüketimi", "waterGoal" to "Su Hedefi", "addWater" to "Su Ekle", "dailyWater" to "Günlük Su", "height" to "Boy", "personalInfo" to "Kişisel Bilgi", "waterAutoCalc" to "Otomatik Su Hesabı", "ml" to "ml", "templates" to "Şablonlar",
            "waterHistory" to "Hidrasyon geçmişi", "last7Days" to "Son 7 gün", "everyDay" to "Her gün", "reminder" to "Hatırlatıcı",
        "recovery" to "İyileşme", "progress" to "İlerleme", "feed" to "Akış", "friends" to "Arkadaşlar",
        "leaderboard" to "Sıralama", "all" to "Tümü", "settings" to "Ayarlar", "language" to "Dil",
        "units" to "Birimler", "logout" to "Çıkış", "login" to "Giriş", "signUp" to "Kayıt Ol",
        "email" to "E-posta", "password" to "Şifre", "forgotPassword" to "Şifreni mi unuttun?",
        "orContinueWith" to "Veya devam et", "loginAsGuest" to "Misafir olarak giriş yap",
        "welcomeBack" to "Tekrar hoş geldin!", "createAccount" to "Hesap Oluştur",
        "goalStrength" to "Güç", "goalMass" to "Kas Kütlesi", "goalWeightLoss" to "Kilo Verme",
        "goalMaintenance" to "Koruma", "selectGoal" to "Hedefinizi seçin", "next" to "İleri",
        "skip" to "Atla", "finish" to "Bitir", "back" to "Geri", "profileSetup" to "Profil Ayarları",
        "enterName" to "Adınızı girin", "pickPhoto" to "Fotoğraf seç", "saveProfile" to "Profili Kaydet",
        "chest" to "Göğüs", "shoulders" to "Omuzlar",             "arms" to "Kollar", "biceps" to "Biceps", "triceps" to "Triceps",
        "legs" to "Bacaklar", "thighs" to "Uyluk", "glutes" to "Kalça", "calves" to "Baldırlar",
        "core" to "Core", "cardio" to "Kardio", "sets" to "Set", "reps" to "Tekrarlar", "weight" to "Ağırlık",
        "addExercise" to "Egzersiz Ekle", "saveWorkout" to "Antrenmanı Kaydet",
        "startWorkout" to "Antrenmanı Başlat", "nextExercise" to "Sonraki egzersiz", "notes" to "Notlar", "cancel" to "İptal",
        "confirm" to "Onayla", "delete" to "Sil", "edit" to "Düzenle", "search" to "Ara",
        "noDataYet" to "Henüz veri yok", "friendRequests" to "Arkadaşlık İstekleri",
        "sendRequest" to "İstek Gönder", "accept" to "Kabul Et", "reject" to "Reddet",
        "removeFriend" to "Arkadaşı Kaldır", "noFriends" to "Henüz arkadaş yok",
        "searchUsers" to "Kullanıcı Ara", "userId" to "Kullanıcı ID",
        "searchByNameOrId" to "İsim veya ID ile ara", "incomingRequests" to "Gelen İstekler",
        "noIncomingRequests" to "Gelen istek yok", "yourFriends" to "Arkadaşların",
        "sendFriendRequest" to "Arkadaşlık İsteği Gönder", "friendRequestSent" to "İstek Gönderildi",
        "byId" to "ID ile", "feedEmpty" to "Akış boş", "postPlaceholder" to "Bir şey yaz...",
        "post" to "Paylaş", "comments" to "Yorumlar", "like" to "Beğen", "likes" to "Beğeni",
        "share" to "Paylaş", "workoutCompleted" to "Antrenman Tamamlandı!",
        "streakLabel" to "Mevcut Seri", "bestStreak" to "En İyi Seri", "badges" to "Rozetler",
        "noBadges" to "Henüz rozet yok", "rank" to "Sıralama", "kg" to "kg", "lbs" to "lbs",
        "kgLbsToggle" to "kg/lbs Değiştir", "exportCsv" to "CSV Dışa Aktar",
        "importCsv" to "CSV İçe Aktar", "subscription" to "Abonelik", "premium" to "Premium",
        "monthlyPlan" to "Aylık Plan", "yearlyPlan" to "Yıllık Plan",
        "subscribe" to "Abone Ol", "subscribed" to "Abone", "notSubscribed" to "Abone Değil",
        "darkMode" to "Karanlık Mod", "lightMode" to "Aydınlık Mod", "systemDefault" to "Sistem",
        "about" to "Hakkında", "version" to "Sürüm", "totalWorkouts" to "Toplam Antrenman",
        "totalWeight" to "Toplam Ağırlık", "personalRecords" to "Kişisel Rekorlar",
        "recentWorkouts" to "Son Antrenmanlar", "viewAll" to "Tümünü Gör",
        "loading" to "Yükleniyor...", "error" to "Hata", "retry" to "Yeniden Dene",
        "success" to "Başarılı", "friendRequestAccepted" to "Arkadaşlık isteği kabul edildi",
        "friendRequestRejected" to "Arkadaşlık isteği reddedildi",
        "profileUpdated" to "Profil güncellendi", "workoutSaved" to "Antrenman kaydedildi",
        "workoutDeleted" to "Antrenman silindi", "noExercises" to "Egzersiz yok",
        "selectExercises" to "Egzersiz Seç", "exerciseList" to "Egzersiz Listesi",
        "customExercises" to "Özel Egzersizler", "defaultExercises" to "Varsayılan Egzersizler",
        "addCustomExercise" to "Özel Egzersiz Ekle",
        "enterExerciseName" to "Egzersiz adını girin", "selectGroup" to "Grup Seç",
        "addTemplate" to "Şablon Ekle", "templateName" to "Şablon Adı",
        "templateSaved" to "Şablon Kaydedildi", "templateDeleted" to "Şablon Silindi",
        "noTemplates" to "Şablon Yok", "createFirstTemplate" to "İlk şablonunuzu oluşturun",
        "selectTemplate" to "Şablon Seç", "useTemplate" to "Şablonu Kullan",
        "deleteTemplate" to "Şablonu Sil", "recoveryInfo" to "İyileşme Bilgisi",
        "lastWorkout" to "Son Antrenman", "daysSince" to "Günden beri",
        "recommendedRecovery" to "Önerilen İyileşme",
        "muscleGroupRecovery" to "Kas Grubu İyileşmesi", "readyToTrain" to "Antrenmana hazır!",
        "needsMoreRest" to "Daha fazla dinlenmeye ihtiyacı var",
        "todayIsRestDay" to "Bugün dinlenme günü", "progressChart" to "İlerleme Grafiği",
        "volumeOverTime" to "Zamana Göre Hacim", "weightProgression" to "Ağırlık İlerlemesi",
        "frequencyChart" to "Sıklık Grafiği", "noChartData" to "Grafik verisi yok",
        "calendarView" to "Takvim Görünümü", "listView" to "Liste Görünümü",
        "sortBy" to "Sırala", "sortByDate" to "Tarihe Göre", "sortByGroup" to "Gruba Göre",
        "filterByGroup" to "Gruba Göre Filtrele", "allGroups" to "Tümü",
        "welcomeTitle" to "Hoş Geldiniz!", "welcomeSubtitle" to "Fitness yolculuğunuza başlayın",
        "featureSocial" to "Sosyal", "featureGamification" to "Oyunlaştırma",
        "featureCharts" to "Grafikler", "featureExport" to "Dışa Aktar",
        "featureTemplates" to "Şablonlar", "featureMultiLang" to "Çok Dilli",
        "notifications" to "Bildirimler", "enableNotifications" to "Bildirimleri Etkinleştir",
        "notificationPermissionRequired" to "Bildirim izni gerekli",
        "friendRequestNotificationTitle" to "Arkadaşlık İsteği",
        "friendRequestNotificationText" to "size arkadaşlık isteği gönderdi!",
        "profilePhotoUpdated" to "Profil fotoğrafı güncellendi",
        "nameRequired" to "İsim gerekli", "settingsSaved" to "Ayarlar kaydedildi",
        "darkTheme" to "Karanlık Tema", "lightTheme" to "Aydınlık Tema",
        "systemTheme" to "Sistem Teması", "selectLanguage" to "Dil Seçin",
        "english" to "İngilizce", "romanian" to "Rumence", "russian" to "Rusça",
        "ukrainian" to "Ukraynaca", "french" to "Fransızca", "german" to "Almanca",
        "spanish" to "İspanyolca", "italian" to "İtalyanca", "turkish" to "Türkçe",
        "portuguese" to "Portekizce", "polish" to "Lehçe", "leaderLabel" to "Lider",
        "workoutsLabel" to "Antrenmanlar", "totalVolume" to "Toplam Hacim",
        "currentStreakLabel" to "Mevcut Seri", "bestStreakLabel" to "En İyi Seri",
        "badgesEarned" to "Kazanılan Rozetler", "days" to "gün", "badge" to "Rozet", "lastPR" to "Son PR",
        "newExercise" to "Yeni egzersiz", "exerciseNameLabel" to "Egzersiz adı", "add" to "Ekle", "demoExercise" to "DEMO EGZERSİZ", "setLabel" to "SET", "prAndVolume" to "Kişisel rekorlar ve hacim", "start" to "Başlat", "stop" to "Durdur", "noSavedSetsYet" to "Henüz kayıtlı set yok.", "editSet" to "Seti düzenle", "chooseTemplate" to "Antrenman şablonu seçin", "exercises" to "egzersiz", "recovered" to "İyileşti", "almostRecovered" to "Neredeyse iyileşti", "moderate" to "Orta", "tired" to "Yorgun", "exhausted" to "Tükenmiş", "fatigue" to "yorgunluk", "chooseMuscleGroup" to "Kas grubu seçin", "changeExercise" to "Egzersizi değiştir",
        "monthlyProgress" to "Aylık ilerleme", "completeWorkoutsToSee" to "İlerlemeyi görmek için antrenmanları tamamlayın", "jan" to "Oca", "feb" to "Şub", "mar" to "Mar", "apr" to "Nis", "may" to "May", "jun" to "Haz", "jul" to "Tem", "aug" to "Ağu", "sep" to "Eyl", "oct" to "Eki", "nov" to "Kas", "dec" to "Ara", "monthlyDetails" to "Aylık detaylar", "month" to "Ay", "mon" to "Pzt", "tue" to "Sal", "wed" to "Çar", "thu" to "Per", "fri" to "Cum", "sat" to "Cmt", "sun" to "Paz", "noWorkouts" to "Bu gün antrenman yok", "workoutDistribution" to "Antrenman dağılımı",
        "notNow" to "Şimdi değil", "subscribeNow" to "Şimdi abone ol", "premiumFeature" to "Premium Özellik", "subscribersOnly" to "\$feature sadece aboneler için mevcut", "choosePlan" to "Bir plan seçin", "youAreSubscribed" to "Abone oldunuz!", "muscleRecovery" to "Kas İyileşmesi", "waterReminder" to "Su Hatırlatıcı", "waterReminderTitle" to "Su içme zamanı!", "waterReminderText" to "Su için! Bir bardak su içme zamanı.", "waterReminderEnabled" to "Aktif", "waterReminderDisabled" to "Pasif", "selectTime" to "Saat seç", "forearms" to "Ön kollar", "neckAndTraps" to "Boyun & Trapez kasları", "welcome" to "Hoş geldin", "athlete" to "Sporcu",
        "biometricTracking" to "Biyometrik Takip", "biometricSubtitle" to "Ağırlık, çevre ölçümü, vücut yağı", "addMeasurement" to "Ölçüm ekle", "bodyFat" to "Vücut yağı", "waistCirc" to "Bel", "hipsCirc" to "Kalça", "thighsCirc" to "Uyluk", "chestCirc" to "Göğüs", "armsCirc" to "Kollar", "lastMeasurement" to "Son ölçüm", "noMeasurements" to "Henüz ölçüm yok", "viewCharts" to "Grafikleri gör", "saveMeasurement" to "Ölçümü kaydet", "measurementSaved" to "Ölçüm kaydedildi", "weeksAgo" to "hafta önce", "cm" to "cm", "percent" to "%", "deleteMeasurement" to "Ölçümü sil", "biometricHistory" to "Ölçüm geçmişi", "weightChart" to "Ağırlık grafiği", "bodyFatChart" to "Yağ grafiği", "circumferenceChart" to "Çevre grafiği", "date" to "Tarih", "biometricReminder" to "Biyometrik Hatırlatıcı", "biometricReminderTitle" to "Ölçüm zamanı!", "biometricReminderText" to "Haftalık vücut ölçümlerinizi kaydetmeyi unutmayın.", "biometricReminderEnabled" to "Aktif", "biometricReminderDisabled" to "Pasif",
        "foodJournal" to "Besin Günlüğü", "scanBarcode" to "Barkod Tara", "scanBarcodeHint" to "Ürünü taramak için barkodu çerçeveye yerleştirin", "cameraPermissionRequired" to "Tarama için kamera izni gereklidir", "scan" to "Tara", "scanning" to "Taranıyor...", "scanBarcodeHelp" to "Google Play Services'in yüklü ve güncel olduğundan emin olun", "noFoodEntries" to "Henüz besin girişi yok", "todaysMacros" to "Bugünün Makroları", "caloriesLabel" to "Kalori", "proteinLabel" to "Protein", "carbsLabel" to "Karb", "fatLabel" to "Yağ", "breakfast" to "Kahvaltı", "lunch" to "Öğle yemeği", "dinner" to "Akşam yemeği", "snack" to "Atıştırmalık", "drinks" to "İçecekler", "selectMealType" to "Öğün türü seçin", "manualFoodEntry" to "Manuel Giriş", "foodName" to "Besin adı", "brandLabel" to "Marka", "calories" to "Kalori", "protein" to "Protein", "carbs" to "Karbonhidrat", "fat" to "Yağ", "fiber" to "Lif",
        "aiTrainer" to "AI Antrenör", "aiTrainerWelcome" to "Merhaba! Ben yapay zeka antrenörünüz", "aiTrainerHint" to "Antrenman, beslenme veya ilerleme hakkında sorun", "aiTrainerHistory" to "Sohbet geçmişi", "noHistoryYet" to "Henüz geçmiş yok", "current" to "Mevcut", "askAiTrainer" to "Antrenöre sor...", "aiSuggestion1" to "Bugun ne onerirsiniz?", "aiSuggestion2" to "Hacmi nasil artirabilirim?", "aiSuggestion3" to "Dinlenme gunune ihtiyacim var mi?", "aiSuggestion4" to "Platodan nasil cikarim?",
        "viewProfile" to "Profili Gör",
        "accountSettings" to "Hesap Ayarları", "deleteAccount" to "Hesabı Sil",
        "privacyPolicy" to "Gizlilik Politikası",
        "termsOfService" to "Kullanım Koşulları", "back_" to "Geri",
        "restTimer" to "Dinlenme zamanlayıcı", "startTimer" to "Başlat", "customTimer" to "Özel zamanlayıcı",
        "seconds" to "Saniye", "custom" to "Özel",
        "exerciseHistory" to "Geçmiş", "bestSet" to "En iyi set", "lastSets" to "Son setler",
        "favorite" to "Favori", "favorites" to "Favoriler", "usageCount" to "Kullanıldı",
        "addSet" to "Set ekle", "exerciseNotes" to "Notlar", "workoutNotes" to "Antrenman notları",
        "saveNotes" to "Kaydet", "editWorkout" to "Düzenle",
        "volume" to "Hacim", "maxWeight" to "Max ağırlık", "maxReps" to "Max tekrar", "maxSet" to "Max set",
        "today" to "Bugün", "thisWeek" to "Bu hafta", "thisMonth" to "Bu ay",
        "totalVolumeLabel" to "Toplam hacim",
        "languageChanged" to "Dil değiştirildi", "themeChanged" to "Tema değiştirildi",
        "guest" to "Misafir", "loginWithGoogle" to "Google ile giriş", "loginWithFacebook" to "Facebook ile giriş",
        "close" to "Kapat", "menu" to "Menü", "profile" to "Profil",
        "appTagline" to "Antrenman yap. Geliş. Tekrarla.", "or" to "veya", "dark" to "Karanlık", "light" to "Aydınlık",
        "system" to "Sistem", "languageTitle" to "Dil", "themeTitle" to "Tema",
        "selectTheme" to "Tema seç", "settingsAndMore" to "Ayarlar ve daha fazlası",
        "muscleGroups" to "Kas grupları", "startHere" to "Buradan başla", "back__" to "Geri",
        "englishUS" to "İngilizce", "romana" to "Rumence", "russkiy" to "Rusça", "ukrainska" to "Ukraynaca",
        "francais" to "Fransızca", "deutsch" to "Almanca", "espanol" to "İspanyolca",
        "italiano" to "İtalyanca", "turkce" to "Türkçe", "portugues" to "Portekizce", "polski" to "Lehçe",
        "motto1" to "Her tekrar sayılır.", "motto2" to "Dünden daha güçlü.",
        "motto3" to "Senin bedenin, senin kuralların.", "motto4" to "Limitlerini zorla.",
        "motto5" to "Tutarlılık yeteneği yener.", "motto6" to "Disiplin özgürlüktür.",
        "motto7" to "Kısa yol yok.", "motto8" to "Kazanılmış, verilmiş değil.",
        "goodMorning" to "Günaydın", "goodAfternoon" to "İyi günler", "goodEvening" to "İyi akşamlar",
        "daysConsecutive" to "art arda gün", "todaysWorkout" to "Bugünkü antrenman",
        "howDoYouFeel" to "Nasıl hissediyorsun?", "tiredLabel" to "Yorgun", "normalLabel" to "Normal", "energeticLabel" to "Enerjik",
        "plusToday" to "Bugün de devam", "technicalTip" to "Teknik ipucu",
        "tomorrowLabel" to "Yarın", "setWorkoutTime" to "Antrenman saatini ayarla",
        "daysSinceLastWorkout" to "Son antrenmandan bu yana", "groupsFullyRecovered" to "Gruplar tamamen iyileşti",
        "recoveryOnGroups" to "Grupların iyileşmesi",
        "weeklySummary" to "Haftalık özet", "lastWeekLabel" to "geçen hafta",
        "goalLabel" to "Hedef ipucu", "volumeLabel" to "Hacim", "topExerciseLabel" to "En iyi egzersiz",
        "nutritionLabel" to "Beslenme", "motivationLabel" to "Motivasyon",
        "gpsCardioMap" to "Cardio", "startTracking" to "Takibi başlat", "stopTracking" to "Takibi durdur",
        "pauseTracking" to "Duraklat", "resumeTracking" to "Devam et",
        "distance" to "Mesafe", "pace" to "Tempo", "speed" to "Hız", "duration" to "Süre",
        "savedRoutes" to "Kayıtlı rotalar", "noSavedRoutes" to "Henüz kayıtlı rota yok",
        "routeName" to "Rota adı", "saveRoute" to "Rotayı kaydet", "deleteRoute" to "Rotayı sil",
        "currentLocation" to "Mevcut konum", "trackingActive" to "Takip aktif",
        "locationPermissionRequired" to "Konum izni gerekli",
        "restDaysTitle" to "Dinlenme günleri & Deşarj", "restDaysSubtitle" to "Otomatik iyileşme, esneme, hafif yoga planlama",
        "deloadWeek" to "Deşarj haftası", "recoverySchedule" to "İyileşme programı",
        "stretching" to "Esneme", "lightYoga" to "Hafif yoga", "foamRolling" to "Köpük rulo",
        "restDayRecommendation" to "Dinlenme günü önerisi", "nextRestDay" to "Sonraki dinlenme günü",
        "muscleNeedsRest" to "Kasların dinlenmeye ihtiyacı var", "recoveryComplete" to "İyileşme tamamlandı",
        "deloadInfo" to "Deşarj bilgisi", "suggestedActivities" to "Önerilen aktiviteler",
        "activeRecovery" to "Aktif iyileşme", "lightWalk" to "Hafif yürüyüş",
        "swimming" to "Yüzme", "mobilityWork" to "Hareketlilik çalışması",
        "noRestDays" to "Planlanan dinlenme günü yok", "selectDay" to "Gün seçin",
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
        "startDeload" to "Deşarja başla", "stretchingDescription" to "Esneklik ve hareketliliği artırın",
        "termsAndConditions" to "Kullanım Koşulları", "termsPrefix" to "Devam ederek şunları kabul etmiş olursunuz",
        "timeForDeload" to "Deşarj zamanı", "weeks" to "hafta",
        "weeksSinceLastDeload" to "Son deşarjdan bu yana hafta", "weightKg" to "Ağırlık (kg)",
        "yogaDescription" to "Hafif yoga ile rahatlayın ve hareketliliği artırın", "tapToEdit" to "Düzenlemek için dokunun", "weeklyReminder" to "Haftalık hatırlatıcı"
    ))

    private fun createPt() = Strings(mapOf(
        "appName" to "Kinetic", "dashboard" to "Painel", "overview" to "Visão Geral", "acasa" to "Início", "workouts" to "Treinos", "stats" to "Estatísticas", "waterIntake" to "Consumo de água", "waterGoal" to "Meta de água", "addWater" to "Adicionar água", "dailyWater" to "Água diária", "height" to "Altura", "personalInfo" to "Informações pessoais", "waterAutoCalc" to "Cálculo auto de água", "ml" to "ml", "templates" to "Modelos",
            "waterHistory" to "Histórico de hidratação", "last7Days" to "Últimos 7 dias", "everyDay" to "Todos os dias", "reminder" to "Lembrete",
        "recovery" to "Recuperação", "progress" to "Progresso", "feed" to "Feed", "friends" to "Amigos",
        "leaderboard" to "Leaderboard", "all" to "Todos", "settings" to "Configurações", "language" to "Idioma",
        "units" to "Unidades", "logout" to "Sair", "login" to "Entrar", "signUp" to "Cadastrar-se",
        "email" to "E-mail", "password" to "Senha", "forgotPassword" to "Esqueceu a senha?",
        "orContinueWith" to "Ou continuar com", "loginAsGuest" to "Entrar como convidado",
        "welcomeBack" to "Bem-vindo de volta!", "createAccount" to "Criar Conta",
        "goalStrength" to "Força", "goalMass" to "Massa Muscular", "goalWeightLoss" to "Perda de Peso",
        "goalMaintenance" to "Manutenção", "selectGoal" to "Selecione seu objetivo",
        "next" to "Próximo", "skip" to "Pular", "finish" to "Finalizar", "back" to "Voltar",
        "profileSetup" to "Configurar Perfil", "enterName" to "Digite seu nome",
        "pickPhoto" to "Escolher foto", "saveProfile" to "Salvar Perfil", "chest" to "Peito",
        "shoulders" to "Ombros",             "arms" to "Braços", "biceps" to "Bíceps", "triceps" to "Tríceps",
        "legs" to "Pernas", "thighs" to "Coxas", "glutes" to "Glúteos", "calves" to "Panturrilhas",
        "core" to "Core",
        "cardio" to "Cardio", "sets" to "Séries", "reps" to "Repetições", "weight" to "Peso",
        "addExercise" to "Adicionar Exercício", "saveWorkout" to "Salvar Treino",
        "startWorkout" to "Iniciar Treino", "nextExercise" to "Próximo exercício", "notes" to "Notas", "cancel" to "Cancelar",
        "confirm" to "Confirmar", "delete" to "Excluir", "edit" to "Editar", "search" to "Pesquisar",
        "noDataYet" to "Ainda sem dados", "friendRequests" to "Solicitações de Amizade",
        "sendRequest" to "Enviar Solicitação", "accept" to "Aceitar", "reject" to "Rejeitar",
        "removeFriend" to "Remover Amigo", "noFriends" to "Ainda sem amigos",
        "searchUsers" to "Pesquisar Usuários", "userId" to "ID do Usuário",
        "searchByNameOrId" to "Pesquisar por nome ou ID",
        "incomingRequests" to "Solicitações Recebidas", "noIncomingRequests" to "Sem solicitações recebidas",
        "yourFriends" to "Seus Amigos", "sendFriendRequest" to "Enviar Solicitação de Amizade",
        "friendRequestSent" to "Solicitação Enviada", "byId" to "Por ID",
        "feedEmpty" to "Feed está vazio", "postPlaceholder" to "Escreva algo...",
        "post" to "Publicar", "comments" to "Comentários", "like" to "Curtir", "likes" to "Curtidas",
        "share" to "Compartilhar", "workoutCompleted" to "Treino Concluído!",
        "streakLabel" to "Sequência Atual", "bestStreak" to "Melhor Sequência",
        "badges" to "Distintivos", "noBadges" to "Ainda sem distintivos", "rank" to "Posição",
        "kg" to "kg", "lbs" to "lbs", "kgLbsToggle" to "Alternar kg/lbs",
        "exportCsv" to "Exportar CSV", "importCsv" to "Importar CSV",
        "subscription" to "Assinatura", "premium" to "Premium", "monthlyPlan" to "Plano Mensal",
        "yearlyPlan" to "Plano Anual", "subscribe" to "Assinar", "subscribed" to "Assinante",
        "notSubscribed" to "Não Assinante", "darkMode" to "Modo Escuro",
        "lightMode" to "Modo Claro", "systemDefault" to "Sistema", "about" to "Sobre",
        "version" to "Versão", "totalWorkouts" to "Total de Treinos", "totalWeight" to "Peso Total",
        "personalRecords" to "Recordes Pessoais", "recentWorkouts" to "Treinos Recentes",
        "viewAll" to "Ver Tudo", "loading" to "Carregando...", "error" to "Erro",
        "retry" to "Tentar Novamente", "success" to "Sucesso",
        "friendRequestAccepted" to "Solicitação de amizade aceita",
        "friendRequestRejected" to "Solicitação de amizade rejeitada",
        "profileUpdated" to "Perfil Atualizado", "workoutSaved" to "Treino Salvo",
        "workoutDeleted" to "Treino Excluído", "noExercises" to "Sem exercícios",
        "selectExercises" to "Selecionar Exercícios", "exerciseList" to "Lista de Exercícios",
        "customExercises" to "Exercícios Personalizados",
        "defaultExercises" to "Exercícios Padrão",
        "addCustomExercise" to "Adicionar Exercício Personalizado",
        "enterExerciseName" to "Digite o nome do exercício", "selectGroup" to "Selecionar Grupo",
        "addTemplate" to "Adicionar Modelo", "templateName" to "Nome do Modelo",
        "templateSaved" to "Modelo Salvo", "templateDeleted" to "Modelo Excluído",
        "noTemplates" to "Sem Modelos", "createFirstTemplate" to "Crie seu primeiro modelo",
        "selectTemplate" to "Selecionar Modelo", "useTemplate" to "Usar Modelo",
        "deleteTemplate" to "Excluir Modelo", "recoveryInfo" to "Informações de Recuperação",
        "lastWorkout" to "Último Treino", "daysSince" to "Dias desde",
        "recommendedRecovery" to "Recuperação Recomendada",
        "muscleGroupRecovery" to "Recuperação do Grupo Muscular",
        "readyToTrain" to "Pronto para treinar!", "needsMoreRest" to "Precisa de mais descanso",
        "todayIsRestDay" to "Hoje é dia de descanso", "progressChart" to "Gráfico de Progresso",
        "volumeOverTime" to "Volume ao Longo do Tempo",
        "weightProgression" to "Progressão de Peso", "frequencyChart" to "Gráfico de Frequência",
        "noChartData" to "Sem dados para o gráfico", "calendarView" to "Visão de Calendário",
        "listView" to "Visão de Lista", "sortBy" to "Ordenar Por", "sortByDate" to "Por Data",
        "sortByGroup" to "Por Grupo", "filterByGroup" to "Filtrar por Grupo",
        "allGroups" to "Todos", "welcomeTitle" to "Bem-vindo!",
        "welcomeSubtitle" to "Comece sua jornada fitness", "featureSocial" to "Social",
        "featureGamification" to "Gamificação", "featureCharts" to "Gráficos",
        "featureExport" to "Exportar", "featureTemplates" to "Modelos",
        "featureMultiLang" to "Multi-idioma", "notifications" to "Notificações",
        "enableNotifications" to "Ativar Notificações",
        "notificationPermissionRequired" to "Permissão de notificação necessária",
        "friendRequestNotificationTitle" to "Solicitação de Amizade",
        "friendRequestNotificationText" to "enviou uma solicitação de amizade!",
        "profilePhotoUpdated" to "Foto do perfil atualizada",
        "nameRequired" to "Nome é obrigatório", "settingsSaved" to "Configurações Salvas",
        "darkTheme" to "Tema Escuro", "lightTheme" to "Tema Claro",
        "systemTheme" to "Tema do Sistema", "selectLanguage" to "Selecionar Idioma",
        "english" to "Inglês", "romanian" to "Romeno", "russian" to "Russo",
        "ukrainian" to "Ucraniano", "french" to "Francês", "german" to "Alemão",
        "spanish" to "Espanhol", "italian" to "Italiano", "turkish" to "Turco",
        "portuguese" to "Português", "polish" to "Polonês", "leaderLabel" to "Líder",
        "workoutsLabel" to "Treinos", "totalVolume" to "Volume Total",
        "currentStreakLabel" to "Sequência Atual", "bestStreakLabel" to "Melhor Sequência",
        "badgesEarned" to "Distintivos Conquistados", "days" to "dias", "badge" to "Distintivo", "lastPR" to "Último PR",
        "newExercise" to "Novo exercício", "exerciseNameLabel" to "Nome do exercício", "add" to "Adicionar", "demoExercise" to "EXERCÍCIO DEMO", "setLabel" to "SÉRIE", "prAndVolume" to "Recordes e volume", "start" to "Iniciar", "stop" to "Parar", "noSavedSetsYet" to "Nenhuma série salva ainda.", "editSet" to "Editar série", "chooseTemplate" to "Escolher modelo de treino", "exercises" to "exercícios", "recovered" to "Recuperado", "almostRecovered" to "Quase recuperado", "moderate" to "Moderado", "tired" to "Cansado", "exhausted" to "Exausto", "fatigue" to "fadiga", "chooseMuscleGroup" to "Escolher grupo muscular", "changeExercise" to "Trocar exercício",
        "monthlyProgress" to "Progresso mensal", "completeWorkoutsToSee" to "Complete treinos para ver o progresso", "jan" to "Jan", "feb" to "Fev", "mar" to "Mar", "apr" to "Abr", "may" to "Mai", "jun" to "Jun", "jul" to "Jul", "aug" to "Ago", "sep" to "Set", "oct" to "Out", "nov" to "Nov", "dec" to "Dez", "monthlyDetails" to "Detalhes mensais", "month" to "Mês", "mon" to "Seg", "tue" to "Ter", "wed" to "Qua", "thu" to "Qui", "fri" to "Sex", "sat" to "Sáb", "sun" to "Dom", "noWorkouts" to "Nenhum treino neste dia", "workoutDistribution" to "Distribuição de treinos",
        "notNow" to "Agora não", "subscribeNow" to "Assinar agora", "premiumFeature" to "Recurso Premium", "subscribersOnly" to "\$feature está disponível apenas para assinantes", "choosePlan" to "Escolher plano", "youAreSubscribed" to "Você está assinado!", "muscleRecovery" to "Recuperação muscular", "waterReminder" to "Lembrete de água", "waterReminderTitle" to "Hora de beber água!", "waterReminderText" to "Mantenha-se hidratado! É hora de beber um copo de água.", "waterReminderEnabled" to "Ativado", "waterReminderDisabled" to "Desativado", "selectTime" to "Selecionar hora", "forearms" to "Antebraços", "neckAndTraps" to "Pescoço & Trapézios", "welcome" to "Bem-vindo", "athlete" to "Atleta",
        "biometricTracking" to "Rastreamento Biométrico", "biometricSubtitle" to "Peso, circunferências, gordura corporal", "addMeasurement" to "Adicionar medição", "bodyFat" to "Gordura corporal", "waistCirc" to "Cintura", "hipsCirc" to "Quadril", "thighsCirc" to "Coxas", "chestCirc" to "Peito", "armsCirc" to "Braços", "lastMeasurement" to "Última medição", "noMeasurements" to "Sem medições ainda", "viewCharts" to "Ver gráficos", "saveMeasurement" to "Salvar medição", "measurementSaved" to "Medição salva", "weeksAgo" to "semanas atrás", "cm" to "cm", "percent" to "%", "deleteMeasurement" to "Excluir medição", "biometricHistory" to "Histórico de medições", "weightChart" to "Gráfico de peso", "bodyFatChart" to "Gráfico de gordura", "circumferenceChart" to "Gráfico de circunferências", "date" to "Data", "biometricReminder" to "Lembrete biométrico", "biometricReminderTitle" to "Hora das medições!", "biometricReminderText" to "Não esqueça de registrar suas medições corporais semanais.", "biometricReminderEnabled" to "Ativado", "biometricReminderDisabled" to "Desativado",
        "foodJournal" to "Diário Alimentar", "scanBarcode" to "Escanear Código de Barras", "scanBarcodeHint" to "Coloque o código de barras no quadro para escanear o produto", "cameraPermissionRequired" to "Acesso à câmera é necessário para escanear", "scan" to "Escanear", "scanning" to "Escaneando...", "scanBarcodeHelp" to "Certifique-se de que o Google Play Services está instalado e atualizado", "noFoodEntries" to "Nenhuma entrada de alimentos ainda", "todaysMacros" to "Macros de Hoje", "caloriesLabel" to "Calorias", "proteinLabel" to "Proteínas", "carbsLabel" to "Carbos", "fatLabel" to "Gorduras", "breakfast" to "Café da manhã", "lunch" to "Almoço", "dinner" to "Jantar", "snack" to "Lanche", "drinks" to "Bebidas", "selectMealType" to "Selecionar tipo de refeição", "manualFoodEntry" to "Entrada Manual", "foodName" to "Nome do alimento", "brandLabel" to "Marca", "calories" to "Calorias", "protein" to "Proteínas", "carbs" to "Carboidratos", "fat" to "Gorduras", "fiber" to "Fibras",
        "aiTrainer" to "Treinador IA", "aiTrainerWelcome" to "Olá! Sou seu treinador IA", "aiTrainerHint" to "Pergunte sobre treino, nutrição ou progresso", "aiTrainerHistory" to "Histórico de chats", "noHistoryYet" to "Sem histórico ainda", "current" to "Atual", "askAiTrainer" to "Perguntar ao treinador...", "aiSuggestion1" to "Que treino você recomenda hoje?", "aiSuggestion2" to "Como posso aumentar o volume?", "aiSuggestion3" to "Preciso de um dia de descanso?", "aiSuggestion4" to "Como sair de um platô?",
        "viewProfile" to "Ver Perfil",
        "accountSettings" to "Configurações da Conta", "deleteAccount" to "Excluir Conta",
        "privacyPolicy" to "Política de Privacidade",
        "termsOfService" to "Termos de Serviço", "back_" to "Voltar",
        "restTimer" to "Timer de descanso", "startTimer" to "Iniciar", "customTimer" to "Personalizado",
        "seconds" to "Segundos", "custom" to "Personalizado",
        "exerciseHistory" to "Histórico", "bestSet" to "Melhor série", "lastSets" to "Últimas séries",
        "favorite" to "Favorito", "favorites" to "Favoritos", "usageCount" to "Usado",
        "addSet" to "Adicionar série", "exerciseNotes" to "Notas", "workoutNotes" to "Notas treino",
        "saveNotes" to "Salvar", "editWorkout" to "Editar",
        "volume" to "Volume", "maxWeight" to "Peso máx", "maxReps" to "Reps máx", "maxSet" to "Série máx",
        "today" to "Hoje", "thisWeek" to "Esta semana", "thisMonth" to "Este mês",
        "totalVolumeLabel" to "Volume total",
        "languageChanged" to "Idioma alterado", "themeChanged" to "Tema alterado",
        "guest" to "Convidado", "loginWithGoogle" to "Entrar com Google", "loginWithFacebook" to "Entrar com Facebook",
        "close" to "Fechar", "menu" to "Menu", "profile" to "Perfil",
        "appTagline" to "Treine. Progrida. Repita.", "or" to "ou", "dark" to "Escuro", "light" to "Claro",
        "system" to "Sistema", "languageTitle" to "Idioma", "themeTitle" to "Tema",
        "selectTheme" to "Selecionar tema", "settingsAndMore" to "Configurações e mais",
        "muscleGroups" to "Grupos musculares", "startHere" to "Comece aqui", "back__" to "Voltar",
        "englishUS" to "Inglês", "romana" to "Romeno", "russkiy" to "Russo", "ukrainska" to "Ucraniano",
        "francais" to "Francês", "deutsch" to "Alemão", "espanol" to "Espanhol",
        "italiano" to "Italiano", "turkce" to "Turco", "portugues" to "Português", "polski" to "Polonês",
        "motto1" to "Cada repetição conta.", "motto2" to "Mais forte que ontem.",
        "motto3" to "Seu corpo, suas regras.", "motto4" to "Supere seus limites.",
        "motto5" to "Consistência supera talento.", "motto6" to "Disciplina é liberdade.",
        "motto7" to "Sem atalhos.", "motto8" to "Ganho, não dado.",
        "goodMorning" to "Bom dia", "goodAfternoon" to "Boa tarde", "goodEvening" to "Boa noite",
        "daysConsecutive" to "dias consecutivos", "todaysWorkout" to "Treino de hoje",
        "howDoYouFeel" to "Como se sente?", "tiredLabel" to "Cansado", "normalLabel" to "Normal", "energeticLabel" to "Energético",
        "plusToday" to "Ainda hoje", "technicalTip" to "Dica técnica",
        "tomorrowLabel" to "Amanhã", "setWorkoutTime" to "Definir hora do treino",
        "daysSinceLastWorkout" to "Dias desde o último treino", "groupsFullyRecovered" to "Grupos totalmente recuperados",
        "recoveryOnGroups" to "Recuperação dos grupos",
        "weeklySummary" to "Resumo semanal", "lastWeekLabel" to "semana passada",
        "goalLabel" to "Dica de objetivo", "volumeLabel" to "Volume", "topExerciseLabel" to "Top exercício",
        "nutritionLabel" to "Nutrição", "motivationLabel" to "Motivação",
        "gpsCardioMap" to "Cardio", "startTracking" to "Iniciar rastreamento", "stopTracking" to "Parar rastreamento",
        "pauseTracking" to "Pausar", "resumeTracking" to "Retomar",
        "distance" to "Distância", "pace" to "Ritmo", "speed" to "Velocidade", "duration" to "Duração",
        "savedRoutes" to "Rotas salvas", "noSavedRoutes" to "Nenhuma rota salva",
        "routeName" to "Nome da rota", "saveRoute" to "Salvar rota", "deleteRoute" to "Excluir rota",
        "currentLocation" to "Localização atual", "trackingActive" to "Rastreamento ativo",
        "locationPermissionRequired" to "Permissão de localização necessária",
        "restDaysTitle" to "Dias de descanso e descarga", "restDaysSubtitle" to "Agendamento automático recuperação, alongamento, yoga leve",
        "deloadWeek" to "Semana de descarga", "recoverySchedule" to "Programa de recuperação",
        "stretching" to "Alongamento", "lightYoga" to "Yoga leve", "foamRolling" to "Rolo de espuma",
        "restDayRecommendation" to "Recomendação dia de descanso", "nextRestDay" to "Próximo dia de descanso",
        "muscleNeedsRest" to "Os músculos precisam de descanso", "recoveryComplete" to "Recuperação completa",
        "deloadInfo" to "Info de descarga", "suggestedActivities" to "Atividades sugeridas",
        "activeRecovery" to "Recuperação ativa", "lightWalk" to "Caminhada leve",
        "swimming" to "Natação", "mobilityWork" to "Trabalho de mobilidade",
        "noRestDays" to "Nenhum dia de descanso programado", "selectDay" to "Selecionar dia",
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
        "startDeload" to "Iniciar descarga", "stretchingDescription" to "Melhore a flexibilidade e mobilidade",
        "termsAndConditions" to "Termos e Condições", "termsPrefix" to "Ao continuar, você concorda com nossos",
        "timeForDeload" to "Hora da descarga", "weeks" to "semanas",
        "weeksSinceLastDeload" to "Semanas desde a última descarga", "weightKg" to "Peso (kg)",
        "yogaDescription" to "Relaxe e melhore a mobilidade com yoga leve", "tapToEdit" to "Toque para editar", "weeklyReminder" to "Lembrete semanal"
    ))

    private fun createPl() = Strings(mapOf(
        "appName" to "Kinetic", "dashboard" to "Panel", "overview" to "Przegląd", "acasa" to "Strona Główna", "workouts" to "Treningi", "stats" to "Statystyki", "waterIntake" to "Spożycie wody", "waterGoal" to "Cel wody", "addWater" to "Dodaj wodę", "dailyWater" to "Woda dzienna", "height" to "Wzrost", "personalInfo" to "Informacje osobiste", "waterAutoCalc" to "Auto kalkulator wody", "ml" to "ml", "templates" to "Szablony",
            "waterHistory" to "Historia nawodnienia", "last7Days" to "Ostatnie 7 dni", "everyDay" to "Codziennie", "reminder" to "Przypomnienie",
        "recovery" to "Regeneracja", "progress" to "Postęp", "feed" to "Feed", "friends" to "Znajomi",
        "leaderboard" to "Ranking", "all" to "Wszyscy", "settings" to "Ustawienia", "language" to "Język",
        "units" to "Jednostki", "logout" to "Wyloguj", "login" to "Zaloguj się",
        "signUp" to "Zarejestruj się", "email" to "E-mail", "password" to "Hasło",
        "forgotPassword" to "Zapomniałeś hasła?", "orContinueWith" to "Lub kontynuuj z",
        "loginAsGuest" to "Zaloguj jako gość", "welcomeBack" to "Witaj ponownie!",
        "createAccount" to "Utwórz konto", "goalStrength" to "Siła", "goalMass" to "Masa mięśniowa",
        "goalWeightLoss" to "Redukcja", "goalMaintenance" to "Utrzymanie",
        "selectGoal" to "Wybierz swój cel", "next" to "Dalej", "skip" to "Pomiń", "finish" to "Zakończ",
        "back" to "Wstecz", "profileSetup" to "Ustawienia profilu", "enterName" to "Wprowadź imię",
        "pickPhoto" to "Wybierz zdjęcie", "saveProfile" to "Zapisz profil", "chest" to "Klatka piersiowa",
        "shoulders" to "Barki",             "arms" to "Ramiona", "biceps" to "Bicepsy", "triceps" to "Tricepsy",
        "legs" to "Nogi", "thighs" to "Uda", "glutes" to "Pośladki", "calves" to "Łydki",
        "core" to "Core",
        "cardio" to "Cardio", "sets" to "Serie", "reps" to "Powtórzenia", "weight" to "Ciężar",
        "addExercise" to "Dodaj ćwiczenie", "saveWorkout" to "Zapisz trening",
        "startWorkout" to "Rozpocznij trening", "nextExercise" to "Następne ćwiczenie", "notes" to "Notatki", "cancel" to "Anuluj",
        "confirm" to "Potwierdź", "delete" to "Usuń", "edit" to "Edytuj", "search" to "Szukaj",
        "noDataYet" to "Brak danych", "friendRequests" to "Zaproszenia do znajomych",
        "sendRequest" to "Wyślij zaproszenie", "accept" to "Akceptuj", "reject" to "Odrzuć",
        "removeFriend" to "Usuń znajomego", "noFriends" to "Brak znajomych",
        "searchUsers" to "Szukaj użytkowników", "userId" to "ID użytkownika",
        "searchByNameOrId" to "Szukaj po nazwie lub ID",
        "incomingRequests" to "Otrzymane zaproszenia",
        "noIncomingRequests" to "Brak otrzymanych zaproszeń", "yourFriends" to "Twoi znajomi",
        "sendFriendRequest" to "Wyślij zaproszenie do znajomych",
        "friendRequestSent" to "Zaproszenie wysłane", "byId" to "Po ID",
        "feedEmpty" to "Feed jest pusty", "postPlaceholder" to "Napisz coś...",
        "post" to "Opublikuj", "comments" to "Komentarze", "like" to "Lubię to", "likes" to "Polubienia",
        "share" to "Udostępnij", "workoutCompleted" to "Trening ukończony!",
        "streakLabel" to "Aktualna seria", "bestStreak" to "Najlepsza seria", "badges" to "Odznaki",
        "noBadges" to "Brak odznak", "rank" to "Ranga", "kg" to "kg", "lbs" to "lbs",
        "kgLbsToggle" to "Przełącz kg/lbs", "exportCsv" to "Eksportuj CSV",
        "importCsv" to "Importuj CSV", "subscription" to "Subskrypcja", "premium" to "Premium",
        "monthlyPlan" to "Plan miesięczny", "yearlyPlan" to "Plan roczny",
        "subscribe" to "Subskrybuj", "subscribed" to "Subskrybent",
        "notSubscribed" to "Bez subskrypcji", "darkMode" to "Tryb ciemny",
        "lightMode" to "Tryb jasny", "systemDefault" to "System", "about" to "O aplikacji",
        "version" to "Wersja", "totalWorkouts" to "Łączna liczba treningów",
        "totalWeight" to "Łączny ciężar", "personalRecords" to "Rekordy osobiste",
        "recentWorkouts" to "Ostatnie treningi", "viewAll" to "Zobacz wszystko",
        "loading" to "Ładowanie...", "error" to "Błąd", "retry" to "Ponów",
        "success" to "Sukces", "friendRequestAccepted" to "Zaproszenie zaakceptowane",
        "friendRequestRejected" to "Zaproszenie odrzucone",
        "profileUpdated" to "Profil zaktualizowany", "workoutSaved" to "Trening zapisany",
        "workoutDeleted" to "Trening usunięty", "noExercises" to "Brak ćwiczeń",
        "selectExercises" to "Wybierz ćwiczenia", "exerciseList" to "Lista ćwiczeń",
        "customExercises" to "Ćwiczenia niestandardowe",
        "defaultExercises" to "Ćwiczenia domyślne",
        "addCustomExercise" to "Dodaj ćwiczenie niestandardowe",
        "enterExerciseName" to "Wprowadź nazwę ćwiczenia", "selectGroup" to "Wybierz grupę",
        "addTemplate" to "Dodaj szablon", "templateName" to "Nazwa szablonu",
        "templateSaved" to "Szablon zapisany", "templateDeleted" to "Szablon usunięty",
        "noTemplates" to "Brak szablonów", "createFirstTemplate" to "Utwórz swój pierwszy szablon",
        "selectTemplate" to "Wybierz szablon", "useTemplate" to "Użyj szablonu",
        "deleteTemplate" to "Usuń szablon", "recoveryInfo" to "Informacje o regeneracji",
        "lastWorkout" to "Ostatni trening", "daysSince" to "Dni od",
        "recommendedRecovery" to "Zalecana regeneracja",
        "muscleGroupRecovery" to "Regeneracja grup mięśniowych",
        "readyToTrain" to "Gotowy do treningu!", "needsMoreRest" to "Potrzebuje więcej odpoczynku",
        "todayIsRestDay" to "Dziś jest dzień odpoczynku", "progressChart" to "Wykres postępu",
        "volumeOverTime" to "Objętość w czasie", "weightProgression" to "Progresja ciężaru",
        "frequencyChart" to "Wykres częstości", "noChartData" to "Brak danych wykresu",
        "calendarView" to "Widok kalendarza", "listView" to "Widok listy",
        "sortBy" to "Sortuj wg", "sortByDate" to "Po dacie", "sortByGroup" to "Po grupie",
        "filterByGroup" to "Filtruj po grupie", "allGroups" to "Wszystkie",
        "welcomeTitle" to "Witaj!", "welcomeSubtitle" to "Rozpocznij swoją drogę fitness",
        "featureSocial" to "Społecznościowy", "featureGamification" to "Grywalizacja",
        "featureCharts" to "Wykresy", "featureExport" to "Eksport",
        "featureTemplates" to "Szablony", "featureMultiLang" to "Wielojęzyczny",
        "notifications" to "Powiadomienia", "enableNotifications" to "Włącz powiadomienia",
        "notificationPermissionRequired" to "Wymagane uprawnienie do powiadomień",
        "friendRequestNotificationTitle" to "Zaproszenie do znajomych",
        "friendRequestNotificationText" to "wysłał(a) Ci zaproszenie do znajomych!",
        "profilePhotoUpdated" to "Zdjęcie profilowe zaktualizowane",
        "nameRequired" to "Imię jest wymagane", "settingsSaved" to "Ustawienia zapisane",
        "darkTheme" to "Ciemny motyw", "lightTheme" to "Jasny motyw",
        "systemTheme" to "Motyw systemowy", "selectLanguage" to "Wybierz język",
        "english" to "Angielski", "romanian" to "Rumuński", "russian" to "Rosyjski",
        "ukrainian" to "Ukraiński", "french" to "Francuski", "german" to "Niemiecki",
        "spanish" to "Hiszpański", "italian" to "Włoski", "turkish" to "Turecki",
        "portuguese" to "Portugalski", "polish" to "Polski", "leaderLabel" to "Lider",
        "workoutsLabel" to "Treningi", "totalVolume" to "Łączna objętość",
        "currentStreakLabel" to "Aktualna seria", "bestStreakLabel" to "Najlepsza seria",
        "badgesEarned" to "Zdobyte odznaki", "days" to "dni", "badge" to "Odznaka", "lastPR" to "Ostatni PR",
        "newExercise" to "Nowe ćwiczenie", "exerciseNameLabel" to "Nazwa ćwiczenia", "add" to "Dodaj", "demoExercise" to "ĆWICZENIE DEMO", "setLabel" to "SERIA", "prAndVolume" to "Rekordy i objętość", "start" to "Start", "stop" to "Stop", "noSavedSetsYet" to "Brak zapisanych serii.", "editSet" to "Edytuj serię", "chooseTemplate" to "Wybierz szablon treningu", "exercises" to "ćwiczeń", "recovered" to "Wypoczęty", "almostRecovered" to "Prawie wypoczęty", "moderate" to "Umiarkowany", "tired" to "Zmęczony", "exhausted" to "Wyczerpany", "fatigue" to "zmęczenie", "chooseMuscleGroup" to "Wybierz grupę mięśniową", "changeExercise" to "Zmień ćwiczenie",
        "monthlyProgress" to "Postęp miesięczny", "completeWorkoutsToSee" to "Ukończ treningi aby zobaczyć postępy", "jan" to "Sty", "feb" to "Lut", "mar" to "Mar", "apr" to "Kwi", "may" to "Maj", "jun" to "Cze", "jul" to "Lip", "aug" to "Sie", "sep" to "Wrz", "oct" to "Paź", "nov" to "Lis", "dec" to "Gru", "monthlyDetails" to "Szczegóły miesięczne", "month" to "Miesiąc", "mon" to "Pon", "tue" to "Wt", "wed" to "Śr", "thu" to "Czw", "fri" to "Pt", "sat" to "Sob", "sun" to "Nd", "noWorkouts" to "Brak treningów w tym dniu", "workoutDistribution" to "Rozkład treningów",
        "notNow" to "Nie teraz", "subscribeNow" to "Subskrybuj teraz", "premiumFeature" to "Funkcja Premium", "subscribersOnly" to "\$feature jest dostępne tylko dla subskrybentów", "choosePlan" to "Wybierz plan", "youAreSubscribed" to "Jesteś subskrybentem!", "muscleRecovery" to "Regeneracja mięśni", "waterReminder" to "Przypomnienie o wodzie", "waterReminderTitle" to "Czas pić wodę!", "waterReminderText" to "Pij wodę! Czas napić się szklanki wody.", "waterReminderEnabled" to "Włączone", "waterReminderDisabled" to "Wyłączone", "selectTime" to "Wybierz godzinę", "forearms" to "Przedramiona", "neckAndTraps" to "Szyja & Czworoboczny", "welcome" to "Witaj", "athlete" to "Sportowiec",
        "biometricTracking" to "Monitorowanie biometryczne", "biometricSubtitle" to "Waga, obwody, tkanka tłuszczowa", "addMeasurement" to "Dodaj pomiary", "bodyFat" to "Tkanka tłuszczowa", "waistCirc" to "Talia", "hipsCirc" to "Biodra", "thighsCirc" to "Uda", "chestCirc" to "Klatka piersiowa", "armsCirc" to "Ramiona", "lastMeasurement" to "Ostatni pomiar", "noMeasurements" to "Brak pomiarów", "viewCharts" to "Zobacz wykresy", "saveMeasurement" to "Zapisz pomiary", "measurementSaved" to "Pomiary zapisane", "weeksAgo" to "tygodnie temu", "cm" to "cm", "percent" to "%", "deleteMeasurement" to "Usuń pomiary", "biometricHistory" to "Historia pomiarów", "weightChart" to "Wykres wagi", "bodyFatChart" to "Wykres tłuszczu", "circumferenceChart" to "Wykres obwodów", "date" to "Data", "biometricReminder" to "Przypomnienie biometryczne", "biometricReminderTitle" to "Czas na pomiary!", "biometricReminderText" to "Nie zapomnij zapisać tygodniowych pomiarów ciała.", "biometricReminderEnabled" to "Włączone", "biometricReminderDisabled" to "Wyłączone",
        "foodJournal" to "Dziennik żywieniowy", "scanBarcode" to "Skanuj kod kreskowy", "scanBarcodeHint" to "Umieść kod kreskowy w kadrze, aby zeskanować produkt", "cameraPermissionRequired" to "Dostęp do kamery jest wymagany do skanowania", "scan" to "Skanuj", "scanning" to "Skanowanie...", "scanBarcodeHelp" to "Upewnij się, że Google Play Services jest zainstalowany i zaktualizowany", "noFoodEntries" to "Brak wpisów żywieniowych", "todaysMacros" to "Makro na dziś", "caloriesLabel" to "Kalorie", "proteinLabel" to "Białko", "carbsLabel" to "Węgle", "fatLabel" to "Tłuszcze", "breakfast" to "Śniadanie", "lunch" to "Obiad", "dinner" to "Kolacja", "snack" to "Przekąska", "drinks" to "Napoje", "selectMealType" to "Wybierz typ posiłku", "manualFoodEntry" to "Wpis ręczny", "foodName" to "Nazwa produktu", "brandLabel" to "Marka", "calories" to "Kalorie", "protein" to "Białko", "carbs" to "Węglowodany", "fat" to "Tłuszcze", "fiber" to "Błonnik",
        "aiTrainer" to "Trener AI", "aiTrainerWelcome" to "Cześć! Jestem twoim trenerem AI", "aiTrainerHint" to "Zapytaj o trening, dietę lub postępy", "aiTrainerHistory" to "Historia czatów", "noHistoryYet" to "Brak historii", "current" to "Aktualny", "askAiTrainer" to "Zapytaj trenera...", "aiSuggestion1" to "Jaki trening polecasz dzisiaj?", "aiSuggestion2" to "Jak zwiększyć objętość?", "aiSuggestion3" to "Czy potrzebuję dnia odpoczynku?", "aiSuggestion4" to "Jak wyjść z plateau?",
        "viewProfile" to "Zobacz profil",
        "accountSettings" to "Ustawienia konta", "deleteAccount" to "Usuń konto",
        "privacyPolicy" to "Polityka prywatności",
        "termsOfService" to "Warunki usługi", "back_" to "Wstecz",
        "restTimer" to "Timer odpoczynku", "startTimer" to "Start", "customTimer" to "Własny timer",
        "seconds" to "Sekundy", "custom" to "Własny",
        "exerciseHistory" to "Historia", "bestSet" to "Najlepsza seria", "lastSets" to "Ostatnie serie",
        "favorite" to "Ulubione", "favorites" to "Ulubione", "usageCount" to "Użyto",
        "addSet" to "Dodaj serię", "exerciseNotes" to "Notatki", "workoutNotes" to "Notatki treningu",
        "saveNotes" to "Zapisz", "editWorkout" to "Edytuj",
        "volume" to "Objętość", "maxWeight" to "Maks waga", "maxReps" to "Maks powtórzenia", "maxSet" to "Maks seria",
        "today" to "Dziś", "thisWeek" to "Ten tydzień", "thisMonth" to "Ten miesiąc",
        "totalVolumeLabel" to "Łączna objętość",
        "languageChanged" to "Język zmieniony", "themeChanged" to "Motyw zmieniony",
        "guest" to "Gość", "loginWithGoogle" to "Zaloguj z Google", "loginWithFacebook" to "Zaloguj z Facebook",
        "close" to "Zamknij", "menu" to "Menu", "profile" to "Profil",
        "appTagline" to "Trenuj. Postępuj. Powtarzaj.", "or" to "lub", "dark" to "Ciemny", "light" to "Jasny",
        "system" to "System", "languageTitle" to "Język", "themeTitle" to "Motyw",
        "selectTheme" to "Wybierz motyw", "settingsAndMore" to "Ustawienia i więcej",
        "muscleGroups" to "Grupy mięśniowe", "startHere" to "Zacznij tutaj", "back__" to "Wstecz",
        "englishUS" to "Angielski", "romana" to "Rumuński", "russkiy" to "Rosyjski", "ukrainska" to "Ukraiński",
        "francais" to "Francuski", "deutsch" to "Niemiecki", "espanol" to "Hiszpański",
        "italiano" to "Włoski", "turkce" to "Turecki", "portugues" to "Portugalski", "polski" to "Polski",
        "motto1" to "Każde powtórzenie się liczy.", "motto2" to "Silniejszy niż wczoraj.",
        "motto3" to "Twoje ciało, twoje zasady.", "motto4" to "Przekrocz swoje granice.",
        "motto5" to "Wytrwałość pokonuje talent.", "motto6" to "Dyscyplina to wolność.",
        "motto7" to "Bez skrótów.", "motto8" to "Zarobione, nie dane.",
        "goodMorning" to "Dzień dobry", "goodAfternoon" to "Dzień dobry", "goodEvening" to "Dobry wieczór",
        "daysConsecutive" to "dni z rzędu", "todaysWorkout" to "Dzisiejszy trening",
        "howDoYouFeel" to "Jak się czujesz?", "tiredLabel" to "Zmęczony", "normalLabel" to "Normalnie", "energeticLabel" to "Energetyczny",
        "plusToday" to "Jeszcze dzisiaj", "technicalTip" to "Wskazówka techniczna",
        "tomorrowLabel" to "Jutro", "setWorkoutTime" to "Ustaw czas treningu",
        "daysSinceLastWorkout" to "Dni od ostatniego treningu", "groupsFullyRecovered" to "Grupy w pełni odzyskane",
        "recoveryOnGroups" to "Regeneracja grup",
        "weeklySummary" to "Podsumowanie tygodnia", "lastWeekLabel" to "poprz. tydzień",
        "goalLabel" to "Wskazówka celu", "volumeLabel" to "Objętość", "topExerciseLabel" to "Top ćwiczenie",
        "nutritionLabel" to "Odżywianie", "motivationLabel" to "Motywacja",
        "gpsCardioMap" to "Cardio", "startTracking" to "Rozpocznij śledzenie", "stopTracking" to "Zatrzymaj śledzenie",
        "pauseTracking" to "Pauza", "resumeTracking" to "Wznów",
        "distance" to "Dystans", "pace" to "Tempo", "speed" to "Prędkość", "duration" to "Czas trwania",
        "savedRoutes" to "Zapisane trasy", "noSavedRoutes" to "Brak zapisanych tras",
        "routeName" to "Nazwa trasy", "saveRoute" to "Zapisz trasę", "deleteRoute" to "Usuń trasę",
        "currentLocation" to "Bieżąca lokalizacja", "trackingActive" to "Śledzenie aktywne",
        "locationPermissionRequired" to "Wymagana zgoda na lokalizację",
        "restDaysTitle" to "Dni odpoczynku i deload", "restDaysSubtitle" to "Automatyczne planowanie regeneracji, rozciągania, jogi",
        "deloadWeek" to "Tydzień deload", "recoverySchedule" to "Harmonogram regeneracji",
        "stretching" to "Rozciąganie", "lightYoga" to "Łagodna joga", "foamRolling" to "Rolowanie",
        "restDayRecommendation" to "Rekomendacja dnia odpoczynku", "nextRestDay" to "Następny dzień odpoczynku",
        "muscleNeedsRest" to "Mięśnie potrzebują odpoczynku", "recoveryComplete" to "Regeneracja zakończona",
        "deloadInfo" to "Informacje o deload", "suggestedActivities" to "Sugerowane aktywności",
        "activeRecovery" to "Aktywna regeneracja", "lightWalk" to "Lekki spacer",
        "swimming" to "Pływanie", "mobilityWork" to "Ćwiczenia mobilności",
        "noRestDays" to "Brak zaplanowanych dni odpoczynku", "selectDay" to "Wybierz dzień",
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
        "startDeload" to "Rozpocznij deload", "stretchingDescription" to "Popraw elastyczność i mobilność",
        "termsAndConditions" to "Warunki Korzystania", "termsPrefix" to "Kontynuując, zgadzasz się z naszymi",
        "timeForDeload" to "Czas na deload", "weeks" to "tygodni",
        "weeksSinceLastDeload" to "Tygodni od ostatniego deloadu", "weightKg" to "Waga (kg)",
        "yogaDescription" to "Zrelaksuj się i popraw mobilność dzięki lekkiej jodze", "tapToEdit" to "Dotknij, aby edytować", "weeklyReminder" to "Przypomnienie tygodniowe"
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
            "Piept" -> strings.chest
            "Spate" -> strings.back
            "Umeri" -> strings.shoulders
            "Biceps" -> strings.biceps
            "Triceps" -> strings.triceps
            "Abdomen" -> strings.core
            "Picioare" -> strings.thighs
            "Fese" -> strings.glutes
            "Gambe" -> strings.calves
            "Cardio" -> strings.cardio
            "Antebrate" -> strings.forearms
            "Gat & Trapezi" -> strings.neckAndTraps
            else -> group
        }
    }
}
