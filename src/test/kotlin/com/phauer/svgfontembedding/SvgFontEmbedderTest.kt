package com.phauer.svgfontembedding

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import javax.inject.Inject


@QuarkusTest
class SvgFontEmbedderTest {
    @Inject
    lateinit var embedder: SvgFontEmbedder

    private val base = "src/test/resources/svg"

    // TODO test output
    // TODO test against mock server
    // TODO test multiple fonts
    // TODO test no fonts

    // ParameterizedTest are not supported yet: https://github.com/quarkusio/quarkus/pull/9340
    //    @ParameterizedTest
    //    @MethodSource("embeddingDataSource")
    @Test
    fun embedding() {
        for(data in embeddingDataSource()) {
            data.inputFile.asClue {
                embedder.embedFont("--input", "$base/${data.inputFile}") shouldBe data.expectedResult
            }
        }
    }

    @Test
    fun pacifico() {
        embedder.embedFont("--input", "$base/inkscape-pacifico.svg") shouldBe EmbeddingResult.Success(
            detectedFonts = setOf("Pacifico")
        )
    }

    private fun embeddingDataSource() = Stream.of(
        EmbedTestData(
            inputFile = "drawio-embedded-simple-rectangle-with-text.svg",
            expectedResult = EmbeddingResult.Success(
                detectedFonts = setOf("Roboto")
            )
        ),
        EmbedTestData(
            inputFile = "drawio-exported-simple-rectangle-with-text.svg",
            expectedResult = EmbeddingResult.Success(
                detectedFonts = setOf("Roboto")
            )
        ),
        EmbedTestData(
            inputFile = "inkscape-text.svg",
            expectedResult = EmbeddingResult.Success(
                detectedFonts = setOf("Roboto")
            )
        ),
        EmbedTestData(
            inputFile = "inkscape-text-and-shapes.svg",
            expectedResult = EmbeddingResult.Success(
                detectedFonts = setOf("Roboto")
            )
        ),
        EmbedTestData(
            inputFile = "inkscape-text-two-fonts.svg",
            expectedResult = EmbeddingResult.Success(
                detectedFonts = setOf("Roboto", "Gochi Hand")
            )
        )
    )

    @Test
    fun returnFailureOnMissingInputArg() {
        embedder.embedFont() shouldBe EmbeddingResult.Failure(
            message = "Missing required option: input"
        )

    }

    @Test
    fun returnFailureIfInputFileDoesntExist() {
        embedder.embedFont("--input", "foooo.svg") shouldBe EmbeddingResult.Failure(
            message = "File foooo.svg not found."
        )
    }

}

data class EmbedTestData(
    val inputFile: String,
    val expectedResult: EmbeddingResult
)