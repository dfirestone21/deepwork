package com.example.deepwork.data.database.room.model

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    primaryKeys = ["timeBlockId", "categoryId"],
    foreignKeys = [
        ForeignKey(
            entity = TimeBlockEntity::class,
            parentColumns = ["id"],
            childColumns = ["timeBlockId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )],
    tableName = TimeBlockCategoryCrossRef.TABLE_NAME
)
data class TimeBlockCategoryCrossRef(
    val timeBlockId: Long,
    val categoryId: Long
) {

    companion object {
        const val TABLE_NAME = "time_block_category_cross_ref"
    }
}
