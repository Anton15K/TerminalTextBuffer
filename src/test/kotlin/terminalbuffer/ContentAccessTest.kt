package terminalbuffer

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ContentAccessTest {
    @Test
    fun `getChar and getAttributes return screen cell data`() {
        val buffer = TerminalBuffer(width = 5, height = 2, maxScrollback = 2)

        buffer.setForeground(Color.BRIGHT_GREEN)
        buffer.setBackground(Color.BLACK)
        buffer.setStyle(Style.BOLD)
        buffer.write("Hi")

        assertEquals('H', buffer.getChar(0, 0))
        assertEquals(TextAttributes(Color.BRIGHT_GREEN, Color.BLACK, setOf(Style.BOLD)), buffer.getAttributes(0, 0))
    }

    @Test
    fun `getLine trims trailing spaces`() {
        val buffer = TerminalBuffer(width = 5, height = 2, maxScrollback = 2)

        buffer.write("abc")

        assertEquals("abc", buffer.getLine(0))
        assertEquals("", buffer.getLine(1))
    }

    @Test
    fun `getScreenContent joins all rows with newlines`() {
        val buffer = TerminalBuffer(width = 4, height = 3, maxScrollback = 2)

        buffer.write("AB")
        buffer.setCursor(0, 1)
        buffer.write("CD")

        assertEquals("AB\nCD\n", buffer.getScreenContent())
    }

    @Test
    fun `scrollback access uses 0 as most recent`() {
        val buffer = TerminalBuffer(width = 3, height = 2, maxScrollback = 3)

        buffer.write("111")
        buffer.setCursor(0, 1)
        buffer.write("222")
        buffer.insertLineAtBottom() // push 111

        buffer.setCursor(0, 1)
        buffer.write("333")
        buffer.insertLineAtBottom() // push 222

        assertEquals("222", buffer.getScrollbackLine(0))
        assertEquals("111", buffer.getScrollbackLine(1))
        assertEquals('2', buffer.getCharFromScrollback(0, 0))
        assertEquals('1', buffer.getCharFromScrollback(0, 1))
    }

    @Test
    fun `getAttributesFromScrollback returns copied attributes`() {
        val buffer = TerminalBuffer(width = 3, height = 2, maxScrollback = 2)

        buffer.setForeground(Color.RED)
        buffer.setStyle(Style.UNDERLINE)
        buffer.write("AAA")
        buffer.insertLineAtBottom()

        assertEquals(
            TextAttributes(Color.RED, Color.DEFAULT, setOf(Style.UNDERLINE)),
            buffer.getAttributesFromScrollback(0, 0),
        )
    }

    @Test
    fun `getFullContent returns scrollback then screen`() {
        val buffer = TerminalBuffer(width = 3, height = 2, maxScrollback = 2)

        buffer.write("111")
        buffer.setCursor(0, 1)
        buffer.write("222")
        buffer.insertLineAtBottom()
        buffer.setCursor(0, 1)
        buffer.write("333")

        assertEquals("111\n222\n333", buffer.getFullContent())
    }

    @Test
    fun `scrollback access out of bounds throws clear error`() {
        val buffer = TerminalBuffer(width = 3, height = 2, maxScrollback = 1)

        val error = assertFailsWith<IllegalArgumentException> {
            buffer.getScrollbackLine(0)
        }

        assertEquals("scrollback row out of bounds", error.message)
    }
}