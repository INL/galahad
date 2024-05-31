package org.ivdnt.galahad.data.document

import org.ivdnt.galahad.BaseFileSystemStore
import org.ivdnt.galahad.app.CRUDSet
import org.ivdnt.galahad.data.DocumentWriteType
import org.ivdnt.galahad.data.DocumentsController
import java.io.File

/**
 * Used as a collection for all documents in a corpus and to create and delete new documents.
 * Documents are saved as folders with their file name as folder name.
 * This class represents their common parent directory "documents/".
 */
class Documents(
    workDirectory: File,
) : BaseFileSystemStore(workDirectory), CRUDSet<String, Document, DocumentWriteType> {

    val allNames: List<String>
        get() = workDirectory.list()?.toList() ?: throw Exception("Could not read document names")

    /** Retrieve a single document */
    override fun readOrNull(key: String) =
        if (workDirectory.resolve(key).exists()) Document(workDirectory.resolve(key)) else null

    // Note: this is a relatively expensive operation, you might want to use a different method
    override fun readAll(): Set<Document> = workDirectory.listFiles()?.map { Document(it) }?.toSet() ?: setOf()

    /** Delete a single document */
    override fun delete(key: String): Document? {
        val fullyDeleted: Boolean = workDirectory.resolve(key).deleteRecursively()
        if (!fullyDeleted) println("Partial deletion of $key")
        // TODO remember we also need to delete in associated jobs
        return readOrNull(key)
    }

    override fun update(key: String, value: DocumentWriteType): Document? {
        throw Exception("Use create instead. New files overwrite existing ones.")
    }

    fun create(file: File) = create(DocumentWriteType(file.name, file.inputStream()))

    /**
     * Create a new document, which includes creating a directory,
     * storing the uploaded file, metadata, format, parsing it to plaintext and extracting source annotations.
     */
    override fun create(value: DocumentWriteType): String {
        val document = Document(workDirectory.resolve(value.filename))
        value.inputStream.copyTo(document.getUploadedFileStorage(value.filename).outputStream())
        document.parse()
        return value.filename
    }
}