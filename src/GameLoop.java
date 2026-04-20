import enigma.console.Console;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;



public class GameLoop {
    private int tick = 0;
    private boolean running = true;
    private Maze maze;
    private Console console;
    private int lastInput = Direction.NONE;
    private long startTime;

    public GameLoop(Console console , Maze maze ) throws Exception {
        this.maze = maze ;
        this.console = console;
        this.startTime = System.currentTimeMillis();

        // Asynchronous key listener
        this.console.getTextWindow().addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_W) { lastInput = Direction.UP; }
                if (e.getKeyCode() == KeyEvent.VK_S) { lastInput = Direction.DOWN; }
                if (e.getKeyCode() == KeyEvent.VK_A) { lastInput = Direction.LEFT; }
                if (e.getKeyCode() == KeyEvent.VK_D) { lastInput = Direction.RIGHT; }
            }
            public void keyReleased(KeyEvent e) { lastInput = Direction.NONE; }
            public void keyTyped(KeyEvent e) {}
        });
    }

    public void start(Player player) {
        while (running) {
            update(player);
            draw(player);
            sleep(GameConstants.TIME_UNIT_MS);
        }
    }

    private void update(Player player) {
        tick = tick + 1;
        player.tickCooldown();

        if (player.canMove() && lastInput != Direction.NONE) {
            int dx = 0; int dy = 0;
            if (lastInput == Direction.UP)    { dy = -1; }
            if (lastInput == Direction.DOWN)  { dy =  1; }
            if (lastInput == Direction.LEFT)  { dx = -1; }
            if (lastInput == Direction.RIGHT) { dx =  1; }

            // Wall collision check
            if (maze.isWall(player.getCol() + dx, player.getRow() + dy) == false) {
                player.performMove(lastInput);
            }
        }
        if (tick > 100000) { tick = 0; }
    }

    private void draw(Player player) {
        char[][] grid = maze.getGrid();

        // Draw maze and player
        for (int r = 0; r < GameConstants.MAZE_ROWS; r = r + 1) {
            console.getTextWindow().setCursorPosition(0, r);
            for (int c = 0; c < GameConstants.MAZE_COLS; c = c + 1) {
                if (r == player.getRow() && c == player.getCol()) {
                    console.getTextWindow().output('P');
                } else if (grid[r][c] == 'A' ||grid[r][c] == 'B' ||grid[r][c] == 'C' ||grid[r][c] == 'D' ||
                        grid[r][c] == 'a' ||grid[r][c] == 'b' ||grid[r][c] == 'c' ||grid[r][c] == 'd' ||
                        grid[r][c] == '˜' ||grid[r][c] == '^' ||grid[r][c] == 'v' ||grid[r][c] == '+' ||
                        grid[r][c] == '>' ||grid[r][c] == '=' ||grid[r][c] == '@' ||grid[r][c] == 'X' ){
                    console.getTextWindow().output(grid[r][c]);
                }
                else{
                    console.getTextWindow().output(grid[r][c]);
                }
            }
        }

        // Draw only the Timer
        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        console.getTextWindow().setCursorPosition(50, 0);
        console.getTextWindow().output("TIME: " + elapsed + "s");
    }

    private void sleep(int ms) {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < ms) {
            // Busy wait to avoid try-catch
        }
    }
}
