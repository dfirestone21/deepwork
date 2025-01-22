package com.example.deepwork.domain.usecase.timeblock

import com.example.deepwork.domain.business.TimeBlockValidator
import com.example.deepwork.domain.exception.TimeBlockException
import com.example.deepwork.domain.model.TimeBlock
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.Uuid

class CreateWorkBlockUseCaseTest {
    private lateinit var createWorkBlock: CreateWorkBlockUseCase
    private lateinit var timeBlockValidator: TimeBlockValidator

    @Before
    fun setup() {
        timeBlockValidator = mockk() {
            every { validate(any()) } returns Unit
        }
        createWorkBlock = CreateWorkBlockUseCase(timeBlockValidator)
    }

    @Test
    fun `when creating work block, should generate uuid`() = runTest {
        // given
        val block = TimeBlock.deepWorkBlock(25.minutes)
        // when
        val createdBlock = createWorkBlock(block).getOrThrow()
        // then
        assert(createdBlock.id != Uuid.NIL)
    }

    @Test
    fun `when time block is invalid, should return error result`() = runTest {
        // given
        val block = TimeBlock.deepWorkBlock(0.minutes, categories = emptyList())
        every { timeBlockValidator.validate(any()) } throws TimeBlockException.InvalidDurationTooShort("20 minutes")

        // when
        val result = createWorkBlock(block)

        // then
        assert(result.isError)
    }
}