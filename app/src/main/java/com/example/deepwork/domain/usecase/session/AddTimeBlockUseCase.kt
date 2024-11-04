package com.example.deepwork.domain.usecase.session

import com.example.deepwork.domain.exception.SessionException
import com.example.deepwork.domain.model.Result
import com.example.deepwork.domain.model.Session
import com.example.deepwork.domain.model.Session.Companion.MAX_TIME_BLOCKS
import com.example.deepwork.domain.model.TimeBlock
import kotlin.time.Duration.Companion.milliseconds

class AddTimeBlockUseCase {

    suspend operator fun invoke(session: Session, timeBlock: TimeBlock, position: Int = -1): Result<Session> {
        return try {
            validate(session, timeBlock, position)
            val updatedSession = session.copy(timeBlocks = session.timeBlocks + timeBlock)
            Result.Success(updatedSession)
        } catch (e: Exception) {
            Result.Error(e)
        }

    }
    
    private fun validate(session: Session, timeBlock: TimeBlock, position: Int) {
        if (session.timeBlocks.size >= MAX_TIME_BLOCKS) {
            throw SessionException.MaxTimeBlocksReached()
        }
        val previousBlock = previousBlock(session, position)
        val consecutiveWorkBlocks = timeBlock is TimeBlock.WorkBlock && previousBlock is TimeBlock.WorkBlock
        if (consecutiveWorkBlocks) {
            throw SessionException.ConsecutiveWorkBlocks()
        }
        val invalidBreakPosition = timeBlock is TimeBlock.BreakBlock && previousBlock !is TimeBlock.WorkBlock
        if (invalidBreakPosition) {
            throw SessionException.InvalidBreakPosition()
        }
        val maxSessionDurationReached = session.timeBlocks.sumOf { it.duration.inWholeMilliseconds}.milliseconds + timeBlock.duration > Session.MAX_DURATION
        if (maxSessionDurationReached) {
            throw SessionException.MaxSessionDurationReached()
        }
    }

    private fun previousBlock(session: Session, position: Int): TimeBlock? {
        return if (position == -1) session.timeBlocks.lastOrNull()
        else session.timeBlocks.getOrNull(position - 1)
    }
}