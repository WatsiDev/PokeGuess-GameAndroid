package com.watsidev.pokeguessredux.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        DiscoveryEntity::class,
        PokemonEntity::class,
        PokemonListEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(PokedexConverters::class)
abstract class PokeDatabase : RoomDatabase() {
    abstract fun discoveryDao(): DiscoveryDao
    abstract fun pokemonDao(): PokemonDao
}
