package com.example.deepwork.ui.session_management.create_session

import com.example.deepwork.ui.model.InputField
import com.example.deepwork.ui.model.TimeBlockUi

data class CreateSessionUiState(
    val name: InputField = InputField(""),
    val timeBlocks: List<TimeBlockUi> = emptyList(),
    val message: String? = null
)