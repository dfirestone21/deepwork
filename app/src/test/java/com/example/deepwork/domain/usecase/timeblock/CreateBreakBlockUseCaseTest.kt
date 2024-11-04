package com.example.deepwork.domain.usecase.timeblock

import com.example.deepwork.domain.exception.TimeBlockException
import com.example.deepwork.domain.model.TimeBlock
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.minutes

class CreateBreakBlockUseCaseTest {
    private lateinit var createBreakBlock: CreateBreakBlockUseCase

    @Before
    fun setup() {
        createBreakBlock = CreateBreakBlockUseCase()
    }

    @Test
    fun `when creating break block, should generate id`() = runTest {
        // given
        val block = TimeBlock.breakBlock()
        // when
        val createdBlock = createBreakBlock(block).getOrThrow()
        // then
        assert(createdBlock.id.isNotBlank())
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