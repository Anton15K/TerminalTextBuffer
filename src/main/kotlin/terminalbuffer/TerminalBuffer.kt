package terminalbuffer

class TerminalBuffer(
    val width: Int,
    val height: Int,
    val maxScrollback: Int,
) {
    private val screen: Array<Row>
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
            insertSingleChar(char)
            advanceCursorWithWrap()
        }
    }

    fun fillLine(char: Char?) {
        screen[cursorRow].fillWith(char, currentAttributes)
    }

    internal fun getCurrentAttributesForTest(): TextAttributes = currentAttributes
    internal fun getCellForTest(col: Int, row: Int): Cell = screen[row][col]

    private fun insertSingleChar(char: Char) {
        var carry = makeCell(char)

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
                break
            }
        }
    }

    private fun advanceCursorWithWrap() {
        if (cursorColumn < width - 1) {
            cursorColumn += 1
            return
        }

        if (cursorRow < height - 1) {
            cursorColumn = 0
            cursorRow += 1
            return
        }

        cursorColumn = width - 1
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