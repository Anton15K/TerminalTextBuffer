package terminalbuffer.screen

import terminalbuffer.model.Cell
import terminalbuffer.model.Row
import terminalbuffer.model.TextAttributes

/**
 * Fixed-size grid of [Row]s representing the visible terminal screen.
 *
 * @property width  number of columns (must be > 0)
 * @property height number of rows (must be > 0)
 */
class ScreenGrid(
    val width: Int,
    val height: Int,
) {
    private val rows: Array<Row>

    init {
        require(width > 0) { "width must be greater than 0" }
        require(height > 0) { "height must be greater than 0" }
        rows = Array(height) { Row(width) }
    }

    operator fun get(row: Int): Row = rows[row]

    fun getCell(col: Int, row: Int): Cell {
        requirePosition(col, row)
        return rows[row][col]
    }

    fun setCell(col: Int, row: Int, cell: Cell) {
        rows[row][col] = cell
    }

    fun fillRow(row: Int, char: Char?, attributes: TextAttributes) {
        rows[row].fillWith(char, attributes)
    }

    /**
     * Shifts all rows up by one. Returns the detached top row (before shifting)
     * and inserts a blank row at the bottom.
     */
    fun shiftRowsUp(): Row {
        val detached = rows[0]
        for (row in 0 until (height - 1)) {
            rows[row] = rows[row + 1]
        }
        rows[height - 1] = Row(width)
        return detached
    }

    fun clearAll() {
        for (row in 0 until height) {
            rows[row] = Row(width)
        }
    }

    fun getRowAsString(row: Int): String = rows[row].asString()

    fun asString(): String {
        val sb = StringBuilder(height * (width + 1))
        for (i in 0 until height) {
            if (i > 0) sb.append('\n')
            sb.append(rows[i].asString())
        }
        return sb.toString()
    }

    fun requirePosition(col: Int, row: Int) {
        require(col in 0 until width) { "screen column out of bounds" }
        require(row in 0 until height) { "screen row out of bounds" }
    }
}
