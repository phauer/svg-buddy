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

        println("Downloading Fonts $detectedFonts...")
        val googleFonts = googleFontsClient.downloadFonts(detectedFonts)

        val filteredGoogleFonts = selectRegularFontFile(googleFonts)

        println("Embedding Google Fonts ${filteredGoogleFonts.map(GoogleFontsEntry::fileName)} into SVG...")
        val outputSvgString = fileEmbedder.embedFontsIntoSvg(inputSvgString, filteredGoogleFonts)

        val newFileName = arguments.outputFile ?: arguments.inputFile.toString().replaceFirst(".svg", "-e.svg")
        println("Write new SVG to $newFileName...")
        writeSvgToFile(newFileName, outputSvgString)

        println("Done.")
        EmbeddingResult.Success(detectedFonts = detectedFonts, outputFile = newFileName)
    } catch (ex: Exception) {
        log.error("Embedding Failed", ex)
        EmbeddingResult.Failure(message = ex.message!!, exception = ex)
    }

    private fun writeSvgToFile(newFileName: String, outputSvgString: String) {
        val newFilePath = Paths.get(newFileName)
        Files.createDirectories(newFilePath.parent)
        Files.writeString(newFilePath, outputSvgString)
    }

    private fun selectRegularFontFile(googleFonts: Map<String, List<GoogleFontsEntry>>): Collection<GoogleFontsEntry> {
        return googleFonts.mapValues { entry ->
            entry.value.first { it.fileName.contains("regular", ignoreCase = true) }
        }.values
    }

    fun printHelp() {
        cliParser.printHelp()
    }
}

sealed class EmbeddingResult {
    data class Success(val detectedFonts: Set<String>, val outputFile: String) : EmbeddingResult()
    data class Failure(val message: String, val exception: Exception) : EmbeddingResult()
}