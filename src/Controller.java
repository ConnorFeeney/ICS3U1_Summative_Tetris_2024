import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Controller implements KeyListener {
    private KeyEvent Key;

    //Constructor
    public Controller(JFrame frame) {
        //Mount a key listener to the window to recorde user inputs
        frame.addKeyListener(this);
    }

    //REQUIRED OVERRIDE FUNCTIONS CANNOT REMOVE
    @Override
    public void keyTyped(KeyEvent e) {}
    @Override
    public void keyReleased(KeyEvent e) {}

    //Overried method to see if a key is pressed
    @Override
    public void keyPressed(KeyEvent e) {
        //Record the key input
        Key = e;
    }

    //Return user key input
    public KeyEvent getInput() {
        return Key;
    }

    //Clear the users input
    public void clearInput(){
        Key = null;
    }
}