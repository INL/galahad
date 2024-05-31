package org.ivdnt.galahad.port.tsv

import org.ivdnt.galahad.data.document.DocumentFormat
import org.ivdnt.galahad.data.layer.Layer
import org.ivdnt.galahad.data.document.SOURCE_LAYER_NAME
import org.ivdnt.galahad.port.*
import org.ivdnt.galahad.port.conllu.ConlluFile
import org.ivdnt.galahad.port.tsv.export.TSVLayerMerger
import java.io.File
import java.io.FileOutputStream
import java.io.Reader

open class TSVFile(
    override val file: File,
) : InternalFile, SourceLayerableFile, PlainTextableFile {

    private val plainTextFile = File.createTempFile("galahad-${file.name}-plaintext", ".txt")
    override val format: DocumentFormat = DocumentFormat.Tsv
    val entries = ArrayList<TSVEntry>()
    private var sourceLayer: Layer = Layer.EMPTY
    open var lemmaIndex: Int? = null
    open var posIndex: Int? = null
    open var literalIndex: Int? = null
    private var isParsed: Boolean = false

    override fun plainTextReader(): Reader {
        if (!isParsed) parse()
        return plainTextFile.reader()
    }

    override fun sourceLayer(): Layer {
        if (!isParsed) parse()
        return sourceLayer
    }

    /**
     * Generate plaintext, sourcelayer and retrieve indices for literal, lemma and PoS.
     */
    fun parse() {
        if (isParsed) return // Don't double parse
        isParsed = true

        // Generate plaintext
        val stream = plainTextFile.outputStream()
        for ((i, line) in file.readLines().withIndex()) {
            if (i == 0 && this !is ConlluFile) {
                parseHeader(line) // Also retrieves indices.
            } else {
                parseBody(line, stream)
            }
        }
        stream.close()
        // Generate sourcelayer
        sourceLayer = mapOnPlainText(plainTextFile.readText(), SOURCE_LAYER_NAME)
    }

    /**
     * Retrieve the indices of the literal, lemma and PoS columns from the header.
     * If any were not found, throw.
     * @param line Header line read from the tsv file.
     */
    private fun parseHeader(line: String) {
        val headers = line.split("\t")
        val errors: MutableList<String> = mutableListOf()

        getColumnIndices(headers, errors)

        if (errors.isNotEmpty()) {
            // Combine the errors for some pretty printing.
            val missingColumns = errors.joinToString(" and ", transform = { error -> "a $error column" })
            throw Exception("Could not find $missingColumns in the TSV header.")
        }
    }

    // Derived classes may want to look for other names or indices.
    protected open fun getColumnIndices(
        headers: List<String>,
        errors: MutableList<String>,
    ) {
        literalIndex = indexOfHeaderNamedAnyOf(headers, listOf("word", "token", "literal", "term", "form"), errors)
        lemmaIndex = indexOfHeaderNamedAnyOf(headers, listOf("lemma"), errors)
        posIndex = indexOfHeaderNamedAnyOf(headers, listOf("pos", "upos", "xpos"), errors)
    }

    /**
     * Get index of the tsvHeader with one of a list of possible name. Priority given to the first match.
     *
     * @param tsvHeaders Available headers in the TSV file.
     * @param searchNames Possible names to search for in order of priority.
     * @param errors A list to put the names of columns in that were not found. The first name is reported in case of an error.
     * @return Index of the named header.
     */
    private fun indexOfHeaderNamedAnyOf(
        tsvHeaders: List<String>, searchNames: List<String>, errors: MutableList<String>,
    ): Int? {
        // asSequence prioritizes the first matching searchName.
        val tsvHeader: String? = searchNames.asSequence().map { searchName ->
            tsvHeaders.firstOrNull {
                it.contains(
                    searchName, ignoreCase = true
                )
            }
        }.firstOrNull { it != null }

        return if (tsvHeader == null) {
            // No results were found
            errors.add(searchNames.first())
            null
        } else {
            tsvHeaders.indexOf(tsvHeader)
        }
    }

    private fun parseBody(line: String, stream: FileOutputStream) {
        // Split on tabs
        val values: List<String> = line.split("\t")

        // Retrieve values
        val literal: String? = getColumn(literalIndex!!, values)
        val lemma: String? = getColumn(lemmaIndex!!, values)
        val pos: String? = getPos(values)

        // Skip newlines by checking for non-empty literals.
        if (!literal.isNullOrEmpty() && values.size >= 3) {
            // Commit values if non-empty
            val tsvEntry = TSVEntry(
                literal = literal, lemma = lemma, pos = pos
            )
            entries.add(tsvEntry)
            // Write to plaintext
            stream.write("$literal ".toByteArray()) // Note space between words.
        } else {
            stream.write("\n".toByteArray())
        }
    }

    // Retrieves a column with bounds checking.
    protected open fun getColumn(index: Int?, values: List<String>): String? {
        return if (index != null && index < values.size) {
            val value = values[index]
            value.ifBlank {
                null
            }
        } else {
            null
        }
    }

    // Derived classes may have to construct a PoS differently.
    protected open fun getPos(values: List<String>): String? = getColumn(posIndex!!, values)

    /**
     * Assumes the TSV file is mappable onto the provided plaintext
     */
    fun mapOnPlainText(plaintext: String, layerName: String): Layer {
        parse() // Parse in order to obtain this.entries.
        val newLayer = Layer(layerName)
        var index = 0

        // We use this variable to skip ahead after a match
        var lastPlainOffset = 0

        for (i in 0..plaintext.length) {
            if (i >= lastPlainOffset) {
                if (index < entries.size) {
                    val literal = entries[index].literal
                    if (i + literal.length <= plaintext.length) {
                        val candidate = plaintext.substring(i until i + literal.length)
                        if (literal == candidate) {
                            // Match!!
                            newLayer.addTSVEntryOnOffset(entries[index], i)
                            lastPlainOffset = i + literal.length
                            index++
                        }
                    }
                }
            }
        }
        return newLayer
    }

    override fun merge(transformMetadata: DocumentTransformMetadata): TSVFile {
        // Sets header indices needed to merge.
        parse()
        return TSVLayerMerger(this, transformMetadata).merge()
    }
}