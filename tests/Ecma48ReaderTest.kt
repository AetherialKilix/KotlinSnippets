package tests

import ecma48.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.io.StringReader

class Ecma48ReaderTest {

    @Test
    fun `test parsing single characters`() {
        val reader = Ecma48Reader(StringReader("A"))
        assertTrue(reader.hasNext())
        assertEquals("A", reader.next())
    }

    @Test
    fun `test parsing simple C0 functions`() {
        val reader = Ecma48Reader(StringReader("\u0007"))  // BEL
        assertTrue(reader.hasNext())
        assertEquals(C0Function.BEL, reader.next())
    }

    @Test
    fun `test parsing simple CSI sequence`() {
        val reader = Ecma48Reader(StringReader("\u001b[31;42m"))
        assertTrue(reader.hasNext())
        val sequence = reader.next()
        assertTrue(sequence is ControlSequence)
        assertEquals(StandardCSFunction.SGR, (sequence as ControlSequence).function)
        assertArrayEquals(doubleArrayOf(31.0, 42.0), sequence.parameters)
    }

    @Test
    fun `test serializing CSI sequence`() {
        val sequence = ControlSequence(StandardCSFunction.SGR, doubleArrayOf(31.0, 42.0))
        assertEquals("\u001b[31;42m", sequence.toString()) // Should match the expected sequence
    }

    @Test
    fun `test unknown sequence handling`() {
        val reader = Ecma48Reader(StringReader("\u001b[99;99X")) // X is invalid
        assertTrue(reader.hasNext())
        val result = reader.next()
        assertEquals("\u001b[99;99X", result.toString()) // Should return the raw sequence
    }

    @Test
    fun `test handling of incomplete sequence`() {
        val reader = Ecma48Reader(StringReader("\u001b[31;")) // Truncated CSI
        assertTrue(reader.hasNext())
        val result = StringBuilder().apply {
            while (reader.hasNext()) {
                append(reader.next())
            }
        }.toString()
        assertEquals("\u001b[31;", result) // Should return as-is
    }
}
