package org.ivdnt.galahad.evaluation.comparison

import org.ivdnt.galahad.data.layer.Term

interface TermFilter {
    fun filter(term: Term): Boolean
}

class PosLemmaTermFilter (
    val posHeadGroup: String? = null,
    val lemma: String? = null,
) : TermFilter {
    val multiplePosFilter: (Term) -> Boolean = { t: Term -> t.isMultiPos }
    val singlePosFilter: (Term) -> Boolean = { t: Term -> t.posHeadGroupOrDefault == posHeadGroup }
    val posFilter: (Term) -> Boolean
    val lemmaFilter: (Term) -> Boolean

    init {
        posFilter = if (posHeadGroup == null) {
            { _:Term -> true}
        } else if (posHeadGroup.lowercase() == "multiple") {
            multiplePosFilter
        } else {
            singlePosFilter
        }

        lemmaFilter = if (lemma==null) {
            { _:Term -> true}
        } else {
            { t:Term -> t.lemmaOrDefault == lemma}
        }
    }

    override fun filter(term: Term): Boolean = posFilter(term) && lemmaFilter(term)
}

