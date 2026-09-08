package org.ivdnt.galahad.web

import java.util.*
import org.ivdnt.galahad.app.Config
import org.ivdnt.galahad.app.Galahad
import org.ivdnt.galahad.exceptions.CorpusNotFoundException
import org.ivdnt.galahad.exceptions.JobNotFoundException
import org.ivdnt.galahad.exceptions.LayerNotFoundException
import org.ivdnt.galahad.exceptions.UserUnauthorizedException
import org.ivdnt.galahad.layer.Layer.Companion.SOURCE_LAYER
import org.ivdnt.galahad.layers.CorpusLayerMetadata
import org.ivdnt.galahad.util.TestConfig
import org.ivdnt.galahad.util.TestUtil
import org.ivdnt.galahad.util.TestUtil.assignHeaders
import org.ivdnt.galahad.util.andDeserialize
import org.ivdnt.galahad.web.controller.ErrorController
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActionsDsl
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get

@SpringBootTest(properties = ["spring.main.allow-bean-definition-overriding=true"])
@AutoConfigureMockMvc
@Import(ErrorController::class)
@ContextConfiguration(classes = [Galahad::class, TestConfig::class])
class LayerControllerTest(@Autowired val mvc: MockMvc, @Autowired val config: Config) {
    // TODO tests that check that deleting a layer also deletes its jobs and evaluations

    @Nested
    inner class LayerGetTest {
        private fun assertGetLayer(user: String, dataset: Boolean = false) {
            val corpus = TestUtil.createFilledCorpus(config, dataset)
            val layer = getLayer(corpus.uuid, SOURCE_LAYER, user)
            assertEquals(SOURCE_LAYER, layer.tagger.name)
        }

        @Test
        fun `Owner can get layer`() {
            assertGetLayer(TestUtil.USER)
        }

        @Test
        fun `Admin can get layer`() {
            assertGetLayer("admin")
        }

        @Test
        fun `Collaborator can get layer`() {
            assertGetLayer("collaborator")
        }

        @Test
        fun `Viewer can get layer`() {
            assertGetLayer("viewer")
        }

        @Test
        fun `Stranger can get layer from dataset`() {
            assertGetLayer("stranger", dataset = true)
        }

        @Test
        fun `Stranger can't get layer from non-dataset`() {
            val corpus = TestUtil.createFilledCorpus(config)
            performGetLayer(corpus.uuid, SOURCE_LAYER, "stranger").andExpect {
                status { isForbidden() }
                match { it.resolvedException is UserUnauthorizedException }
            }
        }

        @Test
        fun `Can't get non-existing layer`() {
            val corpus = TestUtil.createFilledCorpus(config)
            performGetLayer(corpus.uuid, "non-existing").andExpect {
                status { isNotFound() }
                match { it.resolvedException is LayerNotFoundException }
            }
        }

        @Test
        fun `Can't get layer for non-existing corpus`() {
            performGetLayer(UUID.randomUUID(), SOURCE_LAYER).andExpect {
                status { isNotFound() }
                match { it.resolvedException is CorpusNotFoundException }
            }
        }
    }

    @Nested
    inner class LayerDeleteTest {
        private fun assertDeleteLayer(user: String) {
            val corpus = TestUtil.createJobbedCorpus(config)
            assertTrue(getLayers(corpus.uuid).any { it.tagger.name == TestUtil.TAGGER })
            performDeleteLayer(corpus.uuid, TestUtil.TAGGER, user).andExpect {
                status { isNoContent() }
            }
            assertTrue(getLayers(corpus.uuid).none { it.tagger.name == TestUtil.TAGGER })
        }

        @Test
        fun `Owner can delete layer`() {
            assertDeleteLayer(TestUtil.USER)
        }

        @Test
        fun `Admin can delete layer`() {
            assertDeleteLayer("admin")
        }

        @Test
        fun `Collaborator can delete layer`() {
            assertDeleteLayer("collaborator")
        }

        @Test
        fun `Viewer can't delete layer`() {
            val corpus = TestUtil.createJobbedCorpus(config)
            performDeleteLayer(corpus.uuid, TestUtil.TAGGER, "viewer").andExpect {
                status { isForbidden() }
                match { it.resolvedException is UserUnauthorizedException }
            }
            assertTrue(getLayers(corpus.uuid).any { it.tagger.name == TestUtil.TAGGER })
        }

        @Test
        fun `Stranger can't delete layer`() {
            val corpus = TestUtil.createJobbedCorpus(config)
            performDeleteLayer(corpus.uuid, TestUtil.TAGGER, "stranger").andExpect {
                status { isForbidden() }
                match { it.resolvedException is UserUnauthorizedException }
            }
            assertTrue(getLayers(corpus.uuid).any { it.tagger.name == TestUtil.TAGGER })
        }

        @Test
        fun `Can't delete layer for non-existing corpus`() {
            performDeleteLayer(UUID.randomUUID(), TestUtil.TAGGER).andExpect {
                status { isNotFound() }
                match { it.resolvedException is CorpusNotFoundException }
            }
        }

        @Test
        fun `Can't delete existing layer without job`() {
            val corpus = TestUtil.createFilledCorpus(config)
            corpus.layers.createOrThrow(TestUtil.TAGGER)
            performDeleteLayer(corpus.uuid, TestUtil.TAGGER).andExpect {
                status { isNotFound() }
                match { it.resolvedException is JobNotFoundException }
            }
            assertTrue(getLayers(corpus.uuid).any { it.tagger.name == TestUtil.TAGGER })
        }
    }

    private fun performGetLayers(corpus: UUID, user: String = TestUtil.USER): ResultActionsDsl =
        mvc.get("/corpora/$corpus/layers") { headers { assignHeaders(this, user) } }

    private fun getLayers(corpus: UUID, user: String = TestUtil.USER): List<CorpusLayerMetadata> =
        performGetLayers(corpus, user)
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
            .andReturn()
            .andDeserialize()

    private fun performGetLayer(
        corpus: UUID,
        layer: String,
        user: String = TestUtil.USER,
    ): ResultActionsDsl =
        mvc.get("/corpora/$corpus/layers/$layer") { headers { assignHeaders(this, user) } }

    private fun getLayer(
        corpus: UUID,
        layer: String,
        user: String = TestUtil.USER,
    ): CorpusLayerMetadata =
        performGetLayer(corpus, layer, user)
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
            .andReturn()
            .andDeserialize()

    private fun performDeleteLayer(
        corpus: UUID,
        layer: String,
        user: String = TestUtil.USER,
    ): ResultActionsDsl =
        mvc.delete("/corpora/$corpus/layers/$layer") { headers { assignHeaders(this, user) } }
}
