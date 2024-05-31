package org.ivdnt.galahad.jobs

import com.fasterxml.jackson.annotation.JsonProperty
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.BaseFileSystemStore
import org.ivdnt.galahad.FileBackedValue
import org.ivdnt.galahad.data.layer.Layer
import org.ivdnt.galahad.data.layer.LayerPreview
import org.ivdnt.galahad.data.layer.LayerSummary
import org.ivdnt.galahad.tagset.Tagset
import java.io.File
import java.util.*

/**
 * Represents a job that processes a single document in a corpus.
 * Corresponds to a directory in jobs/[jobname]/documents/[documentname], containing:
 * - result: a json [Layer]: when not [Layer.EMPTY], [DocumentProcessingStatus.FINISHED]
 * - processingID: a plaintext [UUID]: when present, [DocumentProcessingStatus.PROCESSING]
 * - error: a plaintext error message: when present, [DocumentProcessingStatus.ERROR]
 */
class DocumentJob(
    workDirectory: File,
) : BaseFileSystemStore(workDirectory), Logging {

    private val processingID = workDirectory.resolve("processingID")

    private val error = workDirectory.resolve("error")

    private val resultStore: FileBackedValue<Layer>
        get() = FileBackedValue(workDirectory.resolve("result"), Layer.EMPTY)

    val name: String = workDirectory.name

    val getError get() = if (error.absoluteFile.exists()) error.readText() else null

    val getProcessingID: UUID? get() = if (processingID.exists()) UUID.fromString(processingID.readText()) else null

    val isProcessing: Boolean get() = processingID.exists()

    val result get() = resultStore.read<Layer>() // Note that it can be empty

    /** Determines the status based on the presence of the processing ID, error file, or result file. */
    val status: DocumentProcessingStatus
        get() {
            if (error.exists()) return DocumentProcessingStatus.ERROR
            if (processingID.exists()) return DocumentProcessingStatus.PROCESSING
            if (resultStore.read<Layer>() != Layer.EMPTY) return DocumentProcessingStatus.FINISHED
            return DocumentProcessingStatus.PENDING
        }

    /** Cancels a job by deleting the processing ID. The [status] is updated accordingly. */
    fun cancel() {
        processingID.delete()
    }

    fun delete() {
        // iffy implementation
        workDirectory.deleteRecursively()
    }

    private fun resetError() = error.delete()

    fun setProcessingID(id: UUID) {
        // Well if you are processing, we will reset any previous errors
        resetError()
        processingID.writeText(id.toString())
    }

    fun setResult(representation: Layer) {
        resultStore.modify<Layer> { representation }
        processingID.delete()
    }

    fun setError(message: String) {
        error.writeText(message)
        processingID.delete()
    }

    enum class DocumentProcessingStatus {
        PENDING,
        ERROR,
        PROCESSING,
        FINISHED
    }
}

/** A small preview of a [Layer] and some metadata. */
data class DocumentJobResult(
    @JsonProperty("preview") val preview: LayerPreview,
    @JsonProperty("name") val name: String,
    @JsonProperty("tagset") val tagset: Tagset,
    @JsonProperty("summary") val summary: LayerSummary,
)