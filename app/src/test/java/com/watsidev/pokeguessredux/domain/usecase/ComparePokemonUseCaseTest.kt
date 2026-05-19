package com.watsidev.pokeguessredux.domain.usecase

import com.watsidev.pokeguessredux.data.model.Pokemon
import com.watsidev.pokeguessredux.domain.model.Direction
import com.watsidev.pokeguessredux.domain.model.MatchState
import org.junit.Assert.assertEquals
import org.junit.Test

class ComparePokemonUseCaseTest {

    private val useCase = ComparePokemonUseCase()

    @Test
    fun `compare same pokemon returns all correct`() {
        val pikachu = Pokemon(
            id = 25,
            name = "pikachu",
            height = 4,
            weight = 60,
            types = listOf("electric"),
            generation = 1,
            imageUrl = null
        )

        val result = useCase(pikachu, pikachu)

        assertEquals(MatchState.CORRECT, result.id.state)
        assertEquals(MatchState.CORRECT, result.types.state)
        assertEquals(MatchState.CORRECT, result.height.state)
        assertEquals(MatchState.CORRECT, result.weight.state)
        assertEquals(MatchState.CORRECT, result.generation.state)
        assertEquals(Direction.EQUAL, result.height.direction)
    }

    @Test
    fun `compare different pokemon returns incorrect and directions`() {
        val bulbasaur = Pokemon(
            id = 1,
            name = "bulbasaur",
            height = 7,
            weight = 69,
            types = listOf("grass", "poison"),
            generation = 1,
            imageUrl = null
        )

        val ivysaur = Pokemon(
            id = 2,
            name = "ivysaur",
            height = 10,
            weight = 130,
            types = listOf("grass", "poison"),
            generation = 1,
            imageUrl = null
        )

        val result = useCase(bulbasaur, ivysaur)

        assertEquals(MatchState.INCORRECT, result.id.state)
        assertEquals(Direction.HIGHER, result.id.direction)
        assertEquals(MatchState.CORRECT, result.types.state)
        assertEquals(MatchState.INCORRECT, result.height.state)
        assertEquals(Direction.HIGHER, result.height.direction)
        assertEquals(Direction.HIGHER, result.weight.direction)
    }

    @Test
    fun `compare partial types returns partial state`() {
        val charizard = Pokemon(
            id = 6,
            name = "charizard",
            height = 17,
            weight = 905,
            types = listOf("fire", "flying"),
            generation = 1,
            imageUrl = null
        )

        val moltres = Pokemon(
            id = 146,
            name = "moltres",
            height = 20,
            weight = 600,
            types = listOf("fire", "flying"),
            generation = 1,
            imageUrl = null
        )
        
        val pidgeot = Pokemon(
            id = 18,
            name = "pidgeot",
            height = 15,
            weight = 395,
            types = listOf("normal", "flying"),
            generation = 1,
            imageUrl = null
        )

        val result = useCase(charizard, pidgeot)

        assertEquals(MatchState.PARTIAL, result.types.state)
    }
}
