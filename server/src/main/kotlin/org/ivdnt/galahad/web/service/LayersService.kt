package org.ivdnt.galahad.web.service

import java.util.*
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.layers.CorpusLayerMetadata
import org.ivdnt.galahad.taggers.Tagger
import org.springframework.stereotype.Service

@Service
class LayersService(private val corpora: CorporaService) : Logging {
    fun readAll(corpus: UUID): List<CorpusLayerMetadata> =
        corpora.readOrThrow(corpus).layers.readAll().map { it.metadata }

    fun readOrThrow(corpus: UUID, layer: String): CorpusLayerMetadata =
        corpora.readOrThrow(corpus).layers.readOrThrow(layer).metadata

    fun createOrThrow(corpus: UUID, tagger: Tagger) {
        val layer = corpora.writeOrThrow(corpus).layers.createOrThrow(tagger.name)
        layer.customTagger = tagger
    }

    fun deleteOrThrow(corpus: UUID, layer: String) {
        // Delete all jobs for this layer
        corpora.writeOrThrow(corpus).jobs.deleteOrThrow(layer)
        // Delete all evaluations
        corpora.writeOrThrow(corpus).evaluation.deleteRecursively()
        // Now delete it as write access
        // Use delete or null, since the layer may have no actual files due to a failed job.
        corpora.writeOrThrow(corpus).layers.deleteOrNull(layer)
    }
}
