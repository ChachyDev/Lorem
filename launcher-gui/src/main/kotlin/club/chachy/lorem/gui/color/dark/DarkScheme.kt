package club.chachy.lorem.gui.color.dark

import club.chachy.lorem.gui.color.ColorScheme
import java.awt.Color
import java.awt.Font

class DarkScheme : ColorScheme {
    override val background: Color = Color(27,27,27)
    override val accentColor: Color = Color(1, 165, 82).darker()
    override val foreground: Color = Color.WHITE.darker()
    override val font: Font = Font.createFont(Font.TRUETYPE_FONT, this::class.java.getResourceAsStream("/fonts/roboto.ttf")).deriveFont(16f)
}