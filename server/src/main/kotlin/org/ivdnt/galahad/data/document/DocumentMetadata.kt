package org.ivdnt.galahad.data.document

import com.fasterxml.jackson.annotation.JsonProperty
import org.ivdnt.galahad.data.layer.LayerSummary

class DocumentMetadata(
    /** Name of the uploaded file including extension. Used as a working directory name. */
    @JsonProperty("name") val name: String,
    /** Format of the uploaded file as induced by FormatInducer. */
    @JsonProperty("format") val format: String,
    /** Whether the document's plaintext exist and its format has been identified.
     * Invalid documents should have been auto-deleted. */
    @JsonProperty("valid") val valid: Boolean,
    /** Number of chars in the parsed plaintext. */
    @JsonProperty("numChars") val numChars: Int,
    /** Number of alphabetic chars in the parsed plaintext. */
    @JsonProperty("numAlphabeticChars") val numAlphabeticChars: Int,
    /** A truncated preview of the parsed plaintext. */
    @JsonProperty("preview") val preview: String,
    /** Some statistics about the source annotations, if present */
    @JsonProperty("layerSummary") val layerSummary: LayerSummary,
    @JsonProperty("lastModified") val lastModified: Long,
) {
    companion object {
        // Used as an initialization value for document metadata cache.
        val EMPTY = DocumentMetadata(
            "", "", false, 0, 0, "", LayerSummary(), 0
        )
    }
}