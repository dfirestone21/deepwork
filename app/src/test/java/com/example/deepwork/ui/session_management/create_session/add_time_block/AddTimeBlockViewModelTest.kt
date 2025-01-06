package com.example.deepwork.ui.session_management.create_session.add_time_block

import com.example.deepwork.domain.business.TimeBlockValidator
import com.example.deepwork.domain.model.TimeBlock
import com.example.deepwork.domain.usecase.timeblock.CreateBreakBlockUseCase
import com.example.deepwork.domain.usecase.timeblock.CreateWorkBlockUseCase
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test

class AddTimeBlockViewModelTest {
    private lateinit var viewModel: AddTimeBlockViewModel
    private lateinit var createWorkBlock: CreateWorkBlockUseCase
    private lateinit var createBreakBlock: CreateBreakBlockUseCase
    private lateinit var timeBlockValidator: TimeBlockValidator

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        createWorkBlock = mockk()
        createBreakBlock = mockk()
        timeBlockValidator = mockk()
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
        val expectedDurationPlaceholder = "${TimeBlock.WorkBlock.DeepWorkBlock.DURATION_MIN} to ${TimeBlock.WorkBlock.DeepWorkBlock.DURATION_MAX} minutes"

        // when
        val state = viewModel.state

        // then
        assert(state.duration.placeHolder == expectedDurationPlaceholder)
    }

    @Test
    fun `when selectedBlockType is SHALLOW, duration placeholder should be SHALLOW MIN to MAX`() {
        // given
        val expectedDurationPlaceholder = "${TimeBlock.WorkBlock.ShallowWorkBlock.DURATION_MIN} to ${TimeBlock.WorkBlock.ShallowWorkBlock.DURATION_MAX} minutes"

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
        val expectedDurationPlaceholder = "${TimeBlock.BreakBlock.DURATION_MIN} to ${TimeBlock.BreakBlock.DURATION_MAX} minutes"

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

        // then
        assert(state.duration.value == expectedDuration)
    }

}