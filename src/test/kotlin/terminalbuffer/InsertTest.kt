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

    @Test
    fun `insert applies current attributes to inserted characters`() {
        val buffer = TerminalBuffer(width = 5, height = 3, maxScrollback = 5)

        buffer.write("ABCDE")
        buffer.setCursor(1, 0)
        buffer.setForeground(Color.MAGENTA)
        buffer.setBackground(Color.BRIGHT_BLACK)
        buffer.setStyle(Style.ITALIC)

        buffer.insert("Z")

        assertEquals('Z', buffer.getChar(1, 0))
        assertEquals(
            TextAttributes(Color.MAGENTA, Color.BRIGHT_BLACK, setOf(Style.ITALIC)),
            buffer.getAttributes(1, 0),
        )
    }

    @Test
    fun `insert can trigger multiple scrolls`() {
        val buffer = TerminalBuffer(width = 2, height = 2, maxScrollback = 10)

        buffer.write("ab")
        buffer.setCursor(0, 1)
        buffer.write("cd")
        buffer.setCursor(1, 1)

        buffer.insert("XYZ")

        assertEquals("cX", buffer.getScrollbackLine(0))
        assertEquals("ab", buffer.getScrollbackLine(1))
        assertEquals("YZ", buffer.getLine(0))
        assertEquals("d", buffer.getLine(1))
    }

    @Test
    fun `insert empty string does not change content or cursor`() {
        val buffer = TerminalBuffer(width = 4, height = 2, maxScrollback = 3)

        buffer.write("AB")
        buffer.setCursor(1, 0)
        val before = buffer.getScreenContent()
        val beforeCursor = buffer.getCursor()

        buffer.insert("")

        assertEquals(before, buffer.getScreenContent())
        assertEquals(beforeCursor, buffer.getCursor())
    }
}