package org.ivdnt.galahad.port.xml

import org.ivdnt.galahad.port.PlainTextableFile
import org.ivdnt.galahad.port.SourceLayerableFile
import java.io.File

abstract class AnnotatedFile (
        file: File
) : XMLFile( file ), PlainTextableFile, SourceLayerableFile
