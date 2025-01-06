package com.example.deepwork.domain.usecase.timeblock

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.deepwork.domain.business.TimeBlockValidator
import com.example.deepwork.domain.exception.TimeBlockException
import com.example.deepwork.domain.model.Category
import com.example.deepwork.domain.model.TimeBlock
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.minutes

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
    fun `when creating work block, should generate id`() = runTest {
        // given
        val block = TimeBlock.deepWorkBlock(25.minutes)
        // when
        val createdBlock = createWorkBlock(block).getOrThrow()
        // then
        assert(createdBlock.id.isNotBlank())
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