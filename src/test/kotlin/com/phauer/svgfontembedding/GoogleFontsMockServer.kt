package com.phauer.svgfontembedding

import io.kotest.assertions.fail
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import java.io.FileInputStream

class GoogleFontsMockServer : QuarkusTestResourceLifecycleManager {
    private var server: MockWebServer? = null

    override fun start(): MutableMap<String, String> {
        server = MockWebServer().apply {
            dispatcher = GoogleFontsHelperDispatcher
            start()
        }
        return mutableMapOf("com.phauer.svgfontembedding.processing.GoogleFontsService/mp-rest/url" to server!!.url("/").toString())
    }

    override fun stop() {
        server?.shutdown()
    }
}

object GoogleFontsHelperDispatcher : Dispatcher() {
    private val zipBaseFolder = "src/test/resources/font-zips"
    private val robotoZip by lazy { Buffer().readFrom(FileInputStream("$zipBaseFolder/roboto.zip")) }
    private val pacificoZip by lazy { Buffer().readFrom(FileInputStream("$zipBaseFolder/pacifico.zip")) }
    private val gochiHandZip by lazy { Buffer().readFrom(FileInputStream("$zipBaseFolder/gochi-hand.zip")) }

    override fun dispatch(request: RecordedRequest): MockResponse {
        val path = request.path!!
        return when {
            path.contains("api/fonts/roboto") -> createMockResponse(payload = robotoZip)
            path.contains("api/fonts/pacifico") -> createMockResponse(payload = pacificoZip)
            path.contains("api/fonts/gochi-hand") -> createMockResponse(payload = gochiHandZip)
            else -> fail("Unexpected path have been called: $path")
        }
    }

    private fun createMockResponse(payload: Buffer) = MockResponse()
        .addHeader("Content-Disposition", "attachment; filename=font.zip")
        .addHeader("Content-Type", "application/zip")
        .setBody(payload)
        .setResponseCode(200)
}