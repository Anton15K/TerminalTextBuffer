package terminalbuffer

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ColorTest {
    @Test
    fun `color entries contain default and 16 ansi colors`() {
        assertEquals(17, Color.entries.size)
        assertEquals(Color.DEFAULT, Color.entries.first())
        assertTrue(Color.entries.contains(Color.BRIGHT_WHITE))
    }
}