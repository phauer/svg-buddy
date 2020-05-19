# TODOs

## Prio 1

- try with some more complicated real world svgs
- README: cli usage

## Prio 2

- Also use the parsed DOM for the Font detection instead of a regex.
- further optimizations:
    - remove comments. e.g. `<!-- Generator: Adobe Illustrator 19.0.0, SVG Export Plug-In . SVG Version: 6.00 Build 0)  -->` before `<svg>`
- Try [XmlBeam](https://xmlbeam.org/) or [jsoup](https://jsoup.org/) instead of jdom2
- dedicated tests for. e.g. for:
    - different fonts
    - multiple fonts
- test and handle different styles of a font. Currently, I'm only supporting 400/regular.
- file watch mode