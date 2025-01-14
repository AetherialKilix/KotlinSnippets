/*
 * This file was written by AetherialKilix (https://github.com/AetherialKilix) on 2025-01-14 (Jan 14 2025).
 *
 * This file is provided "as is" and without any guarantees of quality, functionality, or fitness for a particular purpose.
 * Use at your own risk; I make no claims that this code will work as intended or at all.
 *
 * You are free to use, modify, and distribute this code in any project, personal or professional,
 * without attribution or compensation. No credit to me is required.
 *
 * This file references information from the "ANSI escape code" article on Wikipedia, accessed on the 2025-01-14 (Jan 14 2025):
 * https://en.wikipedia.org/wiki/ANSI_escape_code
 * Content is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License (CC BY-SA 3.0).
 */
import java.awt.Color

/**
 * Provides functions to build ANSI sequences.
 * it is entirely based on [Wikipedia](https://en.wikipedia.org/wiki/ANSI_escape_code)
 */
class AnsiSequence private constructor(
    private val builder: StringBuilder
) : CharSequence by builder {
    constructor() : this(StringBuilder())

    companion object {
        /** the escape character */
        const val ESC = "\u001B"
        /** the command sequence introducer */
        const val CSI = "$ESC["
    }

    /** creates a copy of the current sequence with a disconnected backing [StringBuilder] */
    fun copy(): AnsiSequence = AnsiSequence(StringBuilder(builder.toString()))

    /** appends the supplied text to the sequence as is */
    fun append(string: CharSequence) { builder.append(string) }
    /** appends the supplied text to the sequence, replacing [ESC] with '\u241b' */
    fun appendEscaped(string: CharSequence) { builder.append(string.toString().replace(ESC, "\u241b")) }

    // cursor movement \\
    /** Moves the cursor n (default 1) cells upward. If the cursor is already at the edge of the screen, this has no effect. */
    fun cursorUp(n: Int = 1) { builder.append(CSI).append(n).append('A') }
    /** Moves the cursor n (default 1) cells downward. If the cursor is already at the edge of the screen, this has no effect. */
    fun cursorDown(n: Int = 1) { builder.append(CSI).append(n).append('B') }
    /** Moves the cursor n (default 1) cells forward. If the cursor is already at the edge of the screen, this has no effect. */
    fun cursorForward(n: Int = 1) { builder.append(CSI).append(n).append('C') }
    /** Moves the cursor n (default 1) cells backward. If the cursor is already at the edge of the screen, this has no effect. */
    fun cursorBack(n: Int = 1) { builder.append(CSI).append(n).append('D') }
    /** Moves cursor to beginning of the line n (default 1) lines down. */
    fun cursorNextLine(n: Int = 1) { builder.append(CSI).append(n).append('E') }
    /** Moves cursor to beginning of the line n (default 1) lines up. */
    fun cursorPreviousLine(n: Int = 1) { builder.append(CSI).append(n).append('F') }
    /** Moves the cursor to column n (default 1). */
    fun cursorSetHorizontal(n: Int = 1) { builder.append(CSI).append(n).append('G') }
    /** Moves the cursor to row n, column m. The values are 1-based, and default to 1 (top left corner) */
    fun cursorPosition(x: Int = 1, y: Int = 1) { builder.append(CSI).append(y).append(';').append(x).append('H') }

    /**
     * Same as [cursorPosition], but counts as a format effector function (like CR or LF)
     * rather than an editor function (like CUD or CNL).
     * This can lead to different handling in certain terminal modes.
     */
    fun cursorPositionAsFormatEffector(x: Int = 1, y: Int = 1) { builder.append(CSI).append(y).append(';').append(x).append('f') }

    // erasing \\
    private fun eraseInDisplay(n: Int = 0) { builder.append(CSI).append(n).append('J') }
    private fun eraseInLine(n: Int = 0) { builder.append(CSI).append(n).append('K') }

    /** Clear from cursor to end of screen */
    fun clearFromCursorToEndOfScreen() = eraseInDisplay(0)
    /** Clear from cursor to beginning of the screen */
    fun clearFromBeginningOfScreenToCursor() = eraseInDisplay(1)
    /** Clear entire screen (may move cursor, but not guaranteed) */
    fun clearScreen() = eraseInDisplay(2)
    /** Clear entire screen and the scrollback buffer (may move cursor, but not guaranteed) */
    fun clearScreenAndBuffer() = eraseInDisplay(3)
    /** Clear from cursor to the end of the line. */
    fun clearFromCursorToEndOfLine() = eraseInLine(0)
    /** Clear from cursor to the beginning of the line */
    fun clearFromBeginningOfLineToCursor() = eraseInLine(1)
    /** Clear the entire line */
    fun clearLine() = eraseInLine(2)

    // scrolling \\
    /** Scroll whole page up by n (default 1) lines. New lines are added at the bottom. */
    fun scrollUp(n: Int = 1) { builder.append(CSI).append(n).append('S') }
    /** Scroll whole page down by n (default 1) lines. New lines are added at the top. */
    fun scrollDown(n: Int = 1) { builder.append(CSI).append(n).append('T') }

    // select graphic rendition \\
    private fun sgr(vararg attributes: Int) { builder.append(CSI).append(attributes.joinToString(separator = ";")).append('m') }

    fun reset() = sgr(0)
    fun bold() = sgr(1)
    fun faint() = sgr(2)
    fun italic() = sgr(3)
    fun underline() = sgr(4)
    fun slowBlink() = sgr(5)
    fun rapidBlink() = sgr(6)
    fun invert() = sgr(7)
    fun conceal() = sgr(8)
    fun strike() = sgr(9)
    fun defaultFont() = sgr(10)
    fun font(n: Int) = sgr(n.coerceIn(1..9) + 10)
    /** rarely supported, [Wikipedia](https://en.wikipedia.org/wiki/Fraktur) */
    fun fraktur() = sgr(20)
    /** supposed to double-underline, but instead disabled bold on several terminals */
    fun doubleUnderline() = sgr(21)
    /** neither [bold] nor [faint] */
    fun normalIntensity() = sgr(22)
    fun noItalic() = sgr(23)
    fun noUnderline() = sgr(24)
    fun noBlink() = sgr(25)
    fun noInvert() = sgr(27)
    fun reveal() = sgr(28)
    fun noStrike() = sgr(29)

    fun foregroundBlack() = sgr(30)
    fun foregroundRed() = sgr(31)
    fun foregroundGreen() = sgr(32)
    fun foregroundYellow() = sgr(33)
    fun foregroundBlue() = sgr(34)
    fun foregroundMagenta() = sgr(35)
    fun foregroundCyan() = sgr(36)
    fun foregroundWhite() = sgr(37)

    fun foreground(n: Int) = sgr(38, 5, n)
    fun foreground(r: Int, g: Int, b: Int) = sgr(38, 2, r, g, b)
    fun foreground(color: Color) = sgr(38, 2, color.red, color.green, color.blue)
    fun resetForeground() = sgr(39)

    fun backgroundBlack() = sgr(40)
    fun backgroundRed() = sgr(41)
    fun backgroundGreen() = sgr(42)
    fun backgroundYellow() = sgr(43)
    fun backgroundBlue() = sgr(44)
    fun backgroundMagenta() = sgr(45)
    fun backgroundCyan() = sgr(46)
    fun backgroundWhite() = sgr(47)

    fun background(n: Int) = sgr(48, 5, n)
    fun background(r: Int, g: Int, b: Int) = sgr(38, 2, r, g, b)
    fun background(color: Color) = sgr(48, 2, color.red, color.green, color.blue)
    fun resetBackground() = sgr(49)

    fun framed() = sgr(51)
    fun encircled() = sgr(52)
    fun overline() = sgr(53)
    fun noFramedOrEncircled() = sgr(54)
    fun noOverline() = sgr(55)

    fun underlineColor(n: Int) = sgr(58, 5, n)
    fun underlineColor(r: Int, g: Int, b: Int) = sgr(58, 2, r, g, b)
    fun underlineColor(color: Color) = sgr(58, 2, color.red, color.green, color.blue)
    fun resetUnderlineColor() = sgr(59)

    fun superscript() = sgr(73)
    fun subscript() = sgr(74)
    fun normalScript() = sgr(75)

    fun foregroundBrightGray() = sgr(90)
    fun foregroundBrightRed() = sgr(91)
    fun foregroundBrightGreen() = sgr(92)
    fun foregroundBrightYellow() = sgr(93)
    fun foregroundBrightBlue() = sgr(94)
    fun foregroundBrightMagenta() = sgr(95)
    fun foregroundBrightCyan() = sgr(96)
    fun foregroundBrightWhite() = sgr(97)

    fun backgroundBrightGray() = sgr(100)
    fun backgroundBrightRed() = sgr(101)
    fun backgroundBrightGreen() = sgr(102)
    fun backgroundBrightYellow() = sgr(103)
    fun backgroundBrightBlue() = sgr(104)
    fun backgroundBrightMagenta() = sgr(105)
    fun backgroundBrightCyan() = sgr(106)
    fun backgroundBrightWhite() = sgr(107)

}
