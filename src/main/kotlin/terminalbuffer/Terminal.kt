package terminalbuffer

import terminalbuffer.model.Cell
import terminalbuffer.model.Color
import terminalbuffer.model.CursorPosition
import terminalbuffer.model.Style
import terminalbuffer.model.TextAttributes

/**
 * A terminal screen with a fixed-size [width]×[height] character grid and optional scrollback history.
 *
 * Each [Cell] holds a character with foreground/background color and style attributes.
 * A cursor tracks the current write position. Coordinates are zero-based with `(0, 0)` at the top-left.
 * Scrollback rows are indexed from the most recent (0) to the oldest.
 */
interface Terminal {

    /** Screen width in columns. Always > 0. */
    val width: Int

    /** Screen height in rows. Always > 0. */
    val height: Int

    /** Maximum number of scrollback rows retained. 0 disables scrollback. */
    val maxScrollback: Int

    /** Number of rows currently stored in scrollback. */
    val scrollbackSize: Int

    // ── Cursor ────────────────────────────────────────────────────────

    /** Returns the current cursor position. */
    fun getCursor(): CursorPosition

    /**
     * Moves the cursor to ([col], [row]).
     * Values are clamped to the screen bounds.
     */
    fun setCursor(col: Int, row: Int)

    /** Moves the cursor up by [n] rows (clamped to row 0). No-op if [n] <= 0. */
    fun moveCursorUp(n: Int)

    /** Moves the cursor down by [n] rows (clamped to the last row). No-op if [n] <= 0. */
    fun moveCursorDown(n: Int)

    /** Moves the cursor left by [n] columns (clamped to column 0). No-op if [n] <= 0. */
    fun moveCursorLeft(n: Int)

    /** Moves the cursor right by [n] columns (clamped to the last column). No-op if [n] <= 0. */
    fun moveCursorRight(n: Int)

    // ── Text attributes ──────────────────────────────────────────────

    /** Sets the foreground color for subsequent writes. */
    fun setForeground(color: Color)

    /** Sets the background color for subsequent writes. */
    fun setBackground(color: Color)

    /** Adds [styles] to the current style set for subsequent writes. */
    fun setStyle(vararg styles: Style)

    /** Clears all styles (bold, italic, underline) but keeps colors. */
    fun resetStyle()

    /** Resets foreground, background, and styles to their defaults. */
    fun resetAttributes()

    // ── Content manipulation ─────────────────────────────────────────

    /**
     * Overwrites cells starting at the cursor with [text].
     *
     * Characters are placed left-to-right on the current row using the current
     * attributes. Writing stops at the right edge — no wrapping occurs.
     * After writing, the cursor is at the position after the last written character
     * (clamped to the last column).
     */
    fun write(text: String)

    /**
     * Inserts [text] at the cursor, shifting existing content to the right.
     *
     * For each character:
     * 1. Existing cells from the cursor to the end of the row shift right by one.
     * 2. The overflow cell from the right edge carries to the next row.
     * 3. If overflow reaches the bottom of the screen, [insertLineAtBottom] is
     *    called and the carry cell is placed on the new blank last row.
     *
     * The cursor advances after each character, wrapping to the next row (and
     * scrolling if necessary).
     */
    fun insert(text: String)

    /**
     * Fills the entire current row with [char] using the current attributes.
     * If [char] is `null`, fills with spaces.
     */
    fun fillLine(char: Char?)

    /**
     * Scrolls the screen up by one row.
     *
     * The top row is copied into scrollback (with FIFO eviction if scrollback is
     * full). All rows shift up; a new blank row appears at the bottom. The cursor
     * position does **not** change — content moves under the cursor.
     */
    fun insertLineAtBottom()

    // ── Resize ────────────────────────────────────────────────────────

    /**
     * Resizes the terminal to [newWidth]×[newHeight].
     *
     * Width changes trigger content reflow (wrapped lines are merged/split).
     * Height changes push excess rows to scrollback or pull them back.
     * The cursor is clamped to the new bounds after resize.
     *
     * @throws IllegalArgumentException if [newWidth] or [newHeight] is <= 0.
     */
    fun resize(newWidth: Int, newHeight: Int)

    // ── Screen clearing ──────────────────────────────────────────────

    /** Clears all screen cells and resets the cursor to (0, 0). Scrollback is preserved. */
    fun clearScreen()

    /** Clears both the screen and the scrollback. Resets the cursor to (0, 0). */
    fun clearAll()

    // ── Content access ───────────────────────────────────────────────

    /**
     * Returns the character at screen position ([col], [row]).
     * @throws IllegalArgumentException if the position is out of bounds.
     */
    fun getChar(col: Int, row: Int): Char

    /**
     * Returns the character at ([col], [scrollbackRow]) in the scrollback buffer.
     * Row 0 is the **most recent** scrollback row.
     * @throws IllegalArgumentException if the position is out of bounds.
     */
    fun getCharFromScrollback(col: Int, scrollbackRow: Int): Char

    /**
     * Returns the text attributes at screen position ([col], [row]).
     * @throws IllegalArgumentException if the position is out of bounds.
     */
    fun getAttributes(col: Int, row: Int): TextAttributes

    /**
     * Returns the text attributes at ([col], [scrollbackRow]) in the scrollback buffer.
     * Row 0 is the **most recent** scrollback row.
     * @throws IllegalArgumentException if the position is out of bounds.
     */
    fun getAttributesFromScrollback(col: Int, scrollbackRow: Int): TextAttributes

    /**
     * Returns the content of screen row [row] as a string.
     * Trailing spaces are trimmed.
     */
    fun getLine(row: Int): String

    /**
     * Returns the content of scrollback row [scrollbackRow] as a string.
     * Row 0 is the most recent. Trailing spaces are trimmed.
     */
    fun getScrollbackLine(scrollbackRow: Int): String

    /**
     * Returns the entire screen as a multi-line string (rows joined by `\n`).
     * Trailing spaces on each line are trimmed.
     */
    fun getScreenContent(): String

    /**
     * Returns scrollback + screen as a multi-line string.
     * Scrollback rows come first (oldest to newest), followed by screen rows.
     * Trailing spaces on each line are trimmed.
     */
    fun getFullContent(): String
}
