package org.ivdnt.galahad.evaluation

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.app.*
import org.ivdnt.galahad.data.CorporaController
import org.ivdnt.galahad.data.corpus.CorpusMetadata
import org.ivdnt.galahad.data.document.SOURCE_LAYER_NAME
import org.ivdnt.galahad.evaluation.comparison.ConfusionLayerFilter
import org.ivdnt.galahad.evaluation.comparison.PosLemmaTermFilter
import org.ivdnt.galahad.evaluation.comparison.MetricsLayerFilter
import org.ivdnt.galahad.evaluation.confusion.Confusion
import org.ivdnt.galahad.evaluation.confusion.CorpusConfusion
import org.ivdnt.galahad.evaluation.distribution.CorpusDistribution
import org.ivdnt.galahad.evaluation.metrics.CorpusMetrics
import org.ivdnt.galahad.evaluation.metrics.MetricsSettings
import org.ivdnt.galahad.evaluation.metrics.METRIC_TYPES
import org.ivdnt.galahad.evaluation.metrics.PosByPosMetricsSettings
import org.ivdnt.galahad.port.csv.CSVFile
import org.ivdnt.galahad.util.createZipFile
import org.ivdnt.galahad.util.setContentDisposition
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.io.path.createTempDirectory

const val DISTRIBUTION_MAX_SIZE = 1000

