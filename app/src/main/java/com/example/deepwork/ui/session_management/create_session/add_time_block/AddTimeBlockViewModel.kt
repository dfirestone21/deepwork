package com.example.deepwork.ui.session_management.create_session.add_time_block

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.deepwork.domain.model.TimeBlock
import com.example.deepwork.domain.usecase.timeblock.CreateBreakBlockUseCase
import com.example.deepwork.domain.usecase.timeblock.CreateWorkBlockUseCase
import com.example.deepwork.ui.util.UiEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import kotlin.time.Duration

class AddTimeBlockViewModel @Inject constructor(
    private val createWorkBlock: CreateWorkBlockUseCase,
    private val createBreakBlock: CreateBreakBlockUseCase
) : ViewModel() {

    var state by mutableStateOf(AddTimeBlockState())
        private set

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun onEvent(event: AddTimeBlockEvent) {
        when (event) {
            is AddTimeBlockEvent.BlockTypeSelected -> onBlockTypeSelected(event.blockType)
            is AddTimeBlockEvent.DurationChanged -> onDurationChanged(event.duration)
            is AddTimeBlockEvent.CancelClicked -> TODO()
            is AddTimeBlockEvent.NavigateUp -> TODO()
            is AddTimeBlockEvent.SaveClicked -> TODO()
        }
    }

    private fun onBlockTypeSelected(blockType: TimeBlock.BlockType) {
//        state = state.copy(selectedBlockType = blockType)
    }

    private fun onDurationChanged(duration: String) {
//        state = state.copy(duration = duration)
    }

}