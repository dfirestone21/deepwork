package com.example.deepwork.data.database.db

import android.database.sqlite.SQLiteException
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.deepwork.data.database.room.dao.CategoryDao
import com.example.deepwork.data.database.room.model.category.CategoryEntity
import com.example.deepwork.domain.exception.DatabaseException
import com.example.deepwork.domain.model.Category
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Calendar

class CategoryDbRoomTest {
    private lateinit var categoryDb: CategoryDbRoom
    private lateinit var categoryDao: CategoryDao
    private lateinit var category: Category
    private val currentTime = System.currentTimeMillis()

    @Before
    fun setup() {
        category = Category.create(
            name = "New Category",
            colorHex = Color.Red.toArgb()
        )
        categoryDao = mockk() {
            coEvery { upsert(any()) } just Runs
        }
        categoryDb = CategoryDbRoom(categoryDao)

        mockkStatic(Calendar::class)
        every { Calendar.getInstance().timeInMillis } returns currentTime
    }

    @Test
    fun `upsert() should save category in database`() = runTest {
        // given
        val categoryEntity = CategoryEntity.toEntity(category)

        // when
        categoryDb.upsert(category)

        // then
        coVerify { categoryDao.upsert(match { it.id == categoryEntity.id }) }
    }

    @Test
    fun `upsert() should set createdAt to current time`() = runTest {
        // given
        val expectedCreatedAt = currentTime

        // when
        categoryDb.upsert(category)

        // then
        coVerify {
            categoryDao.upsert(match { it.createdAt == expectedCreatedAt })
        }
    }

    @Test
    fun `upsert() when category has already been saved before, should set updatedAt to current time`() = runTest {
        // given
        // createdAt already set
        val categoryToUpdate = category.copy(createdAt = 98742929287)

        // when
        categoryDb.upsert(categoryToUpdate)

        // then
        coVerify {
            categoryDao.upsert( match { it.updatedAt == currentTime })
        }
    }

    @Test
    fun `upsert() when saving category throws exception, should throw DatabaseException`() = runTest {
        // given
        coEvery { categoryDao.upsert(any()) } throws SQLiteException()

        // when
        val exception = runCatching { categoryDb.upsert(category) }
            .exceptionOrNull()

        // then
        assert(exception is DatabaseException)
    }

    @Test
    fun `getAll() should return all categories`() = runTest {
        // given
        val expectedCategories = listOf(
            Category.create("Category 1", Color.Red.toArgb()),
            Category.create("Category 2", Color.Blue.toArgb()),
            Category.create("Category 3", Color.Green.toArgb()),
            Category.create("Category 4", Color.Yellow.toArgb()),
            Category.create("Category 5", Color.Magenta.toArgb())
        )
        every { categoryDao.getAll() } returns flowOf(expectedCategories.map { CategoryEntity.toEntity(it) })

        // when
        val actualCategories = categoryDb.getAll().first()

        // then
        assertEquals(actualCategories, expectedCategories)
    }

    @Test
    fun `getAll() when error fetching categories, should throw DatabaseException`() = runTest {
        // given
        val exception = SQLiteException("Error fetching categories")
        coEvery { categoryDao.getAll() } returns flow { throw exception }

        // when
        val actualException = runCatching {
            categoryDb.getAll().first()
        }.exceptionOrNull()

        // then
        assert(actualException is DatabaseException)
    }

    @After
    fun tearDown() {
        unmockkStatic(Calendar::class)
    }

}