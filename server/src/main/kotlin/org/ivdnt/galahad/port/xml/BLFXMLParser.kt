package org.ivdnt.galahad.port.xml

import org.ivdnt.galahad.data.document.DocumentFormat
import org.ivdnt.galahad.data.document.SOURCE_LAYER_NAME
import org.ivdnt.galahad.data.layer.Layer
import org.ivdnt.galahad.data.layer.Term
import org.ivdnt.galahad.data.layer.WordForm
import org.ivdnt.galahad.port.BLFXML
import org.ivdnt.galahad.util.getXmlBuilder
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import java.io.OutputStream
import java.io.StringWriter
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathExpression
import javax.xml.xpath.XPathFactory

fun reparseText(text: String): String {
    return text.replace(Regex("\n"), " ").replace(Regex("\\s+"), " ")
}

fun Node.tagName(): String? {
    return if( this.nodeType == Node.ELEMENT_NODE ) (this as Element).tagName else null
}

/**
 * Should the text inside this node be interpreted as source text?
 * Assumes we are already inside a text container e.g. <text>
 */
private fun Node.isTextable(): Boolean {
    if( this.tagName() == "note" && this.attributes.getNamedItem("type")?.textContent == "editorial" ) {
        // ignore editorial notes
        return false
    }
    return when( this.tagName() ) {
        // This list was generated with help from https://github.com/kohsuke/rngom
        "extent", "figDesc", "ident", "del", "t",
        "oVar", "secl", "quote", "subc", "lem", "abbr",
        "meeting", "height", "ab", "am", "etym", "geogFeat",
        "idno", "re", "roleDesc", "nationality", "pRef",
        "domain", "roleName", "accMat", "rs", "role",
        "dateline", "activity", "hom", "docEdition", "syll",
        "term", "acquisition", "email", "creation",
        "altIdent", "c", "surplus", "interp", "g", "sex",
        "gram", "binaryObject", "cl", "alternate", "dictScrap",
        "headItem", "l", "xenoData", "m", "distributor",
        "closer", "p", "actor", "q", "factuality", "s",
        "stage", "rdg", "orig", "u", "series", "w",
        "musicNotation", "funder", "cRefPattern", "country",
        "measureGrp", "dim", "language", "source", "charName",
        "metSym", "emph", "objectType", "forename", "institution", "textLang", "view", "scriptNote", "nameLink", "rendition", "reg", "affiliation", "catDesc", "entryFree", "add", "tech", "eg", "pVar", "socecStatus", "collection", "iType", "geoDecl", "localName", "ex", "measure", "district", "authority", "incipit", "name", "region", "support", "classRef", "desc", "biblScope", "mood", "origin", "fDescr", "edition", "title", "sic", "locale", "expan", "floruit", "salute", "surrogates", "fw", "fsDescr", "genName", "addrLine", "street", "titlePart", "byline", "summary", "classCode", "castItem", "valDesc", "certainty", "label", "layout", "material", "form", "hyph", "formula", "time", "persName", "placeName", "age", "colloc", "bibl", "hi", "mod", "education", "occupation", "channel", "caption", "rubric", "prefixDef", "repository", "signatures", "filiation", "number", "handNote", "headLabel", "docAuthor", "per", "case", "soCalled", "val", "item", "mapping", "watermark", "msName", "birth", "signed", "sense", "explicit", "tns", "damage", "secFol", "stamp", "geogName", "addName", "cell", "foreign", "settlement", "geo", "trailer", "catchwords", "gen", "defaultVal", "pron", "surname", "writing", "memberOf", "camera", "editor", "stress", "metamark", "additions", "supplied", "change", "foliation", "unicodeName", "depth", "preparedness", "width", "constraint", "phr", "imprimatur", "span", "date", "glyphName", "corr", "retrace", "num", "precision", "sound", "usg", "zone", "oRef", "tag", "pubPlace", "collation", "residence", "witDetail", "licence", "orgName", "orth", "offset", "elementRef", "postBox", "author", "egXML", "rhyme", "origDate", "bloc", "sequence", "condition", "opener", "speaker", "lbl", "publisher", "postCode", "gramGrp", "note", "sponsor", "colophon", "code", "langKnown", "decoNote", "constitution", "death", "custEvent", "purpose", "docImprint", "line", "unclear", "distinct", "faith", "heraldry", "tagUsage", "respons", "head", "principal", "provenance", "gloss", "mentioned", "lang", "said", "value", "typeNote", "wit", "restore", "resp", "finalRubric", "docDate", "witness", "pc", "interaction", "derivation", "locus", "origPlace", "citedRange"
        -> true
        null, "ref", "seg" -> this.parentNode?.isTextable() ?: false // null is for text nodes
        else -> this.tagName()?.startsWith("t-") ?: false
    }
}

