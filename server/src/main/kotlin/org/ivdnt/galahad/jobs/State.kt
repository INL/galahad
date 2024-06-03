package org.ivdnt.galahad.jobs

import com.fasterxml.jackson.annotation.JsonProperty
import org.ivdnt.galahad.data.layer.LayerPreview
import org.ivdnt.galahad.data.layer.LayerSummary
import org.ivdnt.galahad.taggers.Taggers

/**
 * Cache-able job metadata.
 */
class State(
    @JsonProperty("tagger") val tagger: Taggers.Summary = Taggers.Summary(),
    @JsonProperty("progress") val progress: Progress = Progress(),
    @JsonProperty("preview") val preview: LayerPreview = LayerPreview(),
    @JsonProperty("resultSummary") val resultSummary: LayerSummary = LayerSummary(),
    @JsonProperty("lastModified") var lastModified: Long? = null,
)