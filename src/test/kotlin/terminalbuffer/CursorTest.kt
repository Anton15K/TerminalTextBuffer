package terminalbuffer

import kotlin.test.Test
import kotlin.test.assertEquals

class CursorTest {
    @Test
    fun `cursor starts at top left`() {
        val buffer = TerminalBuffer(width = 5, height = 3, maxScrollback = 10)

        assertEquals(CursorPosition(0, 0), buffer.getCursor())
    }

    @Test
    fun `setCursor clamps to bounds`() {
        val buffer = TerminalBuffer(width = 5, height = 3, maxScrollback = 10)

        buffer.setCursor(-1, -1)
        assertEquals(CursorPosition(0, 0), buffer.getCursor())

        buffer.setCursor(99, 99)
        assertEquals(CursorPosition(4, 2), buffer.getCursor())
    }

    @Test
    fun `move cursor methods clamp on boundaries`() {
        val buffer = TerminalBuffer(width = 5, height = 3, maxScrollback = 10)

        buffer.moveCursorRight(10)
        assertEquals(CursorPosition(4, 0), buffer.getCursor())

        buffer.moveCursorDown(10)
        assertEquals(CursorPosition(4, 2), buffer.getCursor())

        buffer.moveCursorLeft(10)
        assertEquals(CursorPosition(0, 2), buffer.getCursor())

        buffer.moveCursorUp(10)
        assertEquals(CursorPosition(0, 0), buffer.getCursor())
    }
}