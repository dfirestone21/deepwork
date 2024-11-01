package com.example.deepwork.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration.Companion.minutes


class SessionCreationTest {

    @Test
    fun `should create a session with a single work block`() {
        // given
        val sessionName = "Morning Focus"
        val sessionDescription = "First session"
        // when
        val timeBlock = TimeBlock.WorkBlock(
            id = "1",
            duration = 25.minutes
        )
        val session = Session(
            id = "1",
            name = sessionName,
            description = sessionDescription,
            timeBlocks = listOf(timeBlock)
        )
        assertEquals(sessionName, session.name)
        assertEquals(sessionDescription, session.description)
        assertEquals(1, session.timeBlocks.size)
        assertTrue(session.timeBlocks.first() is TimeBlock.WorkBlock)
    }
}