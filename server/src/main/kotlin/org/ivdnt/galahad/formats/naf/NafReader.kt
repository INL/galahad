package org.ivdnt.galahad.formats.naf

import java.io.File
import java.util.*
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.Layer
import org.ivdnt.galahad.annotations.Term
import org.ivdnt.galahad.annotations.TermSpan
import org.ivdnt.galahad.formats.reader.LayerReader
import org.ivdnt.galahad.util.XmlUtil
import org.ivdnt.galahad.util.childElements
import org.ivdnt.galahad.util.childOrNull

typealias WordformID = String

typealias TermID = String

class NafReader(file: File) : LayerReader() {
    private val xml = XmlUtil.builder.parse(file)
    private val root = xml.documentElement
    private val nafWordforms =
        root.childOrNull("text")!!.childElements.map {
            NafWordform(
                id = it.getAttribute("id"),
                offset = it.getAttribute("offset").toInt(),
                token = it.textContent,
                sent = it.getAttribute("sent").toInt(),
                para = it.getAttribute("para").toIntOrNull(),
            )
        }
    private val nafTerms =
        root
            .childOrNull("terms")!!
            .childElements
            .map {
                NafTerm(
                    id = it.getAttribute("id"),
                    lemma = it.getAttribute("lemma").ifEmpty { null },
                    pos = it.getAttribute("pos").ifEmpty { null },
                    targets =
                        it.childElements
                            .first()
                            .childElements
                            .map { it.getAttribute("id") }
                            .toList(),
                )
            }
            .toList()
    private val nafDeps =
        root
            .childOrNull("deps")
            ?.childElements
            ?.map {
                NafDep(
                    from = it.getAttribute("from"),
                    to = it.getAttribute("to"),
                    rfunc = it.getAttribute("rfunc"),
                )
            }
            ?.toList()
    private val nafEntities =
        root
            .childOrNull("entities")
            ?.childElements
            ?.map {
                NafEntity(
                    type = it.getAttribute("type").ifEmpty { null },
                    references =
                        it.childOrNull("references")
                            ?.childElements
                            ?.map { it.childElements.map { it.getAttribute("id") }.toList() }
                            ?.toList()!!,
                )
            }
            ?.toList()
    private val id: String =
        root
            .childOrNull("nafHeader")
            ?.childOrNull("public")
            ?.getAttribute("publicId")
            .orEmpty()
            .ifEmpty { UUID.randomUUID().toString() }

    override fun read(): Layer {
        // group wordforms paragraph, then sentence, then sort by offset in sentence
        val grouped =
            nafWordforms
                .groupBy { it.para }
                .map { it.value.groupBy { it.sent }.map { it.value.sortedBy { it.offset } } }
        grouped.forEach { para ->
            para.forEach { sent ->
                sent.forEachIndexed { i, wordform ->
                    // retrieve term, entity, and dependencies
                    val term = nafTerms.find { wordform.id in it.targets }!!
                    val entity = nafEntities?.find { it.references.any { term.id in it } }
                    val dep: NafDep? =
                        if (nafDeps.isNullOrEmpty()) {
                            null
                        } else {
                            nafDeps.firstOrNull { it.to == term.id } ?: NafDep("0", "0", "root")
                        }

                    // annotations
                    val annotations = mutableMapOf<Annotation, String>()
                    annotations[Annotation.TOKEN] = wordform.token
                    term.lemma?.let { annotations[Annotation.LEMMA] = it }
                    term.pos?.let { annotations[Annotation.POS] = it }
                    entity?.type?.let { annotations[Annotation.NER] = it }
                    dep?.rfunc?.let { annotations[Annotation.DEPREL] = it }

                    if (dep?.from == "0") {
                        annotations[Annotation.HEAD] = "0"
                    } else {
                        val headTerm = nafTerms.find { it.id == dep?.from }
                        val headWordform = nafWordforms.find { it.id == headTerm?.targets?.first() }
                        headWordform?.let {
                            annotations[Annotation.HEAD] =
                                (sent.indexOf(headWordform) + 1).toString()
                        }
                    }

                    // space after
                    val nextWordform = sent.getOrNull(i + 1)
                    val spaceAfter =
                        nextWordform?.offset != (wordform.offset + wordform.token.length)

                    terms += Term(wordform.id, annotations, spaceAfter)
                }
                // collect all spans that refer to one of the terms in this sentence
                val termIds = terms.map { it.id }
                val nerSpans = nafEntities?.flatMap { e -> e.references.map { e.type!! to it } }
                nerSpans
                    ?.filter { (_, ids) -> ids.any { it in termIds } }
                    ?.ifEmpty { null }
                    ?.map { (value, ids) ->
                        TermSpan(ids.map { id -> sent.indexOfFirst { it.id == id } }, value)
                    }
                    ?.toMutableList()
                    ?.let { spans[Annotation.NER] = it }

                newSentence()
            }
            newParagraph()
        }
        newDocument()
        return Layer(documents.toTypedArray(), id)
    }

    override fun newSentence() {
        // edit the NER value of the terms if spans are present
        spans[Annotation.NER]?.forEach { span ->
            span.indices.forEachIndexed { spanI, termI ->
                // Note the difference spanI and termI; e.g. span.indices = [4, 5]; so (0, 4) = (1,
                // 5)
                val t = terms[termI]
                val iob = (if (spanI == 0) "B-" else "I-") + span.value
                terms[termI] = Term(t.id, t.annotations + (Annotation.NER to iob), t.spaceAfter)
            }
        }
        super.newSentence()
    }

    data class NafWordform(
        val id: WordformID,
        val offset: Int,
        val token: String,
        val sent: Int,
        val para: Int?,
    )

    data class NafTerm(
        val id: TermID,
        val lemma: String?,
        val pos: String?,
        val targets: List<WordformID>,
    )

    data class NafDep(val from: TermID, val to: TermID, val rfunc: String)

    data class NafEntity(val type: String?, val references: List<List<TermID>>)
}
