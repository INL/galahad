package org.ivdnt.galahad.evaluation

import org.ivdnt.galahad.data.layer.Term
import org.ivdnt.galahad.data.layer.WordForm
import org.ivdnt.galahad.evaluation.comparison.TermComparison
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TermComparisonTest {

    // The overlap is more rigorously tested in WordFormComparisonTest.
    @Nested
    inner class OverlapTest {
        @Test
        fun `Full overlap`() {
            val target1 = WordForm("word", 0, 4, "")
            val target2 = WordForm("word", 0, 4, "")
            val term1 = Term("", "", mutableListOf(target1))
            val term2 = Term("", "", mutableListOf(target2))
            TermComparison(term1, term2).apply {
                assertTrue(fullOverlap)
                assertFalse(partialOverlap)
            }
        }

        @Test
        fun `Partial overlap`() {
            val target1 = WordForm("word", 0, 4, "")
            val target2 = WordForm("word", 2, 6, "")
            val term1 = Term("", "", mutableListOf(target1))
            val term2 = Term("", "", mutableListOf(target2))
            TermComparison(term1, term2).apply {
                assertFalse(fullOverlap)
                assertTrue(partialOverlap)
            }
        }
    }

    @Nested
    inner class LemmaPosTest {
        // Default values would make the terms equal.
        private fun assertTerm(
            lemmaEqual: Boolean = true, posEqual: Boolean = true,
            hypoLemma: String? = "school", refLemma: String? = "school",
            hypoPos: String? = "NOU", refPos: String? = "NOU",
        ) {
            val hypoTerm = Term(hypoLemma, hypoPos, mutableListOf())
            val refTerm = Term(refLemma, refPos, mutableListOf())
            TermComparison(hypoTerm, refTerm).apply {
                assertEquals(lemmaEqual, equalLemma)
                assertEquals(posEqual, equalPOS)
                assertEquals(lemmaEqual && posEqual, equalPosLemma)
            }
        }

        @Test
        fun `Equal lemma-pos`() {
            assertTerm() // default values
            // That includes being equal as empty or null.
            assertTerm(lemmaEqual = true, hypoLemma = "", refLemma = "")
            assertTerm(lemmaEqual = true, hypoLemma = null, refLemma = null)
            assertTerm(posEqual = true, hypoPos = "", refPos = "")
            assertTerm(posEqual = true, hypoPos = null, refPos = null)
        }

        @Test
        fun `Different lemma-pos`() {
            assertTerm(lemmaEqual = false, hypoLemma = "school", refLemma = "scholen")
            assertTerm(posEqual = false, hypoPos = "NOU", refPos = "ADJ")
        }

        @Test
        fun `Hypothesis lemma-pos is empty or null`() {
            assertTerm(lemmaEqual = false, hypoLemma = "", refLemma = "school")
            assertTerm(lemmaEqual = false, hypoLemma = null, refLemma = "school")
            assertTerm(posEqual = false, hypoPos = "", refPos = "NOU")
            assertTerm(posEqual = false, hypoPos = null, refPos = "NOU")
        }

        // If no reference lemma-pos is defined, any hypothesis is fine.
        // For example, the reference is punctuation with no lemma-pos defined.
        // Some taggers (=hypothesis) do add a lemma-pos. So we'll allow it.
        @Test
        fun `Reference lemma-pos is empty or null`() {
            assertTerm(lemmaEqual = true, hypoLemma = "school", refLemma = "")
            assertTerm(lemmaEqual = true, hypoLemma = "school", refLemma = null)
            assertTerm(posEqual = true, hypoPos = "NOU", refPos = "")
            assertTerm(posEqual = true, hypoPos = "NOU", refPos = null)

            // Note that the hypothesis can even be empty or null. Anything is fine.
            assertTerm(lemmaEqual = true, hypoLemma = "", refLemma = null)
            assertTerm(lemmaEqual = true, hypoLemma = null, refLemma = "")
            assertTerm(posEqual = true, hypoPos = "", refPos = null)
            assertTerm(posEqual = true, hypoPos = null, refPos = "")
        }
    }
}