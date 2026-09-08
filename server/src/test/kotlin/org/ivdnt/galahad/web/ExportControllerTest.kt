package org.ivdnt.galahad.web

import java.io.File
import java.util.*
import java.util.zip.ZipInputStream
import kotlin.io.path.createTempDirectory
import org.ivdnt.galahad.app.Config
import org.ivdnt.galahad.app.Galahad
import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.exceptions.*
import org.ivdnt.galahad.layer.Layer.Companion.SOURCE_LAYER
import org.ivdnt.galahad.util.TestConfig
import org.ivdnt.galahad.util.TestUtil
import org.ivdnt.galahad.util.TestUtil.WEB_CORPUS
import org.ivdnt.galahad.util.TestUtil.assignHeaders
import org.ivdnt.galahad.util.withoutFormatExt
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActionsDsl
import org.springframework.test.web.servlet.get

/** Web controller tests for serialization, status, exception resolving and permissions. */
@SpringBootTest(properties = ["spring.main.allow-bean-definition-overriding=true"])
@AutoConfigureMockMvc
@ContextConfiguration(classes = [Galahad::class, TestConfig::class])
class ExportControllerTest(@Autowired val mvc: MockMvc, @Autowired val config: Config) {

    @Nested
    inner class ConvertDocumentTest {
        private fun assertConvertDocument(user: String, dataset: Boolean = false) {
            val corpus = TestUtil.createFilledCorpus(config, dataset)
            val files = TestUtil.get(WEB_CORPUS).listFiles()
            val file = files.first { it.name.endsWith(DocumentFormat.TeiP5.extension) }
            val exported =
                performConvertDoc(
                        corpus.uuid,
                        doc = file.withoutFormatExt,
                        format = DocumentFormat.Tsv.identifier,
                        user = user,
                    )
                    .andExpect {
                        status { isOk() }
                        header { exists("Content-Disposition") }
                        content { contentType(MediaType.TEXT_PLAIN) }
                    }
                    .andReturn()
                    .response
                    .contentAsString
            val expectedFile = TestUtil.get("web/exported").listFiles().first()
            assertEquals(expectedFile.readText(), exported)
        }

        @Test
        fun `Owner can convert document`() {
            assertConvertDocument(TestUtil.USER)
        }

        @Test
        fun `Admin can convert document`() {
            assertConvertDocument("admin")
        }

        @Test
        fun `Collaborator can convert document`() {
            assertConvertDocument("collaborator")
        }

        @Test
        fun `Viewer can convert document`() {
            assertConvertDocument("viewer")
        }

        @Test
        fun `Stranger can convert document from dataset`() {
            assertConvertDocument("stranger", dataset = true)
        }

        @Test
        fun `Stranger can't convert document`() {
            val corpus = TestUtil.createFilledCorpus(config)
            val file = TestUtil.get(WEB_CORPUS).listFiles().first()
            performConvertDoc(
                    corpus.uuid,
                    doc = file.name,
                    format = DocumentFormat.Tsv.identifier,
                    user = "stranger",
                )
                .andExpect {
                    status { isForbidden() }
                    match { it.resolvedException is UserUnauthorizedException }
                }
        }

        @Test
        fun `Can't convert non-existing corpus`() {
            performConvertDoc(
                    UUID.randomUUID(),
                    "non-existing",
                    "non-existing.txt",
                    DocumentFormat.Tsv.identifier,
                )
                .andExpect {
                    status { isNotFound() }
                    match { it.resolvedException is CorpusNotFoundException }
                }
        }

        @Test
        fun `Can't convert non-existing layer`() {
            val corpus = TestUtil.createCorpus(config)
            performConvertDoc(
                    corpus.uuid,
                    "non-existing",
                    "non-existing.txt",
                    DocumentFormat.Tsv.identifier,
                )
                .andExpect {
                    status { isNotFound() }
                    match { it.resolvedException is LayerNotFoundException }
                }
        }

        @Test
        fun `Can't convert non-existing document`() {
            val corpus = TestUtil.createFilledCorpus(config)
            performConvertDoc(
                    corpus.uuid,
                    doc = "non-existing.txt",
                    format = DocumentFormat.Tsv.identifier,
                )
                .andExpect {
                    status { isNotFound() }
                    match { it.resolvedException is DocumentNotFoundException }
                }
        }

        @Test
        fun `Can't convert non-existing format`() {
            val corpus = TestUtil.createFilledCorpus(config)
            val file = TestUtil.get(WEB_CORPUS).listFiles().first()
            performConvertDoc(corpus.uuid, doc = file.name, format = "non-existing").andExpect {
                status { isBadRequest() }
                match { it.resolvedException is InvalidDocumentFormatException }
            }
        }
    }

