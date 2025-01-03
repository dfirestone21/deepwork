package com.example.deepwork.domain.usecase.timeblock

import com.example.deepwork.domain.exception.TimeBlockException
import com.example.deepwork.domain.model.Result
import com.example.deepwork.domain.model.TimeBlock
import com.example.deepwork.domain.model.TimeBlock.*
import java.util.UUID
import javax.inject.Inject

class CreateWorkBlockUseCase @Inject constructor() {

    suspend operator fun invoke(timeBlock: WorkBlock): Result<WorkBlock> {
        return try {
            validate(timeBlock)
            val preparedWorkBlock = timeBlock.copyValues(
                id = UUID.randomUUID().toString()
            ) as WorkBlock
            Result.Success(preparedWorkBlock)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    private fun validate(timeBlock: WorkBlock) {
        if (timeBlock.duration < timeBlock.minDuration) {
            throw TimeBlockException.InvalidDurationTooShort(timeBlock.minDuration.toString())
        }
        if (timeBlock.duration > timeBlock.maxDuration) {
            throw TimeBlockException.InvalidDurationTooLong(timeBlock.maxDuration.toString())
        }
        if (timeBlock.categories.isEmpty() || timeBlock.categories.size > WorkBlock.CATEGORIES_MAX) {
            throw TimeBlockException.InvalidCategoriesCount()
        }
        if (timeBlock.categories.size != timeBlock.categories.distinctBy { it.id }.size) {
            throw TimeBlockException.DuplicateCategories()
        }
    }
}