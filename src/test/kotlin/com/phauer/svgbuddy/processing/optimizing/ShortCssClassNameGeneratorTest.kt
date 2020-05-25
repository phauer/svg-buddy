package com.phauer.svgbuddy.processing.optimizing

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ShortCssClassNameGeneratorTest {

    @Test
    fun subsequentNamesAreGenerated() {
        val gen = ShortCssClassNameGenerator()
        gen.getNextClassName() shouldBe "a"
        gen.getNextClassName() shouldBe "b"
        gen.getNextClassName() shouldBe "c"
        gen.getNextClassName() shouldBe "d"
        gen.getNextClassName() shouldBe "e"
        gen.getNextClassName() shouldBe "f"
        gen.getNextClassName() shouldBe "g"
        gen.getNextClassName() shouldBe "h"
        gen.getNextClassName() shouldBe "i"
        gen.getNextClassName() shouldBe "j"
        gen.getNextClassName() shouldBe "k"
        gen.getNextClassName() shouldBe "l"
        gen.getNextClassName() shouldBe "m"
        gen.getNextClassName() shouldBe "n"
        gen.getNextClassName() shouldBe "o"
        gen.getNextClassName() shouldBe "p"
        gen.getNextClassName() shouldBe "q"
        gen.getNextClassName() shouldBe "r"
        gen.getNextClassName() shouldBe "s"
        gen.getNextClassName() shouldBe "t"
        gen.getNextClassName() shouldBe "u"
        gen.getNextClassName() shouldBe "v"
        gen.getNextClassName() shouldBe "w"
        gen.getNextClassName() shouldBe "x"
        gen.getNextClassName() shouldBe "y"
        gen.getNextClassName() shouldBe "z"

        gen.getNextClassName() shouldBe "a0"
        gen.getNextClassName() shouldBe "b0"
        gen.getNextClassName() shouldBe "c0"
        gen.getNextClassName() shouldBe "d0"
        gen.getNextClassName() shouldBe "e0"
        gen.getNextClassName() shouldBe "f0"
        gen.getNextClassName() shouldBe "g0"
        gen.getNextClassName() shouldBe "h0"
        gen.getNextClassName() shouldBe "i0"
        gen.getNextClassName() shouldBe "j0"
        gen.getNextClassName() shouldBe "k0"
        gen.getNextClassName() shouldBe "l0"
        gen.getNextClassName() shouldBe "m0"
        gen.getNextClassName() shouldBe "n0"
        gen.getNextClassName() shouldBe "o0"
        gen.getNextClassName() shouldBe "p0"
        gen.getNextClassName() shouldBe "q0"
        gen.getNextClassName() shouldBe "r0"
        gen.getNextClassName() shouldBe "s0"
        gen.getNextClassName() shouldBe "t0"
        gen.getNextClassName() shouldBe "u0"
        gen.getNextClassName() shouldBe "v0"
        gen.getNextClassName() shouldBe "w0"
        gen.getNextClassName() shouldBe "x0"
        gen.getNextClassName() shouldBe "y0"
        gen.getNextClassName() shouldBe "z0"

        gen.getNextClassName() shouldBe "a1"
        gen.getNextClassName() shouldBe "b1"
    }
}