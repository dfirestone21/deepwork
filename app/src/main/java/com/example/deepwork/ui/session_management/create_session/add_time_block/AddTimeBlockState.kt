package com.example.deepwork.ui.session_management.create_session.add_time_block

import com.example.deepwork.domain.model.Category
import com.example.deepwork.domain.model.TimeBlock
import com.example.deepwork.ui.model.InputField

data class AddTimeBlockState(
    val selectedBlockType: TimeBlock.BlockType = TimeBlock.BlockType.DEEP,
    val duration: InputField = InputField(),
    val categories: List<SelectableCategory> = emptyList(),
    val maxSelectableCategories: Int = 3,
    val isValid: Boolean = false,
    val showConfirmCancelDialog: Boolean = false
) {
    val selectedCategoriesCount: Int
        get() = categories.count { it.isSelected }
}

data class SelectableCategory(
    val category: Category,
    val isSelected: Boolean
)
