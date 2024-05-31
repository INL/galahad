package org.ivdnt.galahad.port

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import org.ivdnt.galahad.app.User
import org.ivdnt.galahad.data.corpus.Corpus
import org.ivdnt.galahad.data.corpus.MutableCorpusMetadata
import org.ivdnt.galahad.data.document.Document
import org.ivdnt.galahad.data.layer.Layer
import org.ivdnt.galahad.data.layer.Term
import org.ivdnt.galahad.data.layer.WordForm
import org.ivdnt.galahad.port.conllu.ConlluFile
import org.ivdnt.galahad.port.conllu.export.LayerToConlluConverter
import org.ivdnt.galahad.port.folia.FoliaFile
import org.ivdnt.galahad.port.folia.export.LayerToFoliaConverter
import org.ivdnt.galahad.port.naf.export.LayerToNAFConverter
import org.ivdnt.galahad.port.tei.TEIFile
import org.ivdnt.galahad.port.tei.export.LayerToTEIConverter
import org.ivdnt.galahad.port.tsv.TSVFile
import org.ivdnt.galahad.port.tsv.export.LayerToTSVConverter
import org.ivdnt.galahad.tagset.Tagset
import org.junit.jupiter.api.Assertions.assertEquals
import java.io.File
import java.net.URL
import java.util.*
import kotlin.io.path.createTempDirectory

object Resource {
    var corpus: Corpus = createCorpus()

    fun get(path: String): File {
        return File(
            this::class.java.classLoader.getResource(path)!!.toURI()
        )
    }

    fun getDoc(path: String): Document {
        val file = get(path)
        val name = corpus.documents.create(file)
        return corpus.documents.readOrThrow(name)
    }
}

fun getJsonMapper(): ObjectMapper {
    return JsonMapper.builder().configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
        .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true).build()
}

fun createCorpus(workdir: File? = null, isDataset: Boolean = false, isAdmin: Boolean = false): Corpus {
    val parent = workdir ?: createTempDirectory().toFile()
    val corpus = Corpus(parent.resolve(UUID.randomUUID().toString()), User("you"))
    corpus.updateMetadata(
        MutableCorpusMetadata(
            "you",
            "testCorpus",
            0,
            0,
            "tagset",
            isDataset,
            isDataset,
            setOf("collaborator1", "collaborator2"),
            setOf(),
            "source name",
            URL("http://source.url")
        ), User("testUser", isAdmin)
    )
    return corpus
}

fun assertPlainText(folder: String, file: InternalFile) {
    // Plain text
    if (file is PlainTextableFile) {
        val plaintext = Resource.get("$folder/plaintext.txt").readText()
        assertEquals(plaintext, file.plainTextReader().readText())
    } else throw Exception("File does not support plain text")
}

fun assertPlaintextAndSourcelayer(folder: String, file: InternalFile) {
    // Plain text
    assertPlainText(folder, file)
    // Source layer
    if (file is SourceLayerableFile) {
        val jsonExpected = Resource.get("$folder/sourcelayer.json").readText()
        val mapper = getJsonMapper()
        val json = mapper.writeValueAsString(file.sourceLayer())
        assertEquals(jsonExpected, json)
    } else throw Exception("File does not support source layer")
}

class LayerBuilder {

    var layer: Layer = Layer(name = "placeholder")

    fun assertWordFromsAndTermsSize(wfs: Int, terms: Int): LayerBuilder {
        assertEquals(wfs, n(layer, "layer").wordForms.size)
        assertEquals(terms, n(layer, "layer").terms.size)
        return this
    }

    fun loadDummies(
        amount: Int, literal: String = "dummy", lemma: String? = "dummy", pos: String? = "pos",
    ): LayerBuilder {
        val baseOffset = layer.terms.lastOrNull()?.targets?.lastOrNull()?.endOffset ?: 0
        if (layer.name != "dummyLayer") layer = Layer(name = "dummyLayer")

        for (i in 0 until amount) {
            val wf = WordForm(
                literal = literal,
                id = "dummy${i}",
                offset = baseOffset + i * literal.length, // of course this is only correct if this is the only function we use to set wordforms
                length = literal.length
            )
            val term = Term(
                lemma = lemma, pos = pos, targets = mutableListOf(wf)
            )
            layer.wordForms.add(wf)
            layer.terms.add(term)
        }
        return this
    }

    fun loadLayerFromTSV(path: String, plaintext: String): LayerBuilder {
        val tsv = TSVFile(Resource.get(path))
        layer = tsv.mapOnPlainText(plaintext, "path")
        return this
    }

    fun loadText(text: String): LayerBuilder {
        val words: List<String> = text.split(" ")
        var offset = layer.terms.lastOrNull()?.targets?.lastOrNull()?.endOffset?.let { it } ?: 0
        for (i in words.indices) {
            val wf = WordForm(
                literal = words[i], id = "w${i}", offset = offset, length = words[i].length
            )
            val term = Term(
                lemma = words[i], pos = offset.toString(), targets = mutableListOf(wf)
            )
            layer.wordForms.add(wf)
            layer.terms.add(term)
            offset += wf.length + 1 // + space
        }
        return this
    }

    fun setTagset(tagset: Tagset): LayerBuilder {
        layer = Layer(layer.name, tagset, layer.wordForms, layer.terms)
        return this
    }

    fun build(): Layer {
        return layer
    }
}

class DocTest {
    companion object {
        fun builder(corpus: Corpus): DocTestBuilder {
            return DocTestBuilder(corpus)
        }
    }
}

