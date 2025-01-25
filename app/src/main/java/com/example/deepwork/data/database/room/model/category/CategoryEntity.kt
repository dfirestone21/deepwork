package com.example.deepwork.data.database.room.model.category

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.deepwork.domain.model.Category
import java.util.UUID
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

@Entity(
    tableName = CategoryEntity.TABLE_NAME,
    indices = [
        Index(value = ["name"], unique = true)
    ]
)
data class CategoryEntity(
    @PrimaryKey
    val id: UUID,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "color")
    val color: Int,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
) {

    companion object {

        const val TABLE_NAME = "category"

        fun toEntity(category: Category): CategoryEntity {
            return CategoryEntity(
                id = category.id.toJavaUuid(),
                name = category.name,
                color = category.colorHex,
                createdAt = category.createdAt,
                updatedAt = category.updatedAt
            )
        }
    }

    fun toDomain(): Category {
        return Category(
            id = id.toKotlinUuid(),
            name = name,
            colorHex = color,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}