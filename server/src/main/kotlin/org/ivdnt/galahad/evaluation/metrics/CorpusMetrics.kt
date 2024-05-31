package org.ivdnt.galahad.evaluation.metrics

import com.fasterxml.jackson.annotation.JsonProperty
import org.ivdnt.galahad.data.corpus.Corpus
import org.ivdnt.galahad.data.document.SOURCE_LAYER_NAME
import org.ivdnt.galahad.evaluation.comparison.LayerFilter

/**
 * The benchmark [Metric]s of a corpus for two different tagger layers.
 * A CorpusMetrics is the sum of the [DocumentMetrics]s of all documents in the corpus.
 */
class CorpusMetrics(
    corpus: Corpus,
    settings: List<MetricsSettings>,
    val hypothesis: String,
    val reference: String = SOURCE_LAYER_NAME,
    layerFilter: LayerFilter? = null,
    truncate: Boolean = true
) : Metrics(settings, truncate = truncate) {

    private val hypothesisJob = corpus.jobs.readOrNull(hypothesis) ?: throw Exception("Hypothesis layer does not exist")
    private val referenceJob = corpus.jobs.readOrNull(reference) ?: throw Exception("Reference layer does not exist")

    @JsonProperty
    val hypothesisLastModified = hypothesisJob.lastModified
    @JsonProperty
    val referenceLastModified = referenceJob.lastModified
    @JsonProperty
    val generated = System.currentTimeMillis()

    init {
        corpus.documents.readAll().forEach {
            val name = it.metadata.expensiveGet().name
            add(
                DocumentMetrics(
                    hypothesisJob.document(name).result,
                    referenceJob.document(name).result,
                    settings,
                    layerFilter,
                    truncate
                )
            )
        }
    }
}