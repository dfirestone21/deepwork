package com.example.deepwork.domain.usecase.session

import com.example.deepwork.domain.model.ScheduledTimeBlock
import com.example.deepwork.domain.model.SessionWarning
import com.example.deepwork.domain.model.WarningLevel
import com.example.deepwork.domain.model.WarningType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class CalculateSessionWarningsUseCaseTest {

    private lateinit var useCase: CalculateSessionWarningsUseCase

    @Before
    fun setUp() {
        useCase = CalculateSessionWarningsUseCase()
    }

    @Test
    fun `when a work block over 60m has no break immediately following it, should return a Yellow LONG_WORK_STRETCH warning`() {
        // given
        val timeBlocks = listOf(
            ScheduledTimeBlock.deepWorkBlock(duration = 65.minutes, position = 0),
            ScheduledTimeBlock.deepWorkBlock(duration = 25.minutes, position = 1)
        )

        // when
        val warnings = useCase(timeBlocks)

        // then
        assertTrue(warnings.any { it == SessionWarning(WarningLevel.YELLOW, WarningType.LONG_WORK_STRETCH) })
    }

    @Test
    fun `when a work block over 60m is the last block, should return a Yellow LONG_WORK_STRETCH warning`() {
        // given
        val timeBlocks = listOf(
            ScheduledTimeBlock.deepWorkBlock(duration = 65.minutes, position = 0)
        )

        // when
        val warnings = useCase(timeBlocks)

        // then
        assertTrue(warnings.any { it == SessionWarning(WarningLevel.YELLOW, WarningType.LONG_WORK_STRETCH) })
    }

    @Test
    fun `when a break under 5m immediately follows a work block 60m or more, should return a Yellow SHORT_BREAK warning`() {
        // given
        val timeBlocks = listOf(
            ScheduledTimeBlock.deepWorkBlock(duration = 60.minutes, position = 0),
            ScheduledTimeBlock.breakBlock(duration = 4.minutes, position = 1)
        )

        // when
        val warnings = useCase(timeBlocks)

        // then
        assertTrue(warnings.any { it == SessionWarning(WarningLevel.YELLOW, WarningType.SHORT_BREAK) })
    }

    @Test
    fun `when a break over 30m separates work blocks that are each under 60m, should return a Blue LONG_BREAK warning`() {
        // given
        val timeBlocks = listOf(
            ScheduledTimeBlock.deepWorkBlock(duration = 25.minutes, position = 0),
            ScheduledTimeBlock.breakBlock(duration = 31.minutes, position = 1),
            ScheduledTimeBlock.deepWorkBlock(duration = 25.minutes, position = 2)
        )

        // when
        val warnings = useCase(timeBlocks)

        // then
        assertTrue(warnings.any { it == SessionWarning(WarningLevel.BLUE, WarningType.LONG_BREAK) })
    }

    @Test
    fun `when total break duration exceeds 1 third of total work duration, should return a Yellow HIGH_BREAK_RATIO warning`() {
        // given
        // 90m work, 31m break — 31 > 90/3 = 30
        val timeBlocks = listOf(
            ScheduledTimeBlock.deepWorkBlock(duration = 45.minutes, position = 0),
            ScheduledTimeBlock.breakBlock(duration = 31.minutes, position = 1),
            ScheduledTimeBlock.deepWorkBlock(duration = 45.minutes, position = 2)
        )

        // when
        val warnings = useCase(timeBlocks)

        // then
        assertTrue(warnings.any { it == SessionWarning(WarningLevel.YELLOW, WarningType.HIGH_BREAK_RATIO) })
    }

    @Test
    fun `when total planned work time exceeds 4 hours, should return a Blue LONG_SESSION warning`() {
        // given
        val timeBlocks = listOf(
            ScheduledTimeBlock.deepWorkBlock(duration = 2.hours, position = 0),
            ScheduledTimeBlock.breakBlock(duration = 5.minutes, position = 1),
            ScheduledTimeBlock.deepWorkBlock(duration = 2.hours + 1.minutes, position = 2)
        )

        // when
        val warnings = useCase(timeBlocks)

        // then
        assertTrue(warnings.any { it == SessionWarning(WarningLevel.BLUE, WarningType.LONG_SESSION) })
    }

    @Test
    fun `when no thresholds are exceeded, should return an empty list`() {
        // given
        val timeBlocks = listOf(
            ScheduledTimeBlock.deepWorkBlock(duration = 25.minutes, position = 0),
            ScheduledTimeBlock.breakBlock(duration = 5.minutes, position = 1),
            ScheduledTimeBlock.deepWorkBlock(duration = 25.minutes, position = 2)
        )

        // when
        val warnings = useCase(timeBlocks)

        // then
        assertEquals(emptyList<SessionWarning>(), warnings)
    }
}
