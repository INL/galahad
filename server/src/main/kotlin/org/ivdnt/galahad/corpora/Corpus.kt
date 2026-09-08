package org.ivdnt.galahad.corpora

import java.io.File
import java.util.*
import org.ivdnt.galahad.documents.Documents
import org.ivdnt.galahad.evaluation.CorpusEvaluation
import org.ivdnt.galahad.files.DiskValue
import org.ivdnt.galahad.files.GalahadFolder
import org.ivdnt.galahad.files.ValidatedDiskValue
import org.ivdnt.galahad.jobs.Jobs
import org.ivdnt.galahad.layer.Layer.Companion.SOURCE_LAYER
import org.ivdnt.galahad.layers.CorpusLayer.Companion.DOCUMENTS_FOLDER
import org.ivdnt.galahad.layers.CorpusLayers

/**
 * A corpus is a collection of documents, metadata and jobs, saved to a folder. The folder contents
 * are:
 *
 * - documents/: a folder containing all documents in the corpus. Represented by [Documents].
 * - jobs/: a folder containing all jobs that were active at some point in the corpus. The
 *   sourceLayer is one of them. Represented by [Jobs].
 * - metadata: a cache file storing [CorpusMetadata] about the corpus.
 * - metadata.cache: a cache file storing [CorpusStatistics] about the corpus.
 *
 * A Corpus has an owner, who can add collaborators and viewers. Collaborators have read and write
 * access. Viewers have read access. Admins have access to all corpora with read and write access.
 */
class Corpus(dir: File) : GalahadFolder(dir) {
    /** Folder name of the corpus. */
    val uuid: UUID = UUID.fromString(dir.name)
    // TODO still used in quite a lot of evaluations when they should use layers
    /** Shortcut to the source layer documents. */
    val documents: Documents =
        Documents(dir.resolve(LAYERS_FOLDER).resolve(SOURCE_LAYER).resolve(DOCUMENTS_FOLDER))

    /** Layers of this corpus. */
    val layers: CorpusLayers = CorpusLayers(dir.resolve(LAYERS_FOLDER), this)

    /** Jobs for layers of this corpus. */
    val jobs: Jobs = Jobs(dir.resolve(JOBS_FOLDER), this)

    /** Evaluations between layers of this corpus. */
    val evaluation: CorpusEvaluation = CorpusEvaluation(dir.resolve(EVALUATIONS_FOLDER), this)

    /** Editable corpus metadata. */
    var metadata: CorpusMetadata
        get() = DiskValue<CorpusMetadata>(dir.resolve(METADATA_FILE)).readOrThrow()
        set(value) {
            DiskValue<CorpusMetadata>(dir.resolve(METADATA_FILE)).write(value)
        }

    /** Computed corpus statistics. Cached and validated. */
    val statistics: CorpusStatistics
        get() =
            object : ValidatedDiskValue<CorpusStatistics>(dir.resolve(STATISTICS_FILE)) {
                    // statistics is invalid if any of the data it depends on changes, including:
                    override fun isValid(modified: Long) =
                        modified >=
                            maxOf(
                                this@Corpus.layers
                                    .readOrThrow(SOURCE_LAYER)
                                    .metadata
                                    .modified, // documents changed
                                this@Corpus.jobs.modified, // jobs changed
                                this@Corpus.modified, // modifications to metadata.json
                            )

                    override fun set(): CorpusStatistics = CorpusStatistics.create(this@Corpus)
                }
                .readOrCreate()

    companion object {
        /** Metadata path. */
        private const val METADATA_FILE = "metadata.json"
        /** Statistics path. */
        private const val STATISTICS_FILE = "statistics.json"
        /** Jobs data directory. */
        private const val JOBS_FOLDER = "jobs"
        /** Layers data directory. */
        private const val LAYERS_FOLDER = "layers"
        /** Evaluations data directory. */
        private const val EVALUATIONS_FOLDER = "evaluations"

        /** Create a new corpus folder from metadata. */
        fun create(dir: File, metadata: CorpusMetadata): Corpus {
            // clean, trim, validate, and set owner; might throw
            val cleanMetadata = CorpusMetadata.clean(metadata)
            // only create the corpus folder after the potential throw to avoid empty folder
            // and write metadata to disk
            return Corpus(dir).apply { this.metadata = cleanMetadata }
        }
    }
}
