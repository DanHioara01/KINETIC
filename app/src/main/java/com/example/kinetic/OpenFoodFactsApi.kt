package com.example.kinetic

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class FoodProduct(
    val barcode: String = "",
    val name: String = "",
    val brand: String = "",
    val servingSize: Double = 100.0,
    val servingUnit: String = "g",
    val calories: Double = 0.0,
    val proteinG: Double = 0.0,
    val carbsG: Double = 0.0,
    val fatG: Double = 0.0,
    val fiberG: Double = 0.0,
    val found: Boolean = false
)

object OpenFoodFactsApi {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun getProduct(barcode: String): FoodProduct = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://world.openfoodfacts.org/api/v2/product/$barcode.json")
                .addHeader("User-Agent", "Kinetic/1.0 (gym app)")
                .build()

            val response = client.newCall(request).execute()
            android.util.Log.d("OpenFoodFacts", "barcode=$barcode, responseCode=${response.code}")
            val body = response.body?.string() ?: return@withContext FoodProduct(barcode = barcode)
            val json = JSONObject(body)

            if (json.optInt("status", 0) != 1) {
                return@withContext FoodProduct(barcode = barcode)
            }

            val product = json.getJSONObject("product")

            // Încearcă mai multe variante de nume pentru a găsi produsul chiar dacă folosește câmpuri diferite
            val name = product.optString("product_name", "")
                .ifBlank { product.optString("product_name_en", "") }
                .ifBlank { product.optString("generic_name", "") }
                .ifBlank { product.optString("generic_name_en", "") }
            val brand = product.optString("brands", "")
                .ifBlank { product.optString("brand_owner", "") }
                .ifBlank { product.optString("manufacturing_places", "") }

            val nutriments = product.optJSONObject("nutriments") ?: JSONObject()

            // Preia caloriile din mai multe câmpuri posibile (diferite formate OpenFoodFacts)
            val calories = nutriments.optDouble("energy-kcal_100g", 0.0)
                .let { if (it > 0) it else nutriments.optDouble("energy-kcal_per_100g", 0.0) }
                .let { if (it > 0) it else nutriments.optDouble("energy_100g", 0.0) / 4.184 } // convertește kJ în kcal dacă e nevoie
            val protein = nutriments.optDouble("proteins_100g", 0.0)
                .let { if (it > 0) it else nutriments.optDouble("proteins_value", 0.0) }
            val carbs = nutriments.optDouble("carbohydrates_100g", 0.0)
                .let { if (it > 0) it else nutriments.optDouble("carbohydrates_value", 0.0) }
            val fat = nutriments.optDouble("fat_100g", 0.0)
                .let { if (it > 0) it else nutriments.optDouble("fat_value", 0.0) }
            val fiber = nutriments.optDouble("fiber_100g", 0.0)
                .let { if (it > 0) it else nutriments.optDouble("fiber_value", 0.0) }

            val servingSizeStr = product.optString("serving_size", "100g")
            val servingParsed = parseServingSize(servingSizeStr)

            // Chiar dacă nu avem nume, verificăm dacă avem macronutrienți și setăm un nume implicit dacă nu găsim
            val finalName = if (name.isNotBlank()) name else "Produs (cod $barcode)"
            val found = name.isNotBlank() || calories > 0 || protein > 0 || carbs > 0 || fat > 0
            
            FoodProduct(
                barcode = barcode,
                name = finalName,
                brand = brand,
                servingSize = servingParsed.first,
                servingUnit = servingParsed.second,
                calories = calories,
                proteinG = protein,
                carbsG = carbs,
                fatG = fat,
                fiberG = fiber,
                found = found
            )
        } catch (e: Exception) {
            e.printStackTrace()
            FoodProduct(barcode = barcode)
        }
    }

    private fun parseServingSize(servingSize: String): Pair<Double, String> {
        val regex = Regex("""([\d.,]+)\s*(g|ml|kg|l|oz|cup|piece|serving)s?""", RegexOption.IGNORE_CASE)
        val match = regex.find(servingSize)
        return if (match != null) {
            val amount = match.groupValues[1].replace(",", ".").toDoubleOrNull() ?: 100.0
            val unit = match.groupValues[2].lowercase()
            Pair(amount, if (unit == "ml" || unit == "l") "ml" else "g")
        } else {
            Pair(100.0, "g")
        }
    }
}