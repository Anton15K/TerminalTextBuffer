package terminalbuffer

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ScrollbackTest {
    @Test
    fun `insertLineAtBottom pushes top row into scrollback and shifts screen`() {
        val buffer = TerminalBuffer(width = 4, height = 3, maxScrollback = 3)

        buffer.write("AAAA")
        buffer.setCursor(0, 1)
        buffer.write("BBBB")
        buffer.setCursor(0, 2)
        buffer.write("CCCC")

        buffer.insertLineAtBottom()

        assertEquals("AAAA", buffer.getScrollbackLine(0))
        assertFailsWith<IllegalArgumentException> { buffer.getScrollbackLine(1) }
        assertEquals("BBBB", rowAsString(buffer, 0))
        assertEquals("CCCC", rowAsString(buffer, 1))
        assertEquals("    ", rowAsString(buffer, 2))
    }

    @Test
    fun `scrollback is capped and discards oldest rows`() {
        val buffer = TerminalBuffer(width = 3, height = 2, maxScrollback = 2)

        buffer.write("111")
        buffer.setCursor(0, 1)
        buffer.write("222")

        buffer.insertLineAtBottom() // push 111
        buffer.setCursor(0, 1)
        buffer.write("333")

        buffer.insertLineAtBottom() // push 222
        buffer.setCursor(0, 1)
        buffer.write("444")

        buffer.insertLineAtBottom() // push 333, evict 111

        assertEquals("333", buffer.getScrollbackLine(0))
        assertEquals("222", buffer.getScrollbackLine(1))
        assertFailsWith<IllegalArgumentException> { buffer.getScrollbackLine(2) }
    }
}

private fun rowAsString(buffer: TerminalBuffer, row: Int): String {
    val chars = CharArray(buffer.width)
    for (col in 0 until buffer.width) {
        chars[col] = buffer.getChar(col, row)
    }
    return String(chars)
}