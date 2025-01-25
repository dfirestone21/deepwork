package com.example.deepwork.domain.business

import com.example.deepwork.domain.exception.TimeBlockException
import com.example.deepwork.domain.model.Category
import com.example.deepwork.domain.model.ScheduledTimeBlock
import com.example.deepwork.domain.model.template.TimeBlockTemplate
import javax.inject.Inject
import kotlin.time.Duration

class TimeBlockValidatorImpl @Inject constructor(

) : TimeBlockValidator {

    override fun validate(timeBlock: ScheduledTimeBlock) {
        validateDuration(timeBlock.type, timeBlock.duration)
        if (timeBlock.type.requiresCategories) {
            validateCategories(timeBlock.type, timeBlock.categories)
        }
    }

    override fun validate(timeBlockTemplate: TimeBlockTemplate) {
        validateDuration(timeBlockTemplate.type, timeBlockTemplate.duration)
        if (timeBlockTemplate.type.requiresCategories) {
            validateCategories(timeBlockTemplate.type, timeBlockTemplate.categories)
        }

    }

    override fun validateDuration(blockType: ScheduledTimeBlock.BlockType, duration: Duration) {
        val minDuration = blockType.minDuration
        if (duration < minDuration) {
            throw TimeBlockException.InvalidDurationTooShort(minDuration.inWholeMinutes.toString())
        }
        val maxDuration = blockType.maxDuration
        if (duration > maxDuration) {
            throw TimeBlockException.InvalidDurationTooLong(maxDuration.inWholeMinutes.toString())
        }

    }

    override fun validateCategories(blockType: ScheduledTimeBlock.BlockType, categories: List<Category>) {
        if (categories.isEmpty() || categories.size > ScheduledTimeBlock.CATEGORIES_MAX) {
            throw TimeBlockException.InvalidCategoriesCount()
        }
        if (categories.size != categories.distinctBy { it.id }.size) {
            throw TimeBlockException.DuplicateCategories()
        }
    }
}