package com.example.deepwork.domain.model

data class Category(
    val id: String,
    val name: String
) {

    companion object {
        val DEFAULT: Category = Category(
            id = "defaultId",
            name = "Uncategorized"
        )
        private const val DEFAULT_ID = "default"
    }

    fun isDefault(): Boolean {
        return id == DEFAULT_ID
    }
}
