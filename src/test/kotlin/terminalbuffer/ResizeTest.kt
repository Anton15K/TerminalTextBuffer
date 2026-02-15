package terminalbuffer

import terminalbuffer.model.Color
import terminalbuffer.model.Style
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ResizeTest {

    // ── Width shrink ─────────────────────────────────────────────────

    @Test
    fun `width shrink reflows content to more rows`() {
        val buf = TerminalBuffer(width = 8, height = 4, maxScrollback = 10)
        buf.setCursor(0, 0)
        buf.insert("ABCDEFGH") // fills row 0 exactly, cursor wraps to row 1
        // Now shrink to width 4: "ABCDEFGH" should become two rows: "ABCD" and "EFGH"
        buf.resize(4, 4)
        assertEquals("ABCD", buf.getLine(0))
        assertEquals("EFGH", buf.getLine(1))
        assertEquals("", buf.getLine(2))
    }

    @Test
    fun `width shrink pushes overflow to scrollback`() {
        val buf = TerminalBuffer(width = 8, height = 2, maxScrollback = 10)
        buf.setCursor(0, 0)
        buf.write("ABCDEFGH") // row 0, not wrapped (write doesn't set wrapped)
        buf.setCursor(0, 1)
        buf.write("12345678") // row 1, not wrapped
        // 2 independent logical lines of 8 chars → each becomes 2 rows at width 4 → 4 total, height=2
        buf.resize(4, 2)
        assertEquals("1234", buf.getLine(0))
        assertEquals("5678", buf.getLine(1))
        assertEquals("EFGH", buf.getScrollbackLine(0)) // most recent scrollback
        assertEquals("ABCD", buf.getScrollbackLine(1))
    }

    // ── Width grow ───────────────────────────────────────────────────

    @Test
    fun `width grow merges wrapped rows back`() {
        val buf = TerminalBuffer(width = 4, height = 4, maxScrollback = 10)
        buf.setCursor(0, 0)
        buf.insert("ABCDEFGH") // wraps: row 0 = "ABCD" (wrapped), row 1 = "EFGH"
        buf.resize(8, 4)
        assertEquals("ABCDEFGH", buf.getLine(0))
        assertEquals("", buf.getLine(1))
    }

    @Test
    fun `width grow does not merge non-wrapped rows`() {
        val buf = TerminalBuffer(width = 4, height = 4, maxScrollback = 10)
        buf.setCursor(0, 0)
        buf.write("AB") // row 0, not wrapped (write doesn't wrap)
        buf.setCursor(0, 1)
        buf.write("CD") // row 1, not wrapped
        buf.resize(8, 4)
        // These are separate logical lines — should NOT merge
        assertEquals("AB", buf.getLine(0))
        assertEquals("CD", buf.getLine(1))
    }

    @Test
    fun `write clears stale wrapped marker before resize reflow`() {
        val buf = TerminalBuffer(width = 4, height = 4, maxScrollback = 10)

        buf.setCursor(0, 0)
        buf.insert("ABCDEFGH") // row0 wrapped into row1

        // Overwrite row1 as a standalone line. If stale wrapped=true is kept,
        // width grow would incorrectly merge row0 and row1.
        buf.setCursor(0, 1)
        buf.write("ZZ")

        buf.resize(8, 4)

        assertEquals("ABCD", buf.getLine(0))
        assertEquals("ZZGH", buf.getLine(1))
    }

    // ── Height shrink ────────────────────────────────────────────────

    @Test
    fun `height shrink pushes bottom rows to scrollback`() {
        val buf = TerminalBuffer(width = 4, height = 4, maxScrollback = 10)
        buf.setCursor(0, 0)
        buf.write("AAA")
        buf.setCursor(0, 1)
        buf.write("BBB")
        buf.setCursor(0, 2)
        buf.write("CCC")
        buf.setCursor(0, 3)
        buf.write("DDD")
        buf.resize(4, 2)
        // Last 2 rows on screen
        assertEquals("CCC", buf.getLine(0))
        assertEquals("DDD", buf.getLine(1))
        // First 2 rows in scrollback
        assertEquals("BBB", buf.getScrollbackLine(0))
        assertEquals("AAA", buf.getScrollbackLine(1))
    }

    // ── Height grow ──────────────────────────────────────────────────

    @Test
    fun `height grow pulls rows back from scrollback`() {
        val buf = TerminalBuffer(width = 4, height = 2, maxScrollback = 10)
        // Push some rows to scrollback
        buf.setCursor(0, 0)
        buf.write("AAA")
        buf.insertLineAtBottom()
        buf.setCursor(0, 0)
        buf.write("BBB")
        buf.insertLineAtBottom()
        buf.setCursor(0, 0)
        buf.write("CCC")
        // scrollback has: AAA, BBB (oldest to newest), screen row 0 = CCC
        buf.resize(4, 4)
        assertEquals("AAA", buf.getLine(0))
        assertEquals("BBB", buf.getLine(1))
        assertEquals("CCC", buf.getLine(2))
        assertEquals("", buf.getLine(3))
    }

    // ── Combined width + height ──────────────────────────────────────

    @Test
    fun `combined width shrink and height grow`() {
        val buf = TerminalBuffer(width = 8, height = 2, maxScrollback = 10)
        buf.setCursor(0, 0)
        buf.insert("ABCDEFGH") // fills row 0, wraps to row 1
        // width 8→4 doubles rows, height 2→6 gives room
        buf.resize(4, 6)
        assertEquals("ABCD", buf.getLine(0))
        assertEquals("EFGH", buf.getLine(1))
        assertEquals(4, buf.width)
        assertEquals(6, buf.height)
    }

    // ── Cursor clamping ──────────────────────────────────────────────

    @Test
    fun `cursor is clamped after resize`() {
        val buf = TerminalBuffer(width = 10, height = 10, maxScrollback = 0)
        buf.setCursor(9, 9)
        buf.resize(5, 5)
        val cursor = buf.getCursor()
        assertEquals(4, cursor.column)
        assertEquals(4, cursor.row)
    }

    @Test
    fun `cursor stays if within new bounds`() {
        val buf = TerminalBuffer(width = 10, height = 10, maxScrollback = 0)
        buf.setCursor(3, 2)
        buf.resize(5, 5)
        val cursor = buf.getCursor()
        assertEquals(3, cursor.column)
        assertEquals(2, cursor.row)
    }

    // ── Attributes preserved ─────────────────────────────────────────

    @Test
    fun `attributes are preserved through reflow`() {
        val buf = TerminalBuffer(width = 4, height = 4, maxScrollback = 10)
        buf.setForeground(Color.RED)
        buf.setStyle(Style.BOLD)
        buf.setCursor(0, 0)
        buf.insert("ABCD") // fills row 0, wraps to row 1
        buf.insert("EF")
        // row 0 = "ABCD" (wrapped), row 1 = "EF"
        buf.resize(8, 4)
        // "ABCDEF" on row 0
        assertEquals("ABCDEF", buf.getLine(0))
        val attrs = buf.getAttributes(0, 0)
        assertEquals(Color.RED, attrs.fg)
        assertEquals(true, attrs.styles.contains(Style.BOLD))
        // Check last char too
        val attrsE = buf.getAttributes(4, 0)
        assertEquals(Color.RED, attrsE.fg)
    }

    // ── Scrollback reflowed ──────────────────────────────────────────

    @Test
    fun `scrollback is reflowed to new width`() {
        val buf = TerminalBuffer(width = 8, height = 2, maxScrollback = 20)
        // Fill and scroll: push wrapped content into scrollback
        buf.setCursor(0, 0)
        buf.insert("ABCDEFGH") // fills row 0 (wrapped), cursor row 1
        buf.insert("12345678") // fills row 1, scrolls row 0 to scrollback, cursor row 1
        // Now scrollback has the wrapped row with ABCDEFGH content
        // Shrink width to 4 — scrollback should be reflowed too
        buf.resize(4, 2)
        assertEquals(4, buf.width)
        // Verify we can access scrollback content (it was reflowed)
        val fullContent = buf.getFullContent()
        assert(fullContent.contains("ABCD")) { "Expected reflowed scrollback content, got: $fullContent" }
    }

    // ── No-op resize ─────────────────────────────────────────────────

    @Test
    fun `resize to same dimensions is a no-op`() {
        val buf = TerminalBuffer(width = 8, height = 4, maxScrollback = 10)
        buf.setCursor(0, 0)
        buf.write("Hello")
        buf.setCursor(3, 2)
        buf.resize(8, 4) // same dimensions
        assertEquals("Hello", buf.getLine(0))
        assertEquals(3, buf.getCursor().column)
        assertEquals(2, buf.getCursor().row)
    }

    // ── Edge cases ───────────────────────────────────────────────────

    @Test
    fun `resize to 1x1`() {
        val buf = TerminalBuffer(width = 4, height = 4, maxScrollback = 10)
        buf.setCursor(0, 0)
        buf.write("ABCD")
        buf.resize(1, 1)
        assertEquals(1, buf.width)
        assertEquals(1, buf.height)
        assertEquals(0, buf.getCursor().column)
        assertEquals(0, buf.getCursor().row)
    }

    @Test
    fun `resize empty buffer`() {
        val buf = TerminalBuffer(width = 4, height = 4, maxScrollback = 10)
        buf.resize(8, 2)
        assertEquals(8, buf.width)
        assertEquals(2, buf.height)
        assertEquals("", buf.getLine(0))
        assertEquals("", buf.getLine(1))
    }

    @Test
    fun `resize with no scrollback configured`() {
        val buf = TerminalBuffer(width = 8, height = 2, maxScrollback = 0)
        buf.setCursor(0, 0)
        buf.write("Hello")
        buf.setCursor(0, 1)
        buf.write("World")
        buf.resize(4, 2)
        // Content should still be present on screen
        assertEquals(4, buf.width)
        assertEquals(2, buf.height)
    }

    @Test
    fun `resize with invalid dimensions throws`() {
        val buf = TerminalBuffer(width = 4, height = 4, maxScrollback = 10)
        assertFailsWith<IllegalArgumentException> { buf.resize(0, 4) }
        assertFailsWith<IllegalArgumentException> { buf.resize(4, 0) }
        assertFailsWith<IllegalArgumentException> { buf.resize(-1, 4) }
    }

    @Test
    fun `multiple consecutive resizes preserve content`() {
        val buf = TerminalBuffer(width = 8, height = 4, maxScrollback = 20)
        buf.setCursor(0, 0)
        buf.write("ABCDEFGH")
        buf.setCursor(0, 1)
        buf.write("12345678")
        buf.resize(4, 8)  // shrink width: each 8-char line → 2 rows at width 4
        buf.resize(16, 2) // grow width: lines merge back
        buf.resize(8, 4)  // back to original
        val content = buf.getScreenContent()
        assert(content.contains("ABCDEFGH")) { "Expected original content after round-trip resize, got: $content" }
        assert(content.contains("12345678")) { "Expected original content after round-trip resize, got: $content" }
    }

    @Test
    fun `operations work correctly after resize`() {
        val buf = TerminalBuffer(width = 8, height = 4, maxScrollback = 10)
        buf.setCursor(0, 0)
        buf.write("Hello")
        buf.resize(12, 6)
        // Write after resize should work with new dimensions
        buf.setCursor(0, 1)
        buf.write("After resize")
        assertEquals("After resize", buf.getLine(1))
        assertEquals(12, buf.width)
    }
}
