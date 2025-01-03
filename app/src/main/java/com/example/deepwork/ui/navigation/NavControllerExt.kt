package com.example.deepwork.ui.navigation

import androidx.navigation.NavController
import com.example.deepwork.ui.util.UiEvent

fun NavController.navigate(event: UiEvent.Navigate) {
    navigate(event.route)
}