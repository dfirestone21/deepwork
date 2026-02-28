package com.example.deepwork.domain.usecase.session

import com.example.deepwork.domain.exception.SessionException
import com.example.deepwork.domain.model.Result
import com.example.deepwork.domain.model.ScheduledSession
import javax.inject.Inject
import kotlin.uuid.Uuid

class RemoveTimeBlockUseCase @Inject constructor() {

    suspend operator fun invoke(session: ScheduledSession, blockId: Uuid): Result<ScheduledSession> {
        return Result.of {
            val block = session.timeBlocks.find { it.id == blockId }
                ?: throw SessionException.InvalidTimeBlockPosition()

            val workBlockCount = session.timeBlocks.count { it.isWorkBlock }
            if (block.isWorkBlock && workBlockCount <= 1) {
                throw SessionException.MinTimeBlocksReached()
            }

            val remaining = session.timeBlocks.filter { it.id != blockId }

            if (remaining.isNotEmpty() && (remaining.first().isBreakBlock || remaining.last().isBreakBlock)) {
                throw SessionException.InvalidBreakPosition()
            }

            session.copy(timeBlocks = remaining)
        }
    }
}
