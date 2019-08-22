package com.chess.clock.engine.stage

interface StageManagerListener {
    fun onNewStageStarted(stage: Stage)
    fun onTotalMoveCountChange(moveCount: Int)
}