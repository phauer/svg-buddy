package com.phauer.svgbuddy.processing.util

import org.jdom2.input.sax.XMLReaderJDOMFactory
import org.jdom2.input.sax.XMLReaders
import org.xml.sax.XMLReader

/**
 * Avoid loading external DTD which takes up to 5 seconds.
 * Draw.io adds the line `<!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">` which causes this unpleasant delay.
 */
object NonValidatingXmlReaderFactory : XMLReaderJDOMFactory {
    override fun isValidating() = false
    override fun createXMLReader(): XMLReader = XMLReaders.NONVALIDATING.createXMLReader().apply {
        setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
    }
}