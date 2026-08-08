package com.example.kinetic

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kinetic.ui.components.AppGlassCard
import com.example.kinetic.ui.components.KineticAppBar
import com.example.kinetic.ui.theme.AppPalette
import com.example.kinetic.ui.theme.appPalette
import com.example.kinetic.ui.theme.*
import java.util.Locale

/** Modul de introducere: caută în baza de date sau introduce manual. */
private enum class EntryMode { SEARCH, MANUAL }

/** Valori calculate pentru cantitatea introdusă. */
private data class CalculatedMacros(
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodScreen(
    isDark: Boolean,
    strings: LanguageManager.Strings,
    prefilledProduct: FoodProduct?,
    onSave: (
        name: String,
        brand: String,
        mealType: String,
        servingSize: Double,
        calories: Double,
        protein: Double,
        carbs: Double,
        fat: Double,
        fiber: Double,
        servingUnit: String
    ) -> Unit,
    onBack: () -> Unit
) {
    val p = appPalette(isDark)

    val foodViewModel: FoodSearchViewModel = viewModel()
    val suggestions by foodViewModel.suggestions.collectAsState()
    val currentLang = LanguageManager.getLanguage()

    var mode by remember {
        mutableStateOf(if (prefilledProduct != null) EntryMode.MANUAL else EntryMode.SEARCH)
    }

    // ── Căutare bază de date ──────────────────────────────
    var searchQuery by remember { mutableStateOf("") }
    var selectedFood by remember { mutableStateOf<FoodItemEntity?>(null) }
    var quantity by remember { mutableStateOf("") }

    // ── Câmpuri manuale (fallback) ────────────────────────
    var name by remember { mutableStateOf(prefilledProduct?.name ?: "") }
    var brand by remember { mutableStateOf(prefilledProduct?.brand ?: "") }
    var servingUnit by remember { mutableStateOf(prefilledProduct?.servingUnit ?: "g") }
    var mealType by remember { mutableStateOf("lunch") }
    var servingSize by remember { mutableStateOf(prefilledProduct?.servingSize?.let { if (it == it.toLong().toDouble()) it.toLong().toString() else String.format(Locale.US, "%.1f", it) } ?: "100") }
    var calories by remember { mutableStateOf(prefilledProduct?.calories?.let { String.format(Locale.US, "%.1f", it) } ?: "") }
    var protein by remember { mutableStateOf(prefilledProduct?.proteinG?.let { String.format(Locale.US, "%.1f", it) } ?: "") }
    var carbs by remember { mutableStateOf(prefilledProduct?.carbsG?.let { String.format(Locale.US, "%.1f", it) } ?: "") }
    var fat by remember { mutableStateOf(prefilledProduct?.fatG?.let { String.format(Locale.US, "%.1f", it) } ?: "") }
    var fiber by remember { mutableStateOf(prefilledProduct?.fiberG?.let { String.format(Locale.US, "%.1f", it) } ?: "") }

    var mealDropdownExpanded by remember { mutableStateOf(false) }
    val mealTypes = listOf("breakfast" to strings.breakfast, "lunch" to strings.lunch, "dinner" to strings.dinner, "snack" to strings.snack, "drinks" to strings.drinks)

    // ── Calcul live pentru alimentul selectat din DB ──────
    val calculated: CalculatedMacros? = remember(selectedFood, quantity) {
        val food = selectedFood ?: return@remember null
        val qty = quantity.toDoubleOrNull() ?: return@remember null
        if (qty <= 0) return@remember null
        if (food.unitType == FoodUnitType.GRAM) {
            CalculatedMacros(
                calories = food.caloriesPer100g * qty / 100.0,
                protein = food.proteinPer100g * qty / 100.0,
                carbs = food.carbsPer100g * qty / 100.0,
                fat = food.fatPer100g * qty / 100.0
            )
        } else {
            // PIECE: valorile sunt per 100g; o bucată cântărește gramsPerPiece grame
            val perPieceCal = food.caloriesPer100g * (food.gramsPerPiece ?: 1.0) / 100.0
            val perPieceProt = food.proteinPer100g * (food.gramsPerPiece ?: 1.0) / 100.0
            val perPieceCarbs = food.carbsPer100g * (food.gramsPerPiece ?: 1.0) / 100.0
            val perPieceFat = food.fatPer100g * (food.gramsPerPiece ?: 1.0) / 100.0
            CalculatedMacros(
                calories = perPieceCal * qty,
                protein = perPieceProt * qty,
                carbs = perPieceCarbs * qty,
                fat = perPieceFat * qty
            )
        }
    }

    fun saveFromDatabase() {
        val food = selectedFood ?: return
        val calc = calculated ?: return
        val qty = quantity.toDoubleOrNull() ?: return
        val unit = if (food.unitType == FoodUnitType.PIECE) "buc" else "g"
        onSave(
            food.nameFor(currentLang), "", mealType,
            qty,
            calc.calories,
            calc.protein,
            calc.carbs,
            calc.fat,
            0.0,
            unit
        )
    }

    fun saveManual() {
        onSave(
            name,
            brand,
            mealType,
            servingSize.toDoubleOrNull() ?: 100.0,
            calories.toDoubleOrNull() ?: 0.0,
            protein.toDoubleOrNull() ?: 0.0,
            carbs.toDoubleOrNull() ?: 0.0,
            fat.toDoubleOrNull() ?: 0.0,
            fiber.toDoubleOrNull() ?: 0.0,
            servingUnit
        )
    }

    fun selectFood(food: FoodItemEntity) {
        selectedFood = food
        searchQuery = food.nameFor(currentLang)
        quantity = if (food.unitType == FoodUnitType.PIECE) "1" else "100"
        foodViewModel.clearQuery()
    }

    Scaffold(
        topBar = {
            KineticAppBar(
                onBack = onBack,
                actions = {
                    IconButton(onClick = {
                        if (mode == EntryMode.SEARCH && selectedFood != null) saveFromDatabase() else saveManual()
                    }) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = p.ac)
                    }
                }
            )
        },
        containerColor = p.bg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // ── Toggle Căutare / Manual ──────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ModeToggleButton(
                    selected = mode == EntryMode.SEARCH,
                    text = strings.searchFood,
                    modifier = Modifier.weight(1f),
                    p = p,
                    onClick = { mode = EntryMode.SEARCH }
                )
                ModeToggleButton(
                    selected = mode == EntryMode.MANUAL,
                    text = strings.manualEntryMode,
                    modifier = Modifier.weight(1f),
                    p = p,
                    onClick = {
                        if (selectedFood != null && name.isBlank()) name = selectedFood!!.nameFor(currentLang)
                        mode = EntryMode.MANUAL
                    }
                )
            }

            if (mode == EntryMode.SEARCH) {
                // ── Câmpul de căutare ─────────────────────────────
                AppGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    p = p,
                    cornerRadius = 16.dp,
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Column {
                        Text(strings.searchFood, style = MaterialTheme.typography.titleMedium, color = p.tp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                if (selectedFood != null && it != selectedFood?.nameFor(currentLang)) {
                                    selectedFood = null
                                    quantity = ""
                                }
                                foodViewModel.onQueryChange(it)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(strings.foodSearchHint, color = p.ts.copy(alpha = 0.5f)) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = p.ac) },
                            trailingIcon = {
                                if (searchQuery.isNotBlank()) {
                                    IconButton(onClick = {
                                        searchQuery = ""
                                        selectedFood = null
                                        quantity = ""
                                        foodViewModel.clearQuery()
                                    }) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = p.ts, modifier = Modifier.size(18.dp))
                                    }
                                }
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = p.ac,
                                unfocusedBorderColor = p.ts.copy(alpha = 0.3f),
                                cursorColor = p.ac,
                                focusedTextColor = p.tp,
                                unfocusedTextColor = p.tp
                            )
                        )

                        // ── Sugestii ────────────────────────────────
                        if (selectedFood == null && suggestions.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            suggestions.forEachIndexed { index, food ->
                                val perPieceCal = if (food.unitType == FoodUnitType.PIECE) {
                                    food.caloriesPer100g * (food.gramsPerPiece ?: 1.0) / 100.0
                                } else food.caloriesPer100g
                                val unitLabel = if (food.unitType == FoodUnitType.PIECE) {
                                    "${food.gramsPerPiece?.toInt() ?: 1}g ${strings.perPiece}"
                                } else strings.per100g
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectFood(food) }
                                        .padding(vertical = 10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(food.nameFor(currentLang), color = p.tp, fontWeight = FontWeight.Medium)
                                            Text(
                                                "${String.format(Locale.US, "%.0f", perPieceCal)} kcal · $unitLabel",
                                                color = p.ts,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                        Icon(Icons.Default.Add, contentDescription = null, tint = p.ac, modifier = Modifier.size(20.dp))
                                    }
                                    if (index < suggestions.lastIndex) {
                                        HorizontalDivider(color = p.ts.copy(alpha = 0.15f))
                                    }
                                }
                            }
                        }

                        // ── Alimentul nu a fost găsit ───────────────
                        if (selectedFood == null && searchQuery.trim().length >= 2 && suggestions.isEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Restaurant, contentDescription = null, tint = p.ts.copy(alpha = 0.6f), modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(strings.noFoodFound, color = p.ts, style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(4.dp))
                                TextButton(onClick = {
                                    name = searchQuery
                                    mode = EntryMode.MANUAL
                                }) {
                                    Text(strings.enterManually, color = p.ac, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // ── Aliment selectat: cantitate + calcul live ─────
                if (selectedFood != null) {
                    AppGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        p = p,
                        cornerRadius = 16.dp,
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    selectedFood!!.nameFor(currentLang),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = p.tp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = { selectedFood = null; quantity = ""; searchQuery = ""; foodViewModel.clearQuery() }) {
                                    Text(strings.cancel, color = p.ts)
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            val isPiece = selectedFood!!.unitType == FoodUnitType.PIECE
                            val unitLabel = if (isPiece) strings.piecesShort else strings.gramsShort
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = quantity,
                                    onValueChange = { quantity = it.filter { c -> c.isDigit() || c == '.' } },
                                    modifier = Modifier.weight(1f),
                                    label = { Text(strings.quantity) },
                                    placeholder = { Text(if (isPiece) "1" else "100") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    suffix = {
                                        Text(unitLabel, color = p.ac, fontWeight = FontWeight.Bold)
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = p.ac,
                                        unfocusedBorderColor = p.ts.copy(alpha = 0.3f),
                                        cursorColor = p.ac,
                                        focusedTextColor = p.tp,
                                        unfocusedTextColor = p.tp,
                                        focusedLabelColor = p.ac,
                                        unfocusedLabelColor = p.ts
                                    )
                                )
                            }

                            if (isPiece && selectedFood!!.gramsPerPiece != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "1 ${strings.piecesShort} ≈ ${selectedFood!!.gramsPerPiece?.toInt()}g",
                                    color = p.ts,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            // ── Valori calculate live ────────────────
                            Spacer(modifier = Modifier.height(12.dp))
                            CalculatedMacroRow(
                                calculated = calculated,
                                strings = strings,
                                p = p
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { saveFromDatabase() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                enabled = calculated != null,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = p.ac)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(strings.addToJournal, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }
                }
            } else {
                // ══ MOD MANUAL — fallback complet ══
                FoodInputCard(p = p, title = strings.foodName, value = name, onValueChange = { name = it }, placeholder = "Ex: Chicken Breast")
                FoodInputCard(p = p, title = strings.brandLabel, value = brand, onValueChange = { brand = it }, placeholder = "Ex: Farm Foods")
            }

            // ── Tipul mesei (comun) ─────────────────────────────
            AppGlassCard(
                modifier = Modifier.fillMaxWidth(),
                p = p,
                cornerRadius = 16.dp,
                contentPadding = PaddingValues(16.dp)
            ) {
                Column {
                    Text(strings.selectMealType, style = MaterialTheme.typography.titleMedium, color = p.tp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = mealTypes.find { it.first == mealType }?.second ?: "",
                        onValueChange = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { mealDropdownExpanded = true },
                        readOnly = true,
                        enabled = false,
                        trailingIcon = { Icon(Icons.Default.ArrowBack, contentDescription = null, tint = p.ts) },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = p.ts.copy(alpha = 0.3f),
                            disabledTextColor = p.tp,
                            disabledTrailingIconColor = p.ts
                        )
                    )
                    DropdownMenu(
                        expanded = mealDropdownExpanded,
                        onDismissRequest = { mealDropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        mealTypes.forEach { (type, label) ->
                            DropdownMenuItem(
                                text = { Text(label, color = p.tp) },
                                onClick = {
                                    mealType = type
                                    mealDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            if (mode == EntryMode.MANUAL) {
                AppGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    p = p,
                    cornerRadius = 16.dp,
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Column {
                        Text(strings.todaysMacros, style = MaterialTheme.typography.titleMedium, color = p.tp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        MacroInputField(strings.caloriesLabel, calories, { calories = it }, p = p)
                        MacroInputField(strings.proteinLabel, protein, { protein = it }, p = p)
                        MacroInputField(strings.carbsLabel, carbs, { carbs = it }, p = p)
                        MacroInputField(strings.fatLabel, fat, { fat = it }, p = p)
                        MacroInputField(strings.fiber, fiber, { fiber = it }, p = p)
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppConstants.BOTTOM_NAV_PADDING))
        }
    }
}

@Composable
private fun ModeToggleButton(
    selected: Boolean,
    text: String,
    modifier: Modifier,
    p: AppPalette,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(12.dp),
        colors = if (selected) {
            ButtonDefaults.buttonColors(containerColor = p.ac)
        } else {
            ButtonDefaults.outlinedButtonColors(contentColor = p.tp)
        },
        border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, p.ac.copy(alpha = 0.4f))
    ) {
        Text(text, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
    }
}

@Composable
private fun CalculatedMacroRow(
    calculated: CalculatedMacros?,
    strings: LanguageManager.Strings,
    p: AppPalette
) {
    val items = if (calculated != null) listOf(
        Triple(strings.caloriesLabel, String.format(Locale.US, "%.0f", calculated.calories), "kcal"),
        Triple(strings.proteinLabel, String.format(Locale.US, "%.1f", calculated.protein), "g"),
        Triple(strings.carbsLabel, String.format(Locale.US, "%.1f", calculated.carbs), "g"),
        Triple(strings.fatLabel, String.format(Locale.US, "%.1f", calculated.fat), "g")
    ) else emptyList()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        if (calculated == null) {
            Text(
                strings.quantity,
                color = p.ts,
                style = MaterialTheme.typography.bodySmall
            )
        } else {
            items.forEach { (label, value, unit) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .width(64.dp)
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            value,
                            color = p.ac,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Text("$label ($unit)", color = p.ts, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun FoodInputCard(
    p: AppPalette,
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    AppGlassCard(
        modifier = Modifier.fillMaxWidth(),
        p = p,
        cornerRadius = 16.dp,
        contentPadding = PaddingValues(16.dp)
    ) {
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium, color = p.tp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(placeholder, color = p.ts.copy(alpha = 0.5f)) },
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = p.ac,
                    unfocusedBorderColor = p.ts.copy(alpha = 0.3f),
                    cursorColor = p.ac,
                    focusedTextColor = p.tp,
                    unfocusedTextColor = p.tp
                )
            )
        }
    }
}

@Composable
private fun MacroInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    p: AppPalette
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            label,
            color = p.ts,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.width(80.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("0", color = p.ts.copy(alpha = 0.5f)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = p.ac,
                unfocusedBorderColor = p.ts.copy(alpha = 0.3f),
                cursorColor = p.ac,
                focusedTextColor = p.tp,
                unfocusedTextColor = p.tp
            )
        )
    }
}
