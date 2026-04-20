import enigma.core.Enigma;
import enigma.console.Console;
import java.util.Random;

public class Main {
}
public static void main(String[] args) throws Exception {
    // Init Enigma with title and window dimensions
    Console console = Enigma.getConsole("Logic Maze Game", 100, 30, 20, 2);


    Random rand = new Random();
    int timer = 0 ;

    Maze mg = new Maze();


    final int ROW = 21;
    final int COL = 53;
    int placed = 0 ;
    /* char[][] maze ;
     maze = mg.loadMazeFromFile("PBL4/maze.txt");
*/

    while(placed < 10) {


        int sely = rand.nextInt(ROW - 2) + 1;
        int selx = rand.nextInt(COL - 2) + 1;
        int selection = rand.nextInt(20) + 1;

        if (!mg.isWall(sely, selx)) {

            if (selection == 1) {
                mg.setGrid(sely, selx, 65);
            }
            else if (selection == 2) {
                mg.setGrid(sely, selx, 66);
            }
            else if (selection == 3) {
                mg.setGrid(sely, selx, 67);
            }
            else if (selection == 4) {
                mg.setGrid(sely, selx, 68);
            }
            else if (selection == 5) {
                mg.setGrid(sely, selx, 97);
            }
            else if (selection == 6) {
                mg.setGrid(sely, selx, 98);
            }
            else if (selection == 7) {
                mg.setGrid(sely, selx, 99);
            }
            else if (selection == 8) {
                mg.setGrid(sely, selx, 100);
            }
            else if (selection == 9) {
                mg.setGrid(sely, selx, 152);
            }
            else if (selection == 10) {
                mg.setGrid(sely, selx, 94);
            }
            else if (selection == 11) {
                mg.setGrid(sely, selx, 118);
            }
            else if (selection == 12) {
                mg.setGrid(sely, selx, 43);
            }
            else if (selection == 13) {
                mg.setGrid(sely, selx, 62);
            }
            else if (selection == 14) {
                mg.setGrid(sely, selx, 61);
            }
            else if (selection == 15 || selection == 16 || selection == 17 || selection == 18) {
                mg.setGrid(sely, selx, 64);
            }
            if (selection == 19 || selection == 20) {
                mg.setGrid(sely, selx, 88);
            }

            placed ++ ;



        }
    }










    Player player = new Player(1, 1);
    GameLoop game = new GameLoop(console,mg);
    game.start(player);
}

