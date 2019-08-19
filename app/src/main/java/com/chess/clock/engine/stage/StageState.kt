package com.chess.clock.engine.stage

sealed class StageState
object StageIdle : StageState()
object StageBegan : StageState()
object StageEnded : StageState()

fun StageState.toInteger(): Int {
    return when (this) {
        is StageIdle -> 0
        is StageBegan -> 1
        is StageEnded -> 2
    }
}

fun stateFromInt(value: Int): StageState {
    return when (value) {
        0 -> StageIdle
        1 -> StageBegan
        else -> StageEnded
    }
}