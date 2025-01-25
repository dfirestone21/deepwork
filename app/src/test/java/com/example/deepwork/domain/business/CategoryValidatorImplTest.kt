package com.example.deepwork.domain.business

import com.example.deepwork.domain.exception.CategoryException
import org.junit.Test

class CategoryValidatorImplTest {
    private val categoryValidator = CategoryValidatorImpl()


    @Test
    fun `validateName() when name is empty, should throw InvalidNameException`() {
        // given

        // when
        val invalidName = ""

        val exception = runCatching {
            categoryValidator.validateName(invalidName)
        }.exceptionOrNull()

        // then
        assert(exception is CategoryException.InvalidNameException)
    }

    @Test
    fun `validateName() when name is only whitespace, should throw InvalidNameException`() {
        // given

        // when
        val invalidName = "   "

        val exception = runCatching {
            categoryValidator.validateName(invalidName)
        }.exceptionOrNull()

        // then
        assert(exception is CategoryException.InvalidNameException)
    }

    @Test
    fun `validateName() when name is longer than NAME_MAX_LENGTH, should throw InvalidNameException`() {
        // given
        val maxLength = CategoryValidator.NAME_MAX_LENGTH
        // when
        val invalidName = "a".repeat(maxLength + 1)

        val exception = runCatching {
            categoryValidator.validateName(invalidName)
        }.exceptionOrNull()

        // then
        assert(exception is CategoryException.InvalidNameException)
    }
}