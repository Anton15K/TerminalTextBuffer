package terminalbuffer

import terminalbuffer.model.Cell
import terminalbuffer.model.Color
import kotlin.test.Test
import kotlin.test.assertEquals

class CellTest {
    @Test
    fun `cell defaults are empty with default attributes`() {
        val cell = Cell()

        assertEquals(' ', cell.char)
        assertEquals(Color.DEFAULT, cell.fg)
        assertEquals(Color.DEFAULT, cell.bg)
        assertEquals(emptySet(), cell.style)
    }
}