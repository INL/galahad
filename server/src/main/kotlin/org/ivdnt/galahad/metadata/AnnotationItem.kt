package org.ivdnt.galahad.metadata

import org.ivdnt.galahad.annotations.Annotation

/** Item to describe the principles guiding a linguistic annotation. */
data class AnnotationItem(
    /** The linguistic annotation described by this item. */
    var annotation: Annotation? = null,
    /** The principles guiding the annotation. */
    var principles: List<MetadataItem>? = null,
)
