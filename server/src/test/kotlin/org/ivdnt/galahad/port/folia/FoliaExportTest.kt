package org.ivdnt.galahad.port.folia

import org.ivdnt.galahad.data.corpus.Corpus
import org.ivdnt.galahad.port.DocTest
import org.ivdnt.galahad.port.LayerBuilder
import org.ivdnt.galahad.port.Resource
import org.ivdnt.galahad.port.createCorpus
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.BeforeEach

internal class FoliaExportTest {

    lateinit var corpus: Corpus

    @BeforeEach
    fun initCorpus() {
        corpus = createCorpus()
    }

    @Test
    fun `Convert dummy layer to folia`() {
        val layer = LayerBuilder().loadDummies(3).build()
        DocTest.builder(corpus)
            .expectingFile("folia/export/converted-output.folia.xml")
            // document.parse() will be called and throw on an empty file, hence the dummy file.
            .convertToFolia(Resource.get("folia/dummy.folia.xml"), layer)
            .ignoreTrailingWhiteSpaces()
            .result()
    }

    @Test
    fun `Merge dummy layer with file containing correction tags`() {
        val layer = LayerBuilder().loadDummies(10, literal="word0 ").build()
        DocTest.builder(corpus)
            .expectingFile("folia/corrections/merged-output.folia.xml")
            .mergeFolia(Resource.get("folia/corrections/input.folia.xml"), layer)
            .ignoreTrailingWhiteSpaces()
            .result()
    }

    @Test
    fun `Merge pie-tdn result with heavily style tag twined folia`() {
        val plaintext: String = Resource.get("folia/twine/plaintext.txt").readText()
        val layer = LayerBuilder()
            .loadLayerFromTSV("folia/twine/pie-tdn.tsv", plaintext)
            .build()
        DocTest.builder(corpus)
            .expectingFile("folia/twine/merged-output.folia.xml")
            .mergeFolia(Resource.get("folia/twine/twine.folia.xml"), layer)
            .ignoreTrailingWhiteSpaces()
            .result()
    }
}