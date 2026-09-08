package org.ivdnt.galahad.web.service.evaluation

import jakarta.servlet.http.HttpServletResponse
import java.io.File
import java.util.UUID
import kotlin.io.path.createTempDirectory
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.corpora.CorpusStatistics
import org.ivdnt.galahad.evaluation.csv.CsvFile
import org.ivdnt.galahad.layer.Layer
import org.ivdnt.galahad.taggers.Tagger
import org.ivdnt.galahad.util.EvaluationMetadataRepresenter
import org.ivdnt.galahad.util.setContentDisposition
import org.ivdnt.galahad.util.toValidFileName
import org.ivdnt.galahad.util.zipDir
import org.ivdnt.galahad.web.service.CorporaService
import org.springframework.beans.factory.annotation.Autowired
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.nodes.Tag

open class BaseEvaluationService(private val corpora: CorporaService) {
    @Autowired private val response: HttpServletResponse? = null

    fun setZipResponseHeader(corpus: UUID) {
        response!!.contentType = "application/zip"
        val corpusName = getCorpusName(corpus)
        response.setContentDisposition("$corpusName-evaluation.zip")
    }

    fun samplesToZip(
        corpus: UUID,
        job: String,
        reference: String?,
        csvBody: String,
        fileName: String,
    ): ByteArray {
        // Create csv file.
        val dir: File = createTempDirectory().toFile()
        val validFileName = fileName.toValidFileName()
        val file = CsvFile(dir.resolve(validFileName))
        file.append(csvBody)
        // Write metadata & create zip
        writeMetadataToDir(corpus, job, reference ?: Layer.SOURCE_LAYER, dir)
        // zip the directory
        return zipDir(dir).readBytes()
    }

    // Create a YAML file metadata.yml with the corpus metadata
    // and the tagger metadata of both layers.
    fun writeMetadataToDir(
        corpus: UUID,
        job: String,
        reference: String,
        dir: File,
    ) {
        val corpusObj = corpora.readOrThrow(corpus)
        val metadata =
            linkedMapOf(
                "corpus" to corpusObj.statistics,
                "hypothesis" to corpusObj.layers.readOrThrow(job).metadata.tagger,
                "reference" to corpusObj.layers.readOrThrow(reference).metadata.tagger,
            )
        val yamlOptions =
            DumperOptions().apply {
                defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
                isPrettyFlow = true
                indent = 4
            }
        val metadataFile = dir.resolve("metadata.yml")
        metadataFile.writeText(
            Yaml(
                    EvaluationMetadataRepresenter(yamlOptions).also {
                        it.addClassTag(CorpusStatistics::class.java, Tag.MAP)
                        it.addClassTag(Tagger::class.java, Tag.MAP)
                    },
                    yamlOptions,
                )
                .dumpAs(metadata, Tag.MAP, null)
        )
    }

    // TODO duplicate code with export service
    fun getCorpusName(corpus: UUID): String = corpora.readOrThrow(corpus).metadata.name

    public fun annotationsInLayer(corpus: UUID, layer: String): Set<Annotation> =
        corpora.readOrThrow(corpus).layers.readOrThrow(layer).metadata.annotations.keys
}
