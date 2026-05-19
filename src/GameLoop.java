import enigma.console.Console;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;

public class GameLoop
{
    private static final int ADJACENT_ROBOT_DAMAGE = 5;

    private int tick = 0;
    private boolean running = true;
    private boolean isGameOver = false;
    private Maze maze;
    public static final int MAZE_ROWS = 21;
    public static final int MAZE_COLS = 45;
    public static final int TIME_UNIT_MS = 100;
    public static final int PLAYER_MOVE_INTERVAL = 1;
    public static final int ROBOT_MOVE_INTERVAL = 4;

    public static final class Direction
    {
        public static final int NONE = 0;
        public static final int UP = 1;
        public static final int DOWN = 2;
        public static final int LEFT = 3;
        public static final int RIGHT = 4;
    }

    private Console console;
    private int lastInput = Direction.NONE;
    private int facingDirection = Direction.RIGHT;
    private boolean fireRequested = false;
    private int elapsedTicks = 0;
    private Random random = new Random();
    private Fireball fireball = new Fireball();

    private int score = 0;
    private int life = 100;
    private int fireballCount = 0;
    private String storageMode = "Backpack";
    private char[] inputQueue = new char[10];

    private int activeScreen = 1;
    private int missingSlotRow = -1;
    private boolean guessingTruthTable = false;
    private String tableFeedback = "";
    private Tree tree = new Tree();
    private Player player;
    private Robot[] robots = new Robot[100];
    private int robotCount = 0;

    private String expressionInfix = "";
    private String expressionPostfix = "";
    private String treeMessage = "";
    private boolean expressionReady = false;
    private HighScoreManager highScoreManager = new HighScoreManager();
    private boolean[] truthTableResults;
    private String expectedSimplified = "";

