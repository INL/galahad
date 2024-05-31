package org.ivdnt.galahad.port.conllu

import org.ivdnt.galahad.data.corpus.Corpus
import org.ivdnt.galahad.data.layer.Layer
import org.ivdnt.galahad.data.layer.Term
import org.ivdnt.galahad.port.DocTest
import org.ivdnt.galahad.port.LayerBuilder
import org.ivdnt.galahad.port.createCorpus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ConlluExportTest {

    lateinit var corpus: Corpus

    @BeforeEach
    fun initCorpus() {
        corpus = createCorpus()
    }

    @Test
    fun `Convert dummy layer to TSV`() {
        val layer: Layer = LayerBuilder().loadDummies(3).build()
        // We'll add some extra edge cases to the dummy layer.
        layer.terms[0] = Term(null, null, mutableListOf(layer.wordForms[0]))
        layer.terms[1] = Term("dummy", "pos(a=1,b=2)", mutableListOf(layer.wordForms[1]))

        DocTest.builder( corpus )
            .expectingFile("conllu/comments/converted-output.conllu")
            .convertToConllu(layer)
            .ignoreTrailingWhiteSpaces()
            .result()
    }

    @Test
    fun `Merge dummy layer with Conllu`() {
        val layer: Layer = LayerBuilder().loadDummies(20).build()
        DocTest.builder( corpus )
            .expectingFile("conllu/comments/merged-output.conllu")
            .mergeConllu("conllu/comments/input.conllu", layer)
            .ignoreTrailingWhiteSpaces()
            .result()
    }
}