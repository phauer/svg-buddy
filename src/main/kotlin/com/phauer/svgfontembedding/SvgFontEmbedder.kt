package com.phauer.svgfontembedding

import org.eclipse.microprofile.rest.client.inject.RestClient
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths
import java.util.zip.ZipInputStream
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class SvgFontEmbedder(
    private val cliParser: CliParser,
    private val svgFontDetector: SvgFontDetector,
    private val googleFontsClient: GoogleFontsClient,
    private val embedder: Embedder
) {
    fun embedFont(vararg args: String): EmbeddingResult = try {
        val arguments = cliParser.parseArguments(args)
        val inputSvgString = Files.readString(arguments.inputFile, StandardCharsets.UTF_8)

        println("Detecting Fonts...")
        val detectedFonts = svgFontDetector.detectUsedFontsInSvg(inputSvgString)

        println("Downloading Fonts...")
        val googleFonts = googleFontsClient.downloadFonts(detectedFonts)

        val filteredGoogleFonts = googleFonts.mapValues { entry ->
            entry.value.first { it.fileName.contains("regular", ignoreCase = true) }
        }.values

        println("Embedding Fonts into SVG...")
        val outputSvgString = embedder.embedFontsIntoSvg(inputSvgString, filteredGoogleFonts)

        val newFileName = arguments.inputFile.toString().replaceFirst(".svg", "-embed.svg")
        println("Write new SVG to $newFileName...")
        Files.writeString(Paths.get(newFileName), outputSvgString)

        println("Done.")
        EmbeddingResult.Success(detectedFonts = detectedFonts)
    } catch (ex: Exception) {
        EmbeddingResult.Failure(message = ex.message!!)
    }

    fun printHelp() {
        cliParser.printHelp()
    }
}

sealed class EmbeddingResult {
    data class Success(val detectedFonts: Set<String>) : EmbeddingResult()
    data class Failure(val message: String) : EmbeddingResult()
}