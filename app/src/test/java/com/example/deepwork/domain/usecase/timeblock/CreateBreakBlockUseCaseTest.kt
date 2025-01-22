package com.example.deepwork.domain.usecase.timeblock

import com.example.deepwork.domain.exception.TimeBlockException
import com.example.deepwork.domain.model.TimeBlock
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.Uuid

class CreateBreakBlockUseCaseTest {
    private lateinit var createBreakBlock: CreateBreakBlockUseCase

    @Before
    fun setup() {
        createBreakBlock = CreateBreakBlockUseCase()
    }

    @Test
    fun `when creating break block, should generate uuid`() = runTest {
        // given
        val block = TimeBlock.breakBlock()
        // when
        val createdBlock = createBreakBlock(block).getOrThrow()
        // then
        assert(createdBlock.id != Uuid.NIL)
    }

    @Test(expected = TimeBlockException.InvalidDurationTooShort::class)
    fun `when duration is less than DURATION_MIN, should throw exception`() = runTest {
        // given
        val duration = TimeBlock.BreakBlock.DURATION_MIN - 5.minutes
        val block = TimeBlock.breakBlock(duration)
        // when
        createBreakBlock(block).getOrThrow()
    }

    @Test(expected = TimeBlockException.InvalidDurationTooLong::class)
    fun `when duration is more than DURATION_MAX, should throw exception`() = runTest {
        // given
        val duration = TimeBlock.BreakBlock.DURATION_MAX + 5.minutes
        val block = TimeBlock.breakBlock(duration)
        // when
        createBreakBlock(block).getOrThrow()
    }
}

/**
 * TODO:
 * Implement position handling in AddTimeBlockUseCase
 *     - if it's right after the same type (work block after work block, break after break)
 *     should put it after the next block of a different type, ie work block after break
 * Implement CreateSessionUseCase to handle validation of session fields
 * Think about how to handle editing, deleting and moving time blocks in a session
 *   RemoveTimeBlockUseCase
 *   - would have to handle the resulting adjacent breaks (merge them or delete one?)
 *      - probably merge and add their time (until max duration)
 */