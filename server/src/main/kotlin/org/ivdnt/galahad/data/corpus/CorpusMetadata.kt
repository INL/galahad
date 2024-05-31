package org.ivdnt.galahad.data.corpus

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import org.ivdnt.galahad.app.JSONable
import java.net.URL
import java.util.*

/**
 * Metadata about a corpus, to be stored in a cache file,
 * as its immutable fields can become invalidated (e.g. [numDocs], [lastModified]),
 * and of course any [MutableCorpusMetadata] field.
 * This is the superset of [MutableCorpusMetadata], and contains,
 * in addition to the mutable fields of the latter, any immutable fields like [sizeInBytes].
 * [sizeInBytes] is expensive to calculate, hence the cache file.
 */
class CorpusMetadata(
    // Mutable fields
    @JsonProperty("owner") owner: String = "",
    @JsonProperty("name") name: String = "",
    @JsonProperty("eraTo") eraTo: Int = 0,
    @JsonProperty("eraFrom") eraFrom: Int = 0,
    @JsonProperty("tagset") tagset: String? = null,
    @JsonProperty("dataset") @JsonInclude(JsonInclude.Include.ALWAYS) dataset: Boolean = false,
    @JsonProperty("public") @JsonInclude(JsonInclude.Include.ALWAYS) public: Boolean = false,
    @JsonProperty("collaborators") collaborators: Set<String> = setOf(),
    @JsonProperty("viewers") viewers: Set<String> = setOf(),
    @JsonProperty("sourceName") sourceName: String? = null,
    @JsonProperty("sourceURL") sourceURL: URL? = null,
    // Immutable fields
    @JsonProperty("uuid") val uuid: UUID = UUID(0, 0),
    @JsonProperty("activeJobs") val activeJobs: Int = 0,
    @JsonProperty("numDocs") val numDocs: Int = 0,
    @JsonProperty("sizeInBytes") val sizeInBytes: Long = 0,
    @JsonProperty("lastModified") val lastModified: Long = 0,
    ) : MutableCorpusMetadata(
        // Note that we set isPublic the same as isDataset.
        owner, name, eraFrom, eraTo, tagset, dataset, dataset, collaborators, viewers, sourceName, sourceURL
), JSONable