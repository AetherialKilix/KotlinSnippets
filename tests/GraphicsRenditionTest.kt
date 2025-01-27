package tests

import ansi.GraphicsRendition
import ecma48.ControlSequence
import ecma48.Ecma48Reader
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.awt.Color

class GraphicsRenditionTest {

    @Test
    fun `test parsing simple SGR sequence`() {
        val rendition = GraphicsRendition.parse(listOf(1).iterator())  // ESC [1m (Bold)
        assertEquals(GraphicsRendition.Intensity.BOLD, rendition[GraphicsRendition.Attribute.INTENSITY])
    }

    @Test
    fun `test parsing multiple SGR attributes`() {
        val rendition = GraphicsRendition.parse(listOf(1, 31).iterator()) // ESC [1;31m (Bold + Red)
        assertEquals(GraphicsRendition.Intensity.BOLD, rendition[GraphicsRendition.Attribute.INTENSITY])
        assertEquals(GraphicsRendition.SimpleColor.RED.color, rendition[GraphicsRendition.Attribute.FOREGROUND])
    }

    @Test
    fun `test reset instruction`() {
        val rendition = GraphicsRendition.parse(listOf(1, 31, 0).iterator()) // ESC [1;31;0m
        assertNull(rendition[GraphicsRendition.Attribute.INTENSITY])
        assertNull(rendition[GraphicsRendition.Attribute.FOREGROUND])
    }

    @Test
    fun `test foreground color parsing`() {
        val rendition = GraphicsRendition.parse(listOf(38, 2, 255, 100, 50).iterator()) // ESC [38;2;255;100;50m
        assertEquals(Color(255, 100, 50), rendition[GraphicsRendition.Attribute.FOREGROUND])
    }

    @Test
    fun `test background color parsing`() {
        val rendition = GraphicsRendition.parse(listOf(48, 2, 10, 20, 30).iterator()) // ESC [48;2;10;20;30m
        assertEquals(Color(10, 20, 30), rendition[GraphicsRendition.Attribute.BACKGROUND])
    }

    @Test
    fun `test compiling ANSI sequence`() {
        val rendition = GraphicsRendition().bold().foregroundRed()
        assertEquals("\u001B[1;31m", rendition.compile())  // ESC [1;31m
    }

    @Test
    fun `test normalizing removes duplicate attributes`() {
        val rendition = GraphicsRendition.parse(listOf(1, 22).iterator()) // ESC [1;22m
        assertEquals(GraphicsRendition.Intensity.NORMAL, rendition[GraphicsRendition.Attribute.INTENSITY]) // Normal Intensity resets bold/faint
    }

    @Test
    fun `test connection to Ecma48Reader`() {
        val reader = Ecma48Reader(java.io.StringReader("\u001B[1;31m"))
        val sequence = reader.next()
        assertTrue(sequence is ControlSequence)

        val rendition = GraphicsRendition.parse((sequence as ControlSequence).parameters.map { it.toInt() }.iterator())
        assertEquals(GraphicsRendition.Intensity.BOLD, rendition[GraphicsRendition.Attribute.INTENSITY])
        assertEquals(GraphicsRendition.SimpleColor.RED.color, rendition[GraphicsRendition.Attribute.FOREGROUND])
    }
}
