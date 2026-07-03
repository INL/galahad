package org.ivdnt.galahad.evaluation.metrics

import com.fasterxml.jackson.annotation.JsonValue
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.Layer.Companion.SOURCE_LAYER
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.evaluation.CorpusEvaluation
import org.ivdnt.galahad.evaluation.JobPair
import org.ivdnt.galahad.util.parallelMap

class CorpusMetrics(@JsonValue val metrics: List<GlobalMetrics>) {
    companion object {
        fun create(
            corpus: Corpus,
            annotations: List<Annotation>,
            group: Annotation,
            analysis: Annotation.Analysis,
            evaluation: CorpusEvaluation,
        ): CorpusMetrics =
            CorpusMetrics(
                corpus.layers
                    .readAll()
                    .filter { it.name != SOURCE_LAYER }
                    .parallelMap {
                        evaluation
                            .createOrThrow(JobPair(it.name, SOURCE_LAYER))
                            .getMetrics(annotations, group, analysis)
                            .metrics
                            .toGlobal(it.name)
                    }
            )
    }
}
