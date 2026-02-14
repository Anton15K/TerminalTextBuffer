package terminalbuffer

fun rowAsString(buffer: TerminalBuffer, row: Int): String {
    val chars = CharArray(buffer.width)
    for (col in 0 until buffer.width) {
        chars[col] = buffer.getChar(col, row)
    }
    return String(chars)
}
