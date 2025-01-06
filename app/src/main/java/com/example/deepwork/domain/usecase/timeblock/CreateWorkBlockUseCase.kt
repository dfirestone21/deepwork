package com.example.deepwork.domain.usecase.timeblock

import com.example.deepwork.domain.business.TimeBlockValidator
import com.example.deepwork.domain.exception.TimeBlockException
import com.example.deepwork.domain.model.Result
import com.example.deepwork.domain.model.TimeBlock
import com.example.deepwork.domain.model.TimeBlock.*
import java.util.UUID
import javax.inject.Inject

class CreateWorkBlockUseCase @Inject constructor(
    private val timeBlockValidator: TimeBlockValidator
) {

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
        timeBlockValidator.validate(timeBlock)
    }
}