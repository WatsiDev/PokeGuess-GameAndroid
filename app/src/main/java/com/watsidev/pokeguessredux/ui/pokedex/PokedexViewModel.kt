package com.watsidev.pokeguessredux.ui.pokedex

import androidx.compose.runtime.Immutable
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.watsidev.pokeguessredux.data.local.DiscoveryEntity
import com.watsidev.pokeguessredux.data.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@Immutable
data class PokedexUiState(
    val discoveredMap: Map<Int, DiscoveryEntity> = emptyMap(),
    val discoveredIds: Set<Int> = emptySet(),
    val shinyDiscoveredIds: Set<Int> = emptySet(),
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
                val map = discovered.associateBy { it.id }
                val normalIds = discovered.filter { it.isNormal }.map { it.id }.toSet()
                val shinyIds = discovered.filter { it.isShiny }.map { it.id }.toSet()
                Triple(map, normalIds, shinyIds)
            }
            .flowOn(Dispatchers.Default)
            .onEach { (map, normalIds, shinyIds) ->
                _uiState.update { 
                    it.copy(
                        discoveredMap = map,
                        discoveredIds = normalIds,
                        shinyDiscoveredIds = shinyIds
                    ) 
                }
            }
            .catch { e ->
                _uiState.update { it.copy(error = "Failed to load discovery data: ${e.message}") }
            }
            .launchIn(viewModelScope)
    }
}
