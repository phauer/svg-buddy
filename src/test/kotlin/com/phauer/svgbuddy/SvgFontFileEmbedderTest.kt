package com.phauer.svgbuddy

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
import java.nio.file.StandardCopyOption
import javax.inject.Inject

@QuarkusTest
@QuarkusTestResource(GoogleFontsMockServer::class) // remove this to run the tests against the live service
class SvgFontFileEmbedderTest {
    @Inject
    lateinit var embedder: SvgFontEmbedder

    @Inject
    @ConfigProperty(name = "svgbuddy.fontDownloadDirectory")
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
            "$resourcesFolder/drawio/$name/input.svg",
            "$outputFolder/$name.svg",
            "--optimize"
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
            failure.message shouldBe "Missing first argument for the input file."
        }
    }

    @Test
    fun `errors - return failure if input file doesnt exist`() {
        embedder.embedFont("foooo.svg").shouldBeTypeOf<EmbeddingResult.Failure> { failure ->
            failure.message shouldBe "File foooo.svg not found."
        }
    }

    @Test
    fun `errors - font doesnt exist at google fonts `() {
        embedder.embedFont("$resourcesFolder/custom/no-google-font/input.svg").shouldBeTypeOf<EmbeddingResult.Failure> { failure ->
            failure.message shouldContain "courier-new could not be found on Google Fonts"
        }
    }

    @Test
    fun `display error if the input file doesnt end with svg`() {
        embedder.embedFont("$resourcesFolder/custom/no-svg-file/text.md").shouldBeTypeOf<EmbeddingResult.Failure> { failure ->
            failure.message shouldContain "is not an SVG file"
        }
    }

    @Test
    fun `fonts get cached locally and not downloaded on the second run`() {
        // little tricky to test because Quarkus doesn't recreate the mock server in the current setup.
        val requestCountBefore = mockServer.requestCount
        repeat(2) {
            embedder.embedFont(
                "$resourcesFolder/inkscape/pacifico/input.svg",
                "$outputFolder/inkscape/pacifico.svg"
            )
        }
        mockServer.requestCount shouldBe requestCountBefore + 1
    }

    @Test
    fun `inkscape - optimize - remove metadata and non-svg-elements`() =
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
    fun `drawio - complex diagram - optimize`() = processAndAssertOutputFileContent(testCaseName = "drawio/complex-diagram-optimize", expectedDetectedFonts = setOf("Roboto"), optimizeSvg = true)

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

    @Test
    fun `no error on relative path - using default output name`() {
        val tempSvg = Paths.get("input.svg")
        Files.copy(Paths.get("$resourcesFolder/custom/no-text/input.svg"), tempSvg, StandardCopyOption.REPLACE_EXISTING)
        embedder.embedFont("input.svg")
        Files.delete(tempSvg)
        Files.delete(Paths.get("input-e.svg"))
    }

    @Test
    fun `no error on relative path - using output parameter`() {
        val tempSvg = Paths.get("input.svg")
        Files.copy(Paths.get("$resourcesFolder/custom/no-text/input.svg"), tempSvg, StandardCopyOption.REPLACE_EXISTING)
        embedder.embedFont("input.svg", "output.svg")
        Files.delete(tempSvg)
        Files.delete(Paths.get("output.svg"))
    }

    /**
     * font-size: Pacifico, Gochi Hand;
     */
    @Test
    fun `two fonts in one definition`() = processAndAssertOutputFileContent(testCaseName = "custom/two-fonts-in-one-def", expectedDetectedFonts = setOf("Pacifico", "Gochi Hand"))

    /**
     * font-family: Roboto, sans-serif;
     */
    @Test
    fun `ignore generic font family`() = processAndAssertOutputFileContent(testCaseName = "custom/ignore-generic-font-family", expectedDetectedFonts = setOf("Roboto"))

    /**
     * For now, let's remove the style after the dash to be able to detect the base font correctly. Later, we can improve this feature to download the font with the correct style.
     * font-family: Roboto-Bold;
     */
    @Test
    fun `ignore style in font`() = processAndAssertOutputFileContent(testCaseName = "custom/font-with-style", expectedDetectedFonts = setOf("Roboto"))

    @Test
    fun `optimize - remove style values and use reusable css classes instead`() = processAndAssertOutputFileContent(testCaseName = "custom/optimize-replace-inline-css-with-central-css", expectedDetectedFonts = setOf(), optimizeSvg = true)

    /**
     * draw.io embedds imported external svg files via a base64 string that is put into an xlink:href attribute in an image tag. prevent those images.
     */
    @Test
    fun `drawio - imported svg files should not be removed`() = processAndAssertOutputFileContent(testCaseName = "drawio/imported-svg", expectedDetectedFonts = setOf(), optimizeSvg = true)


    /**
     * yEd imports an external svg file with a strange attribute `<svg font-family="&quot;Arial&quot;,&quot;Helvetica&quot;,sans-serif">`. ignore this.
     */
    @Test
    fun `yed - ignore invalid font definition`() = processAndAssertOutputFileContent(testCaseName = "yed/ignore-invalid-font-family", expectedDetectedFonts = setOf("Roboto"), optimizeSvg = false)


    /**
     * Corel Draw doesn't add the last trailing comma:
     * .fnt2 {font-family:'Roboto'}
     * Detect the font despite this.
     */
    @Test
    fun `corel draw - detect font even if the trailing comma is missing`() = processAndAssertOutputFileContent(testCaseName = "coreldraw/missing-trailing-comma", expectedDetectedFonts = setOf("Roboto"), optimizeSvg = false)



    private fun processAndAssertOutputFileContent(testCaseName: String, expectedDetectedFonts: Set<String>, optimizeSvg: Boolean = false) {
        val optimizeParams = if (optimizeSvg) arrayOf("--optimize") else arrayOf()
        embedder.embedFont(
            "$resourcesFolder/$testCaseName/input.svg",
            "$outputFolder/$testCaseName.svg",
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