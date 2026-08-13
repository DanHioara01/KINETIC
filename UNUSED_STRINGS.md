# Audit LanguageManager.Strings - proprietati nefolosite

- **Total proprietati:** 753
- **Folosite in cod:** 531
- **Nefolosite (candidate pentru stergere):** 223
- **Chei de map unice de sters (in toate cele 11 limbi):** 220

## Atentie - proprietati care IMPART cheia de map cu una folosita

Aceste proprietati pot fi sterse, dar cheia de map ramane (e folosita de alta proprietate):

- `bestStreakLabel_`  ->  cheia `"bestStreakLabel"` este folosita si de `bestStreakLabel`
- `targetGoal`  ->  cheia `"target"` este folosita si de `target`
- `workoutsLabel_`  ->  cheia `"workoutsLabel"` este folosita si de `workoutsLabel`

## Lista completa a proprietatilor nefolosite

```
about
accountSettings
achievedLabel
addCustomExercise
addExercise
addTemplate
analyticsNote
appName
back_
back__
badge
badgesEarned
bestSet
bestStreakLabel_
biceps
biometricReminder
biometricReminderDisabled
biometricReminderEnabled
biometricSubtitle
byId
calories
calves
cameraPermissionRequired
carbs
changeExercise
chooseTemplate
comments
completeProfileForTargets
createAccount
createFirstTemplate
currentLocation
custom
customExercises
customTimer
dailyWater
darkMode
darkTheme
date
daysSince
daysSinceLastWorkout
defaultExercises
deleteTemplate
editWorkout
enableNotifications
english
enterExerciseName
equipBand
equipBarbell
equipBodyweight
equipCable
equipDumbbells
equipEZBar
equipKettlebell
equipMachine
equipSledMachine
equipSmithMachine
equipStabilityBall
exerciseList
exerciseNameLabel
fat
favorites
featureCharts
featureExport
featureGamification
featureMultiLang
featureSocial
featureTemplates
feed
filterByGroup
forearms
freePlanDesc
french
frequencyChart
friendRequestAccepted
friendRequestRejected
friendRequests
german
goal
groupsFullyRecovered
italian
kgLbsToggle
languageChanged
languageTitle
last7Days
lastMeasurement
lastPR
lastSets
lastWorkout
leaderLabel
lightMode
lightTheme
like
likes
listView
listening
loading
manualFoodEntry
measurementSaved
mobilityWork
monthlyPlan
muscleGroupRecovery
nameRequired
navyMethodDescription
neckAndTraps
need8WeeksData
needsMoreRest
newExercise
newPbsLastMonth
noBadges
noChartData
noDataShort
noExercises
noIncomingRequests
noPersonalBests
noRestDays
noTemplates
notNow
notSubscribed
notificationPermissionRequired
notifications
oneRmCalculator
orContinueWith
overallRisk
overview
personalRecords
plateCalculatorTitle
plusToday
polish
portuguese
post
postPlaceholder
previousPR
privacyPolicy
proPlan
profilePhotoUpdated
profileUpdated
progress
progressTab
protein
rank
readyToTrain
recentWorkouts
recoveryComplete
recoveryOnGroups
remainingCarbs
remainingFat
remainingProtein
restDayRecommendation
restTimer
romanian
russian
saveMeasurement
saveWorkout
scanBarcode
scanBarcodeHint
seconds
selectDay
selectExercises
selectGroup
selectTemplate
sendFriendRequest
setLabel
setNewGoal
setWorkoutTime
settings
settingsSaved
share
sortBy
sortByDate
sortByGroup
spanish
startHere
startTimer
stop
stopTracking
subscribed
success
swimming
systemDefault
systemTheme
tapToEdit
targetGoal
templateDeleted
templateName
templateSaved
termsOfService
themeChanged
themeTitle
thighs
todayIsRestDay
tomorrowLabel
totalVolume
totalWeight
totalWorkouts
trackingActive
triceps
turkish
ukrainian
unlocked
usageCount
useTemplate
userId
version
viewAll
viewCharts
viewProfile
volumeOverTime
waterAutoCalc
waterHistory
waterReminder
waterReminderDisabled
waterReminderEnabled
weeklyReminder
welcomeBack
welcomeSubtitle
welcomeTitle
workoutAnalyticsTitle
workoutDeleted
workoutDistribution
workoutNotes
workoutSaved
workoutsLabel_
yearlyPlan
```

## Numar de intrari de sters per limba

Fiecare proprietate nefolosita are o intrare `"key" to ...` in fiecare din cele 11 limbaje (createRo, createEn, createRu, createUk, createFr, createDe, createEs, createIt, createTr, createPt, createPl). In total ~220 chei unice x 11 limbi = ~2420 intrari de map de sters.

## Verificari facute

- Scanate toate cele ~109 fisiere Kotlin din app/src (inclusiv teste)
- Pattern-uri de acces detectate: `strings.X` si `LanguageManager.getStrings(...).X`
- Verificat ca nu exista alias-uri (`val s = strings`, `with(strings)`, CompositionLocal, acces prin index)
- Toate cele 51 de aparitii brute ale numelor in alte contexte sunt false-positive (coloane DB, endpoint-uri API, variabile locale, comentarii)
