package com.watsidev.myapplication.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PokemonResponse(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val types: List<TypeSlot>,
    val sprites: Sprites
)

@JsonClass(generateAdapter = true)
data class TypeSlot(
    val slot: Int,
    val type: NamedApiResource
)

@JsonClass(generateAdapter = true)
data class NamedApiResource(
    val name: String,
    val url: String
)

@JsonClass(generateAdapter = true)
data class Sprites(
    @Json(name = "other") val other: OtherSprites
)

@JsonClass(generateAdapter = true)
data class OtherSprites(
    @Json(name = "official-artwork") val officialArtwork: OfficialArtwork
)

@JsonClass(generateAdapter = true)
data class OfficialArtwork(
    @Json(name = "front_default") val frontDefault: String?
)

@JsonClass(generateAdapter = true)
data class PokemonSpeciesResponse(
    val id: Int,
    val name: String,
    val generation: NamedApiResource,
    @Json(name = "evolution_chain") val evolutionChain: APIResource
)

@JsonClass(generateAdapter = true)
data class APIResource(
    val url: String
)

@JsonClass(generateAdapter = true)
data class EvolutionChainResponse(
    val id: Int,
    val chain: ChainLink
)

@JsonClass(generateAdapter = true)
data class ChainLink(
    @Json(name = "is_baby") val isBaby: Boolean,
    val species: NamedApiResource,
    @Json(name = "evolves_to") val evolvesTo: List<ChainLink>
)
