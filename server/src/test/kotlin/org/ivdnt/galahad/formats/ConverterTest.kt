package org.ivdnt.galahad.formats

import java.io.ByteArrayOutputStream
import java.io.File
import org.ivdnt.galahad.app.User
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.documents.Document
import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.export.CorpusExport
import org.ivdnt.galahad.files.DiskValue
import org.ivdnt.galahad.layer.Layer
import org.ivdnt.galahad.layer.Layer.Companion.SOURCE_LAYER
import org.ivdnt.galahad.util.TestUtil
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach

open class ConverterTest {
    private lateinit var corpus: Corpus
    private val fileName = "karel_en_martijn"
    private val uuid = "e51560ff-81a2-4ddd-ba04-c7eb07af6d2b"

    protected open val folder: String = "shared/converter"

    /**
     * Whether to override the [Layer].id for the sake of a consistent test. For formats like tsv
     * that don't define an id at the root node.
     */
    protected open val overwriteLayerId: Boolean = false

    @BeforeEach
    fun initCorpus() {
        corpus = TestUtil.createCorpus()
    }

    fun formatToFormat(input: DocumentFormat, output: DocumentFormat) {
        val inputFile: File = TestUtil.get("formats/$folder/$fileName.${input.extension}")
        val outputFile: File = TestUtil.get("formats/$folder/$fileName.${output.extension}")
        val doc = corpus.documents.createOrThrow(inputFile)

        // at this point, a layer id has been assigned
        // but some formats don't have a layer id (i.e. file level id)
        // so we overwrite the layer id for those formats
        if (overwriteLayerId) {
            setLayerId(doc)
        }

        val corpusExport =
            CorpusExport(corpus, SOURCE_LAYER, output, User.DEFAULT_USER, false, false)
        val docExport = corpusExport.document(doc)
        val convertedText =
            ByteArrayOutputStream()
                .also {
                    docExport.convert(it)
                    it.flush()
                }
                .toString()
        val expectedText = outputFile.readText()

        assertEquals(cleanText(expectedText), cleanText(convertedText))
    }

    /**
     * Remove uuids and timestamps For example, naf had the attributes creationtime, timestamp,
     * beginTimestamp and endTimestamp. We remove the whole attribute, not just the value.
     */
    private fun cleanText(text: String): String =
        TEXT_CLEANING.fold(text) { clean, regex -> regex.replace(clean, "") }

    private fun setLayerId(doc: Document) {
        val layer = doc.layer
        val fixedLayer = Layer(layer.documents, uuid)
        DiskValue<Layer>(doc.sourceFile.resolve("../../layer.json").normalize()).write(fixedLayer)
    }

    companion object {
        val TEXT_CLEANING: Array<Regex> =
            arrayOf(
                Regex("""creationtime="[0-9]+""""),
                Regex("""timestamp="[0-9]+""""),
                Regex("""filename="[^"]+""""),
                Regex("""filetype="[^"]+""""),
                Regex("""[0-9]{4}-[0-9]{2}-[0-9]{2}""""),
            )
    }
}
