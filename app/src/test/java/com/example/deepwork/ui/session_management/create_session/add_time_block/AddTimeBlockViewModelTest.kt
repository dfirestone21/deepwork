package com.example.deepwork.ui.session_management.create_session.add_time_block

import com.example.deepwork.domain.usecase.timeblock.CreateBreakBlockUseCase
import com.example.deepwork.domain.usecase.timeblock.CreateWorkBlockUseCase
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Before

class AddTimeBlockViewModelTest {
    private lateinit var viewModel: AddTimeBlockViewModel
    private lateinit var createWorkBlock: CreateWorkBlockUseCase
    private lateinit var createBreakBlock: CreateBreakBlockUseCase

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        createWorkBlock = mockk()
        createBreakBlock = mockk()
    }



}