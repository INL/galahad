package org.ivdnt.galahad.formats.tsv

import java.io.File
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.Layer
import org.ivdnt.galahad.annotations.Term
import org.ivdnt.galahad.exceptions.DocumentInvalidException
import org.ivdnt.galahad.formats.reader.LineReader

class TsvReader(val file: File) : LineReader() {
    private val columnIndices: MutableMap<Annotation, Int> = mutableMapOf()
    private var lastLineWasBlank: Boolean = false

    override fun read(): Layer {
        file.forEachLine { line ->
            if (columnIndices.isEmpty()) {
                parseHeader(line)
            } else if (!line.startsWith("#")) {
                parseBody(line)
            }
        }
        newDocument()
        return Layer(documents.toTypedArray())
    }

    /**
     * Retrieve the indices of the literal, lemma and PoS columns from the header. If any were not
     * found, throw.
     *
     * @param line Header line read from the tsv file.
     */
    private fun parseHeader(line: String) {
        val headers = line.split("\t")

        getColumnIndices(headers)

        // Check for the presence of a token
        if (columnIndices[Annotation.TOKEN] == null) {
            throw DocumentInvalidException(file.name, "No token column found in TSV file.")
        }
    }

    /**
     * Set up columnIndices to reflect the header. For each header column, check if it is the name
     * of any of the AnnotationTypes.
     */
    private fun getColumnIndices(headers: List<String>) {
        headers.forEachIndexed { index, header ->
            columnNames.entries
                // from the columnNames, find the first AnnotationType that has a name that matches
                // the
                // header.
                .firstOrNull { (_, names) ->
                    names.any { name -> header.equals(name, ignoreCase = true) }
                    // if it exists, register the index
                }
                ?.let { (annotation, _) -> columnIndices[annotation] = index }
        }
    }

    private fun parseBody(line: String) {
        if (line.isBlank()) {
            newSentence()
            if (lastLineWasBlank) newParagraph()
            lastLineWasBlank = true
            return
        }
        lastLineWasBlank = false

        // Split on tabs
        val values: List<String> = line.split("\t")

        // Retrieve values
        val annotations: Map<Annotation, String> = buildMap {
            // by default put an empty token
            put(Annotation.TOKEN, "")
            // Add annotations values for non empty columns
            for (column in columnIndices.entries) {
                columnOrNull(column.value, values)?.let { put(column.key, it) }
            }
        }
        Term(wordID(), annotations).also {
            terms += it
        }
    }

    // Retrieves a column with bounds checking.
    private fun columnOrNull(i: Int, values: List<String>): String? =
        values.getOrNull(i)?.ifBlank { null }

    companion object {
        val columnNames: Map<Annotation, List<String>> =
            mapOf(
                Annotation.TOKEN to listOf("word", "token", "literal", "term", "form"),
                Annotation.LEMMA to listOf("lemma"),
                Annotation.POS to listOf("pos", "xpos"),
                Annotation.UPOS to listOf("upos"),
                Annotation.DEPREL to listOf("deprel", "dependency"),
                Annotation.HEAD to listOf("head"),
                Annotation.NER to listOf("entity", "ner", "named-entity", "NamedEntity"),
                Annotation.GROUP to listOf("group", "mwe"),
            )
    }
}
