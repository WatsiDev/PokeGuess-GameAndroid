package com.watsidev.pokeguessredux.data.repository

import androidx.paging.PagingData
import com.watsidev.pokeguessredux.data.local.DiscoveryEntity
import com.watsidev.pokeguessredux.data.model.Item
import com.watsidev.pokeguessredux.data.model.Pokemon
import com.watsidev.pokeguessredux.data.remote.NamedApiResourceShort
import kotlinx.coroutines.flow.Flow

interface PokemonRepository {
    fun getPokemonPagingData(): Flow<PagingData<String>>
    suspend fun getPokemonList(): List<NamedApiResourceShort>
    suspend fun getPokemonByGeneration(gen: Int): List<NamedApiResourceShort>
    suspend fun getPokemon(name: String): Pokemon
    suspend fun getPokemonDetailsParallel(names: List<String>): List<Pokemon>
    suspend fun getItemList(): List<NamedApiResourceShort>
    suspend fun getItem(name: String): Item
    
    // Discovery
    fun getDiscoveredPokemon(): Flow<List<DiscoveryEntity>>
    suspend fun markAsDiscovered(id: Int, name: String)
    suspend fun clearDiscovery()
}
