package terminalbuffer

import terminalbuffer.model.CursorPosition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EdgeCaseTest {
    @Test
    fun `constructor with zero width and height throws`() {
        assertFailsWith<IllegalArgumentException> { TerminalBuffer(0, 0, 0) }
    }

    @Test
    fun `single cell buffer supports operations`() {
        val buffer = TerminalBuffer(1, 1, 0)

        buffer.write("A")
        assertEquals('A', buffer.getChar(0, 0))

        buffer.insert("B")
        assertEquals(CursorPosition(0, 0), buffer.getCursor())

        buffer.fillLine(null)
        assertEquals(' ', buffer.getChar(0, 0))
    }

    @Test
    fun `write empty string keeps content and cursor unchanged`() {
        val buffer = TerminalBuffer(4, 2, 1)

        buffer.write("AB")
        buffer.setCursor(1, 0)
        val before = buffer.getScreenContent()

        buffer.write("")

        assertEquals(before, buffer.getScreenContent())
        assertEquals(CursorPosition(1, 0), buffer.getCursor())
    }

    @Test
    fun `write longer than width truncates at right edge`() {
        val buffer = TerminalBuffer(3, 2, 1)

        buffer.write("ABCDE")

        assertEquals("ABC", buffer.getLine(0))
        assertEquals(CursorPosition(2, 0), buffer.getCursor())
    }

    @Test
    fun `cursor movement is clamped at boundaries`() {
        val buffer = TerminalBuffer(3, 2, 1)

        buffer.setCursor(-10, -10)
        assertEquals(CursorPosition(0, 0), buffer.getCursor())

        buffer.moveCursorUp(999)
        buffer.moveCursorLeft(999)
        assertEquals(CursorPosition(0, 0), buffer.getCursor())

        buffer.setCursor(10, 10)
        assertEquals(CursorPosition(2, 1), buffer.getCursor())

        buffer.moveCursorDown(999)
        buffer.moveCursorRight(999)
        assertEquals(CursorPosition(2, 1), buffer.getCursor())
    }

    @Test
    fun `insert on last row wraps and scrolls into scrollback`() {
        val buffer = TerminalBuffer(3, 2, 2)

        buffer.write("ABC")
        buffer.setCursor(0, 1)
        buffer.write("DEF")

        buffer.setCursor(2, 1)
        buffer.insert("X")

        assertEquals("ABC", buffer.getScrollbackLine(0))
        assertEquals("DEX", buffer.getLine(0))
        assertEquals("F", buffer.getLine(1))
        assertEquals(CursorPosition(0, 1), buffer.getCursor())
    }

    @Test
    fun `maxScrollback zero keeps no history`() {
        val buffer = TerminalBuffer(3, 2, 0)

        buffer.write("111")
        buffer.setCursor(0, 1)
        buffer.write("222")
        buffer.insertLineAtBottom()

        assertFailsWith<IllegalArgumentException> { buffer.getScrollbackLine(0) }
    }

    @Test
    fun `rapid insertLineAtBottom keeps only newest scrollback rows`() {
        val buffer = TerminalBuffer(2, 2, 2)

        buffer.write("AA")
        buffer.setCursor(0, 1)
        buffer.write("BB")
        buffer.insertLineAtBottom() // AA

        buffer.setCursor(0, 1)
        buffer.write("CC")
        buffer.insertLineAtBottom() // BB

        buffer.setCursor(0, 1)
        buffer.write("DD")
        buffer.insertLineAtBottom() // CC, evict AA

        assertEquals("CC", buffer.getScrollbackLine(0))
        assertEquals("BB", buffer.getScrollbackLine(1))
        assertFailsWith<IllegalArgumentException> { buffer.getScrollbackLine(2) }
    }
}
