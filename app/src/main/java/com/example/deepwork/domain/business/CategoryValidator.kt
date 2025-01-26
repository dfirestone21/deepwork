package com.example.deepwork.domain.business

import com.example.deepwork.domain.model.Category

interface CategoryValidator {

    companion object {
        const val NAME_MAX_LENGTH = 30
    }

    fun validate(category: Category)

    fun validateName(name: String)
}