@RestController
class EvaluationController(
    val corpora: CorporaController,
) : Logging {

    @Autowired
    private val request: HttpServletRequest? = null

    @Autowired
    private val response: HttpServletResponse? = null

    @GetMapping(DISTRIBUTION_URL)
    @CrossOrigin
    fun getDistribution(
        @PathVariable corpus: UUID,
        @PathVariable job: String,
    ): CorpusDistribution {
        logger.info("Get distribution for hypothesis layer $job in $corpus")
        return CorpusDistribution(
            corpora.getReadAccessOrThrow(corpus, request),
            job
        ).trim(DISTRIBUTION_MAX_SIZE) as CorpusDistribution
    }

    @GetMapping(METRICS_URL)
    @CrossOrigin
    fun getMetrics(
        @PathVariable corpus: UUID,
        @PathVariable job: String,
        @RequestParam reference: String?,
    ): CorpusMetrics {
        logger.info("Get metrics for reference layer $reference and hypothesis layer $job in $corpus")

        val cm = CorpusMetrics(
            corpora.getReadAccessOrThrow(corpus, request),
            settings = METRIC_TYPES,
            hypothesis = job,
            reference = if (reference.isNullOrBlank()) SOURCE_LAYER_NAME else reference
        )
        return cm
    }

    @GetMapping(METRICS_CSV_URL)
    @CrossOrigin
    fun getMetricsSamples(
        @PathVariable corpus: UUID,
        @PathVariable job: String,
        @RequestParam reference: String,
        @RequestParam setting: String,
        @RequestParam("class") classType: String,
        @RequestParam group: String?
    ): ByteArray {
        val setting = METRIC_TYPES.first { it.id == setting }

        val layerFilter: MetricsLayerFilter? = getLayerFilter(group, setting)

        val cm = CorpusMetrics(
            corpus = corpora.getReadAccessOrThrow(corpus, request),
            hypothesis = job,
            reference = reference,
            layerFilter = layerFilter,
            truncate = false,
            settings = listOf(setting),
        )
        val mt = cm.metricTypes.values.first()

        if (group != null) {
            val fileName = "metrics-$reference-$job-${classType}-${group}.csv"
            val csv = mt.samplesToCsv(group, classType)
            return samplesToZip(corpus, job, reference, csv, fileName)
        } else {
            val fileName = "metrics-$reference-$job-${mt.setting.id}-${classType}.csv"
            val csv = mt.samplesToCsv(classType)
            return samplesToZip(corpus, job, reference, csv, fileName)
        }
    }

    private fun getLayerFilter(group: String?, setting: MetricsSettings): MetricsLayerFilter? {
        val layerFilter: MetricsLayerFilter?
        if (group != null) {
            val hypoFilter: PosLemmaTermFilter
            val refFilter: PosLemmaTermFilter
            if (setting is PosByPosMetricsSettings) {
                hypoFilter = PosLemmaTermFilter(group, null)
                refFilter = PosLemmaTermFilter(group, null)
            } else {
                hypoFilter = PosLemmaTermFilter(null, group)
                refFilter = PosLemmaTermFilter(null, group)
            }
            layerFilter = MetricsLayerFilter(hypoFilter, refFilter)
        } else {
            layerFilter = null
        }
        return layerFilter
    }

    fun samplesToZip(
        corpus: UUID,
        job: String,
        reference: String?,
        csvBody: String,
        fileName: String
    ): ByteArray {
        // Create csv file.
        val dir: File = createTempDirectory("samples").toFile()
        val file = CSVFile(dir.resolve(fileName))
        file.appendText(CSVFile.toCSVHeader(listOf("token","$reference lemma","$reference pos","$job lemma","$job pos")))
        file.appendText(csvBody)
        // Write metadata & create zip
        val metadata = writeMetadataToDir(corpus, job, reference, dir)
        val zipFile = createZipFile(dir.listFiles()!!.asSequence())
        // Configure response for zip.
        response!!.contentType = "application/zip"
        response.setContentDisposition(metadata.name + "-evaluation.zip")
        // zip the directory
        return zipFile.readBytes()
    }

    @GetMapping(CONFUSION_URL)
    @CrossOrigin
    fun getConfusion(
        @PathVariable corpus: UUID,
        @PathVariable job: String,
        @RequestParam reference: String?,
    ): Confusion {
        logger.info("Get confusion for reference layer $reference and hypothesis layer $job in $corpus")
        return CorpusConfusion(
            corpora.getReadAccessOrThrow(corpus, request),
            hypothesis = job,
            reference = if (reference.isNullOrBlank()) SOURCE_LAYER_NAME else reference,
        )
    }

    @GetMapping(CONFUSION_CSV_URL)
    @CrossOrigin
    fun getConfusionSamples(
        @PathVariable corpus: UUID,
        @PathVariable  job: String,
        @RequestParam reference: String,
        @RequestParam hypoPosFilter: String?,
        @RequestParam refPosFilter: String?,
    ): ByteArray {
        val cc = CorpusConfusion(
            corpus = corpora.getReadAccessOrThrow(corpus, request),
            hypothesis = job,
            reference = reference,
            layerFilter = ConfusionLayerFilter(
                PosLemmaTermFilter(hypoPosFilter, null),
                PosLemmaTermFilter(refPosFilter, null)
            )
        )
        val fileName = "confusion-${refPosFilter}-${hypoPosFilter}.csv"
        val csv = cc.samplesToCSV()
        return samplesToZip(corpus, job, reference, csv, fileName)
    }

    @GetMapping(EVALUATION_CSV_URL)
    @ResponseBody
    @CrossOrigin
    fun download(
        @PathVariable corpus: UUID,
        @PathVariable job: String,
        @RequestParam reference: String?,
        @RequestParam hypothesisPos: String?,
        @RequestParam referencePos: String?,
    ): ByteArray {
        if (reference != null && hypothesisPos != null && referencePos != null)
            return getConfusionSamples(corpus, job, reference, hypothesisPos, referencePos)

        return executeAndLogTime("GetEvaluationCSVs") {
            val dir: File = createTempDirectory("evaluation").toFile()
            createDistributionCsv(dir, corpus, job)
            if (reference != null) {
                createMetricsCsv(dir, corpus, job, reference)
                createConfusionCsv(dir, corpus, job, reference)
            }
            val metadata = writeMetadataToDir(corpus, job, reference, dir)
            response!!.contentType = "application/zip"
            response.setContentDisposition(metadata.name + "-evaluation.zip")

            // zip the directory
            val zipFile = createZipFile(dir.listFiles()!!.asSequence())
            zipFile.readBytes()
        }
    }

    private fun createConfusionCsv(dir: File, corpus: UUID, job: String, reference: String?) {
        val file = CSVFile(dir.resolve("confusion.csv"))
        file.appendText(getConfusion(corpus, job, reference).countsToCSV())
    }

    private fun createMetricsCsv(dir: File, corpus: UUID, job: String, reference: String?) {
        val metrics = getMetrics(corpus, job = job, reference = reference)
        val globFile = CSVFile(dir.resolve("metrics-global.csv"))
        globFile.appendText(metrics.toGlobalCsv())

        metrics.metricTypes.values.forEach { mt ->
            val file = CSVFile(dir.resolve("metrics-${mt.setting.id}.csv"))
            file.appendText(mt.toGroupedCsv())
        }
    }

    private fun createDistributionCsv(dir: File, corpus: UUID, job: String) {
        val file = CSVFile(dir.resolve("distribution.csv"))
        file.appendText(getDistribution(corpus, job).toCSV())
    }

    private fun writeMetadataToDir(
        corpus: UUID, job: String, reference: String?, dir: File,
    ): CorpusMetadata {
        val corpus = corpora.getReadAccessOrThrow(corpus, request)
        val metadata = corpus.metadata.expensiveGet()

        val metadataFile = File(dir.resolve("metadata.txt").toURI())
        metadataFile.appendText("Evaluation generated by Galahad\n")
        metadataFile.appendText("${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}\n")
        metadataFile.appendText("Corpus: ${metadata.name}\n")
        metadataFile.appendText("Documents: ${metadata.numDocs}\n")
        metadataFile.appendText("Era: ${metadata.eraFrom}-${metadata.eraTo}\n")
        metadataFile.appendText("Hypothesis: $job\n")
        if (reference != null) metadataFile.appendText("Reference: $reference\n")
        return metadata
    }
}