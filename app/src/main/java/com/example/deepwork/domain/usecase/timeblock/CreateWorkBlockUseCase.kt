package com.example.deepwork.domain.usecase.timeblock

import com.example.deepwork.domain.model.TimeBlock

class CreateTimeBlockUseCase {

    suspend operator fun invoke(timeBlock: TimeBlock): TimeBlock {
        return timeBlock
    }
}