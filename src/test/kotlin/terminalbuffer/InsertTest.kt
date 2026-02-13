package terminalbuffer

import kotlin.test.Test
import kotlin.test.assertEquals

class InsertTest {
    @Test
    fun `insert at start shifts row right`() {
        val buffer = TerminalBuffer(width = 5, height = 3, maxScrollback = 5)

        buffer.write("ABCD")
        buffer.setCursor(0, 0)
        buffer.insert("X")

        assertEquals("XABCD", rowAsString(buffer, 0))
        assertEquals(CursorPosition(1, 0), buffer.getCursor())
    }

    @Test
    fun `insert in middle shifts tail right`() {
        val buffer = TerminalBuffer(width = 5, height = 3, maxScrollback = 5)

        buffer.write("ABCDE")
        buffer.setCursor(2, 0)
        buffer.insert("Z")

        assertEquals("ABZCD", rowAsString(buffer, 0))
        assertEquals(CursorPosition(3, 0), buffer.getCursor())
    }

    @Test
    fun `insert wraps overflow to next line`() {
        val buffer = TerminalBuffer(width = 4, height = 3, maxScrollback = 5)

        buffer.write("ABCD")
        buffer.setCursor(3, 0)
        buffer.insert("X")

        assertEquals("ABCX", rowAsString(buffer, 0))
        assertEquals("D   ", rowAsString(buffer, 1))
        assertEquals(CursorPosition(0, 1), buffer.getCursor())
    }

    @Test
    fun `insert cascades across multiple lines`() {
        val buffer = TerminalBuffer(width = 4, height = 3, maxScrollback = 5)

        buffer.write("ABCD")
        buffer.setCursor(0, 1)
        buffer.write("EFGH")

        buffer.setCursor(2, 0)
        buffer.insert("12")

        assertEquals("AB12", rowAsString(buffer, 0))
        assertEquals("CDEF", rowAsString(buffer, 1))
        assertEquals("GH  ", rowAsString(buffer, 2))
    }

    private fun rowAsString(buffer: TerminalBuffer, row: Int): String {
        val chars = CharArray(buffer.width)
        for (col in 0 until buffer.width) {
            chars[col] = buffer.getCellForTest(col, row).char
        }
        return String(chars)
    }
}