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

import java.awt.Color
import java.util.*

class GraphicsRendition {

    companion object {
        fun parse(args: Iterator<Int>) = GraphicsRendition().apply {
            while (args.hasNext()) {
                try {
                    val code = args.next()
                    val instruction = Instruction.entries[code] ?: continue
                    val value = instruction.read(args)
                    instructions += instruction to value
                } catch (e: Exception) { continue }
            }
        }
        val ansi256Colors =
            Array(16) { SimpleColor.entries[it].color } +
                    Array(216) {
                        val r = ((it / 36) % 6) * 51
                        val g = ((it / 6) % 6) * 51
                        val b = (it % 6) * 51
                        Color(r, g, b)
                    } +
                    Array(24) {
                        val gray = it * 10 + 8
                        Color(gray, gray, gray)
                    }
    }

    private val instructions: MutableList<Pair<Instruction<*>, Any?>> = LinkedList()

    @Suppress("unchecked")
    operator fun <T> get(attribute: Attribute<T>): T? {
        var result: T? = null
        for ((instruction, value) in instructions) {
            if (instruction == attribute) result = value as? T
            else if (instruction == Instruction.RESET) result = null
        }
        return result
    }

    val normalized: GraphicsRendition get() {
        val normalized = GraphicsRendition()
        val state = mutableMapOf<Attribute<*>, Pair<Instruction<*>, Any?>>()

        instructions.forEach {(instruction, value) ->
            if (instruction == Instruction.RESET) state.clear()
            state[instruction.attribute] = instruction to value
        }
        normalized.instructions += state.values.toList()

        return normalized
    }

    private fun <T> then(inst: Instruction<T>, value: T? = null) = GraphicsRendition().apply {
        instructions.addAll(this@GraphicsRendition.instructions)
        instructions.add(inst to value)
    }
    private fun then(index: Int) = GraphicsRendition().apply {
        instructions.addAll(this@GraphicsRendition.instructions)
        instructions.add(Instruction.entries[index]!! to null)
    }

    /** "compile" into an optimized string, useful if the exact same rendition is needed often, or by itself (since the sequence initializer is pre-pended) */
    fun compile() = "\u001B[${normalized}m"
    override fun toString(): String {
        return instructions.joinToString(";") { (instruction: Instruction<*>, value) ->
            val instructionIndex = Instruction.entries.indexOf(instruction)
            val serializedArgs = instruction.write(value).joinToString(";")
            if (serializedArgs.isEmpty()) "$instructionIndex" else "$instructionIndex;$serializedArgs"
        }
    }

    // == mutators == \\
    fun reset() = then(0)
    fun bold() = then(1)
    fun faint() = then(2)
    fun underline() = then(4)
    fun blinkSlow() = then(5)
    fun blinkFast() = then(6)
    fun invert() = then(7)
    fun conceal() = then(8)
    fun strike() = then(9)
    fun resetFont() = then(10)
    fun font(type: Int) = then(10 + type.coerceIn(0..9))
    fun gothic() = then(20)
    fun doubleUnderline() = then(21)
    fun normalIntensity() = then(22)
    fun noItalic() = then(23)
    fun noUnderline() = then(24)
    fun noBlink() = then(25)
    fun proportionalSpacing() = then(26)
    fun notReversed() = then(27)
    fun reveal() = then(28)
    fun noConceal() = then(28)
    fun noStrike() = then(29)

    fun foregroundBlack() = then(30)
    fun foregroundRed() = then(31)
    fun foregroundGreen() = then(32)
    fun foregroundYellow() = then(33)
    fun foregroundBlue() = then(34)
    fun foregroundMagenta() = then(35)
    fun foregroundCyan() = then(36)
    fun foregroundWhite() = then(37)
    fun foreground(color: Color) = then(Instruction.FOREGROUND, color)
    fun foreground(red: Int, green: Int, blue: Int) = then(Instruction.FOREGROUND, Color(red, green, blue))
    fun foregroundReset() = then(39)

