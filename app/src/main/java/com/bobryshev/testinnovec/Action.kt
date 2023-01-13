package com.bobryshev.testinnovec

enum class Action {
    ANIMATION,
    TOAST,
    CALL,
    NOTIFICATION
}

fun getActionFromActionData(actionData: ActionData): Action? {
    Action.values().forEach {
        if (it.name.equals(actionData.type, ignoreCase = true)) {
            return it
        }
    }
    return null
}