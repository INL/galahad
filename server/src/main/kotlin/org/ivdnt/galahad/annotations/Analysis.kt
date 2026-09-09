package org.ivdnt.galahad.annotations

import com.fasterxml.jackson.annotation.JsonValue

/** Analysis type of a linguistic annotation. Represented with '+' in the annotation value. */
enum class Analysis(@JsonValue val value: String) {
    SINGLE("single"),
    MULTIPLE("multiple"),
    BOTH("both");

    // Force lowercase value name
    override fun toString(): String = value
}
