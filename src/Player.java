public class Player {
    private static final int BACKPACK_CAPACITY = 8;

    private int col;
    private int row;
    private int moveCooldown = 0;
    private Stack backpack;

    public Player(int col, int row) {
        this.col = col;
        this.row = row;
        this.backpack = new Stack(BACKPACK_CAPACITY);
    }

    public int getCol() { return col; }
    public int getRow() { return row; }

    public void tickCooldown() {
        if (moveCooldown > 0) {
            moveCooldown = moveCooldown - 1;
        }
    }

    public boolean canMove() {
        return moveCooldown == 0;
    }

    public void performMove(int directionType) {
        int dx = 0;
        int dy = 0;

        if (directionType == Direction.UP)    { dy = -1; }
        if (directionType == Direction.DOWN)  { dy =  1; }
        if (directionType == Direction.LEFT)  { dx = -1; }
        if (directionType == Direction.RIGHT) { dx =  1; }

        this.col = this.col + dx;
        this.row = this.row + dy;
        this.moveCooldown = GameConstants.PLAYER_MOVE_INTERVAL;
    }

    public int getBackpackCapacity() {
        return backpack.capacity();
    }

    public int getBackpackCount() {
        return backpack.size();
    }

    public boolean isBackpackEmpty() {
        return backpack.isEmpty();
    }

    public boolean isBackpackFull() {
        return backpack.isFull();
    }

    public boolean pushBackpack(char item) {
        return backpack.push(item);
    }

    public char popBackpack() {
        return backpack.pop();
    }

    public char peekBackpack() {
        return backpack.peek();
    }

    public char[] getBackpackSnapshot() {
        return backpack.snapshot();
    }
}
