package com.example.deepwork.domain.usecase.timeblock

import com.example.deepwork.domain.exception.TimeBlockException
import com.example.deepwork.domain.model.Category
import com.example.deepwork.domain.model.TimeBlock
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.minutes

class CreateWorkBlockUseCaseTest {
    private lateinit var createWorkBlock: CreateWorkBlockUseCase

    @Before
    fun setup() {
        createWorkBlock = CreateWorkBlockUseCase()
    }

    @Test
    fun `when creating work block, should generate id`() = runTest {
        // given
        val block = TimeBlock.workBlock(25.minutes)
        // when
        val createdBlock = createWorkBlock(block).getOrThrow()
        // then
        assert(createdBlock.id.isNotBlank())
    }

    @Test(expected = TimeBlockException.InvalidDurationTooShort::class)
    fun `when duration is less than DURATION_MIN, should throw exception`() = runTest {
        // given
        val duration = TimeBlock.WorkBlock.DURATION_MIN - 5.minutes
        val block = TimeBlock.workBlock(duration)
        // when
        createWorkBlock(block).getOrThrow()
    }

    @Test(expected = TimeBlockException.InvalidDurationTooLong::class)
    fun `when duration is greater than DURATION_MAX, should throw exception`() = runTest {
        // given
        val duration = TimeBlock.WorkBlock.DURATION_MAX + 5.minutes
        val block = TimeBlock.workBlock(duration)
        // when
        createWorkBlock(block).getOrThrow()
    }

    @Test(expected = TimeBlockException.InvalidCategoriesCount::class)
    fun `when there are no categories, should throw exception`() = runTest {
        // given
        val block = TimeBlock.workBlock(25.minutes, categories = emptyList())
        // when
        createWorkBlock(block).getOrThrow()
    }

    @Test(expected = TimeBlockException.InvalidCategoriesCount::class)
    fun `when there are more categories than CATEGORIES_MAX, should throw exception`() = runTest {
        // given
        val categories = List(TimeBlock.WorkBlock.CATEGORIES_MAX + 1) { Category.DEFAULT }
        val block = TimeBlock.workBlock(25.minutes, categories)
        // when
        createWorkBlock(block).getOrThrow()
    }

    @Test(expected = TimeBlockException.DuplicateCategories::class)
    fun `when there are duplicate categories, should throw exception`() = runTest {
        // given
        val category = Category("id", "name")
        val block = TimeBlock.workBlock(25.minutes, categories = listOf(category, category))
        // when
        createWorkBlock(block).getOrThrow()
    }
}