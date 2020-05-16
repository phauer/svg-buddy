package com.phauer.svgfontembedding

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Test
import java.util.stream.Stream
import javax.inject.Inject


@QuarkusTest
class SvgFontFileEmbedderTest {
    @Inject
    lateinit var embedder: SvgFontEmbedder

    private val base = "src/test/resources/svg"

    // TODO test output
    // TODO test against mock server
    // TODO test multiple fonts
    // TODO test no fonts
    // TODO test systematically fonts: Roboto, Gochi Hand (space), Pacifico

    /**
     * good for ad-hoc testing as I'm not having pacifico installed on my system and it's a font that you can easily distinguish when opening the SVG in the browser.
     */
    @Test
    fun pacifico() {
        embedder.embedFont("--input", "$base/inkscape-pacifico.svg") shouldBe EmbeddingResult.Success(
            detectedFonts = setOf("Pacifico")
        )
    }

    // ParameterizedTest are not supported yet: https://github.com/quarkusio/quarkus/pull/9340
    //    @ParameterizedTest
    //    @MethodSource("embeddingDataSource")
    @Test
    fun embedding() {
        for (data in embeddingDataSource()) {
            data.inputFile.asClue {
                embedder.embedFont("--input", "$base/${data.inputFile}") shouldBe data.expectedResult
            }
        }
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
        embedder.embedFont().shouldBeTypeOf<EmbeddingResult.Failure> { failure ->
            failure.message shouldBe "Missing required option: input"
        }
    }

    @Test
    fun returnFailureIfInputFileDoesntExist() {
        embedder.embedFont("--input", "foooo.svg").shouldBeTypeOf<EmbeddingResult.Failure> { failure ->
            failure.message shouldBe "File foooo.svg not found."
        }
    }

}

data class EmbedTestData(
    val inputFile: String,
    val expectedResult: EmbeddingResult
)