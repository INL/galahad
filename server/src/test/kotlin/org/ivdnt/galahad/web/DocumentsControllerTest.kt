package org.ivdnt.galahad.web

import java.io.File
import java.util.*
import org.ivdnt.galahad.app.Config
import org.ivdnt.galahad.app.Galahad
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.documents.DocumentMetadata
import org.ivdnt.galahad.exceptions.CorpusNotFoundException
import org.ivdnt.galahad.exceptions.DocumentInvalidException
import org.ivdnt.galahad.exceptions.DocumentNotFoundException
import org.ivdnt.galahad.exceptions.UserUnauthorizedException
import org.ivdnt.galahad.formats.ParsedFile
import org.ivdnt.galahad.util.*
import org.ivdnt.galahad.util.TestUtil.WEB_CORPUS
import org.ivdnt.galahad.util.TestUtil.assignHeaders
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
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get

/** Web controller tests for serialization, status, exception resolving and permissions. */
@SpringBootTest(properties = ["spring.main.allow-bean-definition-overriding=true"])
@AutoConfigureMockMvc
@ContextConfiguration(classes = [Galahad::class, TestConfig::class])
class DocumentsControllerTest(@Autowired val mvc: MockMvc, @Autowired val config: Config) {
    // TODO convert to more generic LayerControllerTest?
    // TODO tests that check that deleting a document also deletes its jobs and evaluations

    @Nested
    inner class DocumentUploadTest {
        @Test
        fun `Owner can upload files`() {
            val corpus = TestUtil.createCorpus(config)
            val dir: File = TestUtil.get(WEB_CORPUS)
            val files = dir.listFiles()
            assertEquals(0, getDocs(corpus).size)
            files.forEach { uploadDoc(corpus.uuid, it) }
            assertEquals(files.size, getDocs(corpus).size)
        }

        private fun assertZipUpload(user: String, dataset: Boolean = false) {
            val corpus = TestUtil.createCorpus(config, dataset)
            val dir = TestUtil.get(WEB_CORPUS)
            val files = dir.listFiles()
            val zip = zipDir(dir)
            assertEquals(0, getDocs(corpus).size)
            uploadDoc(corpus.uuid, zip, user, MediaType.APPLICATION_OCTET_STREAM_VALUE)
            assertEquals(files.size, getDocs(corpus).size)
        }

        @Test
        fun `Owner can upload zip to non-dataset`() {
            assertZipUpload(TestUtil.USER)
        }

        @Test
        fun `Admin can upload zip to non-dataset`() {
            assertZipUpload("admin")
        }

        @Test
        fun `Admin can upload zip to dataset`() {
            assertZipUpload("admin", dataset = true)
        }

        @Test
        fun `Collaborator can upload zip to non-dataset`() {
            assertZipUpload("collaborator")
        }

        @Test
        fun `Collaborator can upload zip to dataset`() {
            assertZipUpload("collaborator", dataset = true)
        }

        private fun assertCantUploadFile(user: String, dataset: Boolean = false) {
            val corpus = TestUtil.createCorpus(config, dataset = dataset)
            val file = TestUtil.get(WEB_CORPUS).listFiles().first()
            assertEquals(0, getDocs(corpus).size)
            performUploadDoc(corpus.uuid, file, user).andExpect {
                status { isForbidden() }
                match { it.resolvedException is UserUnauthorizedException }
            }
            assertEquals(0, getDocs(corpus).size)
        }

        @Test
        fun `Viewer can't upload to non-dataset`() {
            assertCantUploadFile("viewer")
        }

        @Test
        fun `Stranger can't upload to non-dataset`() {
            assertCantUploadFile("stranger")
        }

        @Test
        fun `Viewer can't upload to dataset`() {
            assertCantUploadFile("viewer", dataset = true)
        }

        @Test
        fun `Stranger can't upload to dataset`() {
            assertCantUploadFile("stranger", dataset = true)
        }

        @Test
        fun `Can't upload file to non-existing corpus`() {
            val file = TestUtil.get(WEB_CORPUS).listFiles().first()
            performUploadDoc(UUID.randomUUID(), file).andExpect {
                status { isNotFound() }
                match { it.resolvedException is CorpusNotFoundException }
            }
        }

        private fun assertCantUploadInvalidFile(file: File) {
            val corpus = TestUtil.createCorpus(config)
            assertEquals(0, getDocs(corpus).size)
            performUploadDoc(corpus.uuid, file).andExpect {
                status { isBadRequest() }
                match { it.resolvedException is DocumentInvalidException }
            }
            assertEquals(0, getDocs(corpus).size)
        }

        @Test
        fun `Can't upload invalid xml file`() {
            val file = TestUtil.get("formats/invalid/invalid.xml")
            assertCantUploadInvalidFile(file)
        }

        @Test
        fun `Can't upload invalid other extension`() {
            val file = TestUtil.get("formats/invalid/invalid.mp3")
            assertCantUploadInvalidFile(file)
        }
    }

