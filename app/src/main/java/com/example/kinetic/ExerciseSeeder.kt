package com.example.kinetic

class ExerciseSeeder(private val db: AppDatabase) {
    suspend fun seedIfEmpty() {
        if (db.exerciseDefinitionDao().count() == 0) {
            val exercises = DataProvider.defaultExercises.map {
                ExerciseDefinitionEntity(name = it.name, group = it.group, equipment = it.equipment, isDefault = true)
            }
            db.exerciseDefinitionDao().upsertAll(exercises)
        }
    }

    suspend fun forceSeed() {
        val exercises = DataProvider.defaultExercises.map {
            ExerciseDefinitionEntity(name = it.name, group = it.group, equipment = it.equipment, isDefault = true)
        }
        db.exerciseDefinitionDao().upsertAll(exercises)
    }
}
