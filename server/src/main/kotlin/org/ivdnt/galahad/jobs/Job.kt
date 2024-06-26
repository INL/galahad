package org.ivdnt.galahad.jobs

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.BaseFileSystemStore
import org.ivdnt.galahad.FileBackedCache
import org.ivdnt.galahad.FileBackedValue
import org.ivdnt.galahad.data.corpus.Corpus
import org.ivdnt.galahad.data.document.SOURCE_LAYER_NAME
import org.ivdnt.galahad.data.layer.LayerPreview
import org.ivdnt.galahad.data.layer.LayerSummary
import org.ivdnt.galahad.data.layer.plus
import org.ivdnt.galahad.evaluation.metrics.*
import org.ivdnt.galahad.jobs.DocumentJob.DocumentProcessingStatus
import org.ivdnt.galahad.taggers.Tagger
import org.ivdnt.galahad.taggers.TaggerStore
import org.springframework.core.io.FileSystemResource
import org.springframework.http.*
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.io.File
import java.net.URL
import java.util.*

/**
 * A job is saved to disk as a folder under jobs/ (managed by [Jobs]), with the following files:
 *
 * - documents/: a folder containing all documents in the job. A single document is represented by [DocumentJob]. These can be retrieved with [document].
 * - _isActive: a file that stores whether the job is currently being processed by the tagger.
 * - assay.cache: a cache file storing the global [Metrics] of the job.
 * - state.cache: a cache file storing the [State] of the job.
 */
