package org.ivdnt.galahad.documents

import org.ivdnt.galahad.annotations.LayerAnnotations
import org.ivdnt.galahad.annotations.LayerPreview
import org.ivdnt.galahad.annotations.LayerStructure
import org.ivdnt.galahad.formats.ParsedFile
import org.ivdnt.galahad.util.withoutFormatExt

data class DocumentMetadata(
    /** Name of the uploaded file including extension. Used as a working directory name. */
    val name: String,
    /** Format of the uploaded file as induced by FormatInducer. */
    val format: DocumentFormat,
    /** A truncated preview of the parsed plaintext. */
    val text: String, // TODO unnecessary if we have the layer preview
    /** A truncated preview of the annotated layer. */
    val preview: LayerPreview,
    /** Some statistics about the source annotations, if present */
    val annotations: LayerAnnotations,
    /** Some statistics about the layer structure. */
    val structure: LayerStructure,
    /** Last modified timestamp in milliseconds. */
    val modified: Long,
) {
    companion object {
        private const val PREVIEW_LENGTH: Int = 100

        fun create(file: ParsedFile): DocumentMetadata {
            val text = file.layer.toString()
            return DocumentMetadata(
                name = file.file.withoutFormatExt,
                format = file.format,
                text = text.take(PREVIEW_LENGTH) + if (text.length > PREVIEW_LENGTH) "..." else "",
                preview = file.layer.preview,
                annotations = file.layer.summary,
                structure = file.layer.structure,
                modified = System.currentTimeMillis(),
            )
        }
    }
}
