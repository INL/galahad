package org.ivdnt.galahad.port.plain

import org.ivdnt.galahad.data.document.DocumentFormat
import org.ivdnt.galahad.port.DocumentTransformMetadata
import org.ivdnt.galahad.port.InternalFile
import org.ivdnt.galahad.port.PlainTextableFile
import java.io.File
import java.io.Reader

class PlainFile(
    override val file: File,
) : InternalFile, PlainTextableFile {
    override val format: DocumentFormat = DocumentFormat.Txt

    override fun plainTextReader(): Reader {
        return file.reader()
    }

    override fun merge(transformMetadata: DocumentTransformMetadata): PlainFile {
        // merging does not make sense for PlainFile
        throw Exception("Cannot merge layer into plainfile")
    }
}