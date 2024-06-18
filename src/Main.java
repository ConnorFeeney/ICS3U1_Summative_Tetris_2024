import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.*;
import java.util.List;

public class Main implements Runnable{
    //Variables for size of the game board
    static final byte rows = 20;
    static final byte cols = 10;

    static boolean askName = true; //Boolean to decide if user should be asked for name
    static boolean gameOver = false; //Boolean to represent if the game is over
    static double gravity = 1; //Double to represent gravity in the for of cells/s 1 gravity means a fall speed of 1 Cell/s

    static Window gameWindow; //Object for game window
    static GamePanel render; //Object for rendering engine
    static Controller controller; //Object to take in user input

    static final long KEY_COOLDOWN = 50L; //The amount of time between the game registering key presses to avoid multiple registers in a fraction of a second
    static Map<Integer, Long> keyLastPressed = new HashMap<>(); //Stores the key presses and their cooldowns

    static Cell[][] gameState; //2d array of "Cells" used to store teh game state
    static Tetrominoe tetrominoe; //Object to store the active game piece

    static Main gameInstance; //Object for instance of the game to be used by th main thread
    static Thread gameThread; //Main game thread

    static double dropTimer = 0; //The amount of time since the last time the piece dropped automatically

    static double softTimerMS = 250; //The amount of time in ms that the piece stays in a "soft state" where it can still be moved after touching the ground
    static boolean startSoftTimer = false;

    static int score = 0; //Current user score
    static int level = 1; //Current user level

    //Private internal class to generate a custom input dialog for asking the users name
    private static class CustomInputDialog extends JDialog {
        String inputVal; //Stores the input value of the JDialog

        //Constructor
        CustomInputDialog(JFrame parent, String title, String message, int max) {
            super(parent, title, true); //Call the parent constructor to generate the dialog
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); //Disable exit button to force user to input name
            setLayout(new BorderLayout()); //set the layout

            //Create the message and text filed
            JLabel messageLabel = new JLabel(message);
            JTextField textInput = new JTextField(max);
            textInput.setBorder(new EmptyBorder(10, 10, 10, 10));

            //Construct a panel for the inputs
            JPanel inputPanel = new JPanel(new BorderLayout());
            inputPanel.add(messageLabel, BorderLayout.NORTH);
            inputPanel.add(textInput, BorderLayout.CENTER);

            //Build the ok button and add event listener
            Button okButton = new Button("OK");
            okButton.addActionListener(_ -> {
                inputVal = textInput.getText(); //Stores user text on click
                close(); //Close the dialog
            });

            //Packs dialog panel
            this.add(inputPanel, BorderLayout.CENTER);
            this.add(okButton, BorderLayout.SOUTH);
            this.pack();
            this.setLocationRelativeTo(parent);
        }

        //Close th window
        private void close(){
            this.dispose();
        }

