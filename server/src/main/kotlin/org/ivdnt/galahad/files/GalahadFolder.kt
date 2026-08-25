package org.ivdnt.galahad.files

import java.io.File
import org.ivdnt.galahad.util.lastModifiedFile

abstract class GalahadFolder(protected val dir: File) {
    // Note the Kotlin initialization order. Make the dir before access.
    init {
        dir.mkdirs()
    }

    val name: String = dir.name
    open val modified: Long
        get() = dir.lastModifiedFile()

    val size: Long
        get() = dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }

    fun deleteRecursively(): Boolean = dir.deleteRecursively()
}
