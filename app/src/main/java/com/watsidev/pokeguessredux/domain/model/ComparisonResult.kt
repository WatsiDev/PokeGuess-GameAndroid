package com.watsidev.pokeguessredux.domain.model

enum class MatchState {
    CORRECT, PARTIAL, INCORRECT
}

enum class Direction {
    HIGHER, LOWER, EQUAL
}

data class AttributeResult<T>(
    val value: T,
    val state: MatchState,
    val direction: Direction = Direction.EQUAL
)

data class PokemonComparison(
    val id: AttributeResult<Int>,
    val name: String,
    val imageUrl: String?,
    val types: AttributeResult<List<String>>,
    val evolutionaryStage: AttributeResult<Int>,
    val height: AttributeResult<Int>,
    val weight: AttributeResult<Int>,
    val generation: AttributeResult<Int>
)
