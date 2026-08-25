package org.ivdnt.galahad.export

import java.io.OutputStream
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.annotations.Layer
import org.ivdnt.galahad.annotations.Layer.Companion.SOURCE_LAYER
import org.ivdnt.galahad.app.User
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.documents.Document
import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.documents.Documents
import org.ivdnt.galahad.exceptions.MergeNotImplementedException
import org.ivdnt.galahad.layers.CorpusLayer
import org.ivdnt.galahad.util.*

class CorpusExport(
    val corpus: Corpus,
    layer: String,
    val format: DocumentFormat,
    val user: User,
    val merge: Boolean,
    val posHead: Boolean,
) : Logging {
    val layers: CorpusLayer = corpus.layers.readOrThrow(layer)
    val sourceLayers: CorpusLayer = corpus.layers.readOrThrow(SOURCE_LAYER)

    /**
     * Maps all [Document] found in [Documents] to the desired [DocumentFormat] and zips them.
     * [formatMapper] should perform the mapping.
     */
    fun export(out: OutputStream) {
        val docs = layers.documents.readAll().filter { it.layer != Layer.EMPTY }
        val seq: Sequence<FileMapper> =
            docs
                .asSequence()
                .map<Document, Pair<String, (OutputStream) -> Unit>> { doc ->
                    val fileName = doc.sourceFile.asFormat(format)
                    fileName to { out -> formatMapper(doc, out) }
                }
                .dedupeFilenames()
        val seqCmdi: Sequence<FileMapper> =
            docs
                .asSequence()
                .map<Document, Pair<String, (OutputStream) -> Unit>> { doc ->
                    "metadata/CMDI-${doc.sourceFile.withoutFormatExt}.xml" to
                        { out ->
                            document(doc).cmdi(out)
                        }
                }
                .dedupeFilenames()
        createZipFile(seq + seqCmdi, out)
    }

    fun document(document: Document): DocumentExport = document(document.name)

    fun document(document: String): DocumentExport =
        DocumentExport(
            corpus = corpus,
            layers = layers,
            sourceLayers = sourceLayers,
            document = document,
            user = user,
            format = format,
            posHead = posHead,
        )

    private fun formatMapper(doc: Document, out: OutputStream) {
        try {
            // Document conversions.
            val docExport = document(doc)
            if (merge) {
                logger.info("Merging ${doc.name} in format $format")
                docExport.merge(out)
            } else {
                logger.info("Converting ${doc.name} of format ${doc.metadata.format} to $format")
                docExport.convert(out)
            }
        } catch (e: MergeNotImplementedException) {
            throw e
        } catch (e: Exception) {
            throw Exception("Could not convert file ${doc.name} to format ${format}. ${e.message}.")
        }
    }
}
