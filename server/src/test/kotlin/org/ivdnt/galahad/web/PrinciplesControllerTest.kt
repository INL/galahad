package org.ivdnt.galahad.web

import org.ivdnt.galahad.app.Galahad
import org.ivdnt.galahad.exceptions.PrincipleNotFoundException
import org.ivdnt.galahad.taggers.Principle
import org.ivdnt.galahad.util.TestConfig
import org.ivdnt.galahad.util.TestUtil
import org.ivdnt.galahad.util.andDeserialize
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

/** Web controller tests for serialization, status, exception resolving and permissions. */
@SpringBootTest(properties = ["spring.main.allow-bean-definition-overriding=true"])
@AutoConfigureMockMvc
@ContextConfiguration(classes = [Galahad::class, TestConfig::class])
class PrinciplesControllerTest(@Autowired val mvc: MockMvc) {
    @Test
    fun `Can get principles`() {
        val principles: List<Principle> =
            mvc.get("/principles")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                }
                .andReturn()
                .andDeserialize()
        assertEquals(1, principles.count { it.principle.name == TestUtil.TAGSET_NAME })
    }

    @Test
    fun `Can get single tagset`() {
        val principle: Principle =
            mvc.get("/principles/${TestUtil.TAGSET_NAME}")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                }
                .andReturn()
                .andDeserialize()
        assertEquals(TestUtil.TAGSET_NAME, principle.principle.name)
    }

    @Test
    fun `Can't get invalid tagset`() {
        mvc.get("/principles/invalid").andExpect {
            status { isNotFound() }
            match { it.resolvedException is PrincipleNotFoundException }
        }
    }
}
