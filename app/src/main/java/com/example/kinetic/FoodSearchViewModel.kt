package com.example.kinetic

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch

/**
 * ViewModel pentru căutarea alimentelor din baza de date locală (Room).
 * Debounce de 250ms pe textul introdus, apoi interoghează FoodItemDao
 * și emite lista de sugestii pentru autocomplete.
 */
class FoodSearchViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val dao = db.foodItemDao()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _suggestions = MutableStateFlow<List<FoodItemEntity>>(emptyList())
    val suggestions: StateFlow<List<FoodItemEntity>> = _suggestions

    private val _isSeeded = MutableStateFlow(false)
    val isSeeded: StateFlow<Boolean> = _isSeeded

    init {
        viewModelScope.launch {
            try {
                FoodItemSeeder(db, application).seedIfEmpty()
            } finally {
                _isSeeded.value = true
            }
        }

        viewModelScope.launch {
            _query
                .debounce(250)
                .distinctUntilChanged()
                .mapLatest { raw ->
                    val q = raw.trim()
                    if (q.length < 2) {
                        emptyList()
                    } else {
                        // Normalizează interogarea (lowercase + fără diacritice) ca să se
                        // potrivească cu searchKey din bază: 'rosii' găsește 'Roșii'.
                        dao.search(FoodItemSeeder.normalizeForSearch(q))
                    }
                }
                .collect { _suggestions.value = it }
        }
    }

    fun onQueryChange(value: String) {
        _query.value = value
    }

    fun clearQuery() {
        _query.value = ""
    }
}
