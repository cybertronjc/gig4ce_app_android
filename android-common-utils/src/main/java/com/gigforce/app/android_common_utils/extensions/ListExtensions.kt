package com.gigforce.app.android_common_utils.extensions

fun List<String>.containsIgnoreCase(
    textToMatch: String
): Boolean {
    for (s in this) {
        if (s.trim().lowercase() == textToMatch.trim().lowercase()) {
            return true
        }
    }

    return false
}