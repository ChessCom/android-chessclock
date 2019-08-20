package com.chess.clock.engine.time

import com.chess.clock.engine.stage.Stage

/**
 * Interface definition for a callback to be invoked when the player clock gets updated.
 */
interface TimeControlListener {

    /**
     * Called when new game stage begins.
     *
     * @param stage The current game stage.
     */
    fun onStageUpdate(stage: Stage)

    /**
     * Called when the move count is updated.
     */
    fun onMoveCountUpdate(moves: Int)
}