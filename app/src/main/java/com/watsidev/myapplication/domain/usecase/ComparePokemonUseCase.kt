package com.watsidev.myapplication.domain.usecase

import com.watsidev.myapplication.data.model.Pokemon
import com.watsidev.myapplication.domain.model.*
import javax.inject.Inject

class ComparePokemonUseCase @Inject constructor() {
    operator fun invoke(guess: Pokemon, target: Pokemon): PokemonComparison {
        return PokemonComparison(
            id = compareNumerical(guess.id, target.id),
            name = guess.name,
            imageUrl = guess.imageUrl,
            types = compareList(guess.types, target.types),
            abilities = compareList(guess.abilities, target.abilities),
            eggGroups = compareList(guess.eggGroups, target.eggGroups),
            height = compareNumerical(guess.height, target.height),
            weight = compareNumerical(guess.weight, target.weight),
            generation = compareNumerical(guess.generation, target.generation)
        )
    }

    private fun compareNumerical(guess: Int, target: Int): AttributeResult<Int> {
        val state = if (guess == target) MatchState.CORRECT else MatchState.INCORRECT
        val direction = when {
            guess < target -> Direction.HIGHER
            guess > target -> Direction.LOWER
            else -> Direction.EQUAL
        }
        return AttributeResult(guess, state, direction)
    }

    private fun compareList(guess: List<String>, target: List<String>): AttributeResult<List<String>> {
        val common = guess.intersect(target.toSet())
        val state = when {
            common.size == target.size && common.size == guess.size -> MatchState.CORRECT
            common.isNotEmpty() -> MatchState.PARTIAL
            else -> MatchState.INCORRECT
        }
        return AttributeResult(guess, state)
    }
}
