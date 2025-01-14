package com.example.deepwork.ui.session_management.create_session.add_time_block

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deepwork.domain.business.TimeBlockValidator
import com.example.deepwork.domain.model.TimeBlock
import com.example.deepwork.domain.usecase.timeblock.CreateBreakBlockUseCase
import com.example.deepwork.domain.usecase.timeblock.CreateWorkBlockUseCase
import com.example.deepwork.ui.model.InputField
import com.example.deepwork.ui.util.UiEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes

class AddTimeBlockViewModel @Inject constructor(
    private val createWorkBlock: CreateWorkBlockUseCase,
    private val createBreakBlock: CreateBreakBlockUseCase,
    private val timeBlockValidator: TimeBlockValidator
) : ViewModel() {

    var state by mutableStateOf(AddTimeBlockState(
        duration = InputField(
            placeHolder = placeHolderFromBlockType(TimeBlock.BlockType.DEEP),
        )
    ))
        private set

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun onEvent(event: AddTimeBlockEvent) {
        when (event) {
            is AddTimeBlockEvent.BlockTypeSelected -> onBlockTypeSelected(event.blockType)
            is AddTimeBlockEvent.DurationChanged -> onDurationChanged(event.duration)
            is AddTimeBlockEvent.CancelClicked -> state = state.copy(showConfirmCancelDialog = true)
            is AddTimeBlockEvent.ConfirmCancelClicked -> onConfirmCancelClicked()
            is AddTimeBlockEvent.DismissCancelClicked -> state = state.copy(showConfirmCancelDialog = false)
            is AddTimeBlockEvent.NavigateUp -> sendNavigateUpEvent()
            is AddTimeBlockEvent.SaveClicked -> TODO()
            is AddTimeBlockEvent.CategorySelected -> TODO()
            is AddTimeBlockEvent.CategoryUnselected -> TODO()
        }
    }

    private fun onBlockTypeSelected(blockType: TimeBlock.BlockType) {
        state = state.copy(
            selectedBlockType = blockType,
            duration = state.duration.copy(
                value = "",
                placeHolder = placeHolderFromBlockType(blockType)
            )
        )
    }

    private fun onDurationChanged(durationString: String) {
        val duration = try {
            durationString.toInt().minutes
        } catch (e: NumberFormatException) {
            val errorMessage = "Duration must be a number"
            updateDurationError(errorMessage)
            return
        }

        val durationError = try {
            timeBlockValidator.validateDuration(state.selectedBlockType, duration)
            null
        } catch (e: Exception) {
            e.message ?: "Invalid duration"
        }

        state = state.copy(
            duration = state.duration.copy(
                value = durationString,
                message = durationError,
                isError = durationError != null
            )
        )
    }

    private fun updateDurationError(errorMessage: String) {
        state = state.copy(
            duration = state.duration.copy(
                message = errorMessage,
                isError = true
            )
        )
    }

    private fun onConfirmCancelClicked() {
        sendNavigateUpEvent()
        state = state.copy(showConfirmCancelDialog = false)
    }

    private fun sendNavigateUpEvent() {
        viewModelScope.launch {
            _uiEvent.send(UiEvent.NavigateUp)
        }
    }

    private fun placeHolderFromBlockType(blockType: TimeBlock.BlockType): String {
        val min = TimeBlock.minDuration(blockType).inWholeMinutes
        val max = TimeBlock.maxDuration(blockType).inWholeMinutes
        return "$min to $max minutes"
    }

}