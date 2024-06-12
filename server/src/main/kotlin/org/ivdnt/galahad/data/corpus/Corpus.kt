package org.ivdnt.galahad.data.corpus

import org.ivdnt.galahad.BaseFileSystemStore
import org.ivdnt.galahad.FileBackedCache
import org.ivdnt.galahad.FileBackedValue
import org.ivdnt.galahad.app.ExpensiveGettable
import org.ivdnt.galahad.app.User
import org.ivdnt.galahad.app.executeAndLogTime
import org.ivdnt.galahad.data.document.Document
import org.ivdnt.galahad.data.document.DocumentFormat
import org.ivdnt.galahad.data.document.Documents
import org.ivdnt.galahad.data.document.SOURCE_LAYER_NAME
import org.ivdnt.galahad.jobs.Jobs
import org.ivdnt.galahad.port.CmdiMetadata
import org.ivdnt.galahad.port.CorpusTransformMetadata
import org.ivdnt.galahad.taggers.Taggers
import org.ivdnt.galahad.util.createZipFile
import java.io.File
import java.io.OutputStream
import java.util.*

/**
 * A corpus is a collection of documents, metadata and jobs, saved to a folder. The folder contents are:
 *
 * - documents/: a folder containing all documents in the corpus. Represented by [Documents].
 * - jobs/: a folder containing all jobs that were active at some point in the corpus. The sourceLayer is one of them. Represented by [Jobs].
 * - metadata: a cache file storing [MutableCorpusMetadata] about the corpus.
 * - metadata.cache: a cache file storing [CorpusMetadata] about the corpus.
 *
 * A Corpus has an owner, who can add collaborators and viewers.
 * Collaborators have read and write access (and can add viewers and collaborators).
 * Viewers have read access.
 * Admins have access to all corpora with read and write access.
 */
