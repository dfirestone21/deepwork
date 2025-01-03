package com.example.deepwork.domain.exception

sealed class TimeBlockException(message: String) : Exception(message) {
    class InvalidDurationTooShort(minDuration: String) : TimeBlockException("Invalid duration: Must be at least $minDuration minutes")
    class InvalidDurationTooLong(maxDuration: String) : TimeBlockException("Invalid duration: Must be at most $maxDuration minutes")
    class InvalidCategoriesCount : TimeBlockException("Invalid number of categories")
    class DuplicateCategories : TimeBlockException("Cannot have duplicate categories")
}