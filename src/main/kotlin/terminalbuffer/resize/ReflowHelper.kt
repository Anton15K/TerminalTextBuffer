package terminalbuffer.resize

import terminalbuffer.model.Cell
import terminalbuffer.model.Row

/**
 * Reflows a list of rows to a new width by grouping consecutive wrapped rows
 * into logical lines, flattening their cells, and re-chunking to the target width.
 *
 * Cell references are copied — no deep copy is needed since [Cell] is immutable.
 */
fun reflowRows(rows: List<Row>, newWidth: Int): List<Row> {
    require(newWidth > 0) { "newWidth must be greater than 0" }
    if (rows.isEmpty()) return emptyList()

    val result = mutableListOf<Row>()

    var i = 0
    while (i < rows.size) {
        val cells = mutableListOf<Cell>()
        while (i < rows.size) {
            val row = rows[i]
            val isLast = !row.wrapped
            for (col in 0 until row.width) {
                cells.add(row[col])
            }
            i++
            if (isLast) break
        }

        while (cells.isNotEmpty() && isDefaultEmpty(cells.last())) {
            cells.removeAt(cells.size - 1)
        }

        if (cells.isEmpty()) {
            result.add(Row(newWidth))
        } else {
            var offset = 0
            while (offset < cells.size) {
                val end = minOf(offset + newWidth, cells.size)
                val newRow = Row(newWidth)
                for (j in offset until end) {
                    newRow[j - offset] = cells[j]
                }
                newRow.wrapped = end < cells.size
                result.add(newRow)
                offset = end
            }
        }
    }

    return result
}

private fun isDefaultEmpty(cell: Cell): Boolean =
    cell.char == ' ' &&
        cell.fg == terminalbuffer.model.Color.DEFAULT &&
        cell.bg == terminalbuffer.model.Color.DEFAULT &&
        cell.style.isEmpty()
