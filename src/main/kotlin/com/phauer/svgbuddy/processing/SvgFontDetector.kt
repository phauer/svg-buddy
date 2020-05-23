package com.phauer.svgbuddy.processing

import java.util.regex.Pattern
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class SvgFontDetector{

    // TODO switch to DOM-based detection instead of regex. we have to parse the SVG anyway.

    private val genericFontFamilies = setOf("serif", "sans-serif", "cursive", "fantasy", "monospace")

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
            fonts.addAll(extractOneOrMultipleFonts(matcher.group(1)))
        }
        return fonts.toSet()
    }

    /**
     * font-size: Roboto;
     * font-size: Roboto, sans-serif;
     * font-size: Roboto Mono, Pacifico;
     */
    private fun extractOneOrMultipleFonts(fontFamilyValue: String): List<String> =
        fontFamilyValue
            .split(',')
            .map { it.trim(this::isSpaceOrQuotes) }
            .filter { !genericFontFamilies.contains(it) }
            .map { removeStyleAfterDash(it) } // first filter generic families like "sans-serif". then remove the styles by the dash "-".

    /**
     * let's ignore the style part in the font definition for now and download the regular one.
     * e.g. OpenSans-Regular, OpenSans-SemiBold, Roboto-Light
     * styles: "light", "lightitalic", "regular", "italic", "semibold", "semibolditalic", "bold", "bolditalic", "extrabold", "extrabolditalic"
     */
    private fun removeStyleAfterDash(fontFamilyValue: String) = fontFamilyValue.substringBefore('-')

    private fun isSpaceOrQuotes(it: Char) = when (it) {
        ' ', '\'', '"' -> true
        else -> false
    }
}
