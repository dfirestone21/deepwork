package com.example.deepwork.ui.session_management.create_session.add_time_block

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.deepwork.domain.business.TimeBlockValidator
import com.example.deepwork.domain.exception.TimeBlockException
import com.example.deepwork.domain.model.Category
import com.example.deepwork.domain.model.Result
import com.example.deepwork.domain.model.ScheduledTimeBlock
import com.example.deepwork.domain.model.template.TimeBlockTemplate
import com.example.deepwork.domain.usecase.timeblock.CreateTimeBlockUseCase
import com.example.deepwork.domain.usecase.timeblock.category.GetCategoriesUseCase
import com.example.deepwork.ui.model.InputField
import com.example.deepwork.ui.util.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.Uuid

@HiltViewModel
class AddTimeBlockViewModel @Inject constructor(
    private val createTimeBlock: CreateTimeBlockUseCase,
    private val timeBlockValidator: TimeBlockValidator,
    private val getCategories: GetCategoriesUseCase
) : ViewModel() {

    var state by mutableStateOf(AddTimeBlockState(
        duration = InputField(
            placeHolder = placeHolderFromBlockType(ScheduledTimeBlock.BlockType.DEEP_WORK),
        )
    ))
        private set

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()
    private val selectedCategories: List<Category>
        get() = state.categories
            .filter { it.isSelected }
            .map { it.category }

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
            is AddTimeBlockEvent.SaveClicked -> onSaveClicked()
            is AddTimeBlockEvent.CategorySelected -> onCategorySelected(event.category)
            is AddTimeBlockEvent.CategoryUnselected -> onCategoryUnselected(event.category)
            is AddTimeBlockEvent.CreateCategoryClicked -> showAddCategoryBottomSheet()
            is AddTimeBlockEvent.AddCategoryBottomSheetDismissed -> hideAddCategoryBottomSheet()
        }
    }

    private fun onSaveClicked() {
        val duration = parsedDuration()
        val timeBlock = buildTimeBlockTemplate(state.selectedBlockType, duration, selectedCategories)
        viewModelScope.launch {
            createTimeBlock(timeBlock)
                .onSuccess { sendNavigateUpEvent() }
                .onError { exception, _ ->
                    showSnackbar(exception.message ?: "Failed to save time block")
                }
        }
    }

    private fun onBlockTypeSelected(blockType: ScheduledTimeBlock.BlockType) {
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
        validateTimeBlock(duration, selectedCategories)
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

    private fun placeHolderFromBlockType(blockType: ScheduledTimeBlock.BlockType): String {
        val min = blockType.minDuration.inWholeMinutes
        val max = blockType.maxDuration.inWholeMinutes
        return "$min to $max minutes"
    }

    private fun onCategorySelected(category: Category) {
        updateCategoriesWithSelection(
            id = category.id,
            isSelected = true
        )
    }

    private fun onCategoryUnselected(category: Category) {
        updateCategoriesWithSelection(
            id = category.id,
            isSelected = false
        )
    }

    private fun updateCategoriesWithSelection(id: Uuid, isSelected: Boolean) {
        val updatedSelectableCategories = state.categories.map {
            if (it.category.id == id) {
                it.copy(isSelected = isSelected)
            } else {
                it
            }
        }
        val selectedCategories = updatedSelectableCategories
            .filter { it.isSelected }
            .map { it.category }

        val shouldValidateCategories = isSelected
        if (shouldValidateCategories) {
            runCatching { timeBlockValidator.validateCategories(state.selectedBlockType, selectedCategories) }
                .onFailure { exception ->
                    showSnackbar(exception.message ?: "Failed to validate categories")
                    return
                }
        }
        state = state.copy(categories = updatedSelectableCategories)

        validateTimeBlock(parsedDuration(), selectedCategories)
    }

    private fun buildTimeBlockTemplate(
        blockType: ScheduledTimeBlock.BlockType,
        duration: Duration,
        selectedCategories: List<Category>
    ): TimeBlockTemplate = when (blockType) {
        ScheduledTimeBlock.BlockType.DEEP_WORK -> TimeBlockTemplate.deepWorkTemplate(
            duration = duration,
            categories = selectedCategories
        )
        ScheduledTimeBlock.BlockType.SHALLOW_WORK -> TimeBlockTemplate.shallowWorkTemplate(
            duration = duration,
            categories = selectedCategories
        )
        ScheduledTimeBlock.BlockType.BREAK -> TimeBlockTemplate.breakTemplate(
            duration = duration
        )
    }

    private fun parsedDuration(): Duration = state.duration.value.toIntOrNull()?.minutes ?: Duration.ZERO

    private fun validateTimeBlock(duration: Duration, selectedCategories: List<Category>) {
        val timeBlock = buildTimeBlockTemplate(state.selectedBlockType, duration, selectedCategories)
        runCatching {
            timeBlockValidator.validate(timeBlock)
        }.onSuccess {
            state = state.copy(isValid = true)
        }
         .onFailure { exception ->
             if (exception !is TimeBlockException) {
                 val errorMessage = exception.message ?: "Failed to validate time block"
                 showSnackbar(errorMessage)
             }
             state = state.copy(isValid = false)
         }

    }

    private fun showAddCategoryBottomSheet() {
        state = state.copy(showAddCategoryBottomSheet = true)
    }

    private fun hideAddCategoryBottomSheet() {
        state = state.copy(showAddCategoryBottomSheet = false)
    }

    private fun showSnackbar(message: String) {
        viewModelScope.launch {
            _uiEvent.send(UiEvent.ShowSnackbar(message))
        }
    }

}