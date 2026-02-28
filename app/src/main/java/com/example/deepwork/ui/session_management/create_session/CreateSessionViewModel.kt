package com.example.deepwork.ui.session_management.create_session

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deepwork.domain.exception.SessionException
import com.example.deepwork.domain.model.Result
import com.example.deepwork.domain.model.ScheduledSession
import com.example.deepwork.domain.model.ScheduledTimeBlock
import com.example.deepwork.domain.usecase.session.AddTimeBlockUseCase
import com.example.deepwork.domain.usecase.session.CreateSessionUseCase
import com.example.deepwork.domain.usecase.session.SaveSessionUseCase
import com.example.deepwork.domain.usecase.session.validate.ValidateSessionNameUseCase
import com.example.deepwork.ui.model.InputField
import com.example.deepwork.ui.model.TimeBlockUi
import com.example.deepwork.ui.navigation.Routes
import com.example.deepwork.ui.util.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateSessionViewModel @Inject constructor(
    private val createSession: CreateSessionUseCase,
    private val addTimeBlock: AddTimeBlockUseCase,
    private val validateName: ValidateSessionNameUseCase,
    private val saveSession: SaveSessionUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    var state by mutableStateOf(CreateSessionUiState())
        private set
    private var session = ScheduledSession.create("", System.currentTimeMillis())

    fun onEvent(event: CreateSessionEvent) {
        when (event) {
            is CreateSessionEvent.UpdateName -> updateName(event.name)
            is CreateSessionEvent.AddTimeBlock -> addTimeBlock(event.timeBlock)
            is CreateSessionEvent.FabClicked -> navigateToCreateTimeBlock()
            is CreateSessionEvent.SaveClicked -> saveSession()
        }
    }

    private fun updateName(name: String) {
        state = state.copy(name = InputField(name))
        validateName(name)
            .onError { exception, _ ->
                (exception as? SessionException)?.let {
                    state = state.copy(name = state.name.copy(message = it.message))
                }
            }
    }

    private fun addTimeBlock(timeBlock: ScheduledTimeBlock) {
        viewModelScope.launch {
            addTimeBlock(session, timeBlock)
                .onSuccess { updatedSession ->
                    session = updatedSession
                    val updatedTimeBlocks = updatedSession.timeBlocks.map { TimeBlockUi.fromDomain(it) }
                    state = state.copy(timeBlocks = updatedTimeBlocks)
                }.onError { exception, _ ->
                    state = state.copy(message = exception.message)
                }
        }
    }

    private fun navigateToCreateTimeBlock() {
        viewModelScope.launch {
            val uiEvent = UiEvent.Navigate(Routes.CREATE_TIMEBLOCK)
            _uiEvent.send(uiEvent)
        }
    }

    private fun saveSession() {
        viewModelScope.launch {
            when (val result = saveSession(session)) {
                is Result.Success -> _uiEvent.send(UiEvent.NavigateUp)
                is Result.Error -> when (val exception = result.exception) {
                    is SessionException.InvalidName -> {
                        state = state.copy(
                            name = state.name.copy(isError = true, message = exception.message)
                        )
                    }
                    is SessionException.MinTimeBlocksReached -> {
                        _uiEvent.send(UiEvent.ShowSnackbar("Add at least one work block"))
                    }
                    else -> _uiEvent.send(UiEvent.ShowSnackbar("Failed to save session"))
                }
            }
        }
    }
}