package org.ivdnt.galahad.taggers

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.BaseFileSystemStore
import org.ivdnt.galahad.app.ExpensiveGettable
import org.ivdnt.galahad.app.JSONable
import org.ivdnt.galahad.app.application_profile
import org.ivdnt.galahad.data.document.SOURCE_LAYER_NAME
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor
import java.io.File
import java.net.URL

const val TAGGERS_DIR = "data/taggers"
class Taggers : BaseFileSystemStore (
        File( TAGGERS_DIR )
), Logging {

    val ids: List<String>
        get() = workDirectory.listFiles()
            ?.map { it.nameWithoutExtension }
            ?: throw Exception("Failed to get tagger ids")

    val summaries: List<ExpensiveGettable<Summary>>
        get() = ids.map { getSummaryOrThrow(it, null) } // We can provide null since there is nothing like SOURCE_LAYER.yaml in ids

    fun getSummaryOrThrow( tagger: String, sourceLayerSummary: ExpensiveGettable<Summary>? ) = object : ExpensiveGettable<Summary> {
        override fun expensiveGet(): Summary {
            return getSummaryOrNull( tagger, sourceLayerSummary ).expensiveGet() ?: throw Exception("Failed to read tagger $tagger")
        }
    }

    /**
     * @param sourceLayer since SOURCE_LAYER_NAME never corresponds to a valid .yaml file, but it may be considered a 'tagger' in the sense that there
     * exists a resulting job, the cleanest solution is to require explicit definition of the desired return value in case of SOURCE_LAYER_NAME
     */
    fun getSummaryOrNull( tagger: String, sourceLayerSummary: ExpensiveGettable<Summary>? ) = object : ExpensiveGettable<Summary?> {
        override fun expensiveGet(): Summary? {
            if( tagger == SOURCE_LAYER_NAME )  return sourceLayerSummary?.expensiveGet() // throw Exception("Don't use this for sourceLayer")
            val file = workDirectory.resolve( "$tagger.yaml" )
            return try {
                Yaml(Constructor(Summary::class.java, LoaderOptions())).load<Summary>( file.inputStream() )
            } catch ( e:Exception ) {
                logger.error("Failed to read tagger ${file.name} from file. Exception $e")
                return null
            }
        }
    }

    fun getURL( tagger: String ): URL {
        return if(application_profile.contains("dev") ) {
            val summary = getSummaryOrThrow(tagger, null).expensiveGet()
            URL("http://localhost:${summary.devport}")
        } else {
            URL("http://$tagger:8080")
        }
    }

    class Summary (
        // The id should be equal to the filename
        // i.e. mytagger.yaml should have id 'mytagger'
        // This ought te be set when loading from file
        // This name will be used as hostname
        // So can only contain certain characters
        @JsonProperty("id") var id: String = "",
        @JsonProperty("description") var description: String = "",
        @JsonProperty("tagset") var tagset: String? = null,
        @JsonProperty("eraFrom") var eraFrom: Int = 0,
        @JsonProperty("eraTo") var eraTo: Int = 0,
        @JsonProperty("produces") var produces: Set<String> = setOf(),
        @JsonProperty("model") var model: LinkItem = LinkItem(),
        @JsonProperty("software") var software: LinkItem = LinkItem(),
        @JsonProperty("dataset") var dataset: LinkItem = LinkItem(),
        @JsonProperty("trainedBy") var trainedBy: String = "",
        @JsonProperty("date") var date: String = "",
    ) : JSONable {
        @JsonIgnore var devport: Int? = 0

        class LinkItem (
            @JsonProperty("name") var name: String = "",
            @JsonProperty("href") var href: String = ""
        )
    }

}