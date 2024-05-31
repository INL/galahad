package org.ivdnt.galahad.tagset

import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.BaseFileSystemStore
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor
import java.io.File

const val TAGGERS_DIR = "data/tagsets"
class TagsetStore : BaseFileSystemStore (
    File(TAGGERS_DIR)
), Logging {

    val tagsets: Set<Tagset>
        get() = workDirectory.listFiles()
            ?.map{ getTagsetFromFile(it) }
            ?.toSet()
            ?: setOf()

    private fun getTagsetFromFile( file: File ): Tagset {
        val tagset = Yaml(Constructor(Tagset::class.java, LoaderOptions())).load<Tagset>( file.inputStream() )
        tagset.identifier = file.nameWithoutExtension // the tagset name is set here
        return tagset
    }

    fun getOrNull( identifier: String? ): Tagset? {
        val tagsetFile = workDirectory.resolve( "$identifier.yaml" )
        return if ( tagsetFile.exists() ) {
            getTagsetFromFile(tagsetFile)
        } else {
            null
        }
    }

}