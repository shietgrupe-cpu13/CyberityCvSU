package com.cyberity.cvsu

object YearLevels {
    val options = (1..5).toList()
    fun isValid(value: Int?): Boolean = value in options
    fun label(value: Int?): String = when (value) {
        1 -> "1st year"
        2 -> "2nd year"
        3 -> "3rd year"
        4 -> "4th year"
        5 -> "5th year or higher"
        else -> "Not set"
    }
    fun fromStored(value: Any?): Int? = (value as? Number)?.let {
        it.toInt().takeIf { number -> isValid(number) && it.toDouble() == number.toDouble() }
    }
}
