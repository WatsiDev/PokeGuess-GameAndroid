package com.watsidev.pokeguessredux.ui.pokedex

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.watsidev.pokeguessredux.data.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class PokedexUiState(
    val discoveredIds: Set<Int> = emptySet(),
    val totalPokemonCount: Int = 1025, // PokeAPI constant
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PokedexViewModel @Inject constructor(
    private val repository: PokemonRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PokedexUiState())
    val uiState: StateFlow<PokedexUiState> = _uiState.asStateFlow()

    val pokemonPagingData: Flow<PagingData<String>> = repository.getPokemonPagingData()
        .cachedIn(viewModelScope)

    init {
        loadDiscoveryData()
        ensurePokemonListInitialized()
    }

    private fun ensurePokemonListInitialized() {
        viewModelScope.launch {
            // Trigger list fetch to pre-populate Room if needed
            repository.getPokemonList()
        }
    }

    private fun loadDiscoveryData() {
        repository.getDiscoveredPokemon()
            .map { discovered -> 
                // Move heavy set conversion to background thread
                discovered.map { it.id }.toSet()
            }
            .flowOn(Dispatchers.Default)
            .onEach { discoveredIds ->
                _uiState.update { it.copy(discoveredIds = discoveredIds) }
            }
            .catch { e ->
                _uiState.update { it.copy(error = "Failed to load discovery data: ${e.message}") }
            }
            .launchIn(viewModelScope)
    }
}