    @Nested
    inner class DocumentGetTest {
        private fun assertDocumentMetadataEquals(
            expectedMetadata: DocumentMetadata,
            actualMetadata: DocumentMetadata,
        ) {
            assertEquals(expectedMetadata.name, actualMetadata.name)
            assertEquals(expectedMetadata.format, actualMetadata.format)
            assertEquals(expectedMetadata.text, actualMetadata.text)
            assertEquals(expectedMetadata.annotations, actualMetadata.annotations)
            for ((expectedTerm, actualTerm) in
                expectedMetadata.preview.terms.zip(actualMetadata.preview.terms)) {
                assertEquals(expectedTerm.id, actualTerm.id)
                assertEquals(expectedTerm.annotations, actualTerm.annotations)
            }
        }

        private fun assertGetDocument(user: String, dataset: Boolean = false) {
            val corpus = TestUtil.createFilledCorpus(config, dataset)
            val file = TestUtil.get(WEB_CORPUS).listFiles().first()
            val expectedMetadata = DocumentMetadata.create(ParsedFile.create(file))
            val actualMetadata = getDoc(corpus.uuid, file.withoutFormatExt, user)
            assertDocumentMetadataEquals(expectedMetadata, actualMetadata)
        }

        @Test
        fun `Owner can get document`() {
            assertGetDocument(TestUtil.USER)
        }

        @Test
        fun `Admin can get document`() {
            assertGetDocument("admin")
        }

        @Test
        fun `Admin can get document from dataset`() {
            assertGetDocument("admin", dataset = true)
        }

        @Test
        fun `Collaborator can get document`() {
            assertGetDocument("collaborator")
        }

        @Test
        fun `Collaborator can get document from dataset`() {
            assertGetDocument("collaborator", dataset = true)
        }

        @Test
        fun `Viewer can get document`() {
            assertGetDocument("viewer")
        }

        @Test
        fun `Viewer can get document from dataset`() {
            assertGetDocument("viewer", dataset = true)
        }

        @Test
        fun `Stranger can get document from dataset`() {
            assertGetDocument("stranger", dataset = true)
        }

        @Test
        fun `Stranger can't get document from non-dataset`() {
            val corpus = TestUtil.createFilledCorpus(config)
            val file = TestUtil.get(WEB_CORPUS).listFiles().first()
            performGetDoc(corpus.uuid, file.name, "stranger").andExpect {
                status { isForbidden() }
                match { it.resolvedException is UserUnauthorizedException }
            }
        }

        @Test
        fun `Can't get non-existing document`() {
            val corpus = TestUtil.createCorpus(config)
            performGetDoc(corpus.uuid, "non-existing.txt").andExpect {
                status { isNotFound() }
                match { it.resolvedException is DocumentNotFoundException }
            }
        }

        @Test
        fun `Can't get document in non-existing corpus`() {
            performGetDoc(UUID.randomUUID(), "non-existing.txt").andExpect {
                status { isNotFound() }
                match { it.resolvedException is CorpusNotFoundException }
            }
        }
    }

