package org.ivdnt.galahad.web.service.evaluation

import java.io.File
import java.util.*
import kotlin.io.path.createTempDirectory
import org.ivdnt.galahad.layer.Layer
import org.ivdnt.galahad.util.zipDir
import org.ivdnt.galahad.web.service.CorporaService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class EvaluationService(private val corpora: CorporaService) : BaseEvaluationService(corpora) {
    @Autowired private val distributionService: DistributionService? = null
    @Autowired private val metricsService: MetricsService? = null
    @Autowired private val confusionService: ConfusionService? = null

    fun getEvaluation(corpus: UUID, job: String, reference: String?): ByteArray {
        val dir: File = createTempDirectory().toFile()
        distributionService!!.createDistributionCsv(dir.resolve("distribution"), corpus, job)
        if (reference != null) {
            metricsService!!.createMetricsCsv(dir.resolve("metrics"), corpus, job, reference)
            confusionService!!.createConfusionCsv(dir.resolve("confusion"), corpus, job, reference)
        }
        writeMetadataToDir(corpus, job, reference ?: Layer.SOURCE_LAYER, dir)
        // zip the directory
        val zipFile = zipDir(dir)
        return zipFile.readBytes()
    }
}
