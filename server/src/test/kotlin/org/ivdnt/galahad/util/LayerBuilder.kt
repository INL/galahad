package org.ivdnt.galahad.util

import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.io.path.createTempDirectory
import org.ivdnt.galahad.annotations.*
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.app.User
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.export.CorpusExport
import org.ivdnt.galahad.export.DocumentExport
import org.ivdnt.galahad.formats.tsv.TsvFile
import org.ivdnt.galahad.layer.DocumentLayer
import org.ivdnt.galahad.layer.Layer
import org.ivdnt.galahad.layer.ParagraphLayer
import org.ivdnt.galahad.layer.SentenceLayer
import org.junit.jupiter.api.Assertions.assertEquals

class LayerBuilder {

    var terms: MutableList<Term> = mutableListOf()

    fun loadDummies(
        amount: Int,
        literal: String = "dummy",
        lemma: String? = "dummy",
        pos: String? = "pos",
    ): LayerBuilder {
        for (i in 0 until amount) {
            terms +=
                Term(
                    id = "",
                    annotations =
                        mapOf(
                            Annotation.LEMMA to lemma,
                            Annotation.POS to pos,
                            Annotation.TOKEN to literal,
                        ),
                )
        }
        return this
    }

    fun loadLayerFromTSV(path: String, plaintext: String): LayerBuilder {
        val tsv = TsvFile(TestUtil.get(path))
        terms = tsv.layer.terms.toMutableList()
        return this
    }

    fun loadText(text: String): LayerBuilder {
        val words: List<String> = text.split(" ")
        for (i in words.indices) {
            terms +=
                Term(
                    id = "",
                    annotations =
                        mapOf(
                            Annotation.TOKEN to words[i],
                            Annotation.LEMMA to words[i],
                            Annotation.POS to "pos",
                        ),
                )
        }
        return this
    }

    fun build(): Layer =
        Layer(
            listOf(
                DocumentLayer(
                    "",
                    listOf(
                        ParagraphLayer(
                            "",
                            listOf(SentenceLayer("", terms, emptyMap())),
                        )
                    ),
                )
            )
        )
}

class DocTest {
    companion object {
        fun builder(corpus: Corpus): DocTestBuilder = DocTestBuilder(corpus)
    }
}

/** n for null-check */
fun <T> n(x: T?, desc: String = "PLACEHOLDER"): T =
    x ?: throw Exception("$desc is not set, please set it first before calling this operation.")

class DocTestBuilder(val corpus: Corpus) {

    private var expected: String? = null

    fun expecting(result: String): DocTestBuilder {
        this.expected = result
        return this
    }

    fun expectingFile(path: String): DocTestBuilder {
        expected = TestUtil.get(path).readText()
        return this
    }

    /** The file extension is relevant, otherwise conversion will fail */
    private fun getDummyTransformMetadata(
        layer: Layer,
        format: DocumentFormat,
        file: File? = null,
    ): DocumentExport {
        val file = file ?: createTempDirectory().toFile().resolve("dummy.${format.extension}")
        file.createNewFile()
        val doc = corpus.documents.readOrNull(file.name) ?: corpus.documents.createOrThrow(file)
        corpus.jobs.createOrThrow(TestUtil.TAGGER)
        //        job.setLayer(doc.name, layer)
        val corpEx = CorpusExport(corpus, TestUtil.TAGGER, format, User.DEFAULT_USER, false, false)
        return corpEx.document(doc)
    }

    // generic

    private fun convertToFormat(layer: Layer, format: DocumentFormat): TestResult {
        val export = getDummyTransformMetadata(layer, format)
        val text =
            ByteArrayOutputStream()
                .also {
                    export.convert(it)
                    it.flush()
                }
                .toString()
        return got(text)
    }

    private fun mergeToFormat(layer: Layer, format: DocumentFormat, file: File): TestResult {
        val export = getDummyTransformMetadata(layer, format, file)
        val text =
            ByteArrayOutputStream()
                .also {
                    export.merge(it)
                    it.flush()
                }
                .toString()
        return got(text)
    }

    // TSV

    fun convertToTSV(layer: Layer): TestResult = convertToFormat(layer, DocumentFormat.Tsv)

    fun mergeTSV(path: String, layer: Layer): TestResult = mergeTSV(TestUtil.get(path), layer)

    private fun mergeTSV(file: File, layer: Layer): TestResult =
        mergeToFormat(layer, DocumentFormat.Tsv, file)

    // Conllu

    fun convertToConllu(layer: Layer): TestResult = convertToFormat(layer, DocumentFormat.Conllu)

    fun mergeConllu(path: String, layer: Layer): TestResult = mergeConllu(TestUtil.get(path), layer)

    private fun mergeConllu(file: File, layer: Layer): TestResult =
        mergeToFormat(layer, DocumentFormat.Conllu, file)

    // NAF

    fun convertToNaf(file: File, layer: Layer): TestResult =
        convertToFormat(layer, DocumentFormat.Naf)

    // Folia

    fun convertToFolia(file: File, layer: Layer): TestResult =
        convertToFormat(layer, DocumentFormat.Folia)

    fun mergeFolia(file: File, layer: Layer): TestResult =
        mergeToFormat(layer, DocumentFormat.Folia, file)

    // TEI

    fun convertToTEI(file: File, layer: Layer): TestResult =
        convertToFormat(layer, DocumentFormat.TeiP5)

    fun mergeTEI(path: String, layer: Layer): TestResult = mergeTEI(TestUtil.get(path), layer)

    fun mergeTEI(file: File, layer: Layer): TestResult =
        mergeToFormat(layer, DocumentFormat.TeiP5, file)

    /** Specify a custom result */
    fun got(result: String): TestResult {
        return TestResult(
            expected
                ?: throw Exception(
                    "You forgot to set an expecting value, please to so before calling 'got'"
                ),
            result,
        )
    }
}

class TestResult(private var expected: String, private var actual: String) {

    fun ignoreDate(): TestResult {
        val date = Regex("\\d{4}-\\d{2}-\\d{2}")

        actual = date.replace(actual, "__DATE_IGNORED_BY_TEST__")
        expected = date.replace(expected, "__DATE_IGNORED_BY_TEST__")
        return this
    }

    fun ignoreTrailingWhiteSpaces(): TestResult {
        actual = actual.trim()
        expected = expected.trim()
        return this
    }

    fun ignoreLineEndings(): TestResult {
        unixLineEndings()
        actual = actual.replace("\n", "")
        expected = expected.replace("\n", "")
        return this
    }

    fun ignoreWhiteSpaceDocumentWide(): TestResult {
        actual = actual.replace("\\s".toRegex(), "")
        expected = expected.replace("\\s".toRegex(), "")
        return this
    }

    fun ignoreUUID(): TestResult {
        val uuid =
            Regex("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")
        actual = uuid.replace(actual, "__UUID_IGNORED_BY_TEST__")
        expected = uuid.replace(expected, "__UUID_IGNORED_BY_TEST__")
        return this
    }

    private fun unixLineEndings(): TestResult {
        actual = actual.replace("\r\n", "\n")
        expected = expected.replace("\r\n", "\n")
        return this
    }

    fun result() {
        unixLineEndings()
        assertEquals(expected, actual)
    }
}
