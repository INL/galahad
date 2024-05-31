package org.ivdnt.galahad.port

import java.io.Reader

interface PlainTextableFile {

    fun plainTextReader(): Reader

}