package org.ivdnt.galahad.evaluation.entities

import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.Term.Companion.toSpacedString
import org.ivdnt.galahad.layer.Layer

class DocumentEntities(val entities: List<Entity>, val summary: Map<String, Int>, val total: Int) {
    class Entity(val label: String, val form: String, val count: Int)

    companion object {
        fun create(layer: Layer): DocumentEntities {
            val entities: List<Entity> =
                layer.documents
                    .flatMap {
                        it.paragraphs.flatMap {
                            it.sentences.flatMap { sent ->
                                sent.spans?.get(Annotation.NER)?.map { span ->
                                    span.value to
                                        (span.indices.map { sent.terms[it] }).toSpacedString()
                                } ?: emptyList()
                            }
                        }
                    }
                    .groupBy { it }
                    .mapValues { it.value.size }
                    .map { Entity(label = it.key.first, form = it.key.second, count = it.value) }
            val summary: Map<String, Int> =
                entities
                    .groupBy { it.label }
                    .mapValues { it.value.sumOf { entity -> entity.count } }
            val total: Int = summary.values.sum()
            return DocumentEntities(entities, summary, total)
        }
    }
}
