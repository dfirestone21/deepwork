package com.example.deepwork.domain.model

data class SessionWarning(val level: WarningLevel, val type: WarningType)

enum class WarningLevel { YELLOW, BLUE }

enum class WarningType { LONG_WORK_STRETCH, SHORT_BREAK, LONG_BREAK, HIGH_BREAK_RATIO, LONG_SESSION }
