//Simple 2 attribute class for storing score entries
public class ScoreEntry {
    private final String name;
    private int score;
    public ScoreEntry(String name, int score) {
        //Store the name and score
        this.name = name;
        this.score = score;
    }

    //Return the name for the score entry
    public String getName() {
        return name;
    }

    //Return teh score for the score entry
    public int getScore() {
        return score;
    }

    //Set the score for the score entry
    public void setScore(int score) {
        this.score = score;
    }
}
