package org.ivdnt.galahad.tagset

import com.fasterxml.jackson.annotation.JsonProperty

typealias Tag = String

class Tagset {
    // The name should be equal to the filename
    // i.e. mytagset.yaml should have identifier 'mytagset'
    // This ought te be set when loading from file
    @JsonProperty
    var identifier: String = ""

    @JsonProperty
    var longName: String = ""


    @JsonProperty
    var shortName: String = ""

    @JsonProperty
    var punctuationTags: HashSet<Tag> = HashSet()

    companion object {

        val UNKNOWN: Tagset
            get() {
                val t = Tagset()
                t.identifier = "UNKNOWN"
                t.longName = "Unknown Tagset"
                t.shortName = "UNK"
                return t
            }
    }

}