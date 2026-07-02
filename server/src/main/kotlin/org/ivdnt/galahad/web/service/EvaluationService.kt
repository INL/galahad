package org.ivdnt.galahad.web.service

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.io.path.createTempDirectory
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.Layer.Companion.SOURCE_LAYER
import org.ivdnt.galahad.corpora.CorpusStatistics
import org.ivdnt.galahad.evaluation.JobPair
import org.ivdnt.galahad.evaluation.comparison.ConfusionLayerFilter
import org.ivdnt.galahad.evaluation.comparison.HeadGroupTermFilter
import org.ivdnt.galahad.evaluation.comparison.MetricsLayerFilter
import org.ivdnt.galahad.evaluation.confusion.JobConfusion
import org.ivdnt.galahad.evaluation.csv.CsvFile
import org.ivdnt.galahad.evaluation.csv.CsvSampleExporter.Companion.samplesToCSV
import org.ivdnt.galahad.evaluation.distribution.DocumentDistribution
import org.ivdnt.galahad.evaluation.distribution.TypeToken
import org.ivdnt.galahad.evaluation.entities.CorpusEntities
import org.ivdnt.galahad.evaluation.metrics.DocumentMetrics
import org.ivdnt.galahad.evaluation.metrics.JobMetrics
import org.ivdnt.galahad.evaluation.metrics.Metrics
import org.ivdnt.galahad.util.toValidFileName
import org.ivdnt.galahad.util.zipDir
import org.springframework.stereotype.Service

@Service
class EvaluationService(private val corpora: CorporaService) {
    fun getConfusionSamples(
        hypFilter: String,
        refFilter: String,
        annotation: Annotation,
        corpus: UUID,
        layer: String,
        reference: String,
    ): ByteArray {
        // Ensure the job has the required annotation types.
        //        if (annotation !in annotationTypesForTagger(job, corpus)) {
        //            throw AnnotationNotSupported(job, annotation)
        //        }
        val filter =
            ConfusionLayerFilter(
                HeadGroupTermFilter(annotation, hypFilter),
                HeadGroupTermFilter(annotation, refFilter),
            )
        val corpusObj = corpora.readOrThrow(corpus)
        val jobEval = corpusObj.evaluation.createOrThrow(JobPair(layer, reference, filter = filter))
        val fileName = "confusion-$refFilter-$hypFilter.csv"
        val confusion = JobConfusion.create(corpusObj, jobEval.documents, annotation).confusion
        val csv =
            samplesToCSV(
                confusion.values.first().values.first().samples,
                jobEval.hypJob,
                jobEval.refJob,
            )
        return samplesToZip(corpus, layer, reference, csv, fileName)
    }

    fun getMetricsSamples(
        corpus: UUID,
        layer: String,
        reference: String = SOURCE_LAYER,
        annotations: List<Annotation>,
        group: Annotation,
        classification: String,
        groupFilter: String? = null,
    ): ByteArray {
        val filter =
            if (groupFilter != null)
                MetricsLayerFilter(
                    HeadGroupTermFilter(group, groupFilter),
                    HeadGroupTermFilter(group, groupFilter),
                )
            else null
        val corpusObj = corpora.readOrThrow(corpus)
        val jobEval = corpusObj.evaluation.createOrThrow(JobPair(layer, reference, filter = filter))
        val metrics = JobMetrics.create(corpusObj, jobEval.documents, annotations, group)
        val csv =
            samplesToCSV(
                metrics.metrics.grouped.entries
                    .first { it.key == groupFilter }
                    .value
                    .classification(classification)
                    .samples,
                jobEval.hypJob,
                jobEval.refJob,
            )
        val fileName = "${Metrics.Settings(annotations, group).name}-$classification.csv"
        return samplesToZip(corpus, layer, reference, csv, fileName)
    }

    //    fun getLayerComparison(
    //        corpus: UUID,
    //        document: String,
    //        job: String,
    //        reference: String?,
    //    ): List<TermComparison> {
    //        val reference: String = reference ?: SOURCE_LAYER
    //        return LayerComparison(
    //                hypothesis =
    //                    corpora.readOrThrow(corpus,
    // user).jobs.readOrThrow(job).getLayer(document),
    //                reference =
    //                    corpora.readOrThrow(corpus,
    // user).jobs.readOrThrow(reference).getLayer(document),
    //            )
    //            .matches
    //    }

