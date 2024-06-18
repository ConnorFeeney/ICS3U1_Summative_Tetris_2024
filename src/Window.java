import javax.swing.*;

//Class for creating main window
public class Window extends JFrame{
    public Window(String title, int width, int height) {
        super(title); //Create th window with parent constructor
        setSize(width, height); //Set window size
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //Set close operation
        setLocationRelativeTo(null); //Set origin location to center
        setResizable(false); //Force static size
    }
}
