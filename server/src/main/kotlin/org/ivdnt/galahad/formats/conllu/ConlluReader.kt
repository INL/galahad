package org.ivdnt.galahad.formats.conllu

import java.io.File
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.Term
import org.ivdnt.galahad.formats.reader.LineReader
import org.ivdnt.galahad.layer.Layer

class ConlluReader(val file: File) : LineReader() {
    private val ignorableMultiWordIds: MutableList<String> = mutableListOf()

    private val String.id: String?
        get() = idRegex.find(this)?.groupValues?.get(1) // 0 is the whole match

    override fun read(): Layer {
        file.forEachLine {
            when {
                it.startsWith("# newdoc") -> {
                    newDocument()
                    // get ID last, so we don't overwrite it while creating a new unit
                    docID = it.id
                }

                it.startsWith("# newpar") -> {
                    newParagraph()
                    // get ID last, so we don't overwrite it while creating a new unit
                    parID = it.id
                }

                it.startsWith("# sent_id") || it.isBlank() -> {
                    newSentence()
                    // get ID last, so we don't overwrite it while creating a new unit
                    sentID = it.id
                }

                !it.startsWith("#") -> {
                    newWord(it)
                }
            }
        }
        // create a document for the remaining tokens
        newDocument()
        return Layer(documents)
    }

    override fun newSentence() {
        ignorableMultiWordIds.clear()
        super.newSentence()
    }

    private fun parseMultiWordToken(string: String) {
        // val parent = wordforms.last()
        // we could create multiple analysis tokens like PD+NOU-C here.
    }

    private fun newWord(string: String) {
        // split on tabs
        val fields = string.split("\t")
        val id = fields[0]
        if (id.contains(".")) return // ignore empty nodes
        if (id.contains("-")) {
            // remember the range of multi-word tokens
            val range = id.split("-")
            val start = range[0].toInt()
            val end = range[1].toInt()
            for (i in start..end) {
                ignorableMultiWordIds.add(i.toString())
            }
        }
        if (id in ignorableMultiWordIds)
            return parseMultiWordToken(string) // ignore multi-word tokens

        newTerm(fields)
    }

    private fun getColumn(annotation: Annotation, fields: List<String>): String? {
        if (annotation == Annotation.UPOS) return getUpos(fields)
        if (annotation == Annotation.NER) return getNER(fields)
        if (annotation == Annotation.GROUP) return getGroup(fields)
        if (annotation == Annotation.TOKEN) return getColumnRaw(indices[Annotation.TOKEN]!!, fields)
        return getColumn(indices[annotation]!!, fields)
    }

    /** Retrieve the NER from the MISC column and convert it to IOB. */
    private fun getNER(values: List<String>): String? {
        val misc = getColumn(9, values) ?: return null

        // nerKeyValue is for example "NamedEntity=S-LOC"
        val nerKeyValue: String =
            misc.split("|").firstOrNull { it.split("=").first() in nerAttrNames } ?: return null
        val nerValue: String = nerKeyValue.substringAfter('=')

        // convert to IOB
        // Replace /^S\-/ with B- and /^E\-/ with I-.
        // E.g.: S-LOC -> B-LOC, E-LOC -> I-LOC
        val replaceS = Regex("^S-")
        val replaceE = Regex("^E-")
        val nerIOB = nerValue.replace(replaceS, "B-").replace(replaceE, "I-")

        return nerIOB
    }

    /** Retrieve the GROUP from the MISC column */
    private fun getGroup(values: List<String>): String? {
        val misc = getColumn(9, values) ?: return null

        // groupKeyValue is for example "NamedEntity=S-LOC"
        val groupKeyValue: String =
            misc.split("|").firstOrNull { it.split("=").first() in groupAttrNames } ?: return null
        val groupValue: String = groupKeyValue.substringAfter('=')
        return groupValue
    }

    /** returns null on _ */
    private fun getColumn(i: Int, fields: List<String>): String? =
        getColumnRaw(i, fields)?.takeIf { it != "_" }

    /** returns the raw value, even if it is "_" */
    private fun getColumnRaw(i: Int, fields: List<String>): String? = fields.getOrNull(i)

    private fun getUpos(fields: List<String>): String? {
        val head: String =
            getColumn(3, fields) ?: return null // if no head, ignore features and return
        val features: String? = getColumn(5, fields)
        return if (features != null) {
            "$head($features)"
        } else {
            head
        }
    }

    private fun newTerm(fields: List<String>) {
        val spaceAfter = !fields[9].contains("SpaceAfter=No")

        val annotations = mutableMapOf<Annotation, String>()
        for (column in indices.keys) {
            getColumn(column, fields)?.let { annotations[column] = it }
        }
        terms += Term(wordID(), annotations, spaceAfter)
    }

    companion object {
        /** Supported names for the ner attribute in the MISC column. */
        private val nerAttrNames: List<String> = listOf("NamedEntity", "ner")
        private val groupAttrNames: List<String> = listOf("group")
        private val idRegex = Regex("""id = (\S+)""")

        private val indices: Map<Annotation, Int> =
            mapOf(
                Annotation.TOKEN to 1,
                Annotation.LEMMA to 2,
                Annotation.POS to 4,
                Annotation.HEAD to 6,
                Annotation.DEPREL to 7,
                Annotation.UPOS to -1, // see GetUpos
                Annotation.NER to -1, // see GetNer
                Annotation.GROUP to -1, // see GetGroup
            )
    }
}
