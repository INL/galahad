package org.ivdnt.galahad.formats

import java.io.File
import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.formats.conllu.ConlluFile
import org.ivdnt.galahad.formats.docx.DocxFile
import org.ivdnt.galahad.formats.folia.FoliaFile
import org.ivdnt.galahad.formats.json.JsonFile
import org.ivdnt.galahad.formats.naf.NafFile
import org.ivdnt.galahad.formats.pdf.PdfFile
import org.ivdnt.galahad.formats.reader.LayerReader
import org.ivdnt.galahad.formats.tei.TeiFile
import org.ivdnt.galahad.formats.tsv.TsvFile
import org.ivdnt.galahad.formats.txt.TxtFile
import org.ivdnt.galahad.layer.Layer

/**
 * A parsed [file] of a certain [DocumentFormat] e.g. TEI, TSV, Folia.
 * Allows accessing the annotation layer.
 */
abstract class ParsedFile protected constructor() {
    /** Original file to be parsed. */
    abstract val file: File
    /** Format in which it has been parsed. */
    abstract val format: DocumentFormat
    /** Annotation layer retrieved from [file]. */
    val layer: Layer by lazy { reader.layer }
    /** Parser implementation. Reads [layer] from [file]. */
    protected abstract val reader: LayerReader

    companion object {
        /** Parse [file] into a certain [DocumentFormat]. */
        fun create(file: File): ParsedFile {
            return when (val format = DocumentFormat.fromFile(file)) {
                DocumentFormat.Tsv -> TsvFile(file)
                DocumentFormat.Folia -> FoliaFile(file)
                DocumentFormat.Naf -> NafFile(file)
                DocumentFormat.Txt -> TxtFile(file)
                DocumentFormat.Conllu -> ConlluFile(file)
                DocumentFormat.Docx -> DocxFile(file)
                DocumentFormat.Pdf -> PdfFile(file)
                DocumentFormat.Json -> JsonFile(file)
                // Multiple TEI formats
                DocumentFormat.TeiP4Legacy,
                DocumentFormat.TeiP5 -> TeiFile(file, format)
            }
        }
    }
}
