package club.chachy.lorem.gui.panels

import club.chachy.lorem.gui.color.ColorScheme
import club.chachy.lorem.gui.panels.buttons.PlayButton
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.JTextArea

class MainPanel(scheme: ColorScheme, version: String) : JPanel(BorderLayout()) {
    init {
        background = scheme.background
        add(PlayButton(scheme), BorderLayout.PAGE_END)
    }
}