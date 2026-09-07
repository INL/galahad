package org.ivdnt.galahad.metadata

/** Time period of a corpus or tagger in years. */
data class Period(
    /** Start year of the period. */
    var from: Int = 0,
    /** End year of the period. */
    var to: Int = 0,
)
