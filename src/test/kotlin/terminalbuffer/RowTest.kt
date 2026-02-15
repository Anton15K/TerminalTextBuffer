package terminalbuffer

import terminalbuffer.model.Cell
import terminalbuffer.model.Color
import terminalbuffer.model.Row
import terminalbuffer.model.Style
import terminalbuffer.model.TextAttributes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RowTest {
    @Test
    fun `fillWith applies character and attributes to every cell`() {
        val row = Row(4)
        val attrs = TextAttributes(
            fg = Color.RED,
            bg = Color.BLUE,
            styles = setOf(Style.BOLD, Style.UNDERLINE),
        )

        row.fillWith('x', attrs)

        for (col in 0 until row.width) {
            assertEquals(Cell('x', Color.RED, Color.BLUE, setOf(Style.BOLD, Style.UNDERLINE)), row[col])
        }
    }

    @Test
    fun `asString returns empty for blank row`() {
        val row = Row(5)

        assertEquals("", row.asString())
    }

    @Test
    fun `row access outside boundaries throws`() {
        val row = Row(2)

        assertFailsWith<IndexOutOfBoundsException> { row[-1] }
        assertFailsWith<IndexOutOfBoundsException> { row[2] }
    }
}