package org.ivdnt.galahad.formats.tei

import java.text.SimpleDateFormat
import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.export.DocumentExport
import org.ivdnt.galahad.formats.reader.PrettyXMLWriter
import org.ivdnt.galahad.layer.Layer.Companion.SOURCE_LAYER
import org.ivdnt.galahad.util.ifNullOrBlank
import org.ivdnt.galahad.util.toVarArg
import org.ivdnt.galahad.util.withoutFormatExt

class TeiMetadataWriter(val writer: PrettyXMLWriter, val export: DocumentExport) {
    val title = export.document.sourceFile.withoutFormatExt
    val pid = export.layer.id
    val corpusName = export.corpus.metadata.name
    val sourceName =
        export.corpus.metadata.source?.name.ifNullOrBlank { "!No source name defined!" }
    val sourceURL =
        export.corpus.metadata.source?.url?.toString().ifNullOrBlank { "!No source URL defined!" }
    val eraFrom = export.corpus.metadata.period?.from.toString()
    val eraTo = export.corpus.metadata.period?.to.toString()
    val language = export.corpus.metadata.language.ifNullOrBlank { "!No language defined!" }
    val langCode = export.corpus.metadata.langCode
    val today = SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis())
    val annotationSet =
        if (export.tagger.name == SOURCE_LAYER)
            export.corpus.metadata.tagset.ifNullOrBlank { "!No tagset defined!" }
        else export.tagger.principleNames

    fun write() {
        writer.wrapIn("teiHeader") {
            addFileDescMetadata()
            addEncodingDescMetadata()
            addProfileDescMetadata()
        }
    }

    /**
     * Add a file description to [teiHeader]: <fileDesc> <titleStmt>...</titleStmt>
     * <publicationStmt>...</publicationStmt> <notesStmt>...</notesStmt>
     * <sourceDesc>...</sourceDesc> </fileDesc>
     */
    private fun addFileDescMetadata() {
        // <fileDesc>
        writer.wrapIn("fileDesc") {
            // <titleStmt>
            addTitleStmt()
            // <publicationStmt>
            addPublicationStmt()
            // <notesStmt>
            addNotesStmt()
            // <sourceDesc>
            addSourceDesc()
        }
    }

    /**
     * Add title statement to [fileDesc]: <titleStmt> <title>[title]</title>
     * <respStmt>...</respStmt> <respStmt>...</respStmt> </titleStmt>
     */
    private fun addTitleStmt() {
        writer.wrapIn("titleStmt") {
            writer.writeElement("title", title)
            addRespStmt("linguistic annotation by GaLAHaD (https://galahad.ivdnt.org)")
            addRespStmt(
                "exported as ${DocumentFormat.TeiP5.identifier} by GaLAHaD (https://galahad.ivdnt.org)"
            )
        }
    }

    /**
     * Add a responsibility statement to [titleStmt]: <respStmt> <resp>...</resp> <orgName
     * xml:lang="nl">Instituut voor de Nederlandse Taal</orgName> <orgName xml:lang="en">Dutch
     * Language Institute</orgName> </respStmt>
     */
    private fun addRespStmt(resp: String) {
        writer.wrapIn("respStmt") {
            writer.writeElement("resp", resp)
            // TODO check namespace
            writer.writeElement("orgName", "xml:lang" to "nl", "Instituut voor de Nederlandse Taal")
            writer.writeElement("orgName", "xml:lang" to "en", "Dutch Language Institute")
        }
    }

    /**
     * Add publication statement to [fileDesc]: <publicationStmt> <publisher>!Needs to be filled
     * in!</publisher> <idno type="sourceID">[title]</idno> <idno
     * type="internalPersistentIdentifier">[internalPid]</idno> </publicationStmt>
     */
    private fun addPublicationStmt() {
        writer.wrapIn("publicationStmt") {
            writer.writeElement("publisher", "!Needs to be filled in!")
            writer.writeElement("idno", "sourceID", title)
            writer.writeElement("idno", "GaLAHaDPersistentIdentifier", "${pid}_tei")
        }
    }

    /**
     * Add notes statement to [fileDesc]: <notesStmt> <note type="corpusName">[name]</note> <note
     * type="sourceCollection">[sourceName]</note> <note
     * type="sourceCollectionURL">[sourceURL]</note> </notesStmt>
     */
    private fun addNotesStmt() {
        writer.wrapIn("notesStmt") {
            addNote("corpusName", corpusName)
            addNote("sourceCollection", sourceName)
            addNote("sourceCollectionURL", sourceURL)
        }
    }

    private fun addNote(attrVal: String, text: String) {
        writer.writeElement("note", mapOf("resp" to "GaLAHaD", "type" to attrVal), text)
    }

    /**
     * Add source description to [fileDesc]: <sourceDesc> <ab> <idno type="sourceID">[title]</idno>
     * </ab> <ab type="date"> <date from="[eraFrom]" to="[eraTo]"/> </ab> </sourceDesc>
     */
    private fun addSourceDesc() {
        writer.wrapIn("sourceDesc") {
            writer.wrapIn("ab") { writer.writeElement("idno", "sourceID", title) }
            writer.wrapIn("ab", "type" to "date") {
                writer.writeEmptyElement("date", mapOf("from" to eraFrom, "to" to eraTo))
            }
        }
    }

    /**
     * Add encoding description to [teiHeader]: <encodingDesc> <appInfo>...</appInfo>
     * <editorialDecl>...</editorialDecl> </encodingDesc>
     */
    private fun addEncodingDescMetadata() {
        // <encodingDesc>
        writer.wrapIn("encodingDesc") {
            // <appInfo>
            addAppInfo()
            // <editorialDecl>
            addEditorialDecl()
        }
    }

    /**
     * Add app information to [encodingDesc]: <appInfo resp="GaLAHaD">
     * <application @ident @version @xml:id> <label>POS-tagger and lemmatiser</label> <ptr @target>
     * </application> </appInfo>
     */
    private fun addAppInfo() {
        // <appInfo>
        writer.wrapIn("appInfo", "resp" to "GaLAHaD") {
            // <application>
            writer.wrapIn(
                "application",
                *mapOf(
                        "xml:id" to export.tagger.name,
                        "ident" to export.tagger.name,
                        // "version" to export.tagger.version,
                    )
                    .toVarArg(),
            ) {
                // <label>
                writer.writeElement("label", "POS-tagger and lemmatiser")
                // <ptr>
                writer.writeEmptyElement("ptr", mapOf("target" to export.tagger.name))
            }
        }
    }

    /**
     * Add editorial declaration to [encodingDesc]: <editorialDecl resp="GaLAHaD"> <interpretation>
     * <ab>...</ab> // regular <ab>...</ab> // provenance </interpretation> </editorialDecl>
     */
    private fun addEditorialDecl() {
        writer.wrapIn("editorialDecl", "resp" to "GaLAHaD") {
            writer.wrapIn("interpretation", "xml:id" to "A0001") {
                // Regular <ab>
                writer.wrapIn(
                    "ab",
                    *mapOf(
                            "type" to "linguisticAnnotation",
                            "subtype" to "POS-tagging_lemmatisation",
                        )
                        .toVarArg(),
                ) {
                    addInterpGrpTo(
                        mapOf(
                            "annotationStyle" to "inline",
                            "Documentation" to "",
                            "annotationSet" to annotationSet,
                            "annotationDescription" to
                                "The file was automatically annotated within the platform GaLAHaD, which is a central hub for enriching historical Dutch.",
                            "annotationFormat" to "TEI xml",
                        )
                    )
                }
                // Provenance <ab>
                addProvenanceAb()
            }
        }
    }

    /**
     * Add provenance <ab> to [interpretation]: <ab type="linguisticAnnotation"
     * subtype="POS-tagging_lemmatisationProvenance1"> <interpGrp type="annotationMode">
     * <interp>automatically annotated</interp> </interpGrp> <interpGrp type="processor"> <interp
     * sameAs="#[export.tagger.id]"/> </interpGrp> <date from="[date]" to="[date]"/> </ab>
     */
    private fun addProvenanceAb() {
        writer.wrapIn(
            "ab",
            *mapOf(
                    "type" to "linguisticAnnotation",
                    "subtype" to "POS-tagging_lemmatisationProvenance1",
                )
                .toVarArg(),
        ) {
            addInterpGrpTo("annotationMode", "automatically annotated")
            // processor interp is special, using @sameAs
            writer.wrapIn("interpGrp", "type" to "processor") {
                writer.writeEmptyElement("interp", mapOf("sameAs" to "#${export.tagger.name}"))
            }
            // Provenance also has a <date>
            writer.writeEmptyElement("date", mapOf("from" to today, "to" to today))
        }
    }

    /**
     * Add profile description to [teiHeader]: <profileDesc> <langUsage> <language ident="nl"> Dutch
     * <interpGrp type="dominantLanguage"> <interp>true</interp> </interpGrp> </language>
     * </langUsage> </profileDesc>
     */
    private fun addProfileDescMetadata() {
        writer.wrapIn("profileDesc") {
            writer.wrapIn("langUsage") {
                writer.wrapIn("language", "ident" to langCode) {
                    writer.writeCharacters(language, true)
                    writer.writeNewLine()
                    addInterpGrpTo("dominantLanguage", "true")
                }
            }
        }
    }

    /**
     * Add interpretation group to [node]: <interpGrp type="[key]"> <interp>[value]</interp>
     * </interpGrp>
     */
    private fun addInterpGrpTo(key: String, value: String?) {
        // <interpGrp type="[key]">
        writer.wrapIn("interpGrp", "type" to key) {
            // <interp>
            if (value.isNullOrEmpty()) {
                // only write key in self-closing tag
                writer.writeEmptyElement("interp")
            } else {
                writer.writeElement("interp", value)
            }
        }
    }

    private fun addInterpGrpTo(keyValues: Map<String, String>) {
        keyValues.forEach { (key, value) -> addInterpGrpTo(key, value) }
    }
}
