package terminalbuffer

import terminalbuffer.model.Row
import terminalbuffer.scrollback.ScrollbackBuffer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ScrollbackBufferTest {
    @Test
    fun `constructor rejects negative max size`() {
        assertFailsWith<IllegalArgumentException> {
            ScrollbackBuffer(-1)
        }
    }

    @Test
    fun `size and maxSize getters reflect current state`() {
        val scrollback = ScrollbackBuffer(maxSize = 2)

        assertEquals(2, scrollback.maxSize)
        assertEquals(0, scrollback.size)

        scrollback.push(rowOf("ab"))
        assertEquals(1, scrollback.size)
    }

    @Test
    fun `popNewest returns null when empty and newest first when not empty`() {
        val scrollback = ScrollbackBuffer(maxSize = 3)

        assertNull(scrollback.popNewest())

        scrollback.push(rowOf("11"))
        scrollback.push(rowOf("22"))

        assertEquals("22", scrollback.popNewest()?.asString())
        assertEquals("11", scrollback.popNewest()?.asString())
        assertNull(scrollback.popNewest())
    }

    @Test
    fun `replaceAll keeps only newest rows when input exceeds max size`() {
        val scrollback = ScrollbackBuffer(maxSize = 2)

        scrollback.replaceAll(listOf(rowOf("aa"), rowOf("bb"), rowOf("cc")))

        assertEquals(2, scrollback.size)
        assertEquals("cc", scrollback.getLine(0).asString())
        assertEquals("bb", scrollback.getLine(1).asString())
    }

    @Test
    fun `replaceAll with max size zero clears and keeps empty`() {
        val scrollback = ScrollbackBuffer(maxSize = 0)

        scrollback.push(rowOf("xx"))
        scrollback.replaceAll(listOf(rowOf("aa"), rowOf("bb")))

        assertEquals(0, scrollback.size)
        assertEquals(emptyList(), scrollback.getRows())
    }

    @Test
    fun `getLine rejects negative and too large recent index`() {
        val scrollback = ScrollbackBuffer(maxSize = 2)
        scrollback.push(rowOf("ab"))

        assertFailsWith<IllegalArgumentException> { scrollback.getLine(-1) }
        assertFailsWith<IllegalArgumentException> { scrollback.getLine(1) }
    }

    private fun rowOf(text: String): Row {
        val row = Row(text.length)
        for ((index, ch) in text.withIndex()) {
            row[index] = row[index].copy(char = ch)
        }
        return row
    }
}