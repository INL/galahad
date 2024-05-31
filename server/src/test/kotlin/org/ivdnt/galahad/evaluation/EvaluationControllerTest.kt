package org.ivdnt.galahad.evaluation

import java.util.zip.ZipInputStream
import org.ivdnt.galahad.TestConfig
import org.ivdnt.galahad.UserHeader
import org.ivdnt.galahad.app.Config
import org.ivdnt.galahad.app.GalahadApplication
import org.ivdnt.galahad.data.corpus.Corpus
import org.ivdnt.galahad.port.Resource
import org.ivdnt.galahad.port.createCorpus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import java.io.File
import java.nio.charset.StandardCharsets

@WebMvcTest(properties = ["spring.main.allow-bean-definition-overriding=true"])
@ContextConfiguration(classes = [GalahadApplication::class, TestConfig::class])
class EvaluationControllerTest(
    @Autowired val mvc: MockMvc,
    @Autowired val config: Config,
) {
    @Test
    fun `Download evaluation zip of all 3`() {
        val corpus = corpus()
        EvaluationUtil.addDocWithMatchingMultiPosLemma(corpus)

        // url
        val uuid = corpus.metadata.expensiveGet().uuid
        val url = "/corpora/$uuid/jobs/pie-tdn/evaluation/download?reference=sourceLayer"
        // /GET
        val bytes = mvc.perform(
            MockMvcRequestBuilders.get(url)
                .headers(UserHeader.get())
        ).andReturn().response.contentAsByteArray
        assertEvalZip(bytes)
    }

    @Test
    fun `Download confusion samples`() {
        val corpus = corpus()
        // url
        val uuid = corpus.metadata.expensiveGet().uuid
        val url = "/corpora/$uuid/jobs/pie-tdn/evaluation/download?reference=sourceLayer&referencePos=ADJ&hypothesisPos=ADJ"
        val bytes = mvc.perform(
            MockMvcRequestBuilders.get(url)
                .headers(UserHeader.get())
        ).andReturn().response.contentAsByteArray
        assertSingleFileZip(bytes, "confusion-samples.csv")
    }

    @Test
    fun `Download metrics samples`() {
        val corpus = corpus()
        // url
        val uuid = corpus.metadata.expensiveGet().uuid
        val params = mapOf(
            "reference" to "sourceLayer",
            "setting" to "posByPos",
            "class" to "truePositive",
            "group" to "ADJ",
        )
        val url = "/corpora/$uuid/jobs/pie-tdn/evaluation/metrics/download?" +
                params.map { (k, v) -> "$k=$v" }.joinToString("&")
        val bytes = mvc.perform(
            MockMvcRequestBuilders.get(url)
                .headers(UserHeader.get())
        ).andReturn().response.contentAsByteArray
        assertSingleFileZip(bytes, "metrics-samples.csv")
    }

    private fun corpus(): Corpus {
        // Need a corpus first
        val corpus = createCorpus(
            config.getWorkingDirectory().resolve("corpora").resolve("custom")
        )
        EvaluationUtil.add_two_docs_to_corpus(corpus)
        EvaluationUtil.addDocWithMissingMatches(corpus)
        return corpus
    }

    private fun assertEvalZip(byteArray: ByteArray) {
        val zipInputStream = ZipInputStream(byteArray.inputStream())
        var zipEntry = zipInputStream.nextEntry
        var evals = 0
        while(zipEntry != null) {
            println("unzipped: " + (zipEntry.name ?: ""))
            val fileContent = String(zipInputStream.readAllBytes(), StandardCharsets.UTF_16LE)
            if (zipEntry.name.split(".")[1] == "csv") {
                val f: File? = try { Resource.get("evaluation/zip/${zipEntry.name}") } catch (e: Exception) { null }
                val content: String = f?.readText() ?: ""
                assertEquals(content, fileContent)
                evals++
            }
            zipEntry = zipInputStream.nextEntry
        }
        assertEquals(13, evals)
    }

    private fun assertSingleFileZip(byteArray: ByteArray, expected: String) {
        val zipInputStream = ZipInputStream(byteArray.inputStream())
        var zipEntry = zipInputStream.nextEntry
        var evals = 0
        while(zipEntry != null) {
            println("unzipped: " + (zipEntry.name ?: ""))
            val fileContent = String(zipInputStream.readAllBytes(), StandardCharsets.UTF_16LE)
            if (zipEntry.name.split(".")[1] == "csv") {
                assertEquals(Resource.get("evaluation/zip/$expected").readText(), fileContent)
                evals++
            }
            zipEntry = zipInputStream.nextEntry
        }
        assertEquals(1, evals)
    }
}