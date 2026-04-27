import enigma.console.Console;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;



public class GameLoop {
    private static final int ROBOT_MAX_HP = 2;
    private static final int ADJACENT_ROBOT_DAMAGE = 5;

    private int tick = 0;
    private boolean running = true;
    private Maze maze;
    private Console console;
    private int lastInput = Direction.NONE;
    private int facingDirection = Direction.RIGHT;
    private boolean fireRequested = false;
    private long startTime;
    private Random random = new Random();
    private Fireball fireball = new Fireball();

    private int score = 0;
    private int life = 100;
    private int fireballCount = 0;
    private String storageMode = "Backpack";
    private int[][] robotHp = new int[GameConstants.MAZE_ROWS][GameConstants.MAZE_COLS];

    private char[] inputQueue = new char[10];

    private int activeScreen = 1; // 1: Maze, 2: Tree, 3: Table
    private Tree tree = new Tree();
    private Player player;

    public GameLoop(Console console , Maze maze ) throws Exception {
        this.maze = maze ;
        this.console = console;
        this.startTime = System.currentTimeMillis();

        initInputQueueAndPlaceFirstTen();
        initializeRobotHealthFromGrid();

        // Asynchronous key listener
        this.console.getTextWindow().addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {

                if (e.getKeyCode() == KeyEvent.VK_1) { activeScreen = 1; }
                if (e.getKeyCode() == KeyEvent.VK_2) { activeScreen = 2; }
                if (e.getKeyCode() == KeyEvent.VK_3) { activeScreen = 3; }


                if (activeScreen == 1) {
                    if (e.getKeyCode() == KeyEvent.VK_W) { lastInput = Direction.UP; facingDirection = Direction.UP; }
                    if (e.getKeyCode() == KeyEvent.VK_S) { lastInput = Direction.DOWN; facingDirection = Direction.DOWN; }
                    if (e.getKeyCode() == KeyEvent.VK_A) { lastInput = Direction.LEFT; facingDirection = Direction.LEFT; }
                    if (e.getKeyCode() == KeyEvent.VK_D) { lastInput = Direction.RIGHT; facingDirection = Direction.RIGHT; }
                    if (e.getKeyCode() == KeyEvent.VK_SPACE) { fireRequested = true; }

                    if (e.getKeyCode() == KeyEvent.VK_M) {
                        if (storageMode.equals("Backpack")) {
                            storageMode = "Tree";
                        } else {
                            storageMode = "Backpack";
                        }
                    }
                }
                else if (activeScreen == 2) {
                    if (e.getKeyCode() == KeyEvent.VK_W) { tree.moveUp(); score--; }
                    if (e.getKeyCode() == KeyEvent.VK_A) { tree.moveLeft(); score--; }
                    if (e.getKeyCode() == KeyEvent.VK_D) { tree.moveRight(); score--; }

                    if (e.getKeyCode() == KeyEvent.VK_T) {
                        if (!player.isBackpackEmpty()) {
                            char item = player.popBackpack();
                            tree.placeSymbol(item);
                            tree.moveToNextEmpty();
                        }
                    }

                    if (e.getKeyCode() == KeyEvent.VK_R) {
                        char item = tree.getCursorSymbol();
                        if (item != ' ' && !player.isBackpackFull()) {
                            tree.removeSymbol();
                            player.pushBackpack(item);
                            score -= 2;
                        }
                    }

                    if (e.getKeyCode() == KeyEvent.VK_F) {

                        if (tree.checkSyntax()) {
                            int addedScore = 10 * tree.countTotalNodes(tree.getRoot());
                            score += addedScore;
                            activeScreen = 3;
                        } else {
                            score -= 10;

                        }
                    }
                }
            }


            public void keyReleased(KeyEvent e) { lastInput = Direction.NONE; }
            public void keyTyped(KeyEvent e) {}
        });
    }

    public void start(Player player) {

        this.player = player;

        while (running) {
            update(player);
            draw(player);
            sleep(GameConstants.TIME_UNIT_MS);
        }
    }

    private void update(Player player) {

        if (activeScreen != 1)
            return;

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

        collectItemAtPlayer(player);
        handleFire(player);
        applyAdjacentRobotDamage(player);
        updateInputQueue(player);

        if (life <= 0) {
            running = false;
        }

        if (tick > 100000) { tick = 0; }
    }

    private void clearWholeScreen() {
        for (int r = 0; r < 30; r++) {
            console.getTextWindow().setCursorPosition(0, r);
            console.getTextWindow().output("                                                                                                    ");

        }
    }

    private void draw(Player player) {

        console.getTextWindow().setCursorPosition(0, 0);

        if (activeScreen == 1) {
            drawMazeScreen(player);
        }
        else if (activeScreen == 2) {
            clearWholeScreen();
            tree.drawTree(console);
        }
        else if (activeScreen == 3) {

        }
    }

    private void drawMazeScreen(Player player) {
        char[][] grid = maze.getGrid();         // Draw maze and player
        for (int r = 0; r < GameConstants.MAZE_ROWS; r = r + 1) {
            console.getTextWindow().setCursorPosition(0, r);
            for (int c = 0; c < GameConstants.MAZE_COLS; c = c + 1) {
                if (r == player.getRow() && c == player.getCol()) {
                    console.getTextWindow().output('P');
                } else if (fireball.isActive() && r == fireball.getRow() && c == fireball.getCol()) {
                    console.getTextWindow().output('o');
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
        clearRightPanel();
        drawInputQueue();
        drawIndicators(elapsed);

        drawBackpack(player);
    }

    private void collectItemAtPlayer(Player player) {
        char[][] grid = maze.getGrid();
        int row = player.getRow();
        int col = player.getCol();
        char cell = grid[row][col];


        if (cell == '@') {
            fireball.addPacked();
            fireballCount = fireball.getPackedCount();
            grid[row][col] = ' ';
            return;
        }


        if (isCollectible(cell)) {
            if (storageMode.equals("Tree") || player.isBackpackFull()) {
                tree.placeSymbol(cell);
                tree.moveToNextEmpty();
            } else {
                player.pushBackpack(cell);
            }
            score += 5;
            grid[row][col] = ' ';
        }
    }

    private boolean isCollectible(char ch) {
        return ch == 'A' || ch == 'B' || ch == 'C' || ch == 'D'
                || ch == 'a' || ch == 'b' || ch == 'c' || ch == 'd'
                || ch == '~' || ch == '˜' || ch == '^' || ch == 'v'
                || ch == '+' || ch == '>' || ch == '=';
    }

    private void handleFire(Player player) {
        if (fireRequested) {
            fireball.fire(player.getCol(), player.getRow(), facingDirection);
            fireRequested = false;
        }

        int hitRobot = fireball.update(maze.getGrid());
        if (hitRobot > 0) {
            damageRobotAt(fireball.getCol(), fireball.getRow());
        }
        fireballCount = fireball.getPackedCount();
    }

    private void applyAdjacentRobotDamage(Player player) {
        int pRow = player.getRow();
        int pCol = player.getCol();
        char[][] grid = maze.getGrid();

        // Damage applies only for 4-neighbor adjacency (up, down, left, right).
        if (isRobotAt(grid, pRow - 1, pCol)
                || isRobotAt(grid, pRow + 1, pCol)
                || isRobotAt(grid, pRow, pCol - 1)
                || isRobotAt(grid, pRow, pCol + 1)) {
            life = life - ADJACENT_ROBOT_DAMAGE;
        }
    }

    private boolean isRobotAt(char[][] grid, int row, int col) {
        if (row < 0 || row >= GameConstants.MAZE_ROWS || col < 0 || col >= GameConstants.MAZE_COLS) {
            return false;
        }
        return grid[row][col] == 'X';
    }

    private void initializeRobotHealthFromGrid() {
        char[][] grid = maze.getGrid();
        for (int r = 0; r < GameConstants.MAZE_ROWS; r = r + 1) {
            for (int c = 0; c < GameConstants.MAZE_COLS; c = c + 1) {
                if (grid[r][c] == 'X') {
                    robotHp[r][c] = ROBOT_MAX_HP;
                }
            }
        }
    }

    private void damageRobotAt(int col, int row) {
        if (row < 0 || row >= GameConstants.MAZE_ROWS || col < 0 || col >= GameConstants.MAZE_COLS) {
            return;
        }

        char[][] grid = maze.getGrid();
        if (grid[row][col] != 'X') {
            return;
        }

        if (robotHp[row][col] <= 0) {
            robotHp[row][col] = ROBOT_MAX_HP;
        }

        robotHp[row][col] = robotHp[row][col] - 1;
        if (robotHp[row][col] <= 0) {
            grid[row][col] = ' ';
            robotHp[row][col] = 0;
            score = score + 50;
        }
    }

    private void drawBackpack(Player player) {
        int leftX = 57;
        int topY = 11;
        int capacity = player.getBackpackCapacity();

        // Draw vertical backpack body (open top, closed bottom) like project sample.
        for (int i = 0; i < capacity; i = i + 1) {
            console.getTextWindow().setCursorPosition(leftX, topY + i);
            console.getTextWindow().output("|   |");
        }

        console.getTextWindow().setCursorPosition(leftX, topY + capacity);
        console.getTextWindow().output("+---+");

        console.getTextWindow().setCursorPosition(leftX, topY + capacity + 1);
        console.getTextWindow().output("Backpack");

        // Stack view: newest item appears higher, oldest stays near the bottom.
        char[] items = player.getBackpackSnapshot();
        for (int i = 0; i < items.length; i = i + 1) {
            int row = topY + (capacity - 1 - i);
            console.getTextWindow().setCursorPosition(leftX + 2, row);
            console.getTextWindow().output(items[i]);
        }
    }

    private void clearRightPanel() {
        for (int r = 0; r < GameConstants.MAZE_ROWS; r = r + 1) {
            console.getTextWindow().setCursorPosition(46, r);
            console.getTextWindow().output("                              ");
        }
    }

    private void drawInputQueue() {
        int x = 50;
        int y = 0;

        console.getTextWindow().setCursorPosition(x, y);
        console.getTextWindow().output("Input");

        console.getTextWindow().setCursorPosition(x, y + 1);
        console.getTextWindow().output("<<<<<<<<<<");

        console.getTextWindow().setCursorPosition(x, y + 2);
        console.getTextWindow().output(queueToString());

        console.getTextWindow().setCursorPosition(x, y + 3);
        console.getTextWindow().output("<<<<<<<<<<");
    }

    private void drawIndicators(long elapsedSeconds) {
        int x = 50;
        int y = 5;

        console.getTextWindow().setCursorPosition(x, y);
        console.getTextWindow().output("Time     : " + elapsedSeconds + "   ");

        console.getTextWindow().setCursorPosition(x, y + 1);
        console.getTextWindow().output("Score    : " + score + "   ");

        console.getTextWindow().setCursorPosition(x, y + 2);
        console.getTextWindow().output("Fireball : " + fireballCount + "   ");

        console.getTextWindow().setCursorPosition(x, y + 3);
        console.getTextWindow().output("Life     : " + life + "   ");

        console.getTextWindow().setCursorPosition(x, y + 4);
        console.getTextWindow().output("Storage  : " + storageMode + "   ");
    }

    private void initInputQueueAndPlaceFirstTen() {
        for (int i = 0; i < inputQueue.length; i = i + 1) {
            inputQueue[i] = generateQueueElement();
        }

        // At game start, place first 10 queue elements into maze once.
        for (int i = 0; i < inputQueue.length; i = i + 1) {
            placeQueueElementToMaze(inputQueue[i], null);
        }
    }

    private void updateInputQueue(Player player) {
        // Every 20 ticks (2 seconds), insert first queue element into maze.
        if (tick % 20 != 0) {
            return;
        }

        char next = inputQueue[0];
        placeQueueElementToMaze(next, player);

        for (int i = 0; i < inputQueue.length - 1; i = i + 1) {
            inputQueue[i] = inputQueue[i + 1];
        }
        inputQueue[inputQueue.length - 1] = generateQueueElement();
    }

    private char generateQueueElement() {
        int roll = random.nextInt(10);

        // 7/10 logic symbols, 2/10 fireball, 1/10 robot.
        if (roll <= 6) {
            return randomLogicSymbol();
        }
        if (roll <= 8) {
            return '@';
        }
        return 'X';
    }

    private char randomLogicSymbol() {
        char[] logicSymbols = {
                'A', 'B', 'C', 'D',
                'a', 'b', 'c', 'd',
                '~', '^', 'v', '+', '>', '='
        };
        return logicSymbols[random.nextInt(logicSymbols.length)];
    }

    private void placeQueueElementToMaze(char element, Player player) {
        char[][] grid = maze.getGrid();

        for (int attempt = 0; attempt < 200; attempt = attempt + 1) {
            int row = random.nextInt(GameConstants.MAZE_ROWS - 2) + 1;
            int col = random.nextInt(GameConstants.MAZE_COLS - 2) + 1;

            if (maze.isWall(col, row)) {
                continue;
            }
            if (player != null && player.getRow() == row && player.getCol() == col) {
                continue;
            }
            if (grid[row][col] != ' ' && grid[row][col] != '\0') {
                continue;
            }

            grid[row][col] = element;
            if (element == 'X') {
                robotHp[row][col] = ROBOT_MAX_HP;
            }
            return;
        }
    }

    private String queueToString() {
        String text = "";
        for (int i = 0; i < inputQueue.length; i = i + 1) {
            text = text + inputQueue[i];
        }
        return text;
    }

    private void sleep(int ms) {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < ms) {
            // Busy wait to avoid try-catch
        }
    }
}