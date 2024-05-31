package org.ivdnt.galahad.port.xml

import org.ivdnt.galahad.port.InternalFile
import org.xml.sax.Attributes
import java.io.File
import java.util.*

abstract class XMLFile(
    final override val file: File,
) : InternalFile {
}