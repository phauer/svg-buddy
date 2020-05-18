package com.phauer.svgfontembedding.processing

import org.jdom2.Document
import org.jdom2.Element
import org.jdom2.Text
import org.jdom2.output.Format
import org.jdom2.output.XMLOutputter
import javax.enterprise.context.ApplicationScoped

/** mind to match also URI without the tailing slash */
const val SVG_NS_URI = "http://www.w3.org/2000/svg"

@ApplicationScoped
class SvgOptimizer {

    fun optimizeSvgAndReturnSvgString(arguments: Arguments, doc: Document): String = if (arguments.optimize) {
        println("Optimize SVG...")
        val svgTag = doc.rootElement
        removeNonSvgNSDeclarations(svgTag)
        removeMetaData(svgTag)
        removeContentAttribute(svgTag)
        cleanTagsRecursively(svgTag)
        XMLOutputter(Format.getCompactFormat()).outputString(doc)
    } else {
        XMLOutputter(Format.getPrettyFormat()).outputString(doc)
    }

    private fun cleanTagsRecursively(parent: Element) {
        removeNonSvgAttributes(parent)
        removeNonSvgChildren(parent)
        removeEmptyGTagChildren(parent)
        parent.children.forEach { child -> cleanTagsRecursively(child) }
    }

    private fun removeMetaData(svgTag: Element) {
        svgTag.children
            .filter { tag -> tag.name == Tags.metadata }
            .forEach { tag -> svgTag.removeContent(tag) }
    }

    private fun removeNonSvgChildren(parentTag: Element) {
        parentTag.children
            .filter { tag -> tag.namespaceURI != SVG_NS_URI }
            .forEach { tag -> parentTag.removeContent(tag) }
    }

    private fun removeEmptyGTagChildren(parentTag: Element) {
        parentTag.children
            .filter { tag -> tag.name == "g" && !tag.containsElements() }
            .forEach { tag -> parentTag.removeContent(tag) }
    }

    private fun Element.containsElements(): Boolean {
        return when(contentSize) {
            0 -> false
            1 -> {
                val children = content.first()
                children !is Text || !children.text.isBlank()
            }
            else -> true
        }
    }

    private fun removeNonSvgNSDeclarations(svgTag: Element) {
        svgTag.additionalNamespaces
            .filter { ns -> ns.uri != SVG_NS_URI }
            .forEach { ns -> svgTag.removeNamespaceDeclaration(ns) }
    }

    private fun removeNonSvgAttributes(parentTag: Element) {
        parentTag.attributes
            .filter { attr -> attr.namespaceURI != SVG_NS_URI && attr.namespaceURI.isNotBlank() }
            .forEach { attr -> parentTag.removeAttribute(attr) }
    }


    /**
     * Draw.io embeds a copy of its diagram (in its mxfile format) in the "content" attribute of the root svg tag. It' not required for displaying.
     */
    private fun removeContentAttribute(svgTag: Element) {
        svgTag.removeAttribute("content")
    }

}