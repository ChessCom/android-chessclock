package com.chess.clock.engine.stage

/**
 * Interface definition for a callback to be invoked when the Time Control stages gets updated.
 */
interface StageManagerListener {

    /**
     * Called when the first stage begins or new one enters.
     *
     * @param stage The new stage.
     */
    fun onNewStageUpdate(stage: Stage)

    /**
     * Called when total move count is updated.
     */
    fun onMoveCountUpdate(moveCount: Int)
}