package com.phauer.svgfontembedding

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import io.quarkus.test.junit.QuarkusTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Paths
import javax.inject.Inject


@QuarkusTest
class SvgFontFileEmbedderTest {
    @Inject
    lateinit var embedder: SvgFontEmbedder

    private val resourcesFolder = "src/test/resources/svg"
    private val outputFolder = "target/test-classes/output".apply {
        Files.createDirectories(Paths.get(this))
    }

    // TODO test against mock server
    // TODO test multiple fonts
    // TODO test no fonts
    // TODO test systematically fonts: Roboto, Gochi Hand (space), Pacifico
    // TODO test defs tag variations systematically

    // ParameterizedTest are not supported in Quarkus yet: https://github.com/quarkusio/quarkus/pull/9340

    /**
     * good for ad-hoc testing as I'm not having pacifico installed on my system and it's a font that you can easily distinguish when opening the SVG in the browser.
     */
    @Test
    fun pacifico() = processAndAssertOutputFileContent(testCaseName = "inkscape/pacifico", expectedDetectedFonts = setOf("Pacifico"))

    @Test
    fun `text and shapes`() = processAndAssertOutputFileContent(testCaseName = "inkscape/text-shapes", expectedDetectedFonts = setOf("Roboto"))

    @Test
    fun `two fonts`() = processAndAssertOutputFileContent(testCaseName = "inkscape/two-fonts", expectedDetectedFonts = setOf("Roboto", "Gochi Hand"))

    @Test
    fun `drawio rect with text embedded xml`() = processAndAssertOutputFileContent(testCaseName = "drawio/rect-with-text-embedded-xml", expectedDetectedFonts = setOf("Pacifico"))

    @Test
    fun `drawio rect with text exported svg`() = processAndAssertOutputFileContent(testCaseName = "drawio/rect-with-text-exported-svg", expectedDetectedFonts = setOf("Pacifico"))

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

    private fun processAndAssertOutputFileContent(testCaseName: String, expectedDetectedFonts: Set<String>) {
        embedder.embedFont(
            "--input", "$resourcesFolder/$testCaseName/input.svg",
            "--output", "$outputFolder/$testCaseName.svg"
        ).shouldBeTypeOf<EmbeddingResult.Success> { success ->
            success.detectedFonts shouldBe expectedDetectedFonts
            assertEqualContent(actualFile = success.outputFile, expectedFile = "$resourcesFolder/$testCaseName/expected.svg")
        }
    }

    private fun assertEqualContent(actualFile: String, expectedFile: String) {
        val actualSvgString = Files.readString(Paths.get(actualFile))
        val expectedSvgString = Files.readString(Paths.get(expectedFile))
        assertThat(actualSvgString).isEqualToIgnoringWhitespace(expectedSvgString)
    }
}