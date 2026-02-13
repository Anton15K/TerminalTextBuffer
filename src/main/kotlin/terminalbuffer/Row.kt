package terminalbuffer

class Row(
    val width: Int,
) {
    private val cells: Array<Cell>

    init {
        require(width > 0) { "width must be greater than 0" }
        cells = Array(width) { Cell() }
    }

    operator fun get(column: Int): Cell = cells[column]

    operator fun set(column: Int, cell: Cell) {
        cells[column] = cell
    }

    fun fillWith(char: Char?, attributes: TextAttributes) {
        val value = char ?: ' '
        val cell = Cell(value, attributes.fg, attributes.bg, attributes.styles)
        for (column in cells.indices) {
            cells[column] = cell
        }
    }

    fun asString(): String = String(cells.map { it.char }.toCharArray()).trimEnd()
}