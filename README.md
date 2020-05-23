# SVG-Font-Embedding

Command line tool to embed fonts into an SVG file. This way, you can ensure an aligned presentation of your SVG independent of the fonts that are installed on the user's system. 

# Features

There are already other good CLI tools like [svg-embed-font](https://github.com/BTBurke/svg-embed-font) or [nano](https://www.npmjs.com/package/nanosvg) for embedding fonts into SVGs. Nevertheless, I created svg-font-embedding because I believe it uniquely combines the following features at the same time: 

- It automatically downloads the required fonts. Only Google Fonts are supported. The fonts are cached locally to avoid downloading them again and again. Moreover, it embeds [WOFF2](https://caniuse.com/#search=woff2) fonts which are compressed and therefore much smaller (up to the factor 10 compared to TTF). 
- The embedding should always work. See [Reliable Embedding](#reliable-embedding) for details.
- It can optimize the SVG to reduce the file size. See [Optimizations](#optimizations) for details.
    - The optimization is compatible with Diagrams.net/Draw.io's SVGs. No text will be cut off with an ellipsis and no warning about the SVG support will be shown. 
- It's free.
- It works offline.

## Reliable Embedding

The font embedding should always work as `svg-font-embedding` relies on a properly parsed DOM instead of string replacement in the SVG. So it doesn't matter how the `<def>` tag exactly looks like (normal, empty, missing, with whitespace between the attributes). This is also ensured with several tests using real-world SVGs from different editors like:
 
 - Inkscape
 - Draw.io/Diagrams.net
 - yEd

## Optimizations

Currently, `svg-font-embedding` only supports simple optimizations.

- Remove the meta data that some editors write in the svg (like Inkscape's `metadata` tag or Draw.io's `content` attribute).
- Remove all tags, attributes and namespace declarations that don't belong to the SVG namespace.
- Remove empty `g` tags. Adobe Illustrator creates those at the end of an SVG.
- Remove comments.
- Remove all whitespaces like spaces, tabs and line breaks.

## Font Source

`svg-font-embedding` downloads the fonts not directly from [Google Fonts](https://fonts.google.com/) but from [google-webfonts-helper](https://google-webfonts-helper.herokuapp.com/) which is a wrapper around Google Fonts providing a better API.

# System Requirements

- Linux users can use the native executable `svg-font-embedding`.
- Windows and Mac users need a Java 11 Runtime (JRE) on their system. Next, they can use the `svg-font-embedding.jar` to execute the tool.

# Usage

```
Usage: svg-font-embedding INPUT [OUTPUT] [--optimize]
If the OUTPUT path is not submitted a new file is created with the postfix '-e' in the same directory as the INPUT file. If --optimize is set the postfix '-eo' is used.
usage: gnu
 -o,--optimize   If set, simple optimizations are applied to the output
                 SVG to reduce the file size.
```

Examples:

```bash
# embed font into file.svg and save it under file-e.svg
svg-font-embedding file.svg # Linux
java -jar svg-font-embedding.jar file.svg # Windows & Mac

# embed font into file.svg, optimize the file size and save it under file-eo.svg
svg-font-embedding file.svg -o # Linux
java -jar svg-font-embedding.jar file.svg -o # Windows & Mac

# embed font into file.svg and save it under output.svg
svg-font-embedding file.svg output.svg # Linux
java -jar svg-font-embedding.jar file.svg output.svg # Windows & Mac
```

# Development

## Running the application in dev mode

```
./mvnw quarkus:dev
```

## Run the Tests

Execute them directly via IntelliJ IDEA or Maven using `./mvnw test`

## Build

Build the native Linux executable:

```bash
# Build
./mvnw package -Pnative -Dquarkus.native.container-build=true

# Execute
./target/svg-font-embedding-runner input.svg
```

Build the fat jar which can be executed via Java on every system (Mac, Windows):

```bash
# Build
./mvnw package -Dquarkus.package.uber-jar=true

# Execute
java -jar target/svg-font-embedding-runner.jar input.svg
```
