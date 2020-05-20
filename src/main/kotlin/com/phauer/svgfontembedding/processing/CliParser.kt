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
                .longOpt(Args.optimize)
                .desc("If set, simple optimizations are applied to the output SVG to reduce the file size.")
                .build()
        )
    }

    @Throws(ParseException::class, CliParserException::class)
    fun parseArguments(args: Array<out String>): Arguments {
        val commandLine = DefaultParser().parse(options, args)

        return Arguments(
            inputFile = validateAndGetExistingFile(commandLine.argList.getOrNull(0)),
            outputFile = commandLine.argList.getOrNull(1),
            optimize = commandLine.hasOption(Args.optimize)
        )
    }

    private fun validateAndGetExistingFile(inputFile: String?): Path {
        inputFile ?: throw CliParserException("Missing first argument for the input file.")
        val path = Path.of(inputFile)
        if (!Files.exists(path)) {
            throw CliParserException("File $inputFile not found.")
        }
        return path

    }

    fun printHelp() {
        println("Usage: svg-font-embedding INPUT [OUTPUT] [--optimize]")
        println("If the OUTPUT path is not submitted a new file is created with the postfix '-e' in the same directory as the INPUT file. If --optimize is set the postfix '-eo' is used.")
        HelpFormatter().printHelp("gnu", options)
    }
}

class CliParserException(msg: String) : RuntimeException(msg)

data class Arguments(
    val inputFile: Path,
    val outputFile: String?,
    val optimize: Boolean
)

private object Args {
    const val optimize = "optimize"
}