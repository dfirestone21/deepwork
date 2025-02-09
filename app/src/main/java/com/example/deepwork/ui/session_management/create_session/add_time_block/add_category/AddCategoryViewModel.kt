package com.example.deepwork.ui.session_management.create_session.add_time_block.add_category

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import com.example.deepwork.domain.business.CategoryValidator
import com.example.deepwork.domain.repository.CategoryRepository
import com.example.deepwork.ui.model.InputField
import com.example.deepwork.ui.util.UiEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

class AddCategoryViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val categoryValidator: CategoryValidator
) : ViewModel() {

    var state by mutableStateOf(AddCategoryState(
        availableColors = defaultColors()
    ))
        private set

    private var _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun onEvent(event: AddCategoryEvent) {
        when (event) {
            is AddCategoryEvent.NameUpdated -> validateAndUpdateName(event.name)
            is AddCategoryEvent.ColorSelected -> updateSelectedColor(event.colorHex)
            is AddCategoryEvent.SaveClicked -> {

            }
        }
    }

    private fun validateAndUpdateName(name: String) {
        val validationException = runCatching { categoryValidator.validateName(name) }.exceptionOrNull()
        state = state.copy(
            name = InputField(
                value = name,
                message = validationException?.message,
                isError = validationException != null
            )
        )
        determineFormValidity()
    }

    private fun updateSelectedColor(color: Int) {
        state = state.copy(selectedColor = color)
        determineFormValidity()
    }

    private fun determineFormValidity() {
        val isValid = !state.name.isError && state.selectedColor != null
        state = state.copy(isValid = isValid)
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