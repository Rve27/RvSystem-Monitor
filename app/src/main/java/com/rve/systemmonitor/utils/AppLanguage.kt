package com.rve.systemmonitor.utils

import kotlinx.serialization.Serializable

@Serializable
enum class AppLanguage(val languageTag: String?) {
    SYSTEM(null),
    ENGLISH("en"),
    RUSSIAN("ru"),
    UKRAINIAN("uk"),
    CHINESE_SIMPLIFIED("zh-CN"),
    INDONESIAN("in"),
}
