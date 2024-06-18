import java.awt.*;

//Class for representing cells in game state
public class Cell {
    private boolean isFilled; //Stores if the cell is filled
    private Color color; //Stores the color of the block filling the cell

    //Constructor
    public Cell(Boolean isFilled, Color color){
        //Store initial values
        this.isFilled = isFilled;
        this.color = color;
    }

    //Check if the cell is filled
    public boolean isFilled(){
        return isFilled;
    }

    //Set the fill state of the cell
    public void setFilled(boolean isFilled) {
        this.isFilled = isFilled;
    }

    //Check the color of the cell
    public Color getColor(){
        return color;
    }

    //Set the color of the cell
    public void setColor(Color color){
        this.color = color;
    }
}
