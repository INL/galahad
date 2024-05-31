package org.ivdnt.galahad.evaluation.assays

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.FileBackedCache
import org.ivdnt.galahad.app.ASSAYS_URL
import org.ivdnt.galahad.app.ASSAY_URL
import org.ivdnt.galahad.data.CorporaController
import org.ivdnt.galahad.data.document.SOURCE_LAYER_NAME
import org.ivdnt.galahad.evaluation.metrics.FlatMetricType
import org.ivdnt.galahad.evaluation.metrics.FlatMetricTypeAssay
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.util.*
import kotlin.collections.HashMap

/**
 * {
 *     "dataset-1": {
 *         "posByPos": {
 *             "tagger-1": {
 *                 "micro": { ... }, "macro": { ... }
 *             },
 *             "tagger-2": { ... },
 *         },
 *         "lemmaByLemma": { ... },
 *     },
 *     "dataset-2": { ... },
 * }
 */
typealias AssaysMatrix = Map<String, Map<String, FlatMetricTypeAssay>>
typealias MutableAssaysMatrix = MutableMap<String, MutableMap<String, MutableMap<String, FlatMetricType>>>

@RestController
class AssaysController(
    val corpora: CorporaController,
) : Logging {

    @Autowired
    private val request: HttpServletRequest? = null

    @Autowired
    private val response: HttpServletResponse? = null

    /**
     * A matrix of 'tagger' -> 'dataset' -> 'FlatMetric' -> 'scores per category',
     * for all datasets corpora that have been tagged with at least one tagger, excluding the sourceLayer.
     */
    val assaysMatrix = object : FileBackedCache<AssaysMatrix>(corpora.assaysFile, HashMap()) {
        override fun isValid(lastModified: Long): Boolean {
            return corpora.datasets.firstOrNull { it.lastModified > lastModified } == null
            TODO("Maybe just check the validity of the other assays?")
        }

        override fun set(): AssaysMatrix {
            // tagger -> dataset -> assay
            val assaysMatrix: MutableAssaysMatrix = HashMap()
            // For all datasets
            corpora.datasets.forEach { dataset ->
                // For all jobs in the dataset
                dataset.jobs.readAll()
                    // Skip the source layer
                    .filter { it.name != SOURCE_LAYER_NAME }
                    // Add the assay to the matrix
                    .forEach { job ->
                        val meta = dataset.metadata.expensiveGet()
                        // Initialize the dataset row if needed
                        if (assaysMatrix[meta.name] == null) {
                            assaysMatrix[meta.name] = HashMap()
                        }
                        val assay = getAssay(meta.uuid, job.name)
                        assay?.forEach {
                            assaysMatrix[meta.name]?.putIfAbsent(it.key, HashMap())
                            assaysMatrix[meta.name]?.get(it.key)?.put(job.name, it.value)
                        }
                    }
            }
            return assaysMatrix
        }
    }

    /**
     * Get the assay for a single job in a specific corpus. Also used to construct [assaysMatrix].
     */
    @GetMapping(ASSAY_URL)
    @CrossOrigin
    fun getAssay(
        @PathVariable corpus: UUID,
        @PathVariable job: String,
    ): FlatMetricTypeAssay? {
        // The assay is some simple, preferably plain numerical, value that indicates preformance
        // Since the exact requirements for the definition of an assays might still change
        // we don't provide a solid definition, but instead it is defined ad hoc here
        return corpora.getReadAccessOrThrow(corpus, request).jobs.readOrNull(job)?.assay?.get<FlatMetricTypeAssay>()
    }

    /**
     * Return [assaysMatrix].
     */
    @RequestMapping(value = [ASSAYS_URL], method = [RequestMethod.GET], produces = ["application/json"])
    @CrossOrigin
    fun getAssays(): AssaysMatrix {
        return assaysMatrix.get<AssaysMatrix>()
    }
}