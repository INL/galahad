package org.ivdnt.galahad.web

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.ok
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import java.util.*
import org.ivdnt.galahad.app.Config
import org.ivdnt.galahad.app.Galahad
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.exceptions.TaggerNotFoundException
import org.ivdnt.galahad.jobs.JobScheduler
import org.ivdnt.galahad.layer.Layer.Companion.SOURCE_LAYER
import org.ivdnt.galahad.taggers.Tagger
import org.ivdnt.galahad.util.TestConfig
import org.ivdnt.galahad.util.TestUtil
import org.ivdnt.galahad.util.TestUtil.assignHeaders
import org.ivdnt.galahad.util.andDeserialize
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.wiremock.spring.ConfigureWireMock
import org.wiremock.spring.EnableWireMock

/** Web controller tests for serialization, status, exception resolving and permissions. */
@SpringBootTest(properties = ["spring.main.allow-bean-definition-overriding=true"])
@AutoConfigureMockMvc
@ContextConfiguration(classes = [Galahad::class, TestConfig::class])
@EnableWireMock(ConfigureWireMock(name = "my-mock", port = 8102))
class TaggersControllerTest(@Autowired val mvc: MockMvc, @Autowired val config: Config) {
    @BeforeEach
    fun setUp() {
        TestConfig.reset()
    }

    @Test
    fun `Can get taggers`() {
        val taggers: List<Tagger> =
            mvc.get("/taggers")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                }
                .andReturn()
                .andDeserialize()
        assertEquals(1, taggers.count { it.name == TestUtil.TAGGER })
        assert(taggers.sumOf { it.attributions.size } > 0)
        assert(taggers.sumOf { it.annotations.sumOf { it.principles?.size ?: 0 } } > 0)
    }

    @Test
    fun `Can get single tagger`() {
        val tagger: Tagger =
            mvc.get("/taggers/${TestUtil.TAGGER}")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                }
                .andReturn()
                .andDeserialize()
        assertEquals(TestUtil.TAGGER, tagger.name)
    }

    @Test
    fun `Can't get source tagger`() {
        mvc.get("/taggers/$SOURCE_LAYER").andExpect {
            status { isNotFound() }
            match { it.resolvedException is TaggerNotFoundException }
        }
    }

    @Test
    fun `Can't get health of invalid tagger`() {
        mvc.get("/taggers/invalid/health").andExpect {
            status { isNotFound() }
            match { it.resolvedException is TaggerNotFoundException }
        }
    }

    private fun getQueue(): Int =
        mvc.get("/taggers/queue")
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
            .andReturn()
            .andDeserialize()

    @Test
    fun `Can get queue`() {
        JobScheduler.reset()
        assertEquals(0, getQueue())
    }

    private fun increaseQueue(corpus: Corpus) {
        stubFor(WireMock.post("/input").willReturn(ok().withBody(UUID.randomUUID().toString())))
        mvc.post("/corpora/${corpus.uuid}/jobs/${TestUtil.TAGGER}") { headers(::assignHeaders) }
            .andExpect { status { isAccepted() } }
    }

    private fun decreaseQueue(corpus: Corpus) {
        mvc.delete("/corpora/${corpus.uuid}/jobs/${TestUtil.TAGGER}") { headers(::assignHeaders) }
    }

    @Test
    fun `Queue increases`() {
        JobScheduler.reset()
        val corpus = TestUtil.createFilledCorpus(config)
        increaseQueue(corpus)
        assertEquals(1, getQueue())
    }

    @Test
    fun `Queue decreases`() {
        JobScheduler.reset()
        val corpus = TestUtil.createFilledCorpus(config)
        increaseQueue(corpus)
        assertEquals(1, getQueue())
        decreaseQueue(corpus)
        assertEquals(0, getQueue())
    }
}
