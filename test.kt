/*
 * This file was written by AetherialKilix (https://github.com/AetherialKilix) on 2025-01-19 (Jan 19 2025).
 *
 * This file is provided "as is" and without any guarantees of quality, functionality, or fitness for a particular purpose.
 * Use at your own risk; I make no claims that this code will work as intended or at all.
 *
 * You are free to use, modify, and distribute this code in any project, personal or professional,
 * without attribution or compensation. No credit to me is required.
 *
 * NOTE: this file is a bunch of tests, these are not "proper" unit test and should not be relied on in any way.
 *       any code in here should be considered "off-limits", as it is not only possible, but extremely likely that it changes entirely
 */

import ansi.ControlSequenceParser
import ansi.GraphicsRendition
import java.io.StringReader

fun main() {
    Tests.sequenceParser()
    Tests.graphicsRendition()
}

object Tests {
    fun sequenceParser() {
        val testSequences = listOf(
            "\u001B[31m",         // Set foreground to red
            "\u001B[1;32m",      // Bold green
            "\u001B[0m",         // Reset
            "\u001B[3A",         // Move cursor up 3
            "\u001B[2J",         // Clear screen
            "\u001B[48;5;21m",   // Background bright blue (256-color)
            "\u001B[38;2;255;165;0m", // Foreground orange (truecolor)
            "\u001B[10X",        // Invalid sequence (should trigger onError)
        )

        val parser = ControlSequenceParser(StringReader(testSequences.joinToString("")))

        parser.onSequenceParsed += { println("Parsed: $this") }
        parser.onError += { println("Error: $this") }

        while (true) {
            val sequence = parser.readOne() ?: break
            println("Final parsed sequence: $sequence")
        }
    }

    fun graphicsRendition() {

        val examples = mapOf(
            "red text" to listOf(31),
            "green text" to listOf(32),
            "blue text" to listOf(34),
            "bold red" to listOf(1, 31),
            "italic cyan" to listOf(3, 36),
            "blink yellow" to listOf(5, 33),
            "bold yellow" to listOf(1, 33),
            "underlined magenta" to listOf(4, 35),
            "strikethrough white" to listOf(9, 37),
            "inverted black and white" to listOf(7, 30, 47),
            "background magenta" to listOf(45),
            "foreground bright red (256-color)" to listOf(38, 5, 196),
            "background bright blue (256-color)" to listOf(48, 5, 21),
            "foreground truecolor orange" to listOf(38, 2, 255, 165, 0),
            "background truecolor purple" to listOf(48, 2, 128, 0, 128),
            "reset" to listOf(0),
            "bold, underlined, inverted" to listOf(1, 4, 7),
            "dim, italic, blinking" to listOf(2, 3, 5),
            "bold yellow, then reset, then blue" to listOf(1, 33, 0, 34),
            "underlined magenta, reset, then bold red" to listOf(4, 35, 0, 1, 31),
            "blink yellow, reset, italic cyan" to listOf(5, 33, 0, 3, 36),
            "bold red, reset, then normal green" to listOf(1, 31, 0, 32),
            "inverted black and white, reset, strikethrough white" to listOf(7, 30, 47, 0, 9, 37),
            "foreground truecolor orange, reset, background truecolor purple" to listOf(38, 2, 255, 165, 0, 0, 48, 2, 128, 0, 128),
            "bold, underlined, reset, then dim" to listOf(1, 4, 0, 2),
            "dim, italic, blinking, reset, bold yellow" to listOf(2, 3, 5, 0, 1, 33)
        )

        for ((name, arguments) in examples) {
            val raw = arguments.joinToString(separator = ";")
            val parsed = GraphicsRendition.parse(arguments.iterator())
            println("original:")
            println("\u001B[${raw}m$name\u001B[0m")
            println("passthrough (parsed and re-printed):")
            println("\u001B[${parsed}m$name\u001B[0m")
            println("'compiled':")
            println("${parsed.compile()}$name\u001B[0m")
            println("\u001B[0m")
        }
    }
}
