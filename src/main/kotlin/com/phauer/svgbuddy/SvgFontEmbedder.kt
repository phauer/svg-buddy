package com.phauer.svgbuddy

import com.phauer.svgbuddy.processing.Arguments
import com.phauer.svgbuddy.processing.CliParser
import com.phauer.svgbuddy.processing.CliParserException
import com.phauer.svgbuddy.processing.FileEmbedder
import com.phauer.svgbuddy.processing.GoogleFontsClient
import com.phauer.svgbuddy.processing.GoogleFontsClientException
import com.phauer.svgbuddy.processing.GoogleFontsEntry
import com.phauer.svgbuddy.processing.SvgFontDetector
import com.phauer.svgbuddy.processing.SvgOptimizer
import com.phauer.svgbuddy.processing.util.NonValidatingXmlReaderFactory
import org.apache.commons.cli.ParseException
import org.jboss.logging.Logger
import org.jdom2.Document
import org.jdom2.input.SAXBuilder
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class SvgFontEmbedder(
    private val cliParser: CliParser,
    private val svgFontDetector: SvgFontDetector,
    private val googleFontsClient: GoogleFontsClient,
    private val fileEmbedder: FileEmbedder,
    private val optimizer: SvgOptimizer
) {
    private val log: Logger = Logger.getLogger(this::class.java)

    fun embedFont(vararg args: String): EmbeddingResult = try {
        val arguments = cliParser.parseArguments(args)
        val inputSvgString = Files.readString(arguments.inputFile, StandardCharsets.UTF_8)

        println("Detecting Fonts...")
        val detectedFonts = svgFontDetector.detectUsedFontsInSvg(inputSvgString)

        val doc = SAXBuilder(NonValidatingXmlReaderFactory).build(inputSvgString.byteInputStream())
        embeddFonts(detectedFonts, doc)

        val optimizedSvgString = optimizer.optimizeSvgAndReturnSvgString(arguments, doc)

        val newFileName = getOrCreateOutputFileName(arguments)
        println("Write new SVG to $newFileName...")
        writeSvgToFile(newFileName, optimizedSvgString)

        println("Done.")
        EmbeddingResult.Success(detectedFonts = detectedFonts, outputFile = newFileName)
    } catch (ex: Exception) {
        when (ex) {
            is CliParserException, is ParseException, is GoogleFontsClientException -> EmbeddingResult.Failure(message = ex.message!!)
            else -> {
                log.error("Embedding Failed", ex)
                EmbeddingResult.Failure(message = ex.message!!)
            }
        }
    }

    private fun getOrCreateOutputFileName(arguments: Arguments): String {
        if (arguments.outputFile != null) {
            return arguments.outputFile
        }
        val inputFile = arguments.inputFile.toString()
        if (arguments.optimize) {
            return inputFile.replaceFirst(".svg", "-eo.svg")
        } else {
            return inputFile.replaceFirst(".svg", "-e.svg")
        }
    }

    private fun embeddFonts(detectedFonts: Set<String>, svg: Document) {
        if (detectedFonts.isEmpty()) {
            println("No fonts detected.")
        } else {
            println("Downloading Fonts $detectedFonts...")
            val googleFonts = googleFontsClient.downloadFonts(detectedFonts)
            val filteredGoogleFonts = selectRegularFontFile(googleFonts)
            println("Embedding Google Fonts ${filteredGoogleFonts.map(GoogleFontsEntry::fileName)} into SVG...")
            fileEmbedder.embedFontsIntoSvg(svg, filteredGoogleFonts)
        }
    }

    private fun writeSvgToFile(newFileName: String, outputSvgString: String) {
        val newFilePath = Paths.get(newFileName)
        if (newFilePath.parent != null) {
            Files.createDirectories(newFilePath.parent)
        }
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
    data class Failure(val message: String) : EmbeddingResult()
}