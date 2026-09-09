package org.ivdnt.galahad.formats.conllu

import org.ivdnt.galahad.annotations.Annotation
import java.io.OutputStream
import java.io.PrintWriter
import org.ivdnt.galahad.annotations.Term
import org.ivdnt.galahad.export.DocumentExport
import org.ivdnt.galahad.export.LayerWriter

class ConlluWriter(export: DocumentExport) : LayerWriter(export) {
    override fun convert(out: OutputStream): Unit = convert(PrintWriter(out))

    private fun convert(out: PrintWriter) {
        // write the annotation layer to the output stream
        documents.forEachIndexed { docI, doc ->
            out.println("# newdoc id = ${doc.id}")
            doc.paragraphs.forEachIndexed { parI, par ->
                out.println("# newpar id = ${par.id}")
                par.sentences.forEachIndexed { sentI, sent ->
                    out.println("# sent_id = ${sent.id}")
                    out.println("# text = $sent")
                    sent.terms.forEachIndexed { termI, t ->
                        val token = t.token
                        val lemma = t.lemma ?: "_"
                        val upos = t.upos?.let { t.annotationHead(Annotation.UPOS) } ?: "_"
                        val xpos = t.pos ?: "_"
                        val feats = t.upos?.let { t.features(Annotation.UPOS) } ?: "_"
                        val deprel = t.deprel ?: "_"
                        val deps = "_"
                        val head = t.head ?: "_"

                        // Misc in the form spaceAfter=NO|NamedEntity=ORG (depending on the nullable
                        // values)
                        val spaceAfter = if (t.spaceAfter == false) "SpaceAfter=No" else null
                        val ner = t.ner?.let { "NamedEntity=$it" }
                        val misc = listOfNotNull(spaceAfter, ner).joinToString("|").ifEmpty { "_" }

                        val fields =
                            listOf(
                                termI + 1,
                                token,
                                lemma,
                                upos,
                                xpos,
                                feats,
                                head,
                                deprel,
                                deps,
                                misc,
                            )
                        out.println(fields.joinToString("\t"))
                    }
                    // empty line between sentences
                    val isLastSentence =
                        docI == documents.lastIndex &&
                            parI == doc.paragraphs.lastIndex &&
                            sentI == par.sentences.lastIndex
                    if (!isLastSentence) out.println()
                }
            }
        }
        out.flush()
    }
}
