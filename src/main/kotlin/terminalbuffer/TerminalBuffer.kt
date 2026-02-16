package terminalbuffer

import terminalbuffer.model.Cell
import terminalbuffer.model.Color
import terminalbuffer.model.CursorPosition
import terminalbuffer.model.Row
import terminalbuffer.model.Style
import terminalbuffer.model.TextAttributes
import terminalbuffer.resize.reflowRows
import terminalbuffer.screen.ScreenGrid
import terminalbuffer.scrollback.ScrollbackBuffer

/**
 * Default implementation of [Terminal] backed by a [ScreenGrid] and
 * a [ScrollbackBuffer].
 *
 * @param width  screen width in columns (must be > 0)
 * @param height screen height in rows (must be > 0)
 * @param maxScrollback maximum scrollback rows to retain (0 to disable)
 */
class TerminalBuffer(
    width: Int,
    height: Int,
    override val maxScrollback: Int,
) : Terminal {
    override var width: Int = width
        private set
    override var height: Int = height
        private set
    override val scrollbackSize: Int get() = scrollback.size
    private var screen = ScreenGrid(width, height)
    private var scrollback = ScrollbackBuffer(maxScrollback)
    private var cursorColumn: Int = 0
    private var cursorRow: Int = 0
    private var currentAttributes = TextAttributes()

    override fun getCursor(): CursorPosition = CursorPosition(cursorColumn, cursorRow)

    override fun setCursor(col: Int, row: Int) {
        cursorColumn = clampColumn(col)
        cursorRow = clampRow(row)
    }

    override fun moveCursorUp(n: Int) {
        if (n <= 0) return
        cursorRow = clampRow(cursorRow - n)
    }

    override fun moveCursorDown(n: Int) {
        if (n <= 0) return
        cursorRow = clampRow(cursorRow + n)
    }

    override fun moveCursorLeft(n: Int) {
        if (n <= 0) return
        cursorColumn = clampColumn(cursorColumn - n)
    }

    override fun moveCursorRight(n: Int) {
        if (n <= 0) return
        cursorColumn = clampColumn(cursorColumn + n)
    }

    override fun setForeground(color: Color) {
        currentAttributes = currentAttributes.copy(fg = color)
    }

    override fun setBackground(color: Color) {
        currentAttributes = currentAttributes.copy(bg = color)
    }

    override fun setStyle(vararg styles: Style) {
        currentAttributes = currentAttributes.copy(styles = currentAttributes.styles + styles)
    }

    override fun resetStyle() {
        currentAttributes = currentAttributes.copy(styles = emptySet())
    }

    override fun resetAttributes() {
        currentAttributes = TextAttributes()
    }

    override fun write(text: String) {
        if (text.isEmpty()) return

        screen[cursorRow].wrapped = false
        if (cursorColumn == 0 && cursorRow > 0) {
            screen[cursorRow - 1].wrapped = false
        }

        var col = cursorColumn
        var written = 0
        for (char in text) {
            if (col >= width) break
            screen.setCell(col, cursorRow, makeCell(char))
            col += 1
            written += 1
        }

        if (written > 0) {
            cursorColumn = clampColumn(cursorColumn + written)
        }
    }

    override fun insert(text: String) {
        if (text.isEmpty()) return

        for (char in text) {
            val scrolled = insertSingleChar(char)
            advanceCursorWithWrap(scrolled)
        }
    }

    override fun fillLine(char: Char?) {
        screen.fillRow(cursorRow, char, currentAttributes)
        screen[cursorRow].wrapped = false
        if (cursorRow > 0) {
            screen[cursorRow - 1].wrapped = false
        }
    }

    override fun insertLineAtBottom() {
        val detached = screen.shiftRowsUp()
        scrollback.push(detached)
    }

    override fun clearScreen() {
        screen.clearAll()
        cursorColumn = 0
        cursorRow = 0
    }

    override fun clearAll() {
        clearScreen()
        scrollback.clear()
    }

    override fun resize(newWidth: Int, newHeight: Int) {
        require(newWidth > 0) { "newWidth must be greater than 0" }
        require(newHeight > 0) { "newHeight must be greater than 0" }
        if (newWidth == width && newHeight == height) return



        val screenRows = screen.getRows().toMutableList()
        while (screenRows.size > cursorRow + 1 &&
            !screenRows.last().wrapped &&
            screenRows.last().asString().isEmpty()
        ) {
            screenRows.removeAt(screenRows.size - 1)
        }
        val allRows = scrollback.getRows() + screenRows


        val reflowed = reflowRows(allRows, newWidth)


        val newScreen = ScreenGrid(newWidth, newHeight)
        if (reflowed.size <= newHeight) {
            for (i in reflowed.indices) {
                newScreen[i] = reflowed[i]
            }
            scrollback.replaceAll(emptyList())
        } else {
            val scrollbackRows = reflowed.subList(0, reflowed.size - newHeight)
            val screenRows = reflowed.subList(reflowed.size - newHeight, reflowed.size)
            for (i in screenRows.indices) {
                newScreen[i] = screenRows[i]
            }
            scrollback.replaceAll(scrollbackRows)
        }

        screen = newScreen
        width = newWidth
        height = newHeight


        cursorColumn = cursorColumn.coerceIn(0, newWidth - 1)
        cursorRow = cursorRow.coerceIn(0, newHeight - 1)
    }

    override fun getChar(col: Int, row: Int): Char = screen.getCell(col, row).char

    override fun getCharFromScrollback(col: Int, scrollbackRow: Int): Char =
        scrollback.getCell(col, scrollbackRow).char

    override fun getAttributes(col: Int, row: Int): TextAttributes {
        val cell = screen.getCell(col, row)
        return TextAttributes(cell.fg, cell.bg, cell.style)
    }

    override fun getAttributesFromScrollback(col: Int, scrollbackRow: Int): TextAttributes {
        val cell = scrollback.getCell(col, scrollbackRow)
        return TextAttributes(cell.fg, cell.bg, cell.style)
    }

    override fun getLine(row: Int): String = screen.getRowAsString(row)

    override fun getScrollbackLine(scrollbackRow: Int): String =
        scrollback.getLine(scrollbackRow).asString()

    override fun getScreenContent(): String = screen.asString()

    override fun getFullContent(): String {
        val scrollbackContent = scrollback.asString()
        val screenContent = screen.asString()
        return if (scrollbackContent.isEmpty()) {
            screenContent
        } else {
            "$scrollbackContent\n$screenContent"
        }
    }

    private fun insertSingleChar(char: Char): Boolean {
        var carry = makeCell(char)
        var scrolled = false

        for (row in cursorRow until height) {
            val rowObj = screen[row]
            val startColumn = if (row == cursorRow) cursorColumn else 0
            val overflow = rowObj[width - 1]

            for (column in (width - 1) downTo (startColumn + 1)) {
                rowObj[column] = rowObj[column - 1]
            }

            rowObj[startColumn] = carry
            carry = overflow

            if (isEmptyDefaultCell(carry)) {
                return false
            }
        }

        if (!isEmptyDefaultCell(carry)) {
            insertLineAtBottom()
            screen[height - 1][0] = carry
            scrolled = true
        }

        return scrolled
    }

    private fun advanceCursorWithWrap(scrolledByInsert: Boolean) {
        if (cursorColumn < width - 1) {
            cursorColumn += 1
            return
        }


        screen[cursorRow].wrapped = true

        if (cursorRow < height - 1) {
            cursorColumn = 0
            cursorRow += 1
            return
        }

        if (!scrolledByInsert) {
            insertLineAtBottom()
        }

        cursorColumn = 0
        cursorRow = height - 1
    }

    private fun makeCell(char: Char): Cell = Cell(
        char = char,
        fg = currentAttributes.fg,
        bg = currentAttributes.bg,
        style = currentAttributes.styles,
    )

    private fun isEmptyDefaultCell(cell: Cell): Boolean =
        cell.char == ' ' &&
            cell.fg == Color.DEFAULT &&
            cell.bg == Color.DEFAULT &&
            cell.style.isEmpty()

    private fun clampColumn(col: Int): Int = col.coerceIn(0, width - 1)

    private fun clampRow(row: Int): Int = row.coerceIn(0, height - 1)
}
