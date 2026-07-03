package org.ivdnt.galahad.annotations

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import org.ivdnt.galahad.exceptions.InvalidAnnotationException

/** Linguistic enrichment annotation types */
enum class Annotation(@JsonValue val value: String) {
    TOKEN("token"),
    LEMMA("lemma"),
    POS("pos"),
    UPOS("upos"),
    HEAD("head"),
    DEPREL("deprel"),
    NER("ner"),
    GROUP("group");

    // Force lowercase and/or custom name.
    override fun toString(): String = value

    enum class Analysis(@JsonValue val value: String) {
        SINGLE("single"),
        MULTIPLE("multiple"),
        BOTH("both");

        override fun toString(): String = value
    }

    companion object {
        // Used by Spring.
        @JsonCreator
        fun fromString(s: String): Annotation =
            entries.firstOrNull { it.value == s.lowercase() } ?: throw InvalidAnnotationException(s)

        /** Get annotations in consistent enum declaration order. */
        fun order(other: Iterable<Annotation>): Set<Annotation> =
            entries.filter { it in other }.toSet()
    }
}
