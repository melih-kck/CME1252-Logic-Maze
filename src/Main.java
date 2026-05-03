import enigma.core.Enigma;
import enigma.console.Console;

public class Main {

    public static void main(String[] args) throws Exception {
        Console console = Enigma.getConsole("Logic Maze Game", 100, 30, 20, 2);

        Maze maze = new Maze();
        Player player = new Player(1, 1);
        GameLoop game = new GameLoop(console, maze);
        game.start(player);
    }
}
