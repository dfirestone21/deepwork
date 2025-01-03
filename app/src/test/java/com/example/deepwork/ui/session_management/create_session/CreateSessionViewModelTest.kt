package com.example.deepwork.ui.session_management.create_session

import androidx.lifecycle.SavedStateHandle
import com.example.deepwork.domain.exception.SessionException
import com.example.deepwork.domain.model.Result
import com.example.deepwork.domain.model.Session
import com.example.deepwork.domain.model.TimeBlock
import com.example.deepwork.domain.usecase.session.AddTimeBlockUseCase
import com.example.deepwork.domain.usecase.session.CreateSessionUseCase
import com.example.deepwork.domain.usecase.session.validate.ValidateSessionNameUseCase
import com.example.deepwork.ui.model.TimeBlockUi
import com.example.deepwork.ui.navigation.Routes
import com.example.deepwork.ui.util.UiEvent
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
import org.junit.Before
import org.junit.Test

class CreateSessionViewModelTest {
    private lateinit var viewModel: CreateSessionViewModel
    private lateinit var createSession: CreateSessionUseCase
    private lateinit var addTimeBlock: AddTimeBlockUseCase
    private lateinit var validateName: ValidateSessionNameUseCase
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var testScope: TestScope

    @Before
    fun setup() {
        validateName = mockk()
        every { validateName(any()) } answers { Result.Success(firstArg()) }
        createSession = CreateSessionUseCase(validateName)
        addTimeBlock = AddTimeBlockUseCase()
        savedStateHandle = mockk()
        val initialState = CreateSessionUiState()
        val initialSession = Session.create("")

        every { savedStateHandle.get<CreateSessionUiState>("state") } returns initialState
        every { savedStateHandle.get<Session>("session") } returns initialSession
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
        val block = TimeBlock.deepWorkBlock()
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
        val block = TimeBlock.breakBlock() // can't start with break block
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

    private fun initViewModel(coroutineScope: TestScope, advanceUntilIdle: Boolean = true) {
        viewModel = CreateSessionViewModel(createSession, addTimeBlock, validateName, savedStateHandle)
        testScope = coroutineScope
        if (advanceUntilIdle) coroutineScope.advanceUntilIdle()
    }

    private fun uiState(advanceUntilIdle: Boolean = true): CreateSessionUiState {
        if (advanceUntilIdle) testScope.advanceUntilIdle()
        return viewModel.state
    }
}