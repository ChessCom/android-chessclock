package com.chess.clock.engine

import android.os.Parcel
import com.chess.clock.engine.stage.Stage
import com.chess.clock.engine.stage.StageManager
import com.chess.clock.engine.stage.StageManagerListener
import com.chess.clock.engine.stage.StageTypeGame
import com.chess.clock.engine.stage.StageTypeMoves
import com.chess.clock.engine.time.TimeIncrement
import com.chess.clock.engine.time.TimeIncrementBronstein
import com.chess.clock.engine.time.TimeIncrementDelay
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.mockito.Mockito.times

class StageManagerTest {

    private lateinit var testClass: StageManager
    private val stage1 = Mockito.mock(Stage::class.java)
    private val stage2 = Mockito.mock(Stage::class.java)
    private val stage3 = Mockito.mock(Stage::class.java)
    private val stages = arrayOf(stage1, stage2, stage3)

    @Before
    fun setup() {
        testClass = StageManager(stages)
    }

    @Test
    fun getTotalMoveCountDefaultsToZero() {
        assert(testClass.totalMoveCount == 0)
    }

    @Test
    fun getTotalStages() {
        assert(testClass.amountOfStages == 3)
    }

    @Test
    fun getCurrentStageDefaultsToFirstStage() {
        assert(testClass.getCurrentStage() == stage1)
        assert(testClass.getCurrentStage() != stage2)
        assert(testClass.getCurrentStage() != stage3)
    }

    @Test
    fun getStages() {
        assert(testClass.stages.contentEquals(stages))
    }

    @Test
    fun addNewStageDoesntWorkIfAlready3Stages() {
        assert(testClass.amountOfStages == 3)
        testClass.addNewStage()
        assert(testClass.amountOfStages == 3)
    }

    @Test
    fun addingNewStageToManagerWithNoStagesAddaAStage() {
        val stageManager = StageManager(emptyArray())
        stageManager.addNewStage()
        assert(stageManager.amountOfStages == 1)
    }

    @Test
    fun addingNewStageHappyPath() {
        val stageManager = StageManager(arrayOf(stage1))
        stageManager.addNewStage()
        stageManager.addNewStage()

        assert(stageManager.amountOfStages == 3)
        assert(stageManager.stages[0].stageType == StageTypeMoves)
        assert(stageManager.stages[1].stageType == StageTypeMoves)
        assert(stageManager.stages[2].stageType == StageTypeGame)
    }

    @Test
    fun removingNonExistentStagesDoesNothing() {
        testClass.removeStage(-1)
        testClass.removeStage(3)
        testClass.removeStage(4)
        assert(testClass.amountOfStages == 3)
    }

    @Test
    fun canRemoveFirstStage() {
        testClass.removeStage(0)
        assert(testClass.stages.contentEquals(arrayOf(stage2, stage3)))
    }

    @Test
    fun canRemoveMiddleStage() {
        testClass.removeStage(1)
        assert(testClass.stages.contentEquals(arrayOf(stage1, stage3)))
    }

    @Test
    fun canRemoveLastStage() {
        testClass.removeStage(2)
        assert(testClass.stages.contentEquals(arrayOf(stage1, stage2)))
    }

    @Test
    fun removingLastStageSetsPreviousStageToGameType() {
        testClass.stages[1].setStageType(StageTypeMoves)
        testClass.removeStage(2)
        assert(testClass.stages[1].stageType == StageTypeGame)
        assert(testClass.stages[1].totalMoveCount == 0)
    }

    @Test
    fun removingMiddleStageCorrectlyUpdatesStageIds() {
        val stageManager = StageManager(emptyArray())
        stageManager.addNewStage()
        stageManager.addNewStage()
        stageManager.addNewStage()

        testClass.removeStage(1)

        assert(testClass.stages[0].id == 0)
        assert(testClass.stages[1].id == 1)
    }

    @Test
    fun removingFirstStageCorrectlyUpdatesStageIds() {
        val stageManager = StageManager(emptyArray())
        stageManager.addNewStage()
        stageManager.addNewStage()
        stageManager.addNewStage()

        testClass.removeStage(0)

        assert(testClass.stages[0].id == 0)
        assert(testClass.stages[1].id == 1)
    }

    @Test
    fun removingLastStageRetainsCorrectStageIds() {
        val stageManager = StageManager(emptyArray())
        stageManager.addNewStage()
        stageManager.addNewStage()
        stageManager.addNewStage()

        testClass.removeStage(2)

        assert(testClass.stages[0].id == 0)
        assert(testClass.stages[1].id == 1)
    }

    @Test
    fun addMoveIncreasesTotalMoveCount() {
        val a = StageManager(emptyArray())
        a.addNewStage()
        val totalMoveCount = a.totalMoveCount
        a.addMove()
        assert(a.totalMoveCount == totalMoveCount + 1)
    }

    @Test
    fun addMoveTriggersCallback() {
        val listener = Mockito.mock(StageManagerListener::class.java)
        val a = StageManager(emptyArray())
        a.addNewStage()
        a.stageManagerListener = listener
        a.addMove()
        Mockito.verify(listener, times(1)).onTotalMoveCountChange(1)
    }

    @Test
    fun resetSetsMoveCountTo0() {
        val a = StageManager(emptyArray())
        a.addNewStage()
        a.addMove()
        a.reset()
        assert(a.totalMoveCount == 0)
    }

    @Test
    fun resetTriggersCallback() {
        val listener = Mockito.mock(StageManagerListener::class.java)
        val a = StageManager(emptyArray())
        a.addNewStage()
        a.stageManagerListener = listener
        a.reset()
        Mockito.verify(listener, times(1)).onTotalMoveCountChange(0)
    }

    @Test
    fun writeToParcelWritesCorrectData() {
        val parcel = Mockito.mock(Parcel::class.java)
        val argumentCaptor = ArgumentCaptor.forClass(Int::class.java)
        testClass.writeToParcel(parcel, 0)
        Mockito.verify(parcel, times(2))
                .writeInt(argumentCaptor.capture())
        assert(argumentCaptor.allValues[1] == testClass.totalMoveCount)
        Mockito.verify(parcel).writeTypedList(Mockito.anyList())
    }

    @Test
    fun cloneWorksCorrectly() {
        val a = TimeIncrement(TimeIncrementBronstein, 50)
        val b = a.clone()

        assert(a.type == b.type)
        assert(a.valueInMilliseconds == b.valueInMilliseconds)

        a.type = TimeIncrementDelay
        assert(a.type != b.type)
    }

    @Test(expected = IllegalStateException::class)
    fun onStageFinishedThrowsExceptionIfCalledWithDifferentStageThanCurrent() {
        testClass.onStageFinished(10)
    }

    @Test
    fun onStageFinishedCallsListener() {
        val listener = Mockito.mock(StageManagerListener::class.java)
        testClass.stageManagerListener = listener
        testClass.onStageFinished(0)
        Mockito.verify(listener, times(1)).onNewStageStarted(stage2)
    }

    @Test
    fun onStageFinishedUpdatesCurrentStage() {
        val listener = Mockito.mock(StageManagerListener::class.java)
        testClass.stageManagerListener = listener
        testClass.onStageFinished(0)
        assert(testClass.getCurrentStage() != stage1)
        assert(testClass.getCurrentStage() == stage2)
        assert(testClass.getCurrentStage() != stage3)
    }
}