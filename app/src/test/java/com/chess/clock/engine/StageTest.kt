package com.chess.clock.engine

import android.os.Parcel
import com.chess.clock.engine.stage.Stage
import com.chess.clock.engine.stage.StageBegan
import com.chess.clock.engine.stage.StageTypeGame
import com.chess.clock.engine.stage.StageTypeMoves
import com.chess.clock.engine.stage.toInteger
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.times

class StageTest {

    @Test
    fun happyPathConstructionWithoutMoves() {
        val expectedId = 2
        val expectedDuration = 500L
        val a = Stage(expectedId, expectedDuration)
        assert(a.id == expectedId)
        assert(a.totalMoves == 0)
        assert(a.duration == expectedDuration)
        assert(a.stageType == StageTypeGame)
    }

    @Test
    fun happyPathConstructionWithMoves() {
        val expectedId = 2
        val expectedDuration = 500L
        val expectedMoves = 10
        val a = Stage(expectedId, expectedDuration, expectedMoves)
        assert(a.id == expectedId)
        assert(a.totalMoves == expectedMoves)
        assert(a.duration == expectedDuration)
        assert(a.stageType == StageTypeMoves)
    }

    @Test
    fun passingZeroMovesToConstructorSetsTypeToGame() {
        val a = Stage(1, 500, 0)
        assert(a.stageType == StageTypeGame)
    }

    @Test
    fun setIdHappyPath() {
        val expectedId = 2
        val a = Stage(expectedId - 1, 500)
        a.id = expectedId
        assert(a.id == expectedId)
    }

    @Test
    fun setIdToInvalidValueDoesNotWork() {
        val expectedId = 2
        val invalidId = 3
        val a = Stage(expectedId, 500)
        a.id = invalidId
        assert(a.id == expectedId)
    }

    @Test
    fun setDuration() {
        val expectedDuration = 100L
        val a = Stage(1, expectedDuration * 2)
        a.duration = expectedDuration
        assert(a.duration == expectedDuration)
    }

    @Test
    fun setMoves() {
        val expectedMoves = 40
        val a = Stage(1, 100)
        a.setMoves(expectedMoves)
        assert(a.totalMoves == expectedMoves)
    }

    @Test
    fun getStageMoveCountIsInitiallyZero() {
        val a = Stage(1, 100)
        assert(a.stageMoveCount == 0)
    }

    @Test
    fun isEqualHappyPath() {
        val a = Stage(1, 500, 40)
        val b = Stage(1, 500, 40)
        assert(a.isEqual(b))
    }

    @Test
    fun isEqualFalseIfIdsDiffer() {
        val a = Stage(1, 500, 40)
        val b = Stage(2, 500, 40)
        assert(!a.isEqual(b))
    }

    @Test
    fun isEqualFalseIfDurationsDiffer() {
        val a = Stage(1, 500, 40)
        val b = Stage(1, 1000, 40)
        assert(!a.isEqual(b))
    }

    @Test
    fun isEqualFalseIfMoveAmountsDiffer() {
        val a = Stage(1, 500, 40)
        val b = Stage(1, 500, 20)
        assert(!a.isEqual(b))
    }

    @Test
    fun setStageType() {
        val a = Stage(1, 500)
        a.stageType = StageTypeMoves
        assert(a.stageType == StageTypeMoves)
    }

    @Test
    fun setStageTypeDoesNotResetMoveCountIfTypeIsMoves() {
        val expectedMoves = 40
        val a = Stage(1, 500, expectedMoves)
        a.stageType = StageTypeMoves
        assert(a.totalMoves == expectedMoves)
    }

    @Test
    fun setStageTypeResetsMoveCountIfTypeIsGame() {
        val a = Stage(1, 500, 40)
        a.stageType = StageTypeGame
        assert(a.totalMoves == 0)
    }

    @Test
    fun addingMoveIncreasesMoveCounter() {
        val a = Stage(1, 500, 40)
        a.addMove()
        assert(a.stageMoveCount == 1)
    }

    @Test(expected = Stage.GameStageException::class)
    fun addingTooManyMovesThrowsAnException() {
        val a = Stage(1, 500, 1)
        a.addMove()
        a.addMove()
    }

    @Test
    fun addingAllMovesCausesListenerToBeCalled() {
        val expectedId = 2
        val listener = Mockito.mock(Stage.OnStageFinishListener::class.java)
        val a = Stage(expectedId, 500, 1)
        a.setStageListener(listener)
        a.addMove()
        Mockito.verify(listener, times(1)).onStageFinished(expectedId)
    }

    @Test
    fun reset() {
        val a = Stage(1, 500, 40)
        a.addMove()
        a.reset()
        assert(a.stageMoveCount == 0)
    }

    @Test
    fun formatTimeWithMoves() {
        val durationInMilli = 5000000L
        val a = Stage(1, durationInMilli, 40)
        val expectedString = "40 moves in 01:23:20"
        val actualString = a.toString()
        assert(expectedString == actualString)
    }

    @Test
    fun formatTimeWithoutMoves() {
        val durationInMilli = 5000000L
        val a = Stage(1, durationInMilli)
        val expectedString = "Game in 01:23:20"
        val actualString = a.toString()
        assert(expectedString == actualString)
    }

    @Test
    fun getTime() {
        val durationInMilli = 5000000L
        val expectedHours = 1
        val expectedMinutes = 23
        val expectedSeconds = 20

        val a = Stage(1, durationInMilli)
        val duration = a.time

        assert(duration[0] == expectedHours)
        assert(duration[1] == expectedMinutes)
        assert(duration[2] == expectedSeconds)
    }

    @Test
    fun writeToParcelDoesntThrowException() {
        val parcel = Mockito.mock(Parcel::class.java)
        val a = Stage(1, 500, 40)
        a.writeToParcel(parcel, 0)
    }

    @Test
    fun clone() {
        val expectedId = 1
        val expectedDuration = 500L
        val expectedMoves = 20
        val a = Stage(expectedId, expectedDuration, expectedMoves)
        val b = a.clone()

        assert(a.stageType == b.stageType)

        a.id = expectedId + 1
        a.duration = expectedDuration * 2
        a.setMoves(expectedMoves * 2)

        assert(b.id == expectedId)
        assert(b.duration == expectedDuration)
        assert(b.totalMoves == expectedMoves)
    }

    @Test
    fun canCreateFromParcel() {
        val parcel = Mockito.mock(Parcel::class.java)
        val expectedId = 1
        val expectedDuration = 50L
        val expectedMoves = 40
        val expectedStageMoveCount = 20
        val expectedStageType = StageTypeMoves

        Mockito.`when`(parcel.readInt())
                .thenReturn(expectedId)
                .thenReturn(expectedMoves)
                .thenReturn(expectedStageMoveCount)
                .thenReturn(StageBegan.toInteger())
                .thenReturn(expectedStageType.toInteger())
        Mockito.`when`(parcel.readLong()).thenReturn(expectedDuration)

        val a = Stage(parcel)
        assert(a.id == expectedId)
        assert(a.duration == expectedDuration)
        assert(a.totalMoves == expectedMoves)
        assert(a.stageMoveCount == expectedStageMoveCount)
        assert(a.stageType == expectedStageType)
    }
}