    @Nested
    inner class DocumentGetSourceTest {
        private fun assertGetSourceDoc(user: String, dataset: Boolean = false) {
            val files = TestUtil.get(WEB_CORPUS).listFiles()
            val corpus = TestUtil.createFilledCorpus(config, dataset)
            val uuid = corpus.uuid
            for (file in files) {
                assertEquals(
                    file.readText(),
                    getSourceDoc(uuid, file.withoutFormatExt, user),
                )
            }
        }

        @Test
        fun `Owner can get source files`() {
            assertGetSourceDoc(TestUtil.USER)
        }

        @Test
        fun `Admin can get source files from non-dataset`() {
            assertGetSourceDoc("admin")
        }

        @Test
        fun `Admin can get source files from dataset`() {
            assertGetSourceDoc("admin", dataset = true)
        }

        @Test
        fun `Collaborator can get source files from non-dataset`() {
            assertGetSourceDoc("collaborator")
        }

        @Test
        fun `Collaborator can get source files from dataset`() {
            assertGetSourceDoc("collaborator", dataset = true)
        }

        @Test
        fun `Viewer can get source files from non-dataset`() {
            assertGetSourceDoc("viewer")
        }

        @Test
        fun `Viewer can get source files from dataset`() {
            assertGetSourceDoc("viewer", dataset = true)
        }

        @Test
        fun `Stranger can get source files from dataset`() {
            assertGetSourceDoc("stranger", dataset = true)
        }

        @Test
        fun `Stranger can't get source files from non-dataset`() {
            val corpus = TestUtil.createFilledCorpus(config)
            val file = TestUtil.get(WEB_CORPUS).listFiles().first()
            performGetSourceDoc(corpus.uuid, file.name, "stranger").andExpect {
                status { isForbidden() }
                match { it.resolvedException is UserUnauthorizedException }
            }
        }

        @Test
        fun `Can't get non-existing source file`() {
            val corpus = TestUtil.createCorpus(config)
            performGetSourceDoc(corpus.uuid, "non-existing.txt").andExpect {
                status { isNotFound() }
                match { it.resolvedException is DocumentNotFoundException }
            }
        }

        @Test
        fun `Can't get source file in non-existing corpus`() {
            performGetSourceDoc(UUID.randomUUID(), "non-existing.txt").andExpect {
                status { isNotFound() }
                match { it.resolvedException is CorpusNotFoundException }
            }
        }
    }

