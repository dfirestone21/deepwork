package com.example.deepwork.data.database.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.deepwork.data.database.room.model.CategoryEntity
import com.example.deepwork.data.database.room.model.TimeBlockCategoryCrossRef
import com.example.deepwork.data.database.room.model.TimeBlockEntity
import com.example.deepwork.data.database.room.model.TimeBlockWithCategoriesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeBlockDao {

    @Insert
    suspend fun insertTimeBlock(timeBlock: TimeBlockEntity): Long

    @Update
    suspend fun updateTimeBlock(timeBlock: TimeBlockEntity)

    @Insert
    suspend fun insertTimeBlockCategoryCrossRef(crossRef: TimeBlockCategoryCrossRef)

    @Transaction
    suspend fun insertTimeBlockWithCategories(
        timeBlockEntity: TimeBlockEntity,
        categories: List<CategoryEntity>
    ) {
        insertTimeBlock(timeBlockEntity)
        categories.forEach { category ->
            val crossRef = TimeBlockCategoryCrossRef(timeBlockEntity.id, category.id)
            insertTimeBlockCategoryCrossRef(crossRef)
        }
    }

    @Transaction
    @Query("""
        SELECT * FROM time_block 
        INNER JOIN time_block_category_cross_ref 
        ON time_block.id = time_block_category_cross_ref.timeBlockId
        WHERE time_block.id = :id
    """)
    suspend fun getById(id: Long): Flow<List<TimeBlockWithCategoriesEntity>>

    @Transaction
    @Query("""
        SELECT * FROM time_block 
        INNER JOIN time_block_category_cross_ref 
        ON time_block.id = time_block_category_cross_ref.timeBlockId
        WHERE time_block.uuid = :uuid
    """)
    suspend fun getByUuid(uuid: String): Flow<List<TimeBlockWithCategoriesEntity>>

    @Delete
    suspend fun delete(timeBlock: TimeBlockWithCategoriesEntity)

    @Transaction
    @Query("""
        SELECT * FROM time_block
        INNER JOIN time_block_category_cross_ref
        ON time_block.id = time_block_category_cross_ref.timeBlockId
        WHERE time_block_category_cross_ref.categoryId = :categoryId
    """)
    suspend fun getByCategoryId(categoryId: Long): Flow<List<TimeBlockWithCategoriesEntity>>
}