    fun backgroundBlack() = then(40)
    fun backgroundRed() = then(431)
    fun backgroundGreen() = then(42)
    fun backgroundYellow() = then(43)
    fun backgroundBlue() = then(44)
    fun backgroundMagenta() = then(45)
    fun backgroundCyan() = then(46)
    fun backgroundWhite() = then(47)
    fun background(color: Color) = then(Instruction.BACKGROUND, color)
    fun background(red: Int, green: Int, blue: Int) = then(Instruction.BACKGROUND, Color(red, green, blue))
    fun backgroundReset() = then(49)

    fun noProportionalSpacing() = then(50)
    fun framed() = then(51)
    fun encircle() = then(52)
    fun overline() = then(53)
    /** disabled encircled and overline */
    fun noStyle() = then(54)
    fun noOverline() = then(55)

    fun underlineColor(color: Color) = then(Instruction.UNDERLINE_COLOR, color)
    fun underlineColor(red: Int, green: Int, blue: Int) = then(Instruction.UNDERLINE_COLOR, Color(red, green, blue))
    fun underlineColorReset() = then(59)

    fun superscript() = then(73)
    fun subscript() = then(74)
    fun normalScript() = then(75)

    fun foregroundGray() = then(90)
    fun foregroundBrightRed() = then(91)
    fun foregroundBrightGreen() = then(92)
    fun foregroundBrightYellow() = then(93)
    fun foregroundBrightBlue() = then(94)
    fun foregroundBrightMagenta() = then(95)
    fun foregroundBrightCyan() = then(96)
    fun foregroundBrightWhite() = then(97)

    fun backgroundGray() = then(100)
    fun backgroundBrightRed() = then(101)
    fun backgroundBrightGreen() = then(102)
    fun backgroundBrightYellow() = then(103)
    fun backgroundBrightBlue() = then(104)
    fun backgroundBrightMagenta() = then(105)
    fun backgroundBrightCyan() = then(106)
    fun backgroundBrightWhite() = then(107)

    enum class SimpleColor(val color: Color) {
        BLACK(Color(12, 12, 12)),
        RED(Color(197, 15, 31)),
        GREEN(Color(19, 161, 14)),
        YELLOW(Color(193, 156, 0)),
        BLUE(Color(0, 55, 218)),
        MAGENTA(Color(136, 23, 152)),
        CYAN(Color(58, 150, 221)),
        WHITE(Color(204, 204, 204)),
        GRAY(Color(118, 118, 118)),
        RED_BRIGHT(Color(231, 72, 86)),
        GREEN_BRIGHT(Color(22, 198, 12)),
        YELLOW_BRIGHT(Color(249, 241, 165)),
        BLUE_BRIGHT(Color(59, 120, 255)),
        MAGENTA_BRIGHT(Color(180, 0, 158)),
        CYAN_BRIGHT(Color(97, 214, 214)),
        WHITE_BRIGHT(Color(242, 242, 242)),
    }
    enum class Intensity { NORMAL, BOLD, FAINT }
    enum class Underline { NONE, SINGLE, DOUBLE }
    enum class    Script { NORMAL, SUPER, SUB }
    enum class     Blink { NONE, SLOW, FAST }
    enum class     Style { NORMAL, FRAMED, ENCIRCLED }
    enum class      Font {
        DEFAULT,
        ALT_1, ALT_2, ALT_3,
        ALT_4, ALT_5, ALT_6,
        ALT_7, ALT_8, ALT_9,
        GOTHIC
    }

