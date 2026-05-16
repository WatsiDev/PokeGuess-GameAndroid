package com.watsidev.myapplication.data.repository

import com.watsidev.myapplication.data.local.DiscoveryEntity
import com.watsidev.myapplication.data.model.Item
import com.watsidev.myapplication.data.model.Pokemon
import com.watsidev.myapplication.data.remote.NamedApiResourceShort
import kotlinx.coroutines.flow.Flow

interface PokemonRepository {
    suspend fun getPokemonList(): List<NamedApiResourceShort>
    suspend fun getPokemon(name: String): Pokemon
    suspend fun getPokemonDetailsParallel(names: List<String>): List<Pokemon>
    suspend fun getItemList(): List<NamedApiResourceShort>
    suspend fun getItem(name: String): Item
    
    // Discovery
    fun getDiscoveredPokemon(): Flow<List<DiscoveryEntity>>
    suspend fun markAsDiscovered(id: Int, name: String)
    suspend fun clearDiscovery()
}
