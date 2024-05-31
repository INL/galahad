package org.ivdnt.galahad.data.document

import com.fasterxml.jackson.annotation.JsonCreator

/**
 * The DocumentFormat will be used to declare the format for indexing in BlackLab and
 * is used to switch between the different parsers to extract plaintext and merge layers.
 */
enum class DocumentFormat(val identifier: String, val extension: String) {
    TeiP4Legacy("tei-p4-legacy", "xml"),
    TeiP5Legacy("tei-p5-legacy", "xml"),
    TeiP5("tei-p5", "tei.xml"),
    Naf("naf", "naf.xml"),
    Tsv("tsv", "tsv"),
    Conllu("conllu", "conllu"),
    Folia("folia", "folia.xml"),
    Txt("txt", "txt"),
    Unknown("unknown", "unknown");

    companion object {
        /**
         * BlackLab uses formats that contain -, which is not allowed in an enum, so we need this mapping
         */
        @JsonCreator
        fun fromString(s: String): DocumentFormat = entries.firstOrNull { it.identifier == s } ?: throw Exception(
            "Invalid format $s, valid formats are ${entries.map { it.identifier }}"
        )
    }
}