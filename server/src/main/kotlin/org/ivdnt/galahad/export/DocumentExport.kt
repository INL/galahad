package org.ivdnt.galahad.export

import java.io.OutputStream
import org.ivdnt.galahad.app.User
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.documents.Document
import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.layer.Layer
import org.ivdnt.galahad.layers.CorpusLayer
import org.ivdnt.galahad.taggers.Tagger

class DocumentExport(
    val corpus: Corpus,
    val layers: CorpusLayer,
    val sourceLayers: CorpusLayer,
    document: String,
    val user: User,
    val format: DocumentFormat,
    val posHead: Boolean,
) {
    val document: Document = layers.documents.readOrThrow(document)
    val layer: Layer = this.document.layer
    val tagger: Tagger = layers.metadata.tagger
    val sourceDocument: Document = sourceLayers.documents.readOrThrow(document)
    val sourceLayer: Layer = sourceDocument.layer

    fun convert(out: OutputStream): Unit =
        LayerWriter.create(this).convert(out).also { out.flush() }

    fun merge(out: OutputStream): Unit = LayerMerger.create(this).merge(out).also { out.flush() }

    fun cmdi(out: OutputStream): Unit = CmdiMetadata(this).write(out)
}
