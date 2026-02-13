package terminalbuffer

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ClearTest {
    @Test
    fun `clearScreen resets screen and cursor but keeps scrollback`() {
        val buffer = TerminalBuffer(width = 4, height = 2, maxScrollback = 3)

        buffer.write("AAAA")
        buffer.setCursor(0, 1)
        buffer.write("BBBB")
        buffer.insertLineAtBottom()
        buffer.setCursor(2, 1)

        buffer.clearScreen()

        assertEquals(CursorPosition(0, 0), buffer.getCursor())
        assertEquals("    ", rowAsString(buffer, 0))
        assertEquals("    ", rowAsString(buffer, 1))
        assertEquals("AAAA", buffer.getScrollbackLine(0))
    }

    @Test
    fun `clearAll resets screen cursor and scrollback`() {
        val buffer = TerminalBuffer(width = 4, height = 2, maxScrollback = 3)

        buffer.write("AAAA")
        buffer.setCursor(0, 1)
        buffer.write("BBBB")
        buffer.insertLineAtBottom()

        buffer.clearAll()

        assertEquals(CursorPosition(0, 0), buffer.getCursor())
        assertEquals("    ", rowAsString(buffer, 0))
        assertEquals("    ", rowAsString(buffer, 1))
        assertFailsWith<IllegalArgumentException> { buffer.getScrollbackLine(0) }
    }
}

private fun rowAsString(buffer: TerminalBuffer, row: Int): String {
    val chars = CharArray(buffer.width)
    for (col in 0 until buffer.width) {
        chars[col] = buffer.getChar(col, row)
    }
    return String(chars)
}