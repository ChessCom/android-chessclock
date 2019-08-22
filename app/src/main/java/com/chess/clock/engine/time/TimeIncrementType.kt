package com.chess.clock.engine.time

import com.chess.clock.R


sealed class TimeIncrementType {
    abstract fun subtitleId(): Int

    companion object {
        fun fromInteger(parcelledValue: Int): TimeIncrementType {
            return when (parcelledValue) {
                0 -> TimeIncrementDelay
                1 -> TimeIncrementBronstein
                else -> TimeIncrementFischer
            }
        }
    }
}

/**
 * The player's clock starts after the delay period.
 */
object TimeIncrementDelay : TimeIncrementType() {
    override fun toString() = "Delay"
    override fun subtitleId() = R.string.delay_option_subtitle
}

/**
 * Players receive the used portion of the increment at the end of each turn.
 */
object TimeIncrementBronstein: TimeIncrementType() {
    override fun toString() = "Bronstein"
    override fun subtitleId() = R.string.bronstein_option_subtitle
}

/**
 * Players receive the used portion of the increment at the end of each turn.
 */
object TimeIncrementFischer : TimeIncrementType() {
    override fun toString() = "Fischer"
    override fun subtitleId() = R.string.fischer_option_subtitle
}

fun TimeIncrementType.toInteger(): Int {
    return when (this) {
        is TimeIncrementDelay -> 0
        is TimeIncrementBronstein -> 1
        is TimeIncrementFischer -> 2
    }
}