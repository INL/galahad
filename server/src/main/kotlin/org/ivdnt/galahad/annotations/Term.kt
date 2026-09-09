package org.ivdnt.galahad.annotations

import com.fasterxml.jackson.annotation.JsonIgnore

class Term(
    /** Term id, e.g. XML id. */
    val id: String,
    val annotations: Map<Annotation, String?>,
    spaceAfter: Boolean? = null,
) {
    // Default to null to save space in serialization where null is ignored.
    // Set to false to join two terms/tokens in plaintext.
    val spaceAfter: Boolean? = if (spaceAfter == false) false else null
    @get:JsonIgnore val space: String = if (spaceAfter == false) "" else " "

    // Quick access to annotations
    @get:JsonIgnore val token: String = annotations[Annotation.TOKEN]!!
    @get:JsonIgnore val lemma: String? = annotations[Annotation.LEMMA]
    @get:JsonIgnore val pos: String? = annotations[Annotation.POS]
    @get:JsonIgnore val upos: String? = annotations[Annotation.UPOS]
    @get:JsonIgnore val head: String? = annotations[Annotation.HEAD]
    @get:JsonIgnore val deprel: String? = annotations[Annotation.DEPREL]
    @get:JsonIgnore val ner: String? = annotations[Annotation.NER]
    @get:JsonIgnore val group: String? = annotations[Annotation.GROUP]

    /** Is this annotation multi-analyses. */
    fun isMulti(annotation: Annotation): Boolean = annotations[annotation]?.contains("+") == true

    /**
     * Returns the annotation head or NO_[annotation] if it is missing. E.g. NOU-C for
     * NOU-c(num=sg); or NO_POS.
     */
    fun annotationHeadOrMissing(annotation: Annotation): String =
        annotationHead(annotation) ?: missingName(annotation)

    /**
     * Returns the annotation or NO_[annotation] if it is missing. E.g. NOU-C(num=sg); or NO_POS.
     */
    fun annotationOrMissing(annotation: Annotation): String =
        annotations[annotation] ?: missingName(annotation)

    /**
     * The head of [annotation]. E.g. "PD+NOU" for "PD(type=art)+NOU(num=sg)" or "VG" for "VG|neven"
     * or ORG for B-ORG.
     */
    fun annotationHead(annotation: Annotation): String? {
        // get annotation
        val value = annotations[annotation] ?: return null
        // for NER
        if (annotation == Annotation.NER) {
            if ('-' in value) {
                return value.split('-')[1]
            }
        }
        // for POS & UPOS
        else if (annotation in POS_ANNOTATIONS) {
            return if (isMulti(annotation)) {
                // Split on + and transform each part due to potential presence of features
                value.split("+").joinToString("+") { singlePosToHead(it) }
            } else {
                singlePosToHead(value)
            }
        }
        // else leave as is
        return value
    }

    /** The features of [value]. E.g. "num=sg" for "NOU(num=sg)". Does not support multi-pos. */
    fun features(annotation: Annotation): String? {
        val value = annotations[annotation] ?: return null
        val featureStart: Int = value.indexOf('(')
        val featureEnd: Int = value.indexOf(')')
        return if (featureStart != -1 && featureEnd != -1) {
            value.slice(featureStart + 1 until featureEnd)
        } else null
    }

    // simply uppercase and prepend "NO_"
    private fun missingName(annotation: Annotation): String = "NO_${annotation.value.uppercase()}"

    /** Returns the head of a pos string. E.g. NOU for NOU(num=sg). Does not support multi-pos. */
    private fun singlePosToHead(pos: String): String {
        for (separator in POS_HEAD_SEPARATORS) {
            if (separator in pos) {
                val head = pos.split(separator)[0]
                // presumably head won't be empty, but this way we could
                // parse something like (VRB) if anyone would ever use that
                return head.ifEmpty { pos }
            }
        }
        return pos
    }

    companion object {
        /** Empty utility term for comparison. */
        val EMPTY: Term = Term("", mapOf(Annotation.TOKEN to ""))
        /** What annotations are part of speech. For handling features. */
        private val POS_ANNOTATIONS = arrayOf(Annotation.POS, Annotation.UPOS)
        /** What characters separate part of speech head and features. */
        private val POS_HEAD_SEPARATORS = arrayOf('(', '|')

        /** Build string for plain text with spaces. */
        fun List<Term>.toSpacedString(): String = buildString {
            this@toSpacedString.forEachIndexed { i, t ->
                append(t.token)
                if (i != this@toSpacedString.lastIndex) append(t.space)
            }
        }
    }
}
