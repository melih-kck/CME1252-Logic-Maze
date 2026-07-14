import java.util.Scanner;
import java.io.File;

public final class Maze
{
    public char[][] grid;

    public Maze() throws Exception
    {
        grid = new char[GameLoop.MAZE_ROWS][GameLoop.MAZE_COLS];
        loadMazeFromFile("maze.txt");
    }

    public void loadMazeFromFile(String fileName) throws Exception
    {
        File file = findFile(fileName);
        if (!file.exists())
        {
            throw new Exception(fileName + " not found.");
        }

        Scanner sc = new Scanner(file);
        int r = 0;
        while (sc.hasNextLine())
        {
            if (r >= GameLoop.MAZE_ROWS)
            {
                sc.close();
                throw new Exception("Maze must have 21 rows.");
            }
            String line = sc.nextLine();
            if (line.length() != GameLoop.MAZE_COLS)
            {
                sc.close();
                throw new Exception("Maze rows must have 45 columns.");
            }
            for (int c = 0; c < line.length(); c = c + 1)
            {
                grid[r][c] = line.charAt(c);
            }
            r = r + 1;
        }
        sc.close();
        if (r != GameLoop.MAZE_ROWS)
        {
            throw new Exception("Maze must have 21 rows.");
        }
    }

    public static File findFile(String fileName)
    {
        File file = new File(fileName);
        if (file.exists())
        {
            return file;
        }

        try
        {
            File codeFile = new File(Maze.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            File folder = codeFile.isDirectory() ? codeFile : codeFile.getParentFile();
            File nearbyFile = new File(folder, fileName);
            if (nearbyFile.exists())
            {
                return nearbyFile;
            }

            File parentFolder = folder.getParentFile();
            if (parentFolder != null)
            {
                nearbyFile = new File(parentFolder, fileName);
                if (nearbyFile.exists())
                {
                    return nearbyFile;
                }
            }
        }
        catch (Exception ignored)
        {
        }

        return file;
    }

    public boolean isWall(int col, int row)
    {
        if (row < 0 || row >= GameLoop.MAZE_ROWS || col < 0 || col >= GameLoop.MAZE_COLS)
        {
            return true;
        }
        return grid[row][col] == '#';
    }

    public char[][] getGrid()
    {
        return grid;
    }

    public char[][] setGrid(int x , int y , int r)
    {
        grid[x][y] = (char) r ;

        return grid ;
    }
}
