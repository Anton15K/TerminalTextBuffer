# Terminal Text Buffer

## Overview

This project implements a terminal text buffer in Kotlin: a fixed-size screen grid plus scrollback history.
It models character cells with colors and styles, cursor movement, editing operations, and content access APIs.

Build and test:

- `./gradlew build`
- `./gradlew test`

## Architecture

The model is layered as:

- `Cell` — one character with foreground/background `Color` and `Style` set.
- `Row` — fixed-width `Array<Cell>` wrapper.
- `TerminalBuffer` — owns:
  - `screen: Array<Row>` with `height` rows
  - `scrollback: ArrayDeque<Row>` capped by `maxScrollback`

When a line is inserted at the bottom, the top screen row is pushed to scrollback (FIFO eviction on overflow).

## Design decisions and trade-offs

- **2D grid (`Array<Row>`) over circular buffer**: simpler and easier to reason about; direct random access; less complexity at this scale.
- **Color as enum** (`DEFAULT + 16 ANSI`) over RGB values: type-safe and aligned to task requirements.
- **Styles as enum set** (`Set<Style>`) over manual bitmasks: clear and idiomatic Kotlin API.
- **`write` vs `insert`**:
  - `write` overwrites cells in-place and truncates at right edge (no automatic multi-line wrap).
  - If `write` reaches the right edge, cursor remains clamped at the last column.
  - `insert` shifts content right, wraps across rows, and scrolls when wrapping beyond the last row.
- **Cursor and scrolling**: `insertLineAtBottom()` does not change cursor coordinates; screen content shifts under a stable cursor position.
- **Scrollback eviction**: `ArrayDeque` with FIFO cap (`removeFirst` when above max size).

## Possible improvements

- Replace screen shifting with a circular screen buffer to avoid row-copy shifts on scroll.
- Add 256-color and true-color support.
- Build ANSI/VT escape parser to drive buffer updates from terminal streams.
- Add resize support with configurable strategy (truncate vs reflow).
- Add full Unicode grapheme-cluster support (combining marks, ZWJ sequences, wide chars).

## Testing

The test suite is organized by feature (`CellTest`, `RowTest`, `CursorTest`, `WriteTest`, `InsertTest`, `ScrollbackTest`, `ClearTest`, `ContentAccessTest`, `EdgeCaseTest`).
It covers happy paths, boundary behavior, and error scenarios.
