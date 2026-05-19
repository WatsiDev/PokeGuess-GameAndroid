package com.watsidev.pokeguessredux.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.watsidev.pokeguessredux.data.model.EvolutionStep
import com.watsidev.pokeguessredux.data.model.PokemonStat

@Entity(tableName = "pokemon_details")
data class PokemonEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val types: String, // Comma-separated list
    val evolutionaryStage: Int,
    val generation: Int,
    val imageUrl: String?,
    val category: String,
    val stats: String, // JSON
    val evolutionChain: String // JSON
)

@Entity(tableName = "pokemon_list")
data class PokemonListEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val url: String
)

class PokedexConverters {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @TypeConverter
    fun fromStatList(value: List<PokemonStat>): String {
        val type = Types.newParameterizedType(List::class.java, PokemonStat::class.java)
        return moshi.adapter<List<PokemonStat>>(type).toJson(value)
    }

    @TypeConverter
    fun toStatList(value: String): List<PokemonStat> {
        val type = Types.newParameterizedType(List::class.java, PokemonStat::class.java)
        return moshi.adapter<List<PokemonStat>>(type).fromJson(value) ?: emptyList()
    }

    @TypeConverter
    fun fromEvolutionList(value: List<EvolutionStep>): String {
        val type = Types.newParameterizedType(List::class.java, EvolutionStep::class.java)
        return moshi.adapter<List<EvolutionStep>>(type).toJson(value)
    }

    @TypeConverter
    fun toEvolutionList(value: String): List<EvolutionStep> {
        val type = Types.newParameterizedType(List::class.java, EvolutionStep::class.java)
        return moshi.adapter<List<EvolutionStep>>(type).fromJson(value) ?: emptyList()
    }
}
