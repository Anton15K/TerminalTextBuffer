package terminalbuffer

import terminalbuffer.model.Color
import terminalbuffer.model.Style
import terminalbuffer.model.TextAttributes
import kotlin.test.Test
import kotlin.test.assertEquals

class AttributesTest {
    @Test
    fun `setForeground and setBackground update pen state`() {
        val buffer = TerminalBuffer(width = 4, height = 2, maxScrollback = 5)

        buffer.setForeground(Color.CYAN)
        buffer.setBackground(Color.BRIGHT_BLACK)
        buffer.write("X")

        val attrs = buffer.getAttributes(0, 0)
        assertEquals(Color.CYAN, attrs.fg)
        assertEquals(Color.BRIGHT_BLACK, attrs.bg)
    }

    @Test
    fun `setStyle accumulates unique styles and resetStyle clears them`() {
        val buffer = TerminalBuffer(width = 4, height = 2, maxScrollback = 5)

        buffer.setStyle(Style.BOLD)
        buffer.setStyle(Style.BOLD, Style.UNDERLINE)
        buffer.write("X")

        assertEquals(setOf(Style.BOLD, Style.UNDERLINE), buffer.getAttributes(0, 0).styles)

        buffer.resetStyle()
        buffer.setCursor(1, 0)
        buffer.write("Y")
        assertEquals(emptySet(), buffer.getAttributes(1, 0).styles)
    }

    @Test
    fun `resetAttributes restores defaults`() {
        val buffer = TerminalBuffer(width = 4, height = 2, maxScrollback = 5)

        buffer.setForeground(Color.RED)
        buffer.setBackground(Color.BLUE)
        buffer.setStyle(Style.ITALIC)

        buffer.resetAttributes()
        buffer.write("X")

        assertEquals(TextAttributes(), buffer.getAttributes(0, 0))
    }

    @Test
    fun `attributes remain independent between cells after pen changes`() {
        val buffer = TerminalBuffer(width = 4, height = 2, maxScrollback = 5)

        buffer.setForeground(Color.RED)
        buffer.write("A")

        buffer.setForeground(Color.BLUE)
        buffer.setBackground(Color.BRIGHT_WHITE)
        buffer.setStyle(Style.BOLD)
        buffer.write("B")

        assertEquals(TextAttributes(Color.RED, Color.DEFAULT, emptySet()), buffer.getAttributes(0, 0))
        assertEquals(
            TextAttributes(Color.BLUE, Color.BRIGHT_WHITE, setOf(Style.BOLD)),
            buffer.getAttributes(1, 0),
        )
    }
}