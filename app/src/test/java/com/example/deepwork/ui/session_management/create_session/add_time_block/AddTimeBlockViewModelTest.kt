package com.example.deepwork.ui.session_management.create_session.add_time_block

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.deepwork.domain.business.TimeBlockValidator
import com.example.deepwork.domain.exception.TimeBlockException
import com.example.deepwork.domain.model.Category
import com.example.deepwork.domain.model.Result
import com.example.deepwork.domain.model.ScheduledTimeBlock
import com.example.deepwork.domain.model.template.TimeBlockTemplate
import com.example.deepwork.domain.usecase.timeblock.CreateTimeBlockUseCase
import com.example.deepwork.domain.usecase.timeblock.category.GetCategoriesUseCase
import com.example.deepwork.ui.util.UiEvent
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

@OptIn(ExperimentalCoroutinesApi::class)
class AddTimeBlockViewModelTest {
    private lateinit var viewModel: AddTimeBlockViewModel
    private lateinit var createTimeBlock: CreateTimeBlockUseCase
    private lateinit var timeBlockValidator: TimeBlockValidator
    private lateinit var getCategories: GetCategoriesUseCase
    private var testScope: TestScope? = null
    private lateinit var testCategories: List<Category>

    @Before
    fun setup() {
        testCategories = listOf(
            Category.create("Coding", Color.Blue.toArgb()),
            Category.create("Design", Color.Red.toArgb()),
            Category.create("Writing", Color.Green.toArgb()),
            Category.create("Research", Color.Yellow.toArgb()),
            Category.create("Learning", Color.Magenta.toArgb()),
        )
        Dispatchers.setMain(StandardTestDispatcher())
        createTimeBlock = mockk()
        timeBlockValidator = mockk() {
            every { validateDuration(any(), any()) } just Runs
            every { validateCategories(any(), any()) } just Runs
            every { validate(any<TimeBlockTemplate>()) } just Runs
        }
        getCategories = mockk()
        coEvery { getCategories() } returns flowOf(Result.Success(testCategories))
    }

    private fun initViewModel() {
        viewModel = AddTimeBlockViewModel(createTimeBlock, timeBlockValidator, getCategories)
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
        val expectedBlockType = ScheduledTimeBlock.BlockType.DEEP_WORK
        initViewModel()

        // when
        val state = viewModel.state

        // then
        assert(state.selectedBlockType == expectedBlockType)
    }

    @Test
    fun `when selectedBlockType is DEEP, duration placeholder should be DEEP MIN to MAX`() {
        // given
        val expectedDurationPlaceholder = "${ScheduledTimeBlock.Durations.DEEP_WORK_DURATION_MIN.inWholeMinutes} to ${ScheduledTimeBlock.Durations.DEEP_WORK_DURATION_MAX.inWholeMinutes} minutes"
        initViewModel()

        // when
        val state = viewModel.state

        // then
        assert(state.duration.placeHolder == expectedDurationPlaceholder)
    }

    @Test
    fun `when selectedBlockType is SHALLOW, duration placeholder should be SHALLOW MIN to MAX`() {
        // given
        val expectedDurationPlaceholder = "${ScheduledTimeBlock.Durations.SHALLOW_WORK_DURATION_MIN.inWholeMinutes} to ${ScheduledTimeBlock.Durations.SHALLOW_WORK_DURATION_MAX.inWholeMinutes} minutes"
        initViewModel()

        // when
        val event = AddTimeBlockEvent.BlockTypeSelected(ScheduledTimeBlock.BlockType.SHALLOW_WORK)
        viewModel.onEvent(event)
        val state = viewModel.state

        // then
        assert(state.duration.placeHolder == expectedDurationPlaceholder)
    }

    @Test
    fun `when selectedBlockType is BREAK, duration placeholder should be BREAK MIN to MAX`() {
        // given
        val expectedDurationPlaceholder = "${ScheduledTimeBlock.Durations.BREAK_DURATION_MIN.inWholeMinutes} to ${ScheduledTimeBlock.Durations.BREAK_DURATION_MAX.inWholeMinutes} minutes"
        initViewModel()

        // when
        val event = AddTimeBlockEvent.BlockTypeSelected(ScheduledTimeBlock.BlockType.BREAK)
        viewModel.onEvent(event)
        val state = viewModel.state

        // then
        assert(state.duration.placeHolder == expectedDurationPlaceholder)
    }

