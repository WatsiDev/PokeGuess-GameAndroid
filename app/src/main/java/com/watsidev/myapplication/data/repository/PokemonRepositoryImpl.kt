package com.watsidev.myapplication.data.repository

import com.watsidev.myapplication.data.local.DiscoveryDao
import com.watsidev.myapplication.data.local.DiscoveryEntity
import com.watsidev.myapplication.data.model.Item
import com.watsidev.myapplication.data.model.Pokemon
import com.watsidev.myapplication.data.remote.NamedApiResourceShort
import com.watsidev.myapplication.data.remote.PokeApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PokemonRepositoryImpl @Inject constructor(
    private val apiService: PokeApiService,
    private val discoveryDao: DiscoveryDao
) : PokemonRepository {

    private val pokemonCache = ConcurrentHashMap<String, Pokemon>()
    @Volatile
    private var cachedPokemonList: List<NamedApiResourceShort>? = null

    override suspend fun getPokemonList(): List<NamedApiResourceShort> {
        cachedPokemonList?.let { return it }
        return try {
            val list = apiService.getPokemonList().results
            cachedPokemonList = list
            list
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override fun getDiscoveredPokemon(): Flow<List<DiscoveryEntity>> {
        return discoveryDao.getAllDiscovered()
    }

    override suspend fun markAsDiscovered(id: Int, name: String) {
        discoveryDao.insertDiscovery(DiscoveryEntity(id, name))
    }

    override suspend fun clearDiscovery() {
        discoveryDao.clearAll()
    }

    override suspend fun getPokemon(name: String): Pokemon {
        pokemonCache[name]?.let { return it }

        return try {
            val response = apiService.getPokemon(name)
            val speciesResponse = apiService.getPokemonSpecies(name)
            
            // Fetch evolution chain to determine stage
            val evolutionChainUrl = speciesResponse.evolutionChain.url
            val chainId = evolutionChainUrl.split("/").filter { it.isNotEmpty() }.last()
            val evolutionChain = apiService.getEvolutionChain(chainId)
            
            val stage = findEvolutionStage(evolutionChain.chain, response.name)

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
                evolutionaryStage = stage,
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
                evolutionaryStage = 1,
                generation = 0,
                imageUrl = null
            )
        }
    }

    private fun findEvolutionStage(link: com.watsidev.myapplication.data.model.ChainLink, name: String, currentStage: Int = 1): Int {
        if (link.species.name == name) return currentStage
        for (nextLink in link.evolvesTo) {
            val stage = findEvolutionStage(nextLink, name, currentStage + 1)
            if (stage != -1) return stage
        }
        return -1
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
