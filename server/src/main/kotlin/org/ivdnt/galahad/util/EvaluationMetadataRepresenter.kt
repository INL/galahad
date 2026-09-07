package org.ivdnt.galahad.util

import java.net.URI
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.TypeDescription
import org.yaml.snakeyaml.introspector.Property
import org.yaml.snakeyaml.nodes.NodeTuple
import org.yaml.snakeyaml.nodes.Tag
import org.yaml.snakeyaml.representer.Representer

/**
 * YAML representer for pretty printing [org.ivdnt.galahad.taggers.Tagger] in evaluation metadata.
 */
class EvaluationMetadataRepresenter(options: DumperOptions) : Representer(options) {
    // Turns off explicit type description.
    override fun addTypeDescription(td: TypeDescription?): TypeDescription? = null

    // Custom representation
    override fun representJavaBeanProperty(
        javaBean: Any?,
        property: Property?,
        propertyValue: Any?,
        customTag: Tag?,
    ): NodeTuple? {
        return if (propertyValue == null) {
            // if value of property is null, ignore it. Thereby not serializing it.
            null
        } else if (propertyValue is Collection<*> && propertyValue.isEmpty()) {
            // Ignore empty collections
            null
        } else if (property?.name == "port") {
            // Hide tagger.port
            return null
        } else if (propertyValue is URI) {
            // Force stringify URI. Otherwise, they are blank for some reason
            return super.representJavaBeanProperty(
                javaBean,
                property,
                propertyValue.toString(),
                customTag,
            )
        } else {
            // Default
            super.representJavaBeanProperty(
                javaBean,
                property,
                propertyValue,
                customTag,
            )
        }
    }
}
