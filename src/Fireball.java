public class Fireball
{
    private int packedCount = 0;
    private int activeCount = 0;
    private int[] x = new int[100];
    private int[] y = new int[100];
    private int[] dx = new int[100];
    private int[] dy = new int[100];
    private int hitCount = 0;
    private int[] hitX = new int[100];
    private int[] hitY = new int[100];

    public void addPacked()
    {
        packedCount = packedCount + 1;
    }

    public int getPackedCount()
    {
        return packedCount;
    }

    public int getActiveCount()
    {
        return activeCount;
    }

    public int getCol(int index)
    {
        return x[index];
    }

    public int getRow(int index)
    {
        return y[index];
    }

    public boolean isAt(int col, int row)
    {
        for (int i = 0; i < activeCount; i++)
        {
            if (x[i] == col && y[i] == row)
            {
                return true;
            }
        }
        return false;
    }

    public boolean fire(int startCol, int startRow, int direction)
    {
        if (packedCount <= 0 || activeCount >= x.length)
        {
            return false;
        }

        int newDx = 0;
        int newDy = 0;
        if (direction == GameLoop.Direction.UP) { newDy = -1; }
        if (direction == GameLoop.Direction.DOWN) { newDy = 1; }
        if (direction == GameLoop.Direction.LEFT) { newDx = -1; }
        if (direction == GameLoop.Direction.RIGHT) { newDx = 1; }

        if (newDx == 0 && newDy == 0)
        {
            return false;
        }

        packedCount = packedCount - 1;
        x[activeCount] = startCol;
        y[activeCount] = startRow;
        dx[activeCount] = newDx;
        dy[activeCount] = newDy;
        activeCount = activeCount + 1;
        return true;
    }

    public int update(char[][] grid, int playerCol, int playerRow)
    {
        hitCount = 0;
        int index = 0;

        while (index < activeCount)
        {
            int nextX = x[index] + dx[index];
            int nextY = y[index] + dy[index];

            if (nextY < 0 || nextY >= grid.length || nextX < 0 || nextX >= grid[0].length)
            {
                remove(index);
                continue;
            }

            char target = grid[nextY][nextX];
            if ((nextX == playerCol && nextY == playerRow) || isAtOther(nextX, nextY, index))
            {
                remove(index);
                continue;
            }

            if (target == 'X')
            {
                hitX[hitCount] = nextX;
                hitY[hitCount] = nextY;
                hitCount = hitCount + 1;
                x[index] = nextX;
                y[index] = nextY;
            }
            else if (target == ' ' || target == '\0')
            {
                x[index] = nextX;
                y[index] = nextY;
            }
            else
            {
                remove(index);
                continue;
            }

            index = index + 1;
        }

        return hitCount;
    }

    public int getHitCol(int index)
    {
        return hitX[index];
    }

    public int getHitRow(int index)
    {
        return hitY[index];
    }

    private boolean isAtOther(int col, int row, int current)
    {
        for (int i = 0; i < activeCount; i++)
        {
            if (i != current && x[i] == col && y[i] == row)
            {
                return true;
            }
        }
        return false;
    }

    private void remove(int index)
    {
        for (int i = index; i < activeCount - 1; i++)
        {
            x[i] = x[i + 1];
            y[i] = y[i + 1];
            dx[i] = dx[i + 1];
            dy[i] = dy[i + 1];
        }
        activeCount = activeCount - 1;
    }
}
