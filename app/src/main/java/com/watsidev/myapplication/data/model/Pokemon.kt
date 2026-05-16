package com.watsidev.myapplication.data.model

data class Pokemon(
    val id: Int,
    val name: String,
    val height: Int, // decimetres
    val weight: Int, // hectograms
    val types: List<String>,
    val evolutionaryStage: Int,
    val generation: Int,
    val imageUrl: String?
)
