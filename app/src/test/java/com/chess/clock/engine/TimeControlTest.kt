package com.chess.clock.engine

import android.os.Parcel
import com.chess.clock.engine.stage.Stage
import com.chess.clock.engine.stage.StageTypeGame
import com.chess.clock.engine.stage.StageTypeMoves
import com.chess.clock.engine.time.TimeControl
import com.chess.clock.engine.time.TimeControlListener
import com.chess.clock.engine.time.TimeIncrement
import com.chess.clock.engine.time.TimeIncrementDelay
import com.chess.clock.engine.time.TimeIncrementFischer
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.times

class TimeControlTest {

    private val name = "Stage Name"
    private val stage1 = Mockito.mock(Stage::class.java)
    private val stage2 = Mockito.mock(Stage::class.java)
    private val stage3 = Mockito.mock(Stage::class.java)
    private val stages = arrayOf(stage1, stage2, stage3)
    private val timeIncrement = TimeIncrement(TimeIncrementFischer, 500L)
    private lateinit var testClass: TimeControl

    @Before
    fun setup() {
        testClass = TimeControl(
                name,
                stages,
                timeIncrement
        )
        stage1.stageType = StageTypeMoves
        stage2.stageType = StageTypeMoves
        stage3.stageType = StageTypeGame
    }

    @Test
    fun isEqualTrueIfObjectsAreSame() {
        val a = TimeControl(name, stages, timeIncrement)
        val b = TimeControl(name, stages, timeIncrement)
        assert(a.isEqual(b))
    }

    @Test
    fun isEqualFalseIfNamesAreDifferent() {
        val a = TimeControl(name, stages, timeIncrement)
        val b = TimeControl(name, stages, timeIncrement)
        a.name = "New Name"
        assert(!a.isEqual(b))
    }

    @Test
    fun isEqualFalseIfStagesAreDifferent() {
        val a = TimeControl(name, stages, timeIncrement)
        val b = TimeControl(name, arrayOf(stage1), timeIncrement)
        assert(!a.isEqual(b))
    }

    @Test
    fun isEqualFalseIfTimeIncrementIsDifferent() {
        val a = TimeControl(name, stages, timeIncrement)
        val b = TimeControl(name, stages, TimeIncrement(TimeIncrementDelay, 1000L))
        assert(!a.isEqual(b))
    }

    @Test
    fun getName() {
        assert(testClass.name == name)
    }

    @Test
    fun setName() {
        val expectedName = "New Name"
        testClass.name = expectedName
        assert(testClass.name == expectedName)
    }

    @Test
    fun getTimeIncrement() {
        assert(testClass.timeIncrement == timeIncrement)
    }

    @Test
    fun getStageManager() {
        assert(testClass.stageManager.stages.contentEquals(stages))
    }

    @Test
    fun writeToParcelDoesNotThrowException() {
        val parcel = Mockito.mock(Parcel::class.java)
        testClass.writeToParcel(parcel, 0)
    }

    @Test
    fun onNewStageUpdateCallsListener() {
        val listener = Mockito.mock(TimeControlListener::class.java)
        testClass.setTimeControlListener(listener)
        testClass.onNewStageUpdate(stage2)
        Mockito.verify(listener, times(1)).onStageUpdate(stage2)
    }

    @Test
    fun onMoveCountUpdateCallsListener() {
        val listener = Mockito.mock(TimeControlListener::class.java)
        testClass.setTimeControlListener(listener)
        testClass.onMoveCountUpdate(5)
        Mockito.verify(listener, times(1)).onMoveCountUpdate(5)
    }
}