        //Return user input
        public String getInput(){
            return inputVal;
        }
    }

    public static void main(String[] args) {
        //Start the game
        init(1280, 720);
    }

    @Override
    //Run for main thread
    public void run() {
        //Reset drop timer soft timer and askName
        dropTimer = 0;
        softTimerMS = 250;
        askName = true;

        long lastDeltaTime = System.currentTimeMillis();

        //Main Game Loop
        while(!gameOver) {
            //Only run if there is a piece in play
            if(tetrominoe != null) {
                //Calculate deltaTime since last frame
                double deltaTime = (System.currentTimeMillis() - lastDeltaTime);
                lastDeltaTime = System.currentTimeMillis();

                handleInput(); //Handle user input

                //Calculate current drop timer
                dropTimer += deltaTime / 1E3;

                //Attempt to drop piece
                dropTetrominoe(deltaTime);
            }
            //Render the new frame
            render.update(gameState, score, level);
        }
        gameThread = null; //Disable the thread after game over

        if(askName){
            //Ask the user for their name
            CustomInputDialog nameInput = new CustomInputDialog(gameWindow, "Name", "Input Name", 20);
            nameInput.setVisible(true);

            //Update leader board
            String userName = nameInput.getInput();
            if(userName.trim().isEmpty()){
                userName = "No Name";
            }
            writeLeaderBoard(score, userName);

            //Render updated leader board
            render.drawLeaderBoard(readLeaderBoard());
        }
    }

    //Init function to run the initial setup of the game
    private static void init(int width, int height) {
        //Build the main window for the game
        gameWindow = new Window("Tetris ICS301 Summative", width, height);

        //Create a controller object and mount it to the window to record user inputs
        controller = new Controller(gameWindow);

        //Generate the initial empty game state
        gameState = new Cell[rows][cols];
        emptyGameState();

        //Constructs the rendering panel and mounts it to the game window
        render = new GamePanel(gameWindow, rows, cols, gameState);
        gameWindow.add(render);
    }

    //Function to start the game
    public static void start() throws InterruptedException {
        //Empty the game state and generate a new game piece
        emptyGameState();
        generateTetrominoe();

        //Reset the game thread if a reset is called mid-game
        if(gameThread != null){
            gameOver = true;
            askName = false;
            gameThread.join();
        }

        //reset starting game values
        score = 0;
        level = 1;
        gameOver = false;

        //Generate and start a new game thread
        gameInstance = new Main();
        gameThread = new Thread(gameInstance);
        gameThread.start();
    }

    //Function to empty the game state
    private static void emptyGameState(){
        //iterates through game state to fill will empty cell objects
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                gameState[row][col] = new Cell(false, null);
            }
        }
    }

    //Function to handle user input
    private static void handleInput(){
        KeyEvent e = controller.getInput(); //Grabs user input
        if(e != null){
            long currentTime = System.currentTimeMillis();
            int keyCode = e.getKeyCode();
            long lastPressed = keyLastPressed.getOrDefault(keyCode, 0L);

            //Switch case to call different move functions if keypress is less than its cooldown
            if(currentTime - lastPressed >= KEY_COOLDOWN) {
                switch(keyCode){
                    case KeyEvent.VK_LEFT:
                        moveTetrominoe(-1); //Move the piece left
                        break;
                    case KeyEvent.VK_RIGHT:
                        moveTetrominoe(1); //Move the piece right
                        break;
                    case KeyEvent.VK_UP:
                        rotateTetrominoe(false); //Rotate the piece counter-clockwise
                        break;
                    case KeyEvent.VK_DOWN:
                        rotateTetrominoe(true); //Rotate the piece clockwise
                        break;
                    case KeyEvent.VK_SPACE:
                        forceDrop(); //Force piece to bottom of the game board
                        break;
                }
                controller.clearInput(); //Clear the last input
                keyLastPressed.put(keyCode, currentTime); //Store key cooldown
            }
        }
    }

    //Function to generate new game piece
    private static void generateTetrominoe() {
        //Generate the game piece
        Random rand = new Random();
        tetrominoe = new Tetrominoe(rand.nextInt(7));

        //Set the game piece to the center of the board on the x-axis
        tetrominoe.x = (cols / 2) - (tetrominoe.blocks.length / 2);

        //Attempts to place the game piece
        if(canPlaceTetrominoe(tetrominoe.blocks)){
            dropTimer = 0;
            placeTetrominoe(tetrominoe.blocks);
        }else{
            //If the game piece cannot be placed end game
            gameOver = true;
            render.gameOver(score);
        }
    }

    //Function to check if the user has filled any rows
    private static void checkTetris(){
        //Mutable array to store the list of full rows
        ArrayList<Integer> fullRows = getRows();

        if(!fullRows.isEmpty()) {
            double multiplier = fullRows.size() > 1 ? 2 + (fullRows.size() * 0.5) : 1; //Calculate score multiplier based on amount of rows cleared at once
            score += (int)(fullRows.size() * 100 * multiplier); //Calculate the new score
            level = (score / 1000) + 1; //Calculate the current level based on the score
            gravity = ((level - 1) * 0.5) + 1; //Calculate the current fall speed based on level

            int lowestRow = fullRows.getFirst();

            //Iterate through cleared rows to shift reset of game state down
            for (int row = lowestRow; row >= 0; row--) {
                //Grabs the target row to shift down
                Cell[] targetRow = row - fullRows.size() > 0 ? gameState[row - fullRows.size()] : gameState[0];

                //Clone the target row to empty row
                gameState[row] = targetRow.clone();

                //Clear target row
                for(int cell = 0; cell < targetRow.length; cell++){
                    targetRow[cell] = new Cell(false, null);
                }
            }
            //Empty full row arraylist
            fullRows.clear();
        }
    }

    private static ArrayList<Integer> getRows() {
        ArrayList<Integer> fullRows = new ArrayList<>();

        //Iterate through each row
        for(int row = rows - 1; row >= 0; row--){
            boolean rowFull = true;
            for(int col = 0; col < cols; col ++){
                //Check for empty cells
                if(!gameState[row][col].isFilled()){
                    //Row is not full if an empty cell is found break and move to next row
                    rowFull = false;
                    break;
                }
            }

            //Add the full rows to the arraylist
            if(rowFull){
                fullRows.add(row);
            }
        }
        return fullRows;
    }

    //Function to check if a piece can be placed
    private static boolean canPlaceTetrominoe(int[][] blocks) {
        //Iterates through the blocks in the game piece
        for(int[] block : blocks) {
            //Calculate the true position of the blocks
            int blockX = block[0] + tetrominoe.x;
            int blockY = block[1] + tetrominoe.y;

            //Checks if the block is in range of the game board
            if (blockX >= 0 && blockX < cols && blockY >= 0 && blockY < rows) {
                //Checks if the placement is already taken
                if (gameState[blockY][blockX].isFilled()) {
                    return false;
                }
            } else {
                //Return false if not in range
                return false;
            }
        }
        //Return true if playable
        return true;
    }

    //Function to place piece
    private static void placeTetrominoe(int[][] blocks) {
        //Iterate through blocks in piece
        for(int[] block : blocks) {
            //Calculate true x and y of block
            int blockX = block[0] + tetrominoe.x;
            int blockY = block[1] + tetrominoe.y;

            //Places block in game state
            if(blockY >= 0 && !gameState[blockY][blockX].isFilled()){
                gameState[blockY][blockX].setFilled(true);
                gameState[blockY][blockX].setColor(tetrominoe.color);
            }
        }
    }

    //Function to remove piece from game board
    private static void clearTetrominoe(int[][] blocks){
        //Iterate through blocks in game piece
        for(int[] block : blocks) {
            //Calculate true x and y of game piece
            int blockX = block[0] + tetrominoe.x;
            int blockY = block[1] + tetrominoe.y;

            //Removes the block form the game state
            if(blockY >= 0 && gameState[blockY][blockX].isFilled()){
                gameState[blockY][blockX].setFilled(false);
                gameState[blockY][blockX].setColor(null);
            }
        }
    }

    //Function to drop the piece down
    private static void dropTetrominoe(double deltaTime){
        //Calculates if enough time has passed for piece to drop based on gravity value
        if(dropTimer >= 1/gravity){
            dropTimer = 0;

            //Clears old piece placement
            clearTetrominoe(tetrominoe.blocks);
            tetrominoe.y += 1; //Shifts piece down

            //Places piece
            if(canPlaceTetrominoe(tetrominoe.blocks)){
                softTimerMS = 250;
                placeTetrominoe(tetrominoe.blocks);
            }else{
                //If the piece cannot be placed start the soft timer
                tetrominoe.y -= 1;
                placeTetrominoe(tetrominoe.blocks);
                startSoftTimer = true;
            }
        }


        if(startSoftTimer){
            softTimerMS -= deltaTime;

            //If the soft time has reached zero generate a new game piece
            if(softTimerMS <= 0){
                forceDrop();
                startSoftTimer = false;
                softTimerMS = 250;
                checkTetris();
                generateTetrominoe();
            }
        }
    }

    //Function to force drop the piece to the bottom of the board
    private static void forceDrop(){
        //Clear old placement
        clearTetrominoe(tetrominoe.blocks);
        int originY = tetrominoe.y;

        //Brute force loop to find and place block at lowest position
        for(int i = originY; i <= rows; i++){
            tetrominoe.y = i;
            if(!canPlaceTetrominoe(tetrominoe.blocks)){
                tetrominoe.y -= 1;
                placeTetrominoe(tetrominoe.blocks);
                return;
            }
        }

        //Base case if piece is already at lowest position
        tetrominoe.y = originY;
        placeTetrominoe(tetrominoe.blocks);
    }

    //Function to move the tetrominoe left or right
    private static void moveTetrominoe(int dx){
        //Clear the old piece placement
        clearTetrominoe(tetrominoe.blocks);
        tetrominoe.x += dx; //Shift the tetrominoe by the delta x amount

        //Attempt to place the new piece position
        if(canPlaceTetrominoe(tetrominoe.blocks)){
            softTimerMS = 250;
            placeTetrominoe(tetrominoe.blocks);
        }else{
            //If the piece cannot be placed revert position
            tetrominoe.x -= dx;
            placeTetrominoe(tetrominoe.blocks);
        }
    }

    //Function to rotate piece
    private static void rotateTetrominoe(Boolean clockWise){
        //Clear old placement
        clearTetrominoe(tetrominoe.blocks);
        tetrominoe.rotate(clockWise); //Apply rotation

        //Attempt to place piece
        if(canPlaceTetrominoe(tetrominoe.blocks)){
            softTimerMS = 250;
            placeTetrominoe(tetrominoe.blocks);
        }else{
            //If piece cannot be placed revert rotation
            tetrominoe.rotate(!clockWise);
            placeTetrominoe(tetrominoe.blocks);
        }
    }

    //Function to grab current leader board
    public static List<ScoreEntry> readLeaderBoard(){
        List<ScoreEntry> scores = new ArrayList<>();
        String FILENAME = "LeaderBoard.txt";

        //Check fo leader board file, return null if no file fount
        if(!new File(FILENAME).exists()){
            return null;
        }

        //Create a buffered fill reader
        try(BufferedReader in = new BufferedReader(new FileReader(FILENAME))){
            String line;
            //Loop through each lin in leader board file
            while((line = in.readLine()) != null){
                //Parse string into parts at ":"
                String[] tokens = line.trim().split(":");

                //Store name and score
                String name = tokens[0].trim();
                int score = Integer.parseInt(tokens[1].trim());

                //Recode score entry
                scores.add(new ScoreEntry(name, score));
            }
        }catch (IOException e){
            System.err.println("Error reading file: " + FILENAME + ": " + e.getMessage());
        }

        //Return the scores
        return scores;
    }

    //Function for adding new indexes to the leader board
    private static void writeLeaderBoard(int newScore, String name){
        String FILENAME = "LeaderBoard.txt";

        //Check if the leader board file exists
        boolean fileExists = false;
        if(!new File(FILENAME).exists()){
            try{
                //If the leader board file is not found generate one
                File lbFile = new File(FILENAME);
                if(lbFile.createNewFile()){
                    fileExists = true;
                }
            }catch (IOException e){
                System.err.println("Error generating file: " + FILENAME + ": " + e.getMessage());
            }
        }else{
            fileExists = true;
        }

        if(fileExists){
            //Grab existing scores
            List<ScoreEntry> scores = readLeaderBoard();
            boolean nameExists = false;
            if(scores != null){
                //Iterate through each score entry
                for(ScoreEntry score : scores){
                    //Check if user inputted name already exists
                    if(score.getName().equalsIgnoreCase(name) && !score.getName().equalsIgnoreCase("No Name")){
                        //If the name already exists update the score
                        nameExists = true;
                        if(newScore > score.getScore()){
                            score.setScore(newScore);
                        }
                        break;
                    }
                }
                //If the name does not exist create a new score entry
                if(!nameExists){
                    scores.add(new ScoreEntry(name, newScore));
                }
            }else{
                //If there are no preexisting scores create a score list and add the new entry
                scores = new ArrayList<>();
                scores.add(new ScoreEntry(name, newScore));
            }

            //Sort the scores by score in descending order
            scores.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));

            //If there are more then 10 score entries sublist it to remove the lowest score
            if(scores.size() > 10){
                scores = scores.subList(0, 10);
            }

            //Create a writer to write the score to the score file
            try(BufferedWriter out = new BufferedWriter(new FileWriter(FILENAME))){
                //Iterate through each score
                for(ScoreEntry score : scores){
                    //Write the new formated score list
                    out.write(score.getName() + ":" + score.getScore());
                    out.newLine();
                }
            }catch (IOException e){
                System.err.println("Error writing file: " + FILENAME + ": " + e.getMessage());
            }
        }
    }
}