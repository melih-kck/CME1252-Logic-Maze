public class Player {
    private int col;
    private int row;
    private int moveCooldown = 0;

    public Player(int col, int row) {
        this.col = col;
        this.row = row;
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
}