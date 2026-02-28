package com.example.deepwork.ui.session_management.create_session

import androidx.lifecycle.SavedStateHandle
import com.example.deepwork.domain.exception.SessionException
import com.example.deepwork.domain.model.Result
import com.example.deepwork.domain.model.ScheduledSession
import com.example.deepwork.domain.model.ScheduledTimeBlock
import com.example.deepwork.domain.usecase.session.AddTimeBlockUseCase
import com.example.deepwork.domain.usecase.session.CreateSessionUseCase
import com.example.deepwork.domain.usecase.session.SaveSessionUseCase
import com.example.deepwork.domain.usecase.session.validate.ValidateSessionNameUseCase
import com.example.deepwork.ui.model.TimeBlockUi
import com.example.deepwork.ui.navigation.Routes
import com.example.deepwork.ui.util.UiEvent
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CreateSessionViewModelTest {
    private lateinit var viewModel: CreateSessionViewModel
    private lateinit var createSession: CreateSessionUseCase
    private lateinit var addTimeBlock: AddTimeBlockUseCase
    private lateinit var validateName: ValidateSessionNameUseCase
    private lateinit var saveSession: SaveSessionUseCase
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var testScope: TestScope

    @Before
    fun setup() {
        validateName = mockk()
        every { validateName(any()) } answers { Result.Success(firstArg()) }
        createSession = CreateSessionUseCase(validateName)
        addTimeBlock = AddTimeBlockUseCase()
        saveSession = mockk()
        coEvery { saveSession(any()) } returns Result.Success(mockk())
        savedStateHandle = mockk()
        val initialState = CreateSessionUiState()
        val initialSession = ScheduledSession.create("", System.currentTimeMillis())

        every { savedStateHandle.get<CreateSessionUiState>("state") } returns initialState
        every { savedStateHandle.get<ScheduledSession>("session") } returns initialSession
        every { savedStateHandle.set(any(), any<Any>()) } just runs
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @Test
    fun `onEvent() UpdateName should update the session name`() = runTest {
        // given
        val expectedName = "New Session 1"
        val event = CreateSessionEvent.UpdateName(expectedName)
        initViewModel(this)

        // when
        viewModel.onEvent(event)
        val uiState = uiState()
        val actualName = uiState.name.value

        // then
        assertEquals(expectedName, actualName)
    }

    @Test
    fun `onEvent() UpdateName when validate name returns SessionException, should set error message on name field`() = runTest {
        // given
        val invalidName = "(*&^*(^&*^*^&(U^(*&^(*^)"
        val event = CreateSessionEvent.UpdateName(invalidName)
        every { validateName(any()) } returns Result.Error(SessionException.InvalidName("Invalid name"))
        initViewModel(this)

        // when
        viewModel.onEvent(event)
        val uiState = uiState()
        val actualName = uiState.name.value
        val actualError = uiState.name.message

        // then
        assertEquals(invalidName, actualName)
        assertNotNull(actualError)
    }

    @Test
    fun `onEvent() UpdateName when validate name returns a different exception, should not update name error`() = runTest {
        // given
        val validName = "Session"
        val event = CreateSessionEvent.UpdateName(validName)
        every { validateName(any()) } returns Result.Error(NullPointerException("Error!"))
        initViewModel(this)

        // when
        viewModel.onEvent(event)
        val uiState = uiState()
        val actualName = uiState.name.value
        val actualError = uiState.name.message

        // then
        assertEquals(validName, actualName)
        assertNull(actualError)
    }

    @Test
    fun `onEvent() UpdateName when submitting a valid name, should clear any previous error on name field`() =
        runTest {
            // given
            val invalidName = "(*&^*(^&*^*^&(U^(*&^(*^)"
            val event = CreateSessionEvent.UpdateName(invalidName)
            every { validateName(any()) } returnsMany (
                    listOf(
                        Result.Error(SessionException.InvalidName("Invalid name")),
                        Result.Success("Session")
                    )
                    )
            initViewModel(this)

            // when
            viewModel.onEvent(event)
            advanceUntilIdle()
            val validNameEvent = CreateSessionEvent.UpdateName("Session")
            viewModel.onEvent(validNameEvent)
            val uiState = uiState()
            val actualName = uiState.name.value
            val actualError = uiState.name.message

            // then
            assertEquals("Session", actualName)
            assertNull(actualError)
        }

    @Test
    fun `onEvent() AddTimeBlock should add time block to time blocks`() = runTest {
        // given
        val block = ScheduledTimeBlock.deepWorkBlock()
        val event = CreateSessionEvent.AddTimeBlock(block)
        val expectedBlock = TimeBlockUi.fromDomain(block)
        initViewModel(this)

        // when
        viewModel.onEvent(event)
        val uiState = uiState()
        val actualTimeBlocks = uiState.timeBlocks

        // then
        assert(expectedBlock in actualTimeBlocks)
    }

    @Test
    fun `onEvent() AddTimeBlock when adding a block results in an error, should set the error message`() = runTest {
        // given
        val block = ScheduledTimeBlock.breakBlock() // can't start with break block
        val event = CreateSessionEvent.AddTimeBlock(block)
        initViewModel(this)

        // when
        viewModel.onEvent(event)
        val uiState = uiState()
        val actualMessage = uiState.message

        // then
        assertNotNull(actualMessage)
    }

    @Test
    fun `onEvent() FabClicked should send CREATE_TIMEBLOCK Navigate UiEvent`() = runTest {
        // given
        val event = CreateSessionEvent.FabClicked
        val expectedUiEvent = UiEvent.Navigate(Routes.CREATE_TIMEBLOCK)
        initViewModel(this)

        // when
        val events = mutableListOf<UiEvent>()
        val job = launch { viewModel.uiEvent.toList(events) }
        viewModel.onEvent(event)
        advanceUntilIdle()
        val actualUiEvent = events.first()

        // then
        assertEquals(1, events.size)
        assertEquals(expectedUiEvent, actualUiEvent)

        job.cancel()
    }

    @Test
    fun `onEvent() SaveClicked when save succeeds, should send NavigateUp UiEvent`() = runTest {
        // given
        val event = CreateSessionEvent.SaveClicked
        initViewModel(this)

        // when
        val events = mutableListOf<UiEvent>()
        val job = launch { viewModel.uiEvent.toList(events) }
        viewModel.onEvent(event)
        advanceUntilIdle()

        // then
        assertEquals(1, events.size)
        assertEquals(UiEvent.NavigateUp, events.first())

        job.cancel()
    }

    @Test
    fun `onEvent() SaveClicked when save fails with InvalidName, should set name field isError and message`() = runTest {
        // given
        val errorMessage = "Session name is invalid"
        coEvery { saveSession(any()) } returns Result.Error(SessionException.InvalidName(errorMessage))
        val event = CreateSessionEvent.SaveClicked
        initViewModel(this)

        // when
        viewModel.onEvent(event)
        val uiState = uiState()

        // then
        assertTrue(uiState.name.isError)
        assertEquals(errorMessage, uiState.name.message)
    }

    @Test
    fun `onEvent() SaveClicked when save fails with MinTimeBlocksReached, should send ShowSnackbar UiEvent`() = runTest {
        // given
        coEvery { saveSession(any()) } returns Result.Error(SessionException.MinTimeBlocksReached())
        val event = CreateSessionEvent.SaveClicked
        initViewModel(this)

        // when
        val events = mutableListOf<UiEvent>()
        val job = launch { viewModel.uiEvent.toList(events) }
        viewModel.onEvent(event)
        advanceUntilIdle()

        // then
        assertEquals(1, events.size)
        assertEquals(UiEvent.ShowSnackbar("Add at least one work block"), events.first())

        job.cancel()
    }

    @Test
    fun `onEvent() SaveClicked when save fails with any other exception, should send generic ShowSnackbar UiEvent`() = runTest {
        // given
        coEvery { saveSession(any()) } returns Result.Error(RuntimeException("Unexpected error"))
        val event = CreateSessionEvent.SaveClicked
        initViewModel(this)

        // when
        val events = mutableListOf<UiEvent>()
        val job = launch { viewModel.uiEvent.toList(events) }
        viewModel.onEvent(event)
        advanceUntilIdle()

        // then
        assertEquals(1, events.size)
        assertEquals(UiEvent.ShowSnackbar("Failed to save session"), events.first())

        job.cancel()
    }

    private fun initViewModel(coroutineScope: TestScope, advanceUntilIdle: Boolean = true) {
        viewModel = CreateSessionViewModel(createSession, addTimeBlock, validateName, saveSession, savedStateHandle)
        testScope = coroutineScope
        if (advanceUntilIdle) coroutineScope.advanceUntilIdle()
    }

    private fun uiState(advanceUntilIdle: Boolean = true): CreateSessionUiState {
        if (advanceUntilIdle) testScope.advanceUntilIdle()
        return viewModel.state
    }
}