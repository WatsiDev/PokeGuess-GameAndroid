package com.watsidev.pokeguessredux.ui.game

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.imageLoader
import coil.request.ImageRequest
import com.watsidev.pokeguessredux.data.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import kotlin.random.Random

enum class MemoryDifficulty(val pairs: Int, val timeSeconds: Int) {
    EASY(4, 30),
    NORMAL(6, 45),
    HARD(8, 60),
    EXPERT(12, 90),
    MASTER(18, 150)
}

data class MemoryCard(
    val id: Int,
    val pokemonId: Int,
    val name: String,
    val imageUrl: String,
    val isFlipped: Boolean = false,
    val isMatched: Boolean = false
)

data class MemoryGameState(
    val cards: List<MemoryCard> = emptyList(),
    val flippedCards: List<MemoryCard> = emptyList(),
    val timeLeft: Int = 0,
    val isGameOver: Boolean = false,
    val isVictory: Boolean = false,
    val pairsFound: Int = 0,
    val totalPairs: Int = 0,
    val isLoading: Boolean = false
)

@HiltViewModel
class MemoryGameViewModel @Inject constructor(
    private val repository: PokemonRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MemoryGameState())
    val uiState: StateFlow<MemoryGameState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    fun startGame(context: Context, difficulty: MemoryDifficulty) {
        timerJob?.cancel()
        _uiState.update { 
            it.copy(
                isLoading = true, 
                isGameOver = false, 
                isVictory = false, 
                pairsFound = 0, 
                flippedCards = emptyList(),
                timeLeft = difficulty.timeSeconds
            ) 
        }
        
        viewModelScope.launch {
            try {
                val allPokemon = repository.getPokemonList()
                val selectedPokemon = allPokemon.shuffled().take(difficulty.pairs)
                
                val cards = mutableListOf<MemoryCard>()
                val imageUrls = mutableListOf<String>()
                
                selectedPokemon.forEachIndexed { index, p ->
                    val id = p.url.split("/").filter { it.isNotEmpty() }.last().toInt()
                    val imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png"
                    
                    imageUrls.add(imageUrl)
                    cards.add(MemoryCard(id = index * 2, pokemonId = id, name = p.name, imageUrl = imageUrl))
                    cards.add(MemoryCard(id = index * 2 + 1, pokemonId = id, name = p.name, imageUrl = imageUrl))
                }
                
                // Pre-load all images
                preloadImages(context, imageUrls)
                
                _uiState.update { 
                    it.copy(
                        cards = cards.shuffled(),
                        totalPairs = difficulty.pairs
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private suspend fun preloadImages(context: Context, urls: List<String>) = coroutineScope {
        val imageLoader = context.imageLoader
        urls.map { url ->
            async {
                val request = ImageRequest.Builder(context)
                    .data(url)
                    .build()
                imageLoader.execute(request)
            }
        }.awaitAll()
        
        _uiState.update { it.copy(isLoading = false) }
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.timeLeft > 0 && !_uiState.value.isVictory) {
                delay(1000)
                _uiState.update { it.copy(timeLeft = it.timeLeft - 1) }
            }
            if (_uiState.value.timeLeft <= 0 && !_uiState.value.isVictory) {
                _uiState.update { it.copy(isGameOver = true) }
            }
        }
    }

    fun onCardClicked(card: MemoryCard) {
        if (card.isFlipped || card.isMatched || _uiState.value.flippedCards.size >= 2 || _uiState.value.isGameOver) return

        val updatedCards = _uiState.value.cards.map {
            if (it.id == card.id) it.copy(isFlipped = true) else it
        }
        
        val newFlipped = _uiState.value.flippedCards + card.copy(isFlipped = true)
        
        _uiState.update { it.copy(cards = updatedCards, flippedCards = newFlipped) }

        if (newFlipped.size == 2) {
            viewModelScope.launch {
                delay(800) // Delay to let user see the second card
                checkMatch(newFlipped[0], newFlipped[1])
            }
        }
    }

    private fun checkMatch(card1: MemoryCard, card2: MemoryCard) {
        if (card1.pokemonId == card2.pokemonId) {
            // Match found
            val updatedCards = _uiState.value.cards.map {
                if (it.pokemonId == card1.pokemonId) it.copy(isMatched = true) else it
            }
            val newPairsFound = _uiState.value.pairsFound + 1
            val isVictory = newPairsFound == _uiState.value.totalPairs
            
            _uiState.update { 
                it.copy(
                    cards = updatedCards, 
                    flippedCards = emptyList(),
                    pairsFound = newPairsFound,
                    isVictory = isVictory
                ) 
            }
        } else {
            // No match
            val updatedCards = _uiState.value.cards.map {
                if (it.id == card1.id || it.id == card2.id) it.copy(isFlipped = false) else it
            }
            _uiState.update { it.copy(cards = updatedCards, flippedCards = emptyList()) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
