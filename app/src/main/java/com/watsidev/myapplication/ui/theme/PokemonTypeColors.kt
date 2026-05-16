package com.watsidev.myapplication.ui.theme

import androidx.compose.ui.graphics.Color

object PokemonTypeColors {
    val Normal = Color(0xFFA8A77A)
    val Fire = Color(0xFFEE8130)
    val Water = Color(0xFF6390F0)
    val Electric = Color(0xFFF7D02C)
    val Grass = Color(0xFF7AC74C)
    val Ice = Color(0xFF96D9D6)
    val Fighting = Color(0xFFC22E28)
    val Poison = Color(0xFFA33EA1)
    val Ground = Color(0xFFE2BF65)
    val Flying = Color(0xFFA98FF3)
    val Psychic = Color(0xFFF95587)
    val Bug = Color(0xFFA6B91A)
    val Rock = Color(0xFFB6A136)
    val Ghost = Color(0xFF735797)
    val Dragon = Color(0xFF6F35FC)
    val Steel = Color(0xFFB7B7CE)
    val Fairy = Color(0xFFD685AD)
    val Dark = Color(0xFF705746)

    fun getColorForType(type: String): Color {
        return when (type.lowercase()) {
            "normal" -> Normal
            "fire" -> Fire
            "water" -> Water
            "electric" -> Electric
            "grass" -> Grass
            "ice" -> Ice
            "fighting" -> Fighting
            "poison" -> Poison
            "ground" -> Ground
            "flying" -> Flying
            "psychic" -> Psychic
            "bug" -> Bug
            "rock" -> Rock
            "ghost" -> Ghost
            "dragon" -> Dragon
            "steel" -> Steel
            "fairy" -> Fairy
            "dark" -> Dark
            else -> Color.Gray
        }
    }
}
