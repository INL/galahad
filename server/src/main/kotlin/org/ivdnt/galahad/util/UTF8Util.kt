package org.ivdnt.galahad.util

import java.net.URLEncoder
import jakarta.servlet.http.HttpServletResponse

/**
 * A valid filename for windows and linux. Exceptions like COM1 still exist.
 * Uses URL encoding. Replaces invalid characters with a dash (-).
 * @return The escaped filename.
 */
fun String.toValidFileName(): String {
    val urlEncoded = URLEncoder.encode(this.replace(Regex("""[\\/<>:*?"|\000]"""), "-"), "utf-8")
    // URLEncoder turns spaces into +
    return urlEncoded.replace("+", "%20")
}

/** UTF8 compatible content disposition header. */
fun HttpServletResponse.setContentDisposition(filename: String) {
    this.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + filename.toValidFileName() )
    this.setHeader("Access-Control-Expose-Headers", "Content-Disposition")
}

/** Escape some illegals chars that we found in lemmas or pos. */
fun String.escapeXML(): String {
    return this
        .replace("&","&amp;")
        .replace("<","&lt;")
        .replace(">","&gt;")
        .replace("\"","&quot;")
}

// Normally, periods (.) are allowed too, but they have a hierarchical significance, so add any periods yourself.
// Based on https://stackoverflow.com/a/1077111
// TODO utf8/unicode support
fun String.toValidXmlId(): String {
    return "id_" + this.replace(Regex("""[^a-zA-Z0-9_-]"""), "_")
}