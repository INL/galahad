package org.ivdnt.galahad.evaluation.comparison

/**
 * Filter for [LayerComparison] to filter out [TermComparison], used for downloading only the
 * samples you want.
 */
interface LayerFilter {
    val hypTermFilter: TermFilter
    val refTermFilter: TermFilter

    fun filter(comp: TermComparison): Boolean
}

/**
 * In a confusion matrix, the samples are grouped by two groups, an annotation value for the
 * hypoTerm and one for the refTerm. E.g. the confusion between hypo:NOU and ref:VRB. Hence, a
 * logical AND.
 */
class ConfusionLayerFilter(
    override val hypTermFilter: TermFilter,
    override val refTermFilter: TermFilter,
) : LayerFilter {
    override fun filter(comp: TermComparison): Boolean =
        hypTermFilter.filter(comp.hyp) && refTermFilter.filter(comp.ref)
}

/**
 * In a metrics tables, the samples are grouped by a single group, an annotation value as it occurs
 * in either of the hypo-ref-pair. E.g. grouped by NOU: (hypo:NOU, ref:VRB) false positive, but also
 * (hypo:VRB, ref:NOU) false negative. Hence, a logical OR.
 */
class MetricsLayerFilter(
    override val hypTermFilter: TermFilter,
    override val refTermFilter: TermFilter,
) : LayerFilter {
    override fun filter(comp: TermComparison): Boolean =
        hypTermFilter.filter(comp.hyp) || refTermFilter.filter(comp.ref)
}

class DummyFilter : LayerFilter {
    override val hypTermFilter: TermFilter = DummyTermFilter()
    override val refTermFilter: TermFilter = DummyTermFilter()

    override fun filter(comp: TermComparison): Boolean = true
}
