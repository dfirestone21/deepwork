package com.example.deepwork.ui.session_management.create_session

import com.example.deepwork.domain.model.ScheduledTimeBlock

sealed interface CreateSessionEvent {
    data class UpdateName(val name: String) : CreateSessionEvent
    data class AddTimeBlock(val timeBlock: ScheduledTimeBlock) : CreateSessionEvent
    data object FabClicked : CreateSessionEvent
    data object SaveClicked : CreateSessionEvent
}