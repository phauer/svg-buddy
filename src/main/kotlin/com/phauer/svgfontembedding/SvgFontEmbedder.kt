package com.phauer.svgfontembedding

import org.jboss.logging.Logger
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class SvgFontEmbedder(
    private val cliParser: CliParser,
    private val svgFontDetector: SvgFontDetector,
    private val googleFontsClient: GoogleFontsClient,
    private val fileEmbedder: FileEmbedder
) {
    private val log: Logger = Logger.getLogger(this::class.java)

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
        val outputSvgString = fileEmbedder.embedFontsIntoSvg(inputSvgString, filteredGoogleFonts)

        val newFileName = arguments.inputFile.toString().replaceFirst(".svg", "-embed.svg")
        println("Write new SVG to $newFileName...")
        Files.writeString(Paths.get(newFileName), outputSvgString)

        println("Done.")
        EmbeddingResult.Success(detectedFonts = detectedFonts)
    } catch (ex: Exception) {
        log.error("Embedding Failed", ex)
        EmbeddingResult.Failure(message = ex.message!!, exception = ex)
    }

    fun printHelp() {
        cliParser.printHelp()
    }
}

sealed class EmbeddingResult {
    data class Success(val detectedFonts: Set<String>) : EmbeddingResult()
    data class Failure(val message: String, val exception: Exception) : EmbeddingResult()
}