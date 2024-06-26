package org.ivdnt.galahad.port

import org.ivdnt.galahad.data.document.DocumentFormat
import org.ivdnt.galahad.taggers.TaggerStore
import org.ivdnt.galahad.tagset.TagsetStore
import java.io.File
import java.io.OutputStream
import java.text.SimpleDateFormat
import kotlin.io.path.createFile
import kotlin.io.path.createTempDirectory

open class LayerTransformer (
    val transformMetadata: DocumentTransformMetadata
    ) {

    private val tagsets = TagsetStore()
    private val taggerStore = TaggerStore()
    val tagger = taggerStore.getSummaryOrThrow(transformMetadata.job.name, transformMetadata.corpus.sourceTagger ).expensiveGet()
    protected val result = transformMetadata.layer
    protected val document = transformMetadata.document

    val punctuationTags = tagsets.getOrNull( tagger.tagset )?.punctuationTags ?: setOf()

    val dateTimeFormat: SimpleDateFormat
        get() = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    val dateFormat: SimpleDateFormat
        get() = SimpleDateFormat("yyyy-MM-dd")

}

interface LayerConverter {
    /**
     * Convert an annotation layer to the respective file type.
     *
     * @param outputStream The stream to which to output the file of the respective file type.
     */
    fun convert( outputStream: OutputStream )
    fun convertToFileNamed( name: String ): File {
        val tempFile = createTempDirectory("galahad-layer-converter").resolve( "$name.${format.extension}" ).createFile().toFile()
        convert( tempFile.outputStream() )
        return tempFile
    }
    val format: DocumentFormat

}

interface LayerMerger<T: InternalFile> {

    fun merge(): T

}