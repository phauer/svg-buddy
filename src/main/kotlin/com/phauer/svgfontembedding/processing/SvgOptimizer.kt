package com.phauer.svgfontembedding.processing

import org.jdom2.Document
import org.jdom2.Element
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
        removeNonSvgChildrenAndAttributesRecursively(svgTag)
        XMLOutputter(Format.getCompactFormat()).outputString(doc)
    } else {
        XMLOutputter(Format.getPrettyFormat()).outputString(doc)
    }

    private fun removeNonSvgChildrenAndAttributesRecursively(tag: Element) {
        removeNonSvgAttributes(tag)
        removeNonSvgChildren(tag)
        tag.children.forEach { child -> removeNonSvgChildrenAndAttributesRecursively(child) }
    }

    private fun removeMetaData(svgTag: Element) {
        svgTag.children
            .filter { tag -> tag.name == Tags.metadata }
            .forEach { tag -> svgTag.removeChild(tag.name, tag.namespace) }
    }

    private fun removeNonSvgChildren(svgTag: Element) {
        svgTag.children
            .filter { tag -> tag.namespaceURI != SVG_NS_URI }
            .forEach { tag -> svgTag.removeChild(tag.name, tag.namespace) }
    }

    private fun removeNonSvgNSDeclarations(svgTag: Element) {
        svgTag.additionalNamespaces
            .filter { ns -> ns.uri != SVG_NS_URI }
            .forEach { ns -> svgTag.removeNamespaceDeclaration(ns) }
    }

    private fun removeNonSvgAttributes(svgTag: Element) {
        svgTag.attributes
            .filter { attr -> attr.namespaceURI != SVG_NS_URI && attr.namespaceURI.isNotBlank() }
            .forEach { attr -> svgTag.removeAttribute(attr) }
    }


    /**
     * Draw.io embeds a copy of its diagram (in its mxfile format) in the "content" attribute of the root svg tag. It' not required for displaying.
     */
    private fun removeContentAttribute(svgTag: Element) {
        svgTag.removeAttribute("content")
    }

}