class Job(
    workDirectory: File, // the name of this directory is the name of the job/tagger
    private val corpus: Corpus,
) : BaseFileSystemStore(workDirectory), Logging {

    val taggerStore = TaggerStore()
    val name: String = workDirectory.name

    private val documentsWorkDirectory = workDirectory.resolve("documents")
    private val documentNames
        get() = documentsWorkDirectory.list()?.toSet() ?: throw Exception("Error accessing job documents")

    /**
     * Note: this init block has to be above [documents].
     * Because this.documents requires the documents dir to exist.
     */
    init {
        // TODO cleaner solution
        if (workDirectory.name == "null" || workDirectory.name == "undefined") {
            workDirectory.deleteRecursively()
            throw Exception("Job name not allowed")
        }
        documentsWorkDirectory.mkdirs()
        if (!taggerStore.ids.contains(name) && name != SOURCE_LAYER_NAME) {
            // A job without a tagger is probably invalid, but we want to be careful,
            // so we only delete it if the job is empty
            // Otherwise it deserves at least manual inspection
            if (documentNames.isEmpty()) workDirectory.deleteRecursively()
            throw Exception("Tagger $name unknown.")
        }
    }

    private val documents: List<DocumentJob> = documentNames.map { document(it) }

    /** Number of documents at the tagger per job */
    val DOC_PARALLELIZATION_SIZE = 3

    /**
     * Whether the job is currently being processed (i.e. has sent files to the tagger to become tagged at some point).
     */
    var _isActive: FileBackedValue<Boolean> = FileBackedValue(workDirectory.resolve("_isActive"), false)

    var isActive: Boolean
        get() = _isActive.read<Boolean>()
        set(value) {
            _isActive.modify<Boolean> { value }; corpus.invalidateCache()
        }

    /**
     * The sum of the global [Metrics] score of all the documents of the job (as opposed to per PoS).
     * Cached in a file, as it is expensive.
     */
    val assay = object : FileBackedCache<Map<String,FlatMetricType>>(
        file = workDirectory.resolve("assay.cache"), initValue = mapOf()
    ) {
        override fun isValid(lastModified: Long): Boolean {
            return lastModified >= this@Job.lastModified
        }

        override fun set(): Map<String,FlatMetricType> {
            return CorpusMetrics(
                corpus = corpus,
                settings = METRIC_TYPES,
                hypothesis = name,
                reference = SOURCE_LAYER_NAME
            ).metricTypes.mapValues { it.value.toFlat() }
        }
    }

    /**
     * Progress of the job based on the status of the [DocumentJob]s of this job.
     */
    val progress: Progress
        get() {
            val statuses = corpus.documents.allNames.map { document(it).status }
            val errors =
                documentNames.mapNotNull { name -> document(name).getError?.let { error -> name to error } }.toMap()
            return Progress(
                pending = statuses.count { it == DocumentProcessingStatus.PENDING },
                processing = statuses.count { it == DocumentProcessingStatus.PROCESSING },
                failed = statuses.count { it == DocumentProcessingStatus.ERROR },
                finished = statuses.count { it == DocumentProcessingStatus.FINISHED },
                errors = errors
            )
        }

    private fun deleteInactiveProcesses() {
        documents.filter { it.isProcessing }.forEach { documentJob ->
            // For each document that claims to be processing, verify if its pid is present at the tagger
            // If not, delete pid.
            try {
                val jsonStr: String? =
                    taggerRequest(this, "status/${documentJob.getProcessingID}", HttpMethod.GET, String::class.java)
                val parser: Parser = Parser.default()
                val json: JsonObject = parser.parse(StringBuilder(jsonStr!!)) as JsonObject
                if (json.boolean("busy") == false && json.boolean("pending") == false) {
                    // The doc is either finished, has an error, or does not exist.
                    documentJob.cancel()
                    stateFile.delete()
                }
            } catch (e: Exception) {
                // The tagger can't be reached, so no way to tell if the document is still processing.
                // If the tagger restarts, it does reprocess documents. Maybe including this one, so we keep it.
            }
        }
        if (documents.count { it.isProcessing } == 0 && isActive) {
            // Writing invalidates cache, so only write if isActive would change.
            isActive = false
        }
    }

    /**
     * Preview of the resulting terms of this job.
     * Show the first preview of the first document that isn't LayerPreview.EMPTY.
     */
    val preview: LayerPreview
        get() = documents.map { it.result.preview }.firstNotNullOfOrNull { it: LayerPreview ->
            if (it == LayerPreview.EMPTY) null else it
        } ?: LayerPreview.EMPTY
    val stateFile: File = workDirectory.resolve("state.cache")

    /**
     * The state of the job, which is cached in a file.
     * This is a very expensive operation, so we want to cache it.
     */
    private val stateCache = object : FileBackedCache<State>(
        file = stateFile, initValue = State()
    ) {
        override fun isValid(lastModified: Long): Boolean {
            return lastModified >= this@Job.lastModified
        }

        override fun set(): State {
            // sum up the number of tokens/lemmas/etc of all documents
            // This is very expensive
            val resultSummary: LayerSummary =
                documents.map { it.result.summary }.reduceOrNull { a, b -> a + b } ?: LayerSummary()
            return State(
                taggerStore.getSummaryOrNull(name, corpus.sourceTagger).expensiveGet() ?: Tagger(),
                progress,
                preview,
                resultSummary,
                lastModified = this@Job.lastModified
            )
        }
    }

    val state: State
        get() {
            deleteInactiveProcesses()
            return stateCache.get<State>()
        }

    fun document(name: String): DocumentJob {
        return DocumentJob(documentsWorkDirectory.resolve(name))
    }

    fun documentNameForProcessingIDOrNull(id: UUID): String? {
        return documents.filter { it.getProcessingID == id }.map { it.name }.firstOrNull()
    }

    fun start() {
        isActive = true
        next()
    }

    fun next() {
        if (name == SOURCE_LAYER_NAME) return // Nothing to process
        if (!isActive) return
        // Launch a coroutine so we can quickly return
        runBlocking {
            launch {
                uploadDocs()
            }
        }
    }

    /**
     * Upload documents to the tagger where they will be automatically processed.
     * Only ever upload as many files such that there are [DOC_PARALLELIZATION_SIZE] number of documents at the tagger.
     * Upon upload, a processingID is returned by the tagger, which we store in the respective [DocumentJob].
     */
    private fun uploadDocs() {
        // Quickly count the documents currently being processed
        val numCurrentlyBeingProcessed = documents.count { it.status == DocumentProcessingStatus.PROCESSING }

        // Upload the first documents to the tagger
        // Because the tag function might be activated multiple times,
        // We correct the number to remain with the defined parallelization
        val numberToUpload = 0.coerceAtLeast(DOC_PARALLELIZATION_SIZE - numCurrentlyBeingProcessed)

        // Upload the documents to the tagger
        corpus.documents.readAll().filter {
            val metadata = it.metadata.expensiveGet()
            metadata.valid && document(metadata.name).status == DocumentProcessingStatus.PENDING || document(
                metadata.name
            ).status == DocumentProcessingStatus.ERROR
        }.take(numberToUpload).forEach {
            val processingID = postInputToTagger(it.plainTextFile)
            // Store the processingID, so we can match it with the incoming file later
            document(it.metadata.expensiveGet().name).setProcessingID(processingID)
        }
    }

    /** Cancel the job by deleting all the currently processing input files at the tagger. */
    fun cancel() {
        isActive = false
        documents.forEach { documentJob ->
            try {
                if (documentJob.isProcessing) {
                    deleteInputAtTagger(documentJob.getProcessingID!!)
                }
            } catch (e: Exception) {
                // Ignore, so we cancel other documents even if one fails.
            } finally {
                documentJob.cancel()
            }
        }
    }

    fun delete() {
        cancel()
        workDirectory.deleteRecursively()
    }

    /** Upload a single file to the tagger */
    private fun postInputToTagger(file: File): UUID {
        // Custom request entity due to file.
        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA
        val params = LinkedMultiValueMap<String, Any>()
        params.add("file", FileSystemResource(file))
        val requestEntity: HttpEntity<LinkedMultiValueMap<String, Any>> = HttpEntity(params, headers)

        val route = "input"
        val result: String? = taggerRequest(this, route, HttpMethod.POST, String::class.java, requestEntity)
        return UUID.fromString(result) ?: throw Exception("No result received when uploading file")
    }

    // Delete input files so that they won't be processed anymore.
    // For example because the user cancelled the job.
    private fun deleteInputAtTagger(pid: UUID) {
        val route = "input/$pid"
        taggerRequest(this, route, HttpMethod.DELETE, Void::class.java)
    }

    companion object {
        private fun <T : Any> taggerRequest(
            job: Job, route: String, method: HttpMethod, type: Class<T>,
            requestEntity: HttpEntity<LinkedMultiValueMap<String, Any>>? = null,
        ): T? {
            // Setup request.
            val restTemplate = RestTemplate()
            val endpoint = URL("${job.taggerStore.getURL(job.name)}/$route")
            val builder = UriComponentsBuilder.fromUri(endpoint.toURI())
            // Send request.
            val responseEntity = try {
                restTemplate.exchange(
                    builder.build().encode().toUri(), method, requestEntity, // Allowed to be null
                    type
                )
            } catch (e: Exception) {
                throw Exception("Failed to connect to tagger ${job.name} with exception ${e}.")
            }
            // Handle result.
            if (responseEntity.statusCode != HttpStatus.OK) {
                throw Exception("$method file returned ${responseEntity.statusCode} with response ${responseEntity.body}")
            } else {
                return responseEntity.body
            }
        }
    }
}