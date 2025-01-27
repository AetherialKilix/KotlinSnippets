/*
 * This file was written by AetherialKilix (https://github.com/AetherialKilix) on 2025-01-26 (Jan 26 2025).
 *
 * This file is provided "as is" and without any guarantees of quality, functionality, or fitness for a particular purpose.
 * Use at your own risk; I make no claims that this code will work as intended or at all.
 *
 * You are free to use, modify, and distribute this code in any project, personal or professional,
 * without attribution or compensation. No credit to me is required.
 *
 * This file was constructed using the ECMA 48 (5th Edition).
 * I tried to create a light-weight (de-)serialization system, that follows that spec,
 * but as stated above, I provide no guarantees about the functionality!
 */
package ecma48

import ecma48.ControlSequence.Companion.intermediateRange
import ecma48.ControlSequence.Companion.parameterRange
import java.io.EOFException
import java.io.Reader
import java.util.*
import kotlin.collections.*

/**
 * C0 control functions are represented in both 7-bit and 8-bit mode by bit combinations from 0x00 to 0x1F.
 * This Table does not have ecma48 gaps, and no "offset", so each entry corresponds to it's [ordinal]
 */
enum class C0Function: CharSequence {
    NUL, SOH, STX, ETX, EOT, ENQ, ACK, BEL,  BS,  HT,  LF,  VT,  FF,  CR, LS0, LS1,
    DLE, DC1, DC2, DC3, DC4, NAK, SYN, ETB, CAN,  EM, SUB, ESC, IS4, IS3, IS2, IS1;
    val code = this.ordinal
    companion object {
        @JvmStatic @JvmName("find")
        operator fun get(code: Int) = entries.getOrNull(code)
    }
    val str: String by lazy { code.toChar().toString() }
    override fun toString(): String = str
    override val length: Int = str.length
    override fun get(index: Int): Char = str[index]
    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence =
        str.subSequence(startIndex, endIndex)
}

/**
 * C1 control function are represented differently in 7-bit and 8-bit mode,
 * use the [code7bit] and [code8bit] members to use.
 */
enum class C1Function(base: Int): CharSequence {
    /* -- (0x00) */ DCS(0x10),
    /* -- (0x01) */ PU1(0x11),
    BPH(0x02), PU2(0x12),
    BNH(0x03), STS(0x13),
    /* -- (0x04) */ CCH(0x14),
    NEL(0x05),  MW(0x15),
    SSA(0x06), SPA(0x16),
    ESA(0x07), EPA(0x17),
    HTS(0x08), SOS(0x18),
    HTJ(0x09), /* -- (0x19) */
    VTS(0x0A), SCI(0x1A),
    PLD(0x0B), CSI(0x1B),
    PLU(0x0C),  ST(0x1C),
     RI(0x0D), OSC(0x1D),
    SS2(0x0E),  PM(0x1E),
    SS3(0x0F), APC(0x1F);

    val code7bit = base + 0x40
    val code8bit = base + 0x80
    companion object {
        private val map =
            entries.associateBy { it.code7bit } + entries.associateBy { it.code8bit }
        @JvmStatic @JvmName("find")
        operator fun get(code: Int) = map[code]
    }
    fun to8BitString(): String = code8bit.toChar().toString()
    val str: String by lazy { C0Function.ESC.toString() + code7bit.toChar() }
    override fun toString(): String = str
    override val length: Int = str.length
    override fun get(index: Int): Char = str[index]
    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence =
        str.subSequence(startIndex, endIndex)
}

private val IntArray.str: String; get() = joinToString(separator = "") { it.toChar().toString() }

/**
 * A control sequence is a string of bit combinations prefixed by [CSI][C1Function.CSI].
 *
 * CSI is followed by:
 * - ecma48 number of parameter bytes in the range [0x30..0x3F][parameterRange],
 * - ecma48 number of intermediate bytes in the range [0x20..0x25][intermediateRange]
 * - the final byte in the range 0x40..0xFE.
 *
 * Final bytes should be represented by the [StandardCSFunction] enum,
 * however final bytes in the range of 0xF0..0xFE are [private functions][PrivateCSFunction],
 * thus all functions are represented as [CSFunction]
 */
