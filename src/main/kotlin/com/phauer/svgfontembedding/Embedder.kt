package com.phauer.svgfontembedding

import org.apache.commons.codec.binary.Base64
import org.jdom2.CDATA
import org.jdom2.Element
import org.jdom2.Namespace
import org.jdom2.input.SAXBuilder
import org.jdom2.output.Format
import org.jdom2.output.XMLOutputter
import javax.enterprise.context.ApplicationScoped


@ApplicationScoped
class Embedder {
    private val svgNamespace = Namespace.getNamespace("", "http://www.w3.org/2000/svg")

    // TODO write dedicated test for <defs/></defs>, <defs/>, <defs
    // TODO test multiple fonts
    fun embedFontsIntoSvg(inputSvgString: String, fonts: Collection<GoogleFontsEntry>): String {
        // try to hit the def tag with children, empty tags and empty tags with attributes, empty tas having attributes in new lines. or even when there is no def tag at all.
        // TODO test no defs tag at all
        val doc = SAXBuilder().build(inputSvgString.byteInputStream())
        val defsTag: Element? = doc.rootElement.getChild("defs", svgNamespace)
        if (defsTag != null) {
            defsTag.addContent(createStyleTagWithFont(fonts))
        } else {
            val newDefsTag = Element("defs", svgNamespace)
            newDefsTag.addContent(createStyleTagWithFont(fonts))
            doc.rootElement.addContent(newDefsTag) // TODO is inserted before?
        }
        return XMLOutputter(Format.getPrettyFormat()).outputString(doc)
    }

    private fun createStyleTagWithFont(fonts: Collection<GoogleFontsEntry>): Element {
        val css = fonts.joinToString {"""
            @font-face {
                font-family: '${it.font}';
                src: url('data:application/x-font-ttf;base64,${Base64.encodeBase64String(it.bytes)}');
            }
        """.trimIndent()
        }
        val styleTag = Element("style", svgNamespace)
        styleTag.setAttribute("type", "text/css")
        styleTag.addContent(CDATA(css))
        return styleTag
    }


}