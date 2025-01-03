package com.example.deepwork.ui.session_management.create_session.add_time_block

import com.example.deepwork.domain.model.TimeBlock
import com.example.deepwork.ui.model.InputField

data class AddTimeBlockState(
    val selectedBlockType: TimeBlock.BlockType = TimeBlock.BlockType.DEEP,
    val duration: InputField = InputField(),
    val isValid: Boolean = false
)
