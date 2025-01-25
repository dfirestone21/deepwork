package com.example.deepwork.domain.model.template

import kotlin.uuid.Uuid

data class TemplateSession(
    val id: Uuid,
    val name: String,
    val description: String?,
    val isPublic: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)
