package org.ivdnt.galahad.jobs

import org.ivdnt.galahad.TestConfig
import org.ivdnt.galahad.app.Config
import org.ivdnt.galahad.app.GalahadApplication
import org.ivdnt.galahad.createCorpus
import org.ivdnt.galahad.port.Resource
import org.ivdnt.galahad.taggers.TaggersController
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.test.context.ContextConfiguration
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import java.util.*

@ContextConfiguration(classes = [GalahadApplication::class, TestConfig::class])
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
    properties = ["server.port=8010", "spring.main.allow-bean-definition-overriding=true"]
)
class JobsControllerTest(
    @Autowired val rest: TestRestTemplate,
    @Autowired val config: Config,
    @Autowired val ctrl: JobsController,
    @Autowired val taggers: TaggersController,
) {

    @Disabled
    @Test
    fun postJob() {
        val corpus = createCorpus(config)
        val docName = corpus.documents.create(Resource.get("all-formats/input/input.tei.xml"))
        val uuid = corpus.metadata.expensiveGet().uuid

        assertEquals(taggers.getTaggers().size + 1, getJobs(uuid).size) // +1 for the sourceLayer
        var progress: Progress =
            rest.postForEntity("/corpora/$uuid/jobs/pie-tdn", getHeaders(), Progress::class.java).body!!
        assertEquals(1, progress.total)
        assertTrue(progress.busy)

        Thread.sleep(3000)

        // poll progress
        progress = pollProgress(uuid, TestConfig.TAGGER_NAME)
        assertFalse(progress.busy)
        assertEquals(1, progress.finished)

        // check result
        val resultPreview = getDocumentJobResult(uuid, TestConfig.TAGGER_NAME, docName)
        assertEquals(TestConfig.TAGGER_NAME, resultPreview.name)
        assertTrue(resultPreview.summary.numWordForms > 0)
        assertTrue(resultPreview.summary.numTerms > 0)
        assertTrue(resultPreview.summary.numLemma > 0)
        assertTrue(resultPreview.summary.numPOS > 0)
        assertTrue(resultPreview.preview.terms.isNotEmpty())
        assertTrue(resultPreview.preview.wordforms.isNotEmpty())
    }

    private fun pollProgress(uuid: UUID, job: String): Progress {
        return rest.exchange(
            "/corpora/$uuid/jobs/$job/progress", HttpMethod.GET, getHeaders(), Progress::class.java
        ).body!!
    }

    private fun getJobs(uuid: UUID): Set<State> {
        return rest.exchange("/corpora/$uuid/jobs?includePotentialJobs=true",
                             HttpMethod.GET,
                             getHeaders(),
                             object : ParameterizedTypeReference<Set<State>>() {}).body!!
    }

    private fun getDocumentJobResult(uuid: UUID, job: String, document: String): DocumentJobResult {
        return rest.exchange(
            "/corpora/$uuid/jobs/$job/documents/$document/result",
            HttpMethod.GET,
            getHeaders(),
            DocumentJobResult::class.java
        ).body!!
    }

    private fun getHeaders(): HttpEntity<Any> {
        val headers: MultiValueMap<String, String> = LinkedMultiValueMap()
        headers.add("remote_user", "testUser")
        return HttpEntity(null, headers)
    }
}