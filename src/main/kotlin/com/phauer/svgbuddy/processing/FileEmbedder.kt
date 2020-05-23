package com.phauer.svgbuddy.processing

import org.apache.commons.codec.binary.Base64
import org.jdom2.CDATA
import org.jdom2.Document
import org.jdom2.Element
import org.jdom2.Namespace
import javax.enterprise.context.ApplicationScoped


@ApplicationScoped
class FileEmbedder {
    private val svgNamespace = Namespace.getNamespace("", "http://www.w3.org/2000/svg")

    fun embedFontsIntoSvg(svg: Document, fonts: Collection<GoogleFontsEntry>) {
        val defsTag: Element? = svg.rootElement.getChild(Tags.defs, svgNamespace)
        if (defsTag == null) {
            val newDefsTag = Element(Tags.defs, svgNamespace)
            newDefsTag.addContent(createStyleTagWithFont(fonts))
            svg.rootElement.addContent(0, newDefsTag)
        } else {
            defsTag.addContent(createStyleTagWithFont(fonts))
        }
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
        val styleTag = Element(Tags.style, svgNamespace)
        styleTag.setAttribute("type", "text/css")
        styleTag.addContent(CDATA(css))
        return styleTag
    }
}

object Tags {
    const val defs = "defs"
    const val style = "style"
    const val metadata = "metadata"
}
