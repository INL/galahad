package org.ivdnt.galahad.evaluation.spans

import com.fasterxml.jackson.annotation.JsonValue
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.Layer
import org.ivdnt.galahad.evaluation.comparison.LayerComparison
import org.ivdnt.galahad.evaluation.comparison.TermComparison

class SpanEvaluation(var correct: Int, var incorrect: Int)

class DocumentSpanEvaluation(
    @JsonValue val spanEqualityByAnnotation: Map<Annotation, Map<String, SpanEvaluation>>
) {
    companion object {
        fun create(layerComparison: LayerComparison, reference: Layer): DocumentSpanEvaluation {
            val result = mutableMapOf<Annotation, MutableMap<String, SpanEvaluation>>()
            // TODO this should absolutely not go through all paragraphs, sentences, etc.
            // TODO layer comparsion should match spans or something. Perhaps LayerSpanComparison
            reference.documents.forEach { document ->
                document.paragraphs.forEach { paragraph ->
                    paragraph.sentences.forEach { sentence ->
                        sentence.spans?.forEach { (annotation, spans) ->
                            spans.forEach { span ->
                                val comparisons: List<TermComparison> =
                                    span.indices.map { index ->
                                        val term = sentence.terms[index]
                                        layerComparison.matches.find {
                                            it.ref.token ==
                                                term.token // TODO FIXME: used to be offset
                                        }!!
                                    }

                                val equal = comparisons.all { it.equal(annotation) }
                                val spanValue: String = span.value
                                val spanEvaluation =
                                    result
                                        .getOrPut(annotation) { mutableMapOf() }
                                        .getOrPut(spanValue) {
                                            SpanEvaluation(correct = 0, incorrect = 0)
                                        }

                                if (equal) spanEvaluation.correct += 1
                                else spanEvaluation.incorrect += 1
                            }
                        }
                    }
                }
            }

            return DocumentSpanEvaluation(result)
        }
    }
}
