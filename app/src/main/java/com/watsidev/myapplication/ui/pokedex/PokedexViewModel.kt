package com.watsidev.myapplication.ui.pokedex

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.watsidev.myapplication.data.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class PokedexUiState(
    val discoveredIds: Set<Int> = emptySet(),
    val totalPokemonCount: Int = 0,
    val isLoading: Boolean = false,
    val pokemonList: List<String> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class PokedexViewModel @Inject constructor(
    private val repository: PokemonRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PokedexUiState())
    val uiState: StateFlow<PokedexUiState> = _uiState.asStateFlow()

    init {
        Log.d("PokedexVM", "ViewModel initialized")
        loadDiscoveryData()
        loadPokemonList()
    }

    private fun loadDiscoveryData() {
        Log.d("PokedexVM", "Starting discovery data observation")
        repository.getDiscoveredPokemon()
            .map { discovered -> 
                Log.d("PokedexVM", "Processing ${discovered.size} discovered entities")
                // Move heavy set conversion to background thread
                discovered.map { it.id }.toSet()
            }
            .flowOn(Dispatchers.Default)
            .onEach { discoveredIds ->
                Log.d("PokedexVM", "Updating UI with ${discoveredIds.size} discovered IDs")
                _uiState.update { it.copy(discoveredIds = discoveredIds) }
            }
            .catch { e ->
                Log.e("PokedexVM", "Error loading discovery data", e)
                _uiState.update { it.copy(error = "Failed to load discovery data: ${e.message}") }
            }
            .launchIn(viewModelScope)
    }

    fun loadPokemonList() {
        viewModelScope.launch {
            try {
                Log.d("PokedexVM", "Fetching Pokémon list from repository")
                _uiState.update { it.copy(isLoading = true, error = null) }
                val list = repository.getPokemonList()
                
                if (list.isNotEmpty()) {
                    Log.d("PokedexVM", "Fetched ${list.size} Pokémon names. Processing...")
                    // Process heavy list mapping on Default dispatcher
                    val names = withContext(Dispatchers.Default) {
                        list.map { it.name }
                    }
                    Log.d("PokedexVM", "List processing complete. Updating UI.")
                    _uiState.update { it.copy(
                        pokemonList = names,
                        totalPokemonCount = list.size,
                        isLoading = false
                    ) }
                } else {
                    Log.w("PokedexVM", "Fetched list is empty")
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "Failed to fetch Pokémon list. Check your connection."
                    ) }
                }
            } catch (e: Exception) {
                Log.e("PokedexVM", "Error fetching Pokémon list", e)
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
