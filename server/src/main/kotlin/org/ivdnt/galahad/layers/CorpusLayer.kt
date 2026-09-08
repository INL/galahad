package org.ivdnt.galahad.layers

import java.io.File
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.documents.Documents
import org.ivdnt.galahad.files.DiskValue
import org.ivdnt.galahad.files.GalahadFolder
import org.ivdnt.galahad.files.ValidatedDiskValue
import org.ivdnt.galahad.taggers.Tagger

class CorpusLayer(dir: File, private val corpus: Corpus) : GalahadFolder(dir) {
    val documents: Documents = Documents(dir.resolve(DOCUMENTS_FOLDER))
    val metadata: CorpusLayerMetadata
        get() =
            object : ValidatedDiskValue<CorpusLayerMetadata>(dir.resolve(METADATA_FILE)) {
                    override fun isValid(modified: Long) = modified >= this@CorpusLayer.modified

                    override fun set(): CorpusLayerMetadata =
                        CorpusLayerMetadata.create(this@CorpusLayer, corpus)
                }
                .readOrCreate()

    var customTagger: Tagger
        get() = DiskValue<Tagger>(dir.resolve(TAGGER_FILE)).readOrThrow()
        set(value) {
            DiskValue<Tagger>(dir.resolve(TAGGER_FILE)).write(value)
        }

    companion object {
        private const val METADATA_FILE = "metadata.json"
        private const val TAGGER_FILE = "tagger.json"
        const val DOCUMENTS_FOLDER: String = "documents"
    }
}
