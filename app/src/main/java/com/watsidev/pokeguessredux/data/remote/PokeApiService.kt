package com.watsidev.pokeguessredux.data.remote

import com.watsidev.pokeguessredux.data.model.EvolutionChainResponse
import com.watsidev.pokeguessredux.data.model.PokemonResponse
import com.watsidev.pokeguessredux.data.model.PokemonSpeciesResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PokeApiService {
    @GET("pokemon/{name}")
    suspend fun getPokemon(@Path("name") name: String): PokemonResponse

    @GET("pokemon-species/{name}")
    suspend fun getPokemonSpecies(@Path("name") name: String): PokemonSpeciesResponse

    @GET("evolution-chain/{id}")
    suspend fun getEvolutionChain(@Path("id") id: String): EvolutionChainResponse

    @GET("pokemon")
    suspend fun getPokemonList(
        @Query("limit") limit: Int = 1025,
        @Query("offset") offset: Int = 0
    ): PokemonListResponse

    @GET("item/{name}")
    suspend fun getItem(@Path("name") name: String): ItemResponse

    @GET("item")
    suspend fun getItemList(
        @Query("limit") limit: Int = 2110,
        @Query("offset") offset: Int = 0
    ): PokemonListResponse
}

data class ItemResponse(
    val name: String,
    val cost: Int,
    val category: NamedApiResourceShort,
    val attributes: List<NamedApiResourceShort>,
    val sprites: ItemSprites
)

data class ItemSprites(
    val default: String?
)

data class PokemonListResponse(
    val results: List<NamedApiResourceShort>
)

data class NamedApiResourceShort(
    val name: String,
    val url: String
)
