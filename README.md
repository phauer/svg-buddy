# SVG-Font-Embedding

TODO

# Features

- Automatically detect used fonts and download them. Only Google Fonts are supported.
- Embedding always works as we rely on a properly parsed DOM instead of string replacement in the SVG. So it doesn't matter how the `<def>` tag looks like (normal, empty, missing). This is also ensured with several tests.
- We embed WOFF2 instead of TTF. WOFF2 files are compressed and therefore much smaller (up to factor 10). Consequently, the resulting SVG is also smaller. See [Can I Use WOFF2](https://caniuse.com/#search=woff2).
- It's free.
- No installation or certain environment required. Just download the native executable and run it.

# Development

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```
./mvnw quarkus:dev
```

## Packaging and running the application

The application can be packaged using `./mvnw package`.
It produces the `code-with-quarkus-1.0.0-SNAPSHOT-runner.jar` file in the `/target` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/lib` directory.

The application is now runnable using `java -jar target/code-with-quarkus-1.0.0-SNAPSHOT-runner.jar`.

## Creating a native executable

- If you have GraalVM installed: `./mvnw package -Pnative`
- Or via Docker: `./mvnw package -Pnative -Dquarkus.native.container-build=true`

You can then execute your native executable with: `./target/code-with-quarkus-1.0.0-SNAPSHOT-runner`
