package com.example.deepwork.domain.usecase.timeblock

import com.example.deepwork.domain.business.TimeBlockValidator
import com.example.deepwork.domain.model.Result
import com.example.deepwork.domain.model.template.TimeBlockTemplate
import javax.inject.Inject
import kotlin.uuid.Uuid

class CreateTimeBlockUseCase @Inject constructor(
    private val timeBlockValidator: TimeBlockValidator
) {

    suspend operator fun invoke(timeBlock: TimeBlockTemplate): Result<TimeBlockTemplate> {
        return try {
            validate(timeBlock)
            val preparedWorkBlock = timeBlock.copy(
                id = Uuid.random()
            )
            Result.Success(preparedWorkBlock)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    private fun validate(timeBlock: TimeBlockTemplate) {
        timeBlockValidator.validate(timeBlock)
    }
}