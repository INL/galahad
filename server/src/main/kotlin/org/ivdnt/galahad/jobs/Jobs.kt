package org.ivdnt.galahad.jobs

import org.ivdnt.galahad.BaseFileSystemStore
import org.ivdnt.galahad.app.NamedCRUDSet
import org.ivdnt.galahad.data.corpus.Corpus
import org.ivdnt.galahad.data.layer.LayerPreview
import org.ivdnt.galahad.data.layer.LayerSummary
import org.ivdnt.galahad.taggers.Taggers
import java.io.File

class Jobs(
    workDirectory: File,
    private val corpus: Corpus,
) : BaseFileSystemStore(workDirectory), NamedCRUDSet<String, Job, String> {

    private val taggers = Taggers()

    // better be verbose than sorry
    fun readAllJobStatesIncludingPotentialJobs(): Set<State> {
        val existingJobs = readAll().map { it.state }
        val potentialJobs = taggers.summaries.map { it.expensiveGet() }.map {
                State(
                    it, Progress(pending = corpus.documents.readAll().size), LayerPreview.EMPTY, LayerSummary(), 0
                )
            }
        val sourceJobs = setOf(
            State(
                tagger = corpus.sourceTagger.expensiveGet()
            )
        )
        // the latter overrides the former’s value
        val jobMap = HashMap<String, State>()
        potentialJobs.forEach { jobMap[it.tagger.id] = it }
        sourceJobs.forEach { jobMap[it.tagger.id] = it }
        // Existing jobs take precedence above all, so they are put last.
        existingJobs.forEach { jobMap[it.tagger.id] = it }
        return jobMap.values.toSet()
    }

    override fun readAll(): Set<Job> =
        workDirectory.list()?.map { readOrThrow(it) }?.toSet() ?: throw Exception("Could not list jobs")

    override fun createOrNull(key: String): Job? {
        // accessing the job once creates it and it's directories
        Job(workDirectory.resolve(key), corpus)
        return readOrNull(key)
    }

    override fun readOrNull(key: String): Job? {
        if (key.isBlank()) throw Exception("Blank job name not allowed") // An empty job name can not be resolved
        return if (workDirectory.resolve(key).exists()) Job(workDirectory.resolve(key), corpus) else null
    }

    override fun update(key: String, value: String): Job? {
        TODO("Not yet implemented")
    }

    override fun delete(key: String): Job? {
        workDirectory.resolve(key).deleteRecursively()
        return readOrNull(key)
    }
}