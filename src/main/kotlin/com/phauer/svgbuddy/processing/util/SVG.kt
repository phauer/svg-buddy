package com.phauer.svgbuddy.processing.util

import org.jdom2.Namespace

object Tags {
    const val defs = "defs"
    const val style = "style"
    const val metadata = "metadata"
}

object Attributes {
    const val style = "style"
    const val class_ = "class"
}

/** mind to match also URI without the tailing slash */
object NamespaceUris {
    const val SVG = "http://www.w3.org/2000/svg"

    /**
     * Draw.io puts the actual text in `<div xmlns="http://www.w3.org/1999/xhtml">`. So we have to prevent them.
     */
    const val XHTML = "http://www.w3.org/1999/xhtml"

    /**
     * Draw.io puts imported (svg) files as base64 in an xlink:href attribute like `<image xlink:href="data:image/svg+xml;base64,PHN2Z..."/>`. So we have to prevent them.
     */
    const val XLINK = "http://www.w3.org/1999/xlink"
}

object Namespaces {
    val defaultSvg = Namespace.getNamespace("", NamespaceUris.SVG)
}