package com.phauer.svgfontembedding

import net.lingala.zip4j.ZipFile
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import org.eclipse.microprofile.rest.client.inject.RestClient
import java.nio.file.Files
import java.nio.file.Paths
import javax.enterprise.context.ApplicationScoped
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.WebApplicationException


@ApplicationScoped
class GoogleFontsClient(
    @RestClient private val service: GoogleFontsService,
    @ConfigProperty(name = "fontDownloadDirectory") private val fontDownloadDirectory: String
) {
    // TODO test
    // TODO only download if the files don't exists
    fun downloadFonts(detectedFonts: Set<String>): Map<String, List<GoogleFontsEntry>> {
        return detectedFonts.associateWith { detectedFont ->
            val zipBytes = downloadZippedFontFamily(transformToUrlFormat(detectedFont))

            // Neither Java's ZIP APIs nor Zip4J can't read from an in-memory zip stream.
            // So I have to write it to a local directory instead. at least, this enables caching.
            val fontDir = Paths.get(fontDownloadDirectory.replaceFirst("~", System.getProperty("user.home")))
            Files.createDirectories(fontDir)
            val fontZip = Paths.get(fontDir.toString(), "$detectedFont.zip")
            Files.write(fontZip, zipBytes)

            val zipFile = ZipFile(fontZip.toFile())
            zipFile.fileHeaders
                .filter { it.fileName.endsWith(".woff2") }
                .map { GoogleFontsEntry(font = detectedFont, fileName = it.fileName, bytes = zipFile.getInputStream(it).readAllBytes()) }
        }
    }

    // try to match the urls pattern of the google-webfonts-helper
    private fun transformToUrlFormat(detectedFont: String) = detectedFont.toLowerCase().replace(" ", "-")

    private fun downloadZippedFontFamily(detectedFont: String) = try {
        service.downloadZippedFontFamily(font = detectedFont)
    } catch (ex: WebApplicationException) {
        throw RuntimeException("Failed to download google font $detectedFont.")
    }
}


@RegisterRestClient
interface GoogleFontsService {
    // UI https://google-webfonts-helper.herokuapp.com/fonts/pacifico?subsets=latin
    // Api Docs: https://github.com/majodev/google-webfonts-helper#rest-api
    // example: https://google-webfonts-helper.herokuapp.com/api/fonts/pacifico?download=zip&subsets=latin&variants=regular

    @GET
    @Path("/api/fonts/{font}")
    @Produces("application/zip")
    fun downloadZippedFontFamily(
        @PathParam("font") font: String,
        @QueryParam("download") download: String = "zip",
        @QueryParam("subsets") subsets: String = "latin",
        @QueryParam("variants") variants: String = "regular"
    ): ByteArray
}

data class GoogleFontsEntry(
    val font: String,
    val fileName: String,
    val bytes: ByteArray
)