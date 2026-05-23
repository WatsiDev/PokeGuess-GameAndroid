package com.watsidev.pokeguessredux.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        DiscoveryEntity::class,
        PokemonEntity::class,
        PokemonListEntity::class
    ],
    version = 4,
    exportSchema = true
)
@TypeConverters(PokedexConverters::class)
abstract class PokeDatabase : RoomDatabase() {
    abstract fun discoveryDao(): DiscoveryDao
    abstract fun pokemonDao(): PokemonDao

    companion object {
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE pokemon_list ADD COLUMN generation INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
