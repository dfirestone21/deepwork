package com.example.deepwork.ui.session_management.create_session

import com.example.deepwork.domain.model.TimeBlock

sealed interface CreateSessionEvent {
    data class UpdateName(val name: String) : CreateSessionEvent
    data class AddTimeBlock(val timeBlock: TimeBlock) : CreateSessionEvent
    data object FabClicked : CreateSessionEvent
}