/*
 * This file was written by AetherialKilix (https://github.com/AetherialKilix) on 2025-01-19 (Jan 19 2025).
 *
 * This file is provided "as is" and without any guarantees of quality, functionality, or fitness for a particular purpose.
 * Use at your own risk; I make no claims that this code will work as intended or at all.
 *
 * You are free to use, modify, and distribute this code in any project, personal or professional,
 * without attribution or compensation. No credit to me is required.
 */
package ansi

import java.util.NoSuchElementException

data class ControlSequence(
    val terminator: Type,
    val parameters: String,
    val intermediate: String = ""
): CharSequence by "\u001B[$parameters$terminator" {

    constructor(terminator: Char, parameters: String, intermediate: String = "")
            : this(Type.entries.find { it.terminator == terminator }
        ?: throw NoSuchElementException("No Sequence matches terminator: $terminator"), parameters, intermediate)

    /** read the parameters as a list of semicolon seperated numbers */
    fun parseParameters(): List<Int> = parameters.split(";")
        .mapNotNull { try { Integer.parseInt(it) } catch (e: NumberFormatException) { null } }

    open class Type(val name: String, val terminator: Char) {
        override fun toString(): String = "$name ($terminator)"
        init { entries = entries + listOf(this) }
        companion object  {
            @JvmStatic
            var entries: List<Type> = listOf()
                private set
            val CURSOR_UP = Type("CURSOR_UP", 'A')
            val CURSOR_DOWN = Type("CURSOR_DOWN", 'B')
            val CURSOR_FORWARD = Type("CURSOR_FORWARD", 'C')
            val CURSOR_BACK = Type("CURSOR_BACK", 'D')
            val CURSOR_NEXT_LINE = Type("CURSOR_NEXT_LINE", 'E')
            val CURSOR_PREVIOUS_LINE = Type("CURSOR_PREVIOUS_LINE", 'F')
            val CURSOR_HORIZONTAL_ABSOLUTE = Type("CURSOR_HORIZONTAL_ABSOLUTE", 'G')
            val CURSOR_POSITION = Type("CURSOR_POSITION", 'H')
            val ERASE_IN_DISPLAY = Type("ERASE_IN_DISPLAY", 'J')
            val ERASE_IN_LINE = Type("ERASE_IN_LINE", 'K')
            val CURSOR_POSITION_RESPONSE = Type("CURSOR_POSITION_RESPONSE", 'R')
            val SCROLL_UP = Type("SCROLL_UP", 'S')
            val SCROLL_DOWN = Type("SCROLL_DOWN", 'T')
            val HORIZONTAL_VERTICAL_POSITION = Type("HORIZONTAL_VERTICAL_POSITION", 'f')
            val SELECT_GRAPHICS_RENDITION = Type("SELECT_GRAPHICS_RENDITION", 'm')
            val AUX_PORT = Type("AUX_PORT", 'i')
            val REQUEST_CURSOR_POSITION = Type("REQUEST_CURSOR_POSITION", 'n')
            val SAVE_CURSOR_POSITION = Type("SAVE_CURSOR_POSITION", 's')
            val RESTORE_CURSOR_POSITION = Type("RESTORE_CURSOR_POSITION", 'u')
            val FLAG_HIGH = Type("FLAG_HIGH", 'h')
            val FLAG_LOW = Type("FLAG_LOW", 'l')
        }
    }
    open class Flag(val name: String, val id: String) {
        init { entries = entries + (name to this) }
        companion object {
            @JvmStatic
            var entries: Map<String, Flag> = mapOf()
                private set
            val SHOW_CURSOR = Flag("SHOW_CURSOR", "?25")
            val REPORT_FOCUS = Flag("REPORT_FOCUS", "?1004")
            val ALTERNATIVE_SCREEN_BUFFER = Flag("ALTERNATIVE_SCREEN_BUFFER", "?1049")
            val BRACKETED_PASTE = Flag("BRACKETED_PASTE", "?2004")
        }
    }
    companion object {
        fun cursorUp(rows: Int = 1) = ControlSequence(Type.CURSOR_UP, "$rows")
        fun cursorDown(rows: Int = 1) = ControlSequence(Type.CURSOR_DOWN, "$rows")
        fun cursorForward(cols: Int = 1) = ControlSequence(Type.CURSOR_FORWARD, "$cols")
        fun cursorBack(cols: Int = 1) = ControlSequence(Type.CURSOR_BACK, "$cols")
        fun cursorNextLine(rows: Int = 1) = ControlSequence(Type.CURSOR_NEXT_LINE, "$rows")
        fun cursorPreviousLine(rows: Int = 1) = ControlSequence(Type.CURSOR_PREVIOUS_LINE, "$rows")
        fun cursorHorizontalAbsolute(row: Int = 1) = ControlSequence(Type.CURSOR_HORIZONTAL_ABSOLUTE, "$row")
        fun cursorPosition(x: Int = 1, y: Int = 1) = ControlSequence(Type.CURSOR_POSITION, "$y;$x")

        fun eraseFromCursorToEndOfScreen() = ControlSequence(Type.ERASE_IN_DISPLAY, "0")
        fun eraseFromCursorToStartOfScreen() = ControlSequence(Type.ERASE_IN_DISPLAY, "1")
        fun eraseDisplay() = ControlSequence(Type.ERASE_IN_DISPLAY, "2")
        fun eraseDisplayAndBuffer() = ControlSequence(Type.ERASE_IN_DISPLAY, "3")

        fun eraseFromCursorToEndOfLine() = ControlSequence(Type.ERASE_IN_LINE, "0")
        fun eraseFromCursorToStartOfLine() = ControlSequence(Type.ERASE_IN_DISPLAY, "1")
        fun eraseLine() = ControlSequence(Type.ERASE_IN_DISPLAY, "2")

        fun scrollUp(rows: Int = 1) = ControlSequence(Type.SCROLL_UP, "$rows")
        fun scrollDown(rows: Int = 1) = ControlSequence(Type.SCROLL_DOWN, "$rows")

        fun horizontalVerticalPosition(x: Int = 1, y: Int = 1) = ControlSequence(Type.HORIZONTAL_VERTICAL_POSITION, "$y:$x")
        fun graphicsRendition(rendition: GraphicsRendition) = ControlSequence(Type.SELECT_GRAPHICS_RENDITION, "$rendition")
        fun auxPort(enable: Boolean) = ControlSequence(Type.AUX_PORT, if (enable) "5" else "4")
        fun deviceStatusReport() = ControlSequence(Type.REQUEST_CURSOR_POSITION, "6")

        fun saveCursor() = ControlSequence(Type.SAVE_CURSOR_POSITION, "")
        fun restoreCursor() = ControlSequence(Type.RESTORE_CURSOR_POSITION, "")
        fun setFlag(flag: Flag, enable: Boolean = true) = ControlSequence(if (enable) Type.FLAG_HIGH else Type.FLAG_LOW, flag.id)
        fun unsetFlag(flag: Flag) = setFlag(flag, false)
    }

}
