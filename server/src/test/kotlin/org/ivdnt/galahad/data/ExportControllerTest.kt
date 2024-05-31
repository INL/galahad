package org.ivdnt.galahad.data

import org.ivdnt.galahad.TestConfig
import org.ivdnt.galahad.UserHeader
import org.ivdnt.galahad.app.Config
import org.ivdnt.galahad.app.GalahadApplication
import org.ivdnt.galahad.createCorpus
import org.ivdnt.galahad.data.corpus.Corpus
import org.ivdnt.galahad.data.layer.Layer
import org.ivdnt.galahad.port.LayerBuilder
import org.ivdnt.galahad.port.Resource
import org.ivdnt.galahad.port.TestResult
import org.ivdnt.galahad.uploadFile
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.zip.ZipInputStream

@WebMvcTest(properties = ["spring.main.allow-bean-definition-overriding=true"])
@ContextConfiguration(classes = [GalahadApplication::class, TestConfig::class])
class ExportControllerTest(
    @Autowired val mvc: MockMvc,
    @Autowired val config: Config,
    @Autowired val ctrl: ExportController,
) {

    @Test
    fun convertAndExportJob() {
        val corpus = createAndPopulateCorpus()

        val uuid = corpus.metadata.expensiveGet().uuid
        val bytes = mvc.perform(
            MockMvcRequestBuilders.get("/corpora/$uuid/jobs/pie-tdn/export/convert")
                .param("format", "folia").headers(
                    UserHeader.get())
        ).andReturn().response.contentAsByteArray
        val files = unzip(bytes)
        val teiToFolia: File = files.first { it.name.endsWith("tei.folia.xml") }
        val result = TestResult(Resource.get("all-formats/output/from-TeiP5-to-Folia.folia.xml").readText(), teiToFolia.readText())
        result.ignoreLineEndings().result()
    }

    fun unzip(bytes: ByteArray): List<File> {
        val zipInputStream = ZipInputStream(bytes.inputStream())
        var zipEntry = zipInputStream.nextEntry
        val files = mutableListOf<File>()

        while (zipEntry != null) {
            println("unzipped: " + (zipEntry.name ?: ""))
            val file = File.createTempFile("export", zipEntry.name)
            file.writeBytes(zipInputStream.readBytes())
            files.add(file)
            zipEntry = zipInputStream.nextEntry
        }

        return files
    }

    @Test
    fun mergeAndExportJob() {
    }

    // Create and populate a corpus with a TEI and Folia document.
    fun createAndPopulateCorpus(): Corpus {
        val corpus = createCorpus(config)
        mvc.uploadFile(Resource.get("all-formats/input/input.tei.xml"), corpus)
        // hardcode layer
        val layer: Layer = LayerBuilder().loadLayerFromTSV("all-formats/input/pie-tdn.tsv", Resource.get("all-formats/input/input.txt").readText()).build()
        val job = corpus.jobs.createOrThrow("pie-tdn")
        job.document("input.tei.xml").setResult(layer)
        mvc.uploadFile(Resource.get("all-formats/input/input.folia.xml"), corpus)
        return corpus
    }
}