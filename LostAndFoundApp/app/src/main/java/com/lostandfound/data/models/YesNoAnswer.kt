package com.lostandfound.data.models

/**
 * Helpers for Yes/No verification answers stored as Boolean (true = yes, false = no).
 */
object YesNoAnswer {
    fun isValid(answer: Boolean?): Boolean = answer != null

    fun toBoolean(value: String): Boolean? = when (value.trim().lowercase()) {
        "yes", "y", "true" -> true
        "no", "n", "false" -> false
        else -> null
    }
}
