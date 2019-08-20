package com.chess.clock.engine

import android.os.Parcel
import com.chess.clock.engine.time.TimeControl
import com.chess.clock.engine.time.TimeControlWrapper
import com.chess.clock.engine.time.TimeIncrement
import com.chess.clock.engine.time.TimeIncrementDelay
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class TimeControlWrapperTest {

    private val playerOne = TimeControl("Player 1", emptyArray(), TimeIncrement(TimeIncrementDelay, 500L))
    private val playerTwo = TimeControl("Player 2", emptyArray(), TimeIncrement(TimeIncrementDelay, 500L))
    private lateinit var testClass: TimeControlWrapper

    @Before
    fun setup() {
        testClass = TimeControlWrapper(playerOne, playerTwo)
    }

    @Test
    fun getTimeControlPlayerOne() {
        assert(testClass.timeControlPlayerOne == playerOne)
    }

    @Test
    fun setTimeControlPlayerOne() {
        testClass.timeControlPlayerOne = playerTwo
        assert(testClass.timeControlPlayerOne != playerOne)
        assert(testClass.timeControlPlayerOne == playerTwo)
    }

    @Test
    fun getTimeControlPlayerTwo() {
        assert(testClass.timeControlPlayerOne == playerOne)
    }

    @Test
    fun setTimeControlPlayerTwo() {
        testClass.timeControlPlayerTwo = playerOne
        assert(testClass.timeControlPlayerTwo == playerOne)
        assert(testClass.timeControlPlayerTwo != playerTwo)
    }

    @Test
    fun isSameAsPlayerOneDefaultsToTrue() {
        assert(testClass.isSameAsPlayerOne)
    }

    @Test
    fun setSameAsPlayerOne() {
        testClass.isSameAsPlayerOne = false
        assert(!testClass.isSameAsPlayerOne)
    }

    @Test
    fun clone() {
        val a = testClass.clone()
        assert(a.isEqual(testClass))
    }

    @Test
    fun isEqualFalseIfSameAsPlayerOneDiffers() {
        val a = testClass.clone()
        a.isSameAsPlayerOne = !testClass.isSameAsPlayerOne
    }

    @Test
    fun writeToParcelDoesNotThrowException() {
        val parcel = Mockito.mock(Parcel::class.java)
        testClass.writeToParcel(parcel, 0)
    }

    @Test
    fun canCreateFromParcel() {
        val parcel = Mockito.mock(Parcel::class.java)

        Mockito.`when`(parcel.readParcelable<TimeControl>(TimeControl::class.java.classLoader))
                .thenReturn(playerOne)
                .thenReturn(playerTwo)
        Mockito.`when`(parcel.readByte()).thenReturn(1)

        val a = TimeControlWrapper(parcel)
        assert(a.timeControlPlayerOne == playerOne)
        assert(a.timeControlPlayerTwo == playerTwo)
        assert(a.isSameAsPlayerOne)
    }
}