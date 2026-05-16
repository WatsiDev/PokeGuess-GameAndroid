package com.watsidev.myapplication.ui.pokedex

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
        loadDiscoveryData()
        loadPokemonList()
    }

    private fun loadDiscoveryData() {
        repository.getDiscoveredPokemon()
            .map { discovered -> 
                withContext(Dispatchers.Default) {
                    discovered.map { it.id }.toSet()
                }
            }
            .onEach { discoveredIds ->
                _uiState.update { it.copy(discoveredIds = discoveredIds) }
            }
            .catch { e ->
                _uiState.update { it.copy(error = "Failed to load discovery data: ${e.message}") }
            }
            .launchIn(viewModelScope)
    }

    fun loadPokemonList() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                val list = repository.getPokemonList()
                if (list.isNotEmpty()) {
                    _uiState.update { it.copy(
                        pokemonList = list.map { it.name },
                        totalPokemonCount = list.size,
                        isLoading = false
                    ) }
                } else {
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "Failed to fetch Pokémon list. Check your connection."
                    ) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
