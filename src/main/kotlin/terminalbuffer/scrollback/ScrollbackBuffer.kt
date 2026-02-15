package terminalbuffer.scrollback

import terminalbuffer.model.Cell
import terminalbuffer.model.Row

/**
 * Scrollback history buffer with a configurable maximum capacity.
 *
 * Rows are stored oldest-first internally, but accessed by recency index
 * (0 = most recent scrollback row).
 *
 * @property maxSize maximum number of rows to retain (0 disables scrollback)
 */
class ScrollbackBuffer(
    val maxSize: Int,
) {
    private val rows = ArrayDeque<Row>()

    init {
        require(maxSize >= 0) { "maxSize must be non-negative" }
    }

    /** Number of rows currently stored. */
    val size: Int get() = rows.size

    /**
     * Pushes a deep copy of [row] into the buffer.
     * If the buffer is at capacity, the oldest row is evicted.
     */
    fun push(row: Row) {
        if (maxSize == 0) return
        rows.addLast(copyRow(row))
        if (rows.size > maxSize) {
            rows.removeFirst()
        }
    }

    /**
     * Returns the scrollback row at [recentIndex] (0 = most recent).
     * @throws IllegalArgumentException if [recentIndex] is out of bounds.
     */
    fun getLine(recentIndex: Int): Row {
        require(recentIndex in 0 until rows.size) { "scrollback row out of bounds" }
        val indexFromOldest = rows.size - 1 - recentIndex
        return rows.elementAt(indexFromOldest)
    }

    /**
     * Returns the cell at ([col], [recentIndex]) in the scrollback buffer.
     * @throws IllegalArgumentException if [recentIndex] is out of bounds.
     */
    fun getCell(col: Int, recentIndex: Int): Cell = getLine(recentIndex)[col]

    fun clear() {
        rows.clear()
    }

    fun asString(): String {
        val sb = StringBuilder()
        for ((i, row) in rows.withIndex()) {
            if (i > 0) sb.append('\n')
            sb.append(row.asString())
        }
        return sb.toString()
    }

    private fun copyRow(source: Row): Row {
        val copy = Row(source.width)
        for (column in 0 until source.width) {
            copy[column] = source[column]
        }
        return copy
    }
}
