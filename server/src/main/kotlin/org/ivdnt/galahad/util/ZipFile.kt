package org.ivdnt.galahad.util

import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Create zip file for the given files, optionally to a specific stream.
 * Can be used as a streaming response zip, for when expensive transformations are applied to the Sequence<File>.
 *
 * @param files Sequence of files to be zipped. As it is a Sequence type,
 * you may want to perform some transformations to map data to a file.
 * @param outStream If provided, used as a ZipOutputStream.
 * @return The flushed and closed zipfile.
 */
fun createZipFile(files: Sequence<File>, outStream: OutputStream? = null): File {
    // Create zip and stream.
    val zipFile = File.createTempFile("tmp", ".zip")
    val zipStream = ZipOutputStream(
        BufferedOutputStream(
            outStream ?: FileOutputStream(zipFile)
        )
    )
    // Loop through the Sequence of files
    // Any transformations occur on demand.
    for (f in files) {
        zipStream.putNextEntry(ZipEntry(f.name))
        zipStream.write(f.readBytes())
    }
    // Close
    zipStream.flush()
    zipStream.close()
    return zipFile
}

