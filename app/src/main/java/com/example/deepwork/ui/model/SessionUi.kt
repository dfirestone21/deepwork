package com.example.deepwork.ui.model

import com.example.deepwork.domain.model.Session

data class SessionUi(
    val name: String,
    val timeBlocks: List<TimeBlockUi>
) {

    companion object {

            fun fromDomain(session: Session): SessionUi {
                return SessionUi(
                    name = session.name,
                    timeBlocks = session.timeBlocks.map { TimeBlockUi.fromDomain(it) }
                )
            }
    }
}
