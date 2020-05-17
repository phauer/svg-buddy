# SVG-Font-Embedding

Command line tool to embed fonts into an SVG file. This way, you can ensure an aligned presentation of your SVG independent of the fonts that are installed on the user's system. 

# Features

There are already other CLI tools like [svg-embed-font](https://github.com/BTBurke/svg-embed-font) or [nano](https://www.npmjs.com/package/nanosvg) for embedding fonts into SVGs. Nevertheless, we created svg-font-embedding because it uniquely combines the following features at the same time: 
- It automatically downloads the required fonts. Only Google Fonts are supported. The fonts are cached locally to avoid downloading them again and again.
- It embeds WOFF2 instead of TTF. WOFF2 files are compressed and therefore much smaller (up to factor 10). Consequently, the size of the output SVG is still acceptable. See [Can I Use WOFF2](https://caniuse.com/#search=woff2).
- Embedding always works as we rely on a properly parsed DOM instead of string replacement in the SVG. So it doesn't matter how the `<def>` tag exactly looks like (normal, empty, missing). This is also ensured with several tests using real-world SVGs from different editors like Inkscape or Draw.io.
- It's free.
- It works offline.
- No installation or certain environment (like npm) required. Just download the native executable and run it.

# Development

## Running the application in dev mode

```
./mvnw quarkus:dev
```

## Run the Tests

```
./mvnw test
```

## Creating a native executable

- If you have GraalVM installed: `./mvnw package -Pnative`
- If not, you can use Docker: `./mvnw package -Pnative -Dquarkus.native.container-build=true`

You can then execute your native executable with: `./target/svg-font-embedding`
