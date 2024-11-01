package com.example.deepwork.domain.model

data class Session(
    val id: String,
    val name: String,
    val description: String?,
    val timeBlocks: List<TimeBlock>,
)
