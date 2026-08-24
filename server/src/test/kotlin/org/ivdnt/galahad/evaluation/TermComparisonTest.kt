package org.ivdnt.galahad.evaluation

import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.Term
import org.ivdnt.galahad.evaluation.comparison.TermComparison
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TermComparisonTest {
    @Nested
    inner class LemmaPosTest {
        // Default values would make the terms equal.
        private fun assertTerm(
            lemmaEqual: Boolean = true,
            posEqual: Boolean = true,
            hypoLemma: String? = "school",
            refLemma: String? = "school",
            hypoPos: String? = "NOU",
            refPos: String? = "NOU",
        ) {
            val hypoTerm =
                Term(
                    "",
                    mapOf(
                        Annotation.TOKEN to "dummy",
                        Annotation.LEMMA to hypoLemma,
                        Annotation.POS to hypoPos,
                    ),
                )
            val refTerm =
                Term(
                    "",
                    mapOf(
                        Annotation.TOKEN to "dummy",
                        Annotation.LEMMA to refLemma,
                        Annotation.POS to refPos,
                    ),
                )
            TermComparison(hypoTerm, refTerm).apply {
                assertEquals(lemmaEqual, equal(Annotation.LEMMA))
                assertEquals(posEqual, equal(Annotation.POS))
            }
        }

        @Test
        fun `Equal lemma-pos`() {
            assertTerm() // default values
            // That includes being equal as empty or null.
            assertTerm(hypoLemma = "", refLemma = "")
            assertTerm(hypoLemma = null, refLemma = null)
            assertTerm(hypoPos = "", refPos = "")
            assertTerm(hypoPos = null, refPos = null)
        }

        @Test
        fun `Different lemma-pos`() {
            assertTerm(lemmaEqual = false, refLemma = "scholen")
            assertTerm(posEqual = false, refPos = "ADJ")
        }

        @Test
        fun `Hypothesis lemma-pos is empty or null`() {
            assertTerm(lemmaEqual = false, hypoLemma = "")
            assertTerm(lemmaEqual = false, hypoLemma = null)
            assertTerm(posEqual = false, hypoPos = "")
            assertTerm(posEqual = false, hypoPos = null)
        }

        // If no reference lemma-pos is defined, any hypothesis is fine.
        // For example, the reference is punctuation with no lemma-pos defined.
        // Some taggers (=hypothesis) do add a lemma-pos. So we'll allow it.
        @Test
        fun `Reference lemma-pos is empty or null`() {
            assertTerm(refLemma = "")
            assertTerm(refLemma = null)
            assertTerm(refPos = "")
            assertTerm(refPos = null)

            // Note that the hypothesis can even be empty or null. Anything is fine.
            assertTerm(hypoLemma = "", refLemma = null)
            assertTerm(hypoLemma = null, refLemma = "")
            assertTerm(hypoPos = "", refPos = null)
            assertTerm(hypoPos = null, refPos = "")
        }
    }
}
