package com.phauer

import io.quarkus.runtime.QuarkusApplication
import io.quarkus.runtime.annotations.QuarkusMain

@QuarkusMain
class SvgFontEmbedding : QuarkusApplication {
    override fun run(vararg args: String): Int {
        println("Hello World2")
        return 10
    }
}