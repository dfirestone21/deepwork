package com.example.deepwork.domain.usecase.timeblock

import com.example.deepwork.domain.exception.TimeBlockException
import com.example.deepwork.domain.model.Result
import com.example.deepwork.domain.model.TimeBlock
import com.example.deepwork.domain.model.TimeBlock.*
import java.util.UUID

class CreateWorkBlockUseCase {

    suspend operator fun invoke(timeBlock: WorkBlock): Result<WorkBlock> {
        return try {
            validate(timeBlock)
            val preparedWorkBlock = timeBlock.copy(
                id = UUID.randomUUID().toString()
            )
            Result.Success(preparedWorkBlock)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    private fun validate(timeBlock: WorkBlock) {
        if (timeBlock.duration < WorkBlock.DURATION_MIN) {
            throw TimeBlockException.InvalidDurationTooShort()
        }
        if (timeBlock.duration > WorkBlock.DURATION_MAX) {
            throw TimeBlockException.InvalidDurationTooLong()
        }
        if (timeBlock.categories.isEmpty() || timeBlock.categories.size > WorkBlock.CATEGORIES_MAX) {
            throw TimeBlockException.InvalidCategoriesCount()
        }
        if (timeBlock.categories.size != timeBlock.categories.distinctBy { it.id }.size) {
            throw TimeBlockException.DuplicateCategories()
        }
    }
}