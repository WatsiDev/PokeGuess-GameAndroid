package com.watsidev.myapplication.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [DiscoveryEntity::class], version = 1, exportSchema = false)
abstract class PokeDatabase : RoomDatabase() {
    abstract fun discoveryDao(): DiscoveryDao
}
