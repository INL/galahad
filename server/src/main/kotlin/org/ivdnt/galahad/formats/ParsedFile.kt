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

/** A document parsed as a file of a certain file type, e.g. TEI, TSV, Folia. */
abstract class ParsedFile protected constructor() {
    abstract val file: File
    abstract val format: DocumentFormat
    val layer: Layer by lazy { reader.layer }
    protected abstract val reader: LayerReader

    companion object {
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
