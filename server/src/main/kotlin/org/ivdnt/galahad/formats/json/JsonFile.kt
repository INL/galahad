package org.ivdnt.galahad.formats.json

import java.io.File
import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.formats.ParsedFile
import org.ivdnt.galahad.formats.reader.LayerReader
import org.ivdnt.galahad.layer.Layer
import org.ivdnt.galahad.util.JsonUtil

class JsonFile(override val file: File) : ParsedFile() {
    override val format: DocumentFormat = DocumentFormat.Json
    override val reader: LayerReader by lazy {
        object : LayerReader() {
            override fun read() = JsonUtil.mapper.readValue(file, Layer::class.java)
        }
    }
}
