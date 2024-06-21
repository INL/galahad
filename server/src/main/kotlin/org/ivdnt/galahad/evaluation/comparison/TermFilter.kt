package org.ivdnt.galahad.evaluation.comparison

import org.ivdnt.galahad.data.layer.Term
import org.ivdnt.galahad.evaluation.confusion.MULTIPLE_POS
import org.ivdnt.galahad.evaluation.confusion.OTHER_POS
import org.ivdnt.galahad.evaluation.confusion.OTHER_POS_REGEX

interface TermFilter {
    fun filter(term: Term): Boolean
}

class PosLemmaTermFilter (
    val posHeadGroup: String? = null,
    val lemma: String? = null,
) : TermFilter {
    private val multiplePosFilter: (Term) -> Boolean = { t: Term -> t.isMultiPos }
    private val otherPosFilter: (Term) -> Boolean = { t: Term -> t.pos?.matches(OTHER_POS_REGEX.toRegex()) ?: false}
    private val singlePosFilter: (Term) -> Boolean = { t: Term -> t.posHeadGroupOrDefault == posHeadGroup }
    val posFilter: (Term) -> Boolean
    val lemmaFilter: (Term) -> Boolean

    init {
        posFilter = when {
            (posHeadGroup == null) -> { _:Term -> true}
            (posHeadGroup.uppercase() == MULTIPLE_POS) -> multiplePosFilter
            (posHeadGroup.uppercase() == OTHER_POS) -> otherPosFilter
            else -> singlePosFilter
        }

        lemmaFilter = when {
            (lemma==null) ->  { _:Term -> true}
            else -> { t:Term -> t.lemmaOrDefault == lemma}
        }
    }

    override fun filter(term: Term): Boolean = posFilter(term) && lemmaFilter(term)
}

