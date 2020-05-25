package com.phauer.svgbuddy.processing.optimizing

import com.phauer.svgbuddy.processing.util.Attributes
import com.phauer.svgbuddy.processing.util.Namespaces
import com.phauer.svgbuddy.processing.util.Tags
import org.jdom2.CDATA
import org.jdom2.Element

class StyleValueReplacer {

    private val cssPropToClass = mutableMapOf<PropertyAndValue, CssName>()
    private var nameGenerator = ShortCssClassNameGenerator()

    fun replaceStyleValuesWithCssClass(svgTag: Element) {
        removeStyleValuesAndPutItToMapRecursively(svgTag)
        insertStylesAsCssDefinitions(svgTag)
    }

    private fun insertStylesAsCssDefinitions(svgTag: Element) {
        if (cssPropToClass.isNotEmpty()) {
            val cssClassDefinitions = cssPropToClass
                .map { (style, cssName) -> ".${cssName.name}{${style.line}}" }
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
            styleValue
                ?.split(';')
                ?.filter { it.isNotBlank() }
                ?.map { PropertyAndValue("${it.trim().replaceFirst(": ", ":")};") }
                ?.forEach { propWithValue ->
                    val className = cssPropToClass.computeIfAbsent(propWithValue) { CssName(nameGenerator.getNextClassName()) }
                    tag.removeAttribute(Attributes.style)
                    addToCssClassAttribute(className, tag)
            }
            removeStyleValuesAndPutItToMapRecursively(tag)
        }
    }

    private fun addToCssClassAttribute(newClassValue: CssName, tag: Element) {
        val oldValue: String? = tag.getAttributeValue(Attributes.class_)
        val newValue = if (oldValue == null) newClassValue.name else "$oldValue ${newClassValue.name}"
        tag.setAttribute(Attributes.class_, newValue)
    }
}

data class CssName (val name: String)
data class PropertyAndValue (val line: String)