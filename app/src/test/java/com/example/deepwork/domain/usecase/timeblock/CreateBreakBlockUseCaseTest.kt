package com.example.deepwork.domain.usecase.timeblock

import com.example.deepwork.domain.business.TimeBlockValidator
import com.example.deepwork.domain.exception.TimeBlockException
import com.example.deepwork.domain.model.template.TimeBlockTemplate
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.Uuid

class CreateBreakBlockUseCaseTest {
    private lateinit var createWorkBlock: CreateTimeBlockUseCase
    private lateinit var timeBlockValidator: TimeBlockValidator

    @Before
    fun setup() {
        timeBlockValidator = mockk() {
            every { validate(any<TimeBlockTemplate>()) } returns Unit
        }
        createWorkBlock = CreateTimeBlockUseCase(timeBlockValidator)
    }

    @Test
    fun `when creating work block, should generate uuid`() = runTest {
        // given
        val block = TimeBlockTemplate.deepWorkTemplate(25.minutes)
        // when
        val createdBlock = createWorkBlock(block).getOrThrow()
        // then
        assert(createdBlock.id != Uuid.NIL)
    }

    @Test
    fun `when time block is invalid, should return error result`() = runTest {
        // given
        val block = TimeBlockTemplate.deepWorkTemplate(0.minutes, categories = emptyList())
        every { timeBlockValidator.validate(any<TimeBlockTemplate>()) } throws TimeBlockException.InvalidDurationTooShort("20 minutes")

        // when
        val result = createWorkBlock(block)

        // then
        assert(result.isError)
    }
}