package terminalbuffer.model

/** Immutable cursor coordinates (zero-based). */
data class CursorPosition(
    val column: Int,
    val row: Int,
)
