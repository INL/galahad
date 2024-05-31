package org.ivdnt.galahad.evaluation

import org.ivdnt.galahad.data.layer.Term
import org.ivdnt.galahad.evaluation.comparison.TermComparison
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

class EvaluationEntryTest {

    @Test
    fun `Add without samples`() {
        val a = EvaluationEntry(1, mutableListOf())
        val b = EvaluationEntry(1, mutableListOf())
        val result = EvaluationEntry.add(a, b)
        assertEquals(2, result.count)
        assertEquals(0, result.jsonSamples.size)
        assertEquals(0, result.samples.size)
    }

    @Test
    fun `Add with samples below 10`() {
        val a = EvaluationEntry(3, mutableListOf(TermComparison(Term.EMPTY, Term.EMPTY)))
        val b = EvaluationEntry(4, mutableListOf(TermComparison(Term.EMPTY, Term.EMPTY)))
        val result = EvaluationEntry.add(a, b)
        assertEquals(7, result.count)
        assertEquals(2, result.jsonSamples.size)
        assertEquals(2, result.samples.size)
    }

    @Test
    fun `Add with samples above 10 and truncate`() {
        val a = EvaluationEntry(7, MutableList(7) { TermComparison(Term.EMPTY, Term.EMPTY) })
        val b = EvaluationEntry(9, MutableList(9) { TermComparison(Term.EMPTY, Term.EMPTY) })
        var result = EvaluationEntry.add(a, b)
        assertEquals(16, result.count)
        assertEquals(10, result.jsonSamples.size)
        assertEquals(16, result.samples.size)
        val c = EvaluationEntry(3, MutableList(3) { TermComparison(Term.EMPTY, Term.EMPTY) })
        result = EvaluationEntry.add(result, c)
        assertEquals(19, result.count)
        assertEquals(10, result.jsonSamples.size)
        assertEquals(16, result.samples.size) // should not grow once the truncation limit is reached
    }

    @Test
    fun `Add with samples above 10 without truncation`() {
        fun samples(n: Int): EvaluationEntry = EvaluationEntry(samples = MutableList(n) { TermComparison(Term.EMPTY, Term.EMPTY) })
        val a = samples(7)
        val b = samples(9)
        val c = samples(3)
        var result = EvaluationEntry.add(a, b, false)
        assertEquals(16, result.samples.size)
        result = EvaluationEntry.add(result, c, false)
        assertEquals(19, result.samples.size) // grows
    }
}