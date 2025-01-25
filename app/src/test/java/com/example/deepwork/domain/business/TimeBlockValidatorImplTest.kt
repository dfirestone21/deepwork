package com.example.deepwork.domain.business

import com.example.deepwork.domain.exception.TimeBlockException
import com.example.deepwork.domain.model.Category
import com.example.deepwork.domain.model.ScheduledTimeBlock
import com.example.deepwork.domain.model.template.TimeBlockTemplate
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.minutes

class TimeBlockValidatorImplTest {
    private lateinit var timeBlockValidator: TimeBlockValidatorImpl

    @Before
    fun setup() {
        timeBlockValidator = TimeBlockValidatorImpl()
    }

    @Test(expected = TimeBlockException.InvalidDurationTooShort::class)
    fun `scheduled timeblock when type is work block and duration is less than DURATION_MIN, should throw exception`() = runTest {
        // given
        val duration = ScheduledTimeBlock.Durations.DEEP_WORK_DURATION_MIN - 5.minutes
        val block = ScheduledTimeBlock.deepWorkBlock(duration)
        // when
        timeBlockValidator.validate(block)
    }

    @Test(expected = TimeBlockException.InvalidDurationTooLong::class)
    fun `scheduled timeblock when type is work block and duration is greater than DURATION_MAX, should throw exception`() = runTest {
        // given
        val duration = ScheduledTimeBlock.Durations.DEEP_WORK_DURATION_MAX + 5.minutes
        val block = ScheduledTimeBlock.deepWorkBlock(duration)
        // when
        timeBlockValidator.validate(block)
    }

    @Test(expected = TimeBlockException.InvalidCategoriesCount::class)
    fun `scheduled timeblock when type is work block and there are no categories, should throw exception`() = runTest {
        // given
        val block = ScheduledTimeBlock.deepWorkBlock(25.minutes, categories = emptyList())
        // when
        timeBlockValidator.validate(block)
    }

    @Test(expected = TimeBlockException.InvalidCategoriesCount::class)
    fun `scheduled timeblock when type is work block and there are more categories than CATEGORIES_MAX, should throw exception`() = runTest {
        // given
        val categories = List(ScheduledTimeBlock.CATEGORIES_MAX + 1) { Category.DEFAULT }
        val block = ScheduledTimeBlock.deepWorkBlock(25.minutes, categories)
        // when
        timeBlockValidator.validate(block)
    }

    @Test(expected = TimeBlockException.DuplicateCategories::class)
    fun `scheduled timeblock when type is work block and there are duplicate categories, should throw exception`() = runTest {
        // given
        val red = 0xFF0000FF.toInt()
        val category = Category.create("name", red)
        val block = ScheduledTimeBlock.deepWorkBlock(25.minutes, categories = listOf(category, category))
        // when
        timeBlockValidator.validate(block)
    }

    @Test(expected = TimeBlockException.InvalidDurationTooShort::class)
    fun `template when type is work block and duration is less than DURATION_MIN, should throw exception`() = runTest {
        // given
        val duration = ScheduledTimeBlock.Durations.DEEP_WORK_DURATION_MIN - 5.minutes
        val blockTemplate = TimeBlockTemplate.deepWorkTemplate(duration)
        // when
        timeBlockValidator.validate(blockTemplate)
    }

    @Test(expected = TimeBlockException.InvalidDurationTooLong::class)
    fun `template when type is work block and duration is greater than maxDuration, should throw exception`() = runTest {
        // given
        val duration = ScheduledTimeBlock.Durations.DEEP_WORK_DURATION_MAX + 5.minutes
        val blockTemplate = TimeBlockTemplate.deepWorkTemplate(duration)
        // when
        timeBlockValidator.validate(blockTemplate)
    }

    @Test(expected = TimeBlockException.InvalidCategoriesCount::class)
    fun `template when type is work block and there are no categories, should throw exception`() = runTest {
        // given
        val blockTemplate = TimeBlockTemplate.deepWorkTemplate(25.minutes, categories = emptyList())
        // when
        timeBlockValidator.validate(blockTemplate)
    }

    @Test(expected = TimeBlockException.InvalidCategoriesCount::class)
    fun `template when type is work block and there are more categories than CATEGORIES_MAX, should throw exception`() = runTest {
        // given
        val categories = List(ScheduledTimeBlock.CATEGORIES_MAX + 1) { Category.DEFAULT }
        val blockTemplate = TimeBlockTemplate.deepWorkTemplate(25.minutes, categories)
        // when
        timeBlockValidator.validate(blockTemplate)
    }

    @Test(expected = TimeBlockException.DuplicateCategories::class)
    fun `template when type is work block and there are duplicate categories, should throw exception`() = runTest {
        // given
        val red = 0xFF0000FF.toInt()
        val category = Category.create("name", red)
        val blockTemplate = TimeBlockTemplate.deepWorkTemplate(25.minutes, categories = listOf(category, category))
        // when
        timeBlockValidator.validate(blockTemplate)
    }
}