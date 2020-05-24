package com.phauer.svgbuddy.processing.optimizing

import com.phauer.svgbuddy.processing.util.Attributes
import com.phauer.svgbuddy.processing.util.Namespaces
import com.phauer.svgbuddy.processing.util.Tags
import org.jdom2.CDATA
import org.jdom2.Element

class StyleValueReplacer {

    private val styleValueToCssClass = mutableMapOf<Style, CssName>()
    private var classCounter = 0

    fun replaceStyleValuesWithCssClass(svgTag: Element) {
        removeStyleValuesAndPutItToMapRecursively(svgTag)
        insertStylesAsCssDefinitions(svgTag)
    }

    private fun insertStylesAsCssDefinitions(svgTag: Element) {
        if (styleValueToCssClass.isNotEmpty()) {
            val cssClassDefinitions = styleValueToCssClass
                .map { (style, cssName) -> ".${cssName.name}{${style.cssValue}}" }
                .joinToString(separator = "")
            val styleTag = Element(Tags.style, Namespaces.defaultSvg)
            styleTag.setAttribute("type", "text/css")
            styleTag.addContent(CDATA(cssClassDefinitions))
            svgTag.addContent(0, styleTag)
        }
    }

    private fun removeStyleValuesAndPutItToMapRecursively(parent: Element) {
        parent.children.forEach { tag ->
            val styleValue: String? = tag.getAttributeValue(Attributes.style)
            if (styleValue != null) {
                val style = Style(styleValue)
                styleValueToCssClass.computeIfAbsent(style) { CssName("c${classCounter++}") }
                tag.removeAttribute(Attributes.style)
                setCssClassAttribute(style, tag)
            }
            removeStyleValuesAndPutItToMapRecursively(tag)
        }
    }

    private fun setCssClassAttribute(style: Style, tag: Element) {
        val newClassValue = styleValueToCssClass[style]!!.name
        val oldValue: String? = tag.getAttributeValue(Attributes.class_)
        val newValue = if (oldValue == null) {
            newClassValue
        } else {
            "$oldValue $newClassValue"
        }
        tag.setAttribute(Attributes.class_, newValue)
    }

}

data class CssName (val name: String)
data class Style (val cssValue: String)