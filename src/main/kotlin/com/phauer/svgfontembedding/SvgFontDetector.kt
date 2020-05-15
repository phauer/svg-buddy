package com.phauer.svgfontembedding

import java.util.regex.Pattern
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class SvgFontDetector{

    // test with https://regex101.com/
    fun detectUsedFontsInSvg(inputSvgString: String): Set<String> {
        val pattern = Pattern.compile("font-family:(.*?);")
        val matcher = pattern.matcher(inputSvgString)
        val fonts = mutableListOf<String>()
        while (matcher.find()) {
            fonts.add(matcher.group(1).trim { isSpaceOrQuotes(it) })
        }
        return fonts.toSet()
    }

    private fun isSpaceOrQuotes(it: Char) = when (it) {
        ' ', '\'', '"' -> true
        else -> false
    }
}
