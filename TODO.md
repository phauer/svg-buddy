# TODOs

- poor xml parsing performance in drawio files: culprit: `<!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">`
- dedicated tests for. e.g. for:
    - different fonts
    - multiple fonts
- test and handle different styles of a font. Currently, I'm only supporting 400/regular.
- apply simple svg optimizations (e.g. remove metadata, don't pretty print)
- file watch mode