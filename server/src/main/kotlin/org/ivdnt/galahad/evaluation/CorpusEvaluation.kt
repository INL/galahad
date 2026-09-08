package org.ivdnt.galahad.evaluation

import java.io.File
import org.ivdnt.galahad.annotations.Analysis
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.evaluation.entities.CorpusEntities
import org.ivdnt.galahad.evaluation.metrics.CorpusMetrics
import org.ivdnt.galahad.evaluation.metrics.Metrics
import org.ivdnt.galahad.exceptions.JobNotFoundException
import org.ivdnt.galahad.files.GalahadFolderManager
import org.ivdnt.galahad.files.ValidatedDiskValue

/**
 * Defines evaluations at the level of a corpus, i.e. where all jobs (and their documents) are
 * accumulated in some manner.
 */
class CorpusEvaluation(dir: File, private val corpus: Corpus) :
    GalahadFolderManager<JobEvaluation, JobPair>(dir) {
    override fun createOrThrow(key: JobPair): JobEvaluation =
        JobEvaluation(dir.resolve(key.toString()), corpus, key)

    private fun ctor(key: JobPair): JobEvaluation =
        JobEvaluation(dir.resolve(key.toString()), corpus, key)

    override fun ctor(key: String): JobEvaluation = ctor(JobPair.fromString(key))

    override fun throwNotFound(key: String): Nothing = throw JobNotFoundException(key)

    fun getMetrics(
        annotations: List<Annotation>,
        group: Annotation,
        analysis: Analysis,
    ): CorpusMetrics =
        object :
                ValidatedDiskValue<CorpusMetrics>(
                    dir.resolve("${Metrics.Settings(annotations,group,analysis).name}.json")
                ) {
                override fun isValid(modified: Long) = modified >= corpus.modified

                override fun set(): CorpusMetrics =
                    CorpusMetrics.create(
                        corpus,
                        annotations,
                        group,
                        analysis,
                        this@CorpusEvaluation,
                    )
            }
            .readOrCreate()

    val entities: CorpusEntities
        get() =
            object : ValidatedDiskValue<CorpusEntities>(dir.resolve(ENTITIES_FILE)) {
                    override fun isValid(modified: Long) = modified >= corpus.modified

                    override fun set(): CorpusEntities =
                        CorpusEntities.create(corpus, this@CorpusEvaluation)
                }
                .readOrCreate()

    companion object {
        private const val ENTITIES_FILE = "entities.json"
    }
}
