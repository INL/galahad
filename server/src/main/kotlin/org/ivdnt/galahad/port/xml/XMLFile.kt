package org.ivdnt.galahad.port.xml

import org.ivdnt.galahad.port.InternalFile
import java.io.File

abstract class XMLFile(
    final override val file: File,
) : InternalFile {
}