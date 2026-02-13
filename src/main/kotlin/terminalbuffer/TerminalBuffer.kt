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

    internal fun getCurrentAttributesForTest(): TextAttributes = currentAttributes

    private fun clampColumn(col: Int): Int = col.coerceIn(0, width - 1)

    private fun clampRow(row: Int): Int = row.coerceIn(0, height - 1)
}