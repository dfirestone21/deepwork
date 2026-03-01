package com.example.deepwork.domain.usecase.session

import com.example.deepwork.domain.model.ScheduledTimeBlock
import com.example.deepwork.domain.model.SessionWarning
import com.example.deepwork.domain.model.WarningLevel
import com.example.deepwork.domain.model.WarningType
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes

class CalculateSessionWarningsUseCase @Inject constructor() {

    operator fun invoke(timeBlocks: List<ScheduledTimeBlock>): List<SessionWarning> {
        val warnings = mutableListOf<SessionWarning>()

        checkLongWorkStretch(timeBlocks, warnings)
        checkShortBreak(timeBlocks, warnings)
        checkLongBreak(timeBlocks, warnings)
        checkHighBreakRatio(timeBlocks, warnings)
        checkLongSession(timeBlocks, warnings)

        return warnings
    }

    // Rule 1: LONG_WORK_STRETCH (Yellow)
    // A work block with duration > 60m where the immediately next block is NOT a break (or there is no next block)
    private fun checkLongWorkStretch(timeBlocks: List<ScheduledTimeBlock>, warnings: MutableList<SessionWarning>) {
        for (i in timeBlocks.indices) {
            val block = timeBlocks[i]
            if (block.isWorkBlock && block.duration > 60.minutes) {
                val nextBlock = timeBlocks.getOrNull(i + 1)
                if (nextBlock == null || nextBlock.isWorkBlock) {
                    warnings.add(SessionWarning(WarningLevel.YELLOW, WarningType.LONG_WORK_STRETCH))
                    return
                }
            }
        }
    }

    // Rule 2: SHORT_BREAK (Yellow)
    // A break with duration < 5m that immediately follows a work block with duration >= 60m
    private fun checkShortBreak(timeBlocks: List<ScheduledTimeBlock>, warnings: MutableList<SessionWarning>) {
        for (i in timeBlocks.indices) {
            val block = timeBlocks[i]
            if (block.isBreakBlock && block.duration < 5.minutes) {
                val prevBlock = timeBlocks.getOrNull(i - 1)
                if (prevBlock != null && prevBlock.isWorkBlock && prevBlock.duration >= 60.minutes) {
                    warnings.add(SessionWarning(WarningLevel.YELLOW, WarningType.SHORT_BREAK))
                    return
                }
            }
        }
    }

    // Rule 3: LONG_BREAK (Blue)
    // A break with duration > 30m where BOTH adjacent work blocks (preceding AND following, if they exist) each have duration < 60m
    private fun checkLongBreak(timeBlocks: List<ScheduledTimeBlock>, warnings: MutableList<SessionWarning>) {
        for (i in timeBlocks.indices) {
            val block = timeBlocks[i]
            if (block.isBreakBlock && block.duration > 30.minutes) {
                val prevBlock = timeBlocks.getOrNull(i - 1)
                val nextBlock = timeBlocks.getOrNull(i + 1)
                val prevQualifies = prevBlock == null || (prevBlock.isWorkBlock && prevBlock.duration < 60.minutes)
                val nextQualifies = nextBlock == null || (nextBlock.isWorkBlock && nextBlock.duration < 60.minutes)
                if (prevQualifies && nextQualifies) {
                    warnings.add(SessionWarning(WarningLevel.BLUE, WarningType.LONG_BREAK))
                    return
                }
            }
        }
    }

    // Rule 4: HIGH_BREAK_RATIO (Yellow)
    // sum of all break durations > (1/3) x sum of all work durations
    private fun checkHighBreakRatio(timeBlocks: List<ScheduledTimeBlock>, warnings: MutableList<SessionWarning>) {
        val totalWorkDuration = totalWorkDuration(timeBlocks)
        val totalBreakDuration = timeBlocks.filter { it.isBreakBlock }.fold(kotlin.time.Duration.ZERO) { acc, b -> acc + b.duration }
        if (totalWorkDuration > kotlin.time.Duration.ZERO && totalBreakDuration > totalWorkDuration / 3) {
            warnings.add(SessionWarning(WarningLevel.YELLOW, WarningType.HIGH_BREAK_RATIO))
        }
    }

    // Rule 5: LONG_SESSION (Blue)
    // sum of all work block durations > 4 hours (240 minutes)
    private fun checkLongSession(timeBlocks: List<ScheduledTimeBlock>, warnings: MutableList<SessionWarning>) {
        if (totalWorkDuration(timeBlocks) > 240.minutes) {
            warnings.add(SessionWarning(WarningLevel.BLUE, WarningType.LONG_SESSION))
        }
    }

    private fun totalWorkDuration(timeBlocks: List<ScheduledTimeBlock>): kotlin.time.Duration =
        timeBlocks.filter { it.isWorkBlock }.fold(kotlin.time.Duration.ZERO) { acc, b -> acc + b.duration }
}
