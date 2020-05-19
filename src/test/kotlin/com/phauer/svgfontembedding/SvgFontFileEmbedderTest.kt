package com.phauer.svgfontembedding

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeTypeOf
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import okhttp3.mockwebserver.MockWebServer
import org.apache.commons.io.FileUtils
import org.assertj.core.api.Assertions.assertThat
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import javax.inject.Inject

@QuarkusTest
@QuarkusTestResource(GoogleFontsMockServer::class)
class SvgFontFileEmbedderTest {
    @Inject
    lateinit var embedder: SvgFontEmbedder

    @Inject
    @ConfigProperty(name = "fontDownloadDirectory")
    lateinit var fontDownloadDirectory: String
    lateinit var mockServer: MockWebServer

    private val resourcesFolder = "src/test/resources/svg"
    private val outputFolder = "target/test-classes/output".apply {
        Files.createDirectories(Paths.get(this))
    }

    // ParameterizedTest are not supported in Quarkus yet: https://github.com/quarkusio/quarkus/pull/9340
    /**
     * the font pacifico good for testing as I'm not having pacifico installed on my system and it's a font that you can easily distinguish when opening the SVG in the browser.
     */

    @BeforeEach
    fun init() {
        val fontDownloadDir = File(fontDownloadDirectory)
        FileUtils.deleteDirectory(fontDownloadDir)
        FileUtils.forceMkdir(fontDownloadDir)
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "adhoc", matches = "true")
    fun `ad-hoc test to execute the embedding for a specific file`() {
        val name = "complex-diagram"
        embedder.embedFont(
            "--input", "$resourcesFolder/drawio/$name/input.svg",
            "--output", "$outputFolder/$name.svg",
            "--optimize", "true"
        )
    }

    @Test
    fun `no text at all - don't change anything`() = processAndAssertOutputFileContent(testCaseName = "custom/no-text", expectedDetectedFonts = setOf())

    @Test
    fun `defs tag - empty tag`() = processAndAssertOutputFileContent(testCaseName = "custom/defs-tag-empty", expectedDetectedFonts = setOf("Pacifico"))

    @Test
    fun `defs tag - no tag at all`() = processAndAssertOutputFileContent(testCaseName = "custom/defs-tag-none", expectedDetectedFonts = setOf("Pacifico"))

    @Test
    fun `inkscape - pacifico`() = processAndAssertOutputFileContent(testCaseName = "inkscape/pacifico", expectedDetectedFonts = setOf("Pacifico"))

    @Test
    fun `inkscape - text and shapes`() = processAndAssertOutputFileContent(testCaseName = "inkscape/text-shapes", expectedDetectedFonts = setOf("Roboto"))

    @Test
    fun `inkscape - two fonts`() = processAndAssertOutputFileContent(testCaseName = "inkscape/two-fonts", expectedDetectedFonts = setOf("Roboto", "Gochi Hand"))

    @Test
    fun `drawio - rect with text embedded xml`() = processAndAssertOutputFileContent(testCaseName = "drawio/rect-with-text-embedded-xml", expectedDetectedFonts = setOf("Pacifico"))

    @Test
    fun `drawio - rect with text exported svg`() = processAndAssertOutputFileContent(testCaseName = "drawio/rect-with-text-exported-svg", expectedDetectedFonts = setOf("Pacifico"))

    @Test
    fun `errors - return failure on missing input arg`() {
        embedder.embedFont().shouldBeTypeOf<EmbeddingResult.Failure> { failure ->
            failure.message shouldBe "Missing required option: input"
        }
    }

    @Test
    fun `errors - return failure if input file doesnt exist`() {
        embedder.embedFont("--input", "foooo.svg").shouldBeTypeOf<EmbeddingResult.Failure> { failure ->
            failure.message shouldBe "File foooo.svg not found."
        }
    }

    @Test
    fun `errors - font doesnt exist at google fonts `() {
        embedder.embedFont("--input", "$resourcesFolder/custom/no-google-font/input.svg").shouldBeTypeOf<EmbeddingResult.Failure> { failure ->
            failure.message shouldContain "courier-new could not be found on Google Fonts"
        }
    }


    @Test
    fun `fonts get cached locally and not downloaded on the second run`() {
        // little tricky because Quarkus doesn't recreate the mock server in the current setup.
        val requestCountBefore = mockServer.requestCount
        repeat(2) {
            embedder.embedFont(
                "--input", "$resourcesFolder/inkscape/pacifico/input.svg",
                "--output", "$outputFolder/inkscape/pacifico.svg"
            )
        }
        mockServer.requestCount shouldBe requestCountBefore + 1
    }

    @Test
    fun `inkscape - optimize - remove metadata and non-svg-elemetns`() =
        processAndAssertOutputFileContent(testCaseName = "inkscape/optimize-no-text", expectedDetectedFonts = setOf(), optimizeSvg = true)

    @Test
    fun `drawio - optimize - remove content attribute`() = processAndAssertOutputFileContent(testCaseName = "drawio/optimize-no-text", expectedDetectedFonts = setOf(), optimizeSvg = true)

    /**
     * remove empty g tags
     * remove comments
     */
    @Test
    fun `illustrator - optimize - remove empty g tags`() = processAndAssertOutputFileContent(testCaseName = "illustrator/optimize-empty-g-tags", expectedDetectedFonts = setOf(), optimizeSvg = true)

    /**
     * don't remove html tags as they contain the text in draw.io
     * don't remove empty g tag if there are attributes. draw.io uses it to hide the "SVG not supported" warning.
     */
    @Test
    fun `drawio - complex diagram`() = processAndAssertOutputFileContent(testCaseName = "drawio/complex-diagram", expectedDetectedFonts = setOf("Roboto"), optimizeSvg = false)

    @Test
    fun `drawio - complex diagram - optimize`() = processAndAssertOutputFileContent(testCaseName = "drawio/optimize-complex-diagram", expectedDetectedFonts = setOf("Roboto"), optimizeSvg = true)

    @Test
    fun `inkscape - complex diagram`() = processAndAssertOutputFileContent(testCaseName = "inkscape/complex-diagram", expectedDetectedFonts = setOf("Roboto Mono", "Roboto"), optimizeSvg = false)

    @Test
    fun `inkscape - complex diagram - optimize`() = processAndAssertOutputFileContent(testCaseName = "inkscape/complex-diagram-optimize", expectedDetectedFonts = setOf("Roboto Mono", "Roboto"), optimizeSvg = true)

    /** yEd places the font declaration in a dedicated attribute instead of the style attr: <bla font-family="'Roboto'"> */
    @Test
    fun `yed - shape and text`() = processAndAssertOutputFileContent(testCaseName = "yed/shapes-texts", expectedDetectedFonts = setOf("Roboto"))

    /**
     * remove comments
     */
    @Test
    fun `yed - shape and text - optimize`() = processAndAssertOutputFileContent(testCaseName = "yed/shapes-texts-optimize", expectedDetectedFonts = setOf("Roboto"),optimizeSvg = true)

    private fun processAndAssertOutputFileContent(testCaseName: String, expectedDetectedFonts: Set<String>, optimizeSvg: Boolean = false) {
        val optimizeParams = if (optimizeSvg) arrayOf("--optimize", "true") else arrayOf()
        embedder.embedFont(
            "--input", "$resourcesFolder/$testCaseName/input.svg",
            "--output", "$outputFolder/$testCaseName.svg",
            *optimizeParams
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