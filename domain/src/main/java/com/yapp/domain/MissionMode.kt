package com.yapp.domain

enum class MissionMode {
    REAL,
    PREVIEW,
    ;

    companion object {
        fun fromRaw(raw: String?): MissionMode {
            return raw?.let { entries.find { it.name == raw } } ?: REAL
        }
    }
}
