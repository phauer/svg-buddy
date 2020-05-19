package com.phauer.svgfontembedding.processing

import java.util.regex.Pattern
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class SvgFontDetector{

    // TODO switch to DOM-based detection instead of regex. we have to parse the SVG anyway.

    // test with https://regex101.com/
    fun detectUsedFontsInSvg(inputSvgString: String): Set<String> {
        // most editors use CSS in a style attribute (inkscape, draw.io): style="font-family:'Roboto';"
        val fonts1 = detectusedFontsWithRegex(inputSvgString, Pattern.compile("font-family:(.*?);"))
        // but some use the font-family attribute (yEd): font-family="'Roboto'"
        val fonts2 = detectusedFontsWithRegex(inputSvgString, Pattern.compile("""font-family="(.*?)""""))
            .filter { it != "Dialog" } // yEd places a font-family="'Dialog'" in the <svg> tag even if there are only other Fonts used.
        return fonts1 + fonts2
    }

    private fun detectusedFontsWithRegex(inputSvgString: String, pattern: Pattern): Set<String> {
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
