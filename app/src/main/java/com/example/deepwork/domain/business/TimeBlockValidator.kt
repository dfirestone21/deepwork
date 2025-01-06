package com.example.deepwork.domain.business

import com.example.deepwork.domain.model.Category
import com.example.deepwork.domain.model.TimeBlock
import kotlin.time.Duration

interface TimeBlockValidator {

    fun validate(timeBlock: TimeBlock)

    fun validateDuration(blockType: TimeBlock.BlockType, duration: Duration)

    fun validateCategories(blockType: TimeBlock.BlockType, categories: List<Category>)
}