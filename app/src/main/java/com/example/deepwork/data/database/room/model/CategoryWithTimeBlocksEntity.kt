package com.example.deepwork.data.database.room.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class CategoryWithTimeBlocksEntity(
    @Embedded val category: CategoryEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = TimeBlockCategoryCrossRef::class,
            parentColumn = "categoryId",
            entityColumn = "timeBlockId"
        )
    )
    val timeBlocks: List<TimeBlockEntity>
)
