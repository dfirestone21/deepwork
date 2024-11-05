package com.example.deepwork.domain.usecase.session

import com.example.deepwork.domain.exception.SessionException
import com.example.deepwork.domain.model.Result
import com.example.deepwork.domain.model.Session
import com.example.deepwork.domain.model.Session.Companion.MAX_TIME_BLOCKS
import com.example.deepwork.domain.model.TimeBlock
import kotlin.time.Duration.Companion.milliseconds

class AddTimeBlockUseCase {

    companion object {
        const val POSITION_DEFAULT = -1
    }

    suspend operator fun invoke(session: Session, timeBlock: TimeBlock, position: Int = POSITION_DEFAULT): Result<Session> {
        return try {
            validate(session, timeBlock, position)
            val timeBlocks = addTimeBlock(session, timeBlock, position)
            val updatedSession = session.copy(timeBlocks = timeBlocks)
            Result.Success(updatedSession)
        } catch (e: Exception) {
            Result.Error(e)
        }

    }
    
    private fun validate(session: Session, timeBlock: TimeBlock, position: Int) {
        val invalidPosition = position < POSITION_DEFAULT || position > session.timeBlocks.size - 1
        if (invalidPosition) {
            throw SessionException.InvalidTimeBlockPosition()
        }
        if (session.timeBlocks.size >= MAX_TIME_BLOCKS) {
            throw SessionException.MaxTimeBlocksReached()
        }
        val cantStartWithBreakBlock = timeBlock is TimeBlock.BreakBlock && session.timeBlocks.isEmpty()
        if (cantStartWithBreakBlock) {
            throw SessionException.InvalidBreakPosition()
        }
        val previousBlock = previousBlock(session, position)
        val nextBlock = nextBlock(session, position)
        val consecutiveWorkBlocks = timeBlock is TimeBlock.WorkBlock && (previousBlock is TimeBlock.WorkBlock || nextBlock is TimeBlock.WorkBlock)
        if (consecutiveWorkBlocks) {
            throw SessionException.ConsecutiveBlockTypes()
        }
        val consecutiveBreakBlocks = timeBlock is TimeBlock.BreakBlock && (previousBlock is TimeBlock.BreakBlock || nextBlock is TimeBlock.BreakBlock)
        if (consecutiveBreakBlocks) {
            throw SessionException.ConsecutiveBlockTypes()
        }
        val maxSessionDurationReached = session.timeBlocks.sumOf { it.duration.inWholeMilliseconds}.milliseconds + timeBlock.duration > Session.MAX_DURATION
        if (maxSessionDurationReached) {
            throw SessionException.MaxSessionDurationReached()
        }
    }

    private fun previousBlock(session: Session, position: Int): TimeBlock? {
        return if (position == POSITION_DEFAULT) {
            session.timeBlocks.lastOrNull()
        } else {
            session.timeBlocks.getOrNull(position - 1)
        }
    }

    private fun nextBlock(session: Session, position: Int): TimeBlock? {
        return if (position == POSITION_DEFAULT) {
            null
        } else {
            session.timeBlocks.getOrNull(position + 1)
        }
    }

    private fun addTimeBlock(session: Session, timeBlock: TimeBlock, position: Int): List<TimeBlock> {
        return if (position == POSITION_DEFAULT) {
            session.timeBlocks + timeBlock
        } else {
            session.timeBlocks.toMutableList().apply { set(position, timeBlock) }
        }
    }
}