    fun getEvaluation(corpus: UUID, job: String, reference: String?): ByteArray {
        val dir: File = createTempDirectory().toFile()
        createDistributionCsv(dir.resolve("distribution"), corpus, job)
        createEntitiesCsv(dir.resolve("entities"), corpus)
        if (reference != null) {
            createMetricsCsv(dir.resolve("metrics"), corpus, job, reference)
            createConfusionCsv(dir.resolve("confusion"), corpus, job, reference)
        }
        writeMetadataToDir(corpus, job, reference, dir)
        // zip the directory
        val zipFile = zipDir(dir)
        return zipFile.readBytes()
    }

    private fun createEntitiesCsv(dir: File, corpus: UUID) {
        dir.mkdirs()
        val entities = getCorpusEntities(corpus)
        val file = CsvFile(dir.resolve("entities.csv"))
        file.append(CorpusEntities.toCsv(entities))
    }

    private fun createDistributionCsv(dir: File, corpus: UUID, job: String) {
        dir.mkdirs()
        // val distributions = getLayerDistribution(corpus, job)
        // TODO export
        //        distributions.typeTokens.forEach { (annotation, distribution) ->
        //            val file = CsvFile(dir.resolve("distribution-${annotation.value}.csv"))
        //            file.append(JobDistribution.toCsv(distribution))
        //        }
    }

    private fun createConfusionCsv(dir: File, corpus: UUID, hypothesis: String, reference: String) {
        dir.mkdirs()
        //        val confusions = getJobConfusion(corpus, hypothesis, reference)
        //        confusions.confusion.forEach { (annotation, confusion) ->
        //            val file = CsvFile(dir.resolve("confusion-${annotation.value}.csv"))
        //            file.append(JobConfusion.toCsv(confusion))
        //        }
    }

    private fun createMetricsCsv(dir: File, corpus: UUID, hypothesis: String, reference: String) {
        dir.mkdirs()
        //        val metrics = getJobMetric(corpus, hypothesis, reference, Annotation.POS,
        // Annotation.POS)
        //        val globFile = CsvFile(dir.resolve("metrics-global.csv"))
        //        globFile.append(metrics.toGlobalCsv())

        //        metrics.classesByGroup.values.forEach { mt ->
        //            val file = CsvFile(dir.resolve("metrics-${mt.settings.name}.csv"))
        //            file.append(JobMetric.toCsv(mt))
        //        }
    }

    private fun writeMetadataToDir(
        corpus: UUID,
        job: String,
        reference: String?,
        dir: File,
    ): CorpusStatistics {
        val metadata = corpora.readOrThrow(corpus).statistics

        val metadataFile = File(dir.resolve("metadata.txt").toURI())
        metadataFile.appendText("Evaluation generated by Galahad\n")
        metadataFile.appendText(
            "${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}\n"
        )
        metadataFile.appendText("Corpus: ${metadata.name}\n")
        metadataFile.appendText("Documents: ${metadata.documents}\n")
        metadataFile.appendText("Era: ${metadata.period?.from}-${metadata.period?.to}\n")
        metadataFile.appendText("Hypothesis: $job\n")
        if (reference != null) metadataFile.appendText("Reference: $reference\n")
        return metadata
    }

    //    private fun annotationTypesForTagger(job: String, corpus: UUID): Set<Annotation> {
    //        val corpusObj = corpora.readOrThrow(corpus, user)
    //        return Tagger.readOrThrow(job, corpusObj).annotationSet
    //    }

    fun samplesToZip(
        corpus: UUID,
        job: String,
        reference: String?,
        csvBody: String,
        fileName: String,
    ): ByteArray {
        // Create csv file.
        val dir: File = createTempDirectory().toFile()
        val validFileName = fileName.toValidFileName()
        val file = CsvFile(dir.resolve(validFileName))
        file.append(csvBody)
        // Write metadata & create zip
        writeMetadataToDir(corpus, job, reference, dir)
        // zip the directory
        return zipDir(dir).readBytes()
    }

