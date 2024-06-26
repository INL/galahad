package org.ivdnt.galahad.taggers

import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.BaseFileSystemStore
import org.ivdnt.galahad.app.ExpensiveGettable
import org.ivdnt.galahad.app.application_profile
import org.ivdnt.galahad.data.document.SOURCE_LAYER_NAME
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor
import java.io.File
import java.net.URL

const val TAGGERS_DIR = "data/taggers"
class TaggerStore : BaseFileSystemStore (
        File( TAGGERS_DIR )
), Logging {

    val ids: List<String>
        get() = workDirectory.listFiles()
            ?.map { it.nameWithoutExtension }
            ?: throw Exception("Failed to get tagger ids")

    val taggers: List<ExpensiveGettable<Tagger>>
        get() = ids.map { getSummaryOrThrow(it, null) } // We can provide null since there is nothing like SOURCE_LAYER.yaml in ids

    fun getSummaryOrThrow(tagger: String, sourceLayerTagger: ExpensiveGettable<Tagger>? ) = object : ExpensiveGettable<Tagger> {
        override fun expensiveGet(): Tagger {
            return getSummaryOrNull( tagger, sourceLayerTagger ).expensiveGet() ?: throw Exception("Failed to read tagger $tagger")
        }
    }

    /**
     * @param sourceLayer since SOURCE_LAYER_NAME never corresponds to a valid .yaml file, but it may be considered a 'tagger' in the sense that there
     * exists a resulting job, the cleanest solution is to require explicit definition of the desired return value in case of SOURCE_LAYER_NAME
     */
    fun getSummaryOrNull(tagger: String, sourceLayerTagger: ExpensiveGettable<Tagger>? ) = object : ExpensiveGettable<Tagger?> {
        override fun expensiveGet(): Tagger? {
            if( tagger == SOURCE_LAYER_NAME )  return sourceLayerTagger?.expensiveGet() // throw Exception("Don't use this for sourceLayer")
            val file = workDirectory.resolve( "$tagger.yaml" )
            return try {
                Yaml(Constructor(Tagger::class.java, LoaderOptions())).load<Tagger>( file.inputStream() )
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

}