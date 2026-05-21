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
        File file = new File(fileName);
        if (!file.exists()) { return; }

        Scanner sc = new Scanner(file);
        int r = 0;
        while (sc.hasNextLine() && r < GameLoop.MAZE_ROWS)
        {
            String line = sc.nextLine();
            for (int c = 0; c < line.length() && c < GameLoop.MAZE_COLS; c = c + 1)
            {
                grid[r][c] = line.charAt(c);
            }
            r = r + 1;
        }
        sc.close();
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
