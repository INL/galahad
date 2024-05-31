package org.ivdnt.galahad.port.tsv.export

import org.ivdnt.galahad.data.layer.Layer
import org.ivdnt.galahad.port.DocumentTransformMetadata
import org.ivdnt.galahad.port.LayerMerger
import org.ivdnt.galahad.port.LayerTransformer
import org.ivdnt.galahad.port.tsv.TSVFile
import java.io.File
import kotlin.io.path.createTempDirectory

/**
 * Do not call directly. Use [TSVFile.merge] instead.
 */
internal open class TSVLayerMerger(
    open val sourceFile: TSVFile,
    transformMetadata: DocumentTransformMetadata,
) : LayerMerger<TSVFile>, LayerTransformer(transformMetadata) {
    val layer = transformMetadata.layer
    val outFile: File = createTempDirectory("teimerge").toFile().resolve(transformMetadata.document.name)
    protected open val hasHeader: Boolean = true
    /**
     * Merge uploaded raw file with tagger layer. Headers indices are already determined by TSVFile.
     * Read in per line, split on tabs, swap out pos & lemma and commit to new file
     */
    override fun merge(): TSVFile {
        sourceFile.parse() // parse the sourceFile if needed.
        parseByLine()
        return TSVFile(outFile)
    }

    protected fun parseByLine() {
        var termIndex = if (hasHeader) -1 else 0 // Start at -1 to take the header into account.
        sourceFile.file.inputStream().bufferedReader().forEachLine { line ->
            if (termIndex == -1) {
                // Copy header to output & continue
                outFile.appendText(line + "\n")
                termIndex++
            } else {
                val columns = line.split("\t").toMutableList()
                if (columns.size >= 3) {
                    // Swap out pos & lemma, keep the rest.
                    replaceColumns(columns, layer, termIndex)
                    outFile.appendText(columns.joinToString("\t") + "\n")
                    termIndex++
                } else {
                    // Output whatever was on that line. Presumably whitespace.
                    outFile.appendText(line + "\n")
                }
            }
        }
    }

    /*
     * Replace the PoS and lemma values in their previously indexed columns.
     */
    protected open fun replaceColumns(
        columns: MutableList<String>, layer: Layer,
        termIndex: Int,
    ) {
        columns[sourceFile.posIndex!!] = layer.terms[termIndex].pos.toString()
        columns[sourceFile.lemmaIndex!!] = layer.terms[termIndex].lemma.toString()
    }
}