package com.watsidev.myapplication.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "discovered_pokemon")
data class DiscoveryEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val discoveredAt: Long = System.currentTimeMillis()
)
