package com.example.deepwork.domain.exception

sealed class SessionException(message: String) : Exception(message) {
    class MaxTimeBlocksReached : SessionException("Max timeBlocks reached")
    class ConsecutiveWorkBlocks : SessionException("Cannot have consecutive work blocks without a break")
    class InvalidBreakPosition : SessionException("Invalid break position")
    class MaxSessionDurationReached : SessionException("Max session duration reached")
}