package club.chachy.lorem.gui.panels.buttons

import club.chachy.lorem.Launcher
import club.chachy.lorem.gui.color.ColorScheme
import kotlinx.coroutines.runBlocking
import java.awt.Component
import java.awt.Graphics
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.SwingUtilities

class PlayButton(private val scheme: ColorScheme) : JButton("Play") {
    init {
        background = scheme.accentColor
        foreground = scheme.foreground
        font = scheme.font
        isFocusPainted = false
        isContentAreaFilled = false
        isBorderPainted = false

        addMouseListener(LaunchMouseListener())
    }

    override fun paintComponent(g: Graphics) {
        when {
            getModel().isPressed -> {
                g.color = scheme.accentColor.darker()
            }
            else -> {
                g.color = background
            }
        }
        g.fillRect(0, 0, width, height)
        super.paintComponent(g)
    }

    private class LaunchMouseListener : MouseListener {
        override fun mouseClicked(e: MouseEvent?) {
            if (e != null) {
                val frame = SwingUtilities.getRoot(e.source as Component) as JFrame
                frame.isVisible = false
                val closeHandler = { frame.isVisible = true }
                runBlocking {
                    Launcher {
                        version = "1.13.2"
                        closeHandlers = listOf(closeHandler)
                    }.begin()
                }
            }
        }
        override fun mousePressed(e: MouseEvent?) {}
        override fun mouseReleased(e: MouseEvent?) {}
        override fun mouseEntered(e: MouseEvent?) {}
        override fun mouseExited(e: MouseEvent?) {}
    }
}