package terminalbuffer.model

/**
 * An immutable character cell with styling attributes.
 *
 * A [Cell] is the smallest unit in the terminal grid. By default it represents
 * an unstyled space character.
 *
 * @property char  the character displayed in this cell
 * @property fg    foreground color
 * @property bg    background color
 * @property style set of active text styles (bold, italic, underline)
 */
data class Cell(
    val char: Char = ' ',
    val fg: Color = Color.DEFAULT,
    val bg: Color = Color.DEFAULT,
    val style: Set<Style> = emptySet(),
)
