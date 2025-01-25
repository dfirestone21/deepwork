package com.example.deepwork.ui.session_management.create_session.add_time_block.add_category

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import com.example.deepwork.ui.util.UiEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

class AddCategoryViewModel @Inject constructor() : ViewModel() {

    var state by mutableStateOf(AddCategoryState(
        availableColors = defaultColors()
    ))
        private set

    private var _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun onEvent(event: AddCategoryEvent) {
        when (event) {
            is AddCategoryEvent.NameUpdated -> {
                // TODO TEST THIS
                state = state.copy(name = state.name.copy(value = event.name))
            }

            is AddCategoryEvent.ColorSelected -> {
                // TODO TEST THIS
                state = state.copy(
                    selectedColor = event.colorHex,
                    isValid = true
                )
            }
            is AddCategoryEvent.SaveClicked -> {

            }
        }
    }

    private fun defaultColors(): List<Int> {
        return listOf(
            Color.Red.toArgb(),
            Color.Blue.toArgb(),
            Color.Green.toArgb(),
            Color.Yellow.toArgb(),
            Color.Magenta.toArgb(),
            Color.Black.toArgb(),
            Color.Cyan.toArgb(),
            Color.White.toArgb()
        )
    }
}