package terminalbuffer

import kotlin.test.Test
import kotlin.test.assertEquals

class AttributesTest {
    @Test
    fun `setForeground and setBackground update pen state`() {
        val buffer = TerminalBuffer(width = 4, height = 2, maxScrollback = 5)

        buffer.setForeground(Color.CYAN)
        buffer.setBackground(Color.BRIGHT_BLACK)

        val attrs = buffer.getCurrentAttributesForTest()
        assertEquals(Color.CYAN, attrs.fg)
        assertEquals(Color.BRIGHT_BLACK, attrs.bg)
    }

    @Test
    fun `setStyle accumulates unique styles and resetStyle clears them`() {
        val buffer = TerminalBuffer(width = 4, height = 2, maxScrollback = 5)

        buffer.setStyle(Style.BOLD)
        buffer.setStyle(Style.BOLD, Style.UNDERLINE)

        assertEquals(setOf(Style.BOLD, Style.UNDERLINE), buffer.getCurrentAttributesForTest().styles)

        buffer.resetStyle()
        assertEquals(emptySet(), buffer.getCurrentAttributesForTest().styles)
    }

    @Test
    fun `resetAttributes restores defaults`() {
        val buffer = TerminalBuffer(width = 4, height = 2, maxScrollback = 5)

        buffer.setForeground(Color.RED)
        buffer.setBackground(Color.BLUE)
        buffer.setStyle(Style.ITALIC)

        buffer.resetAttributes()

        assertEquals(TextAttributes(), buffer.getCurrentAttributesForTest())
    }
}