# TODOs

## Prio 1

## Prio 2

- Support more SVG optimizations.
    - e.g. remove empty `<defs>` tags or tags with only one attribute like a single `id`.
    - some inspirations can be found in the [svgo docs](https://github.com/svg/svgo)
- Also use the parsed DOM for the font detection instead of a regex. We have to parse the SVG anyway.
- Use parameterized tests when Quarkus supports this with 1.5.0
- Try [XmlBeam](https://xmlbeam.org/) or [jsoup](https://jsoup.org/) instead of jdom2. but as of now, jdom2 work good.
- test and handle different styles of a font. Currently, I'm only supporting 400/regular.
- file watch mode