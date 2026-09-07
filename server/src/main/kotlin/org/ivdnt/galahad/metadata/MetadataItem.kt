package org.ivdnt.galahad.metadata

import java.net.URI

/** Generic item to describe resources or simple data in metadata profiles. */
data class MetadataItem(
    /** The name of the data or resource. */
    var name: String? = null,
    /** A short description of the resource or the data value. */
    var description: String? = null,
    /** The URL pointing to the resource. */
    var url: URI? = null,
)
