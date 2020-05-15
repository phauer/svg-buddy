package com.phauer.svgfontembedding

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.regex.Pattern
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class SvgFontEmbedder(
    private val googleFontsClient: GoogleFontsClient,
    private val cliParser: CliParser
) {
    fun embedFont(vararg args: String): EmbeddingResult = try {
        val arguments = cliParser.parseArguments(args)
        val detectedFonts = detectUsedFontsInSvg(arguments.inputFile)
        // TODO get used fonts in svg
        // TODO retrieve fonts from google fonts
        googleFontsClient.getFonts(listOf("Roboto"))
        // TODO embed font into svg
        // TODO write new svg
        EmbeddingResult.Success(detectedFonts = detectedFonts)
    } catch (ex: Exception) {
        EmbeddingResult.Failure(message = ex.message!!)
    }

    // test with https://regex101.com/
    private fun detectUsedFontsInSvg(inputFile: Path): List<String> {
        val svg = Files.readString(inputFile, StandardCharsets.UTF_8)
        val pattern = Pattern.compile("font-family:(.*?);")
        val matcher = pattern.matcher(svg)
        val fonts = mutableListOf<String>()
        while (matcher.find()) {
            fonts.add(matcher.group(1).trim())
        }
        return fonts.toList()
    }

    fun printHelp() {
        cliParser.printHelp()
    }
}

sealed class EmbeddingResult {
    data class Success(val detectedFonts: List<String>) : EmbeddingResult()
    data class Failure(val message: String) : EmbeddingResult()
}