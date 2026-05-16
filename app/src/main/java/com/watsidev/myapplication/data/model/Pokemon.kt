package com.watsidev.myapplication.data.model

data class Pokemon(
    val id: Int,
    val name: String,
    val height: Int, // decimetres
    val weight: Int, // hectograms
    val types: List<String>,
    val evolutionaryStage: Int,
    val generation: Int,
    val imageUrl: String?,
    val category: String = "",
    val stats: List<PokemonStat> = emptyList(),
    val evolutionChain: List<EvolutionStep> = emptyList()
)

data class PokemonStat(
    val name: String,
    val value: Int
)

data class EvolutionStep(
    val id: Int,
    val name: String,
    val trigger: String = "",
    val minLevel: Int? = null,
    val imageUrl: String = ""
)
