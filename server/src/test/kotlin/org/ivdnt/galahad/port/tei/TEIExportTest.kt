package org.ivdnt.galahad.port.tei

import org.ivdnt.galahad.app.User
import org.ivdnt.galahad.app.executeAndLogTime
import org.ivdnt.galahad.data.corpus.Corpus
import org.ivdnt.galahad.data.document.DocumentFormat
import org.ivdnt.galahad.port.*
import org.ivdnt.galahad.tagset.TagsetStore
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

internal class TEIExportTest {

    lateinit var corpus: Corpus

    @BeforeEach
    fun initCorpus() {
        corpus = createCorpus()
    }

    @Test
    fun `Merge pie-tdn result with heavily twined tei`() {
        val plaintext: String = Resource.get("tei/twine/plaintext.txt").readText()
        val layer = LayerBuilder()
            .loadLayerFromTSV("tei/twine/pie-tdn.tsv", plaintext)
            .build()
        DocTest.builder(corpus)
            .expectingFile("tei/twine/merged-output.xml")
            .mergeTEI(Resource.get("tei/twine/twine.input.xml"), layer)
            .ignoreTrailingWhiteSpaces()
            .ignoreDate()
            .ignoreUUID()
            .result()
    }

    @Test
    fun `Merge a pie-tdn layer with a tei file that only contains plaintext`() {
        val plaintext: String = Resource.get("tei/brieven/plaintext.txt").readText()
        val layer = LayerBuilder()
            .loadLayerFromTSV("tei/brieven/pie.tsv", plaintext)
            .build()
        DocTest.builder(corpus)
            .expectingFile("tei/brieven/merged-output.tei.xml")
            .mergeTEI(Resource.get("tei/brieven/input.tei.xml"), layer)
            .ignoreDate()
            .ignoreUUID()
            .result()
    }

    @Test
    fun punctuationExportTest() {

        val teiFile = TEIFile(Resource.get("tei/oneparagraph/mocktei.xml"))
        DocTest.builder(corpus)
            .expecting("Dit is wat oefentekst.")
            .got(teiFile.plainTextReader().readText())
            .ignoreTrailingWhiteSpaces()
            .result()

        val tagset = TagsetStore().getOrNull("TDN-Core")!!

        val layer = LayerBuilder()
            .loadLayerFromTSV( "tei/export/mock-TDN-with-punctuation.tsv", teiFile.plainTextReader().readText() )
            .assertWordFromsAndTermsSize( 5, 5 )
            .setTagset( tagset )
            .build()

        DocTest.builder(corpus)
            .expectingFile("tei/export/mock-TDN-with-punctuation-result.xml")
            .convertToTEI(teiFile.file, layer)
            .ignoreDate()
            .ignoreUUID()
            // When using just .ignoreWhiteSpace() the test fails, even though comparison tools shows no difference
            .ignoreWhiteSpaceDocumentWide()
            .result()
    }

    @Test
    fun mergePuncutationTest() {

        val tagset = TagsetStore().getOrNull("TDN-Core")!!
        val plaintext = TEIFile(Resource.get("tei/dummies/punctutation-mixed-tags.xml")).plainTextReader().readText()
        val layer = LayerBuilder()
            .loadLayerFromTSV("tei/dummies/punctuation-mixed-tags-sample-layer.tsv", plaintext)
            .setTagset(tagset)
            .build()

        DocTest.builder(corpus)
            .expectingFile("tei/export/punctuation-mixed-tags-merge-export-result.xml")
            .mergeTEI("tei/dummies/punctutation-mixed-tags.xml", layer)
            .ignoreDate()
            .ignoreUUID()
            .result()
    }

    fun displayMemory() {
        val runtime = Runtime.getRuntime()
        val usedMemInMB = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L
        val maxHeapSizeInMB = runtime.maxMemory() / 1048576L
        val availHeapSizeInMB = maxHeapSizeInMB - usedMemInMB

        println("Used memory: $usedMemInMB MB")
        println("Max heap size: $maxHeapSizeInMB MB")
        println("Available heap size: $availHeapSizeInMB MB")
    }

    // @Disabled("This test is just to test large layer, adjust the numbers as you see fit")
    /*
    Remember to increase heapsize in build.gradle.kts when running a large version of this test
    We do not increase it by default, because the remote build server can not handle it.
     */
    @Test
    fun bigLayerConvertTest() {
        val tagset = TagsetStore().getOrNull("TDN-Core")!!
        val jobName = "pie-tdn"
        val testsize = 2 // Kdummies

        println("Starting test with testsize: $testsize Kdummies. Feel free to adjust the testsize in the test.")

        displayMemory()

        // build a document with 2 million dummy words
        // create temp file
        val tempFile = File.createTempFile("bigLayerTest", ".txt")
        // add 2 million dummy words, do it smart to avoid out of memory
        val oneKDummies = "dummy ".repeat(1000)
        for (i in 1..testsize) {
            tempFile.appendText(oneKDummies)
        }
        val docName = corpus.documents.create(tempFile)

        println("Created temp file. Temp file size: ${tempFile.length()} bytes")

        // build a layer that is a valid annotation of the temp file
        val layer = LayerBuilder()
            .loadDummies( testsize * 1000 )
            .setTagset( tagset )
            .build()
        corpus.jobs.readOrCreateOrNull(jobName)?.document(docName)?.setResult(layer) ?: throw Exception("Could not set layer result")

        println("Created layer. Layer size: ${layer.wordForms.size} wordforms")

        displayMemory()

        // convert the layer to TEI to test conversion
        // remember the output to reuse as TEI-file in the merge test
        val teiConvertedFile = executeAndLogTime("convert large TEI file") {
            corpus.documents.readOrThrow(docName).generateAs(
                DocumentFormat.TeiP5, DocumentTransformMetadata(
                    corpus = corpus,
                    job = corpus.jobs.readOrThrow(jobName),
                    document = corpus.documents.readOrThrow(docName),
                    user = User("test-user")
                )
        ) }

        displayMemory()
        println("Created teiFile. teiFile size: ${teiConvertedFile.length()} bytes")

        val teiUploadedFileName = corpus.documents.create(teiConvertedFile)
        corpus.jobs.readOrCreateOrNull(jobName)?.document(teiUploadedFileName)?.setResult(layer)
            ?: throw Exception("Could not set layer result")

        // merge the layer with the TEI-file
        val teiMergedFile = executeAndLogTime("merge large TEI file") {
            corpus.documents.readOrThrow(teiUploadedFileName).merge(
                DocumentTransformMetadata(
                    corpus = corpus,
                    job = corpus.jobs.readOrThrow(jobName),
                    document = corpus.documents.readOrThrow(teiUploadedFileName),
                    user = User("test-user")
                )
            )
        }

        println("Created teiMergedFile. teiMergedFile size: ${teiMergedFile.file.length()} bytes")
        displayMemory()
    }
}