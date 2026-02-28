package com.example.deepwork.di

import com.example.deepwork.data.repository.CategoryRepositoryImpl
import com.example.deepwork.data.repository.SessionRepositoryImpl
import com.example.deepwork.domain.repository.CategoryRepository
import com.example.deepwork.domain.repository.SessionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Binds
    fun bindCategoryRepository(
        categoryRepositoryImpl: CategoryRepositoryImpl
    ): CategoryRepository

    @Binds
    fun bindSessionRepository(
        sessionRepositoryImpl: SessionRepositoryImpl
    ): SessionRepository
}