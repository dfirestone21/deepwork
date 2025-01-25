package com.example.deepwork.ui.session_management.create_session.add_time_block.add_category

sealed interface AddCategoryEvent {

    data class NameUpdated(val name: String) : AddCategoryEvent

    data class ColorSelected(val colorHex: Int) : AddCategoryEvent

    data object SaveClicked : AddCategoryEvent
}