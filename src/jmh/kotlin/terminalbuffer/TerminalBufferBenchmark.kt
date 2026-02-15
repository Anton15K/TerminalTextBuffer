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
}
