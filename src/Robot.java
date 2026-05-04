import java.util.Random;

public class Robot {
    public int x, y;
    public int life = 1000;
    public int hp = 1;  // Hit points: robot dies when hp reaches 0

    private Random rnd = new Random();
    public int roboMode = 0;
    private int moveCounter = 0;

    public Robot(int sx, int sy) {
        x = sx;
        y = sy;
    }

    public void moveAndCollect(char[][] map, int ax, int ay, Robot[] robots, int robotCount, int currentRobotIndex) {
        if (life < 1) return;

        int oldx = x;
        int oldy = y;

        if (roboMode == 0) {
            int dir = rnd.nextInt(4) + 1;
            if (dir == 1) y--;
            else if (dir == 2) x++;
            else if (dir == 3) y++;
            else if (dir == 4) x--;
        }
        else if (roboMode == 1) {
            int targetX = -1; int targetY = -1;
            int minDistance = 9999;

            for (int r = 0; r < map.length; r++) {
                for (int c = 0; c < map[0].length; c++) {
                    if (isCollectible(map[r][c])) {
                        int dist = Math.abs(x - c) + Math.abs(y - r);
                        if (dist < minDistance) {
                            minDistance = dist;
                            targetX = c;
                            targetY = r;
                        }
                    }
                }
            }

            if (targetX != -1) {
                int dx = targetX - x;
                int dy = targetY - y;

                if (Math.abs(dx) > Math.abs(dy)) {
                    if (dx > 0 && canRobotStepTo(map, x + 1, y)) x++;
                    else if (dx < 0 && canRobotStepTo(map, x - 1, y)) x--;
                    else if (dy > 0 && canRobotStepTo(map, x, y + 1)) y++;
                    else if (dy < 0 && canRobotStepTo(map, x, y - 1)) y--;
                } else {
                    if (dy > 0 && canRobotStepTo(map, x, y + 1)) y++;
                    else if (dy < 0 && canRobotStepTo(map, x, y - 1)) y--;
                    else if (dx > 0 && canRobotStepTo(map, x + 1, y)) x++;
                    else if (dx < 0 && canRobotStepTo(map, x - 1, y)) x--;
                }
            } else {
                // If there is no symbol target, fall back to random movement.
                int dir = rnd.nextInt(4) + 1;
                if (dir == 1) y--; else if (dir == 2) x++; else if (dir == 3) y++; else if (dir == 4) x--;
            }
        }

        moveCounter ++ ;
        if (moveCounter >= 10) {
            if(roboMode == 0)
                roboMode = 1 ;
            else
                roboMode = 0;

            moveCounter = 0;
        }

        boolean collision = false;


        if (!canRobotStepTo(map, x, y) || (x == ax && y == ay)) {
            collision = true;
        }

        // Robot collision control
        for (int i = 0; i < robotCount; i++) {
            if (i != currentRobotIndex && robots[i] != null && robots[i].life > 0) {
                if (x == robots[i].x && y == robots[i].y) {
                    collision = true;
                    break;
                }
            }
        }

        if (collision) {
            x = oldx;
            y = oldy;
        }
    }

    public void collect(char[][] map) {
        char cell = map[y][x];
        if (cell == 'A' || cell == 'B' || cell == 'C' || cell == 'D'
                || cell == 'a' || cell == 'b' || cell == 'c' || cell == 'd'
                || cell == '~' || cell == '^' || cell == 'v'
                || cell == '+' || cell == '>' || cell == '=') {
            map[y][x] = ' ';
        }
    }

    private boolean canRobotStepTo(char[][] map, int nextX, int nextY) {
        if (nextY < 0 || nextY >= map.length || nextX < 0 || nextX >= map[0].length) {
            return false;
        }
        char cell = map[nextY][nextX];
        return cell != '#' && cell != '@' && cell != 'X';
    }

    private boolean isCollectible(char ch) {
        return ch == 'A' || ch == 'B' || ch == 'C' || ch == 'D'
                || ch == 'a' || ch == 'b' || ch == 'c' || ch == 'd'
                || ch == '~' || ch == '^' || ch == 'v'
                || ch == '+' || ch == '>' || ch == '=';
    }
}