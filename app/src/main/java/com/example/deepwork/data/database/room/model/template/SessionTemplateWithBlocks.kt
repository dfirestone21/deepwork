package com.example.deepwork.data.database.room.model.template

import androidx.room.Embedded
import androidx.room.Relation

data class SessionTemplateWithBlocks(
    @Embedded
    val sessionTemplate: SessionTemplateEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "session_template_id"
    )
    val timeBlockTemplates: List<TimeBlockTemplateEntity>
)
