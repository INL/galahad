package org.ivdnt.galahad.util

import jakarta.servlet.http.HttpServletResponse
import java.net.URLEncoder

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

// default support https://stackoverflow.com/a/19975244
private val defaultReplacements = mapOf(
    '&' to "&amp;",
    '\"' to "&quot;",
    '\'' to "&apos;",
    '<' to "&lt;",
    '>' to "&gt;"
)

fun String.escapeXML(): String {
    return buildString {
        for (char in this@escapeXML) {
            when {
                defaultReplacements.containsKey(char) -> append(defaultReplacements[char])
                char.code == 0xA0 -> append(" ") // nbsp
                char.code == 0x0D -> { } // ignore CR
                char.isLetterOrDigit() || char.isWhitespace() || (char.code in 0x20..0x7E) -> append(char)
                else -> append("&#${char.code};")
            }
        }
    }
}

// Normally, periods (.) are allowed too, but they have a hierarchical significance, so add any periods yourself.
// Based on https://stackoverflow.com/a/1077111
// TODO utf8/unicode support
fun String.toValidXmlId(): String {
    return "id_" + this.replace(Regex("""[^a-zA-Z0-9_-]"""), "_")
}