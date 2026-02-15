package terminalbuffer

interface Terminal {
    val width: Int
    val height: Int
    val maxScrollback: Int

    fun getCursor(): CursorPosition
    fun setCursor(col: Int, row: Int)
    fun moveCursorUp(n: Int)
    fun moveCursorDown(n: Int)
    fun moveCursorLeft(n: Int)
    fun moveCursorRight(n: Int)

    fun setForeground(color: Color)
    fun setBackground(color: Color)
    fun setStyle(vararg styles: Style)
    fun resetStyle()
    fun resetAttributes()

    fun write(text: String)
    fun insert(text: String)
    fun fillLine(char: Char?)
    fun insertLineAtBottom()

    fun clearScreen()
    fun clearAll()

    fun getChar(col: Int, row: Int): Char
    fun getCharFromScrollback(col: Int, scrollbackRow: Int): Char
    fun getAttributes(col: Int, row: Int): TextAttributes
    fun getAttributesFromScrollback(col: Int, scrollbackRow: Int): TextAttributes
    fun getLine(row: Int): String
    fun getScrollbackLine(scrollbackRow: Int): String
    fun getScreenContent(): String
    fun getFullContent(): String
}
