package com.example.deepwork.ui.util

sealed interface UiEvent {
    data class Navigate(val route: String) : UiEvent
    data object NavigateUp : UiEvent
    data class ShowSnackbar(val message: String) : UiEvent
}