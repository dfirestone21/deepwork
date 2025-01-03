package com.example.deepwork.domain.usecase.timeblock

import com.example.deepwork.domain.exception.TimeBlockException
import com.example.deepwork.domain.model.Result
import com.example.deepwork.domain.model.TimeBlock.BreakBlock
import java.util.UUID
import javax.inject.Inject

class CreateBreakBlockUseCase @Inject constructor() {

    suspend operator fun invoke(timeBlock: BreakBlock): Result<BreakBlock> {
        return try {
            validate(timeBlock)
            val preparedBreakBlock = timeBlock.copy(
                id = UUID.randomUUID().toString()
            )
            Result.Success(preparedBreakBlock)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    private fun validate(timeBlock: BreakBlock) {
        if (timeBlock.duration < BreakBlock.DURATION_MIN) {
            throw TimeBlockException.InvalidDurationTooShort(timeBlock.minDuration.toString())
        }
        if (timeBlock.duration > BreakBlock.DURATION_MAX) {
            throw TimeBlockException.InvalidDurationTooLong(timeBlock.maxDuration.toString())
        }
    }
}