    @Nested
    inner class DocumentDeleteTest {
        private fun assertDeleteDoc(user: String, dataset: Boolean = false) {
            val corpus = TestUtil.createFilledCorpus(config, dataset)
            val files = TestUtil.get(WEB_CORPUS).listFiles()
            val file = files.first()
            assertEquals(files.size, getDocs(corpus).size)
            deleteDoc(corpus.uuid, file.withoutFormatExt, user)
            assertEquals(files.size - 1, getDocs(corpus).size)
        }

        @Test
        fun `Owner can delete document`() {
            assertDeleteDoc(TestUtil.USER)
        }

        @Test
        fun `Admin can delete document from non-dataset`() {
            assertDeleteDoc("admin")
        }

        @Test
        fun `Admin can delete document from dataset`() {
            assertDeleteDoc("admin", dataset = true)
        }

        @Test
        fun `Collaborator can delete document from non-dataset`() {
            assertDeleteDoc("collaborator")
        }

        @Test
        fun `Collaborator can delete document from dataset`() {
            assertDeleteDoc("collaborator", dataset = true)
        }

        private fun assertCantDeleteDoc(user: String, dataset: Boolean = false) {
            val corpus = TestUtil.createFilledCorpus(config, dataset)
            val files = TestUtil.get(WEB_CORPUS).listFiles()
            val file = files.first()
            assertEquals(files.size, getDocs(corpus).size)
            performDeleteDoc(corpus.uuid, file.name, user).andExpect {
                status { isForbidden() }
                match { it.resolvedException is UserUnauthorizedException }
            }
            assertEquals(files.size, getDocs(corpus).size)
        }

        @Test
        fun `Viewer can't delete document from non-dataset`() {
            assertCantDeleteDoc("viewer")
        }

        @Test
        fun `Stranger can't delete document from non-dataset`() {
            assertCantDeleteDoc("stranger")
        }

        @Test
        fun `Viewer can't delete document from dataset`() {
            assertCantDeleteDoc("viewer", dataset = true)
        }

        @Test
        fun `Stranger can't delete document from dataset`() {
            assertCantDeleteDoc("stranger", dataset = true)
        }

        @Test
        fun `Can't delete document in non-existing corpus`() {
            performDeleteDoc(UUID.randomUUID(), "non-existing.txt").andExpect {
                status { isNotFound() }
                match { it.resolvedException is CorpusNotFoundException }
            }
        }

        @Test
        fun `Can't delete non-existing document`() {
            val corpus = TestUtil.createCorpus(config)
            performDeleteDoc(corpus.uuid, "non-existing.txt").andExpect {
                status { isNotFound() }
                match { it.resolvedException is DocumentNotFoundException }
            }
        }
    }

    private fun performUploadDoc(
        uuid: UUID,
        file: File,
        user: String = TestUtil.USER,
        mediaType: String = MediaType.TEXT_PLAIN_VALUE,
    ): ResultActionsDsl =
        mvc.uploadFile("/corpora/$uuid/layers/source/documents", file, mediaType) {
            headers { assignHeaders(this, user) }
        }

    private fun uploadDoc(
        uuid: UUID,
        file: File,
        user: String = TestUtil.USER,
        mediaType: String = MediaType.TEXT_PLAIN_VALUE,
    ) {
        performUploadDoc(uuid, file, user, mediaType).andExpect { status { isCreated() } }
    }

    private fun performGetDoc(
        uuid: UUID,
        doc: String,
        user: String = TestUtil.USER,
    ): ResultActionsDsl =
        mvc.get("/corpora/$uuid/layers/source/documents/$doc") {
            headers { assignHeaders(this, user) }
        }

    private fun getDoc(uuid: UUID, doc: String, user: String = TestUtil.USER): DocumentMetadata =
        performGetDoc(uuid, doc, user)
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
            .andReturn()
            .andDeserialize()

    private fun performGetSourceDoc(
        uuid: UUID,
        doc: String,
        user: String = TestUtil.USER,
    ): ResultActionsDsl =
        mvc.get("/corpora/$uuid/layers/source/documents/$doc/download") {
            headers { assignHeaders(this, user) }
        }

    private fun getSourceDoc(uuid: UUID, doc: String, user: String = TestUtil.USER): String =
        performGetSourceDoc(uuid, doc, user)
            .andExpect {
                status { isOk() }
                header { exists("Content-Disposition") }
                content { contentType(MediaType.TEXT_PLAIN) }
            }
            .andReturn()
            .response
            .contentAsString

    private fun performDeleteDoc(
        uuid: UUID,
        doc: String,
        user: String = TestUtil.USER,
    ): ResultActionsDsl =
        mvc.delete("/corpora/$uuid/layers/source/documents/${doc}") {
            headers { assignHeaders(this, user) }
        }

    private fun deleteDoc(uuid: UUID, doc: String, user: String = TestUtil.USER) {
        performDeleteDoc(uuid, doc, user).andExpect { status { isNoContent() } }
    }

    private fun getDocs(corpus: Corpus): List<DocumentMetadata> =
        mvc.get("/corpora/${corpus.uuid}/layers/source/documents") { headers(::assignHeaders) }
            .andReturn()
            .andDeserialize()
}
