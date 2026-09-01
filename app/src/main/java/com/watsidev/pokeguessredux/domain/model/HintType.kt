package com.watsidev.pokeguessredux.domain.model

enum class HintType(val labelResId: Int) {
    GENERATION(com.watsidev.pokeguessredux.R.string.attr_gen),
    EVOLUTIONARY_STAGE(com.watsidev.pokeguessredux.R.string.attr_stage),
    TYPES(com.watsidev.pokeguessredux.R.string.attr_type),
    HEIGHT(com.watsidev.pokeguessredux.R.string.attr_height),
    WEIGHT(com.watsidev.pokeguessredux.R.string.attr_weight)
}