    class Attribute<T> private constructor(val type: Class<T>) {
        companion object {
            val RESET = Attribute(Unit::class.java)
            val INTENSITY = Attribute(Intensity::class.java)
            val ITALIC = Attribute(Boolean::class.java)
            val UNDERLINE = Attribute(Underline::class.java)
            val BLINK = Attribute(Blink::class.java)
            val INVERT = Attribute(Boolean::class.java)
            val CONCEIL = Attribute(Boolean::class.java)
            val STRIKE = Attribute(Boolean::class.java)
            val FONT = Attribute(Font::class.java)
            val PROPORTIONAL_SPACING = Attribute(Boolean::class.java)
            val STYLE = Attribute(Style::class.java)
            val OVERLINE = Attribute(Boolean::class.java)
            val SCRIPT = Attribute(Script::class.java)
            val FOREGROUND = Attribute(Color::class.java)
            val BACKGROUND = Attribute(Color::class.java)
            val UNDERLINE_COLOR = Attribute(Color::class.java)
        }
    }

    data class Instruction<T> private constructor(
        val attribute: Attribute<T>,
        val write: Any?.() -> List<Int> = { listOf() },
        val read: Iterator<Int>.() -> T? = { null }
    ) {
        companion object {
            private fun Iterator<Int>.parseColor(): Color? = when (next()) {
                // simple rgb
                2 -> Color(next(), next(), next())
                // 256 color
                5 -> ansi256Colors[next().coerceIn(0..255)]
                else -> null
            }
            private fun writeColor(value: Any?): List<Int> = (value as? Color)
                ?.run { listOf(2, red, green, blue) }
                ?: emptyList()

            val RESET = Instruction(Attribute.RESET)

            val FOREGROUND = Instruction(Attribute.FOREGROUND, ::writeColor) { parseColor() }
            val BACKGROUND = Instruction(Attribute.BACKGROUND, ::writeColor) { parseColor() }
            val UNDERLINE_COLOR = Instruction(Attribute.UNDERLINE_COLOR, ::writeColor) { parseColor() }


            val entries: List<Instruction<*>?> = listOf(
                // 00
                RESET,
                Instruction(Attribute.INTENSITY) { Intensity.BOLD },
                Instruction(Attribute.INTENSITY) { Intensity.FAINT },
                Instruction(Attribute.ITALIC) { true },
                Instruction(Attribute.UNDERLINE) { Underline.SINGLE },
                Instruction(Attribute.BLINK) { Blink.SLOW },
                Instruction(Attribute.BLINK) { Blink.FAST },
                Instruction(Attribute.INVERT) { true },
                Instruction(Attribute.CONCEIL) { true },
                Instruction(Attribute.STRIKE) { true },
                // 10
                Instruction(Attribute.FONT) { Font.DEFAULT },
                Instruction(Attribute.FONT) { Font.ALT_1 },
                Instruction(Attribute.FONT) { Font.ALT_2 },
                Instruction(Attribute.FONT) { Font.ALT_3 },
                Instruction(Attribute.FONT) { Font.ALT_4 },
                Instruction(Attribute.FONT) { Font.ALT_5 },
                Instruction(Attribute.FONT) { Font.ALT_6 },
                Instruction(Attribute.FONT) { Font.ALT_7 },
                Instruction(Attribute.FONT) { Font.ALT_8 },
                Instruction(Attribute.FONT) { Font.ALT_9 },
                // 20
                Instruction(Attribute.FONT) { Font.GOTHIC },
                Instruction(Attribute.UNDERLINE) { Underline.DOUBLE },
                Instruction(Attribute.INTENSITY) { Intensity.NORMAL },
                Instruction(Attribute.ITALIC) { false },
                Instruction(Attribute.UNDERLINE) { Underline.NONE },
                Instruction(Attribute.BLINK) { Blink.NONE },
                Instruction(Attribute.PROPORTIONAL_SPACING) { true },
                Instruction(Attribute.INVERT) { false },
                Instruction(Attribute.CONCEIL) { false },
                Instruction(Attribute.STRIKE) { false },
                // 30
                Instruction(Attribute.FOREGROUND) { SimpleColor.BLACK.color },
                Instruction(Attribute.FOREGROUND) { SimpleColor.RED.color },
                Instruction(Attribute.FOREGROUND) { SimpleColor.GREEN.color },
                Instruction(Attribute.FOREGROUND) { SimpleColor.YELLOW.color },
                Instruction(Attribute.FOREGROUND) { SimpleColor.BLUE.color },
                Instruction(Attribute.FOREGROUND) { SimpleColor.MAGENTA.color },
                Instruction(Attribute.FOREGROUND) { SimpleColor.CYAN.color },
                Instruction(Attribute.FOREGROUND) { SimpleColor.WHITE.color },
                FOREGROUND,
                Instruction(Attribute.FOREGROUND) { null },
                // 40
                Instruction(Attribute.BACKGROUND) { SimpleColor.BLACK.color },
                Instruction(Attribute.BACKGROUND) { SimpleColor.RED.color },
                Instruction(Attribute.BACKGROUND) { SimpleColor.GREEN.color },
                Instruction(Attribute.BACKGROUND) { SimpleColor.YELLOW.color },
                Instruction(Attribute.BACKGROUND) { SimpleColor.BLUE.color },
                Instruction(Attribute.BACKGROUND) { SimpleColor.MAGENTA.color },
                Instruction(Attribute.BACKGROUND) { SimpleColor.CYAN.color },
                Instruction(Attribute.BACKGROUND) { SimpleColor.WHITE.color },
                BACKGROUND,
                Instruction(Attribute.BACKGROUND) { null },
                // 50
                Instruction(Attribute.PROPORTIONAL_SPACING) { false },
                Instruction(Attribute.STYLE) { Style.FRAMED },
                Instruction(Attribute.STYLE) { Style.ENCIRCLED },
                Instruction(Attribute.OVERLINE) { true },
                Instruction(Attribute.STYLE) { Style.NORMAL },
                // 55
                Instruction(Attribute.OVERLINE) { false },
                null, null,
                UNDERLINE_COLOR,
                Instruction(Attribute.UNDERLINE_COLOR) { null },
                // 60 - 72
                null, null, null, null, null, null, null, null, null, null,
                null, null, null,
                // 73
                Instruction(Attribute.SCRIPT) { Script.SUPER },
                Instruction(Attribute.SCRIPT) { Script.SUB },
                Instruction(Attribute.SCRIPT) { Script.NORMAL },
                // 76 - 89
                null, null, null, null,
                null, null, null, null, null, null, null, null, null, null,
                // 90
                Instruction(Attribute.FOREGROUND) { SimpleColor.GRAY.color },
                Instruction(Attribute.FOREGROUND) { SimpleColor.RED_BRIGHT.color },
                Instruction(Attribute.FOREGROUND) { SimpleColor.GREEN_BRIGHT.color },
                Instruction(Attribute.FOREGROUND) { SimpleColor.YELLOW_BRIGHT.color },
                Instruction(Attribute.FOREGROUND) { SimpleColor.BLUE_BRIGHT.color },
                Instruction(Attribute.FOREGROUND) { SimpleColor.MAGENTA_BRIGHT.color },
                Instruction(Attribute.FOREGROUND) { SimpleColor.CYAN_BRIGHT.color },
                Instruction(Attribute.FOREGROUND) { SimpleColor.WHITE_BRIGHT.color },
                // 98 - 99
                null, null,
                // 100
                Instruction(Attribute.BACKGROUND) { SimpleColor.GRAY.color },
                Instruction(Attribute.BACKGROUND) { SimpleColor.RED_BRIGHT.color },
                Instruction(Attribute.BACKGROUND) { SimpleColor.GREEN_BRIGHT.color },
                Instruction(Attribute.BACKGROUND) { SimpleColor.YELLOW_BRIGHT.color },
                Instruction(Attribute.BACKGROUND) { SimpleColor.BLUE_BRIGHT.color },
                Instruction(Attribute.BACKGROUND) { SimpleColor.MAGENTA_BRIGHT.color },
                Instruction(Attribute.BACKGROUND) { SimpleColor.CYAN_BRIGHT.color },
                Instruction(Attribute.BACKGROUND) { SimpleColor.WHITE_BRIGHT.color },
            )
        }
    }

}