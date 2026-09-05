package com.watsidev.pokeguessredux.ui.pokedex

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.watsidev.pokeguessredux.data.local.DiscoveryEntity
import com.watsidev.pokeguessredux.data.model.Pokemon
import com.watsidev.pokeguessredux.data.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PokemonDetailUiState(
    val pokemon: Pokemon? = null,
    val discovery: DiscoveryEntity? = null,
    val isShowingShiny: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PokemonDetailViewModel @Inject constructor(
    private val repository: PokemonRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PokemonDetailUiState())
    val uiState: StateFlow<PokemonDetailUiState> = _uiState.asStateFlow()

    fun loadPokemon(name: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val pokemon = repository.getPokemon(name)
                
                // Observe discovery status for this pokemon
                launch {
                    repository.getDiscoveredPokemon().collect { list ->
                        val disc = list.find { it.id == pokemon.id }
                        val onlyShiny = disc != null && disc.isShiny && !disc.isNormal
                        _uiState.update { current ->
                            current.copy(
                                discovery = disc,
                                isShowingShiny = if (current.discovery == null && onlyShiny) true else current.isShowingShiny
                            )
                        }
                    }
                }

                _uiState.update { it.copy(pokemon = pokemon, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun toggleShiny() {
        val discovery = _uiState.value.discovery ?: return
        if (discovery.isNormal && discovery.isShiny) {
            _uiState.update { it.copy(isShowingShiny = !it.isShowingShiny) }
        }
    }
}
