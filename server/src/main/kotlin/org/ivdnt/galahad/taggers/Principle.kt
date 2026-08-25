package org.ivdnt.galahad.taggers

import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.exceptions.PrincipleNotFoundException

data class Principle(
    val annotation: Annotation,
    val principle: Tagger.LinkItem,
    val taggers: List<String>,
) {
    companion object {
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

        fun readOrNull(name: String?): Principle? = principles.find {
            it.principle.name == name
        }

        fun readOrThrow(name: String): Principle =
            readOrNull(name) ?: throw PrincipleNotFoundException(name)
    }
}
