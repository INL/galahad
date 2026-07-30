package org.ivdnt.galahad.evaluation.distribution

import com.fasterxml.jackson.annotation.JsonValue
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.evaluation.DocumentEvaluations
import org.ivdnt.galahad.evaluation.csv.CsvFile
import org.ivdnt.galahad.evaluation.csv.CsvString
import org.ivdnt.galahad.util.merge
import org.ivdnt.galahad.util.parallelMap

/**
 * The frequency distribution of terms in a corpus for a specific tagger layer. A CorpusDistribution
 * is the sum of the [DocumentDistribution]s of all documents in the corpus.
 */
class JobDistribution(@JsonValue val typeTokens: List<TypeToken>) {
    companion object {
        fun create(
            corpus: Corpus,
            docEvals: DocumentEvaluations,
            annotation: Annotation,
            group: Annotation,
        ): JobDistribution =
            JobDistribution(
                corpus.documents
                    .readAll()
                    .parallelMap {
                        docEvals
                            .createOrThrow(it.name)
                            .getDistribution(annotation, group)
                            .typeTokens
                    }
                    .reduce { v1, v2 ->
                        (v1 + v2)
                            .groupingBy { it.annotation to it.group }
                            .reduce { _, a, b ->
                                TypeToken(
                                    annotation = a.annotation,
                                    group = a.group,
                                    tokens = a.tokens.merge(b.tokens, Integer::sum),
                                )
                            }
                            .values
                            .sortedByDescending { it.count }
                    }
            )

        fun toCsv(typeTokens: List<TypeToken>): CsvString = buildString {
            append(CsvFile.toCsvString(listOf("annotation", "group", "count", "unique", "tokens")))
            for (tt in typeTokens) {
                append(
                    CsvFile.toCsvString(
                        listOf(
                            tt.annotation,
                            tt.group,
                            tt.count,
                            tt.tokens.size,
                            tt.tokens.entries
                                .sortedByDescending { it.value }
                                .joinToString { "${it.key} (${it.value})" },
                        )
                    )
                )
            }
        }
    }
}
