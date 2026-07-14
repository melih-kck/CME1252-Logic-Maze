public class HighScoreEntry
{
    private final String name;
    private final int score;

    public HighScoreEntry(String name, int score)
    {
        this.name = name;
        this.score = score;
    }

    public String getName()
    {
        return name;
    }

    public int getScore()
    {
        return score;
    }
}
