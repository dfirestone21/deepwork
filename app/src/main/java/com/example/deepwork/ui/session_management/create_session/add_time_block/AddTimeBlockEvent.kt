package com.example.deepwork.ui.session_management.create_session.add_time_block

import com.example.deepwork.domain.model.Category
import com.example.deepwork.domain.model.ScheduledTimeBlock

sealed interface AddTimeBlockEvent {
    data class BlockTypeSelected(val blockType: ScheduledTimeBlock.BlockType) : AddTimeBlockEvent
    data class DurationChanged(val duration: String) : AddTimeBlockEvent
    data object SaveClicked : AddTimeBlockEvent
    data object CancelClicked : AddTimeBlockEvent
    data object ConfirmCancelClicked: AddTimeBlockEvent
    data object DismissCancelClicked: AddTimeBlockEvent
    data object NavigateUp : AddTimeBlockEvent
    data class CategorySelected(val category: Category) : AddTimeBlockEvent
    data class CategoryUnselected(val category: Category) : AddTimeBlockEvent
    data object CreateCategoryClicked : AddTimeBlockEvent
    data object AddCategoryBottomSheetDismissed : AddTimeBlockEvent
}