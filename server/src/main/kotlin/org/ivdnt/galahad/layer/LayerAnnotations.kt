package org.ivdnt.galahad.layer

import com.fasterxml.jackson.annotation.JsonValue
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.Term

/**
 * Stores the size of the [Layer] in terms of number of [WordForm],
 * [org.ivdnt.galahad.annotations.Term], lemma and pos.
 */
data class LayerAnnotations(@JsonValue val annotations: Map<Annotation, Int>) {
    val keys: Set<Annotation>
        get() = Annotation.sort(annotations.keys)

    companion object {
        val EMPTY: LayerAnnotations = LayerAnnotations(emptyMap())

        fun fromTerms(terms: List<Term>): LayerAnnotations =
            LayerAnnotations(
                annotations =
                    terms
                        .flatMap { it.annotations.keys }
                        .groupingBy { it }
                        .eachCount()
                        .toSortedMap { a, b ->
                            Annotation.entries.indexOf(a).compareTo(Annotation.entries.indexOf(b))
                        }
            )

        operator fun LayerAnnotations.contains(annotation: Annotation): Boolean =
            annotation in annotations

        operator fun LayerAnnotations.plus(b: LayerAnnotations): LayerAnnotations {
            return LayerAnnotations(
                annotations =
                    this.annotations.toMutableMap().also {
                        b.annotations.forEach { (annotation, count) ->
                            it.merge(annotation, count, Integer::sum)
                        }
                    }
            )
        }
    }
}
