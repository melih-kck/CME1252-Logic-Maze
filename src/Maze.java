import java.util.Scanner;
import java.io.File;

public class Maze {
    private char[][] grid;

    public Maze() throws Exception {
        grid = new char[GameConstants.MAZE_ROWS][GameConstants.MAZE_COLS];
        loadMazeFromFile("maze.txt");
    }

    private void loadMazeFromFile(String fileName) throws Exception {
        File file = new File(fileName);
        if (!file.exists()) { return; }

        Scanner sc = new Scanner(file);
        int r = 0;
        while (sc.hasNextLine() && r < GameConstants.MAZE_ROWS) {
            String line = sc.nextLine();
            for (int c = 0; c < line.length() && c < GameConstants.MAZE_COLS; c = c + 1) {
                grid[r][c] = line.charAt(c);
            }
            r = r + 1;
        }
        sc.close();
    }

    public boolean isWall(int col, int row) {
        if (row < 0 || row >= GameConstants.MAZE_ROWS || col < 0 || col >= GameConstants.MAZE_COLS) {
            return true;
        }
        return grid[row][col] == '#';
    }

    public char[][] getGrid() {
        return grid;
    }
}