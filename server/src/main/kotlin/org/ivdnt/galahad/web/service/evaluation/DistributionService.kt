package org.ivdnt.galahad.web.service.evaluation

import java.io.File
import java.util.*
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.evaluation.JobPair
import org.ivdnt.galahad.evaluation.csv.CsvFile
import org.ivdnt.galahad.evaluation.distribution.DocumentDistribution
import org.ivdnt.galahad.evaluation.distribution.JobDistribution
import org.ivdnt.galahad.evaluation.distribution.TypeToken
import org.ivdnt.galahad.web.service.CorporaService
import org.springframework.stereotype.Service

@Service
class DistributionService(private val corpora: CorporaService) : BaseEvaluationService(corpora) {

    fun getDocumentDistribution(
        corpus: UUID,
        layer: String,
        document: String,
        annotation: Annotation,
        group: Annotation,
    ): DocumentDistribution {
        val corpusObj = corpora.readOrThrow(corpus)
        val jobEval = corpusObj.evaluation.createOrThrow(JobPair(layer))
        val docEval = jobEval.documents.createOrThrow(document)
        return docEval.getDistribution(annotation, group)
    }

    fun getLayerDistribution(
        corpus: UUID,
        layer: String,
        annotation: Annotation,
        group: Annotation,
    ): List<TypeToken> {
        val corpusObj = corpora.readOrThrow(corpus)
        val jobEval = corpusObj.evaluation.createOrThrow(JobPair(layer))
        return jobEval.getDistribution(annotation, group).typeTokens
    }

    fun createDistributionCsv(dir: File, corpus: UUID, job: String) {
        dir.mkdirs()
        val annotations = annotationsInLayer(corpus, job)
        for (annotation1 in annotations) {
            for (annotation2 in annotations) {
                val distribution = getLayerDistribution(corpus, job, annotation1, annotation2)
                val file = CsvFile(dir.resolve("distribution-$annotation1-$annotation2.csv"))
                file.append(JobDistribution.toCsv(distribution))
            }
        }
    }
}
