package com.bobryshev.testinnovec

import com.google.gson.annotations.SerializedName
import org.joda.time.DateTime

data class ActionData(
    @SerializedName("type")
    val type: String,
    @SerializedName("enabled")
    val enabled: Boolean,
    @SerializedName("priority")
    val priority: Int,
    @SerializedName("valid_days")
    val validDays: List<Int>,
    @SerializedName("cool_down")
    val coolDown: Long
) {

    @Transient var lastExecute: Long = Long.MAX_VALUE

    fun checkConditions(): Boolean {
        return enabled &&
                validDays.contains(DateTime.now().dayOfWeek().get()) && checkIsCoolDown()

    }

    private fun checkIsCoolDown(): Boolean {
        return when {
            lastExecute == Long.MAX_VALUE -> true
            (DateTime.now().millis - lastExecute) > coolDown -> true
            else -> false
        }
    }
}

