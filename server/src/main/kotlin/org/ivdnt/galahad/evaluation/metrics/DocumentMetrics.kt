package org.ivdnt.galahad.evaluation.metrics

import org.ivdnt.galahad.data.layer.Layer
import org.ivdnt.galahad.data.layer.Term
import org.ivdnt.galahad.evaluation.comparison.LayerComparison
import org.ivdnt.galahad.evaluation.comparison.LayerFilter
import org.ivdnt.galahad.evaluation.comparison.TermComparison

/**
 * The benchmark [Metric]s of a document for two different tagger layers.
 */
class DocumentMetrics(
    hypothesis: Layer,
    reference: Layer,
    settings: List<MetricsSettings>,
    layerFilter: LayerFilter? = null,
    truncate: Boolean = true
) : Metrics(settings, truncate = truncate) {

    init {
        val layerComparison = LayerComparison(hypothesis, reference, layerFilter)

        layerComparison.matches.forEach(this::add)

        layerComparison.referenceTermsWithoutMatches.forEach {
            add(
                TermComparison(hypoTerm = Term.EMPTY, refTerm = it)
            )
        }
    }
}