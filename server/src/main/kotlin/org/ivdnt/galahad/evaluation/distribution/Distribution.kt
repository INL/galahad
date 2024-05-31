package org.ivdnt.galahad.evaluation.distribution

import com.fasterxml.jackson.annotation.JsonIgnore
import org.ivdnt.galahad.data.layer.Term

/**
 * Generic class for frequency distributions of terms in a corpus or document.
 * The idea is to sum up the distribution as we go through the terms one by one using [add].
 */
open class Distribution {

    /**
     * (lem, pos) -> (count, literal[])
     */
    @JsonIgnore
    val distributionMap: MutableMap<Pair<String, String>, Pair<Int, LiteralsEntry>> = HashMap()

    var isTrimmed = false
    var coveredChars = 0
    var coveredAlphabeticChars = 0
    var totalChars = 0
    var totalAlphabeticChars = 0

    /**
     * Is serialized and send through API, so it is in fact used.
     */
    @Suppress("unused")
    val distribution: Set<DistributionRow>
        get() = distributionMap.entries.map {
                DistributionRow(
                    it.key.first,
                    it.key.second,
                    it.value.first,
                    it.value.second
                )
            }.toSet()

    fun add(term: Term) {
        val literal: String = term.literals
        coveredChars += literal.length
        coveredAlphabeticChars += literal.count { char -> char.isLetter() }
        add(
            lemma = term.lemma ?: Term.NO_LEMMA,
            pos = term.posHeadGroup ?: Term.NO_POS,
            count = 1,
            literals = LiteralsEntry(mapOf(term.literals to 1))
        )
    }

    private fun add(lemma: String, pos: String, count: Int, literals: LiteralsEntry) {
        distributionMap.merge(
            Pair(lemma, pos), Pair(count, literals)
        ) { p1, p2 -> Pair(p1.first + p2.first, p1.second.add(p2.second)) }
    }

    fun add(other: Distribution) {
        coveredChars += other.coveredChars
        coveredAlphabeticChars += other.coveredAlphabeticChars
        totalChars += other.totalChars
        totalAlphabeticChars += other.totalAlphabeticChars
        other.distributionMap.forEach {
            add(it.key.first, it.key.second, it.value.first, it.value.second)
        }
    }

    fun toCSV(): String {
        var csv: String = DistributionRow.getCsvHeader()
        getSorted().forEach { csv += it.toCSVRecord() }
        return csv
    }

    @JsonIgnore
    fun getSorted(): Set<DistributionRow> {
        return distributionMap.toList()
            // Sort on count (descending, hence minus), then on lemma.
            // Note that 'it' is of type Pair< Pair<String, String> , Pair<Int, LiteralsEntry> >
            .sortedWith(compareBy({ -it.second.first }, { it.first.first.lowercase() })).toMap()
            // Map to serializable rows.
            .entries.map { DistributionRow(it.key.first, it.key.second, it.value.first, it.value.second) }.toSet()
    }

    fun trim(maxSize: Int, minVal: Int = 2): Distribution {
        if (distributionMap.size > maxSize) {
            isTrimmed = true
            distributionMap.entries.removeIf { it.value.first < minVal }
            trim(maxSize, minVal + 1) // recursion ftw
        }
        return this
    }
}