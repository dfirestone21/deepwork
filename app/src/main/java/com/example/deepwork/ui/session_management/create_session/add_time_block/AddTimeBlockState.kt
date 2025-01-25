package com.example.deepwork.ui.session_management.create_session.add_time_block

import com.example.deepwork.domain.model.Category
import com.example.deepwork.domain.model.ScheduledTimeBlock
import com.example.deepwork.ui.model.InputField

data class AddTimeBlockState(
    val selectedBlockType: ScheduledTimeBlock.BlockType = ScheduledTimeBlock.BlockType.DEEP_WORK,
    val duration: InputField = InputField(),
    val categories: List<SelectableCategory> = emptyList(),
    val isValid: Boolean = false,
    val showConfirmCancelDialog: Boolean = false,
    val showAddCategoryBottomSheet: Boolean = false,
) {
    val selectedCategoriesCount: Int
        get() = categories.count { it.isSelected }
}

data class SelectableCategory(
    val category: Category,
    val isSelected: Boolean = false
)
