package org.ivdnt.galahad.port.folia

import org.ivdnt.galahad.port.LayerTransformer
import org.ivdnt.galahad.port.xml.XMLMetadata
import org.ivdnt.galahad.port.xml.tagName
import org.ivdnt.galahad.util.insertAfter
import org.ivdnt.galahad.util.nextNonTextSibling
import org.w3c.dom.Document
import org.w3c.dom.Node

class FoliaMetadata(xmlDoc: Document, root: Node, layer: LayerTransformer) : XMLMetadata(xmlDoc, root, layer) {
    fun write() {
        val meta = root.getOrCreateChild("metadata")
        val annotations: Node = meta.getOrCreateChild("annotations")

        // We also try to fix some documents with missing annotation definitions.
        // Really this should be the user's problem. But why not.
        annotations.getOrCreateChild("text-annotation")
        annotations.getOrCreateChild("paragraph-annotation")
        annotations.getOrCreateChild("sentence-annotation")
        annotations.getOrCreateChild("token-annotation")

        addAnnotationDefinition(annotations, "lemma")
        addAnnotationDefinition(annotations, "pos")

        // Order matters, <provenance> needs to be directly after annotations
        val provenance: Node
        val nextNonTextSibling = annotations.nextNonTextSibling()
        if (nextNonTextSibling?.tagName() == "provenance") {
            provenance = nextNonTextSibling
        } else {
            provenance = xmlDoc.createElement("provenance")
            annotations.parentNode.insertAfter(provenance,annotations)
        }

        val processor = xmlDoc.createElement("processor")
        processor.setAttribute("xml:id", layer.tagger.id)
        processor.setAttribute("name", layer.tagger.id)
        processor.setAttribute("type", "auto")
        processor.setAttribute("src", "https://github.com/INL/galahad-taggers-dockerized")
        processor.setAttribute("host", "galahad.ivdnt.org")
        processor.setAttribute("user", layer.transformMetadata.user.id)

        provenance.appendChild(processor)
    }

    private fun addAnnotationDefinition(annotations: Node, name: String) {
        val anot = xmlDoc.createElement("$name-annotation")
        anot.setAttribute("set", layer.tagger.id)
        val annotator = xmlDoc.createElement("annotator")
        annotator.setAttribute("processor", layer.tagger.id)
        anot.appendChild(annotator)
        annotations.appendChild(anot)
    }
}