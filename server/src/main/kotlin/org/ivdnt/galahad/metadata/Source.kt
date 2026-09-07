package org.ivdnt.galahad.metadata

import java.net.URI

/** Data source origin of a corpus. */
data class Source(
    /** Name of the data source. */
    var name: String?,
    /** URL of the data source. */
    var url: URI?,
)
