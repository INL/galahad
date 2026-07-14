package org.ivdnt.galahad.files

import java.io.File
import org.apache.logging.log4j.kotlin.logger
import org.ivdnt.galahad.util.lastModifiedFile

/**
 * Generic base class for file system operations. Create, read and delete GalahadFiles in a folder.
 * Usage:
 * ```
 * val folder = GalahadFolder<ReadType, CreateType>(...)
 * val data: CreateType = ...
 * val key: String = ...
 *
 * val file = folder.createOrThrow(data)
 * val files = folder.readAll()
 *
 * folder.deleteOrNull(key)
 * if (folder.deleteOrNull(key) == null) { println("Nothing to delete") } // prints
 * // folder.deleteOrThrow(key) // throws
 *
 * val file2 = folder.readOrNull(key) // returns null
 * // val file3 = folder.readOrThrow(key) // throws
 */
abstract class GalahadFolderManager<ReadType : GalahadFolder, CreateType : Any>(file: File) :
    GalahadFolder(file) {

    override val modified: Long
        get() =
            maxOf(
                dir.lastModifiedFile(),
                dir.listFiles()?.maxOfOrNull { it.lastModifiedFile() } ?: 0L,
            )

    protected abstract fun ctor(key: String): ReadType

    protected abstract fun throwNotFound(key: String): Nothing

    open fun createOrThrow(key: CreateType): ReadType = ctor(key.toString())

    open fun readAll(): List<ReadType> = dir.list()?.map { readOrThrow(it) } ?: emptyList()

    open fun readAllSequence(): Sequence<ReadType> =
        dir.list()?.asSequence()?.map { readOrThrow(it) } ?: emptySequence()

    // TODO resolve in folder only?
    fun readOrNull(key: String): ReadType? = if (dir.resolve(key).exists()) ctor(key) else null

    fun readOrThrow(key: String): ReadType = readOrNull(key) ?: throwNotFound(key)

    open fun deleteOrThrow(key: String) {
        readOrThrow(key) // does it exist?
        if (!dir.resolve(key).deleteRecursively()) {
            logger.warn("Partial deletion of $key")
        }
    }

    fun deleteOrNull(key: String): Unit? = readOrNull(key)?.let { deleteOrThrow(key) }
}
