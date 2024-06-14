package org.ivdnt.galahad.port.tei.export

import org.ivdnt.galahad.data.corpus.CorpusMetadata
import org.ivdnt.galahad.data.document.DocumentFormat
import org.ivdnt.galahad.port.LayerTransformer
import org.ivdnt.galahad.port.xml.XMLMetadata
import org.ivdnt.galahad.util.childOrNull
import org.ivdnt.galahad.util.getXmlBuilder
import org.ivdnt.galahad.util.toNonEmptyString
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.InputSource
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.StringReader
import java.util.*
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class TEIMetadata(
        xmlDoc: Document,
        root: Node,
        layer: LayerTransformer,
        val merging: Boolean,
) : XMLMetadata(xmlDoc, root, layer) {

    /** GaLAHaD-generated UUID */
    private val internalPid: String = layer.transformMetadata.document.uuid.toString()

    /**
     * Return the title of the document as described in titleStmt,
     * or the filename without extension if the former is missing.
     */
    private val title: String
        get() {
            return root.childOrNull("teiHeader")
                ?.childOrNull("fileDesc")
                ?.childOrNull("titleStmt")
                ?.childOrNull("title")?.textContent
                ?: // if null, use filename without extension
                layer.transformMetadata.document.getUploadedRawFile().nameWithoutExtension
        }

    private val corpusMetadata: CorpusMetadata = layer.transformMetadata.corpus.metadata.expensiveGet()

    init {
        write()
    }

    fun toPlainXMLText(stream: OutputStream) {
        // For now, we only write the listBibl to stream.
        // Future metadata implementation might need something different?
        // In that case, perhaps use a `nodeName: string` parameter and a recursive node grabber.
        val teiHeader = root.getOrCreateChild("teiHeader")
        val tf: Transformer = TransformerFactory.newInstance().newTransformer()
        tf.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
        tf.setOutputProperty(OutputKeys.INDENT, "yes")
        tf.transform(DOMSource(teiHeader), StreamResult(stream))
    }

    // There could be multiple root nodes in the document.
    // The caller specifies which one.
    private fun write() {
        // Add namespace to root for LAnCeLoT compatibility
        (root as Element).setAttribute("xmlns","http://www.tei-c.org/ns/1.0")

        val teiHeader = root.getOrCreateChild("teiHeader", true)
        // remove namespace for Cobalt compatibility
        teiHeader.removeAttribute("xmlns")
        root.childOrNull("text")?.removeAttribute("xmlns")

        // Add metadata to the document
        addFileDescMetadata(teiHeader)
        addEncodingDescMetadata(teiHeader)
        addProfileDescMetadata(teiHeader)
    }

    /**
     * Add a file description to [teiHeader]:
     * <fileDesc>
     *     <titleStmt>...</titleStmt>
     *     <publicationStmt>...</publicationStmt>
     *     <notesStmt>...</notesStmt>
     *     <sourceDesc>...</sourceDesc>
     * </fileDesc>
     */
    private fun addFileDescMetadata(teiHeader: Element) {
        // <fileDesc>
        val fileDesc = teiHeader.getOrCreateChild("fileDesc")
        // <titleStmt>
        addTitleStmt(fileDesc)
        // <publicationStmt>
        addPublicationStmt(fileDesc)
        // <notesStmt>
        addNotesStmt(fileDesc)
        // <sourceDesc>
        addSourceDesc(fileDesc)
    }

    /**
     * Add title statement to [fileDesc]:
     * <titleStmt>
     *     <title>[title]</title>
     *     <respStmt>...</respStmt>
     *     <respStmt>...</respStmt>
     * </titleStmt>
     */
    private fun addTitleStmt(fileDesc: Element) {
        val titleStmt = fileDesc.getOrCreateChild("titleStmt")
        if (titleStmt.childOrNull("title") == null) {
            // Only add if not already present (when merging).
            titleStmt.createChild("title", title)
        }
        addRespStmt(titleStmt, "linguistic annotation by GaLAHaD (https://portal.clarin.ivdnt.org/galahad)")
        if (merging) {
            addRespStmt(titleStmt, "TEI merged by GaLAHaD (https://portal.clarin.ivdnt.org/galahad)")
        } else {
            addRespStmt(titleStmt, "exported as ${DocumentFormat.TeiP5.identifier} by GaLAHaD (https://portal.clarin.ivdnt.org/galahad)")
        }
    }

    /**
     * Add a responsibility statement to [titleStmt]:
     * <respStmt>
     *     <resp>...</resp>
     *     <orgName xml:lang="nl">Instituut voor de Nederlandse Taal</orgName>
     *     <orgName xml:lang="en">Dutch Language Institute</orgName>
     * </respStmt>
     */
    private fun addRespStmt(titleStmt: Element, resp: String) {
        val respStmt = titleStmt.createChild("respStmt")
        respStmt.createChild("resp", resp)
        respStmt.createChild("orgName", "xml:lang" to "nl", "Instituut voor de Nederlandse Taal")
        respStmt.createChild("orgName", "xml:lang" to "en", "Dutch Language Institute")
    }

    /**
     * Add publication statement to [fileDesc]:
     * <publicationStmt>
     *     <publisher>!Needs to be filled in!</publisher>
     *     <idno type="sourceID">[title]</idno>
     *     <idno type="internalPersistentIdentifier">[internalPid]</idno>
     * </publicationStmt>
     */
    private fun addPublicationStmt(fileDesc: Element) {
        val publicationStmt = fileDesc.getOrCreateChild("publicationStmt")
        if (publicationStmt.childOrNull("publisher") == null) {
            // Only add if not already present (when merging).
            publicationStmt.createChild("publisher", "!Needs to be filled in!")
        }
        publicationStmt.createChild("idno", title, "sourceID")
        publicationStmt.createChild("idno", "${internalPid}_tei", "GaLAHaDPersistentIdentifier")
    }

    /**
     * Add notes statement to [fileDesc]:
     * <notesStmt>
     *     <note type="corpusName">[name]</note>
     *     <note type="sourceCollection">[sourceName]</note>
     *     <note type="sourceCollectionURL">[sourceURL]</note>
     * </notesStmt>
     */
    private fun addNotesStmt(fileDesc: Element) {
        val notesStmt = fileDesc.getOrCreateChild("notesStmt")
        addNote(notesStmt, "corpusName", corpusMetadata.name)
        addNote(notesStmt, "sourceCollection", corpusMetadata.sourceName.toNonEmptyString("!No source name defined!"))
        val url = corpusMetadata.sourceURL.toNonEmptyString("!No source URL defined!")
        addNote(notesStmt, "sourceCollectionURL", url)
    }

    private fun addNote(notesStmt: Element, attrVal: String, textContent: String) {
        notesStmt.createChild("note", mapOf(
            "type" to attrVal,
            "resp" to "GaLAHaD",
        ), textContent)
    }

    /**
     * Add source description to [fileDesc]:
     * <sourceDesc>
     *     <ab>
     *         <idno type="sourceID">[title]</idno>
     *     </ab>
     *     <ab type="date">
     *         <date from="[eraFrom]" to="[eraTo]"/>
     *     </ab>
     * </sourceDesc>
     */
    private fun addSourceDesc(fileDesc: Element) {
        // Only add if not already present (when merging).
        if (fileDesc.childOrNull("sourceDesc") == null) {
            // <sourceDesc>
            val sourceDesc = fileDesc.createChild("sourceDesc")
            // <ab>
            val ab = sourceDesc.createChild("ab")
            // <idno>
            ab.createChild("idno", title, "sourceID")
            // <ab type="date">
            val abDate = sourceDesc.createChild("ab", "", "date")
            // <date>
            abDate.createChild("date", mapOf(
                "from" to corpusMetadata.eraFrom.toString(),
                "to" to corpusMetadata.eraTo.toString(),
            ))
        }
    }

    /**
     * Add encoding description to [teiHeader]:
     * <encodingDesc>
     *    <appInfo>...</appInfo>
     *    <editorialDecl>...</editorialDecl>
     * </encodingDesc>
     */
    private fun addEncodingDescMetadata(teiHeader: Node) {
        // <encodingDesc>
        val encodingDesc = teiHeader.getOrCreateChild("encodingDesc")
        // <appInfo>
        addAppInfo(encodingDesc)
        // <editorialDecl>
        addEditorialDecl(encodingDesc)
    }

    /**
     * Add app information to [encodingDesc]:
     * <appInfo resp="GaLAHaD">
     *     <application @ident @version @xml:id>
     *         <label>POS-tagger and lemmatiser</label>
     *         <ptr @target>
     *     </application>
     * </appInfo>
     */
    private fun addAppInfo(encodingDesc: Node) {
        // <appInfo>
        val appInfo = encodingDesc.createChild("appInfo", "resp" to "GaLAHaD")
        // <application>
        val application = appInfo.createChild("application", mapOf(
            "version" to  layer.tagger.version,
            "ident" to  layer.tagger.id,
            "xml:id" to  layer.tagger.id,
        ))
        // <label>
        application.createChild("label", "POS-tagger and lemmatiser")
        // <ptr>
        application.createChild("ptr", "target" to layer.tagger.model.href)
    }

    /**
     * Add editorial declaration to [encodingDesc]:
     * <editorialDecl resp="GaLAHaD">
     *     <interpretation>
     *         <ab>...</ab> // regular
     *         <ab>...</ab> // provenance
     *     </interpretation>
     * </editorialDecl>
     */
    private fun addEditorialDecl(encodingDesc: Element) {
        val editorialDecl = encodingDesc.createChild("editorialDecl", "resp" to "GaLAHaD")
        val interpretation = editorialDecl.createChild("interpretation", "xml:id" to "A0001")
        // Regular <ab>
        val ab = interpretation.createChild("ab", mapOf(
            "type" to "linguisticAnnotation",
            "subtype" to "POS-tagging_lemmatisation",
        ))
        addInterGrpTo(ab, mapOf(
            "annotationStyle" to "inline",
            "Documentation" to "",
            "annotationSet" to (layer.tagger.tagset ?: ""),
            "annotationDescription" to "The file was automatically annotated within the platform GaLAHaD, which is a central hub for enriching historical Dutch.",
            "annotationFormat" to "TEI xml",
        ))
        // Provenance <ab>
        addProvenanceAb(interpretation)
    }

    /**
     * Add provenance <ab> to [interpretation]:
     * <ab type="linguisticAnnotation" subtype="POS-tagging_lemmatisationProvenance1">
     *     <interpGrp type="annotationMode">
     *         <interp>automatically annotated</interp>
     *     </interpGrp>
     *     <interpGrp type="processor">
     *         <interp sameAs="#[layer.tagger.id]"/>
     *     </interpGrp>
     *     <date from="[date]" to="[date]"/>
     * </ab>
     */
    private fun addProvenanceAb(interpretation: Element) {
        val ab = interpretation.createChild("ab", mapOf(
            "type" to "linguisticAnnotation",
            "subtype" to "POS-tagging_lemmatisationProvenance1",
        ))
        addInterGrpTo(ab, "annotationMode", "automatically annotated")
        // processor interp is special, using @sameAs
        val processor = ab.createChild("interpGrp", "type" to "processor")
        processor.createChild("interp", "sameAs" to "#${layer.tagger.id}")
        // Provenance also has a <date>
        val now = layer.dateFormat.format(Date())
        ab.createChild("date", mapOf(
            "from" to now,
            "to" to now,
        ))
    }

    /**
     * Add profile description to [teiHeader]:
     * <profileDesc>
     *     <langUsage>
     *         <language ident="nl">
     *             Dutch
     *             <interpGrp type="dominantLanguage">
     *                 <interp>true</interp>
     *             </interpGrp>
     *         </language>
     *     </langUsage>
     * </profileDesc>
     */
    private fun addProfileDescMetadata(teiHeader: Node) {
        val profileDesc = teiHeader.getOrCreateChild("profileDesc")
        // Only add if not already present (when merging).
        if (profileDesc.childOrNull("langUsage") == null) {
            val langUsage = profileDesc.createChild("langUsage")
            val language = langUsage.createChild("language", "ident" to "nl", "Dutch")
            addInterGrpTo(language, "dominantLanguage", "true")
        }
    }

    /**
     * Add interpretation group to [node]:
     * <interpGrp type="[key]">
     *     <interp>[value]</interp>
     * </interpGrp>
     */
    private fun addInterGrpTo(node: Node, key: String, value: String) {
        // <interpGrp type="[key]">
        val interpGrp = node.createChild("interpGrp", "", key)
        // <interp>
        interpGrp.createChild("interp", value)
    }

    private fun addInterGrpTo(node: Node, keyValues: Map<String, String>) {
        keyValues.forEach { (key, value) -> addInterGrpTo(node, key, value) }
    }

}