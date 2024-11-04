package com.example.deepwork.domain.exception

import com.example.deepwork.domain.model.TimeBlock

sealed class TimeBlockException(message: String) : Exception(message) {
    class InvalidDurationTooShort : TimeBlockException("Invalid duration: Must be at least ${TimeBlock.WorkBlock.DURATION_MIN.inWholeMinutes} minutes")
    class InvalidDurationTooLong : TimeBlockException("Invalid duration: Must be at most ${TimeBlock.WorkBlock.DURATION_MAX.inWholeMinutes} minutes")
    class InvalidCategoriesCount : TimeBlockException("Invalid number of categories")
    class DuplicateCategories : TimeBlockException("Cannot have duplicate categories")
}