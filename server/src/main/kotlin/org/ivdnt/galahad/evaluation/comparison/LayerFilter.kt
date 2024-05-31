package org.ivdnt.galahad.evaluation.comparison

interface LayerFilter {
    val hypoTermFilter: TermFilter
    val refTermFilter: TermFilter
    fun filter(comp: TermComparison): Boolean
}

class ConfusionLayerFilter(
    override val hypoTermFilter: TermFilter,
    override val refTermFilter: TermFilter
) : LayerFilter {
    override fun filter(comp: TermComparison): Boolean {
        return hypoTermFilter.filter(comp.hypoTerm) && refTermFilter.filter(comp.refTerm)
    }
}

class MetricsLayerFilter(
    override val hypoTermFilter: TermFilter,
    override val refTermFilter: TermFilter
) : LayerFilter {
    override fun filter(comp: TermComparison): Boolean {
        return hypoTermFilter.filter(comp.hypoTerm) || refTermFilter.filter(comp.refTerm)
    }
}