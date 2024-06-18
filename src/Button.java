import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

//Custom styled button class
public class Button extends JButton {
    public Button(String text) {
        super(text);
        setForeground(Color.BLACK);
        setBackground(Color.WHITE);
        Border line = new LineBorder(Color.BLACK);
        Border margin = new EmptyBorder(5, 15, 5, 15);
        Border compound = new CompoundBorder(line, margin);
        setBorder(compound);
        setFocusPainted(false);
        getInputMap().put(KeyStroke.getKeyStroke("SPACE"), "none");
    }
}
