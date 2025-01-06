package com.example.deepwork.domain.model

import com.example.deepwork.R

data class Category(
    val id: String,
    val name: String,
    val colorHex: Int
) {

    companion object {
        val DEFAULT: Category = Category(
            id = "defaultId",
            name = "Uncategorized",
            colorHex = R.color.category_default
        )
        private const val DEFAULT_ID = "default"
        private const val DEFAULT_COLOR = 0
    }

    fun isDefault(): Boolean {
        return id == DEFAULT_ID
    }
}
