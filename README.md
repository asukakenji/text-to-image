# Text To Image

## Compilation

    javac TextToImage.java

## Usage

    java TextToImage [options...] <text>

### Options

`-t, --text TEXT`: Specifies the text to be converted.
This has the same effect as specifying the text at the end of the command.

`-n, --name NAME`: Specifies the name of the font.
The default is `System Regular`.
If this is enabled, `-f`, `-w`, `-p` will be disabled.

`-f, --family FAMILY`: Specifies the font family.
The default is `System`.
If this is enabled, `-n` will be disabled.

`-w, --weight WEIGHT`: Specifies the font weight.

Available `WEIGHT`s are:

- `THIN` (100)
- `EXTRA_LIGHT` (200)
- `LIGHT` (300)
- `NORMAL` (400)
- `MEDIUM` (500)
- `SEMI_BOLD` (600)
- `BOLD` (700)
- `EXTRA_BOLD` (800)
- `BLACK` (900)

The default is `NORMAL`.
If `-n` is enabled, this will be ignored.

`--bold`: A shorthand for `-w BOLD`.

`-p, --posture POSTURE`: Specifies the font posture.

Available `POSTURE`s are:

- `REGULAR`
- `ITALIC`

The default is `REGULAR`.
If `-n` is enabled, this will be ignored.

`--italic`: A shorthand for `-p ITALIC`.

`-s, --size SIZE`: Specifies the font size.

`--fgColor COLOR`: Specifies the foreground (text) color.
See below for available options.

`--bgColor COLOR`: Specifies the background color.
See below for available options.

`--underline`: Specifies whether the text should be underlined.

`-o, --output OUTPUT`: Specifies the output file.

`--show`: Specifies whether the image should be shown on screen,
in addition to being generated.

`--families`: Print a list of available font families and quit.

`--names`: Print a list of available font names and quit.

`--names-in FAMILY`: Print a list of available font names
that belongs to the specified font family and quit.

`--help`: Print help text.

### Colors

Colors could be specified by:

- Common name like `BLACK`, and `WHITE`
- Hexadecimal values like `0x336699`, `0x369`, `336699`, and `369`
- Web RGB values like `#336699`, `#369`, and `rgb(51,102,153)`
- Web RGBA values like `rgba(51,102,153,1.0)`
- Web HSL values like `hsl(270,100%,100%)`
- Web HSLA values like `hsla(210,50%,40%,1.0)`

For a full description, please visit:

https://docs.oracle.com/javase/9/docs/api/javafx/scene/paint/Color.html

## References

- http://java-buddy.blogspot.hk/2013/04/save-canvas-to-png-file.html
- https://stackoverflow.com/a/18483029/142239

## TODOs / Known Bugs

- Fix BMP / WBMP cannot be used
- Fix JPEG incorrect color problem
- Add transparency support
- Check BufferedImage / RenderedImage issue
