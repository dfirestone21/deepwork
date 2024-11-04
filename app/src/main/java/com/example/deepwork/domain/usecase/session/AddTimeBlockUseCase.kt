package com.example.deepwork.domain.usecase.session

import com.example.deepwork.domain.exception.SessionException
import com.example.deepwork.domain.model.Result
import com.example.deepwork.domain.model.Session
import com.example.deepwork.domain.model.Session.Companion.MAX_TIME_BLOCKS
import com.example.deepwork.domain.model.TimeBlock
import kotlin.time.Duration.Companion.milliseconds

class AddTimeBlockToSessionUseCase {

    suspend operator fun invoke(session: Session, timeBlock: TimeBlock, position: Int = -1): Result<Session> {
        if (session.timeBlocks.size >= MAX_TIME_BLOCKS) {
            return Result.Error(SessionException.MaxTimeBlocksReached())
        }
        val previousBlock = previousBlock(session, position)
        val consecutiveWorkBlocks = timeBlock is TimeBlock.WorkBlock && previousBlock is TimeBlock.WorkBlock
        if (consecutiveWorkBlocks) {
            return Result.Error(SessionException.ConsecutiveWorkBlocks())
        }
        val invalidBreakPosition = timeBlock is TimeBlock.BreakBlock && previousBlock !is TimeBlock.WorkBlock
        if (invalidBreakPosition) {
            return Result.Error(SessionException.InvalidBreakPosition())
        }
        val maxSessionDurationReached = session.timeBlocks.sumOf { it.duration.inWholeMilliseconds}.milliseconds + timeBlock.duration > Session.MAX_DURATION
        if (maxSessionDurationReached) {
            return Result.Error(SessionException.MaxSessionDurationReached())
        }
        val updatedSession = session.copy(timeBlocks = session.timeBlocks + timeBlock)
        return Result.Success(updatedSession)
    }

    private fun previousBlock(session: Session, position: Int): TimeBlock? {
        return if (position == -1) session.timeBlocks.lastOrNull()
        else session.timeBlocks.getOrNull(position - 1)
    }
}