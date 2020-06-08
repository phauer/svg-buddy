# TODOs

## Prio 1

## Prio 2

- Use parameterized tests when Quarkus supports this with 1.5.0
- Support more SVG optimizations. ideas:
    - reduce the size of the embedded font by manipulating the font data to only include the letters that are really used in the svg. 
        - this would bring by far the biggest size reduction but it's tricky. 
        - the code of [FontVerter](https://github.com/m-abboud/FontVerter) can be useful to learn how to parse and edit a woff2 file in java. 
        - The idea behind can be demonstrated with the tool FontForge that can remove single letters from a woff2 file by right-click on it. 
        - I believe that all other optimizations of the SVG itself don't have a big impact anymore - especially when the SVG is gzipped.
    - remove unused ids (often only internally used by editors).
    - remove empty `<defs>` tags or tags with only one attribute like a single `id`.
    - remove useless groups `g`. move content one level to the top.
    - more ideas can be found here: [svgo](https://github.com/svg/svgo), [nano blog post 1](https://vecta.io/blog/how-nano-compresses-svg/), [nano blog post 2](https://vecta.io/blog/tips-for-smaller-svg-sizes). 
- Also use the parsed DOM for the font detection instead of a regex. We have to parse the SVG anyway.
- Try [XmlBeam](https://xmlbeam.org/) or [jsoup](https://jsoup.org/) instead of jdom2. but as of now, jdom2 work good.
- test and handle different styles of a font. Currently, I'm ignoring the style and use the regular style.
- file watch mode