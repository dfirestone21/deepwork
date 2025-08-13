package com.example.deepwork.ui.session_management.create_session.add_time_block.add_category

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deepwork.domain.business.CategoryValidator
import com.example.deepwork.domain.model.Category
import com.example.deepwork.domain.model.Result
import com.example.deepwork.domain.repository.CategoryRepository
import com.example.deepwork.domain.usecase.category.CreateCategoryUseCase
import com.example.deepwork.ui.model.InputField
import com.example.deepwork.ui.util.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddCategoryViewModel @Inject constructor(
    private val createCategory: CreateCategoryUseCase,
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
            is AddCategoryEvent.SaveClicked -> saveCategory()
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

    private fun saveCategory() {
        if (!state.isValid) {
            showSnackbar("Please fill out all fields correctly.")
            return
        }
        val category = Category.create(
            name = state.name.value,
            colorHex = state.selectedColor ?: Color.Unspecified.toArgb()
        )
        save(category)
    }

    private fun save(category: Category) {
        viewModelScope.launch {
            val result = createCategory(category)
            when (result) {
                is Result.Success -> {
                    _uiEvent.send(UiEvent.NavigateUp)
                }
                is Result.Error -> {
                    val errorMessage = result.exceptionOrNull()?.message ?: "Failed to save category"
                    showSnackbar(errorMessage)
                }
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

    private fun showSnackbar(message: String) {
        viewModelScope.launch {
            _uiEvent.send(UiEvent.ShowSnackbar(message))
        }
    }
}