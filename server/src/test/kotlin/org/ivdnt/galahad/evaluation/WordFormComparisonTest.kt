package org.ivdnt.galahad.evaluation

import org.ivdnt.galahad.data.layer.WordForm
import org.ivdnt.galahad.evaluation.comparison.WordFormComparison
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

class WordFormComparisonTest {
    val long: String = "very_long_word"
    val short: String = "short"

    // A is generated
    private fun wfAt(offset: Int) = WordForm(short, offset, short.length, "")

    // B is constant
    val b: WordForm = WordForm(long, 0, long.length, "")

    private fun assertSymmetricPartialOverlapOnly(a: WordForm, b: WordForm) {
        WordFormComparison(a, b).apply {
            assertFalse(fullOverlap)
            assertTrue(partialOverlap)
        }
        WordFormComparison(b, a).apply {
            assertFalse(fullOverlap)
            assertTrue(partialOverlap)
        }
    }

    @Test
    fun `A starts before B and ends before B`() {
        val a = wfAt(-2)
        assertTrue(a.offset < b.offset)
        assertTrue(a.endOffset < b.endOffset)
        assertSymmetricPartialOverlapOnly(a, b)
    }

    @Test
    fun `A starts at B and ends before B`() {
        val a = wfAt(0)
        assertTrue(a.offset == b.offset)
        assertTrue(a.endOffset < b.endOffset)
        assertSymmetricPartialOverlapOnly(a, b)
    }

    @Test
    fun `A after B and ends before B`() {
        val a = wfAt(2)
        assertTrue(a.offset > b.offset)
        assertTrue(a.endOffset < b.endOffset)
        assertSymmetricPartialOverlapOnly(a, b)
    }

    @Test
    fun `A starts after B and ends at B`() {
        val a = wfAt(9)
        assertTrue(a.offset > b.offset)
        assertTrue(a.endOffset == b.endOffset)
        assertSymmetricPartialOverlapOnly(a, b)
    }

    @Test
    fun `A starts at B's end and ends after B`() {
        val a = wfAt(14)
        assertTrue(a.offset == b.endOffset)
        assertTrue(a.endOffset > b.endOffset)
        assertSymmetricPartialOverlapOnly(a, b)
    }

    @Test
    fun `A is B`() {
        val a = WordForm(long, 0, long.length, "")
        WordFormComparison(a, b).apply {
            assertTrue(fullOverlap)
            assertFalse(partialOverlap)
        }
    }
}