package com.chess.clock.engine

import android.os.Parcel
import com.chess.clock.engine.time.TimeIncrement
import com.chess.clock.engine.time.TimeIncrementBronstein
import com.chess.clock.engine.time.TimeIncrementDelay
import com.chess.clock.engine.time.TimeIncrementFischer
import com.chess.clock.engine.time.toInteger
import org.junit.Test
import org.mockito.Mockito

internal class TimeIncrementTest {

    @Test
    fun getTypeReturnsCorrectType() {
        val a = TimeIncrement(TimeIncrementDelay, 50)
        val b = TimeIncrement(TimeIncrementBronstein, 50)
        val c = TimeIncrement(TimeIncrementFischer, 50)

        assert(a.type == TimeIncrementDelay)
        assert(a.type != TimeIncrementBronstein)
        assert(a.type != TimeIncrementFischer)
        assert(b.type != TimeIncrementDelay)
        assert(b.type == TimeIncrementBronstein)
        assert(b.type != TimeIncrementFischer)
        assert(c.type != TimeIncrementDelay)
        assert(c.type != TimeIncrementBronstein)
        assert(c.type == TimeIncrementFischer)
    }

    @Test
    fun setTypeCorrectlySetsType() {
        val a = TimeIncrement(TimeIncrementDelay, 50)
        a.type = TimeIncrementBronstein
        assert(a.type != TimeIncrementDelay)
        assert(a.type == TimeIncrementBronstein)
    }

    @Test
    fun getValueReturnsCorrectValue() {
        val a = TimeIncrement(TimeIncrementDelay, 50)
        assert(a.value == 50L)
    }

    @Test
    fun setValueSetsValue() {
        val a = TimeIncrement(TimeIncrementDelay, 50)
        a.value = 10L
        assert(a.value != 50L)
        assert(a.value == 10L)
    }

    @Test
    fun getDuration() {
        val durationInMilli = 5000000L
        val expectedHours = 1
        val expectedMinutes = 23
        val expectedSeconds = 20

        val a = TimeIncrement(TimeIncrementDelay, durationInMilli)
        val duration = a.duration

        assert(duration[0] == expectedHours)
        assert(duration[1] == expectedMinutes)
        assert(duration[2] == expectedSeconds)
    }

    @Test
    fun equalObjectsAreEqual() {
        val a = TimeIncrement(TimeIncrementFischer, 50)
        val b = TimeIncrement(TimeIncrementFischer, 50)

        assert(a.isEqual(b))
    }

    @Test
    fun differentIncrementTypesAreNotEqual() {
        val a = TimeIncrement(TimeIncrementBronstein, 50)
        val b = TimeIncrement(TimeIncrementFischer, 50)

        assert(!a.isEqual(b))
    }

    @Test
    fun differentValuesAreNotEqual() {
        val a = TimeIncrement(TimeIncrementBronstein, 50)
        val b = TimeIncrement(TimeIncrementBronstein, 10)

        assert(!a.isEqual(b))
    }

    @Test
    fun toStringFormatsCorrectly() {
        val durationInMilli = 5000000L
        val a = TimeIncrement(TimeIncrementBronstein, durationInMilli)

        val expectedString = "Bronstein, 01:23:20"

        val actualString = a.toString()
        assert(expectedString == actualString)
    }

    @Test
    fun writeToParcelDoesntThrowException() {
        val parcel = Mockito.mock(Parcel::class.java)
        val a = TimeIncrement(TimeIncrementBronstein, 50)
        a.writeToParcel(parcel, 0)
    }

    @Test
    fun cloneWorksCorrectly() {
        val a = TimeIncrement(TimeIncrementBronstein, 50)
        val b = a.clone()

        assert(a.type == b.type)
        assert(a.value == b.value)

        a.type = TimeIncrementDelay
        assert(a.type != b.type)
    }

    @Test
    fun canCreateFromParcel() {
        val parcel = Mockito.mock(Parcel::class.java)
        val expectedType = TimeIncrementFischer
        val expectedValue = 50L

        Mockito.`when`(parcel.readInt()).thenReturn(TimeIncrementFischer.toInteger())
        Mockito.`when`(parcel.readLong()).thenReturn(expectedValue)

        val a = TimeIncrement(parcel)
        assert(a.type == expectedType)
        assert(a.value == expectedValue)
    }
}