class Corpus(
    workDirectory: File,
    user: User,
) : BaseFileSystemStore(workDirectory) {

    val documents = Documents(workDirectory.resolve("documents"))

    // Make sure this is initialized before accessing metadata
    private val fileBackedMetadata: FileBackedValue<MutableCorpusMetadata>
        get() = FileBackedValue(workDirectory.resolve("metadata"), MutableCorpusMetadata.initValue())

    /**
     * Convenient access to [MutableCorpusMetadata] without the need to get the expensive [CorpusMetadata]
     * When uploading docs, for example, all we need to know is if the user has permission.
     */
    var mutableCorpusMetadata: MutableCorpusMetadata
        get() = fileBackedMetadata.read<MutableCorpusMetadata>()
        private set(value) = fileBackedMetadata.modify<MutableCorpusMetadata> { value }

    private val metadataCache = object : FileBackedCache<CorpusMetadata>(
        file = getMetadataFile(), initValue = CorpusMetadata()
    ) {
        override fun isValid(lastModified: Long): Boolean {
            return lastModified >= fileBackedMetadata.lastModified
        }

        override fun set(): CorpusMetadata {
            return CorpusMetadata(
                // Mutable fields
                owner = mutableCorpusMetadata.owner,
                name = mutableCorpusMetadata.name,
                eraTo = mutableCorpusMetadata.eraTo,
                eraFrom = mutableCorpusMetadata.eraFrom,
                tagset = mutableCorpusMetadata.tagset,
                dataset = mutableCorpusMetadata.isDataset,
                public = mutableCorpusMetadata.isDataset, // Note that we set isPublic the same as isDataset.
                collaborators = mutableCorpusMetadata.collaborators ?: setOf(),
                viewers = mutableCorpusMetadata.viewers ?: setOf(),
                sourceName = mutableCorpusMetadata.sourceName,
                sourceURL = mutableCorpusMetadata.sourceURL,
                // Immutable/calculated fields
                uuid = UUID.fromString(workDirectory.name),
                activeJobs = jobs.readAll().filter { it.isActive }.size,
                numDocs = documents.readAll().size,
                sizeInBytes = workDirectory.walkTopDown().filter { it.isFile }.map { it.length() }.sum(), // expensive
                lastModified = System.currentTimeMillis(),
            )
        }
    }

    private fun getMetadataFile() = workDirectory.resolve("metadata.cache")

    /** Invalidate cache when new documents are uploaded or job activity changes */
    fun invalidateCache() {
        getMetadataFile().delete()
    }

    val metadata: ExpensiveGettable<CorpusMetadata> = object : ExpensiveGettable<CorpusMetadata> {
        override fun expensiveGet() = metadataCache.get<CorpusMetadata>()
    }

    val sourceTagger: ExpensiveGettable<Taggers.Summary> = object : ExpensiveGettable<Taggers.Summary> {
        override fun expensiveGet(): Taggers.Summary {
            val metadata = metadata.expensiveGet()
            return Taggers.Summary(
                id = SOURCE_LAYER_NAME,
                description = "uploaded annotations",
                tagset = metadata.tagset,
                eraFrom = metadata.eraFrom,
                eraTo = metadata.eraTo,
                produces = setOf("TODO"),
            )
        }
    }

    // Note: this is somewhat inefficient, since have to get the sourceTagger, even though we might not use it.
    val jobs get() = Jobs(workDirectory.resolve("jobs"), this)

    fun delete() {
        workDirectory.deleteRecursively()
    }

    /**
     * Overwrite the [CorpusMetadata] in [metadata] with [newMeta],
     * except for the owner, which should be grabbed from the existing [metadata].
     *
     * If a user appears multiple times in the permission hierarchy, only the upper level remains.
     */
    fun updateMetadata(newMeta: MutableCorpusMetadata, user: User): ExpensiveGettable<CorpusMetadata> {
        if (!mutableCorpusMetadata.isPublic && newMeta.isPublic) {
            // Corpus is being set to public
            if (!mutableCorpusMetadata.canMakePublic(user)) {
                throw Exception("Unauthorized")
            }
        }
        if (mutableCorpusMetadata.collaborators != newMeta.collaborators || mutableCorpusMetadata.viewers != newMeta.viewers) {
            // Collaborators have changed
            if (!mutableCorpusMetadata.canAddNewUsers(user) && mutableCorpusMetadata.owner != "") {
                throw Exception("Unauthorized")
            }
        }
        // If mutableCorpusMetadata.owner is "", we are working with the InitValue of FileBackedValue,
        // so the updateMetadata call is initializing the corpus.
        val owner = if (mutableCorpusMetadata.owner == "") user.id else mutableCorpusMetadata.owner
        // Overwrite the owner with the original, so collaborators can't change it.
        newMeta.owner = owner

        // Trim textual intputs
        newMeta.name = newMeta.name.trim()
        newMeta.sourceName = newMeta.sourceName?.trim()
        newMeta.tagset = newMeta.tagset?.trim()
        newMeta.collaborators = newMeta.collaborators?.map { it.trim() }?.toSet()
        newMeta.viewers = newMeta.viewers?.map { it.trim() }?.toSet()

        // merge isPublic and isDataset
        newMeta.isPublic = newMeta.isDataset

        // Remove owner from list of collaborators & viewers
        newMeta.collaborators = newMeta.collaborators?.filter { it != owner }?.toSet()
        newMeta.viewers = newMeta.viewers?.filter { it != owner }?.toSet()
        // Remove collaborators from list of viewers
        if (newMeta.collaborators != null) newMeta.viewers = newMeta.viewers?.filter {
            !newMeta.collaborators!!.contains(it)
        }?.toSet()

        mutableCorpusMetadata = newMeta
        return metadata
    }

    fun removeAsViewer(user: User) {
        fileBackedMetadata.modify<MutableCorpusMetadata> {
            val viewers = it.viewers?.toMutableSet()
            viewers?.removeIf { i -> i == user.id }
            it.viewers = viewers
            it
        }
    }

    fun removeAsCollaborator(user: User) {
        fileBackedMetadata.modify<MutableCorpusMetadata> {
            val collaborators = it.collaborators?.toMutableSet()
            collaborators?.removeIf { i -> i == user.id }
            it.collaborators = collaborators
            it
        }
    }

    /**
     * Maps all [Document] found in [Documents] to the desired [DocumentFormat] and zips them. [formatMapper] should perform the mapping.
     */
    fun getZipped(
        ctm: CorpusTransformMetadata,
        formatMapper: (Document) -> File,
        filter: (Document) -> Boolean,
        outputStream: OutputStream? = null,
    ): File {
        val name = metadata.expensiveGet().name
        var zipFile: File? = null
        val documents = documents.readAll().filter(filter)
        executeAndLogTime("Generating $name zip") {
            val convertedDocs = documents.asSequence().map(formatMapper)
            val docsToCmdi = documents.asSequence().map { CmdiMetadata(ctm.documentMetadata(it.name)).file }
            zipFile = createZipFile(convertedDocs + docsToCmdi, outputStream)
        }
        return zipFile!!
    }
}