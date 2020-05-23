# TODOs

## Prio 1

## Prio 2

- Support more SVG optimizations.
    - idea: introduce a reusable css class for each different `style` (if the same `style` value is used more than once).
- Also use the parsed DOM for the Font detection instead of a regex. We have to parse the SVG anyway.
- Use parameterized tests when Quarkus supports this with 1.5.0
- print version number. `--version`. ideally, retrieve the version from the git tag during the build?
- Try [XmlBeam](https://xmlbeam.org/) or [jsoup](https://jsoup.org/) instead of jdom2. but as of now, jdom2 work good.
    - of several SVG complexity 
- test and handle different styles of a font. Currently, I'm only supporting 400/regular.
- file watch mode