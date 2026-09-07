package org.ivdnt.galahad.taggers

import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.exceptions.PrincipleNotFoundException
import org.ivdnt.galahad.metadata.MetadataItem

/** Principles of linguistic annotations. */
data class Principle(
    /** Linguistic annotation for which the principle holds. */
    val annotation: Annotation,
    /** The principle resource. */
    val principle: MetadataItem,
    /** Taggers that apply this principle. */
    val taggers: List<String>,
) {
    companion object {
        /** List of principles used by the present taggers. */
        val principles: List<Principle> by lazy {
            Tagger.taggers.values
                .flatMap {
                    // We want to create a tuple of (Tagger, Annotation, LinkItem)
                    it.annotations.flatMap { annotationItem ->
                        annotationItem.principles?.map { principle ->
                            annotationItem.annotation!! to principle
                        } ?: emptyList()
                    }
                }
                .toSet()
                .map {
                    Principle(
                        it.first,
                        it.second,
                        Tagger.taggers.values
                            .filter { tagger ->
                                it.second.name in
                                    tagger.annotations.flatMap {
                                        it.principles?.map { it.name } ?: emptyList()
                                    }
                            }
                            .map { tagger -> tagger.name },
                    )
                }
        }

        /** Read principle by name or null if absent. */
        fun readOrNull(name: String?): Principle? = principles.find {
            it.principle.name == name
        }

        /** Read principle by name or throw if absent. */
        fun readOrThrow(name: String): Principle =
            readOrNull(name) ?: throw PrincipleNotFoundException(name)
    }
}
