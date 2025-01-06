package com.example.deepwork.ui.session_management.create_session.add_time_block

import com.example.deepwork.domain.model.Category
import com.example.deepwork.domain.model.TimeBlock
import kotlin.time.Duration

sealed interface AddTimeBlockEvent {
    data class BlockTypeSelected(val blockType: TimeBlock.BlockType) : AddTimeBlockEvent
    data class DurationChanged(val duration: String) : AddTimeBlockEvent
    data object SaveClicked : AddTimeBlockEvent
    data object CancelClicked : AddTimeBlockEvent
    data object NavigateUp : AddTimeBlockEvent
    data class CategorySelected(val category: Category) : AddTimeBlockEvent
    data class CategoryUnselected(val category: Category) : AddTimeBlockEvent
}