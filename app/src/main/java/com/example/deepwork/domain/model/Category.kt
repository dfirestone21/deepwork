package com.example.deepwork.domain.model

import com.example.deepwork.R
import kotlin.uuid.Uuid

data class Category(
    val id: Uuid,
    val name: String,
    val colorHex: Int,
    val createdAt: Long,
    val updatedAt: Long
) {

    companion object {
        val DEFAULT: Category = Category(
            id = Uuid.NIL,
            name = "Uncategorized",
            colorHex = R.color.category_default,
            createdAt = 0,
            updatedAt = 0
        )

        fun create(name: String, colorHex: Int): Category {
            return Category(
                id = Uuid.random(),
                name = name,
                colorHex = colorHex,
                createdAt = 0,
                updatedAt = 0
            )
        }
    }

    fun isDefault(): Boolean {
        return id == Uuid.NIL
    }
}
