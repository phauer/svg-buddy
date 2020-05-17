package com.phauer.svgfontembedding.processing

import org.apache.commons.codec.binary.Base64
import org.jdom2.CDATA
import org.jdom2.Element
import org.jdom2.Namespace
import org.jdom2.input.SAXBuilder
import org.jdom2.output.Format
import org.jdom2.output.XMLOutputter
import javax.enterprise.context.ApplicationScoped


@ApplicationScoped
class FileEmbedder {
    private val svgNamespace = Namespace.getNamespace("", "http://www.w3.org/2000/svg")

    fun embedFontsIntoSvg(inputSvgString: String, fonts: Collection<GoogleFontsEntry>): String {
        val doc = SAXBuilder(false).build(inputSvgString.byteInputStream())
        val defsTag: Element? = doc.rootElement.getChild("defs", svgNamespace)
        if (defsTag == null) {
            val newDefsTag = Element("defs", svgNamespace)
            newDefsTag.addContent(createStyleTagWithFont(fonts))
            doc.rootElement.addContent(0, newDefsTag)
        } else {
            defsTag.addContent(createStyleTagWithFont(fonts))
        }
        return XMLOutputter(Format.getPrettyFormat()).outputString(doc)
    }

    private fun createStyleTagWithFont(fonts: Collection<GoogleFontsEntry>): Element {
        val css = fonts.joinToString(separator = "") {
            """
            @font-face {
                font-family:'${it.font}';
                src:url('data:application/font-woff2;charset=utf-8;base64,${Base64.encodeBase64String(it.bytes)}') format("woff2");
                font-weight:normal;
                font-style:normal;
            }
        """.trimIndent()
        }
        val styleTag = Element("style", svgNamespace)
        styleTag.setAttribute("type", "text/css")
        styleTag.addContent(CDATA(css))
        return styleTag
    }
}