    //
    //    fun getTokenFrequency(corpus: UUID, job: String, reference: String?): CorpusMetrics {
    //        val corpusObj = corpora.readOrThrow(corpus, user)
    //        val setting =
    //            FrequencyMetricsSettings(TokenFrequency(corpusObj, job),
    // LemmaByLemmaMetricsSettings())
    //        val settings = listOf(setting)
    //        val cm =
    //            CorpusMetrics(
    //                corpusObj,
    //                settings = settings,
    //                hypothesis = job,
    //                reference = if (reference.isNullOrBlank()) SOURCE_LAYER else reference,
    //            )
    //        return cm
    //    }
    //
    //    fun getDocumentEntities(corpus: UUID, document: String, job: String): DocumentEntities {
    //        val corpus = corpora.readOrThrow(corpus, user)
    //        val jobEval = corpus.evaluation.createOrThrow(JobPair(job))
    //        val docEval = jobEval.documents.createOrThrow(document)
    //        return docEval.entities
    //    }
    //
    //    fun getJobEntities(corpus: UUID, job: String): JobEntities {
    //        val corpusObj = corpora.readOrThrow(corpus, user)
    //        val jobEval = corpusObj.evaluation.createOrThrow(JobPair(job))
    //        return jobEval.entities
    //    }

    fun getCorpusEntities(corpus: UUID): CorpusEntities {
        val corpusObj = corpora.readOrThrow(corpus)
        return corpusObj.evaluation.entities
    }

    fun getDocumentDistribution(
        corpus: UUID,
        layer: String,
        document: String,
        annotation: Annotation,
        group: Annotation,
    ): DocumentDistribution {
        val corpusObj = corpora.readOrThrow(corpus)
        val jobEval = corpusObj.evaluation.createOrThrow(JobPair(layer))
        val docEval = jobEval.documents.createOrThrow(document)
        return docEval.getDistribution(annotation, group)
    }

    fun getLayerDistribution(
        corpus: UUID,
        layer: String,
        annotation: Annotation,
        group: Annotation,
    ): List<TypeToken> {
        val corpusObj = corpora.readOrThrow(corpus)
        val jobEval = corpusObj.evaluation.createOrThrow(JobPair(layer))
        return jobEval.getDistribution(annotation, group).typeTokens
    }

    fun getJobConfusion(
        corpus: UUID,
        hypothesis: String,
        reference: String,
        annotation: Annotation,
    ): JobConfusion {
        val corpusObj = corpora.readOrThrow(corpus)
        val jobEval = corpusObj.evaluation.createOrThrow(JobPair(hypothesis, reference))
        return jobEval.getConfusion(annotation)
    }

    fun getDocumentMetric(
        corpus: UUID,
        document: String,
        hypothesis: String,
        reference: String,
        annotations: List<Annotation>,
        group: Annotation,
    ): DocumentMetrics {
        val corpusObj = corpora.readOrThrow(corpus)
        val jobEval = corpusObj.evaluation.createOrThrow(JobPair(hypothesis, reference))
        val docEval = jobEval.documents.createOrThrow(document)
        return docEval.getMetrics(annotations, group)
    }

    fun getJobMetric(
        corpus: UUID,
        hypothesis: String,
        reference: String,
        annotations: List<Annotation>,
        group: Annotation,
    ): JobMetrics {
        val corpusObj = corpora.readOrThrow(corpus)
        val jobEval = corpusObj.evaluation.createOrThrow(JobPair(hypothesis, reference))
        return jobEval.getMetrics(annotations, group)
    }

    //
    //    fun getDocumentSpanEvaluation(
    //        corpus: UUID,
    //        document: String,
    //        hypothesis: String,
    //        reference: String,
    //    ): DocumentSpanEvaluation {
    //        val corpusObj = corpora.readOrThrow(corpus, user)
    //        val jobEval = corpusObj.evaluation.createOrThrow(JobPair(hypothesis, reference))
    //        val docEval = jobEval.documents.createOrThrow(document)
    //        return docEval.spans
    //    }

    // TODO duplicate code with export service
    fun getCorpusName(corpus: UUID): String = corpora.readOrThrow(corpus).metadata.name
}
