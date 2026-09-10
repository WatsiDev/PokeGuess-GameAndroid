package com.watsidev.pokeguessredux.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface DiscoveryDao {
    @Query("SELECT * FROM discovered_pokemon")
    fun getAllDiscovered(): Flow<List<DiscoveryEntity>>

    @Query("SELECT * FROM discovered_pokemon WHERE id = :id")
    suspend fun getDiscoveryById(id: Int): DiscoveryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiscovery(discovery: DiscoveryEntity)

    @Transaction
    suspend fun upsertDiscovery(id: Int, name: String, isShiny: Boolean, isDaily: Boolean) {
        val existing = getDiscoveryById(id)
        val updated = if (existing != null) {
            existing.copy(
                isNormal = existing.isNormal || !isShiny,
                isShiny = existing.isShiny || isShiny,
                isDaily = existing.isDaily || isDaily
            )
        } else {
            DiscoveryEntity(
                id = id,
                name = name,
                isNormal = !isShiny,
                isShiny = isShiny,
                isDaily = isDaily
            )
        }
        insertDiscovery(updated)
    }

    @Query("SELECT EXISTS(SELECT 1 FROM discovered_pokemon WHERE id = :id)")
    suspend fun isDiscovered(id: Int): Boolean

    @Query("DELETE FROM discovered_pokemon")
    suspend fun clearAll()
}
