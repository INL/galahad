package org.ivdnt.galahad.formats

import java.io.ByteArrayOutputStream
import java.io.File
import org.ivdnt.galahad.app.User
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.export.CorpusExport
import org.ivdnt.galahad.util.TestUtil
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach

abstract class MergerTest {
    private lateinit var corpus: Corpus
    abstract val folder: String
    abstract val format: DocumentFormat

    @BeforeEach
    fun initCorpus() {
        corpus = TestUtil.createCorpus()
    }

    fun merge() {
        val input: File = TestUtil.get("formats/$folder/input.${format.extension}")
        val merge = TestUtil.get("formats/$folder/input.json")
        val output: File = TestUtil.get("formats/$folder/output.${format.extension}")

        val doc = corpus.documents.createOrThrow(input)
        // set merge layer as a job
        corpus.layers.createOrThrow("spacy").documents.createOrThrow(merge)

        // merge
        val corpusExport = CorpusExport(corpus, "spacy", format, User.DEFAULT_USER, true, false)
        val docExport = corpusExport.document(doc)

        val convertedText =
            ByteArrayOutputStream()
                .also {
                    docExport.merge(it)
                    it.flush()
                }
                .toString()
        val expectedText = output.readText()

        Assertions.assertEquals(expectedText, convertedText)
    }
}
