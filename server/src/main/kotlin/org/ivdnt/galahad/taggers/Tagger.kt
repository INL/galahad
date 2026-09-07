package org.ivdnt.galahad.taggers

import com.fasterxml.jackson.annotation.JsonIgnore
import java.io.File
import java.net.URI
import org.ivdnt.galahad.annotations.Layer.Companion.SOURCE_LAYER
import org.ivdnt.galahad.app.application_profile
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.exceptions.TaggerNotFoundException
import org.ivdnt.galahad.metadata.AnnotationItem
import org.ivdnt.galahad.metadata.MetadataItem
import org.ivdnt.galahad.metadata.Period
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor

/** Metadata of a tagger (annotation tool). */
data class Tagger(
    /** Name of the tagger, used as docker hostname. */
    var name: String = "",
    /** Short description of the tagger and training data. */
    var description: String? = "",
    /** Language of the training data. */
    var language: String? = "", // TODO multiple languages
    /** Period covered by the training data. */
    var period: Period? = null,
    /** Annotations produced by the tagger, including principles guiding them. */
    var annotations: List<AnnotationItem> = emptyList(),
    /** Attributions related to the tagger, its training data and software. */
    var attributions: List<MetadataItem> = emptyList(),
    /** Hosted port for local development only. */
    @JsonIgnore var port: Int? = 0,
) {
    /** Comma-separated names of principles used by the tagger. */
    @get:JsonIgnore
    val principleNames: String
        get() = annotations.mapNotNull { it.principles }.flatten().joinToString { it.name!! }

    // Has to be a getter, because taggers are first initialized with an empty constructor,
    // and then filled from yaml, meaning that devport is 0 at the time of initialization.
    /** Base URL of the tagger API. */
    @get:JsonIgnore
    val url: URI
        get() =
            if ("dev" in application_profile) {
                URI("http://localhost:$port")
            } else {
                URI("http://$name:8080")
            }

    companion object {
        /** YAML data directory. */
        private const val TAGGERS_DIR: String = "data/taggers"

        /** List of taggers present in the data directory. */
        val taggers: Map<String, Tagger> =
            File(TAGGERS_DIR)
                .listFiles()
                .map {
                    Yaml(
                            Constructor(
                                Tagger::class.java,
                                LoaderOptions().apply { isEnumCaseSensitive = false },
                            )
                        )
                        .load<Tagger>(it.inputStream())
                }
                .associateBy { it.name }

        /** Read tagger by name or throw if absent. */
        fun readOrThrow(id: String): Tagger = taggers[id] ?: throw TaggerNotFoundException(id)

        /** Create a custom tagger of the source layer for a given corpus. */
        fun createSourceTagger(corpus: Corpus): Tagger {
            val metadata = corpus.metadata
            val produces =
                corpus.documents.readAll().flatMap { it.metadata.annotations.keys }.toSet()
            return Tagger(
                name = SOURCE_LAYER,
                description = "uploaded annotations",
                period = metadata.period,
                language = metadata.language,
                annotations = produces.map { AnnotationItem(it) },
            )
        }
    }
}
