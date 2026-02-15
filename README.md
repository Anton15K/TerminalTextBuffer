# Terminal Text Buffer

## Overview

This project implements a terminal text buffer in Kotlin: a fixed-size screen grid plus scrollback history.
It models character cells with colors and styles, cursor movement, editing operations, and content access APIs.

Build and test:

- `./gradlew build`
- `./gradlew test`
- `./gradlew test jacocoTestReport`

Coverage report:

- `build/reports/jacoco/test/html/index.html`

## Architecture

The model is layered as:

- `Cell` — one character with foreground/background `Color` and `Style` set.
- `Row` — fixed-width `Array<Cell>` wrapper. Carries a `wrapped` flag indicating the row's content continues on the next row (soft wrap).
- `ReflowHelper` — standalone reflow algorithm that groups wrapped rows into logical lines and re-chunks them to a new width.
- `TerminalBuffer` — owns:
  - `screen: ScreenGrid`
  - `scrollback: ScrollbackBuffer` capped by `maxScrollback`

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
- **Resize — hybrid reflow width + simple height**: width changes trigger content reflow using the `wrapped` flag on `Row` to identify soft-wrapped logical lines. Consecutive wrapped rows are merged, cells are flattened, and re-chunked to the new width. Height changes push excess rows to scrollback or pull them back. Scrollback is also reflowed to the new width. This preserves content across width changes while keeping the implementation simple. The `wrapped` flag is tracked during `insert` operations (which produce soft wraps); `write` (which truncates at the edge) does not set it.
- **Wrap metadata consistency**: rows edited via `write`/`fillLine` are treated as explicit standalone line state, and stale wrap links are cleared to keep resize reflow behavior correct.

## Possible improvements

- Replace screen shifting with a circular screen buffer to avoid row-copy shifts on scroll.
- Build ANSI/VT escape parser to drive buffer updates from terminal streams.


## Testing

The test suite is organized by feature and model type:

- Core buffer behavior: `WriteTest`, `InsertTest`, `CursorTest`, `ScrollbackTest`, `ClearTest`, `ContentAccessTest`, `ResizeTest`, `EdgeCaseTest`
- Model/value objects: `CellTest`, `RowTest`, `ColorTest`, `TextAttributesTest`, `AttributesTest`
- Internal component behavior: `ScrollbackBufferTest`

It covers happy paths, boundary behavior, resize/reflow semantics, and regression scenarios.
