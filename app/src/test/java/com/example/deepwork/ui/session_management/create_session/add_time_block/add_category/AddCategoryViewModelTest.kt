package com.example.deepwork.ui.session_management.create_session.add_time_block.add_category

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.deepwork.domain.business.CategoryValidator
import com.example.deepwork.domain.exception.CategoryException
import com.example.deepwork.domain.repository.CategoryRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import net.bytebuddy.matcher.ElementMatchers.returns
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddCategoryViewModelTest {
    private lateinit var viewModel: AddCategoryViewModel
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var categoryValidator: CategoryValidator
    private lateinit var testScope: TestScope

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        categoryRepository = mockk() {
            coEvery { upsert(any()) } answers { firstArg() }
        }
        categoryValidator = mockk() {
            every { validateName(any()) } just Runs
            every { validate(any()) } just Runs
        }
    }

    private fun initViewModel() {
        viewModel = AddCategoryViewModel(categoryRepository, categoryValidator)
    }

    private fun initViewModel(testScope: TestScope, advanceUntilIdle: Boolean = true) {
        initViewModel()
        this.testScope = testScope
        if (advanceUntilIdle) {
            testScope.advanceUntilIdle()
        }
    }

    @Test
    fun `onEvent NameUpdated when name is valid, should update name`() = runTest {
        // given
        initViewModel()
        val expectedName = "Category 1"

        // when
        val event = AddCategoryEvent.NameUpdated(expectedName)
        viewModel.onEvent(event)
        val state = viewModel.state
        val actualName = state.name.value

        // then
        assertEquals(expectedName, actualName)
    }

    @Test
    fun `onEvent NameUpdated when name is valid, should set error to false`() = runTest {
        // given
        initViewModel()
        val name = "Category 1"

        // when
        val event = AddCategoryEvent.NameUpdated(name)
        viewModel.onEvent(event)
        val state = viewModel.state
        val nameField = state.name

        // then
        assertFalse(nameField.isError)
    }

    @Test
    fun `onEvent NameUpdated when name is invalid, should still update name`() = runTest {
        // given
        val expectedName = "   "
        every { categoryValidator.validateName(any()) } throws CategoryException.InvalidNameException("Wrong name!!")
        initViewModel()

        // when
        val event = AddCategoryEvent.NameUpdated(expectedName)
        viewModel.onEvent(event)
        val state = viewModel.state
        val actualName = state.name.value

        // then
        assertEquals(expectedName, actualName)
    }

    @Test
    fun `onEvent NameUpdated when name is invalid, should set error message`() = runTest {
        // given
        val expectedName = "   "
        val expectedErrorMessage = "Wrong name!!"
        every { categoryValidator.validateName(any()) } throws CategoryException.InvalidNameException(expectedErrorMessage)
        initViewModel()

        // when
        val event = AddCategoryEvent.NameUpdated(expectedName)
        viewModel.onEvent(event)
        val state = viewModel.state
        val nameField = state.name

        // then
        assertEquals(expectedErrorMessage, nameField.message)
        assert(nameField.isError)
    }

    @Test
    fun `onEvent NameUpdated when name is invalid, should set state isValid to false`() = runTest {
        // given
        val expectedName = "   "
        every { categoryValidator.validateName(any()) } throws CategoryException.InvalidNameException("Wrong name!!")
        initViewModel()

        // when
        val event = AddCategoryEvent.NameUpdated(expectedName)
        viewModel.onEvent(event)
        val state = viewModel.state

        // then
        assertFalse(state.isValid)
    }

    @Test
    fun `onEvent NameUpdated when name is valid, should recalculate state isValid`() = runTest {
        // given
        initViewModel()
        val invalidName = "   "
        val name = "Category 1"
        every { categoryValidator.validateName(invalidName) } throws CategoryException.InvalidNameException("Wrong name!!") andThen Unit

        // when
        // set selectedColor
        val colorEvent = AddCategoryEvent.ColorSelected(Color.Red.toArgb())
        viewModel.onEvent(colorEvent)

        val invalidNameEvent = AddCategoryEvent.NameUpdated(invalidName)
        viewModel.onEvent(invalidNameEvent)
        val invalidState = viewModel.state
        assertFalse(invalidState.isValid)

        val event = AddCategoryEvent.NameUpdated(name)
        viewModel.onEvent(event)
        val state = viewModel.state

        // then
        assert(state.isValid)
    }

    @Test
    fun `onEvent ColorSelected should update the selectedColor`() = runTest {
        // given
        val expectedColor = Color.Red.toArgb()
        initViewModel()

        // when
        val event = AddCategoryEvent.ColorSelected(expectedColor)
        viewModel.onEvent(event)
        val state = viewModel.state
        val actualColorSelected = state.selectedColor

        // then
        assertEquals(expectedColor, actualColorSelected)
    }

    @Test
    fun `onEvent ColorSelected should recalculate state isValid`() = runTest {
        // given
        val color = Color.Red.toArgb()
        initViewModel()

        // when
        // set valid name
        val nameEvent = AddCategoryEvent.NameUpdated("Category 1")
        viewModel.onEvent(nameEvent)
        val invalidState = viewModel.state
        assertFalse(invalidState.isValid)

        val event = AddCategoryEvent.ColorSelected(color)
        viewModel.onEvent(event)
        val state = viewModel.state

        // then
        assert(state.isValid)
    }
}