    public GameLoop(Console console , Maze maze ) throws Exception
    {
        this.maze = maze ;
        this.console = console;

        initializeRobotHealthFromGrid();
        initInputQueueAndPlaceFirstTen();

        this.console.getTextWindow().addKeyListener(new KeyListener()
        {
            public void keyPressed(KeyEvent e)
            {
                if (isGameOver)
                {
                    System.exit(0);
                }

                if (e.getKeyCode() == KeyEvent.VK_1) { activeScreen = 1; treeMessage = ""; }

                if (e.getKeyCode() == KeyEvent.VK_1) { activeScreen = 1; treeMessage = ""; }
                if (e.getKeyCode() == KeyEvent.VK_2) { activeScreen = 2; treeMessage = ""; lastInput = Direction.NONE; }
                if (e.getKeyCode() == KeyEvent.VK_3) { activeScreen = 3; treeMessage = ""; lastInput = Direction.NONE; }


                if (activeScreen == 1)
                {
                    if (e.getKeyCode() == KeyEvent.VK_W || e.getKeyCode() == KeyEvent.VK_UP)    { lastInput = Direction.UP;    facingDirection = Direction.UP; }
                    if (e.getKeyCode() == KeyEvent.VK_S || e.getKeyCode() == KeyEvent.VK_DOWN)  { lastInput = Direction.DOWN;  facingDirection = Direction.DOWN; }
                    if (e.getKeyCode() == KeyEvent.VK_A || e.getKeyCode() == KeyEvent.VK_LEFT)  { lastInput = Direction.LEFT;  facingDirection = Direction.LEFT; }
                    if (e.getKeyCode() == KeyEvent.VK_D || e.getKeyCode() == KeyEvent.VK_RIGHT) { lastInput = Direction.RIGHT; facingDirection = Direction.RIGHT; }
                    if (e.getKeyCode() == KeyEvent.VK_SPACE) { fireRequested = true; }

                    if (e.getKeyCode() == KeyEvent.VK_M)
                    {
                        if (storageMode.equals("Backpack"))
                        {
                            storageMode = "Tree";
                        }
                        else
                        {
                            storageMode = "Backpack";
                        }
                    }
                }
                else if (activeScreen == 2)
                {
                    if (e.getKeyCode() == KeyEvent.VK_W && tree.moveUp()) { score--; }
                    if (e.getKeyCode() == KeyEvent.VK_A && tree.moveLeft()) { score--; }
                    if (e.getKeyCode() == KeyEvent.VK_D && tree.moveRight()) { score--; }

                    if (e.getKeyCode() == KeyEvent.VK_T)
                    {
                        if (!player.isBackpackEmpty())
                        {
                            if (tree.getCursorSymbol() != ' ')
                            {
                                tree.moveToNextEmpty();
                            }

                            char item = player.peekBackpack();
                            if (tree.placeSymbol(item))
                            {
                                player.popBackpack();
                                tree.moveToNextEmpty();
                                treeMessage = "";
                                expressionReady = false;
                                expressionInfix = "";
                                expressionPostfix = "";
                            }
                            else
                            {
                                treeMessage = "ERROR: Tree is full.";
                            }
                        }
                    }

                    if (e.getKeyCode() == KeyEvent.VK_R)
                    {
                        char item = tree.getCursorSymbol();
                        if (item != ' ' && !player.isBackpackFull())
                        {
                            tree.removeSymbol();
                            player.pushBackpack(item);
                            score -= 2;
                            treeMessage = "";
                            expressionReady = false;
                            expressionInfix = "";
                            expressionPostfix = "";
                        }
                        else if (item == ' ')
                        {
                            treeMessage = "ERROR: No symbol at cursor.";
                        }
                        else
                        {
                            treeMessage = "ERROR: Backpack is full.";
                        }
                    }

                    if (e.getKeyCode() == KeyEvent.VK_F)
                    {

                        if (tree.checkSyntax())
                        {
                            if (!expressionReady)
                            {
                                int addedScore = 10 * tree.countTotalNodes(tree.getRoot());
                                score += addedScore;
                                expressionInfix = tree.getFullInfix();
                                expressionPostfix = tree.getFullPostfix();
                                truthTableResults = new Expression(tree).evaluateAllRows();
                                expectedSimplified = KMapSimplifier.simplify(truthTableResults);
                                treeMessage = "";
                                expressionReady = true;
                                activeScreen = 3;
                                missingSlotRow = random.nextInt(16);
                                guessingTruthTable = true;
                                tableFeedback = "";
                            }
                            else
                            {
                                score -= 10;
                                treeMessage = "ERROR: Invalid expression! (-10 pts)";
                                expressionReady = false;
                            }
                        }
                    }
                }
                else if (activeScreen == 3)
                {
                    if (guessingTruthTable)
                    {
                        if (e.getKeyChar() == '0' || e.getKeyChar() == '1')
                        {
                            boolean guess = (e.getKeyChar() == '1');
                            if (guess == truthTableResults[missingSlotRow])
                            {
                                score += 3;
                                tableFeedback = "Correct! (+3 pts).";
                            }
                            else
                            {
                                score -= 2;
                                tableFeedback = "Wrong! (-2 pts).";
                            }
                            guessingTruthTable = false;
                            treeMessage = "";
                        }
                    }
                    else
                    {
                        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE && treeMessage.length() > 0)
                        {
                            treeMessage = treeMessage.substring(0, treeMessage.length() - 1);
                        }
                        else if (e.getKeyCode() == KeyEvent.VK_ENTER)
                        {
                            boolean[] userResults = StringEvaluator.evaluate(treeMessage);
                            if (userResults != null && java.util.Arrays.equals(truthTableResults, userResults))
                            {
                                score += 10;
                            }
                            else
                            {
                                score -= 10;
                            }
                            treeMessage = "";
                            activeScreen = 4;
                            running = false;
                        }
                        else
                        {
                            char c = e.getKeyChar();
                            if (c >= 32 && c <= 126)
                            {
                                treeMessage += c;
                            }
                        }
                    }
                }
                else if (activeScreen == 4)
                {
                    if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE && treeMessage.length() > 0)
                    {
                        treeMessage = treeMessage.substring(0, treeMessage.length() - 1);
                    }
                    else if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    {
                        if (treeMessage.trim().isEmpty()) treeMessage = "Anonymous";
                        highScoreManager.addScore(treeMessage.trim(), score);
                        activeScreen = 5;
                    }
                    else
                    {
                        char c = e.getKeyChar();
                        if (c >= 32 && c <= 126)
                        {
                            treeMessage += c;
                        }
                    }
                }
                else if (activeScreen == 5)
                {
                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
                    {
                        System.exit(0);
                    }
                }
            }

            public void keyReleased(KeyEvent e) { lastInput = GameLoop.Direction.NONE; }
            public void keyTyped(KeyEvent e) {}
        });
    }

    public void start(Player player)
    {

        this.player = player;
        placePlayerRandomly(player);

        while (running)
        {
            update(player);
            draw(player);
            sleep(TIME_UNIT_MS);
        }

        drawGameOver();
    }

    private void drawGameOver()
    {
        if (activeScreen != 4 && activeScreen != 5)
        {
            activeScreen = 4;
            treeMessage = "";
        }

        while (true)
        {
            clearWholeScreen();
            if (activeScreen == 4)
            {
                console.getTextWindow().setCursorPosition(35, 10);
                console.getTextWindow().output("*** GAME OVER ***");
                console.getTextWindow().setCursorPosition(32, 12);
                console.getTextWindow().output("Final Score : " + score);
                console.getTextWindow().setCursorPosition(29, 14);
                console.getTextWindow().output("Enter Name: " + treeMessage + "_");
            }
            else if (activeScreen == 5)
            {
                console.getTextWindow().setCursorPosition(35, 5);
                console.getTextWindow().output("HIGH SCORES");
                HighScoreEntry[] tops = highScoreManager.getTopScores(10);
                for (int i = 0; i < tops.length; i++)
                {
                    console.getTextWindow().setCursorPosition(30, 7 + i);
                    console.getTextWindow().output((i + 1) + ". " + tops[i].getName() + " - " + tops[i].getScore());
                }
                console.getTextWindow().setCursorPosition(30, 7 + tops.length + 2);
                console.getTextWindow().output("Press ESC to exit...");
            }
            sleep(50);
        }
    }

    private void placePlayerRandomly(Player player)
    {
        char[][] grid = maze.getGrid();
        for (int attempt = 0; attempt < 500; attempt = attempt + 1)
        {
            int row = random.nextInt(MAZE_ROWS - 2) + 1;
            int col = random.nextInt(MAZE_COLS - 2) + 1;
            if (!maze.isWall(col, row) && grid[row][col] == ' ')
            {
                player.setPosition(col, row);
                return;
            }
        }
    }

    private void update(Player player)
    {

        if (activeScreen != 1)
            return;

        tick = tick + 1;
        elapsedTicks = elapsedTicks + 1;
        player.tickCooldown();

        if (tick % GameLoop.ROBOT_MOVE_INTERVAL == 0)
        {
            for (int i = 0; i < robotCount; i++)
            {
                if (robots[i] != null && robots[i].life > 0)
                {
                    robots[i].moveAndCollect(maze.getGrid(), player.getCol(), player.getRow(), robots, robotCount, i);
                    robots[i].collect(maze.getGrid());
                }
            }
            syncAllRobotsOnGrid();
        }

        if (player.canMove() && lastInput != Direction.NONE)
        {
            int dx = 0; int dy = 0;
            if (lastInput == Direction.UP)    { dy = -1; }
            if (lastInput == Direction.DOWN)  { dy =  1; }
            if (lastInput == Direction.LEFT)  { dx = -1; }
            if (lastInput == Direction.RIGHT) { dx =  1; }

            if (canPlayerEnter(player.getCol() + dx, player.getRow() + dy))
            {
                player.performMove(lastInput);
            }
        }

        collectItemAtPlayer(player);
        handleFire(player);
        applyAdjacentRobotDamage(player);
        updateInputQueue(player);

        if (life <= 0)
        {
            running = false;
        }

        if (tick > 100000) { tick = 0; }
    }



    private void clearWholeScreen()
    {
        for (int r = 0; r < 30; r++)
        {
            console.getTextWindow().setCursorPosition(0, r);
            console.getTextWindow().output("                                                                                                    ");

        }
    }

    private void draw(Player player)
    {

        console.getTextWindow().setCursorPosition(0, 0);

        if (activeScreen == 1)
        {
            drawMazeScreen(player);
        }
        else if (activeScreen == 2)
        {
            clearWholeScreen();
            tree.drawTree(console);
            long elapsed = getElapsedSeconds();
            clearRightPanel();
            drawInputQueue();
            drawIndicators(elapsed);
            drawBackpack(player);
            drawTreeExpression();
            if (!treeMessage.isEmpty())
            {
                console.getTextWindow().setCursorPosition(0, 23);
                console.getTextWindow().output(treeMessage);
            }
        }
        else if (activeScreen == 3)
        {
            clearWholeScreen();
            drawTableScreen();
        }
    }

    private void drawMazeScreen(Player player)
    {
        char[][] grid = maze.getGrid();
        for (int r = 0; r < MAZE_ROWS; r = r + 1)
        {
            console.getTextWindow().setCursorPosition(0, r);
            for (int c = 0; c < MAZE_COLS; c = c + 1)
            {
                if (r == player.getRow() && c == player.getCol())
                {
                    console.getTextWindow().output('P');
                }
                else if (fireball.isActive() && r == fireball.getRow() && c == fireball.getCol())
                {
                    console.getTextWindow().output('o');
                }
                else
                {
                    char drawCell = grid[r][c];
                    if (drawCell == '\0')
                    {
                        drawCell = ' ';
                    }
                    console.getTextWindow().output(drawCell);
                }
            }
        }

        long elapsed = getElapsedSeconds();
        clearRightPanel();
        drawInputQueue();
        drawIndicators(elapsed);
        drawBackpack(player);

        if (!treeMessage.isEmpty())
        {
            int msgX = getPanelX();
            console.getTextWindow().setCursorPosition(msgX, 10);
            console.getTextWindow().output(treeMessage + "          ");
        }
    }

    private void collectItemAtPlayer(Player player)
    {
        char[][] grid = maze.getGrid();
        int row = player.getRow();
        int col = player.getCol();
        char cell = grid[row][col];


        if (cell == '@')
        {
            fireball.addPacked();
            fireballCount = fireball.getPackedCount();
            grid[row][col] = ' ';
            return;
        }


        if (isCollectible(cell))
        {
            boolean stored = false;
            if (storageMode.equals("Tree") || player.isBackpackFull())
            {
                if (tree.getCursorSymbol() != ' ')
                {
                    tree.moveToNextEmpty();
                }
                stored = tree.placeSymbol(cell);
                if (stored)
                {
                    tree.moveToNextEmpty();
                }
            }

            if (!stored && !player.isBackpackFull())
            {
                stored = player.pushBackpack(cell);
            }

            if (stored)
            {
                score += 5;
                grid[row][col] = ' ';
                treeMessage = "";
            }
            else
            {
                treeMessage = "ERROR: Backpack and tree are full.";
            }
        }
    }
    private boolean isCollectible(char ch)
    {
        return ch == 'A' || ch == 'B' || ch == 'C' || ch == 'D'
                || ch == 'a' || ch == 'b' || ch == 'c' || ch == 'd'
                || ch == '~' || ch == '^' || ch == 'v'
                || ch == '+' || ch == '>' || ch == '=';
    }

    private boolean canPlayerEnter(int col, int row)
    {
        if (maze.isWall(col, row))
        {
            return false;
        }
        return maze.getGrid()[row][col] != 'X';
    }

    private void handleFire(Player player)
    {
        if (fireRequested)
        {
            fireball.fire(player.getCol(), player.getRow(), facingDirection);
            fireRequested = false;
        }

        int hitRobot = fireball.update(maze.getGrid());
        if (hitRobot > 0)
        {
            damageRobotAt(fireball.getCol(), fireball.getRow());
        }
        fireballCount = fireball.getPackedCount();
    }

    private void applyAdjacentRobotDamage(Player player)
    {
        int pRow = player.getRow();
        int pCol = player.getCol();
        char[][] grid = maze.getGrid();

        if (isRobotAt(grid, pRow - 1, pCol)
                || isRobotAt(grid, pRow + 1, pCol)
                || isRobotAt(grid, pRow, pCol - 1)
                || isRobotAt(grid, pRow, pCol + 1))
                {
            life = life - ADJACENT_ROBOT_DAMAGE;
        }
    }

    private boolean isRobotAt(char[][] grid, int row, int col)
    {
        if (row < 0 || row >= MAZE_ROWS || col < 0 || col >= MAZE_COLS)
        {
            return false;
        }
        return grid[row][col] == 'X';
    }



    private void initializeRobotHealthFromGrid()
    {
        char[][] grid = maze.getGrid();
        for (int r = 0; r < MAZE_ROWS; r = r + 1)
        {
            for (int c = 0; c < MAZE_COLS; c = c + 1)
            {
                if (grid[r][c] == 'X')
                {
                    addRobot(c, r);
                }
            }
        }
    }

    private boolean addRobot(int col, int row)
    {
        if (robotCount >= robots.length)
        {
            return false;
        }
        robots[robotCount] = new Robot(col, row);
        robotCount = robotCount + 1;
        return true;
    }

    private void damageRobotAt(int col, int row)
    {
        if (row < 0 || row >= MAZE_ROWS || col < 0 || col >= MAZE_COLS)
        {
            return;
        }

        char[][] grid = maze.getGrid();
        if (grid[row][col] != 'X')
        {
            return;
        }

        for (int i = 0; i < robotCount; i++)
        {
            if (robots[i] != null && robots[i].life > 0 && robots[i].x == col && robots[i].y == row)
            {
                robots[i].hp = robots[i].hp - 1;
                if (robots[i].hp <= 0)
                {
                    grid[row][col] = ' ';
                    robots[i].life = 0;
                    score = score + 50;
                }
                return;
            }
        }
    }


    private void syncAllRobotsOnGrid()
    {
        char[][] grid = maze.getGrid();

        for (int r = 0; r < GameLoop.MAZE_ROWS; r++)
        {
            for (int c = 0; c < MAZE_COLS; c++)
            {
                if (grid[r][c] == 'X') grid[r][c] = ' ';
            }
        }

        for (int i = 0; i < robotCount; i++)
        {
            if (robots[i] != null && robots[i].life > 0)
            {
                grid[robots[i].y][robots[i].x] = 'X';
            }
        }
    }

    private void drawBackpack(Player player)
    {
        int leftX = getPanelX() + 7;
        int topY = 11;
        int capacity = player.getBackpackCapacity();

        for (int i = 0; i < capacity; i = i + 1)
        {
            console.getTextWindow().setCursorPosition(leftX, topY + i);
            console.getTextWindow().output("|   |");
        }

        console.getTextWindow().setCursorPosition(leftX, topY + capacity);
        console.getTextWindow().output("+---+");

        console.getTextWindow().setCursorPosition(leftX, topY + capacity + 1);
        console.getTextWindow().output("Backpack");

        char[] items = player.getBackpackSnapshot();
        for (int i = 0; i < items.length; i = i + 1)
        {
            int row = topY + (capacity - 1 - i);
            console.getTextWindow().setCursorPosition(leftX + 2, row);
            console.getTextWindow().output(items[i]);
        }
    }

    private void clearRightPanel()
    {
        int clearX = 46;
        if (activeScreen == 2)
        {
            clearX = 70;
        }

        for (int r = 0; r < MAZE_ROWS; r = r + 1)
        {
            console.getTextWindow().setCursorPosition(clearX, r);
            console.getTextWindow().output("                                                        ");
        }
    }

    private void drawInputQueue()
    {
        int x = getPanelX();
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

    private void drawIndicators(long elapsedSeconds)
    {
        int x = getPanelX();
        int y = 5;

        console.getTextWindow().setCursorPosition(x, y);
        console.getTextWindow().output("Time     : " + elapsedSeconds + "      ");

        console.getTextWindow().setCursorPosition(x, y + 1);
        console.getTextWindow().output("Score    : " + score + "      ");

        console.getTextWindow().setCursorPosition(x, y + 2);
        console.getTextWindow().output("Fireball : " + fireballCount + "      ");

        console.getTextWindow().setCursorPosition(x, y + 3);
        console.getTextWindow().output("Life     : " + life + "      ");

        console.getTextWindow().setCursorPosition(x, y + 4);
        console.getTextWindow().output("Storage  : " + storageMode + "   ");
    }

    private int getPanelX()
    {
        if (activeScreen == 2)
        {
            return 74;
        }
        return 50;
    }

    private long getElapsedSeconds()
    {
        return (long) elapsedTicks * TIME_UNIT_MS / 1000;
    }

    private void initInputQueueAndPlaceFirstTen()
    {
        for (int i = 0; i < inputQueue.length; i = i + 1)
        {
            inputQueue[i] = generateQueueElement();
        }

        for (int i = 0; i < inputQueue.length; i = i + 1)
        {
            placeQueueElementToMaze(inputQueue[i], null);
        }
    }

    private void updateInputQueue(Player player)
    {
        if (tick % 20 != 0)
        {
            return;
        }

        char next = inputQueue[0];
        placeQueueElementToMaze(next, player);

        for (int i = 0; i < inputQueue.length - 1; i = i + 1)
        {
            inputQueue[i] = inputQueue[i + 1];
        }
        inputQueue[inputQueue.length - 1] = generateQueueElement();
    }

    private char generateQueueElement()
    {
        int roll = random.nextInt(10);

        if (roll <= 6)
        {
            return randomLogicSymbol();
        }
        if (roll <= 8)
        {
            return '@';
        }
        return 'X';
    }

    private char randomLogicSymbol()
    {
        char[] logicSymbols =
        {
                'A', 'B', 'C', 'D',
                'a', 'b', 'c', 'd',
                '~', '^', 'v', '+', '>', '='
        };
        return logicSymbols[random.nextInt(logicSymbols.length)];
    }

    private void placeQueueElementToMaze(char element, Player player)
    {
        char[][] grid = maze.getGrid();

        for (int attempt = 0; attempt < 200; attempt = attempt + 1)
        {
            int row = random.nextInt(MAZE_ROWS - 2) + 1;
            int col = random.nextInt(MAZE_COLS - 2) + 1;

            if (maze.isWall(col, row))
            {
                continue;
            }
            if (player != null && player.getRow() == row && player.getCol() == col)
            {
                continue;
            }
            if (grid[row][col] != ' ' && grid[row][col] != '\0')
            {
                continue;
            }

            if (element == 'X')
            {
                if (addRobot(col, row))
                {
                    grid[row][col] = element;
                }
            }
            else
            {
                grid[row][col] = element;
            }
            return;
        }
    }

    private String queueToString()
    {
        String text = "";
        for (int i = 0; i < inputQueue.length; i = i + 1)
        {
            text = text + inputQueue[i];
        }
        return text;
    }

    private void drawTreeExpression()
    {
        String infixText = "";
        String postfixText = "";
        if (expressionReady)
        {
            infixText = expressionInfix;
            postfixText = expressionPostfix;
        }

        console.getTextWindow().setCursorPosition(0, 19);
        console.getTextWindow().output("Expression");
        console.getTextWindow().setCursorPosition(0, 20);
        console.getTextWindow().output("Infix   : " + infixText);
        console.getTextWindow().setCursorPosition(0, 21);
        console.getTextWindow().output("Postfix : " + postfixText);
    }

    private void drawTableScreen()
    {
        long elapsed = getElapsedSeconds();

        console.getTextWindow().setCursorPosition(0, 0);
        console.getTextWindow().output("--- TABLE SCREEN ---");

        clearRightPanel();
        drawInputQueue();
        drawIndicators(elapsed);
        drawBackpack(player);

        if (!expressionReady)
        {
            console.getTextWindow().setCursorPosition(0, 2);
            console.getTextWindow().output("Finish a valid tree with F to compute the table.");
            return;
        }

        console.getTextWindow().setCursorPosition(0, 2);
        console.getTextWindow().output("Expression");
        console.getTextWindow().setCursorPosition(0, 3);
        console.getTextWindow().output("Infix   : " + expressionInfix);
        console.getTextWindow().setCursorPosition(0, 4);
        console.getTextWindow().output("Postfix : " + expressionPostfix);

        boolean[] results;
        if (truthTableResults != null)
        {
            results = truthTableResults;
        }
        else
        {
            results = new Expression(tree).evaluateAllRows();
        }

        console.getTextWindow().setCursorPosition(0, 6);
        console.getTextWindow().output("ABCD | Result");
        console.getTextWindow().setCursorPosition(0, 7);
        console.getTextWindow().output("-------------");

        for (int row = 0; row < results.length; row = row + 1)
        {
            console.getTextWindow().setCursorPosition(0, 8 + row);
            if (guessingTruthTable && row == missingSlotRow)
            {
                console.getTextWindow().output(Expression.formatRow(row, "?"));
            }
            else
            {
                console.getTextWindow().output(Expression.formatRow(row, results[row]));
            }
        }

        if (guessingTruthTable)
        {
            console.getTextWindow().setCursorPosition(25, 8);
            console.getTextWindow().output("Guess the missing value (?) by typing 0 or 1.");
        }
        else
        {
            console.getTextWindow().setCursorPosition(25, 8);
            console.getTextWindow().output("Simplify the K-Map (Quine-McCluskey)");
            console.getTextWindow().setCursorPosition(25, 10);
            console.getTextWindow().output(tableFeedback);
            console.getTextWindow().setCursorPosition(25, 11);
            console.getTextWindow().output("Enter simplified expression (use v, +, ^, ~):");
            console.getTextWindow().setCursorPosition(25, 12);
            console.getTextWindow().output("> " + treeMessage + "_");
        }
    }

    private void sleep(int ms)
    {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < ms)
        {
        }
    }
}