data class ControlSequence(
    val function: CSFunction,
    val parameterBytes: IntArray,
    val intermediateBytes: IntArray,
) : CharSequence {

    constructor(
        function: StandardCSFunction,
        parameters: DoubleArray,
        intermediateBytes: IntArray = IntArray(0)
    ) : this(
        function,
        parameters
            .map<Number> { if (it % 1 == 0.0) it.toInt() else it }
            .joinToString(separator = ";")
            .map { it.code }
            .toIntArray(),
        intermediateBytes
    )

    companion object {
        /**
         * Represents all allowed byte values for parameters.
         * These are usually a semicolon-seperated list of numbers.
         * If such a syntax is expected, the [parameters] member may be used to access them.
         */
        val parameterRange = 0x30 .. 0x3F
        /**
         * Represents all allowed byte values for intermediate bytes.
         * Intermediate bytes could be interpreted as modifiers for the final byte.
         */
        val intermediateRange = 0x20..0x25
    }

    /** the parameter bytes interpreted as a char array */
    val parameterChars: CharArray by lazy { CharArray(parameterBytes.size) { index -> parameterBytes[index].toChar() } }
    /** the parameter bytes interpreted as a semicolon-seperated list of number  */
    val parameters: DoubleArray by lazy { String(parameterChars).split(";").map { it.toDoubleOrNull() ?: 1.0 }.toDoubleArray() }
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ControlSequence) return false

        if (!function.isSame(other.function)) return false
        if (!parameterBytes.contentEquals(other.parameterBytes)) return false
        if (!intermediateBytes.contentEquals(other.intermediateBytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = function.hash
        result = 31 * result + parameterBytes.contentHashCode()
        result = 31 * result + intermediateBytes.contentHashCode()
        return result
    }

    val str: String by lazy { "${C1Function.CSI}${parameterBytes.str}$function" }

    override fun toString(): String = str
    override val length: Int = str.length

    override fun get(index: Int): Char = str[index]
    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence =
        str.subSequence(startIndex, endIndex)



}

/**
 * Basic representation of a [ControlSequence] function (identifier)
 * It defines both intermediate and final bytes, and provides [isSame] and [hash].
 * Instead of implementing this interface please extend [PrivateCSFunction].
 * @see [StandardCSFunction] [PrivateCSFunction].
 */
sealed interface CSFunction {
    val final: Int
    val intermediate: IntArray

    fun isSame(other: Any?): Boolean =
        other is PrivateCSFunction
                && final == other.final
                && intermediate.contentEquals(other.intermediate)
    val hash: Int; get() = 31 * final + intermediate.contentHashCode()
    val str: String; get() = "${intermediate.str}${final.toChar()}"
    companion object {
        @JvmStatic @JvmName("find") @JvmOverloads
        operator fun get(code: Int, intermediate: IntArray? = null): CSFunction? = when {
            code in 0x40..0x6F && (intermediate == null || intermediate.isEmpty()) -> StandardCSFunction[code]
            code in 0x40..0x6F && intermediate?.single() == 0x20 -> StandardCSFunction[code, true]
            code in 0x70..0x7F -> PrivateCSFunction(code, intermediate ?: IntArray(0))
            else -> null
        }
    }
}

/**
 * Standard [functions][CSFunction] defined by ECMA 48.
 */
enum class StandardCSFunction(override val final: Int, intermediate: Int? = null) : CSFunction {
    ICH(0x40),  DCH(0x50), HPA(0x60), /* | */   SL(0x40, 0x20),  PPA(0x50, 0x20), TATE(0x60, 0x20),
    CUU(0x41),  SSE(0x51), HPR(0x61), /* | */   SR(0x41, 0x20),  PPR(0x51, 0x20), TALE(0x61, 0x20),
    CUD(0x42),  CPR(0x52), REP(0x62), /* | */  GSM(0x42, 0x20),  PPB(0x52, 0x20),  TAC(0x62, 0x20),
    CUF(0x43),   SU(0x53),  DA(0x63), /* | */  GSS(0x43, 0x20),  SPD(0x53, 0x20),  TCC(0x63, 0x20),
    CUB(0x44),   SD(0x54), VPA(0x64), /* | */  FNT(0x44, 0x20),  DTA(0x54, 0x20),  TSR(0x64, 0x20),
    CNL(0x45),   NP(0x55), VPR(0x65), /* | */  TSS(0x45, 0x20),  SHL(0x55, 0x20),  SCO(0x65, 0x20),
    CPL(0x46),   PP(0x56), HVP(0x66), /* | */  JFY(0x46, 0x20),  SLL(0x56, 0x20), SRCS(0x66, 0x20),
    CHA(0x47),  CTC(0x57), TBC(0x67), /* | */  SPI(0x47, 0x20),  FNK(0x57, 0x20),  SCS(0x67, 0x20),
    CUP(0x48),  ECH(0x58),  SM(0x68), /* | */ QUAD(0x48, 0x20), SPQR(0x58, 0x20),  SLS(0x68, 0x20),
    CHT(0x49),  CVT(0x59),  MC(0x69), /* | */  SSU(0x49, 0x20),  SEF(0x59, 0x20), /*             */
     ED(0x4A),  CBT(0x5A), HPB(0x6A), /* | */  PFS(0x4A, 0x20),  PEC(0x5A, 0x20), /*             */
     EL(0x4B),  SRS(0x5B), VPB(0x6B), /* | */  SHS(0x4B, 0x20),  SSW(0x5B, 0x20),  SCP(0x6B, 0x20),
     IL(0x4C),  PTX(0x5C),  RM(0x6C), /* | */  SVS(0x4C, 0x20), SACS(0x5C, 0x20), /*             */
     DL(0x4D),  SDS(0x5D), SGR(0x6D), /* | */  IGS(0x4D, 0x20), SAPV(0x5D, 0x20), /*             */
     EF(0x4E), SIMD(0x5E), DSR(0x6E), /* | */ /*             */ STAB(0x5E, 0x20), /*             */
     EA(0x4F), /*       */ DAQ(0x6F), /* | */ IDCS(0x4F, 0x20),  GCC(0x5F, 0x20); /*             */

