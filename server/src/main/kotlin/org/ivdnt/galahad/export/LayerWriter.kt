package org.ivdnt.galahad.export

import java.io.OutputStream
import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.exceptions.InvalidDocumentFormatException
import org.ivdnt.galahad.formats.conllu.ConlluWriter
import org.ivdnt.galahad.formats.folia.FoliaWriter
import org.ivdnt.galahad.formats.json.JsonWriter
import org.ivdnt.galahad.formats.naf.NafWriter
import org.ivdnt.galahad.formats.tei.TeiWriter
import org.ivdnt.galahad.formats.tsv.TsvWriter
import org.ivdnt.galahad.formats.txt.TxtWriter
import org.ivdnt.galahad.layer.DocumentLayer

abstract class LayerWriter protected constructor(protected val export: DocumentExport) {
    protected val documents: Array<DocumentLayer> = export.layer.documents

    abstract fun convert(out: OutputStream)

    companion object {
        fun create(export: DocumentExport): LayerWriter =
            when (export.format) {
                DocumentFormat.Tsv -> TsvWriter(export)
                DocumentFormat.Folia -> FoliaWriter(export)
                DocumentFormat.Naf -> NafWriter(export)
                DocumentFormat.Txt -> TxtWriter(export)
                DocumentFormat.Conllu -> ConlluWriter(export)
                DocumentFormat.TeiP5 -> TeiWriter(export)
                DocumentFormat.Json -> JsonWriter(export)
                else ->
                    throw InvalidDocumentFormatException(
                        "Unsupported export conversion format: ${export.format}"
                    )
            }
    }
}
