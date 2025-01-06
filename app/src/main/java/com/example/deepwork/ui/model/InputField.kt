package com.example.deepwork.ui.model

data class InputField(
    val value: String = "",
    val message: String? = null,
    val isError: Boolean = false,
    val placeHolder: String? = null
)