/**
 * Like node.textContent, but return only what is considered plaintext according to Node.isTextable
 * Use to extract all the text, but this is too expensive for large documents
 */
fun Node.getPlainTextContent(): String {
    var ret = if (this.nodeType == Node.TEXT_NODE) this.textContent else ""
    for( i in 0 until this.childNodes.length ) {
        ret += if( this.childNodes.item( i ).nodeType == Node.TEXT_NODE ) {
            if (this.isTextable()) this.childNodes.item(i).textContent else ""
        } else {
            this.childNodes.item(i).getPlainTextContent()
        }
    }
    return ret
}

class BLFXMLParser (
    private val plainTextOutputStream: OutputStream,
    private val blf: BLFXML,
    val xmlDocument: Document,
    format: DocumentFormat,
    val nodeHandler: ((node: Node, offset: Int, document: Document) -> Unit )
) {

    private var plaintextTail = "" // ugly

    val sourceLayer = Layer( SOURCE_LAYER_NAME )
    var offset = 0
    val rootNodes: NodeList

    private val documentPathExpression: XPathExpression
        get() = xPath.compile("${blf.documentPath}")
    private val containerPathExpression
        get() = xPath.compile("${blf.annotatedFields?.contents?.containerPath ?: '.'}")

    // I wrote a somewhat more elegant implementation with the XPaths, but it turned out to be too slow
    // So we will just use these efficient extractors.
    private val lemmaExtractor: (node: Node) -> String?
    private val literalExtractor: (node: Node) -> String
    private val posExtractor: (node: Node) -> String?
    private val idExtractor: (node: Node) -> String?

    init {

        idExtractor = { node -> node.attributes.getNamedItem("xml:id")?.textContent }
        val getPlainText = { node: Node ->
            var literal = ""
            for( i in 0 until node.childNodes.length ) {
                val childNode = node.childNodes.item(i)
                if( childNode.isTextable() ) {
                    literal += childNode.textContent
                    // We recurse 1 level deep here, but in theory an arbitrary number of recursions could be needed
                }
            }
            literal.replace(Regex("""\s"""),"")
        }
        when( format ) {
            DocumentFormat.TeiP5 -> {
                lemmaExtractor = { node -> node.attributes.getNamedItem("lemma")?.textContent }
                literalExtractor = { node -> getPlainText(node) }
                posExtractor = { node -> node.attributes.getNamedItem("pos")?.textContent }

            }
            DocumentFormat.TeiP4Legacy, DocumentFormat.TeiP5Legacy -> {
                lemmaExtractor = { node -> node.attributes.getNamedItem("lemma")?.textContent }
                literalExtractor = { node -> getPlainText(node) }
                posExtractor = {
                    // Note that this is not actually according to a well-defined format
                    // But supports most cases
                    node -> node.attributes.getNamedItem("pos")?.textContent
                    ?: node.attributes.getNamedItem("type")?.textContent
                }
            }
            else -> throw Exception("This format is not supported")
        }

        val nodeList = documentPathExpression.evaluate(xmlDocument, XPathConstants.NODESET) as NodeList

        rootNodes = nodeList

        for (i in 0 until nodeList.length) {
            handleDocumentNode( nodeList.item(i) )
        }
    }

    companion object {

        val xPath: XPath = XPathFactory.newInstance().newXPath()

        fun forFileWithFormat(
            format: DocumentFormat,
            file: File,
            plainTextOutputStream: OutputStream,
            nodeHandler: (node: Node, offset: Int, document: Document) -> Unit
        ): BLFXMLParser {
            val xmlDocument = getXmlBuilder().parse(file)
            return forFormat(format, plainTextOutputStream, xmlDocument, nodeHandler)
        }

        fun forFormat(
            format: DocumentFormat,
            plainTextOutputStream: OutputStream,
            xmlDocument: Document,
            nodeHandler: (node: Node, offset: Int, document: Document) -> Unit
        ): BLFXMLParser {
            return when ( format ) {
                DocumentFormat.TeiP4Legacy -> BLFXMLParser(
                    plainTextOutputStream,
                    BLFXML.from(File("data/formats/tei-p4-legacy.blf.yaml")),
                    xmlDocument,
                    format,
                    nodeHandler
                )
                DocumentFormat.TeiP5Legacy -> BLFXMLParser(
                    plainTextOutputStream,
                    blf = BLFXML.from(File("data/formats/tei-p5-legacy.blf.yaml")),
                    xmlDocument = xmlDocument,
                    format = format,
                    nodeHandler = nodeHandler
                )
                DocumentFormat.TeiP5 -> BLFXMLParser(
                    plainTextOutputStream,
                    BLFXML.from(File("data/formats/tei-p5.blf.yaml")),
                    xmlDocument,
                    format,
                    nodeHandler
                )
                else -> throw Exception("Unsupported format $format in BLFXMLReader")
            }
        }

        fun xmlToString(pretty: Boolean, xmlDocument: Document): String {
            val writer = StringWriter()
            val transformer = if ( pretty ) TransformerFactory.newInstance()
                .newTransformer(StreamSource(this::class.java.classLoader.getResourceAsStream("exporttemplates/tei.xslt")))
            else TransformerFactory.newInstance().newTransformer()
            if( pretty ) {
                transformer.setOutputProperty(OutputKeys.STANDALONE, "no") // ??
            }
            transformer.transform(DOMSource(xmlDocument), StreamResult(writer))
            return writer.buffer.toString()
        }
    }



    private fun addPlaintext(literal: String) {
        plainTextOutputStream.write( literal.toByteArray() )
        offset += literal.length

        if( literal.length < 2 ) { // so ugly
            plaintextTail += literal
        } else {
            plaintextTail = literal
        }
    }

    private fun handleDocumentNode( node: Node ) {
        val nodeList = containerPathExpression.evaluate( node, XPathConstants.NODESET) as NodeList
        for (i in 0 until nodeList.length) {
            handleNode( nodeList.item(i) )
        }
    }

    /**
     * recursive
     */
    private fun handleNode( node: Node ) {
        if( node.nodeType == Node.ELEMENT_NODE ) {
            // custom rendering and parsing
            when( node.tagName() ) {
                "lb", "p" -> addPlaintext("\n")
            }

            // We are going to add nodes when exporting, but we don't want to iterate over them
            // So better store references to the current nodes and use them
            val oldChildNodes = ArrayList<Node>()
            for (i in 0 until node.childNodes.length) {
                oldChildNodes.add( node.childNodes.item(i) )
            }

            for( child in oldChildNodes ) {
                if( child.tagName() == "w" || child.tagName() == "pc" ) { handleWordOrPunctNode( child ); continue } // Word node handles itself, so don't recurse
                // Recursion
                handleNode( child )
                if ( child.isTextable() ) {
                    // custom node handling
                    nodeHandler(node, offset, xmlDocument)
                    handleTextableNode(child)
                } else {
                    handleNonTextableNode(child)
                }
            }
        } else if( node.nodeType == Node.TEXT_NODE ) {
            if( node.isTextable() ) {
                node.textContent = reparseText(node.textContent)
                nodeHandler(node, offset, xmlDocument)
            }
        }
    }

    private fun handleNonTextableNode( node: Node ) {
        // TODO: rethink this
        if( !plaintextTail.endsWith("\n\n") ) addPlaintext("\n")
    }


    private fun handleTextableNode( node: Node ) {
        // TEXT_NODE
        if (node.nodeType == Node.TEXT_NODE) addPlaintext(node.textContent)
    }

    private fun handleWordOrPunctNode( node: Node ) {
        // Handle cases like <w>a</w><w>b</w> -> "a b" (add space in plaintext)
        val needsSpacing = node.tagName() == "w" && plaintextTail.isNotBlank() && !Regex("""\s$""").containsMatchIn(plaintextTail)
        val spaceOffset = if (needsSpacing) 1 else 0
        val trueWordOffset = offset + spaceOffset

        // Handle merging
        nodeHandler(node, trueWordOffset, xmlDocument)

        // Extraction
        val literal = literalExtractor(node).trim() // wordPathExpression.evaluate( node )
        val lem = lemmaExtractor(node) // lemPathExpression.evaluate( node )
        val pos = posExtractor(node) // posPathExpression.evaluate( node )
        val id = idExtractor(node)

        // Add the word to the source layer
        val wordForm = WordForm(literal, trueWordOffset, literal.length, id ?: "no-id" )
        val term = Term(lem, pos, mutableListOf(wordForm))
        sourceLayer.wordForms.add(wordForm)
        sourceLayer.terms.add(term)

        // Add the word to the plaintext
        var text = literal.trim()
        if (needsSpacing) {
            text = " $text"
        }
        addPlaintext(text)
    }

    fun xmlToString(pretty: Boolean): String {
        return xmlToString(pretty, xmlDocument)
    }

}