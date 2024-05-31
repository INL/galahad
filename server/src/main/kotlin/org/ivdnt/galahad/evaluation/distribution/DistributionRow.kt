package org.ivdnt.galahad.evaluation.distribution

import org.ivdnt.galahad.port.csv.CSVFile

class LiteralsEntry(
    val literals: Map<String, Int>,
) {
    fun add(other: LiteralsEntry): LiteralsEntry {
        val ret = HashMap<String, Int>()
        literals.entries.forEach {
            ret[it.key] = it.value
        }
        other.literals.entries.forEach {
            ret.merge(it.key, it.value) { a, b -> a + b }
        }
        return LiteralsEntry(ret)
    }
}

class DistributionRow(
    val lemma: String,
    val pos: String,
    val count: Int,
    val literals: LiteralsEntry,
) {
    fun toCSVRecord(): String {
        return CSVFile.toCSVRecord(listOf(lemma, pos, count.toString(), literals.literals.toString()))
    }

    companion object {
        fun getCsvHeader(): String {
            return CSVFile.toCSVHeader(listOf("lemma", "pos", "count", "literals"))
        }
    }
}