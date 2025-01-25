package com.example.deepwork.domain.business

import com.example.deepwork.domain.exception.CategoryException
import com.example.deepwork.domain.model.Category
import javax.inject.Inject

class CategoryValidatorImpl @Inject constructor() : CategoryValidator {

    override fun validate(category: Category) {
        validateName(category.name)
        validateColor(category.colorHex)
    }

    override fun validateName(name: String) {
        if (name.isBlank()) {
            throw CategoryException.InvalidNameException("Name cannot be empty")
        }
        if (name.length > CategoryValidator.NAME_MAX_LENGTH) {
            throw CategoryException.InvalidNameException("Name can't be longer than ${CategoryValidator.NAME_MAX_LENGTH} characters")
        }
    }

    override fun validateColor(colorHex: Int) {
        TODO("Not yet implemented")
    }
}