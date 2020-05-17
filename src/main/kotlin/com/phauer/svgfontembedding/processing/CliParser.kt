package com.phauer.svgfontembedding.processing

import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import java.nio.file.Files
import java.nio.file.Path
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class CliParser {
    private val options = Options().apply {
        addOption(
            Option.builder()
                .longOpt(Args.input)
                .desc("The input SVG where the font should be embedded")
                .hasArg()
                .required()
                .build()
        )
        addOption(
            Option.builder()
                .longOpt(Args.output)
                .desc("The file path of the output SVG")
                .hasArg()
                .build()
        )
    }

    @Throws(ParseException::class, CliParserException::class)
    fun parseArguments(args: Array<out String>): Arguments {
        val commandLine = DefaultParser().parse(options, args)

        val inputFile = commandLine.getOptionValue(Args.input)
        return Arguments(
            inputFile = validateAndGetExistingFile(inputFile),
            outputFile = if (commandLine.hasOption(Args.output)) commandLine.getOptionValue(Args.output) else null
        )
    }

    private fun validateAndGetExistingFile(inputFile: String): Path {
        val path = Path.of(inputFile)
        if (!Files.exists(path)) {
            throw CliParserException("File $inputFile not found.")
        }
        return path

    }

    fun printHelp() {
        HelpFormatter().printHelp("gnu", options)
    }
}

class CliParserException(msg: String) : RuntimeException(msg)

data class Arguments(
    val inputFile: Path,
    val outputFile: String?
)

private object Args {
    const val input = "input"
    const val output = "output"
}