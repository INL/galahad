package org.ivdnt.galahad.web.service.evaluation

import java.io.File
import java.util.*
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.Layer.Companion.SOURCE_LAYER
import org.ivdnt.galahad.evaluation.JobPair
import org.ivdnt.galahad.evaluation.comparison.DummyFilter
import org.ivdnt.galahad.evaluation.comparison.HeadGroupTermFilter
import org.ivdnt.galahad.evaluation.comparison.MetricsLayerFilter
import org.ivdnt.galahad.evaluation.csv.CsvFile
import org.ivdnt.galahad.evaluation.csv.CsvSampleExporter.Companion.samplesToCSV
import org.ivdnt.galahad.evaluation.metrics.*
import org.ivdnt.galahad.web.service.CorporaService
import org.springframework.stereotype.Service

@Service
class MetricsService(private val corpora: CorporaService) : BaseEvaluationService(corpora) {

    fun getDocumentMetrics(
        corpus: UUID,
        document: String,
        hypothesis: String,
        reference: String,
        annotations: List<Annotation>,
        group: Annotation,
        analysis: Annotation.Analysis,
    ): DocumentMetrics {
        val corpusObj = corpora.readOrThrow(corpus)
        val jobEval = corpusObj.evaluation.createOrThrow(JobPair(hypothesis, reference))
        val docEval = jobEval.documents.createOrThrow(document)
        return docEval.getMetrics(annotations, group, analysis)
    }

    fun getLayerMetrics(
        corpus: UUID,
        hypothesis: String,
        reference: String,
        analysis: Annotation.Analysis,
    ): List<GlobalMetrics> {
        val corpusObj = corpora.readOrThrow(corpus)
        val jobEval = corpusObj.evaluation.createOrThrow(JobPair(hypothesis, reference))
        val hypothesisAnnotations =
            corpusObj.layers.readOrThrow(hypothesis).metadata.annotations.keys
        val referenceAnnotations = corpusObj.layers.readOrThrow(reference).metadata.annotations.keys
        val commonAnnotations = hypothesisAnnotations.intersect(referenceAnnotations)
        // remove token if present
        return commonAnnotations
            .filter { it != Annotation.TOKEN }
            .map {
                jobEval.getMetrics(listOf(it), it, analysis).metrics.toGlobal(hypothesis)
            }
    }

    fun getLayerMetrics(
        corpus: UUID,
        hypothesis: String,
        reference: String,
        annotations: List<Annotation>,
        group: Annotation,
        analysis: Annotation.Analysis,
    ): JobMetrics {
        val corpusObj = corpora.readOrThrow(corpus)
        val jobEval = corpusObj.evaluation.createOrThrow(JobPair(hypothesis, reference))
        return jobEval.getMetrics(annotations, group, analysis)
    }

    fun getCorpusMetrics(
        corpus: UUID,
        annotations: List<Annotation>,
        group: Annotation,
        analysis: Annotation.Analysis,
    ): CorpusMetrics {
        val corpusObj = corpora.readOrThrow(corpus)
        return corpusObj.evaluation.getMetrics(annotations, group, analysis)
    }

    fun getMetricsSamples(
        corpus: UUID,
        layer: String,
        reference: String = SOURCE_LAYER,
        annotations: List<Annotation>,
        group: Annotation,
        analysis: Annotation.Analysis,
        classification: String,
        groupFilter: String? = null,
    ): ByteArray {
        val filter =
            if (groupFilter != null)
                MetricsLayerFilter(
                    HeadGroupTermFilter(group, groupFilter),
                    HeadGroupTermFilter(group, groupFilter),
                )
            else DummyFilter()
        val corpusObj = corpora.readOrThrow(corpus)
        val jobEval = corpusObj.evaluation.createOrThrow(JobPair(layer, reference, filter = filter))
        val metrics = JobMetrics.create(corpusObj, jobEval.documents, annotations, group, analysis)
        var csv = ""
        if (groupFilter != null) {
            csv =
                samplesToCSV(
                    metrics.metrics.grouped.entries
                        .first { it.key == groupFilter }
                        .value
                        .classification(classification)
                        .samples,
                    jobEval.hypJob,
                    jobEval.refJob,
                )
        } else {
            csv =
                samplesToCSV(
                    metrics.metrics.untruncatedClasses.classification(classification).samples,
                    jobEval.hypJob,
                    jobEval.refJob,
                )
        }
        val fileName = "${Metrics.Settings(annotations, group,analysis).name}-$classification.csv"
        return samplesToZip(corpus, layer, reference, csv, fileName)
    }

    fun createMetricsCsv(dir: File, corpus: UUID, hypothesis: String, reference: String) {
        dir.mkdirs()

        val hypAnnotations = annotationsInLayer(corpus, hypothesis)
        val refAnnotations = annotationsInLayer(corpus, reference)
        val supported =
            setOf(
                Annotation.POS,
                Annotation.LEMMA,
                Annotation.UPOS,
                Annotation.DEPREL,
                Annotation.NER,
            )
        val common = hypAnnotations.intersect(refAnnotations).intersect(supported)

        val metrics =
            getLayerMetrics(
                corpus,
                hypothesis,
                reference,
                Annotation.Analysis.BOTH,
            )
        val globFile = CsvFile(dir.resolve("metrics-global.csv"))
        for (globMetric in metrics) {
            globFile.append(GlobalMetrics.getCsvHeader())
            globFile.append(globMetric.toCsv())
        }

        for (annotation in common) {
            val groupedMetrics =
                getLayerMetrics(
                    corpus,
                    hypothesis,
                    reference,
                    listOf(annotation),
                    annotation,
                    Annotation.Analysis.BOTH,
                )
            val file = CsvFile(dir.resolve("metrics-${groupedMetrics.metrics.settings.name}.csv"))
            file.append(JobMetrics.toCsv(groupedMetrics.metrics))
        }
    }
}
