package terminalbuffer

data class Cell(
    val char: Char = ' ',
    val fg: Color = Color.DEFAULT,
    val bg: Color = Color.DEFAULT,
    val style: Set<Style> = emptySet(),
)
