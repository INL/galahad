package org.ivdnt.galahad.port.csv

import java.io.File

typealias CSVHeader = String
typealias CSVRecord = String

class CSVFile(
    path: File
) : File(path.toURI()) {

    init {
        this.appendText(getExcelCompatibilityHeader())
    }

    /** Append Excel compatible text. */
    fun appendText(text: String) {
        this.appendText(text, Charsets.UTF_16LE)
    }

    companion object {
        private fun toCSVSafeString( s: String ): String {
            // Alternatively we could check for forbidden characters first, and the wrap/replace only when necessary.
            // However, this works and gives a consistent result
            return "\"${s.replace("\"", "\"\"")}\""
        }

        // Okay, basically CSV and Excel are not a good match.
        // The default separator is defined by Windows and will be ',' in the US but ';' in EU
        // This is because ',' is reserved for denoting quantities in EU (e.g. 1,000 euro)
        // To open the CSV's correctly in all excel we need to add the instruction returned by this function to the CSV's
        // However we will see this instruction as the first row when we use a different (read linuxy) spreadsheet program
        // This is sad and I don't know of a clean solution short of having a configuration option 'excel-compatibility'
        // We will just except this loss and accommodate Excel/Windows user who might be less technically skilled
        // and hope that the Linux crowd will be able to handle this slightly weird header themselves
        fun getExcelCompatibilityHeader(): String {
            // Force Excel to read the csv as UTF16LE. Needed to render e.g. 'ü'.
            // https://en.wikipedia.org/wiki/Byte_order_mark
            val bom = '\uFEFF'
            return "${bom}sep=,\n"
        }

        fun toCSVHeader(headers: List<String>): CSVHeader {
            // This is just an alias
            return toCSVRecord( headers )
        }

        fun toCSVRecord(values: List<Any>): CSVRecord {
            return values.joinToString(",") { toCSVSafeString(it.toString()) }.plus("\n")
        }
    }
}