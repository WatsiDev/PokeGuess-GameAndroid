package com.watsidev.myapplication.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DiscoveryDao {
    @Query("SELECT * FROM discovered_pokemon")
    fun getAllDiscovered(): Flow<List<DiscoveryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiscovery(discovery: DiscoveryEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM discovered_pokemon WHERE id = :id)")
    suspend fun isDiscovered(id: Int): Boolean

    @Query("DELETE FROM discovered_pokemon")
    suspend fun clearAll()
}
