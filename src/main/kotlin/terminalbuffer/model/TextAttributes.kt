package terminalbuffer.model

/**
 * Immutable snapshot of text styling state (colors and styles).
 *
 * Used both as the "current attributes" for new characters and as a return
 * value when querying existing cell attributes.
 */
data class TextAttributes(
    val fg: Color = Color.DEFAULT,
    val bg: Color = Color.DEFAULT,
    val styles: Set<Style> = emptySet(),
)
