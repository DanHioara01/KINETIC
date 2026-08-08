package com.example.kinetic

data class NutritionTargets(
    val calories: Int,
    val proteinG: Float,
    val carbsG: Float,
    val fatG: Float
)

object NutritionCalculator {

    fun calculateBmr(weightKg: Float, heightCm: Float, age: Int, gender: String): Float {
        return if (gender == "male") {
            10f * weightKg + 6.25f * heightCm - 5f * age - 5f
        } else {
            10f * weightKg + 6.25f * heightCm - 5f * age - 161f
        }
    }

    fun activityFactor(level: String): Float {
        return when (level) {
            "sedentary" -> 1.2f
            "active" -> 1.55f
            "very_active" -> 1.725f
            else -> 1.2f
        }
    }

    fun calculateTdee(bmr: Float, activityLevel: String): Float {
        return bmr * activityFactor(activityLevel)
    }

    fun adjustForGoal(tdee: Float, goal: String): Float {
        return when (goal) {
            "weight_loss" -> tdee - 400f
            "mass" -> tdee + 300f
            "strength" -> tdee + 150f
            else -> tdee
        }
    }

    fun calculateTargets(calories: Float, weightKg: Float, goal: String): NutritionTargets {
        val proteinPerKg = when (goal) {
            "weight_loss" -> 2.0f
            "strength" -> 2.0f
            "mass" -> 1.8f
            else -> 1.8f
        }
        val fatPerKg = 1.0f
        val proteinG = weightKg * proteinPerKg
        val fatG = weightKg * fatPerKg
        val remainingCalories = calories - (proteinG * 4f) - (fatG * 9f)
        val carbsG = (remainingCalories / 4f).coerceAtLeast(0f)

        return NutritionTargets(
            calories = calories.toInt(),
            proteinG = proteinG,
            carbsG = carbsG,
            fatG = fatG
        )
    }

    fun getTargets(preferencesManager: PreferencesManager): NutritionTargets {
        val weight = preferencesManager.getUserWeight()
        val height = preferencesManager.getUserHeight()
        val age = preferencesManager.getUserAge()
        val gender = preferencesManager.getUserGender()
        val activityLevel = preferencesManager.getActivityLevel()
        val goal = preferencesManager.getFitnessGoal()

        if (gender.isBlank() || age <= 0) {
            return NutritionTargets(calories = 0, proteinG = 0f, carbsG = 0f, fatG = 0f)
        }

        val bmr = calculateBmr(weight, height, age, gender)
        val tdee = calculateTdee(bmr, activityLevel)
        val adjusted = adjustForGoal(tdee, goal)
        return calculateTargets(adjusted, weight, goal)
    }
}
