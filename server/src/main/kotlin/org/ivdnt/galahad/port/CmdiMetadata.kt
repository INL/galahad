package org.ivdnt.galahad.port

import org.ivdnt.galahad.data.corpus.CorpusMetadata
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.io.path.createTempDirectory

class CmdiMetadata(transformMetadata: DocumentTransformMetadata) : LayerTransformer(transformMetadata) {
    companion object {
        val tmp_dir: File = createTempDirectory("cmdi").toFile()
    }

    val file: File

    init {
        var template = this::class.java.classLoader.getResource("CMDI-template.xml")!!.readText()
        val corpusMetadata: CorpusMetadata = this.transformMetadata.corpus.metadata.expensiveGet()
        val docTitle = document.getUploadedRawFile().nameWithoutExtension

        // Current year, month and day, zero-padded
        val year = SimpleDateFormat("yyyy").format(Date())
        val month = SimpleDateFormat("MM").format(Date())
        val day = SimpleDateFormat("dd").format(Date())
        val date = "$year-$month-$day"

        // Retrieve GaLAHaD version from the same version.yml used in the client about page.
        val versionStream = this::class.java.classLoader.getResource("version.yml")!!.openStream()
        val versionProperties = Properties()
        versionProperties.load(versionStream)
        val galahadVersion = versionProperties.getProperty("VERSION")

        val replacements = mapOf<String, String>(
            "CORPUS_NAME" to corpusMetadata.name,
            "DATE" to date,
            "YEAR" to year,
            "MONTH" to month,
            "DAY" to day,
            "PID" to document.uuid.toString(),
            "GALAHAD_VERSION" to galahadVersion,
            "TITLE" to docTitle,
            "SOURCE_NAME" to (corpusMetadata.sourceName ?: "!No source name defined!"),
            "SOURCE_URL" to (corpusMetadata.sourceURL?.toString() ?: "!No source URL defined!"),
            "ERA_FROM" to corpusMetadata.eraFrom.toString(),
            "ERA_TO" to corpusMetadata.eraTo.toString(),
            "TAGSET" to (tagger.tagset ?: "!No tagset defined!"),
            "FORMAT" to document.format.identifier,
            "TAGGER_NAME" to tagger.id,
            "TAGGER_VERSION" to tagger.version, //TODO
            "TAGGER_URL" to tagger.model.href,
        )
        for ((key, value) in replacements) {
            template = template.replace(key, value)
        }
        file = tmp_dir.resolve("CMDI-$docTitle.xml")
        file.writeText(template)
    }
}