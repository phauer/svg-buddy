package com.phauer.svgfontembedding

import io.kotest.matchers.shouldBe
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Test
import javax.inject.Inject


@QuarkusTest
class SvgFontEmbedderTest {
    @Inject
    lateinit var embedder: SvgFontEmbedder

    // TODO test multiple fonts
    // TODO test no fonts

    @Test
    fun test() {
        // TODO test detection in all test svgs. -> parameterized test
        embedder.embedFont("--input", "src/test/resources/svg/drawio-embedded-simple-rectangle-with-text.svg") shouldBe EmbeddingResult.Success(
            detectedFonts = listOf("Roboto")
        )
    }

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