    @Nested
    inner class ConvertCorpusTest {
        private fun assertConvertCorpus(user: String, dataset: Boolean = false) {
            val corpus = TestUtil.createFilledCorpus(config, dataset)
            val exportedBytes =
                performConvertCorpus(
                        corpus.uuid,
                        format = DocumentFormat.Tsv.identifier,
                        user = user,
                    )
                    .andExpect {
                        status { isOk() }
                        header { exists("Content-Disposition") }
                        content { contentType("application/zip") }
                    }
                    .andReturn()
                    .response
                    .contentAsByteArray
            // assert that unzipping results in the same number of files (+metadata)
            // we have other tests for matching content
            val expectedFiles = TestUtil.get(WEB_CORPUS).listFiles()
            val actualFiles = unzip(exportedBytes)
            assertEquals(expectedFiles.size * 2 + 2, actualFiles.size)
        }

        @Test
        fun `Owner can convert corpus`() {
            assertConvertCorpus(TestUtil.USER)
        }

        @Test
        fun `Admin can convert corpus`() {
            assertConvertCorpus("admin")
        }

        @Test
        fun `Collaborator can convert corpus`() {
            assertConvertCorpus("collaborator")
        }

        @Test
        fun `Viewer can convert corpus`() {
            assertConvertCorpus("viewer")
        }

        @Test
        fun `Stranger can convert dataset corpus`() {
            assertConvertCorpus("stranger", dataset = true)
        }

        @Test
        fun `Stranger can't convert corpus`() {
            val corpus = TestUtil.createFilledCorpus(config)
            performConvertCorpus(
                    corpus.uuid,
                    format = DocumentFormat.Tsv.identifier,
                    user = "stranger",
                )
                .andExpect {
                    status { isForbidden() }
                    match { it.resolvedException is UserUnauthorizedException }
                }
        }

        @Test
        fun `Can't convert non-existing corpus`() {
            performConvertCorpus(UUID.randomUUID(), "non-existing", DocumentFormat.Tsv.identifier)
                .andExpect {
                    status { isNotFound() }
                    match { it.resolvedException is CorpusNotFoundException }
                }
        }

        @Test
        fun `Can't convert non-existing layer`() {
            val corpus = TestUtil.createCorpus(config)
            performConvertCorpus(corpus.uuid, "non-existing", DocumentFormat.Tsv.identifier)
                .andExpect {
                    status { isNotFound() }
                    match { it.resolvedException is LayerNotFoundException }
                }
        }

        @Test
        fun `Can't convert non-existing format`() {
            val corpus = TestUtil.createFilledCorpus(config)
            performConvertCorpus(corpus.uuid, format = "non-existing").andExpect {
                status { isBadRequest() }
                match { it.resolvedException is InvalidDocumentFormatException }
            }
        }
    }

    private fun performConvertDoc(
        corpus: UUID,
        layer: String = SOURCE_LAYER,
        doc: String,
        format: String,
        user: String = TestUtil.USER,
    ): ResultActionsDsl =
        mvc.get(
            "/corpora/$corpus/layers/$layer/documents/$doc/export/convert?format={format}",
            format,
        ) {
            headers { assignHeaders(this, user) }
        }

    private fun performConvertCorpus(
        corpus: UUID,
        layer: String = SOURCE_LAYER,
        format: String,
        user: String = TestUtil.USER,
    ): ResultActionsDsl =
        mvc.get("/corpora/$corpus/layers/$layer/export/convert?format={format}", format) {
            headers { assignHeaders(this, user) }
        }

    private fun unzip(bytes: ByteArray): List<File> {
        val tmpDir = createTempDirectory().toFile()
        return ZipInputStream(bytes.inputStream()).use { stream ->
            generateSequence { stream.nextEntry }
                .filterNot { it.isDirectory }
                .map {
                    tmpDir.resolve(it.name).apply {
                        parentFile.mkdirs()
                        writeBytes(stream.readBytes())
                    }
                }
                .toList()
        }
    }
}
