package org.ivdnt.galahad.port

import org.ivdnt.galahad.data.corpus.Corpus
import org.ivdnt.galahad.app.User
import org.ivdnt.galahad.data.document.Document
import org.ivdnt.galahad.data.layer.Layer
import org.ivdnt.galahad.data.layer.Term
import org.ivdnt.galahad.jobs.Job

open class CorpusTransformMetadata(
    val corpus: Corpus,
    val job: Job,
    val user: User
) {

    fun documentMetadata( document: String ): DocumentTransformMetadata {
        return DocumentTransformMetadata(
            corpus = corpus,
            job = job,
            document = corpus.documents.readOrThrow( document ),
            user = user
        )
    }
}

class DocumentTransformMetadata(
    val corpus: Corpus,
    val job: Job,
    val document: Document,
    val user: User
) {

    val layer: Layer = job.document(document.name).result

    val plainText: String
        get() = document.plaintext

    fun convertLayerToPosHead() {
        for (i in layer.terms.indices) {
            val t = layer.terms[i]
            layer.terms[i] = Term(
                lemma = t.lemma,
                pos = t.posHeadGroup,
                targets = t.targets)
        }
    }
}
