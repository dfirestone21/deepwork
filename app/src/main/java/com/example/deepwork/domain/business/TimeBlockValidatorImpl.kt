package com.example.deepwork.domain.business

import com.example.deepwork.domain.exception.TimeBlockException
import com.example.deepwork.domain.model.Category
import com.example.deepwork.domain.model.TimeBlock
import com.example.deepwork.domain.model.TimeBlock.WorkBlock
import javax.inject.Inject
import kotlin.time.Duration

class TimeBlockValidatorImpl @Inject constructor(

) : TimeBlockValidator {

    override fun validate(timeBlock: TimeBlock) {
        validateDuration(timeBlock.blockType, timeBlock.duration)
        if (timeBlock is WorkBlock) {
            validateCategories(timeBlock.blockType, timeBlock.categories)
        }
    }

    override fun validateDuration(blockType: TimeBlock.BlockType, duration: Duration) {
        val minDuration = TimeBlock.minDuration(blockType)
        if (duration < minDuration) {
            throw TimeBlockException.InvalidDurationTooShort(minDuration.inWholeMinutes.toString())
        }
        val maxDuration = TimeBlock.maxDuration(blockType)
        if (duration > maxDuration) {
            throw TimeBlockException.InvalidDurationTooLong(maxDuration.inWholeMinutes.toString())
        }

    }

    override fun validateCategories(blockType: TimeBlock.BlockType, categories: List<Category>) {
        if (categories.isEmpty() || categories.size > WorkBlock.CATEGORIES_MAX) {
            throw TimeBlockException.InvalidCategoriesCount()
        }
        if (categories.size != categories.distinctBy { it.uuid }.size) {
            throw TimeBlockException.DuplicateCategories()
        }
    }
}