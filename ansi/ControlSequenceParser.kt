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

import Emitter
import SafeEmitter
import SimpleEmitter
import java.io.EOFException
import java.io.Reader
import java.util.*
import javax.naming.ldap.Control

class ControlSequenceParser(private val reader: Reader) {

    companion object {
        const val ESC: Char = '\u001B'
        /** bytes from 0x30 until 0x3F */
        val PARAMETER_RANGE = '0'..'?'
        /** bytes from 0x20 until 0x2F */
        val INTERMEDIATE_RANGE = ' '..'/'
        /** bytes from 0x40 until 0x7E */
        val FINAL_RANGE = '@'..'~'
    }

    // emitters hold a list of event handlers, and emit things using operator fun invoke(data: T?)
    /** emitted, when a sequence was successfully parsed */
    val onSequenceParsed: Emitter<ControlSequence> = SimpleEmitter()
    val onNonSequence: Emitter<Char> = SimpleEmitter()
    /** emitted, when parser encounters an error */
    val onError: Emitter<Throwable> = SimpleEmitter()

    private fun read(): ControlSequence {
        // linked lists because I THINK it's the most allocation efficient
        val parameters = LinkedList<Char>()
        val intermediate = LinkedList<Char>()

        while (true) {
            val byte = reader.read()
            if (byte == -1) throw EOFException()

            when (val char = byte.toChar()) {
                in PARAMETER_RANGE -> parameters.add(char)
                in INTERMEDIATE_RANGE -> intermediate.add(char)
                in FINAL_RANGE -> {
                    val sequence = ControlSequence(char, parameters.joinToString(""), intermediate.joinToString(""))
                    onSequenceParsed(sequence)
                    return sequence
                }
            }

        }
    }

    fun readOne(): ControlSequence? {
        try {
            while (true) {
                val first = reader.read().toChar()
                if (first != ESC) {
                    onNonSequence(first)
                    continue
                }
                val second = reader.read().toChar()
                if (second != '[') {
                    onNonSequence(first)
                    onNonSequence(second)
                    continue
                }
                return read()
            }
        } catch (t: Throwable) { onError(t); return null }
    }

    fun readForever(): Nothing {
        try {
            while (true) {
                if (reader.read().toChar() != ESC) continue
                if (reader.read().toChar() != '[') continue
                read()
            }
        } catch (t: Throwable) {
            onError(t)
            throw RuntimeException(t)
        }
    }

}