package org.ivdnt.galahad.data.document

import org.ivdnt.galahad.TestConfig
import org.ivdnt.galahad.app.User
import org.ivdnt.galahad.data.corpus.Corpus
import org.ivdnt.galahad.port.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class DocumentTest {
    lateinit var corpus: Corpus

    @BeforeEach
    fun initCorpus() {
        corpus = createCorpus()
    }

    @Test
    fun `UUID stored in cache file`() {
        val doc = Resource.getDoc("all-formats/input/input.txt")
        // At this point, no uuid exists
        assertFalse(doc.uuidFile.exists())
        // Retrieve the uuid, which in fact generates one
        val uuid = doc.uuid
        // After the uuid is generated, it should be stored in the cache file
        assert(doc.uuidFile.exists())
        val uuidFromFile = doc.uuidFile.readText()
        assertEquals(uuid.toString(), uuidFromFile)
        // The property should not generate a new one
        assertEquals(uuid, doc.uuid)
    }

    @Nested
    inner class GenerateAsTest {
        // Yes, these tests could be in a for loop on DocumentFormat.entries,
        // but that would make the test output less readable.

        @Test
        fun `Convert Conllu to each other format`() {
            convertFormatToAllOthers(DocumentFormat.Conllu)
        }

        @Test
        fun `Convert TEI to each other format`() {
            convertFormatToAllOthers(DocumentFormat.TeiP5)
        }

        @Test
        fun `Convert Txt to each other format`() {
            convertFormatToAllOthers(DocumentFormat.Txt)
        }

        @Test
        fun `Convert TSV to each other format`() {
            convertFormatToAllOthers(DocumentFormat.Tsv)
        }

        @Test
        fun `Convert Folia to each other format`() {
            convertFormatToAllOthers(DocumentFormat.Folia)
        }

        @Test
        fun `Convert Naf to each other format`() {
            convertFormatToAllOthers(DocumentFormat.Naf)
        }

        private fun convertFormatToAllOthers(formatFrom: DocumentFormat) {
            val tempDir: File = createTempDirectory().toFile()
            // Skip the formats that are not supported
            if (formatFrom == DocumentFormat.TeiP4Legacy || formatFrom == DocumentFormat.TeiP5Legacy || formatFrom == DocumentFormat.Unknown) {
                return
            }

            // Create the file of formatFrom
            val inputFile = Resource.get("all-formats/input/input.${formatFrom.extension}")
            val tempFile = tempDir.resolve("input." + formatFrom.extension)
            inputFile.copyTo(tempFile, true)
            val name = corpus.documents.create(tempFile)
            val doc = corpus.documents.readOrThrow(name)
            val job = corpus.jobs.createOrThrow(TestConfig.TAGGER_NAME)
            // create the layer based on the plaintext parsing
            val plaintext = doc.plaintext
            val layer = LayerBuilder().loadLayerFromTSV("all-formats/input/pie-tdn.tsv", plaintext).build()
            job.document(name).setResult(layer)

            // Convert to each other format
            for (formatTo in DocumentFormat.entries) {
                val meta = DocumentTransformMetadata(
                    corpus, job, doc, User("testUser"), formatTo
                )
                when (formatTo) {
                    // Skip the unsupported
                    DocumentFormat.TeiP4Legacy,
                    DocumentFormat.TeiP5Legacy,
                    DocumentFormat.Unknown,
                    -> assertThrows(Exception::class.java) { doc.generateAs(formatTo, meta) }
                    // Convert to the supported
                    else -> {
                        // Skip the same format
                        if (formatFrom == formatTo) continue
                        println("Converting ${formatFrom.name} to ${formatTo.name}")
                        val result: File = doc.generateAs(formatTo, meta)
                        val expected: File = Resource.get("all-formats/output/from-$formatFrom-to-$formatTo.${formatTo.extension}")
                        val test = TestResult(expected.readText(), result.readText())
                        test.ignoreDate().ignoreUUID().ignoreTrailingWhiteSpaces().result()
                    }
                }
            }
        }
    }
}