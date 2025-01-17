package com.example.deepwork.ui.session_management.create_session.add_time_block

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.deepwork.domain.business.TimeBlockValidator
import com.example.deepwork.domain.exception.TimeBlockException
import com.example.deepwork.domain.model.Category
import com.example.deepwork.domain.model.Result
import com.example.deepwork.domain.model.TimeBlock
import com.example.deepwork.domain.usecase.timeblock.CreateBreakBlockUseCase
import com.example.deepwork.domain.usecase.timeblock.CreateWorkBlockUseCase
import com.example.deepwork.domain.usecase.timeblock.category.GetCategoriesUseCase
import com.example.deepwork.ui.util.UiEvent
import io.mockk.Runs
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class AddTimeBlockViewModelTest {
    private lateinit var viewModel: AddTimeBlockViewModel
    private lateinit var createWorkBlock: CreateWorkBlockUseCase
    private lateinit var createBreakBlock: CreateBreakBlockUseCase
    private lateinit var timeBlockValidator: TimeBlockValidator
    private lateinit var getCategories: GetCategoriesUseCase
    private var testScope: TestScope? = null

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        createWorkBlock = mockk()
        createBreakBlock = mockk()
        timeBlockValidator = mockk() {
            every { validateDuration(any(), any()) } just Runs
            every { validateCategories(any(), any()) } just Runs
        }
        getCategories = mockk()
        coEvery { getCategories() } returns flowOf(Result.Success(testCategories()))
    }

    private fun initViewModel() {
        viewModel = AddTimeBlockViewModel(createWorkBlock, createBreakBlock, timeBlockValidator, getCategories)
    }

    private fun initViewModel(testScope: TestScope, advanceUntilIdle: Boolean = true) {
        initViewModel()
        this.testScope = testScope
        if (advanceUntilIdle) {
            testScope.advanceUntilIdle()
        }
    }

    @Test
    fun `default selectedBlockType should be DEEP`() {
        // given
        val expectedBlockType = TimeBlock.BlockType.DEEP
        initViewModel()

        // when
        val state = viewModel.state

        // then
        assert(state.selectedBlockType == expectedBlockType)
    }

    @Test
    fun `when selectedBlockType is DEEP, duration placeholder should be DEEP MIN to MAX`() {
        // given
        val expectedDurationPlaceholder = "${TimeBlock.WorkBlock.DeepWorkBlock.DURATION_MIN.inWholeMinutes} to ${TimeBlock.WorkBlock.DeepWorkBlock.DURATION_MAX.inWholeMinutes} minutes"
        initViewModel()

        // when
        val state = viewModel.state

        // then
        assert(state.duration.placeHolder == expectedDurationPlaceholder)
    }

    @Test
    fun `when selectedBlockType is SHALLOW, duration placeholder should be SHALLOW MIN to MAX`() {
        // given
        val expectedDurationPlaceholder = "${TimeBlock.WorkBlock.ShallowWorkBlock.DURATION_MIN.inWholeMinutes} to ${TimeBlock.WorkBlock.ShallowWorkBlock.DURATION_MAX.inWholeMinutes} minutes"
        initViewModel()

        // when
        val event = AddTimeBlockEvent.BlockTypeSelected(TimeBlock.BlockType.SHALLOW)
        viewModel.onEvent(event)
        val state = viewModel.state

        // then
        assert(state.duration.placeHolder == expectedDurationPlaceholder)
    }

    @Test
    fun `when selectedBlockType is BREAK, duration placeholder should be BREAK MIN to MAX`() {
        // given
        val expectedDurationPlaceholder = "${TimeBlock.BreakBlock.DURATION_MIN.inWholeMinutes} to ${TimeBlock.BreakBlock.DURATION_MAX.inWholeMinutes} minutes"
        initViewModel()

        // when
        val event = AddTimeBlockEvent.BlockTypeSelected(TimeBlock.BlockType.BREAK)
        viewModel.onEvent(event)
        val state = viewModel.state

        // then
        assert(state.duration.placeHolder == expectedDurationPlaceholder)
    }

    @Test
    fun `onEvent BlockTypeSelected when selected block type is DEEP, selectedBlockType should be DEEP`() {
        // given
        val expectedBlockType = TimeBlock.BlockType.DEEP
        initViewModel()

        // when
        val event = AddTimeBlockEvent.BlockTypeSelected(TimeBlock.BlockType.DEEP)
        viewModel.onEvent(event)
        val state = viewModel.state

        // then
        assert(state.selectedBlockType == expectedBlockType)
    }

    @Test
    fun `onEvent BlockTypeSelected when selected block type is SHALLOW, selectedBlockType should be SHALLOW`() {
        // given
        val expectedBlockType = TimeBlock.BlockType.SHALLOW
        initViewModel()

        // when
        val event = AddTimeBlockEvent.BlockTypeSelected(TimeBlock.BlockType.SHALLOW)
        viewModel.onEvent(event)
        val state = viewModel.state

        // then
        assert(state.selectedBlockType == expectedBlockType)
    }

    @Test
    fun `onEvent BlockTypeSelected when selected block type is BREAK, selectedBlockType should be BREAK`() {
        // given
        val expectedBlockType = TimeBlock.BlockType.BREAK
        initViewModel()

        // when
        val event = AddTimeBlockEvent.BlockTypeSelected(TimeBlock.BlockType.BREAK)
        viewModel.onEvent(event)
        val state = viewModel.state

        // then
        assert(state.selectedBlockType == expectedBlockType)
    }

    @Test
    fun `onEvent BlockTypeSelected should reset duration`() {
        // given
        val expectedDuration = ""
        initViewModel()

        // when
        val initialDurationEvent = AddTimeBlockEvent.DurationChanged("30")
        viewModel.onEvent(initialDurationEvent)
        val durationBefore = viewModel.state.duration.value
        assertNotEquals(durationBefore, expectedDuration)

        val event = AddTimeBlockEvent.BlockTypeSelected(TimeBlock.BlockType.SHALLOW)
        viewModel.onEvent(event)
        val state = viewModel.state
        val actualDuration = state.duration

        // then
        assert(actualDuration.value == expectedDuration)
    }

    @Test
    fun `onEvent DurationChanged when duration is valid, should update duration`() {
        // given
        val expectedDuration = "30"
        initViewModel()

        // when
        val event = AddTimeBlockEvent.DurationChanged(expectedDuration)
        viewModel.onEvent(event)
        val state = viewModel.state
        val actualDuration = state.duration

        // then
        assert(actualDuration.value == expectedDuration)
        assertFalse(actualDuration.isError)
    }

    @Test
    fun `onEvent DurationChanged when duration is not a number, duration should contain error message`() = runTest {
        // given
        val expectedError = "Duration must be a number"
        initViewModel()

        // when
        val event = AddTimeBlockEvent.DurationChanged("not a number")
        viewModel.onEvent(event)
        val state = viewModel.state
        val actualDuration = state.duration

        // then
        assert(actualDuration.message == expectedError)
        assert(actualDuration.isError)
    }

    @Test
    fun `onEvent DurationChanged when duration is too short, duration should contain error message`() = runTest {
        // given
        every { timeBlockValidator.validateDuration(any(), any()) } throws TimeBlockException.InvalidDurationTooShort("10")
        initViewModel()

        // when
        val event = AddTimeBlockEvent.DurationChanged("0")
        viewModel.onEvent(event)
        val state = viewModel.state
        val actualDuration = state.duration

        // then
        assertNotNull(actualDuration.message)
        assert(actualDuration.isError)
    }

    @Test
    fun `onEvent DurationChanged when duration is too long, duration should contain error message`() = runTest {
        // given
        every { timeBlockValidator.validateDuration(any(), any()) } throws TimeBlockException.InvalidDurationTooLong("10")
        initViewModel()

        // when
        val event = AddTimeBlockEvent.DurationChanged("1000")
        viewModel.onEvent(event)
        val state = viewModel.state
        val actualDuration = state.duration

        // then
        assertNotNull(actualDuration.message)
        assert(actualDuration.isError)
    }

    @Test
    fun `onEvent DurationChanged when duration is invalid, should still update duration`() = runTest {
        // given
        every { timeBlockValidator.validateDuration(any(), any()) } throws TimeBlockException.InvalidDurationTooShort("10")
        initViewModel()
        val initialDuration = viewModel.state.duration.value
        val expectedDuration = "5"

        // when
        val event = AddTimeBlockEvent.DurationChanged(expectedDuration)
        viewModel.onEvent(event)
        val state = viewModel.state
        val actualDuration = state.duration.value

        // then
        assert(initialDuration != actualDuration)
        assertEquals(expectedDuration, actualDuration)
    }

    @Test
    fun `onEvent CancelClicked should set showConfirmCancelDialog to true`() {
        // given
        val expectedShowConfirmCancelDialog = true
        initViewModel()

        // when
        val event = AddTimeBlockEvent.CancelClicked
        viewModel.onEvent(event)
        val state = viewModel.state

        // then
        assert(state.showConfirmCancelDialog == expectedShowConfirmCancelDialog)
    }

    @Test
    fun `onEvent ConfirmCancelClicked should send UiEvent NavigateUp`() = runTest {
        // given
        val expectedUiEvent = UiEvent.NavigateUp
        initViewModel()

        // when
        val actualEvents = mutableListOf<UiEvent>()
        val job = launch { viewModel.uiEvent.toList(actualEvents) }
        val event = AddTimeBlockEvent.ConfirmCancelClicked
        viewModel.onEvent(event)
        advanceUntilIdle()
        val actualEvent = actualEvents.first()

        // then
        assertEquals(expectedUiEvent, actualEvent)
        job.cancel()
    }

    @Test
    fun `onEvent ConfirmCancelClicked should set showConfirmCancelDialog to false`() {
        // given
        val expectedShowConfirmCancelDialog = false
        initViewModel()

        // when
        val firstEvent = AddTimeBlockEvent.CancelClicked
        viewModel.onEvent(firstEvent)
        val event = AddTimeBlockEvent.ConfirmCancelClicked
        viewModel.onEvent(event)
        val state = viewModel.state

        // then
        assert(state.showConfirmCancelDialog == expectedShowConfirmCancelDialog)
    }

    @Test
    fun `onEvent DismissCancelClicked should set showConfirmCancelDialog to false`() {
        // given
        val expectedShowConfirmCancelDialog = false
        initViewModel()

        // when
        val firstEvent = AddTimeBlockEvent.CancelClicked
        viewModel.onEvent(firstEvent)
        val event = AddTimeBlockEvent.DismissCancelClicked
        viewModel.onEvent(event)
        val state = viewModel.state

        // then
        assert(state.showConfirmCancelDialog == expectedShowConfirmCancelDialog)
    }

    @Test
    fun `onEvent NavigateUp should send UiEvent NavigateUp`() = runTest {
        // given
        val expectedUiEvent = UiEvent.NavigateUp
        initViewModel()

        // when
        val actualEvents = mutableListOf<UiEvent>()
        val job = launch { viewModel.uiEvent.toList(actualEvents) }
        val event = AddTimeBlockEvent.NavigateUp
        viewModel.onEvent(event)
        advanceUntilIdle()
        val actualEvent = actualEvents.first()

        // then
        assertEquals(expectedUiEvent, actualEvent)
        job.cancel()
    }

    @Test
    fun `UI State should contain users categories`() = runTest {
        // given
        val expectedCategories = testCategories()
        coEvery { getCategories() } returns flowOf(Result.Success(expectedCategories))
        initViewModel()

        // when
        advanceUntilIdle()
        val actualCategories = viewModel.state.categories

        // then
        assert(
            actualCategories.all { expectedCategories.contains(it.category) }
        )
    }

    @Test
    fun `When error fetching categories, should send error snackbar UiEvent`() = runTest {
        // given
        coEvery { getCategories() } returns flowOf(Result.Error(Exception("Error fetching categories")))
        initViewModel()

        // when
        val actualEvents = mutableListOf<UiEvent>()
        val job = launch { viewModel.uiEvent.toList(actualEvents) }
        advanceUntilIdle()
        val actualEvent = actualEvents.firstOrNull()

        // then
        assert(actualEvent is UiEvent.ShowSnackbar)
        job.cancel()
    }

    @Test
    fun `onEvent CategorySelected should set isSelected to true on Category`() = runTest {
        // given
        val expectedCategory = SelectableCategory(
            category = testCategories().first(),
            isSelected = true
        )
        initViewModel(this)

        // when
        val event = AddTimeBlockEvent.CategorySelected(expectedCategory.category)
        viewModel.onEvent(event)
        val actualCategories = viewModel.state.categories
        val actualCategory = actualCategories.first()

        // then
        assert(actualCategory.isSelected)
    }

    @Test
    fun `onEvent CategorySelected should increment selectedCategories count`() = runTest {
        // given
        val expectedSelectedCategories = 1
        initViewModel(this)

        // when
        val event = AddTimeBlockEvent.CategorySelected(testCategories().first())
        viewModel.onEvent(event)
        val actualSelectedCategories = viewModel.state.selectedCategoriesCount

        // then
        assertEquals(expectedSelectedCategories, actualSelectedCategories)
    }

    @Test
    fun `onEvent CategorySelected when category is already selected, should remain isSelected == true`() = runTest {
        // given
        val category = testCategories().first()
        initViewModel(this)

        // when
        val event = AddTimeBlockEvent.CategorySelected(category)
        viewModel.onEvent(event)
        // select it again
        viewModel.onEvent(event)
        val actualCategory = viewModel.state.categories.first()

        // then
        assert(actualCategory.isSelected)
    }

    @Test
    fun `onEvent CategorySelected when category is already selected, selectedCategory count should not change`() = runTest {
        // given
        val expectedSelectedCategories = 1
        val category = testCategories().first()
        initViewModel(this)

        // when
        val event = AddTimeBlockEvent.CategorySelected(category)
        viewModel.onEvent(event)
        viewModel.onEvent(event)
        val actualSelectedCategories = viewModel.state.selectedCategoriesCount

        // then
        assertEquals(expectedSelectedCategories, actualSelectedCategories)
    }

    @Test
    fun `onEvent CategorySelected when validating selected category fails, should show error snackbar`() = runTest {
        // given
        every { timeBlockValidator.validateCategories(any(), any()) } throws TimeBlockException.InvalidCategoriesCount()
        initViewModel(this)

        // when
        repeat(TimeBlock.WorkBlock.CATEGORIES_MAX) { count ->
            val category = testCategories()[count]
            val event = AddTimeBlockEvent.CategorySelected(category)
            viewModel.onEvent(event)
        }
        val actualEvents = mutableListOf<UiEvent>()
        val job = launch { viewModel.uiEvent.toList(actualEvents) }
        val event = AddTimeBlockEvent.CategorySelected(testCategories().last())
        viewModel.onEvent(event)
        advanceUntilIdle()
        val actualEvent = actualEvents.firstOrNull()

        // then
        assert(actualEvent is UiEvent.ShowSnackbar)
        job.cancel()
    }

    @Test
    fun `onEvent CategorySelected when validating selected category fails, should not set selected == true`() = runTest {
        // given
        every { timeBlockValidator.validateCategories(any(), any()) } throws Exception()
        initViewModel(this)

        // when
        val event = AddTimeBlockEvent.CategorySelected(testCategories().first())
        viewModel.onEvent(event)
        val actualCategories = viewModel.state.categories
        val actualCategory = actualCategories.first()

        // then
        assert(!actualCategory.isSelected)
    }

    @Test
    fun `onEvent CategorySelected when validating selected category fails, should not increment selectedCategories count`() = runTest {
        // given
        val expectedSelectedCategoriesCount = 0
        every { timeBlockValidator.validateCategories(any(), any()) } throws Exception()
        initViewModel(this)

        // when
        val event = AddTimeBlockEvent.CategorySelected(testCategories().first())
        viewModel.onEvent(event)
        val actualSelectedCategories = viewModel.state.selectedCategoriesCount

        // then
        assertEquals(expectedSelectedCategoriesCount, actualSelectedCategories)
    }

    private fun testCategories(): List<Category> {
        return listOf(
            Category("1", "Coding", Color.Blue.toArgb()),
            Category("2", "Design", Color.Red.toArgb()),
            Category("3", "Writing", Color.Green.toArgb()),
            Category("4", "Research", Color.Yellow.toArgb()),
            Category("5", "Learning", Color.Magenta.toArgb()),
        )
    }

}