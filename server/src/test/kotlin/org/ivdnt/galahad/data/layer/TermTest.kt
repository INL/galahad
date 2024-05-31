package org.ivdnt.galahad.data.layer

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TermTest {
    // Only the relevant parameters are initialized.
    private val singleTarget = Term(
        "", "", mutableListOf(WordForm("schooltje", 0, 0, "0"))
    )
    private val multiTarget = Term(
        "", "", mutableListOf(
            WordForm("hoogbouw", 0, 0, "0"), WordForm("en", 0, 0, "0"), WordForm("laagbouw", 0, 0, "0")
        )
    )

    private val multiPos = Term("", "PD(type=art)+NOU-C(num=sg)", mutableListOf())
    private val singlePos = Term("", "NOU-C(num=sg)", mutableListOf())



    @Test
    fun `Single PoS Head`() {
        assertEquals("NOU-C", singlePos.posHead)
        assertEquals("NOU-C", singlePos.posHeadGroup)

    }

    @Test
    fun `Single PoS Features`() {
        assertEquals("num=sg", singlePos.posFeatures)
    }

    @Test
    fun `Multi PoS Head`() {
        assertEquals("PD", multiPos.posHead)
        assertEquals("PD+NOU-C", multiPos.posHeadGroup)
    }

    @Test
    fun `Literals for multi target term`() {
        assertEquals("hoogbouw en laagbouw", multiTarget.literals)
    }

    @Test
    fun `Literals for single target term`() {
        assertEquals("schooltje", singleTarget.literals)
    }
}