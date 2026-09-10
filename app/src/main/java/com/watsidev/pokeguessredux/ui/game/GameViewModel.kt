package com.watsidev.pokeguessredux.ui.game

import androidx.compose.runtime.Immutable
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.random.Random

enum class GameMode {
    DAILY, INFINITE, GENERATION
}

enum class ShinyBonusType {
    NONE, DOUBLE_RATE, FIFTY_PERCENT, GUARANTEED
}

@Immutable
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
    val activeShinyBonus: ShinyBonusType = ShinyBonusType.NONE,
    val consumedMilestones: Set<Int> = emptySet(),
    val shouldShowStreakSaverDialog: Boolean = false,
    val brokenStreakToRestore: Int = 0,
    val error: String? = null
)

private data class PrefGroup1(
    val streak: Int,
    val consumed: Set<Int>,
    val captured: Set<Int>,
    val theme: String
)

private data class PrefGroup2(
    val vibs: Boolean,
    val dailyNotifs: Boolean,
    val streakNotifs: Boolean,
    val updateNoticeShown: Boolean,
    val adAvailable: Boolean
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
                
                // Combine user preferences flows reactively into uiState
                val group1Flow = combine(
                    userPreferences.currentStreak,
                    userPreferences.consumedMilestones,
                    userPreferences.capturedPokemonIds,
                    userPreferences.themePreference
                ) { streak, consumed, captured, theme ->
                    PrefGroup1(streak, consumed, captured, theme)
                }

                val group2Flow = combine(
                    userPreferences.vibrationsEnabled,
                    userPreferences.dailyNotificationsEnabled,
                    userPreferences.streakNotificationsEnabled,
                    userPreferences.hasShownUpdateNotice,
                    rewardedAdManager.isAdAvailable
                ) { vibs, dailyNotifs, streakNotifs, updateNoticeShown, adAvailable ->
                    PrefGroup2(vibs, dailyNotifs, streakNotifs, updateNoticeShown, adAvailable)
                }

                combine(group1Flow, group2Flow) { g1, g2 ->
                    _uiState.update { current ->
                        current.copy(
                            streak = g1.streak,
                            consumedMilestones = g1.consumed,
                            activeShinyBonus = calculateActiveShinyBonus(g1.streak, g1.consumed),
                            capturedIds = g1.captured,
                            theme = g1.theme,
                            vibrationsEnabled = g2.vibs,
                            dailyNotificationsEnabled = g2.dailyNotifs,
                            streakNotificationsEnabled = g2.streakNotifs,
                            shouldShowUpdateNotice = !g2.updateNoticeShown,
                            isAdAvailable = g2.adAvailable
                        )
                    }
                }.launchIn(this)

                // Initial setup for Daily mode
                setupDailyGame()
                startTimeUntilNextUpdate()

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

    private fun calculateActiveShinyBonus(streak: Int, consumedMilestones: Set<Int>): ShinyBonusType {
        return when {
            streak >= 30 && 30 !in consumedMilestones -> ShinyBonusType.GUARANTEED
            streak >= 14 && 14 !in consumedMilestones -> ShinyBonusType.FIFTY_PERCENT
            streak >= 7 && 7 !in consumedMilestones -> ShinyBonusType.DOUBLE_RATE
            else -> ShinyBonusType.NONE
        }
    }

    private fun getEffectiveShinyProbability(bonusType: ShinyBonusType): Float {
        return when (bonusType) {
            ShinyBonusType.GUARANTEED -> 1.0f
            ShinyBonusType.FIFTY_PERCENT -> 0.50f
            ShinyBonusType.DOUBLE_RATE -> 0.25f
            ShinyBonusType.NONE -> SHINY_PROBABILITY
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
                val previousStreak = userPreferences.currentStreak.first()
                if (previousStreak > 0) {
                    userPreferences.updateBrokenStreak(previousStreak)
                    userPreferences.updateStreak(0)
                    userPreferences.clearConsumedMilestones()
                    _uiState.update { 
                        it.copy(
                            shouldShowStreakSaverDialog = true, 
                            brokenStreakToRestore = previousStreak 
                        ) 
                    }
                }
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

        val effectiveProb = getEffectiveShinyProbability(_uiState.value.activeShinyBonus)

        val isShiny = if (isGameOver && existingDiscovery != null) {
            existingDiscovery.isShiny
        } else {
            random.nextFloat() < effectiveProb
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
        val effectiveProb = getEffectiveShinyProbability(_uiState.value.activeShinyBonus)
        val isShiny = Random.nextFloat() < effectiveProb
        _uiState.update { it.copy(targetPokemon = target, isTargetShiny = isShiny) }
    }

    private suspend fun setupGenerationGame(gen: Int) {
        _uiState.update { it.copy(isLoading = true) }
        try {
            val genPokemon = repository.getPokemonByGeneration(gen)
            if (genPokemon.isNotEmpty()) {
                val randomIndex = Random.nextInt(genPokemon.size)
                val target = repository.getPokemon(genPokemon[randomIndex].name)
                val effectiveProb = getEffectiveShinyProbability(_uiState.value.activeShinyBonus)
                val isShiny = Random.nextFloat() < effectiveProb
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

                    val shortResults = withContext(Dispatchers.Default) {
                        filteredList.filter {
                            it.name.contains(query, ignoreCase = true) 
                        }.take(10)
                    }
                    
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

                    // Consume active shiny bonus if used
                    val currentStreak = _uiState.value.streak
                    val consumed = _uiState.value.consumedMilestones
                    if (currentStreak >= 30 && 30 !in consumed) userPreferences.markMilestoneConsumed(30)
                    else if (currentStreak >= 14 && 14 !in consumed) userPreferences.markMilestoneConsumed(14)
                    else if (currentStreak >= 7 && 7 !in consumed) userPreferences.markMilestoneConsumed(7)

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

    fun recoverStreakWithAd(activity: android.app.Activity) {
        if (rewardedAdManager.isAdAvailable()) {
            rewardedAdManager.showAd(activity) {
                viewModelScope.launch {
                    val broken = userPreferences.brokenStreak.first()
                    if (broken > 0) {
                        val calendar = Calendar.getInstance()
                        calendar.add(Calendar.DAY_OF_YEAR, -1)
                        val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

                        userPreferences.updateStreak(broken)
                        userPreferences.updateLastGuessDate(yesterday)
                        userPreferences.clearBrokenStreak()
                        _uiState.update { 
                            it.copy(
                                streak = broken,
                                shouldShowStreakSaverDialog = false,
                                brokenStreakToRestore = 0
                            ) 
                        }
                    }
                }
            }
        } else {
            rewardedAdManager.loadAd()
        }
    }

    fun dismissStreakSaverDialog() {
        viewModelScope.launch {
            userPreferences.clearBrokenStreak()
            _uiState.update { it.copy(shouldShowStreakSaverDialog = false) }
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
