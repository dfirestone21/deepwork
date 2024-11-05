package com.example.deepwork.domain.exception

sealed class SessionException(message: String) : Exception(message) {
    class InvalidName(message: String) : SessionException(message)
    class MaxTimeBlocksReached : SessionException("Max timeBlocks reached")
    class ConsecutiveBlockTypes : SessionException("Cannot have consecutive time blocks of the same type")
    class InvalidBreakPosition : SessionException("Invalid break position")
    class MaxSessionDurationReached : SessionException("Max session duration reached")
    class InvalidTimeBlockPosition : SessionException("Invalid time block position")
}