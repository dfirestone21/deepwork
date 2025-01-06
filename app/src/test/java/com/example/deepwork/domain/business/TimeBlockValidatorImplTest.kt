package com.example.deepwork.domain.business

import com.example.deepwork.domain.exception.TimeBlockException
import com.example.deepwork.domain.model.Category
import com.example.deepwork.domain.model.TimeBlock
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.minutes

class TimeBlockValidatorImplTest {
    private lateinit var timeBlockValidator: TimeBlockValidatorImpl
    private var timeBlock = TimeBlock.deepWorkBlock(45.minutes)

    @Before
    fun setup() {
        timeBlockValidator = TimeBlockValidatorImpl()
    }

    @Test(expected = TimeBlockException.InvalidDurationTooShort::class)
    fun `when type is work block and duration is less than DURATION_MIN, should throw exception`() = runTest {
        // given
        val duration = TimeBlock.WorkBlock.DeepWorkBlock.DURATION_MIN - 5.minutes
        val block = TimeBlock.deepWorkBlock(duration)
        // when
        timeBlockValidator.validate(block)
    }

    @Test(expected = TimeBlockException.InvalidDurationTooLong::class)
    fun `when type is work block and duration is greater than DURATION_MAX, should throw exception`() = runTest {
        // given
        val duration = TimeBlock.WorkBlock.DeepWorkBlock.DURATION_MAX + 5.minutes
        val block = TimeBlock.deepWorkBlock(duration)
        // when
        timeBlockValidator.validate(block)
    }

    @Test(expected = TimeBlockException.InvalidCategoriesCount::class)
    fun `when type is work block and there are no categories, should throw exception`() = runTest {
        // given
        val block = TimeBlock.deepWorkBlock(25.minutes, categories = emptyList())
        // when
        timeBlockValidator.validate(block)
    }

    @Test(expected = TimeBlockException.InvalidCategoriesCount::class)
    fun `when type is work block and there are more categories than CATEGORIES_MAX, should throw exception`() = runTest {
        // given
        val categories = List(TimeBlock.WorkBlock.CATEGORIES_MAX + 1) { Category.DEFAULT }
        val block = TimeBlock.deepWorkBlock(25.minutes, categories)
        // when
        timeBlockValidator.validate(block)
    }

    @Test(expected = TimeBlockException.DuplicateCategories::class)
    fun `when type is work block and there are duplicate categories, should throw exception`() = runTest {
        // given
        val red = 0xFF0000FF.toInt()
        val category = Category("id", "name", red)
        val block = TimeBlock.deepWorkBlock(25.minutes, categories = listOf(category, category))
        // when
        timeBlockValidator.validate(block)
    }
}