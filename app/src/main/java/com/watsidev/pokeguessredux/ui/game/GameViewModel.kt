package com.watsidev.pokeguessredux.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.watsidev.pokeguessredux.ad.RewardedAdManager
import com.watsidev.pokeguessredux.data.local.UserPreferencesRepository
import com.watsidev.pokeguessredux.data.model.Pokemon
import com.watsidev.pokeguessredux.data.remote.NamedApiResourceShort
import com.watsidev.pokeguessredux.data.repository.PokemonRepository
import com.watsidev.pokeguessredux.domain.model.HintType
import com.watsidev.pokeguessredux.domain.model.MatchState
import com.watsidev.pokeguessredux.domain.model.PokemonComparison
import com.watsidev.pokeguessredux.domain.usecase.ComparePokemonUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.random.Random

enum class GameMode {
    DAILY, INFINITE, GENERATION
}

data class GameUiState(
    val gameMode: GameMode = GameMode.DAILY,
    val selectedGeneration: Int? = null,
    val targetPokemon: Pokemon? = null,
    val isTargetShiny: Boolean = false,
    val guesses: List<PokemonComparison> = emptyList(),
    val isGameOver: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<Pokemon> = emptyList(),
    val isLoading: Boolean = false,
    val isSearching: Boolean = false,
    val streak: Int = 0,
    val timeUntilNext: String = "",
    val capturedIds: Set<Int> = emptySet(),
    val revealedHints: Set<HintType> = emptySet(),
    val isAdAvailable: Boolean = false,
    val theme: String = "system",
    val vibrationsEnabled: Boolean = true,
    val dailyNotificationsEnabled: Boolean = true,
    val streakNotificationsEnabled: Boolean = true,
    val shouldShowUpdateNotice: Boolean = false,
    val shouldShowNotificationPermissionPrompt: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class GameViewModel @Inject constructor(
    private val repository: PokemonRepository,
    private val userPreferences: UserPreferencesRepository,
    private val comparePokemonUseCase: ComparePokemonUseCase,
    private val rewardedAdManager: RewardedAdManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var allPokemon: List<NamedApiResourceShort> = emptyList()
    private var searchJob: Job? = null

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                allPokemon = repository.getPokemonList()
                
                // Collect streak
                launch {
                    userPreferences.currentStreak.collect { streak ->
                        _uiState.update { it.copy(streak = streak) }
                    }
                }

                // Collect captured IDs
                launch {
                    userPreferences.capturedPokemonIds.collect { ids ->
                        _uiState.update { it.copy(capturedIds = ids) }
                    }
                }

                // Collect theme
                launch {
                    userPreferences.themePreference.collect { theme ->
                        _uiState.update { it.copy(theme = theme) }
                    }
                }

                // Collect vibrations
                launch {
                    userPreferences.vibrationsEnabled.collect { enabled ->
                        _uiState.update { it.copy(vibrationsEnabled = enabled) }
                    }
                }

                // Collect daily notifications preference
                launch {
                    userPreferences.dailyNotificationsEnabled.collect { enabled ->
                        _uiState.update { it.copy(dailyNotificationsEnabled = enabled) }
                    }
                }

                // Collect streak notifications preference
                launch {
                    userPreferences.streakNotificationsEnabled.collect { enabled ->
                        _uiState.update { it.copy(streakNotificationsEnabled = enabled) }
                    }
                }

                // Collect notice state
                launch {
                    userPreferences.hasShownUpdateNotice.collect { shown ->
                        _uiState.update { it.copy(shouldShowUpdateNotice = !shown) }
                    }
                }

                // Monitor ad availability reactively
                launch {
                    rewardedAdManager.isAdAvailable.collect { available ->
                        _uiState.update { it.copy(isAdAvailable = available) }
                    }
                }

                // Initial setup for Daily mode
                setupDailyGame()
                startTimeUntilNextUpdate()

            } catch (e: Exception) {

            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun startTimeUntilNextUpdate() {
        viewModelScope.launch {
            while (true) {
                val now = Calendar.getInstance()
                val tomorrow = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }
                val diff = tomorrow.timeInMillis - now.timeInMillis
                val hours = diff / (1000 * 60 * 60)
                val minutes = (diff % (1000 * 60 * 60)) / (1000 * 60)
                val seconds = (diff % (1000 * 60)) / 1000
                _uiState.update { it.copy(timeUntilNext = String.format("%02d:%02d:%02d", hours, minutes, seconds)) }
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    fun setGameMode(mode: GameMode, generation: Int? = null) {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    gameMode = mode, 
                    selectedGeneration = generation,
                    guesses = emptyList(),
                    revealedHints = emptySet(),
                    isGameOver = false 
                ) 
            }
            when (mode) {
                GameMode.DAILY -> setupDailyGame()
                GameMode.INFINITE -> setupInfiniteGame()
                GameMode.GENERATION -> {
                    if (generation != null) setupGenerationGame(generation)
                }
            }
        }
    }

    private suspend fun setupDailyGame() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastDate = userPreferences.lastGuessDate.first()
        
        if (lastDate != today && lastDate != null) {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            
            if (lastDate != yesterday) {
                userPreferences.updateStreak(0)
            }
            userPreferences.clearDailyData()
        }

        val seed = today.hashCode().toLong()
        val random = Random(seed)
        val randomIndex = random.nextInt(allPokemon.size)
        val target = repository.getPokemon(allPokemon[randomIndex].name)
        
        val savedGuessesJson = userPreferences.dailyGuesses.first()
        val savedGuessNames: List<String> = if (savedGuessesJson.isNotEmpty()) {
            Json.decodeFromString(savedGuessesJson)
        } else {
            emptyList()
        }

        val comparisons = savedGuessNames.map { name ->
            val guessPokemon = repository.getPokemon(name)
            comparePokemonUseCase(guessPokemon, target)
        }

        val isGameOver = comparisons.any { it.name == target.name }

        // If the daily game is already completed/discovered, check Room DB for actual shiny status
        val discoveredList = repository.getDiscoveredPokemon().first()
        val existingDiscovery = discoveredList.find { it.id == target.id }

        val isShiny = if (isGameOver && existingDiscovery != null) {
            existingDiscovery.isShiny
        } else {
            random.nextFloat() < SHINY_PROBABILITY
        }

        _uiState.update { 
            it.copy(
                targetPokemon = target,
                isTargetShiny = isShiny,
                guesses = comparisons.reversed(),
                isGameOver = isGameOver
            ) 
        }
    }

    private suspend fun setupInfiniteGame() {
        val randomIndex = Random.nextInt(allPokemon.size)
        val target = repository.getPokemon(allPokemon[randomIndex].name)
        val isShiny = Random.nextFloat() < SHINY_PROBABILITY
        _uiState.update { it.copy(targetPokemon = target, isTargetShiny = isShiny) }
    }

    private suspend fun setupGenerationGame(gen: Int) {
        _uiState.update { it.copy(isLoading = true) }
        try {
            val genPokemon = repository.getPokemonByGeneration(gen)
            if (genPokemon.isNotEmpty()) {
                val randomIndex = Random.nextInt(genPokemon.size)
                val target = repository.getPokemon(genPokemon[randomIndex].name)
                val isShiny = Random.nextFloat() < SHINY_PROBABILITY
                _uiState.update { it.copy(targetPokemon = target, isTargetShiny = isShiny, isLoading = false) }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message, isLoading = false) }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        
        if (query.length >= 2) {
            searchJob = viewModelScope.launch {
                try {
                    delay(300)
                    _uiState.update { it.copy(isSearching = true) }
                    
                    // Offline Search: Filter from allPokemon list (which is loaded from Room/API in loadInitialData)
                    val gen = _uiState.value.selectedGeneration
                    val filteredList = if (gen != null) {
                        repository.getPokemonByGeneration(gen)
                    } else {
                        allPokemon
                    }

                    val shortResults = filteredList.filter {
                        it.name.contains(query, ignoreCase = true) 
                    }.take(10)
                    
                    if (shortResults.isNotEmpty()) {
                        // repository.getPokemonDetailsParallel will check Room for each Pokemon
                        val detailedResults = repository.getPokemonDetailsParallel(shortResults.map { it.name })
                        _uiState.update { it.copy(searchResults = detailedResults, isSearching = false) }
                    } else {
                        _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(isSearching = false) }
                }
            }
        } else {
            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
        }
    }

    fun makeGuess(pokemonName: String) {
        viewModelScope.launch {
            try {
                val guessPokemon = repository.getPokemon(pokemonName)
                val target = _uiState.value.targetPokemon ?: return@launch
                
                val comparison = comparePokemonUseCase(guessPokemon, target)
                val newGuesses = listOf(comparison) + _uiState.value.guesses
                
                val isCorrect = comparison.name == target.name
                
                if (_uiState.value.gameMode == GameMode.DAILY) {
                    val savedGuessesJson = userPreferences.dailyGuesses.first()
                    val savedGuessNames: MutableList<String> = if (savedGuessesJson.isNotEmpty()) {
                        Json.decodeFromString<List<String>>(savedGuessesJson).toMutableList()
                    } else {
                        mutableListOf()
                    }
                    if (!savedGuessNames.contains(pokemonName)) {
                        savedGuessNames.add(pokemonName)
                        userPreferences.updateDailyGuesses(Json.encodeToString(savedGuessNames))
                    }
                }

                var shouldShowNotificationPrompt = false
                if (isCorrect) {
                    val isDaily = _uiState.value.gameMode == GameMode.DAILY
                    val isShiny = _uiState.value.isTargetShiny

                    if (isDaily) {
                        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        userPreferences.updateLastGuessDate(today)
                        userPreferences.updateStreak(_uiState.value.streak + 1)
                        val hasAsked = userPreferences.hasAskedNotificationPermission.first()
                        if (!hasAsked) {
                            shouldShowNotificationPrompt = true
                        }
                    }
                    userPreferences.addCapturedPokemon(target.id)
                    repository.markAsDiscovered(
                        id = target.id,
                        name = target.name,
                        isShiny = isShiny,
                        isDaily = isDaily
                    )
                }

                _uiState.update { 
                    it.copy(
                        guesses = newGuesses,
                        isGameOver = isCorrect,
                        shouldShowNotificationPermissionPrompt = shouldShowNotificationPrompt,
                        searchQuery = "",
                        searchResults = emptyList()
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun resetAllProgress() {
        viewModelScope.launch {
            repository.clearDiscovery()
            userPreferences.resetAll()
            _uiState.update { GameUiState() }
            loadInitialData()
        }
    }

    fun setTheme(theme: String) {
        viewModelScope.launch {
            userPreferences.updateTheme(theme)
        }
    }

    fun setVibrationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.updateVibrations(enabled)
        }
    }

    fun setDailyNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.updateDailyNotifications(enabled)
        }
    }

    fun setStreakNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.updateStreakNotifications(enabled)
        }
    }

    fun dismissNotificationPermissionPrompt() {
        viewModelScope.launch {
            userPreferences.setNotificationPermissionAsked()
            _uiState.update { it.copy(shouldShowNotificationPermissionPrompt = false) }
        }
    }

    fun dismissUpdateNotice() {
        viewModelScope.launch {
            userPreferences.setUpdateNoticeShown()
            _uiState.update { it.copy(shouldShowUpdateNotice = false) }
        }
    }

    fun onHintRequested(activity: android.app.Activity) {
        if (rewardedAdManager.isAdAvailable()) {
            rewardedAdManager.showAd(activity) {
                revealNewHint()
            }
        } else {
            rewardedAdManager.loadAd()
        }
    }

    private fun revealNewHint() {
        val target = _uiState.value.targetPokemon ?: return
        val currentGuesses = _uiState.value.guesses
        val alreadyRevealed = _uiState.value.revealedHints

        // Determine which attributes are already "solved" (correct in any guess)
        val solvedAttributes = mutableSetOf<HintType>()
        currentGuesses.forEach { guess ->
            if (guess.generation.state == MatchState.CORRECT) solvedAttributes.add(HintType.GENERATION)
            if (guess.evolutionaryStage.state == MatchState.CORRECT) solvedAttributes.add(HintType.EVOLUTIONARY_STAGE)
            if (guess.types.state == MatchState.CORRECT) solvedAttributes.add(HintType.TYPES)
            if (guess.height.state == MatchState.CORRECT) solvedAttributes.add(HintType.HEIGHT)
            if (guess.weight.state == MatchState.CORRECT) solvedAttributes.add(HintType.WEIGHT)
        }

        // Available hints: not solved and not already revealed
        val availableHints = HintType.entries.filter { it !in solvedAttributes && it !in alreadyRevealed }

        if (availableHints.isNotEmpty()) {
            val randomHint = availableHints.random()
            _uiState.update { it.copy(revealedHints = it.revealedHints + randomHint) }
        }
    }

    companion object {
        const val SHINY_PROBABILITY = 0.125f // 1 in 8 chance (12.5%). Set to 1.0f to test 100% shiny rate.
    }
}
