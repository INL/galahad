package org.ivdnt.galahad.evaluation

import java.io.File
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.evaluation.confusion.JobConfusion
import org.ivdnt.galahad.evaluation.distribution.JobDistribution
import org.ivdnt.galahad.evaluation.entities.JobEntities
import org.ivdnt.galahad.evaluation.metrics.JobMetrics
import org.ivdnt.galahad.evaluation.metrics.Metrics
import org.ivdnt.galahad.files.GalahadFolder
import org.ivdnt.galahad.files.ValidatedDiskValue
import org.ivdnt.galahad.layers.CorpusLayer

/**
 * Defines evaluations at the level of a job pair, i.e. where all documents of a hypothesis is
 * compared to a reference.
 */
class JobEvaluation(dir: File, private val corpus: Corpus, private val jobs: JobPair) :
    GalahadFolder(dir) {
    val refJob: CorpusLayer
        get() = corpus.layers.readOrThrow(jobs.reference)

    val hypJob: CorpusLayer
        get() = corpus.layers.readOrThrow(jobs.hypothesis)

    /** Access evaluations of individual documents in this job pair. */
    val documents: DocumentEvaluations =
        DocumentEvaluations(dir.resolve(DOCUMENTS_FOLDER), corpus, jobs)

    val entities: JobEntities
        get() =
            object : ValidatedDiskValue<JobEntities>(dir.resolve(ENTITIES_FILE)) {
                    override fun isValid(modified: Long) =
                        modified >= maxOf(refJob.modified, hypJob.modified)

                    override fun set(): JobEntities = JobEntities.create(corpus, documents)
                }
                .readOrCreate()

    fun getDistribution(annotation: Annotation, group: Annotation): JobDistribution =
        object :
                ValidatedDiskValue<JobDistribution>(
                    dir.resolve("distribution-$annotation-$group.json")
                ) {
                override fun isValid(modified: Long) =
                    modified >= maxOf(refJob.modified, hypJob.modified)

                override fun set(): JobDistribution =
                    JobDistribution.create(corpus, documents, annotation, group)
            }
            .readOrCreate()

    fun getConfusion(annotation: Annotation): JobConfusion =
        object : ValidatedDiskValue<JobConfusion>(dir.resolve("confusion-$annotation.json")) {
                override fun isValid(modified: Long) =
                    modified >= maxOf(refJob.modified, hypJob.modified)

                override fun set(): JobConfusion =
                    JobConfusion.create(corpus, documents, annotation)
            }
            .readOrCreate()

    fun getMetrics(annotations: List<Annotation>, group: Annotation): JobMetrics =
        object :
                ValidatedDiskValue<JobMetrics>(
                    dir.resolve("${Metrics.Settings(annotations,group).name}.json")
                ) {
                override fun isValid(modified: Long) =
                    modified >= maxOf(refJob.modified, hypJob.modified)

                override fun set(): JobMetrics =
                    JobMetrics.create(corpus, documents, annotations, group)
            }
            .readOrCreate()

    companion object {
        private const val ENTITIES_FILE = "entities.json"
        private const val DOCUMENTS_FOLDER = "documents"
    }
}
