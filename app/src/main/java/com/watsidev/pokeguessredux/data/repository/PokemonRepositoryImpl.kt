package com.watsidev.pokeguessredux.data.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.watsidev.pokeguessredux.data.local.*
import com.watsidev.pokeguessredux.data.paging.PokemonPagingSource
import com.watsidev.pokeguessredux.data.model.*
import com.watsidev.pokeguessredux.data.remote.NamedApiResourceShort
import com.watsidev.pokeguessredux.data.remote.PokeApiService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PokemonRepositoryImpl @Inject constructor(
    private val apiService: PokeApiService,
    private val discoveryDao: DiscoveryDao,
    private val pokemonDao: PokemonDao
) : PokemonRepository {

    private val pokemonCache = ConcurrentHashMap<String, Pokemon>()

    override fun getPokemonPagingData(): Flow<PagingData<String>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { PokemonPagingSource(apiService) }
        ).flow
    }

    override suspend fun getPokemonList(): List<NamedApiResourceShort> {
        val localCount = pokemonDao.getPokemonListCount()
        if (localCount > 0) {
            Log.d("Repo", "Returning $localCount Pokémon from local DB")
            return pokemonDao.getAllPokemonList().map { 
                NamedApiResourceShort(it.name, it.url)
            }
        }

        return try {
            Log.d("Repo", "Fetching initial Pokémon list from API")
            val response = apiService.getPokemonList(limit = 1025)
            val entities = response.results.map { 
                val id = it.url.split("/").filter { s -> s.isNotEmpty() }.last().toInt()
                PokemonListEntity(id, it.name, it.url)
            }
            pokemonDao.insertPokemonList(entities)
            response.results
        } catch (e: Exception) {
            Log.e("Repo", "Error fetching Pokémon list", e)
            emptyList()
        }
    }
    
    override fun getDiscoveredPokemon(): Flow<List<DiscoveryEntity>> {
        Log.d("Repo", "Observing discovered Pokémon from database")
        return discoveryDao.getAllDiscovered()
    }

    override suspend fun markAsDiscovered(id: Int, name: String) {
        discoveryDao.insertDiscovery(DiscoveryEntity(id, name))
    }

    override suspend fun clearDiscovery() {
        discoveryDao.clearAll()
    }

    override suspend fun getPokemon(name: String): Pokemon {
        // 1. Check Memory Cache
        pokemonCache[name]?.let { return it }

        // 2. Check Room DB
        val localPokemon = pokemonDao.getPokemonByName(name)
        if (localPokemon != null) {
            Log.d("Repo", "Returning $name from local DB")
            val pokemon = mapEntityToModel(localPokemon)
            pokemonCache[name] = pokemon
            return pokemon
        }

        // 3. Fetch from API
        Log.d("Repo", "Fetching $name from API")
        return try {
            val response = apiService.getPokemon(name)
            val speciesResponse = apiService.getPokemonSpecies(name)
            
            // Fetch evolution chain to determine stage
            val evolutionChainUrl = speciesResponse.evolutionChain.url
            val chainId = evolutionChainUrl.split("/").filter { it.isNotEmpty() }.last()
            val evolutionChain = apiService.getEvolutionChain(chainId)
            
            val stage = findEvolutionStage(evolutionChain.chain, response.name)
            val evolutionSteps = flattenEvolutionChain(evolutionChain.chain)

            // Extract generation number from generation name
            val genName = speciesResponse.generation.name
            val genNumber = parseGeneration(genName)

            val category = speciesResponse.genera.find { it.language.name == "en" }?.genus ?: ""

            val pokemon = Pokemon(
                id = response.id,
                name = response.name,
                height = response.height,
                weight = response.weight,
                types = response.types.map { it.type.name },
                evolutionaryStage = stage,
                generation = genNumber,
                imageUrl = response.sprites.other.officialArtwork.frontDefault,
                category = category,
                stats = response.stats.map { PokemonStat(it.stat.name, it.baseStat) },
                evolutionChain = evolutionSteps
            )
            
            // 4. Save to Room
            pokemonDao.insertPokemon(mapModelToEntity(pokemon))
            
            pokemonCache[name] = pokemon
            pokemon
        } catch (e: Exception) {
            Log.e("Repo", "Error fetching Pokémon details for $name", e)
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

    private fun parseGeneration(genName: String): Int = when (genName) {
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

    private fun mapEntityToModel(entity: PokemonEntity): Pokemon {
        val converters = PokedexConverters()
        return Pokemon(
            id = entity.id,
            name = entity.name,
            height = entity.height,
            weight = entity.weight,
            types = entity.types.split(",").filter { it.isNotEmpty() },
            evolutionaryStage = entity.evolutionaryStage,
            generation = entity.generation,
            imageUrl = entity.imageUrl,
            category = entity.category,
            stats = converters.toStatList(entity.stats),
            evolutionChain = converters.toEvolutionList(entity.evolutionChain)
        )
    }

    private fun mapModelToEntity(model: Pokemon): PokemonEntity {
        val converters = PokedexConverters()
        return PokemonEntity(
            id = model.id,
            name = model.name,
            height = model.height,
            weight = model.weight,
            types = model.types.joinToString(","),
            evolutionaryStage = model.evolutionaryStage,
            generation = model.generation,
            imageUrl = model.imageUrl,
            category = model.category,
            stats = converters.fromStatList(model.stats),
            evolutionChain = converters.fromEvolutionList(model.evolutionChain)
        )
    }

    private fun findEvolutionStage(link: ChainLink, name: String, currentStage: Int = 1): Int {
        if (link.species.name == name) return currentStage
        for (nextLink in link.evolvesTo) {
            val stage = findEvolutionStage(nextLink, name, currentStage + 1)
            if (stage != -1) return stage
        }
        return -1
    }

    private fun flattenEvolutionChain(link: ChainLink): List<EvolutionStep> {
        val steps = mutableListOf<EvolutionStep>()
        
        fun processLink(current: ChainLink) {
            val id = current.species.url.split("/").filter { it.isNotEmpty() }.last().toInt()
            val detail = current.evolutionDetails?.firstOrNull()
            
            steps.add(EvolutionStep(
                id = id,
                name = current.species.name,
                trigger = detail?.trigger?.name ?: "",
                minLevel = detail?.minLevel,
                imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png"
            ))
            
            current.evolvesTo.forEach { processLink(it) }
        }
        
        processLink(link)
        return steps
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
