package org.ivdnt.galahad.util

import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.TypeDescription
import org.yaml.snakeyaml.introspector.Property
import org.yaml.snakeyaml.nodes.Node
import org.yaml.snakeyaml.nodes.NodeTuple
import org.yaml.snakeyaml.nodes.Tag
import org.yaml.snakeyaml.representer.Represent
import org.yaml.snakeyaml.representer.Representer

class EvaluationMetadataRepresenter(options: DumperOptions) : Representer(options) {
    init {
        this.nullRepresenter = RepresentNull()
    }

    override fun addTypeDescription(td: TypeDescription?): TypeDescription? = null

    override fun representJavaBeanProperty(
        javaBean: Any?,
        property: Property?,
        propertyValue: Any?,
        customTag: Tag?,
    ): NodeTuple? {
        // if value of property is null, ignore it. Thereby not serializing it.
        return if (propertyValue == null) {
            null
        } else if (propertyValue is Collection<*> && propertyValue.isEmpty()) {
            null
        } else if (property?.name == "port") {
            return null
        } else {
            super.representJavaBeanProperty(
                javaBean,
                property,
                propertyValue,
                customTag,
            )
        }
    }

    private inner class RepresentNull : Represent {
        override fun representData(data: Any?): Node? = representScalar(Tag.NULL, "")
    }
}
