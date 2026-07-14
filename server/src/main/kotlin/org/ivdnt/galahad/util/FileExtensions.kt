package org.ivdnt.galahad.util

import java.io.File
import java.nio.file.Files
import org.ivdnt.galahad.documents.DocumentFormat

val File.withoutFormatExt: String
    get() {
        DocumentFormat.extensions.forEach {
            if (this.name.endsWith(it)) {
                return this.name.substringBeforeLast(it)
            }
        }
        return this.nameWithoutExtension
    }

fun File.asFormat(format: DocumentFormat): String = this.withoutFormatExt + "." + format.extension

fun File.lastModifiedFile(): Long =
    listFiles()?.maxOfOrNull { Files.getLastModifiedTime(it.toPath()).toMillis() } ?: 0L
