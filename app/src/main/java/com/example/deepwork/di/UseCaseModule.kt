package com.example.deepwork.di

import com.example.deepwork.domain.business.CategoryValidator
import com.example.deepwork.domain.business.CategoryValidatorImpl
import com.example.deepwork.domain.business.TimeBlockValidator
import com.example.deepwork.domain.business.TimeBlockValidatorImpl
import com.example.deepwork.domain.usecase.session.CreateSessionUseCase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
interface UseCaseModule {

    @Binds
    fun bindTimeBlockValidator(validator: TimeBlockValidatorImpl): TimeBlockValidator

    @Binds
    fun bindCategoryValidator(validator: CategoryValidatorImpl): CategoryValidator
}