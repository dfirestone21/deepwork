package com.example.deepwork.domain.exception

sealed class CategoryException(message: String) : Exception(message) {
    data class InvalidNameException(override val message: String) : CategoryException(message)
}