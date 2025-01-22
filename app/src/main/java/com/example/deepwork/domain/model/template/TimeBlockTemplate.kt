package com.example.deepwork.domain.model.template

import kotlin.time.Duration
import kotlin.uuid.Uuid

data class TimeBlockTemplate(
    val id: Uuid,
    val sessionTemplateId: Uuid,
    val duration: Duration
)
