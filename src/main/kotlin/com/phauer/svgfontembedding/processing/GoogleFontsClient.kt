package com.phauer.svgfontembedding.processing

import net.lingala.zip4j.ZipFile
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import org.eclipse.microprofile.rest.client.inject.RestClient
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.enterprise.context.ApplicationScoped
import javax.ws.rs.GET
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.WebApplicationException
import javax.ws.rs.Path as UrlPath


@ApplicationScoped
class GoogleFontsClient(
    @RestClient private val service: GoogleFontsService,
    @ConfigProperty(name = "fontDownloadDirectory") fontDownloadDirectory: String
) {
    private val fontCacheDir: Path = fontDownloadDirectory.let {
        val fontCacheDir = Paths.get(it.replaceFirst("~", System.getProperty("user.home")))
        Files.createDirectories(fontCacheDir)
        fontCacheDir
    }

    fun downloadFonts(detectedFonts: Set<String>): Map<String, List<GoogleFontsEntry>> {
        return detectedFonts.associateWith { detectedFont ->
            val fontZip = getFontZip(detectedFont)
            val zipFile = ZipFile(fontZip.toFile())
            zipFile.fileHeaders
                .filter { it.fileName.endsWith(".woff2") }
                .map {
                    GoogleFontsEntry(
                        font = detectedFont,
                        fileName = it.fileName,
                        bytes = zipFile.getInputStream(it).readAllBytes()
                    )
                }
        }
    }

    private fun getFontZip(detectedFont: String): Path {
        // try to match the urls pattern of the google-webfonts-helper
        val detectedFontInUrlFormat = detectedFont.toLowerCase().replace(" ", "-")
        val fontZip = fontCacheDir.resolve("$detectedFontInUrlFormat.zip")
        if (Files.exists(fontZip)) {
            println("Reusing already downloaded font at $fontZip...")
            return fontZip
        } else {
            println("Downloading font $detectedFont and caching it at $fontZip...")
            val zipBytes = downloadZippedFontFamily(detectedFontInUrlFormat)
            Files.write(fontZip, zipBytes)
            return fontZip
        }
    }

    private fun downloadZippedFontFamily(detectedFont: String) = try {
        service.downloadZippedFontFamily(font = detectedFont)
    } catch (ex: WebApplicationException) {
        val msg = if (ex.response.status == 404) {
            "Font $detectedFont could not be found on Google Fonts. Check out https://google-webfonts-helper.herokuapp.com/fonts to find an available font."
        } else {
            "Failed to download google font $detectedFont. Message: ${ex.message}"
        }
        throw GoogleFontsClientException(msg, ex)
    }
}

class GoogleFontsClientException(message: String, ex: Exception) : RuntimeException(message, ex)

@ApplicationScoped
@RegisterRestClient
interface GoogleFontsService {
    // UI https://google-webfonts-helper.herokuapp.com/fonts/pacifico?subsets=latin
    // Api Docs: https://github.com/majodev/google-webfonts-helper#rest-api
    // example: https://google-webfonts-helper.herokuapp.com/api/fonts/pacifico?download=zip&subsets=latin&variants=regular&formats=woff2
    @GET
    @UrlPath("/api/fonts/{font}")
    @Produces("application/zip")
    fun downloadZippedFontFamily(
        @PathParam("font") font: String,
        @QueryParam("download") download: String = "zip",
        @QueryParam("subsets") subsets: String = "latin",
        @QueryParam("variants") variants: String = "regular",
        @QueryParam("formats") formats: String = "woff2"
    ): ByteArray
}

data class GoogleFontsEntry(
    val font: String,
    val fileName: String,
    val bytes: ByteArray
)