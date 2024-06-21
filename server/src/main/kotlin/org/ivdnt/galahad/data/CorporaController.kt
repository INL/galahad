package org.ivdnt.galahad.data

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.BaseFileSystemStore
import org.ivdnt.galahad.app.*
import org.ivdnt.galahad.data.corpus.Corpus
import org.ivdnt.galahad.data.corpus.CorpusMetadata
import org.ivdnt.galahad.data.corpus.MutableCorpusMetadata
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import java.io.File
import java.util.*

@RestController
class CorporaController(
    config: Config,
) : BaseFileSystemStore(config.getWorkingDirectory().resolve("corpora")), CRUDSet<UUID, Corpus, MutableCorpusMetadata>,
    Logging {

    @Autowired
    private val request: HttpServletRequest? = null
    @Autowired
    private val response: HttpServletResponse? = null
    private fun File.corpus(): Corpus {
        return Corpus(this)
    }

    private fun assertCorpusNameValidOrThrow(corpus: String) {
        if (Regex("^.{3,100}$").matches(corpus.trim())) {
            // name is valid
        } else {
            // name is invalid
            throw Exception("Corpus name is invalid. No newlines and length 3-100.")
        }
    }

    private val customDir = workDirectory.resolve("custom")
    private val presetsDir = workDirectory.resolve("presets")
    val assaysFile get() = workDirectory.resolve("assays.cache")

    val custom get() = customDir.listFiles()?.map { it.corpus() } ?: listOf()
    val presets get() = presetsDir.listFiles()?.map { it.corpus() } ?: listOf()
    val all: List<Corpus> get() = custom + presets
    val datasets get() = all.filter { it.metadata.expensiveGet().isDataset }
    val publics get() = all.filter { it.metadata.expensiveGet().isPublic }

    /** Get all corpora the user can see. */
    private fun getCorporaForUser(user: User): Set<Corpus> {
        // We don't want to pollute the admin's corpora list.
        return all.filter { corpus ->
            corpus.metadata.expensiveGet().hasReadAccess(user, excludeAdmin = true)
        }.toSet()
    }

    override fun readAll(): Set<Corpus> {
        val user = User.getUserFromRequestOrThrow(request)
        return getCorporaForUser(user)
    }

    /**
     * Gives unauthorized access to the corpus given a UUID,
     * therefore it should not be directly used for external access.
     */
    fun getUncheckedCorpusAccess(corpus: UUID): Corpus {
        return if (presetsDir.resolve(corpus.toString()).exists()) {
            presetsDir.resolve(corpus.toString()).corpus()
        } else {
            customDir.resolve(corpus.toString()).corpus()
        }
    }

    //We should have the return type of this method be an object or interface of only read-methods
    //Since this is currently not the case, the 'Read' or 'Write' is just a marker of intended actions
    //but nothing is enforced, could be worth the refactor
    fun getReadAccessOrNull(corpus: UUID, request: HttpServletRequest?): Corpus? {
        if (request == null) return null
        val cs = getUncheckedCorpusAccess(corpus)
        // security like a pro
        val metadata: MutableCorpusMetadata = cs.mutableCorpusMetadata
        val user: User = User.getUserFromRequestOrThrow(request)
        if (metadata.hasReadAccess(user)) return cs
        return null
    }

    fun getWriteAccessOrNull(corpus: UUID, request: HttpServletRequest?): Corpus? {
        if (request == null) return null
        val cs = getReadAccessOrNull(corpus, request)
        // security like a pro
        val metadata: MutableCorpusMetadata? = cs?.mutableCorpusMetadata
        val user: User = User.getUserFromRequestOrThrow(request)
        if (metadata?.hasWriteAccess(user) == true) return cs
        return null
    }

    fun getReadAccessOrThrow(corpus: UUID, request: HttpServletRequest?): Corpus {
        return getReadAccessOrNull(corpus, request) ?: throw Exception("Corpus not found")
    }

    fun getWriteAccessOrThrow(corpus: UUID, request: HttpServletRequest?): Corpus {
        return getWriteAccessOrNull(corpus, request) ?: throw Exception("Corpus not found")
    }

    override fun readOrNull(key: UUID) = getReadAccessOrNull(key, request)

    override fun create(value: MutableCorpusMetadata): UUID {
        assertCorpusNameValidOrThrow(value.name)
        val uuid = UUID.randomUUID()
        val corpusDir = customDir.resolve(uuid.toString())
        val corpusStore = corpusDir.corpus()
        val user = User.getUserFromRequestOrThrow(request)
        val newVal = MutableCorpusMetadata(
            owner = user.id, // The creator of the request is the owner.
            name = value.name,
            eraFrom = value.eraFrom,
            eraTo = value.eraTo,
            tagset = value.tagset,
            isPublic = value.isPublic || value.isDataset, // any dataset is public
            isDataset = value.isDataset,
            collaborators = value.collaborators,
            viewers = value.viewers,
            sourceName = value.sourceName,
            sourceURL = value.sourceURL,
        )
        corpusStore.updateMetadata(newVal, user)

        return corpusStore.metadata.expensiveGet().uuid
    }

    override fun update(key: UUID, value: MutableCorpusMetadata): Corpus? {
        assertCorpusNameValidOrThrow(value.name)
        val user = User.getUserFromRequestOrThrow(request)

        // Viewers are allowed to remove themselves, but no more than that.
        val cp = getUncheckedCorpusAccess(key)
        if (!value.isViewer(user) && cp.metadata.expensiveGet().isViewer(user)) {
            cp.removeAsViewer(user)
            return null
        }

        // Same for collaborators
        if (!value.isCollaborator(user) && cp.metadata.expensiveGet().isCollaborator(user)) {
            cp.removeAsCollaborator(user)
            // Although collaborators could change other metadata,
            // if you have chosen to remove yourself as a collaborator,
            // you probably don't want to change anything else.
            return null
        }

        val corpus = getWriteAccessOrThrow(key, request)
        if (corpus.metadata.expensiveGet().isDataset) {
            if (!value.isDataset) {
                // This corpus is no longer a dataset.
                // Invalidate assays.cache
                assaysFile.delete()
            }
        }
        corpus.updateMetadata(value, user)
        return corpus
    }

    override fun delete(key: UUID): Corpus? {
        val corpus = getReadAccessOrThrow(key, request)
        // security like a pro
        val metadata: CorpusMetadata = corpus.metadata.expensiveGet()
        val user = User.getUserFromRequestOrThrow(request)
        if (!metadata.canDelete(user)) {
            throw Exception("Unauthorized")
        }
        getWriteAccessOrThrow(key, request).delete()
        // Invalidate assays.cache
        assaysFile.delete()
        return null
    }

    @GetMapping(CORPORA_URL)
    @CrossOrigin
    fun getCorpora(): Set<CorpusMetadata> = readAll().map { it.metadata.expensiveGet() }.toSet()

    @GetMapping(DATASETS_CORPORA_URL)
    @CrossOrigin
    fun getDatasetsCorpora(): Set<CorpusMetadata> = datasets.map { it.metadata.expensiveGet() }.toSet()

    @GetMapping(PUBLIC_CORPORA_URL)
    @CrossOrigin
    fun getPublicCorpora(): Set<CorpusMetadata> = publics.map { it.metadata.expensiveGet() }.toSet()

    @GetMapping(CORPUS_URL)
    @CrossOrigin
    fun getCorpus(@PathVariable corpus: UUID): CorpusMetadata? = readOrNull(corpus)?.metadata?.expensiveGet()

    @PostMapping(value = [CORPORA_URL], consumes = [MediaType.APPLICATION_JSON_VALUE])
    @CrossOrigin
    fun postCorpus(@RequestBody value: MutableCorpusMetadata): UUID = create(value)

    /**
     * Note that this patch operation allows editing (so also removing) collaborators. Call it carefully!
     */
    @PatchMapping(CORPUS_URL)
    @CrossOrigin
    fun patchCorpus(@PathVariable corpus: UUID, @RequestBody value: MutableCorpusMetadata): CorpusMetadata? =
        update(corpus, value)?.metadata?.expensiveGet()

    @DeleteMapping(CORPUS_URL)
    @CrossOrigin
    fun deleteCorpus(@PathVariable corpus: UUID): CorpusMetadata? = delete(corpus)?.metadata?.expensiveGet()
}
