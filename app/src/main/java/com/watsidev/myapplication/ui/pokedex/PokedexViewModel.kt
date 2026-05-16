package com.watsidev.myapplication.ui.pokedex

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.watsidev.myapplication.data.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PokedexUiState(
    val discoveredIds: Set<Int> = emptySet(),
    val totalPokemonCount: Int = 1025,
    val isLoading: Boolean = false,
    val pokemonList: List<String> = emptyList()
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
            .onEach { discovered ->
                _uiState.update { it.copy(discoveredIds = discovered.map { it.id }.toSet()) }
            }
            .launchIn(viewModelScope)
    }

    private fun loadPokemonList() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                val list = repository.getPokemonList()
                _uiState.update { it.copy(pokemonList = list.map { it.name }, totalPokemonCount = list.size) }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
