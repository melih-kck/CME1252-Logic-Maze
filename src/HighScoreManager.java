import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class HighScoreManager
{
    private static final String FILE_NAME = "highscore.txt";
    private final DoublyLinkedList scores;

    public HighScoreManager()
    {
        scores = new DoublyLinkedList();
        loadScores();
    }

    private void loadScores()
    {
        File file = Maze.findFile(FILE_NAME);
        if (!file.exists())
        {
            loadDefaultScores();
            saveScores();
            return;
        }

        try (Scanner scanner = new Scanner(file))
        {
            while (scanner.hasNextLine())
            {
                String line = scanner.nextLine().trim();
                if (line.isEmpty())
                {
                    continue;
                }

                int lastSpace = line.lastIndexOf(' ');
                if (lastSpace != -1)
                {
                    String name = line.substring(0, lastSpace).trim();
                    try
                    {
                        int score = Integer.parseInt(line.substring(lastSpace + 1).trim());
                        scores.insertSorted(new HighScoreEntry(name, score));
                    }
                    catch (NumberFormatException ignored)
                    {
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void loadDefaultScores()
    {
        scores.insertSorted(new HighScoreEntry("Tarkan Bulut", 728));
        scores.insertSorted(new HighScoreEntry("Irmak Yol", 412));
        scores.insertSorted(new HighScoreEntry("Deniz Toprak", 190));
        scores.insertSorted(new HighScoreEntry("Ali Deniz", 56));
    }

    public void addScore(String name, int score)
    {
        scores.insertSorted(new HighScoreEntry(name, score));
        saveScores();
    }

    private void saveScores()
    {
        try (PrintWriter writer = new PrintWriter(new FileWriter(Maze.findFile(FILE_NAME))))
        {
            HighScoreEntry[] allScores = scores.toArray();
            for (HighScoreEntry entry : allScores)
            {
                writer.println(entry.getName() + " " + entry.getScore());
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public HighScoreEntry[] getTopScores(int limit)
    {
        HighScoreEntry[] allScores = scores.toArray();
        int count = Math.min(limit, allScores.length);
        HighScoreEntry[] topScores = new HighScoreEntry[count];
        for (int scoreIndex = 0; scoreIndex < count; scoreIndex++)
        {
            topScores[scoreIndex] = allScores[scoreIndex];
        }
        return topScores;
    }
}
