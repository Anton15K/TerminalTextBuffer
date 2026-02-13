package terminalbuffer

import kotlin.test.Test
import kotlin.test.assertEquals

class TextAttributesTest {
    @Test
    fun `copy updates selected fields and keeps others`() {
        val original = TextAttributes(
            fg = Color.GREEN,
            bg = Color.BLACK,
            styles = setOf(Style.ITALIC),
        )

        val copy = original.copy(bg = Color.BRIGHT_WHITE)

        assertEquals(Color.GREEN, copy.fg)
        assertEquals(Color.BRIGHT_WHITE, copy.bg)
        assertEquals(setOf(Style.ITALIC), copy.styles)
    }
}