package org.ivdnt.galahad.evaluation.assays

import org.ivdnt.galahad.JSON
import org.ivdnt.galahad.TestConfig
import org.ivdnt.galahad.app.Config
import org.ivdnt.galahad.app.GalahadApplication
import org.ivdnt.galahad.evaluation.EvaluationUtil
import org.ivdnt.galahad.evaluation.metrics.FlatMetricType
import org.ivdnt.galahad.port.LayerBuilder
import org.ivdnt.galahad.port.createCorpus
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import java.io.File

@WebMvcTest(properties = ["spring.main.allow-bean-definition-overriding=true"])
@ContextConfiguration(classes = [GalahadApplication::class, TestConfig::class])
class AssaysControllerTest(
    @Autowired val mvc: MockMvc,
    @Autowired val config: Config,
    @Autowired val ctrl: AssaysController,
) {
    @Test
    fun getAssays() {
        // No assays should exist
        var assays = ctrl.assaysMatrix.get<AssaysMatrix>()
        assertEquals(0, assays.size)

        // Need a corpus first
        val corpus = createCorpus(
            config.getWorkingDirectory().resolve("corpora").resolve("custom"),
            isDataset = true,
            isAdmin = true
        )

        // Neither should individual ones
        val assayRequest: MvcResult = mvc.perform(
            MockMvcRequestBuilders.get("/corpora/${corpus.metadata.expensiveGet().uuid}/jobs/pie-tdn/evaluation/assay")
        ).andReturn()
        assertEquals("", assayRequest.response.contentAsString)

        // Add result
        val name = corpus.documents.create(File.createTempFile("tmp", ".txt"))
        val layer = LayerBuilder().loadDummies(100).build()
        EvaluationUtil.addLayersAsJobs(corpus, name, layer, layer)

        // job assay should exist
        assertNotNull(corpus.jobs.readOrThrow("pie-tdn").assay.get<Map<String,FlatMetricType>>())

        // /GET
        val assaysRequest: MvcResult = mvc.perform(
            MockMvcRequestBuilders.get("/assays")
        ).andReturn()
        assays = JSON.fromStr<AssaysMatrix>(assaysRequest.response.contentAsString)
        assertEquals(1, assays.size)
        assertEquals(1f, assays["testCorpus"]!!["lemmaPosByPos"]!!["pie-tdn"]!!.micro.accuracy, 0.00001f)
        // We don't want the source layer, as it would always 100% agree with itself.
        assertFalse(assays.containsKey("sourceLayer"))
    }
}