/**
 * n for null-check
 */
fun <T> n(x: T?, desc: String = "PLACEHOLDER"): T {
    return x ?: throw Exception("$desc is not set, please set it first before calling this operation.")
}

class DocTestBuilder(
    val corpus: Corpus,
) {

    var expected: String? = null

    fun expecting(result: String): DocTestBuilder {
        this.expected = result
        return this
    }

    fun expectingFile(path: String): DocTestBuilder {
        expected = Resource.get(path).readText()
        return this
    }

    /** The file extension is relevant, otherwise conversion will fail */
    fun getDummyTransformMetadata(
        layer: Layer,
        ext: String? = null,
        file: File? = null,
    ): DocumentTransformMetadata {
        val file = file ?: createTempDirectory().toFile().resolve("dummy.$ext")
        file.createNewFile()
        val docName = corpus.documents.create(file)
        val job = corpus.jobs.createOrThrow("pie-tdn")
        job.document(docName).setResult(layer)
        return DocumentTransformMetadata(
            corpus, job, corpus.documents.readOrThrow(docName), User("testUser")
        )
    }

    // TSV

    fun convertToTSV(layer: Layer): TestResult {
        val exporter = LayerToTSVConverter(
            getDummyTransformMetadata(layer, "tsv")
        )
        val result = exporter.convertToFileNamed("test")
        return got(result.readText())
    }

    fun mergeTSV(path: String, layer: Layer): TestResult {
        return mergeTSV(Resource.get(path), layer)
    }

    fun mergeTSV(file: File, layer: Layer): TestResult {
        val transformMetadata = getDummyTransformMetadata(layer, file = file)
        val result: TSVFile = TSVFile(file).merge(transformMetadata)
        return got(result.file.readText())
    }

    // Conllu

    fun convertToConllu(layer: Layer): TestResult {
        val exporter = LayerToConlluConverter(
            getDummyTransformMetadata(layer, "conllu")
        )
        val result = exporter.convertToFileNamed("test")
        return got(result.readText())
    }

    fun mergeConllu(path: String, layer: Layer): TestResult {
        return mergeConllu(Resource.get(path), layer)
    }

    fun mergeConllu(file: File, layer: Layer): TestResult {
        val transformMetadata = getDummyTransformMetadata(layer, file = file)
        val result: ConlluFile = ConlluFile(file).merge(transformMetadata)
        return got(result.file.readText())
    }

    // NAF

    fun convertToNaf(file: File, layer: Layer): TestResult {
        val exporter = LayerToNAFConverter(
            getDummyTransformMetadata(layer, file = file)
        )
        val result = exporter.convertToFileNamed("test")
        return got(result.readText())
    }

    // Folia

    fun convertToFolia(file: File, layer: Layer): TestResult {
        val exporter = LayerToFoliaConverter(
            getDummyTransformMetadata(layer, file = file)
        )
        val result = exporter.convertToFileNamed("test")
        return got(result.readText())
    }

    fun mergeFolia(file: File, layer: Layer): TestResult {
        val transformMetadata = getDummyTransformMetadata(layer, file = file)
        val result: FoliaFile = FoliaFile(file).merge(transformMetadata)
        return got(result.file.readText())
    }

    // TEI

    fun convertToTEI(teiFile: File, layer: Layer): TestResult {
        val docName = corpus.documents.create(teiFile)
        val job = corpus.jobs.createOrThrow("pie-tdn")
        job.document(docName).setResult(layer)
        val exporter = LayerToTEIConverter(
            DocumentTransformMetadata(
                corpus, job, corpus.documents.readOrThrow(docName), User("testUser")
            )
        )

        val result = exporter.convertToFileNamed("tst")
        return got(result.readText())
    }

    fun mergeTEI(path: String, layer: Layer): TestResult {
        return mergeTEI(Resource.get(path), layer)
    }

    fun mergeTEI(file: File, layer: Layer): TestResult {
        val transformMetadata = getDummyTransformMetadata(layer, file = file)
        val result: TEIFile = TEIFile(file).merge(transformMetadata)
        return got(result.file.readText())
//        val docName = corpus.documents.create(file)
//        val job = corpus.jobs.createOrThrow("pie-tdn")
//        job.document( docName ).setResult( layer )
//        val exporter = TEILayerMerger( TEIFile(file, DocumentFormat.TeiP5), CorpusTransformMetadata.DocumentTransformMetadata(corpus, job, corpus.documents.readOrThrow( docName ),
//            User("testUser")
//        ))
//
//        return got( exporter.parser.xmlToString(false) )
    }

    /**
     * Specify a custom result
     */
    fun got(result: String): TestResult {
        return TestResult(
            expected ?: throw Exception("You forgot to set an expecting value, please to so before calling 'got'"),
            result
        )
    }
}

class TestResult(
    var expected: String,
    var actual: String,
) {

    fun ignoreDate(): TestResult {
        val date = Regex("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")

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
        val uuid = Regex("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")
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

@Deprecated("Use TestResult instead")
class Util {

    companion object {
        @Deprecated("Use TestResult instead")
        fun ignoreDateAndUUID(string: String): String {
            // The date is set for each export, and therefore breaks the test
            // so we remove it
            // same for corpus uuid
            val uuid = Regex("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")
            val date = Regex("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")

            return date.replace(uuid.replace(string, "__UUID_IGNORED_BY_TEST__"), "__DATE_IGNORED_BY_TEST__")
        }
    }
}