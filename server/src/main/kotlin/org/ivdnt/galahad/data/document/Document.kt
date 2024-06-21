package org.ivdnt.galahad.data.document

import org.ivdnt.galahad.BaseFileSystemStore
import org.ivdnt.galahad.FileBackedCache
import org.ivdnt.galahad.FileBackedValue
import org.ivdnt.galahad.app.ExpensiveGettable
import org.ivdnt.galahad.data.layer.Layer
import org.ivdnt.galahad.port.DocumentTransformMetadata
import org.ivdnt.galahad.port.InternalFile
import org.ivdnt.galahad.port.PlainTextableFile
import org.ivdnt.galahad.port.SourceLayerableFile
import org.ivdnt.galahad.port.conllu.export.LayerToConlluConverter
import org.ivdnt.galahad.port.folia.export.LayerToFoliaConverter
import org.ivdnt.galahad.port.naf.export.LayerToNAFConverter
import org.ivdnt.galahad.port.tei.export.LayerToTEIConverter
import org.ivdnt.galahad.port.tsv.export.LayerToTSVConverter
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*
import kotlin.io.path.createTempDirectory

const val SOURCE_LAYER_NAME = "sourceLayer"
const val PREVIEW_LENGTH: Int = 100

/**
 * Documents are saved as folders with their file name as folder name, including extension.
 * Initializing a document object resolves a path, but does not yet [Document.parse] the document.
 * Parsing is done on demand, as it is an expensive operation.
 *
 * A folder can have the following files, that store the document's data:
 * - format: the [DocumentFormat] of the document.
 * - plaintext: the document's text content
 * - uuid: a unique identifier for the document. Used as a metadata pid when converting a layer to TEI.
 * - sourceLayer: the document's source annotations as a [Layer]
 * - metadata.cache: a cache file storing [DocumentMetadata] about the document
 * - uploaded/[name]: the uploaded raw file
 */
class Document(
    workDirectory: File,
) : BaseFileSystemStore(
    workDirectory
) {
    /** File name including extension */
    val name: String = workDirectory.name

    /** Source annotations, if present. Saved to disk. */
    val sourceLayer: FileBackedValue<Layer>
        get() = FileBackedValue(workDirectory.resolve(SOURCE_LAYER_NAME), Layer.EMPTY)

    // Files in the document folder.
    val formatFile = workDirectory.resolve("format")
    val plainTextFile = workDirectory.resolve("plaintext")
    val uuidFile = workDirectory.resolve("uuid")

    // Metadata as a cache file. Avoids retrieving the entire file for e.g. sizeInBytes.
    private val metadataCache = object : FileBackedCache<DocumentMetadata>(
        file = workDirectory.resolve("metadata.cache"), initValue = DocumentMetadata.EMPTY
    ) {
        override fun isValid(lastModified: Long): Boolean {
            return lastModified >= this@Document.lastModified
        }

        override fun set(): DocumentMetadata {
            return DocumentMetadata(
                name = name,
                format = format.identifier,
                valid = plainTextFile.exists() && formatFile.exists(),
                numChars = plaintext.length,
                numAlphabeticChars = plaintext.filter { it.isLetter() }.length,
                preview = plaintext.take(PREVIEW_LENGTH) + if (plaintext.length > PREVIEW_LENGTH) "..." else "",
                layerSummary = sourceLayer.read<Layer>().summary,
                lastModified = this@Document.lastModified
            )
        }
    }

    // Redirect metadata to cache.
    val metadata: ExpensiveGettable<DocumentMetadata> = object : ExpensiveGettable<DocumentMetadata> {
        override fun expensiveGet() = metadataCache.get<DocumentMetadata>()
    }

    /**
     * Format calculation can be expensive because we have to read an xml file to discern between tei and folia for example
     * There we just commit the format directly to disk, as we hope to skip opening the file
     */
    var format: DocumentFormat
        get() = if (formatFile.exists()) {
            DocumentFormat.fromString(formatFile.readText())
        } else {
            val f = FormatInducer.determineFormat(getUploadedRawFile())
            format = f
            f
        }
        set(value) = formatFile.writeText(value.identifier)

    var plaintext: String
        get() = if (plainTextFile.exists()) plainTextFile.readText() else throw Exception("Plaintext file not found")
        set(value) = plainTextFile.writeText(value)

    /**
     * The UUID is only used as a metadata pid when converting a layer to TEI (for now).
     * When merging TEI, it is only used if the document itself defines no pid.
     */
    var uuid: UUID
        get() = if (uuidFile.exists()) {
            UUID.fromString(uuidFile.readText())
        } else {
            val randomUuid = UUID.randomUUID()
            uuid = randomUuid
            randomUuid
        }
        set(value) = uuidFile.writeText(value.toString())

    // This is a bit hacky implementation
    fun getUploadedFileStorage(name: String): File {
        val file = workDirectory.resolve("uploaded").resolve(name)
        file.parentFile.mkdirs()
        file.createNewFile()
        return file
    }

    /** Convert the document to a DocumentFormat-typed file, e.g. TEI or TSV.*/
    private fun getUploadedFile(): ExpensiveGettable<InternalFile> = InternalFile.from(getUploadedRawFile(), format)

    // This val is a convenience so we can get the raw file without initializing an UploadedFile
    fun getUploadedRawFile(): File =
        workDirectory.resolve("uploaded").listFiles()?.firstOrNull() ?: throw Exception("Uploaded raw file not found")

    /** Parse the uploaded file to plaintext and extract its source annotations if present.
     * This is expensive. */
    fun parse() {
        // Store some one-time (sometimes expensive) calculations
        val uf: InternalFile = getUploadedFile().expensiveGet()
        if (uf is PlainTextableFile) {
            plaintext = (uf as PlainTextableFile).plainTextReader().readText()
        }
        if (uf is SourceLayerableFile) {
            sourceLayer.modify<Layer> { (uf as SourceLayerableFile).sourceLayer() }
        }
    }

    /** Convert document to desired format. */
    fun generateAs(format: DocumentFormat, transformMetadata: DocumentTransformMetadata): File {
        val docName = workDirectory.resolve(transformMetadata.document.name).nameWithoutExtension
        return when (format) {
            // The file is what we are interested in, and it is expensive to initialize the documents, so we just pass the file
            DocumentFormat.Folia -> LayerToFoliaConverter(transformMetadata).convertToFileNamed(docName)
            DocumentFormat.Naf -> LayerToNAFConverter(transformMetadata).convertToFileNamed(docName)
            DocumentFormat.TeiP5 -> LayerToTEIConverter(transformMetadata).convertToFileNamed(docName)
            DocumentFormat.Tsv -> LayerToTSVConverter(transformMetadata).convertToFileNamed(docName)
            DocumentFormat.Conllu -> LayerToConlluConverter(transformMetadata).convertToFileNamed(docName)
            DocumentFormat.Txt -> {
                val tempPath = createTempDirectory("galahad-layer-converter")
                Files.copy(
                    plainTextFile.toPath(), Paths.get("$tempPath/$docName.txt"), StandardCopyOption.REPLACE_EXISTING
                )
                File(tempPath.toString(), "$docName.txt")
            }

            else -> throw Exception("Conversion to $format not supported")
        }
    }

    /** Merge an annotation layer with the original uploaded file, retaining the document structure. */
    fun merge(transformMetadata: DocumentTransformMetadata) =
        getUploadedFile().expensiveGet().merge(transformMetadata)
}
