package org.ivdnt.galahad.port

import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor
import org.yaml.snakeyaml.representer.Representer
import java.io.File

open class BLF

/**
 * The BLF file can contain many configurations, however we only need a few.
 */
class BLFXML: BLF() {
    class AnnotatedFields {
        class Contents {
            class Annotation {
                var name: String? = null
                var valuePath: String? = null
            }
            var containerPath: String? = null
            var wordPath: String? = null
            var punctPath: String? = null
            var annotations: List<Annotation>? = null
        }
        var contents: Contents? = null
    }

    var namespaces: Map<String, String>? = null
    var documentPath: String? = null
    var annotatedFields: AnnotatedFields? = null

    companion object {
        fun from(configFile: File): BLFXML {
            val representer = Representer(DumperOptions())
            representer.propertyUtils.isSkipMissingProperties = true
            return Yaml(Constructor(BLFXML::class.java, LoaderOptions()), representer).load(configFile.inputStream())
        }
    }

}
