package com.chess.clock.engine.stage

/**
 * There are two stage types in a Time Control. The StageTypeGame is used for one-stage
 * Time Controls or the last stage of a multi-stage Time Control. The StageTypeMoves is used
 * for the remaining ones (multi-stage Time Controls besides the last stage).
 */
sealed class StageType

/**
 * Used for one-stage only type of game or the last stage of a multiple stage time control.
 */
object StageTypeGame : StageType() {
    override fun toString() = "Stage Type Game"
}

/**
 * Used for all stages of a the multi-stage time control, besides the last one.
 */
object StageTypeMoves : StageType() {
    override fun toString() = "Stage Type Moves"
}


fun StageType.toInteger(): Int {
    return when (this) {
        is StageTypeGame -> 0
        is StageTypeMoves -> 1
    }
}

fun typeFromInt(value: Int): StageType {
    return when (value) {
        0 -> StageTypeGame
        else -> StageTypeMoves
    }
}
