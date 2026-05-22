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
import java.util.*
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
            return pokemonDao.getAllPokemonList().map {
                NamedApiResourceShort(it.name, it.url)
            }
        }

        return try {
            val response = apiService.getPokemonList(limit = 1025)
            val entities = response.results.map { 
                val id = it.url.split("/").filter { s -> s.isNotEmpty() }.last().toInt()
                PokemonListEntity(id, it.name, it.url)
            }
            pokemonDao.insertPokemonList(entities)
            response.results
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
        val currentLanguage = Locale.getDefault().language
        
        // 1. Check Memory Cache
        pokemonCache[name]?.let { 
            // We can't easily check language in memory without model update, 
            // but Room check will handle it if we clear cache or just trust Room.
            return it 
        }

        // 2. Check Room DB
        val localPokemon = pokemonDao.getPokemonByName(name)
        if (localPokemon != null && localPokemon.languageCode == currentLanguage) {
            val pokemon = mapEntityToModel(localPokemon)
            pokemonCache[name] = pokemon
            return pokemon
        }

        // 3. Fetch from API
        return try {
            val response = apiService.getPokemon(name)
            
            // Extract species name from response
            val speciesName = response.species.name
            val speciesResponse = apiService.getPokemonSpecies(speciesName)
            
            // Fetch evolution chain
            val evolutionChainUrl = speciesResponse.evolutionChain.url
            val chainId = evolutionChainUrl.split("/").filter { it.isNotEmpty() }.last()
            val evolutionChain = apiService.getEvolutionChain(chainId)
            
            val stage = findEvolutionStage(evolutionChain.chain, response.name)
            
            // Localized Category
            val category = speciesResponse.genera.find { it.language.name == currentLanguage }?.genus 
                ?: speciesResponse.genera.find { it.language.name == "en" }?.genus 
                ?: ""

            // Localized Stats
            val localizedStats = response.stats.map { statSlot ->
                val statId = statSlot.stat.url.split("/").filter { it.isNotEmpty() }.last()
                val statDetail = apiService.getStat(statId)
                val localizedName = statDetail.names.find { it.language.name == currentLanguage }?.name
                    ?: statDetail.names.find { it.language.name == "en" }?.name
                    ?: statSlot.stat.name
                
                PokemonStat(localizedName, statSlot.baseStat)
            }

            // Localized Evolution Steps
            val evolutionSteps = flattenEvolutionChain(evolutionChain.chain, currentLanguage)

            val genName = speciesResponse.generation.name
            val genNumber = parseGeneration(genName)

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
                stats = localizedStats,
                evolutionChain = evolutionSteps
            )
            
            // 4. Save to Room
            pokemonDao.insertPokemon(mapModelToEntity(pokemon, currentLanguage))
            
            pokemonCache[name] = pokemon
            pokemon
        } catch (e: Exception) {
            Log.e("Repo", "Error fetching Pokémon details for $name", e)
            Pokemon(id = 0, name = name, height = 0, weight = 0, types = emptyList(), evolutionaryStage = 1, generation = 0, imageUrl = null)
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

    private fun mapModelToEntity(model: Pokemon, language: String): PokemonEntity {
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
            evolutionChain = converters.fromEvolutionList(model.evolutionChain),
            languageCode = language
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

    private fun flattenEvolutionChain(link: ChainLink, language: String): List<EvolutionStep> {
        val steps = mutableListOf<EvolutionStep>()
        
        fun processLink(current: ChainLink) {
            val id = current.species.url.split("/").filter { it.isNotEmpty() }.last().toInt()
            val detail = current.evolutionDetails?.firstOrNull()
            
            var triggerName = detail?.trigger?.name ?: ""
            if (triggerName.isNotEmpty()) {
                // Fetch localized trigger name
                // Note: In a production app, we might want to pre-fetch all triggers 
                // or cache them specifically to avoid many network calls here.
                runBlocking {
                    try {
                        val triggerId = detail?.trigger?.url?.split("/")?.filter { it.isNotEmpty() }?.last()
                        if (triggerId != null) {
                            val triggerDetail = apiService.getEvolutionTrigger(triggerId)
                            triggerName = triggerDetail.names.find { it.language.name == language }?.name
                                ?: triggerDetail.names.find { it.language.name == "en" }?.name
                                ?: triggerName
                        }
                    } catch (e: Exception) {
                        // Keep original triggerName
                    }
                }
            }
            
            steps.add(EvolutionStep(
                id = id,
                name = current.species.name,
                trigger = triggerName,
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
