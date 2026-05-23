package com.watsidev.pokeguessredux.data.local

import androidx.room.*

@Dao
interface PokemonDao {
    // Pokemon Details
    @Query("SELECT * FROM pokemon_details WHERE name = :name")
    suspend fun getPokemonByName(name: String): PokemonEntity?

    @Query("SELECT * FROM pokemon_details WHERE id = :id")
    suspend fun getPokemonById(id: Int): PokemonEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPokemon(pokemon: PokemonEntity)

    // Pokemon List (for offline search)
    @Query("SELECT * FROM pokemon_list")
    suspend fun getAllPokemonList(): List<PokemonListEntity>

    @Query("SELECT * FROM pokemon_list WHERE name LIKE :query || '%'")
    suspend fun searchPokemonNames(query: String): List<PokemonListEntity>

    @Query("SELECT * FROM pokemon_list WHERE generation = :gen")
    suspend fun getPokemonByGeneration(gen: Int): List<PokemonListEntity>

    @Query("SELECT * FROM pokemon_list WHERE name LIKE :query || '%' AND generation = :gen")
    suspend fun searchPokemonByGeneration(query: String, gen: Int): List<PokemonListEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPokemonList(list: List<PokemonListEntity>)

    @Query("SELECT COUNT(*) FROM pokemon_list")
    suspend fun getPokemonListCount(): Int
}
