# SVG-Font-Embedding

Command line tool to embed fonts into an SVG file. This way, you can ensure an aligned presentation of your SVG independent of the fonts that are installed on the user's system. 

# Features

There are already other good CLI tools like [svg-embed-font](https://github.com/BTBurke/svg-embed-font) or [nano](https://www.npmjs.com/package/nanosvg) for embedding fonts into SVGs. Nevertheless, I created svg-font-embedding because it uniquely combines the following features at the same time: 

- It automatically downloads the required fonts. Only Google Fonts are supported. The fonts are cached locally to avoid downloading them again and again. Moreover, it embeds [WOFF2](https://caniuse.com/#search=woff2) fonts which are compressed and therefore much smaller (up to the factor 10 compared to TTF). 
- The embedding should always work. See [Reliable Embedding](#reliable-embedding) for details.
- It can optimize the SVG to reduce the file size. See [Optimizations](#optimizations) for details.
- Compatible with Draw.io SVGs. No text will be cut off with an ellipsis and no warning about the SVG support will be shown. 
- It's free.
- It works offline.
- No installation or certain environment (like npm) required. Just download the native executable and run it.

## Reliable Embedding

The font embedding should always work as `svg-font-embedding` relies on a properly parsed DOM instead of string replacement in the SVG. So it doesn't matter how the `<def>` tag exactly looks like (normal, empty, missing, with whitespace between the attributes). This is also ensured with several tests using real-world SVGs from different editors like:
 
 - Inkscape
 - Draw.io
 - yEd
 
 Moreover, `svg-font-embedding` won't destroy the SVG.

## Optimizations

Currently, `svg-font-embedding` only supports simple optimizations.

- Remove the meta data that some editors write in the svg (like Inkscape's `metadata` tag or Draw.io's `content` attribute).
- Remove all tags, attributes and namespace declarations that don't belong to the SVG namespace.
- Remove empty `g` tags. Adobe Illustrator creates those at the end of an SVG.
- Remove all whitespaces like spaces, tabs and line breaks.

# Development

## Running the application in dev mode

```
./mvnw quarkus:dev
```

## Run the Tests

Execute them directly via IntelliJ IDEA or Maven using `./mvnw test`

## Creating a native executable

- If you have GraalVM installed: `./mvnw package -Pnative`
- If not, you can use Docker: `./mvnw package -Pnative -Dquarkus.native.container-build=true`

You can then execute your native executable with: `./target/svg-font-embedding`
