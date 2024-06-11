package org.ivdnt.galahad.port.folia.export

import org.ivdnt.galahad.data.document.DocumentFormat
import org.ivdnt.galahad.data.layer.Term
import org.ivdnt.galahad.port.DocumentTransformMetadata
import org.ivdnt.galahad.port.LayerConverter
import org.ivdnt.galahad.port.LayerTransformer
import org.ivdnt.galahad.util.XMLWriter
import org.ivdnt.galahad.util.escapeXML
import org.ivdnt.galahad.util.toValidXmlId
import java.io.OutputStream

class LayerToFoliaConverter (
    transformMetadata: DocumentTransformMetadata,
) : LayerConverter, LayerTransformer( transformMetadata ) {

    override val format: DocumentFormat
        get() = DocumentFormat.Folia

    val id: String
        get() = document.getUploadedRawFile().nameWithoutExtension.toValidXmlId()

    override fun convert(outputStream: OutputStream) {
        val taggerName = tagger.id
        val writer = XMLWriter(outputStream)
        // XML Header
        writer.writeLineRaw("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
        writer.openTag("<FoLiA xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns=\"http://ilk.uvt.nl/folia\" xml:id=\"$id\" generator=\"galahad.ivdnt.org\" version=\"2.5.3\">")
        // Metadata
        writeMetadata(writer, taggerName)
        // Textbody
        writeTextBody(writer, taggerName)
    }

    private fun writeMetadata(writer: XMLWriter, taggerName: String) {
        writer.openTag("<metadata type=\"native\">")
        // Annotations
        writer.openTag("<annotations>")
        writer.writeLine("<text-annotation/>")
        writer.writeLine("<paragraph-annotation/>")
        writer.writeLine("<sentence-annotation/>")
        writer.writeLine("<token-annotation/>")
        for (annotation in setOf("lemma", "pos")) {
            writer.openTag("<$annotation-annotation set=\"${taggerName}\">")
            writer.writeLine("<annotator processor=\"${taggerName}\"/>")
            writer.closeTag("</$annotation-annotation>")
        }
        writer.closeTag("</annotations>")
        // Provenance
        writer.openTag("<provenance>")
        writer.writeLine(
            "<processor host=\"galahad.ivdnt.org\" name=\"${taggerName}\" src=\"https://github.com/INL/taggers-dockerized\" type=\"auto\" user=\"${transformMetadata.user.id}\" xml:id=\"${taggerName}\"/>"
        )
        writer.closeTag("</provenance>")
        writer.closeTag("</metadata>")
    }

    private fun writeTextBody(writer: XMLWriter, taggerName: String) {
        writer.openTag("<text xml:id=\"$id.text\">")
        writer.openTag("<p xml:id=\"$id.p1\">")
        for ((index, term) in this.result.terms.withIndex()) {
            // Single W
            writeSingleW(writer, index, term, taggerName)
        }
        writer.closeTag("</p>")
        writer.closeTag("</text>")
        writer.closeTag("</FoLiA>")
    }

    private fun writeSingleW(
        writer: XMLWriter, index: Int, term: Term,
        taggerName: String,
    ) {
        writer.openTag("<w xml:id=\"$id.p1.w${index + 1}\">")
        writer.writeLine("<t>${term.targets[0].literal.escapeXML()}</t>")
        writer.writeLine("<lemma class=\"${term.lemma?.escapeXML()}\" processor=\"$taggerName\" set=\"$taggerName\"/>")
        writer.writeLine(
            "<pos class=\"${term.pos?.escapeXML()}\" head=\"${term.posHeadGroup?.escapeXML()}\" processor=\"$taggerName\" set=\"$taggerName\"/>"
        )
        writer.closeTag("</w>")
    }
}