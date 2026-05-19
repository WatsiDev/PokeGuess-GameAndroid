package com.watsidev.pokeguessredux.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPokemonList(list: List<PokemonListEntity>)

    @Query("SELECT COUNT(*) FROM pokemon_list")
    suspend fun getPokemonListCount(): Int
}
