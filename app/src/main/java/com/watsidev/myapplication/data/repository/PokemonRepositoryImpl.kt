package com.watsidev.myapplication.data.repository

import com.watsidev.myapplication.data.model.Item
import com.watsidev.myapplication.data.model.Pokemon
import com.watsidev.myapplication.data.remote.NamedApiResourceShort
import com.watsidev.myapplication.data.remote.PokeApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PokemonRepositoryImpl @Inject constructor(
    private val apiService: PokeApiService
) : PokemonRepository {

    private val pokemonCache = mutableMapOf<String, Pokemon>()

    override suspend fun getPokemonList(): List<NamedApiResourceShort> {
        return apiService.getPokemonList().results
    }

    override suspend fun getPokemon(name: String): Pokemon {
        pokemonCache[name]?.let { return it }

        return try {
            val response = apiService.getPokemon(name)
            val speciesResponse = apiService.getPokemonSpecies(name)
            
            // Extract generation number from generation name
            val genName = speciesResponse.generation.name
            val genNumber = when (genName) {
                "generation-i" -> 1
                "generation-ii" -> 2
                "generation-iii" -> 3
                "generation-iv" -> 4
                "generation-v" -> 5
                "generation-vi" -> 6
                "generation-vii" -> 7
                "generation-viii" -> 8
                "generation-ix" -> 9
                else -> 1
            }

            val pokemon = Pokemon(
                id = response.id,
                name = response.name,
                height = response.height,
                weight = response.weight,
                types = response.types.map { it.type.name },
                abilities = response.abilities.map { it.ability.name },
                eggGroups = speciesResponse.eggGroups.map { it.name },
                generation = genNumber,
                imageUrl = response.sprites.other.officialArtwork.frontDefault
            )
            
            pokemonCache[name] = pokemon
            pokemon
        } catch (e: Exception) {
            // Fallback for failed fetches - prevents complete crash
            Pokemon(
                id = 0,
                name = name,
                height = 0,
                weight = 0,
                types = emptyList(),
                abilities = emptyList(),
                eggGroups = emptyList(),
                generation = 0,
                imageUrl = null
            )
        }
    }

    override suspend fun getPokemonDetailsParallel(names: List<String>): List<Pokemon> = coroutineScope {
        names.map { name ->
            async { getPokemon(name) }
        }.awaitAll()
    }

    override suspend fun getItemList(): List<NamedApiResourceShort> {
        return apiService.getItemList().results
    }

    override suspend fun getItem(name: String): Item {
        val response = apiService.getItem(name)
        return Item(
            name = response.name,
            cost = response.cost,
            category = response.category.name,
            attributes = response.attributes.map { it.name },
            imageUrl = response.sprites.default
        )
    }
}
