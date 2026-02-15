package terminalbuffer.demo

import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import java.awt.GridLayout
import java.time.LocalTime
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JSplitPane
import javax.swing.JTextArea
import javax.swing.JTextField
import javax.swing.SwingUtilities
import terminalbuffer.TerminalBuffer
import terminalbuffer.model.Color
import terminalbuffer.model.Style

fun main() {
    SwingUtilities.invokeLater {
        BufferUiDemo().show()
    }
}

private class BufferUiDemo {
    private val buffer = TerminalBuffer(width = 24, height = 8, maxScrollback = 20)

    private var selectedFg: Color = Color.DEFAULT
    private var selectedBg: Color = Color.DEFAULT

    private val screenArea = JTextArea()
    private val fullArea = JTextArea()
    private val logArea = JTextArea()
    private val cursorLabel = JLabel()

    private val textField = JTextField("hello")
    private val fillField = JTextField("*")
    private val colField = JTextField("0")
    private val rowField = JTextField("0")

    private val fgCombo = JComboBox(Color.entries.toTypedArray())
    private val bgCombo = JComboBox(Color.entries.toTypedArray())
    private val boldCheckbox = JCheckBox("Bold")
    private val italicCheckbox = JCheckBox("Italic")
    private val underlineCheckbox = JCheckBox("Underline")

    fun show() {
        val frame = JFrame("TerminalBuffer UI Demo")
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.contentPane.layout = BorderLayout()

        frame.contentPane.add(buildControls(), BorderLayout.NORTH)
        frame.contentPane.add(buildContentSplit(), BorderLayout.CENTER)
        frame.contentPane.add(buildStatusPanel(), BorderLayout.SOUTH)

        setupTextAreas()
        refreshView("initial state")

        frame.preferredSize = Dimension(1200, 760)
        frame.pack()
        frame.setLocationRelativeTo(null)
        frame.isVisible = true
    }

    private fun buildControls(): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.border = BorderFactory.createEmptyBorder(8, 8, 8, 8)

        val row1 = JPanel(GridLayout(2, 6, 8, 4))
        row1.add(JLabel("Text"))
        row1.add(textField)
        row1.add(button("Write") { perform("write") { buffer.write(textField.text) } })
        row1.add(button("Insert") { perform("insert") { buffer.insert(textField.text) } })
        row1.add(JLabel("Fill char"))
        row1.add(fillField)
        row1.add(button("Fill Line") {
            perform("fillLine") {
                val char = fillField.text.firstOrNull()
                buffer.fillLine(char)
            }
        })
        row1.add(button("Scroll +1") { perform("insertLineAtBottom") { buffer.insertLineAtBottom() } })
        row1.add(button("Clear Screen") { perform("clearScreen") { buffer.clearScreen() } })
        row1.add(button("Clear All") { perform("clearAll") { buffer.clearAll() } })
        row1.add(button("Reset Attr") { perform("resetAttributes") { buffer.resetAttributes() } })
        row1.add(button("Reset Styles") { perform("resetStyle") { buffer.resetStyle() } })

        val row2 = JPanel(GridLayout(2, 8, 8, 4))
        row2.add(JLabel("Cursor col"))
        row2.add(colField)
        row2.add(JLabel("Cursor row"))
        row2.add(rowField)
        row2.add(button("Set Cursor") {
            perform("setCursor") {
                val col = colField.text.toIntOrNull() ?: 0
                val row = rowField.text.toIntOrNull() ?: 0
                buffer.setCursor(col, row)
            }
        })
        row2.add(button("←") { perform("moveCursorLeft(1)") { buffer.moveCursorLeft(1) } })
        row2.add(button("→") { perform("moveCursorRight(1)") { buffer.moveCursorRight(1) } })
        row2.add(button("↑") { perform("moveCursorUp(1)") { buffer.moveCursorUp(1) } })
        row2.add(button("↓") { perform("moveCursorDown(1)") { buffer.moveCursorDown(1) } })
        row2.add(JLabel("FG"))
        row2.add(fgCombo)
        row2.add(JLabel("BG"))
        row2.add(bgCombo)
        row2.add(boldCheckbox)
        row2.add(italicCheckbox)
        row2.add(underlineCheckbox)
        row2.add(button("Apply Attr") { applyAttributes() })

        panel.add(row1)
        panel.add(row2)
        return panel
    }

    private fun buildContentSplit(): JSplitPane {
        val screenPanel = JPanel(BorderLayout())
        screenPanel.border = BorderFactory.createTitledBorder("Screen Content")
        screenPanel.add(JScrollPane(screenArea), BorderLayout.CENTER)

        val fullPanel = JPanel(BorderLayout())
        fullPanel.border = BorderFactory.createTitledBorder("Full Content (Scrollback + Screen)")
        fullPanel.add(JScrollPane(fullArea), BorderLayout.CENTER)

        val logPanel = JPanel(BorderLayout())
        logPanel.border = BorderFactory.createTitledBorder("Event Log")
        logPanel.add(JScrollPane(logArea), BorderLayout.CENTER)

        val top = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, screenPanel, fullPanel)
        top.resizeWeight = 0.5

        val root = JSplitPane(JSplitPane.VERTICAL_SPLIT, top, logPanel)
        root.resizeWeight = 0.65
        return root
    }

    private fun buildStatusPanel(): JPanel {
        val panel = JPanel(BorderLayout())
        panel.border = BorderFactory.createEmptyBorder(6, 10, 6, 10)
        panel.add(cursorLabel, BorderLayout.WEST)
        panel.add(JLabel("Tip: use Write/Insert and arrows; compare Screen vs Full to monitor scrollback."), BorderLayout.EAST)
        return panel
    }

    private fun setupTextAreas() {
        val mono = Font(Font.MONOSPACED, Font.PLAIN, 15)
        listOf(screenArea, fullArea, logArea).forEach {
            it.font = mono
            it.isEditable = false
        }
    }

    private fun applyAttributes() {
        perform("applyAttributes") {
            selectedFg = fgCombo.selectedItem as Color
            selectedBg = bgCombo.selectedItem as Color

            buffer.setForeground(selectedFg)
            buffer.setBackground(selectedBg)
            buffer.resetStyle()

            val styles = mutableListOf<Style>()
            if (boldCheckbox.isSelected) styles += Style.BOLD
            if (italicCheckbox.isSelected) styles += Style.ITALIC
            if (underlineCheckbox.isSelected) styles += Style.UNDERLINE
            if (styles.isNotEmpty()) {
                buffer.setStyle(*styles.toTypedArray())
            }
        }
    }

    private fun perform(action: String, block: () -> Unit) {
        try {
            block()
            refreshView(action)
        } catch (e: Exception) {
            appendLog("$action failed: ${e.message}")
            refreshView("$action (error)")
        }
    }

    private fun refreshView(action: String) {
        screenArea.text = buffer.getScreenContent()
        fullArea.text = buffer.getFullContent()

        val cursor = buffer.getCursor()
        cursorLabel.text = "Cursor: (${cursor.column}, ${cursor.row}) | FG=$selectedFg BG=$selectedBg"

        appendLog("$action | cursor=(${cursor.column}, ${cursor.row})")
    }

    private fun appendLog(message: String) {
        val timestamp = LocalTime.now().withNano(0)
        logArea.append("[$timestamp] $message\n")
        logArea.caretPosition = logArea.document.length
    }

    private fun button(label: String, onClick: () -> Unit): JButton = JButton(label).apply {
        addActionListener { onClick() }
    }
}
