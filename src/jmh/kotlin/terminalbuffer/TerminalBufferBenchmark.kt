package terminalbuffer

import org.openjdk.jmh.annotations.*

@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(java.util.concurrent.TimeUnit.MICROSECONDS)
open class TerminalBufferBenchmark {

    private lateinit var buffer: TerminalBuffer

    private val shortText = "Hello"
    private val longText = "A".repeat(50)

    @Setup(Level.Invocation)
    fun setUp() {
        buffer = TerminalBuffer(width = 80, height = 24, maxScrollback = 1000)
    }

    @Benchmark
    fun writeShortText() {
        buffer.write(shortText)
    }

    @Benchmark
    fun writeLongText() {
        buffer.write(longText)
    }

    @Benchmark
    fun insertSingleChar() {
        buffer.insert("A")
    }

    @Benchmark
    fun insertText() {
        buffer.insert("Hello")
    }

    @Benchmark
    fun insertLineAtBottom() {
        buffer.insertLineAtBottom()
    }

    @Benchmark
    fun clearScreen() {
        buffer.clearScreen()
    }

    @Benchmark
    fun getScreenContent(): String {
        return buffer.getScreenContent()
    }

    @Benchmark
    fun getFullContent(): String {
        // Fill some scrollback so there's content to build
        repeat(30) {
            buffer.setCursor(0, 0)
            buffer.write("Line $it: sample scrollback content")
            buffer.insertLineAtBottom()
        }
        return buffer.getFullContent()
    }

    @Benchmark
    fun resizeShrinkWidth() {
        fillScreenWithContent(buffer)
        buffer.resize(40, 24)
    }

    @Benchmark
    fun resizeGrowWidth() {
        val narrow = TerminalBuffer(width = 40, height = 24, maxScrollback = 1000)
        fillScreenWithContent(narrow)
        narrow.resize(80, 24)
    }

    @Benchmark
    fun resizeWithFullScrollback() {
        repeat(1000) {
            buffer.setCursor(0, 0)
            buffer.write("Scrollback line $it with some content here")
            buffer.insertLineAtBottom()
        }
        buffer.resize(40, 24)
    }

    private fun fillScreenWithContent(buf: TerminalBuffer) {
        for (row in 0 until buf.height) {
            buf.setCursor(0, row)
            buf.write("Row $row: sample content for resize benchmark")
        }
    }
}
