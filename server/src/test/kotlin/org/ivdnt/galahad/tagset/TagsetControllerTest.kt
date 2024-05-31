package org.ivdnt.galahad.tagset

import org.ivdnt.galahad.app.GalahadApplication
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc

@WebMvcTest(properties = ["spring.main.allow-bean-definition-overriding=true"])
@ContextConfiguration(classes = [GalahadApplication::class])
class TagsetControllerTest(
    @Autowired val mvc: MockMvc,
    @Autowired val ctrl: TagsetController,
) {

    @Test
    fun `Get valid tagset`() {
        val tagset = ctrl.getTagset("TDN-Core")
        assertNotNull(tagset)
        assertTrue(tagset.punctuationTags.contains("PC"))
    }

    @Test
    fun `Get invalid tagset`() {
        assertThrows(Exception::class.java) { ctrl.getTagset("invalid") }
    }

    @Test
    fun `Get tagsets`() {
        val tagsets = ctrl.getTagsets()
        assertEquals(1, tagsets.count { it.identifier == "TDN-Core" })
    }
}