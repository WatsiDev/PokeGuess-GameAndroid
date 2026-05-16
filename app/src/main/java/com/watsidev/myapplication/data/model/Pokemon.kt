package com.watsidev.myapplication.data.model

data class Pokemon(
    val id: Int,
    val name: String,
    val height: Int, // decimetres
    val weight: Int, // hectograms
    val types: List<String>,
    val abilities: List<String>,
    val eggGroups: List<String>,
    val generation: Int,
    val imageUrl: String?
)
