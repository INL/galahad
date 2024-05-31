package org.ivdnt.galahad.evaluation.metrics

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.ivdnt.galahad.data.layer.Term
import org.ivdnt.galahad.evaluation.comparison.TermComparison

interface MetricsSettings {
    /** When are terms equal? */
    fun termsEqual(comp: TermComparison): Boolean

    /** What to group terms by */
    fun groupBy(term: Term): String

    /** What terms to keep in the metrics. Keep = return true */
    fun filterBy(term: TermComparison): Boolean = true

    @get:JsonIgnore
    val nullTerm: String

    @get:JsonProperty("id")
    val id: String

    @get:JsonProperty("annotation")
    val annotation: String

    @get:JsonProperty("group")
    val group: String
}

open class PosByPosMetricsSettings : MetricsSettings {
    override val id: String = "posByPos"
    override val annotation: String = "PoS"
    override val group: String = "PoS"
    override val nullTerm: String = Term.NO_POS

    override fun termsEqual(comp: TermComparison): Boolean {
        return comp.equalPOS
    }

    override fun groupBy(term: Term): String {
        return term.posHeadGroup ?: nullTerm
    }
}

class MultiPosByPosMetricsSettings : PosByPosMetricsSettings() {
    override val id: String = "multiPosByPos"
    override val annotation: String = "PoS (multiple)"
    override fun filterBy(term: TermComparison): Boolean {
        return term.refTerm.isMultiPos
    }
}

class SinglePosByPosMetricsSettings : PosByPosMetricsSettings() {
    override val id: String = "singlePosByPos"
    override val annotation: String = "PoS (single)"
    override fun filterBy(term: TermComparison): Boolean {
        return !term.refTerm.isMultiPos
    }
}

open class LemmaByLemmaMetricsSettings : MetricsSettings {
    override val id: String = "lemmaByLemma"
    override val annotation: String = "Lemma"
    override val group: String = "Lemma"
    override val nullTerm: String = Term.NO_LEMMA

    override fun termsEqual(comp: TermComparison): Boolean {
        return comp.equalLemma
    }

    override fun groupBy(term: Term): String {
        return term.lemma ?: nullTerm
    }
}

class MultiLemmaByLemmaMetricsSettings : LemmaByLemmaMetricsSettings() {
    override val id: String = "multiLemmaByLemma"
    override val annotation: String = "Lemma (multiple)"
    override fun filterBy(term: TermComparison): Boolean {
        return term.refTerm.lemma?.contains("+") ?: false
    }
}

class SingleLemmaByLemmaMetricsSettings : LemmaByLemmaMetricsSettings() {
    override val id: String = "singleLemmaByLemma"
    override val annotation: String = "Lemma (single)"
    override fun filterBy(term: TermComparison): Boolean {
        val isMulti = term.refTerm.lemma?.contains("+")  ?: false
        return !isMulti
    }
}

class LemmaByPosMetricsSettings : PosByPosMetricsSettings() {
    override val id: String = "lemmaByPos"
    override val annotation: String = "Lemma"
    override fun termsEqual(comp: TermComparison): Boolean {
        return comp.equalLemma
    }
}

class PosByLemmaMetricsSettings : LemmaByLemmaMetricsSettings() {
    override val id: String = "posByLemma"
    override val annotation: String = "PoS"
    override fun termsEqual(comp: TermComparison): Boolean {
        return comp.equalPOS
    }
}

class LemmaPosByPosMetricsSettings : PosByPosMetricsSettings() {
    override val id: String = "lemmaPosByPos"
    override val annotation: String = "Lemma + PoS"
    override fun termsEqual(comp: TermComparison): Boolean {
        return comp.equalPosLemma
    }
}

class LemmaPosByLemmaMetricsSettings : LemmaByLemmaMetricsSettings() {
    override val id: String = "lemmaPosByLemma"
    override val annotation: String = "Lemma + PoS"
    override fun termsEqual(comp: TermComparison): Boolean {
        return comp.equalPosLemma
    }
}

/** Used by [Metrics] to instantiate a [MetricsType] for each setting. */
val METRIC_TYPES = listOf(
    // Pos
    PosByPosMetricsSettings(),
    PosByLemmaMetricsSettings(),
    MultiPosByPosMetricsSettings(),
    SinglePosByPosMetricsSettings(),
    // Lemma
    LemmaByLemmaMetricsSettings(),
    LemmaByPosMetricsSettings(),
    MultiLemmaByLemmaMetricsSettings(),
    SingleLemmaByLemmaMetricsSettings(),
    // Lemma + Pos
    LemmaPosByPosMetricsSettings(),
    LemmaPosByLemmaMetricsSettings(),
)