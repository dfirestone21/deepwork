package com.example.deepwork.ui.session_management.create_session.add_time_block.add_category

import com.example.deepwork.ui.model.InputField

data class AddCategoryState(
    val name: InputField = InputField(),
    val availableColors: List<Int> = emptyList(),
    val selectedColor: Int? = null,
    val isValid: Boolean = false
)
