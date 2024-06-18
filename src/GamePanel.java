import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GamePanel extends JPanel {
    //Game board size variables
    private final int _cellSize;
    private final byte _cellColumns;
    private final byte _cellRows;

    //Window dimensions
    int frameWidth;
    int frameHeight;

    Cell[][] gameState; //Local Game state for rendering

    //Labels for different text
    JLabel scoreLabel;
    JLabel levelLabel;
    JLabel gameOverLabel;
    JLabel creatorLabel;
    List<JLabel> leaderBoardLabels = new ArrayList<>();
    JLabel leaderBoardText;

    //Buttons
    Button startButton;
    Button howToPlayButton;

    //Background Image
    Image Background;


    public GamePanel(JFrame frame, byte cellRows, byte cellColumns, Cell[][] INITgameState){
        //Load the background image
        try {
            Background = ImageIO.read(new File("src\\tetrisBackground.jpg"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //Size the board
        setPreferredSize(frame.getSize());
        setBackground(Color.BLACK);
        setLayout(null);

        //Store game board dimensions
        _cellRows = cellRows;
        _cellColumns = cellColumns;

        //Calculate frame dimensions
        frame.setVisible(true);
        frameHeight = frame.getHeight() - frame.getInsets().top - frame.getInsets().bottom;
        frameWidth = frame.getWidth() - frame.getInsets().left - frame.getInsets().right;
        _cellSize = frameHeight / _cellRows; //Calculate cell size

        gameState = INITgameState; //Store game state

        //Add start button
        startButton = new Button("Start New Game");
        this.add(startButton);
        startButton.setBounds(35, 20, 150, 30);

        //Add how to play button
        howToPlayButton = new Button("How to Play");
        this.add(howToPlayButton);
        howToPlayButton.setBounds(185, 20, 150, 30);

        //Add score label
        scoreLabel = new JLabel(String.format("<html><font style='color: white; font-size: %dpx'>Score: %d</font></html>",20 ,0));
        this.add(scoreLabel);
        scoreLabel.setBounds(35, 55, 200, 30);

        //Add level label
        levelLabel = new JLabel(String.format("<html><font style='color: white; font-size: %dpx'>Level: %d</font></html>",20 ,0));
        this.add(levelLabel);
        levelLabel.setBounds(35, 90, 200, 30);

        //Add game over label
        gameOverLabel = new JLabel(String.format("<html><font style='color: red; font-size: %dpx;'>Game Over! <br><font style='color: white; font-size: %dpx'>Your Final Score Was: %d</font></br></font></html>", 35, 15, 0));
        this.add(gameOverLabel);
        gameOverLabel.setBounds(35, frameHeight - 115, 350, 80);

        //Add creator label
        creatorLabel = new JLabel(String.format("<html><font style='color: white; font-size: %dpx;'>By Connor & Storm</font></html>", 15));
        this.add(creatorLabel);
        creatorLabel.setBounds(frameWidth - 205, frameHeight - 45, 200, 30);

        //Add leaderboard title lable
        leaderBoardText = new JLabel(String.format("<html><font style='color: white; font-size: %dpx;'>Leader Board</font></html>", 25));
        this.add(leaderBoardText);
        leaderBoardText.setBounds(frameWidth - 315, 20, 275, 30);

        //Set labels to hidden on init
        levelLabel.setVisible(false);
        scoreLabel.setVisible(false);
        gameOverLabel.setVisible(false);

        //Create action listener for start button
        startButton.addActionListener(_ -> {
            //Adjust label visibility for new game
            levelLabel.setVisible(true);
            scoreLabel.setVisible(true);
            gameOverLabel.setVisible(false);

            //Start main game thread
            try {
                Main.start();
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }

            //Set new text for start button
            startButton.setText("Reset");

            //Set input focus back to main window
            frame.requestFocusInWindow();
        });

        //Create how to play button action listener
        howToPlayButton.addActionListener(_ -> {
            //Set button backgrounds to white
            UIManager.put("Button.background", Color.white);

            //Display message dialog for how to paly instructions
            JOptionPane.showMessageDialog(frame, "Upon pressing start game blocks called \"Tetrominoes\" will begin falling from the top of the board you can use the right or left " +
                    "\narrow keys to move the piece side to side you can press space to instantly drop the piece to the bottom of the board," +
                    "\nonce the pieces touch the board you will no longer be able to move them and a new piece will generate at the top" +
                    "\nyour goal is to fill up rows to gain points, when a row is filled it will clear. " +
                    "\nTry not to let the pieces pile up to the top or you will lose", "How To Play", JOptionPane.INFORMATION_MESSAGE);
        });

        //Draw the initial leader board
        drawLeaderBoard(Main.readLeaderBoard());
    }

    //Function to render game over text when game ends
    public void gameOver(int score){
        gameOverLabel.setText(String.format("<html><font style='color: red; font-size: %dpx;'>Game Over! <br><font style='color: white; font-size: %dpx'>Your Final Score Was: %d</font></br></font></html>", 25, 15, score));
        gameOverLabel.setVisible(true);
    }

    //Function to call an update to render the next frame
    public void update(Cell[][] updatedGameState, int score, int level){
        gameState = updatedGameState; //Update the game state
        scoreLabel.setText(String.format("<html><font style='color: white; font-size: %dpx'>Score: %d</font></html>",20 ,score)); //Update the score
        levelLabel.setText(String.format("<html><font style='color: white; font-size: %dpx'>Level: %d</font></html>",20 ,level)); //Update the level
        repaint(); //Call the Override paint component method to render new frame updates
    }

    //Function to draw the leader board
    public void drawLeaderBoard(List<ScoreEntry> scores){
        //Remove any old leader board renderings to prevent double rendering
        for(JLabel label : leaderBoardLabels){
            this.remove(label);
        }
        leaderBoardLabels.clear();

        //For loop to add each leader board score
        for(int i = 0; i < scores.size(); i++){
            //Create labels for leader board text
            JLabel placement = new JLabel(String.format("<html><font style='color: white; font-size: %dpx'>%d </font></html>", 20, i + 1));
            JLabel leaderBoardText = new JLabel(String.format("<html><font style='color: white; font-size: %dpx'>%s: </font><font style='color: red; font-size: %dpx'>%d</font></html>", 20, scores.get(i).getName(), 20, scores.get(i).getScore()));

            //Add leader board text and set position
            this.add(leaderBoardText);
            leaderBoardText.setBounds(frameWidth - 275, 60 + (i * 30), 275, 35);

            this.add(placement);
            placement.setBounds(frameWidth - 275 - 40, 60 + (i* 30), 275, 35);

            //Store reference of labels for destruction on future calls
            leaderBoardLabels.add(placement);
            leaderBoardLabels.add(leaderBoardText);
        }
    }

    //Function to draw the game board
    private void drawBoard(Graphics g){
        //Nested for loop to iterate through each row and column
        for(int rows = 0; rows < _cellRows; rows++){
            for(int cols = 0; cols < _cellColumns; cols++){
                //If the game state cell is filled draw that block
                if (gameState[rows][cols].isFilled()) {
                    g.setColor(gameState[rows][cols].getColor());
                    g.fillRect((cols * _cellSize) + ((frameWidth /2) - ((_cellColumns * _cellSize)/2)), rows * _cellSize, _cellSize, _cellSize);

                }
                //Drawing the grid for the board
                g.setColor(Color.WHITE);
                g.drawRect((cols * _cellSize) + ((frameWidth /2) - ((_cellColumns * _cellSize)/2)), rows * _cellSize, _cellSize, _cellSize);
            }
        }
    }

    @Override
    //Override paintComponent of JPanel class
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); //Call parent to draw non-custom graphics
        g.drawImage(Background, 0, 0, 1280, 720, null); // draws background image
        drawBoard(g); //Calls to draw board
    }
}