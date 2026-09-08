package org.ivdnt.galahad.evaluation.distribution

import com.fasterxml.jackson.annotation.JsonValue
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.layer.Layer

/** Type-token distribution of terms in a layer. */
class DocumentDistribution(@JsonValue val typeTokens: List<TypeToken>) {
    companion object {
        fun create(layer: Layer, annotation: Annotation, group: Annotation): DocumentDistribution =
            DocumentDistribution(
                buildMap<Pair<String, String>, MutableMap<String, Int>> {
                        // We're going to construct a map for each annotation,
                        // with key [annotation; group] and [tokens; their counts] as
                        // values.
                        // such that we can group by lemma and annotation head.
                        // Then convert it to List<TypeToken>.
                        layer.terms.forEach { t ->
                            // For each term in the layer, get the valid annotations and their
                            // values

                            // map the annotation to a map of pairs [lemma; annotation head]
                            // and
                            // tokens and their counts.
                            // Pair examples: [NO_LEMMA; PC], [lopen; VRB] (so head only)
                            val pair =
                                t.annotationOrMissing(annotation) to
                                    t.annotationHeadOrMissing(group)
                            val tokens = mutableMapOf(t.token to 1)

                            // merge this pair-tokencount mapping if needed

                            merge(pair, tokens) { oldTokens, newTokens ->
                                // In doing so, we only need to sum the token counts
                                oldTokens.apply {
                                    this.merge(t.token, 1, Integer::sum)
                                }
                            }
                        }
                    }
                    .map {
                        // convert key [lemma; that annotation head] and value [tokens; their
                        // counts]
                        // to simply TypeToken(lemma, group, tokens).
                        (k, v) ->
                        TypeToken(annotation = k.first, group = k.second, tokens = v)
                    }
                    .sortedByDescending { it.count } // nice to have it sorted on count
            )
    }
}
