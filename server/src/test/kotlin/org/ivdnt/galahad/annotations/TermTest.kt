package org.ivdnt.galahad.annotations

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TermTest {
    // upos
    private val singleUPos =
        Term("", mapOf(Annotation.TOKEN to "token", Annotation.UPOS to "NOU-C(num=sg)"))

    // ner
    private val singleNer = Term("", mapOf(Annotation.TOKEN to "token", Annotation.NER to "B-LOC"))

    // pos
    private val singlePos =
        Term("", mapOf(Annotation.TOKEN to "token", Annotation.POS to "NOU-C(num=sg)"))

    private val multiPos =
        Term(
            "",
            mapOf(Annotation.TOKEN to "token", Annotation.POS to "PD(type=art)+NOU-C(num=sg)"),
        )

    private val headOnlyPos =
        Term("", mapOf(Annotation.TOKEN to "token", Annotation.POS to "NOU-C"))

    @Test
    fun `PoS head and features`() {
        // single pos
        assertEquals("NOU-C", singlePos.annotationHead(Annotation.POS))
        assertEquals("num=sg", Term.features(singlePos.pos))

        // upos
        assertEquals("NOU-C", singleUPos.annotationHead(Annotation.UPOS))
        assertEquals("num=sg", Term.features(singleUPos.upos))

        // ner
        assertEquals("LOC", singleNer.annotationHead(Annotation.NER))

        // multi pos
        assertEquals("PD+NOU-C", multiPos.annotationHead(Annotation.POS))
        // no features for now...

        // head only pos
        assertEquals("NOU-C", headOnlyPos.annotationHead(Annotation.POS))
        assertEquals(null, Term.features(headOnlyPos.pos))
    }
}