    @Test
    fun `onEvent BlockTypeSelected when selected block type is DEEP, selectedBlockType should be DEEP`() {
        // given
        val expectedBlockType = ScheduledTimeBlock.BlockType.DEEP_WORK
        initViewModel()

        // when
        val event = AddTimeBlockEvent.BlockTypeSelected(ScheduledTimeBlock.BlockType.DEEP_WORK)
        viewModel.onEvent(event)
        val state = viewModel.state

        // then
        assert(state.selectedBlockType == expectedBlockType)
    }

    @Test
    fun `onEvent BlockTypeSelected when selected block type is SHALLOW, selectedBlockType should be SHALLOW`() {
        // given
        val expectedBlockType = ScheduledTimeBlock.BlockType.SHALLOW_WORK
        initViewModel()

        // when
        val event = AddTimeBlockEvent.BlockTypeSelected(ScheduledTimeBlock.BlockType.SHALLOW_WORK)
        viewModel.onEvent(event)
        val state = viewModel.state

        // then
        assert(state.selectedBlockType == expectedBlockType)
    }

    @Test
    fun `onEvent BlockTypeSelected when selected block type is BREAK, selectedBlockType should be BREAK`() {
        // given
        val expectedBlockType = ScheduledTimeBlock.BlockType.BREAK
        initViewModel()

        // when
        val event = AddTimeBlockEvent.BlockTypeSelected(ScheduledTimeBlock.BlockType.BREAK)
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

        val event = AddTimeBlockEvent.BlockTypeSelected(ScheduledTimeBlock.BlockType.SHALLOW_WORK)
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
    fun `onEvent DurationChanged should revalidate timeBlock`() = runTest {
        // given
        initViewModel()

        // when
        val event = AddTimeBlockEvent.DurationChanged("30")
        viewModel.onEvent(event)

        // then
        coVerify { timeBlockValidator.validate(any<TimeBlockTemplate>()) }
    }

    @Test
    fun `onEvent DurationChanged when timeBlock is valid, isValid should be true`() = runTest {
        // given
        coEvery { timeBlockValidator.validate(any<TimeBlockTemplate>()) } just Runs
        initViewModel()

        // when
        val event = AddTimeBlockEvent.DurationChanged("30")
        viewModel.onEvent(event)
        val actualIsValid = viewModel.state.isValid

        // then
        assert(actualIsValid)
    }

    @Test
    fun `onEvent DurationChanged when timeBlock is not valid, isValid should not be true`() = runTest {
        // given
        coEvery { timeBlockValidator.validate(any<TimeBlockTemplate>()) } throws TimeBlockException.InvalidDurationTooShort("10")
        initViewModel()

        // when
        val event = AddTimeBlockEvent.DurationChanged("5")
        viewModel.onEvent(event)
        val actualIsValid = viewModel.state.isValid

        // then
        assertFalse(actualIsValid)
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
        val expectedCategories = testCategories
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
            category = testCategories.first(),
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
        val event = AddTimeBlockEvent.CategorySelected(testCategories.first())
        viewModel.onEvent(event)
        val actualSelectedCategories = viewModel.state.selectedCategoriesCount

        // then
        assertEquals(expectedSelectedCategories, actualSelectedCategories)
    }

    @Test
    fun `onEvent CategorySelected when category is already selected, should remain isSelected == true`() = runTest {
        // given
        val category = testCategories.first()
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
        val category = testCategories.first()
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
        repeat(ScheduledTimeBlock.CATEGORIES_MAX) { count ->
            val category = testCategories[count]
            val event = AddTimeBlockEvent.CategorySelected(category)
            viewModel.onEvent(event)
        }
        val actualEvents = mutableListOf<UiEvent>()
        val job = launch { viewModel.uiEvent.toList(actualEvents) }
        val event = AddTimeBlockEvent.CategorySelected(testCategories.last())
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
        val event = AddTimeBlockEvent.CategorySelected(testCategories.first())
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
        val event = AddTimeBlockEvent.CategorySelected(testCategories.first())
        viewModel.onEvent(event)
        val actualSelectedCategories = viewModel.state.selectedCategoriesCount

        // then
        assertEquals(expectedSelectedCategoriesCount, actualSelectedCategories)
    }

    @Test
    fun `onEvent CategorySelected should revalidate timeblock`() = runTest {
        // given
        val category = testCategories.first()
        initViewModel(this)

        // when
        val event = AddTimeBlockEvent.CategorySelected(category)
        viewModel.onEvent(event)

        // then
        coVerify { timeBlockValidator.validate(any<TimeBlockTemplate>()) }
    }

    @Test
    fun `onEvent CategorySelected when timeblock is valid, isValid should be true`() = runTest {
        // given
        coEvery { timeBlockValidator.validate(any<TimeBlockTemplate>()) } just Runs
        val category = testCategories.first()
        initViewModel(this)

        // when
        val event = AddTimeBlockEvent.CategorySelected(category)
        viewModel.onEvent(event)
        val actualIsValid = viewModel.state.isValid

        // then
        assert(actualIsValid)
    }

    @Test
    fun `onEvent CategorySelected when timeBlockValidator throws TimeBlockException, isValid should be false`() = runTest {
        // given
        coEvery { timeBlockValidator.validate(any<TimeBlockTemplate>()) } throws TimeBlockException.InvalidCategoriesCount()
        val category = testCategories.first()
        initViewModel(this)

        // when
        val event = AddTimeBlockEvent.CategorySelected(category)
        viewModel.onEvent(event)
        val actualIsValid = viewModel.state.isValid

        // then
        assertFalse(actualIsValid)
    }

    @Test
    fun `onEvent CreateCategoryClicked should set showAddCategoryBottomSheet to true`() {
        // given
        val expectedShowAddCategoryBottomSheet = true
        initViewModel()

        // when
        val event = AddTimeBlockEvent.CreateCategoryClicked
        viewModel.onEvent(event)
        val state = viewModel.state

        // then
        assert(state.showAddCategoryBottomSheet == expectedShowAddCategoryBottomSheet)
    }

    @Test
    fun `onEvent AddCategoryBottomSheetDismissed should set showAddCategoryBottomSheet to false`() {
        // given
        val expectedShowAddCategoryBottomSheet = false
        initViewModel()

        // when
        val event = AddTimeBlockEvent.AddCategoryBottomSheetDismissed
        viewModel.onEvent(event)
        val state = viewModel.state

        // then
        assert(state.showAddCategoryBottomSheet == expectedShowAddCategoryBottomSheet)
    }

    @Test
    fun `onEvent CategoryUnselected should set isSelected to false on Category`() = runTest {
        // given
        val selectedCategory = testCategories.first()
        initViewModel(this)

        // when
        // select the category first
        val selectedEvent = AddTimeBlockEvent.CategorySelected(selectedCategory)
        viewModel.onEvent(selectedEvent)
        advanceUntilIdle()
        // then unselect it
        val unselectedEvent = AddTimeBlockEvent.CategoryUnselected(selectedCategory)
        viewModel.onEvent(unselectedEvent)


        // then
        val actualState = viewModel.state
        val actualCategory = actualState.categories.first { it.category.id == selectedCategory.id }
        assertFalse(actualCategory.isSelected)
    }

    @Test
    fun `onEvent CategoryUnselected should decrement selectedCategoriesCount`() = runTest {
        // given
        val selectedCategory = testCategories.first()
        initViewModel(this)

        // when
        // select the category first
        val selectedEvent = AddTimeBlockEvent.CategorySelected(selectedCategory)
        viewModel.onEvent(selectedEvent)
        advanceUntilIdle()
        val selectedCategoriesCountBefore = viewModel.state.selectedCategoriesCount
        val expectedCategoriesCount = selectedCategoriesCountBefore - 1
        // then unselect it
        val unselectedEvent = AddTimeBlockEvent.CategoryUnselected(selectedCategory)
        viewModel.onEvent(unselectedEvent)
        val selectedCategoriesCountAfter = viewModel.state.selectedCategoriesCount

        // then
        assertEquals(expectedCategoriesCount, selectedCategoriesCountAfter)
    }

    @Test
    fun `onEvent CategoryUnselected should re-validate timeBlock`() = runTest {
        // given
        val selectedCategory = testCategories.first()
        initViewModel(this)

        // when
        // select the category first
        val selectedEvent = AddTimeBlockEvent.CategorySelected(selectedCategory)
        viewModel.onEvent(selectedEvent)
        advanceUntilIdle()
        // then unselect it
        val unselectedEvent = AddTimeBlockEvent.CategoryUnselected(selectedCategory)
        viewModel.onEvent(unselectedEvent)

        // then
        // twice because validated when selected
        coVerify(exactly = 2) { timeBlockValidator.validate(any<TimeBlockTemplate>()) }
    }

    @Test
    fun `onEvent CategoryUnselected when timeBlockValidator throws TimeBlockException, isValid should be false`() = runTest {
        // given
        coEvery { timeBlockValidator.validate(any<TimeBlockTemplate>()) } throws TimeBlockException.InvalidCategoriesCount()
        val selectedCategory = testCategories.first()
        initViewModel(this)

        // when
        // select the category first
        val selectedEvent = AddTimeBlockEvent.CategorySelected(selectedCategory)
        viewModel.onEvent(selectedEvent)
        advanceUntilIdle()
        // then unselect it
        val unselectedEvent = AddTimeBlockEvent.CategoryUnselected(selectedCategory)
        viewModel.onEvent(unselectedEvent)
        val actualIsValid = viewModel.state.isValid

        // then
        assertFalse(actualIsValid)
    }

    @Test
    fun `onEvent CategoryUnselected when validating categories fails, should show error message`() = runTest {
        // given
        every { timeBlockValidator.validateCategories(any(), any()) } throws IllegalStateException()
        val selectedCategory = testCategories.first()
        initViewModel(this)

        // when
        // select the category first
        val selectedEvent = AddTimeBlockEvent.CategorySelected(selectedCategory)
        viewModel.onEvent(selectedEvent)
        advanceUntilIdle()
        // then unselect it
        val actualEvents = mutableListOf<UiEvent>()
        val job = launch { viewModel.uiEvent.toList(actualEvents) }
        val unselectedEvent = AddTimeBlockEvent.CategoryUnselected(selectedCategory)
        viewModel.onEvent(unselectedEvent)
        advanceUntilIdle()
        val actualEvent = actualEvents.firstOrNull()

        // then
        assert(actualEvent is UiEvent.ShowSnackbar)
        job.cancel()
    }

    @Test
    fun `onEvent CategoryUnselected when timeBlock is valid, isValid should be true`() = runTest {
        // given
        val selectedCategory = testCategories.first()
        initViewModel(this)

        // when
        // select the category first
        val selectedEvent = AddTimeBlockEvent.CategorySelected(selectedCategory)
        viewModel.onEvent(selectedEvent)
        advanceUntilIdle()
        // then unselect it
        val unselectedEvent = AddTimeBlockEvent.CategoryUnselected(selectedCategory)
        viewModel.onEvent(unselectedEvent)
        val actualIsValid = viewModel.state.isValid

        // then
        assert(actualIsValid)
    }

    @Test
    fun `onEvent CategoryUnselected if category was already unselected, category should remain unselected`() = runTest {
        // given
        val selectedCategory = testCategories.first()
        initViewModel(this)

        // when
        // select the category first
        val selectedEvent = AddTimeBlockEvent.CategorySelected(selectedCategory)
        viewModel.onEvent(selectedEvent)
        advanceUntilIdle()
        // then unselect it
        val unselectedEvent = AddTimeBlockEvent.CategoryUnselected(selectedCategory)
        viewModel.onEvent(unselectedEvent)
        advanceUntilIdle()
        // then unselect it again
        viewModel.onEvent(unselectedEvent)
        val actualCategories = viewModel.state.categories
        val actualCategory = actualCategories.first { it.category.id == selectedCategory.id }

        // then
        assertFalse(actualCategory.isSelected)
    }

    @Test
    fun `onEvent SaveClicked should invoke createTimeBlock use case`() = runTest {
        // given
        coEvery { createTimeBlock(any()) } returns Result.Success(mockk())
        initViewModel(this)
        viewModel.onEvent(AddTimeBlockEvent.DurationChanged("30"))

        // when
        viewModel.onEvent(AddTimeBlockEvent.SaveClicked)
        advanceUntilIdle()

        // then
        coVerify { createTimeBlock(any()) }
    }

    @Test
    fun `onEvent SaveClicked when createTimeBlock succeeds, should send NavigateUp UiEvent`() = runTest {
        // given
        coEvery { createTimeBlock(any()) } returns Result.Success(mockk())
        initViewModel(this)
        viewModel.onEvent(AddTimeBlockEvent.DurationChanged("30"))

        // when
        val actualEvents = mutableListOf<UiEvent>()
        val job = launch { viewModel.uiEvent.toList(actualEvents) }
        viewModel.onEvent(AddTimeBlockEvent.SaveClicked)
        advanceUntilIdle()

        // then
        assertEquals(UiEvent.NavigateUp, actualEvents.first())
        job.cancel()
    }

    @Test
    fun `onEvent SaveClicked when createTimeBlock fails, should send ShowSnackbar UiEvent`() = runTest {
        // given
        coEvery { createTimeBlock(any()) } returns Result.Error(Exception("Save failed"))
        initViewModel(this)
        viewModel.onEvent(AddTimeBlockEvent.DurationChanged("30"))

        // when
        val actualEvents = mutableListOf<UiEvent>()
        val job = launch { viewModel.uiEvent.toList(actualEvents) }
        viewModel.onEvent(AddTimeBlockEvent.SaveClicked)
        advanceUntilIdle()

        // then
        assert(actualEvents.first() is UiEvent.ShowSnackbar)
        job.cancel()
    }

}