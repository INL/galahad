package org.ivdnt.galahad.evaluation

import java.io.File
import org.ivdnt.galahad.annotations.Analysis
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.evaluation.comparison.LayerComparison
import org.ivdnt.galahad.evaluation.confusion.DocumentConfusion
import org.ivdnt.galahad.evaluation.distribution.DocumentDistribution
import org.ivdnt.galahad.evaluation.entities.DocumentEntities
import org.ivdnt.galahad.evaluation.metrics.DocumentMetrics
import org.ivdnt.galahad.evaluation.metrics.Metrics
import org.ivdnt.galahad.evaluation.spans.DocumentSpanEvaluation
import org.ivdnt.galahad.files.GalahadFolder
import org.ivdnt.galahad.files.ValidatedDiskValue
import org.ivdnt.galahad.layer.Layer
import org.ivdnt.galahad.layers.CorpusLayer

/**
 * Defines evaluations at the level of a document, i.e. where a single document hypothesis is
 * compared to a reference. Or only the reference layer is considered, in which case the JobPair is
 * (reference, reference). Typically, these are only invalidated when the document itself is
 * modified.
 */
class DocumentEvaluation(dir: File, private val corpus: Corpus, private val jobs: JobPair) :
    GalahadFolder(dir) {
    private val referenceDocuments: CorpusLayer
        get() = corpus.layers.readOrThrow(jobs.reference)

    private val hypothesisDocuments: CorpusLayer
        get() = corpus.layers.readOrThrow(jobs.hypothesis)

    private val refLayer: Layer
        get() = referenceDocuments.documents.readOrThrow(name).layer

    private val hypLayer: Layer
        get() = hypothesisDocuments.documents.readOrThrow(name).layer

    private val refModified: Long
        get() = referenceDocuments.documents.readOrThrow(name).modified

    private val hypModified: Long
        get() = hypothesisDocuments.documents.readOrThrow(name).modified

    private val lastModified: Long
        get() = maxOf(hypModified, refModified)

    private val availableAnnotations: Set<Annotation>
        get() =
            referenceDocuments.metadata.annotations.keys
                .intersect(hypothesisDocuments.metadata.annotations.keys)
                .filter { it != Annotation.TOKEN }
                .toSet()

    val layerComparison: LayerComparison
        get() =
            LayerComparison(
                hypLayer,
                refLayer,
                jobs.filter,
            )

    val entities: DocumentEntities
        get() =
            object : ValidatedDiskValue<DocumentEntities>(dir.resolve(ENTITIES_FILE)) {
                    override fun isValid(modified: Long) = modified >= lastModified

                    override fun set(): DocumentEntities = DocumentEntities.create(refLayer)
                }
                .readOrCreate<DocumentEntities>()

    fun getDistribution(annotation: Annotation, group: Annotation): DocumentDistribution =
        object :
                ValidatedDiskValue<DocumentDistribution>(
                    dir.resolve("distribution-$annotation-$group.json")
                ) {
                override fun isValid(modified: Long) = modified >= lastModified

                override fun set(): DocumentDistribution =
                    DocumentDistribution.create(refLayer, annotation, group)
            }
            .readOrCreate()

    fun getConfusion(annotation: Annotation): DocumentConfusion =
        object : ValidatedDiskValue<DocumentConfusion>(dir.resolve("confusion-$annotation.json")) {
                override fun isValid(modified: Long) = modified >= lastModified

                override fun set(): DocumentConfusion =
                    DocumentConfusion.create(
                        layerComparison,
                        annotation,
                    )
            }
            .readOrCreate()

    fun getMetrics(
        annotations: List<Annotation>,
        group: Annotation,
        analysis: Analysis,
    ): DocumentMetrics =
        object :
                ValidatedDiskValue<DocumentMetrics>(
                    dir.resolve("${Metrics.Settings(annotations, group,analysis).name}.json")
                ) {
                override fun isValid(modified: Long) = modified >= lastModified

                override fun set(): DocumentMetrics =
                    DocumentMetrics.create(
                        layerComparison,
                        annotations,
                        group,
                        analysis,
                    )
            }
            .readOrCreate()

    val spans: DocumentSpanEvaluation
        get() =
            object : ValidatedDiskValue<DocumentSpanEvaluation>(dir.resolve(SPANS_FILE)) {
                    override fun isValid(modified: Long) = modified >= lastModified

                    override fun set(): DocumentSpanEvaluation =
                        DocumentSpanEvaluation.create(
                            layerComparison,
                            refLayer,
                        )
                }
                .readOrCreate<DocumentSpanEvaluation>()

    companion object {
        private const val ENTITIES_FILE = "entities.json"
        private const val METRICS_FILE = "metrics.json"
        private const val SPANS_FILE = "spans.json"
    }
}
