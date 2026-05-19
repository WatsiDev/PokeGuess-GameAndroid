package com.watsidev.pokeguessredux.data.model

data class Item(
    val name: String,
    val cost: Int,
    val category: String,
    val attributes: List<String>,
    val imageUrl: String?
)
