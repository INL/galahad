package org.ivdnt.galahad.data

import org.ivdnt.galahad.*
import org.ivdnt.galahad.app.Config
import org.ivdnt.galahad.app.GalahadApplication
import org.ivdnt.galahad.data.corpus.Corpus
import org.ivdnt.galahad.data.document.DocumentMetadata
import org.ivdnt.galahad.port.Resource
import org.ivdnt.galahad.util.createZipFile
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import java.io.File

@WebMvcTest(properties = ["spring.main.allow-bean-definition-overriding=true"])
@ContextConfiguration(classes = [GalahadApplication::class, TestConfig::class])
class DocumentsControllerTest(
    @Autowired val mvc: MockMvc,
    @Autowired val config: Config,
    @Autowired val ctrl: DocumentsController,
) {

    @Test
    fun `Upload files of all formats`() {
        val corpus = createCorpus(config)

        // list files in directory
        val dir: File = Resource.get("all-formats/input")
        for (file in dir.listFiles()) {
            // skip layer pie-tdn.tsv
            if (file.name != "pie-tdn.tsv") {
                mvc.uploadFile(file, corpus)
            }
        }
        // check if all files are uploaded
        assertEquals(6, getDocs(corpus).size)
        // Get raw file
        val doc = getDocs(corpus)[0]
        val uuid = corpus.metadata.expensiveGet().uuid
        val result: MvcResult = mvc.perform(
            MockMvcRequestBuilders.get("/corpora/$uuid/documents/${doc.name}/raw").headers(UserHeader.get())
        ).andReturn()
        assertEquals(Resource.get("all-formats/input/${doc.name}").readText(), result.response.contentAsString)
        // Delete a doc
        mvc.perform(
            MockMvcRequestBuilders.delete("/corpora/$uuid/documents/${doc.name}").headers(UserHeader.get())
        )

        assertEquals(5, getDocs(corpus).size)
    }

    @Test
    fun `Upload zip with all formats`() {
        val corpus = createCorpus(config)
        val zip = createZipFile(Resource.get("all-formats/input").listFiles().filter { it.name != "pie-tdn.tsv" }.asSequence())
        mvc.uploadFile(zip, corpus, MediaType.APPLICATION_OCTET_STREAM_VALUE)
        assertEquals(6, getDocs(corpus).size)
    }

    @Test
    fun `Upload invalid file`() {
        val corpus = createCorpus(config)
        val zip = createZipFile(Resource.get("all-formats/invalid").listFiles().asSequence())
        assertThrows(Exception::class.java){ mvc.uploadFile(zip, corpus, MediaType.APPLICATION_OCTET_STREAM_VALUE) }
    }

    private fun getDocs(corpus: Corpus): List<DocumentMetadata> {
        // Request doc metadata
        val uuid = corpus.metadata.expensiveGet().uuid
        val result: MvcResult = mvc.perform(
            MockMvcRequestBuilders.get("/corpora/$uuid/documents").headers(UserHeader.get())
        ).andReturn()

        // check doc count
        val docs: List<DocumentMetadata> = JSON.fromStr(result.response.contentAsString)
        return docs
    }


}