public class Fireball {
    private int packedCount = 0;

    private boolean active = false;
    private int x = -1;
    private int y = -1;
    private int dx = 0;
    private int dy = 0;

    public void addPacked() {
        packedCount = packedCount + 1;
    }

    public int getPackedCount() {
        return packedCount;
    }

    public boolean isActive() {
        return active;
    }

    public int getCol() {
        return x;
    }

    public int getRow() {
        return y;
    }

    public boolean fire(int startCol, int startRow, int direction) {
        if (packedCount <= 0 || active) {
            return false;
        }

        int ndx = 0;
        int ndy = 0;
        if (direction == Direction.UP) { ndy = -1; }
        if (direction == Direction.DOWN) { ndy = 1; }
        if (direction == Direction.LEFT) { ndx = -1; }
        if (direction == Direction.RIGHT) { ndx = 1; }

        if (ndx == 0 && ndy == 0) {
            return false;
        }

        packedCount = packedCount - 1;
        active = true;
        x = startCol;
        y = startRow;
        dx = ndx;
        dy = ndy;
        return true;
    }

    // Returns 1 on a robot hit; non-robot objects stop the fireball.
    public int update(char[][] grid) {
        if (!active) {
            return 0;
        }

        int nextX = x + dx;
        int nextY = y + dy;

        if (nextY < 0 || nextY >= grid.length || nextX < 0 || nextX >= grid[0].length) {
            deactivate();
            return 0;
        }

        char target = grid[nextY][nextX];

        int destroyed = 0;
        if (target == 'X') {
            destroyed = 1;
            x = nextX;
            y = nextY;
        } else if (target == ' ' || target == '\0') {
            x = nextX;
            y = nextY;
        } else {
            // Hit a wall or other obstacle — stop the fireball.
            deactivate();
            return 0;
        }

        return destroyed;
    }

    public void deactivate() {
        active = false;
        dx = 0;
        dy = 0;
    }
}