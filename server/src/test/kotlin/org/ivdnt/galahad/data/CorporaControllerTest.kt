package org.ivdnt.galahad.data

import org.ivdnt.galahad.JSON
import org.ivdnt.galahad.TestConfig
import org.ivdnt.galahad.UserHeader
import org.ivdnt.galahad.app.Config
import org.ivdnt.galahad.app.GalahadApplication
import org.ivdnt.galahad.data.corpus.CorpusMetadata
import org.ivdnt.galahad.data.corpus.MutableCorpusMetadata
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import java.nio.charset.StandardCharsets
import java.util.*

@WebMvcTest(properties = ["spring.main.allow-bean-definition-overriding=true"])
@ContextConfiguration(classes = [GalahadApplication::class, TestConfig::class])
class CorporaControllerTest(
    @Autowired val mvc: MockMvc,
    @Autowired val config: Config,
    @Autowired val ctrl: CorporaController,
) {

    @Test
    fun `Post unicode name corpora`() {
        val name = "日本語"
        val meta = MutableCorpusMetadata("", name, 0, 0, null, false, false, null, null, null, null)

        val uuid = postCorpus(meta)

        val metaResponse = getCorpus(uuid)
        assertEquals(name, metaResponse.name)

        assertEquals(1, getAllCorpora().size)
        // Delete it
        deleteCorpus(uuid)
        assertEquals(0, getAllCorpora().size)
    }

    @Test
    fun `Test owner, collaborators and viewers`() {
        val owner = "testUser"
        val collabs = setOf("collab1", owner)
        val viewers = setOf("viewer1", "collab1", owner)

        // Create
        val meta = MutableCorpusMetadata(
            owner = "", // Set by request header
            "test", 0, 0, null, false, false, collabs, viewers, null, null
        )
        val uuid = postCorpus(meta)

        // Check if created correctly, i.e. no permission overlap.
        // first try to get it as a stranger
        assertThrows(Exception::class.java) { getCorpus(uuid, "stranger") }
        // then as a viewer
        val metaResponse = getCorpus(uuid)
        assertEquals(setOf("collab1"), metaResponse.collaborators)
        assertEquals(setOf("viewer1"), metaResponse.viewers)
        assertEquals(owner, metaResponse.owner)
        assertEquals(1, getAllCorpora().size)

        // Update with new collaborators
        val moreSharers = meta.also {
            it.collaborators = setOf("collab1", "collab2")
            it.viewers = setOf("viewer1", "viewer2")
        }

        // Try to update as viewer
        assertThrows(Exception::class.java) { patchCorpus(uuid, moreSharers, "viewer1") }
        // Try to update as collaborator
        val moreSharersResponse = patchCorpus(uuid, moreSharers)

        // Check if updated correctly
        assertEquals(setOf("collab1", "collab2"), moreSharersResponse?.collaborators)
        assertEquals(setOf("viewer1", "viewer2"), moreSharersResponse?.viewers)

        // Let viewer2 remove themselves
        val viewer2gone = meta.also {
            it.collaborators = setOf("collab1", "collab2")
            it.viewers = setOf("viewer1")
        }
        assertDoesNotThrow { patchCorpus(uuid, viewer2gone, "viewer2") }

        // Try to delete it as viewer1
        assertThrows(Exception::class.java) { deleteCorpus(uuid, "viewer1") }
        assertEquals(1, getAllCorpora().size) // Should still be there

        // Delete it as collab1
        assertThrows(Exception::class.java) { deleteCorpus(uuid, "collab1") }
        assertEquals(1, getAllCorpora().size) // Should still be there

        // Delete it as the owner
        deleteCorpus(uuid)
        assertEquals(0, getAllCorpora().size) // Gone
    }

    private fun deleteCorpus(uuid: UUID?, username: String = "testUser") {
        mvc.perform(
            MockMvcRequestBuilders.delete("/corpora/$uuid").headers(UserHeader.get(username))
        )
    }

    private fun postCorpus(meta: MutableCorpusMetadata): UUID? {
        val result: MvcResult = mvc.perform(
            MockMvcRequestBuilders.post("/corpora").headers(UserHeader.get())
                // utf-8
                .characterEncoding("utf-8").contentType("application/json").content(JSON.toStr(meta))
        ).andReturn()
        return UUID.fromString(JSON.fromStr<String>(result.response.getContentAsString(StandardCharsets.UTF_8)))
    }

    private fun patchCorpus(uuid: UUID?, meta: MutableCorpusMetadata, username: String = "testUser"): CorpusMetadata? {
        val result: MvcResult = mvc.perform(
            MockMvcRequestBuilders.patch("/corpora/$uuid").headers(UserHeader.get(username))
                // utf-8
                .characterEncoding("utf-8").contentType("application/json").content(JSON.toStr(meta))
        ).andReturn()
        // Patch doesn't always return a value.
        return try {
            JSON.fromStr<CorpusMetadata>(result.response.getContentAsString(StandardCharsets.UTF_8))
        } catch (e: Exception) {
            null
        }
    }


    private fun getCorpus(uuid: UUID?, username: String = "testUser"): CorpusMetadata {
        val corpusResponse = mvc.perform(
            MockMvcRequestBuilders.get("/corpora/$uuid").headers(UserHeader.get(username))
        ).andReturn().response.getContentAsString(StandardCharsets.UTF_8)
        return JSON.fromStr<CorpusMetadata>(corpusResponse)
    }

    private fun getAllCorpora(): List<CorpusMetadata> {
        val result: MvcResult = mvc.perform(
            MockMvcRequestBuilders.get("/corpora").headers(UserHeader.get())
        ).andReturn()
        return JSON.fromStr<List<CorpusMetadata>>(result.response.getContentAsString(StandardCharsets.UTF_8))
    }
}