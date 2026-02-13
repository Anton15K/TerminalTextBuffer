package terminalbuffer

data class TextAttributes(
    val fg: Color = Color.DEFAULT,
    val bg: Color = Color.DEFAULT,
    val styles: Set<Style> = emptySet(),
)
