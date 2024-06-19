package org.ivdnt.galahad.util

fun String.matchesUpTo(textToMatch: String): Int {
    var matchingIndex = 0
    for (i in 1 until this.length + 1) {
        if (textToMatch.startsWith(this.substring(0, i))) matchingIndex = i
        else break
    }
    return matchingIndex
}

fun Any?.toNonEmptyString(default: String): String {
    return if (this == null || this.toString().isEmpty()) default else this.toString()
}