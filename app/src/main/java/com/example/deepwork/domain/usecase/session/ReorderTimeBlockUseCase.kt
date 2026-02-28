package com.example.deepwork.domain.usecase.session

import com.example.deepwork.domain.exception.SessionException
import com.example.deepwork.domain.model.Result
import com.example.deepwork.domain.model.ScheduledSession
import com.example.deepwork.domain.model.ScheduledTimeBlock
import javax.inject.Inject
import kotlin.uuid.Uuid

class ReorderTimeBlockUseCase @Inject constructor() {

    suspend operator fun invoke(session: ScheduledSession, blockId: Uuid, targetPosition: Int): Result<ScheduledSession> {
        return Result.of {
            validate(session, blockId, targetPosition)
            val reordered = reorder(session, blockId, targetPosition)
            session.copy(timeBlocks = reordered)
        }
    }

    private fun validate(session: ScheduledSession, blockId: Uuid, targetPosition: Int) {
        if (targetPosition < 0 || targetPosition >= session.timeBlocks.size) {
            throw SessionException.InvalidTimeBlockPosition()
        }

        val block = session.timeBlocks.find { it.id == blockId }
            ?: throw SessionException.InvalidTimeBlockPosition()

        val lastPosition = session.timeBlocks.size - 1
        if (block.type == ScheduledTimeBlock.BlockType.BREAK &&
            (targetPosition == 0 || targetPosition == lastPosition)) {
            throw SessionException.InvalidBreakPosition()
        }
    }

    /**
     * Removes [blockId] from its current position and inserts it at [targetPosition].
     * [targetPosition] refers to the index in the original list. When moving forward
     * (currentIndex < targetPosition), removing the element shifts subsequent elements
     * left by one, so the effective insert index is targetPosition - 1.
     */
    private fun reorder(session: ScheduledSession, blockId: Uuid, targetPosition: Int): List<ScheduledTimeBlock> {
        val block = session.timeBlocks.first { it.id == blockId }
        val currentIndex = session.timeBlocks.indexOf(block)
        val insertIndex = if (currentIndex < targetPosition) targetPosition - 1 else targetPosition
        val reordered = session.timeBlocks.toMutableList().apply {
            removeAt(currentIndex)
            add(insertIndex, block)
        }

        val hasConsecutiveBreaks = reordered.zipWithNext().any { (a, b) ->
            a.type == ScheduledTimeBlock.BlockType.BREAK && b.type == ScheduledTimeBlock.BlockType.BREAK
        }
        if (hasConsecutiveBreaks) throw SessionException.ConsecutiveBlockTypes()

        return reordered
    }
}
