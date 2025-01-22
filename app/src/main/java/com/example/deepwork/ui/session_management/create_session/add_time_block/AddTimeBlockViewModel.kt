package com.example.deepwork.ui.session_management.create_session.add_time_block

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deepwork.domain.business.TimeBlockValidator
import com.example.deepwork.domain.model.Category
import com.example.deepwork.domain.model.Result
import com.example.deepwork.domain.model.TimeBlock
import com.example.deepwork.domain.usecase.timeblock.CreateBreakBlockUseCase
import com.example.deepwork.domain.usecase.timeblock.CreateWorkBlockUseCase
import com.example.deepwork.domain.usecase.timeblock.category.GetCategoriesUseCase
import com.example.deepwork.ui.model.InputField
import com.example.deepwork.ui.util.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes

@HiltViewModel
class AddTimeBlockViewModel @Inject constructor(
    private val createWorkBlock: CreateWorkBlockUseCase,
    private val createBreakBlock: CreateBreakBlockUseCase,
    private val timeBlockValidator: TimeBlockValidator,
    private val getCategories: GetCategoriesUseCase
) : ViewModel() {

    var state by mutableStateOf(AddTimeBlockState(
        duration = InputField(
            placeHolder = placeHolderFromBlockType(TimeBlock.BlockType.DEEP),
        )
    ))
        private set

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        initState()
    }

    private fun initState() {
        viewModelScope.launch {
            getCategories().collect { result ->
                when (result) {
                    is Result.Success -> state = state.copy(
                        categories = result.value.map { SelectableCategory(it) }
                    )
                    is Result.Error -> {
                        val errorMessage = result.exception.message ?: "Failed to load categories"
                        showSnackbar(errorMessage)
                    }
                }
            }
        }
    }

    fun onEvent(event: AddTimeBlockEvent) {
        when (event) {
            is AddTimeBlockEvent.BlockTypeSelected -> onBlockTypeSelected(event.blockType)
            is AddTimeBlockEvent.DurationChanged -> onDurationChanged(event.duration)
            is AddTimeBlockEvent.CancelClicked -> state = state.copy(showConfirmCancelDialog = true)
            is AddTimeBlockEvent.ConfirmCancelClicked -> onConfirmCancelClicked()
            is AddTimeBlockEvent.DismissCancelClicked -> state = state.copy(showConfirmCancelDialog = false)
            is AddTimeBlockEvent.NavigateUp -> sendNavigateUpEvent()
            is AddTimeBlockEvent.SaveClicked -> TODO()
            is AddTimeBlockEvent.CategorySelected -> onCategorySelected(event.category)
            is AddTimeBlockEvent.CategoryUnselected -> TODO()
            is AddTimeBlockEvent.CreateCategoryClicked -> TODO() // show bottom sheet for creating category
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

    private fun onCategorySelected(category: Category) {
        val updatedSelectableCategories = state.categories.map {
            if (it.category == category) {
                it.copy(isSelected = true)
            } else {
                it
            }
        }
        val selectedCategories = updatedSelectableCategories
            .filter { it.isSelected }
            .map { it.category }

        runCatching { timeBlockValidator.validateCategories(state.selectedBlockType, selectedCategories) }
            .onSuccess { state = state.copy(categories = updatedSelectableCategories) }
            .onFailure { exception -> showSnackbar(exception.message ?: "Failed to validate categories") }
    }

    private fun showSnackbar(message: String) {
        viewModelScope.launch {
            _uiEvent.send(UiEvent.ShowSnackbar(message))
        }
    }

}