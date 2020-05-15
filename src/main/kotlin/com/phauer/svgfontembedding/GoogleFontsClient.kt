package com.phauer.svgfontembedding

import net.lingala.zip4j.ZipFile
import org.apache.commons.io.FileUtils
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import org.eclipse.microprofile.rest.client.inject.RestClient
import java.nio.file.Files
import java.nio.file.Paths
import javax.enterprise.context.ApplicationScoped
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam


@ApplicationScoped
class GoogleFontsClient(
    @RestClient private val service: GoogleFontsService,
    @ConfigProperty(name = "fontDownloadDirectory") private val fontDownloadDirectory: String
) {
    // TODO use WOFF instead of TTF. or even WOFF2? -> https://google-webfonts-helper.herokuapp.com/fonts/pacifico?subsets=latin
    // TODO test
    // TODO only download if the files don't exists
    fun downloadFonts(detectedFonts: Set<String>): Map<String, List<GoogleFontsEntry>> {
        return detectedFonts.associateWith { detectedFont ->
            val fontQueryParam = detectedFonts.joinToString("|")
            val zipBytes = service.downloadZippedFontFamily(fontQueryParam)

            // Neither Java's ZIP APIs nor Zip4J can't read from an in-memory zip stream.
            // So I have to write it to a local directory instead. at least, this enables caching.
            val fontDir = Paths.get(fontDownloadDirectory.replaceFirst("~", System.getProperty("user.home")))
            Files.createDirectories(fontDir)
            val fontZip = Paths.get(fontDir.toString(), "$fontQueryParam.zip")
            Files.write(fontZip, zipBytes)

            val zipFile = ZipFile(fontZip.toFile())
            zipFile.fileHeaders
                .filter { it.fileName.endsWith(".ttf") }
                .map { GoogleFontsEntry(font=detectedFont, fileName= it.fileName, bytes = zipFile.getInputStream(it).readAllBytes()) }
        }
    }
}

@RegisterRestClient
interface GoogleFontsService{
    // https://fonts.google.com/download?family=Roboto
    @GET
    @Path("/download")
    @Produces("application/zip")
    fun downloadZippedFontFamily(@QueryParam("family") fontFamilies: String): ByteArray
}

data class GoogleFontsEntry(
    val font: String,
    val fileName: String,
    val bytes: ByteArray
)