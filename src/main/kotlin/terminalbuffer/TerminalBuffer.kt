package terminalbuffer

class TerminalBuffer(
    val width: Int,
    val height: Int,
    val maxScrollback: Int,
) {
    private val screen: Array<Row>
    private val scrollback = ArrayDeque<Row>()
    private var cursorColumn: Int = 0
    private var cursorRow: Int = 0
    private var currentAttributes = TextAttributes()

    init {
        require(width > 0) { "width must be greater than 0" }
        require(height > 0) { "height must be greater than 0" }
        require(maxScrollback >= 0) { "maxScrollback must be non-negative" }
        screen = Array(height) { Row(width) }
    }

    fun getCursor(): CursorPosition = CursorPosition(cursorColumn, cursorRow)

    fun setCursor(col: Int, row: Int) {
        cursorColumn = clampColumn(col)
        cursorRow = clampRow(row)
    }

    fun moveCursorUp(n: Int) {
        if (n <= 0) return
        cursorRow = clampRow(cursorRow - n)
    }

    fun moveCursorDown(n: Int) {
        if (n <= 0) return
        cursorRow = clampRow(cursorRow + n)
    }

    fun moveCursorLeft(n: Int) {
        if (n <= 0) return
        cursorColumn = clampColumn(cursorColumn - n)
    }

    fun moveCursorRight(n: Int) {
        if (n <= 0) return
        cursorColumn = clampColumn(cursorColumn + n)
    }

    fun setForeground(color: Color) {
        currentAttributes = currentAttributes.copy(fg = color)
    }

    fun setBackground(color: Color) {
        currentAttributes = currentAttributes.copy(bg = color)
    }

    fun setStyle(vararg styles: Style) {
        currentAttributes = currentAttributes.copy(styles = currentAttributes.styles + styles)
    }

    fun resetStyle() {
        currentAttributes = currentAttributes.copy(styles = emptySet())
    }

    fun resetAttributes() {
        currentAttributes = TextAttributes()
    }

    fun write(text: String) {
        if (text.isEmpty()) return

        var col = cursorColumn
        var written = 0
        for (char in text) {
            if (col >= width) break
            screen[cursorRow][col] = makeCell(char)
            col += 1
            written += 1
        }

        if (written > 0) {
            cursorColumn = clampColumn(cursorColumn + written)
        }
    }

    fun insert(text: String) {
        if (text.isEmpty()) return

        for (char in text) {
            val scrolled = insertSingleChar(char)
            advanceCursorWithWrap(scrolled)
        }
    }

    fun fillLine(char: Char?) {
        screen[cursorRow].fillWith(char, currentAttributes)
    }

    fun insertLineAtBottom() {
        if (maxScrollback > 0) {
            scrollback.addLast(copyRow(screen[0]))
            if (scrollback.size > maxScrollback) {
                scrollback.removeFirst()
            }
        }

        // Rows are intentionally moved by reference within the screen region.
        // Only scrollback receives a detached copy of the previous top row.
        for (row in 0 until (height - 1)) {
            screen[row] = screen[row + 1]
        }
        screen[height - 1] = Row(width)
    }

    fun clearScreen() {
        for (row in 0 until height) {
            screen[row] = Row(width)
        }
        cursorColumn = 0
        cursorRow = 0
    }

    fun clearAll() {
        clearScreen()
        scrollback.clear()
    }

    fun getChar(col: Int, row: Int): Char {
        requireScreenPosition(col, row)
        return screen[row][col].char
    }

    fun getCharFromScrollback(col: Int, scrollbackRow: Int): Char =
        getScrollbackRowByRecentIndex(scrollbackRow)[col].char

    fun getAttributes(col: Int, row: Int): TextAttributes {
        requireScreenPosition(col, row)
        val cell = screen[row][col]
        return TextAttributes(cell.fg, cell.bg, cell.style)
    }

    fun getAttributesFromScrollback(col: Int, scrollbackRow: Int): TextAttributes {
        val cell = getScrollbackRowByRecentIndex(scrollbackRow)[col]
        return TextAttributes(cell.fg, cell.bg, cell.style)
    }

    fun getLine(row: Int): String = screen[row].asString()

    fun getScrollbackLine(scrollbackRow: Int): String = getScrollbackRowByRecentIndex(scrollbackRow).asString()

    fun getScreenContent(): String {
        val sb = StringBuilder(height * (width + 1))
        for (i in 0 until height) {
            if (i > 0) sb.append('\n')
            sb.append(screen[i].asString())
        }
        return sb.toString()
    }

    fun getFullContent(): String {
        val totalRows = scrollback.size + height
        val sb = StringBuilder(totalRows * (width + 1))
        for ((i, row) in scrollback.withIndex()) {
            if (i > 0) sb.append('\n')
            sb.append(row.asString())
        }
        if (scrollback.isNotEmpty()) sb.append('\n')
        for (i in 0 until height) {
            if (i > 0) sb.append('\n')
            sb.append(screen[i].asString())
        }
        return sb.toString()
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

    private fun copyRow(source: Row): Row {
        val copy = Row(width)
        for (column in 0 until width) {
            copy[column] = source[column]
        }
        return copy
    }

    private fun getScrollbackRowByRecentIndex(scrollbackRow: Int): Row {
        require(scrollbackRow in 0 until scrollback.size) { "scrollback row out of bounds" }
        val indexFromOldest = scrollback.size - 1 - scrollbackRow
        return scrollback.elementAt(indexFromOldest)
    }

    private fun requireScreenPosition(col: Int, row: Int) {
        require(col in 0 until width) { "screen column out of bounds" }
        require(row in 0 until height) { "screen row out of bounds" }
    }

    private fun isEmptyDefaultCell(cell: Cell): Boolean =
        cell.char == ' ' &&
            cell.fg == Color.DEFAULT &&
            cell.bg == Color.DEFAULT &&
            cell.style.isEmpty()

    private fun clampColumn(col: Int): Int = col.coerceIn(0, width - 1)

    private fun clampRow(row: Int): Int = row.coerceIn(0, height - 1)
}