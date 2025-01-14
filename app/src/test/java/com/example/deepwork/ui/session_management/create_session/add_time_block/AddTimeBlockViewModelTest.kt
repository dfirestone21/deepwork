package com.example.deepwork.ui.session_management.create_session.add_time_block

import com.example.deepwork.domain.business.TimeBlockValidator
import com.example.deepwork.domain.exception.TimeBlockException
import com.example.deepwork.domain.model.TimeBlock
import com.example.deepwork.domain.usecase.timeblock.CreateBreakBlockUseCase
import com.example.deepwork.domain.usecase.timeblock.CreateWorkBlockUseCase
import com.example.deepwork.ui.util.UiEvent
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class
AddTimeBlockViewModelTest {
    private lateinit var viewModel: AddTimeBlockViewModel
    private lateinit var createWorkBlock: CreateWorkBlockUseCase
    private lateinit var createBreakBlock: CreateBreakBlockUseCase
    private lateinit var timeBlockValidator: TimeBlockValidator

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        createWorkBlock = mockk()
        createBreakBlock = mockk()
        timeBlockValidator = mockk() {
            every { validateDuration(any(), any()) } just Runs
        }
        viewModel = AddTimeBlockViewModel(createWorkBlock, createBreakBlock, timeBlockValidator)
    }

    @Test
    fun `default selectedBlockType should be DEEP`() {
        // given
        val expectedBlockType = TimeBlock.BlockType.DEEP

        // when
        val state = viewModel.state

        // then
        assert(state.selectedBlockType == expectedBlockType)
    }

    @Test
    fun `when selectedBlockType is DEEP, duration placeholder should be DEEP MIN to MAX`() {
        // given
        val expectedDurationPlaceholder = "${TimeBlock.WorkBlock.DeepWorkBlock.DURATION_MIN.inWholeMinutes} to ${TimeBlock.WorkBlock.DeepWorkBlock.DURATION_MAX.inWholeMinutes} minutes"

        // when
        val state = viewModel.state

        // then
        assert(state.duration.placeHolder == expectedDurationPlaceholder)
    }

    @Test
    fun `when selectedBlockType is SHALLOW, duration placeholder should be SHALLOW MIN to MAX`() {
        // given
        val expectedDurationPlaceholder = "${TimeBlock.WorkBlock.ShallowWorkBlock.DURATION_MIN.inWholeMinutes} to ${TimeBlock.WorkBlock.ShallowWorkBlock.DURATION_MAX.inWholeMinutes} minutes"

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
        val initialDuration = viewModel.state.duration.value
        val expectedDuration = "5"
        every { timeBlockValidator.validateDuration(any(), any()) } throws TimeBlockException.InvalidDurationTooShort("10")

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

}