    override val intermediate: IntArray = intermediate?.let { intArrayOf(it) } ?: IntArray(0)
    companion object {
        private val map = entries.associateBy { it.final to it.intermediate.isNotEmpty() }
        @JvmStatic @JvmName("find") @JvmOverloads
        operator fun get(code: Int, has0x20Intermediate: Boolean = false) = map[code to has0x20Intermediate]
    }
    override fun toString(): String = super.str
}

private val ranges = arrayOf(
    0x0000 .. 0x001F,
    0x007F .. 0x009F,
    0x200B .. 0x200D,
    0x202A .. 0x202E
)
/**
 * Convert all non-printable characters to their \uXXXX-representation
 */
fun CharSequence.printable(): String = StringBuilder().apply {
    for (c in this@printable)
        if (ranges.any { c.code in it }) append("\\u%04x".format(c.code))
        else append(c)
}.toString()

/**
 * Any non-standard [CSFunction].
 */
open class PrivateCSFunction(
    override val final: Int,
    override val intermediate: IntArray
) : CSFunction {
    final override fun equals(other: Any?): Boolean = super.isSame(other)
    final override fun hashCode(): Int = super.hash
    final override fun toString(): String = super.str
}

class Ecma48Reader(
    private val source: Reader
) : Iterator<CharSequence>, AutoCloseable {

    private var closed = false
    private val buffer = ArrayDeque<Int>()

    private fun readNext(): Int =
        if (closed) -1
        else source.read().apply { if (this == -1) closed = true }

    private fun nextByte(): Int? = buffer.removeFirstOrNull() ?: readNext().takeIf { it >= 0 }

    /**
     * Reads until a Sequence (Control or otherwise) is found.
     * This may read:
     * - zero bytes from the source if there are bytes buffered
     * - one byte from the source if no bytes are buffered, and the first byte is a non-ecma48 byte
     * - multiple bytes from the source if no bytes are buffered, and the first byte is an ecma48 byte
     *
     * Note that:
     * - The number of bytes added/removed from the buffer and the bytes read have no correlation!!
     * - This method assumes that [C1 functions][C1Function] are used in the 7-bit [ESC][C0Function.ESC]-prefixed mode
     */
    override fun next(): CharSequence {
        // read the first byte, if null (-1), EOF
        val firstByte: Int = nextByte() ?: throw NoSuchElementException(EOFException())

        // find the corresponding C0 function, if none, return the byte
        val c0: C0Function = C0Function[firstByte] ?: return firstByte.toChar().toString()
        // return any non-complex functions immediately
        if (c0 != C0Function.ESC) return c0

        // read the next byte, if null (-1), return the function from last step
        val secondByte: Int = nextByte() ?: return c0
        // find the corresponding C1 function, if none, return the byte
        val c1: C1Function = C1Function[secondByte] ?: return firstByte.toChar().toString().apply {
            // we return the first byte and put the second byte into the
            buffer.addLast(secondByte)
        }
        // return any non-CSI functions immediately (for now)
        if (c1 != C1Function.CSI) return c1


        // temporary storage for loops
        var tmp: Int = nextByte() ?: return firstByte.toChar().toString().apply { // incomplete/broken sequence
            buffer.addLast(secondByte)
        }

        // read parameters
        val parameters: Queue<Int> = LinkedList()
        while (tmp in ControlSequence.parameterRange) {
            parameters.offer(tmp)
            tmp = nextByte() ?: return firstByte.toChar().toString().apply {
                buffer.addLast(secondByte)
                buffer.addAll(parameters)
            }
        }

        // read intermediate
        val intermediate: Queue<Int> = LinkedList()
        while (tmp in ControlSequence.intermediateRange) {
            intermediate.offer(tmp)
            tmp = nextByte() ?: return firstByte.toChar().toString().apply {
                buffer.addLast(secondByte)
                buffer.addAll(parameters)
                buffer.addAll(intermediate)
            }
        }

        // final byte is left in tmp var
        val finalByte = tmp
        val intermediateArray = intermediate.toIntArray()
        // try to find CS function, or append all the unused bytes so far to the buffer
        val function = CSFunction[finalByte, intermediateArray] ?: return firstByte.toChar().toString().apply {
            buffer.addLast(secondByte)
            buffer.addAll(parameters)
            buffer.addAll(intermediate)
            buffer.addLast(finalByte)
        }

        return ControlSequence(function, parameters.toIntArray(), intermediateArray)
    }

    override fun hasNext(): Boolean = buffer.isNotEmpty() || ! closed
    override fun close() { source.close(); closed = true }

}






