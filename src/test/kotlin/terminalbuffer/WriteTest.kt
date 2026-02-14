package terminalbuffer

import kotlin.test.Test
import kotlin.test.assertEquals

class WriteTest {
    @Test
    fun `write at start overwrites cells and moves cursor`() {
        val buffer = TerminalBuffer(width = 5, height = 2, maxScrollback = 5)

        buffer.write("abc")

        assertEquals("abc  ", rowAsString(buffer, 0))
        assertEquals(CursorPosition(3, 0), buffer.getCursor())
    }

    @Test
    fun `write at middle overwrites without shifting`() {
        val buffer = TerminalBuffer(width = 5, height = 2, maxScrollback = 5)

        buffer.write("12345")
        buffer.setCursor(2, 0)
        buffer.write("XY")

        assertEquals("12XY5", rowAsString(buffer, 0))
        assertEquals(CursorPosition(4, 0), buffer.getCursor())
    }

    @Test
    fun `write at end of line updates last cell`() {
        val buffer = TerminalBuffer(width = 4, height = 2, maxScrollback = 5)

        buffer.setCursor(3, 0)
        buffer.write("Z")

        assertEquals("   Z", rowAsString(buffer, 0))
        assertEquals(CursorPosition(3, 0), buffer.getCursor())
    }

    @Test
    fun `write exceeding line width truncates at right edge`() {
        val buffer = TerminalBuffer(width = 5, height = 2, maxScrollback = 5)

        buffer.setCursor(3, 0)
        buffer.write("WXYZ")

        assertEquals("   WX", rowAsString(buffer, 0))
        assertEquals(CursorPosition(4, 0), buffer.getCursor())
    }

    @Test
    fun `write applies current pen attributes to written cells`() {
        val buffer = TerminalBuffer(width = 4, height = 2, maxScrollback = 5)

        buffer.setForeground(Color.YELLOW)
        buffer.setBackground(Color.BLUE)
        buffer.setStyle(Style.BOLD, Style.UNDERLINE)
        buffer.write("A")

        assertEquals('A', buffer.getChar(0, 0))
        assertEquals(
            TextAttributes(Color.YELLOW, Color.BLUE, setOf(Style.BOLD, Style.UNDERLINE)),
            buffer.getAttributes(0, 0),
        )
    }

    @Test
    fun `fillLine fills current row and keeps cursor position`() {
        val buffer = TerminalBuffer(width = 4, height = 2, maxScrollback = 5)

        buffer.setCursor(2, 1)
        buffer.setForeground(Color.CYAN)
        buffer.fillLine('*')

        assertEquals("****", rowAsString(buffer, 1))
        assertEquals(CursorPosition(2, 1), buffer.getCursor())
        assertEquals(Color.CYAN, buffer.getAttributes(0, 1).fg)
    }

    @Test
    fun `fillLine null clears row with current attributes`() {
        val buffer = TerminalBuffer(width = 3, height = 2, maxScrollback = 5)

        buffer.write("XYZ")
        assertEquals("XYZ", rowAsString(buffer, 0))
        buffer.setForeground(Color.GREEN)
        buffer.fillLine(null)

        assertEquals("   ", rowAsString(buffer, 0))
        assertEquals(Color.GREEN, buffer.getAttributes(1, 0).fg)
    }
}