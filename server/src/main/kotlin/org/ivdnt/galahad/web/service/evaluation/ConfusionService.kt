package org.ivdnt.galahad.web.service.evaluation

import java.io.File
import java.util.*
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.evaluation.JobPair
import org.ivdnt.galahad.evaluation.comparison.ConfusionLayerFilter
import org.ivdnt.galahad.evaluation.comparison.HeadGroupTermFilter
import org.ivdnt.galahad.evaluation.confusion.JobConfusion
import org.ivdnt.galahad.evaluation.csv.CsvFile
import org.ivdnt.galahad.evaluation.csv.CsvSampleExporter.Companion.samplesToCSV
import org.ivdnt.galahad.web.service.CorporaService
import org.springframework.stereotype.Service

@Service
class ConfusionService(private val corpora: CorporaService) : BaseEvaluationService(corpora) {

    fun getLayerConfusion(
        corpus: UUID,
        hypothesis: String,
        reference: String,
        annotation: Annotation,
    ): JobConfusion {
        val corpusObj = corpora.readOrThrow(corpus)
        val jobEval = corpusObj.evaluation.createOrThrow(JobPair(hypothesis, reference))
        return jobEval.getConfusion(annotation)
    }

    fun getConfusionSamples(
        hypFilter: String,
        refFilter: String,
        annotation: Annotation,
        corpus: UUID,
        layer: String,
        reference: String,
    ): ByteArray {
        // Ensure the job has the required annotation types.
        //        if (annotation !in annotationTypesForTagger(job, corpus)) {
        //            throw AnnotationNotSupported(job, annotation)
        //        }
        val filter =
            ConfusionLayerFilter(
                HeadGroupTermFilter(annotation, hypFilter),
                HeadGroupTermFilter(annotation, refFilter),
            )
        val corpusObj = corpora.readOrThrow(corpus)
        val jobEval = corpusObj.evaluation.createOrThrow(JobPair(layer, reference, filter = filter))
        val fileName = "confusion-$refFilter-$hypFilter.csv"
        val confusion = JobConfusion.create(corpusObj, jobEval.documents, annotation).confusion
        val csv =
            samplesToCSV(
                confusion.values.first().values.first().samples,
                jobEval.hypJob,
                jobEval.refJob,
            )
        return samplesToZip(corpus, layer, reference, csv, fileName)
    }

    fun createConfusionCsv(dir: File, corpus: UUID, hypothesis: String, reference: String) {
        dir.mkdirs()

        val hypAnnotations = annotationsInLayer(corpus, hypothesis)
        val refAnnotations = annotationsInLayer(corpus, reference)
        val supported = setOf(Annotation.POS, Annotation.UPOS, Annotation.DEPREL)
        val common = hypAnnotations.intersect(refAnnotations).intersect(supported)

        for (annotation in common) {
            val confusion = getLayerConfusion(corpus, hypothesis, reference, annotation)
            val file = CsvFile(dir.resolve("confusion-$annotation.csv"))
            file.append(JobConfusion.toCsv(confusion.confusion))
        }
    }
}
