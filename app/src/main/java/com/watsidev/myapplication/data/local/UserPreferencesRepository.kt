package com.watsidev.myapplication.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val STREAK_KEY = intPreferencesKey("current_streak")
    private val LAST_GUESS_DATE = stringPreferencesKey("last_guess_date")
    private val DAILY_GUESSES = stringPreferencesKey("daily_guesses")
    private val CAPTURED_POKEMON_IDS = stringPreferencesKey("captured_pokemon_ids")
    private val THEME_KEY = stringPreferencesKey("theme_preference")

    val currentStreak: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[STREAK_KEY] ?: 0
    }

    val lastGuessDate: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[LAST_GUESS_DATE]
    }

    val dailyGuesses: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[DAILY_GUESSES] ?: ""
    }

    val capturedPokemonIds: Flow<Set<Int>> = context.dataStore.data.map { preferences ->
        val idsString = preferences[CAPTURED_POKEMON_IDS] ?: ""
        if (idsString.isEmpty()) emptySet() else idsString.split(",").mapNotNull { it.toIntOrNull() }.toSet()
    }

    val themePreference: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[THEME_KEY] ?: "system"
    }

    suspend fun updateStreak(streak: Int) {
        context.dataStore.edit { preferences ->
            preferences[STREAK_KEY] = streak
        }
    }

    suspend fun updateLastGuessDate(date: String) {
        context.dataStore.edit { preferences ->
            preferences[LAST_GUESS_DATE] = date
        }
    }

    suspend fun updateDailyGuesses(guessesJson: String) {
        context.dataStore.edit { preferences ->
            preferences[DAILY_GUESSES] = guessesJson
        }
    }

    suspend fun updateTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme
        }
    }

    suspend fun addCapturedPokemon(id: Int) {
        context.dataStore.edit { preferences ->
            val currentIds = (preferences[CAPTURED_POKEMON_IDS] ?: "").split(",")
                .filter { it.isNotEmpty() }.toMutableSet()
            currentIds.add(id.toString())
            preferences[CAPTURED_POKEMON_IDS] = currentIds.joinToString(",")
        }
    }

    suspend fun clearDailyData() {
        context.dataStore.edit { preferences ->
            preferences.remove(DAILY_GUESSES)
        }
    }

    suspend fun resetAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

