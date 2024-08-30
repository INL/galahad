package org.ivdnt.galahad.taggers

import org.ivdnt.galahad.TestConfig
import org.ivdnt.galahad.app.GalahadApplication
import org.ivdnt.galahad.taggers.TaggersController.TaggerHealthStatus
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.ContextConfiguration

@WebMvcTest(properties = ["spring.main.allow-bean-definition-overriding=true"])
@ContextConfiguration(classes = [GalahadApplication::class])
class TaggerStoreControllerTest(
    @Autowired val ctrl: TaggersController
) {

    @Test
    fun getTaggers() {
        val taggers = ctrl.getTaggers()
        assertEquals(1, taggers.count { it.id == TestConfig.TAGGER_NAME })
    }

    @Test
    fun `Get valid tagger`() {
        val tagger = ctrl.getTagger(TestConfig.TAGGER_NAME)
        assertNotNull(tagger)
        assertEquals(TestConfig.TAGGER_NAME, tagger?.id)
    }

    @Test
    fun `Get invalid tagger`() {
        val tagger = ctrl.getTagger("invalid")
        assertNull(tagger)
    }

    @Test
    fun `Get health of invalid tagger`() {
        assertEquals(TaggerHealthStatus.ERROR,ctrl.getTaggerHealth("invalid").status)
    }
}