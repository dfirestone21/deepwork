package com.example.deepwork.domain.business

import com.example.deepwork.domain.model.Category
import com.example.deepwork.domain.model.ScheduledTimeBlock
import com.example.deepwork.domain.model.template.TimeBlockTemplate
import kotlin.time.Duration

interface TimeBlockValidator {

    fun validate(timeBlock: ScheduledTimeBlock)

    fun validate(timeBlockTemplate: TimeBlockTemplate)

    fun validateDuration(blockType: ScheduledTimeBlock.BlockType, duration: Duration)

    fun validateCategories(blockType: ScheduledTimeBlock.BlockType, categories: List<Category>)
}