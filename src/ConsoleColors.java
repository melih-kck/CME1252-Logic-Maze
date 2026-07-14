import enigma.console.Console;
import enigma.console.TextAttributes;

import java.awt.Color;

public class ConsoleColors
{
    public static final TextAttributes NORMAL = new TextAttributes(Color.WHITE, Color.BLACK);
    public static final TextAttributes WALL = new TextAttributes(Color.GRAY, Color.BLACK);
    public static final TextAttributes TITLE = new TextAttributes(Color.CYAN, Color.BLACK);
    public static final TextAttributes PLAYER = new TextAttributes(Color.YELLOW, Color.BLACK);
    public static final TextAttributes LOGIC = new TextAttributes(Color.RED, Color.BLACK);
    public static final TextAttributes UPPER_LOGIC = new TextAttributes(Color.RED, Color.BLACK);
    public static final TextAttributes LOWER_LOGIC = new TextAttributes(Color.CYAN, Color.BLACK);
    public static final TextAttributes FIREBALL = new TextAttributes(Color.BLUE, Color.BLACK);
    public static final TextAttributes OPERATOR = new TextAttributes(Color.RED, Color.BLACK);
    public static final TextAttributes ROBOT_RANDOM = new TextAttributes(Color.GREEN, Color.BLACK);
    public static final TextAttributes ROBOT_TARGET = new TextAttributes(Color.RED, Color.BLACK);
    public static final TextAttributes CURSOR = new TextAttributes(Color.GREEN, Color.BLACK);
    public static final TextAttributes QUESTION = new TextAttributes(Color.YELLOW, Color.BLACK);

    public static void print(Console console, char ch)
    {
        console.getTextWindow().output(ch, getColor(ch));
    }

    public static void print(Console console, char ch, TextAttributes color)
    {
        console.getTextWindow().output(ch, color);
    }

    public static void print(Console console, String text, TextAttributes color)
    {
        console.getTextWindow().output(text, color);
    }

    public static void printPlayer(Console console)
    {
        console.getTextWindow().output("P", PLAYER);
    }

    public static void printRobot(Console console, int robotMode)
    {
        if (robotMode == 1)
        {
            console.getTextWindow().output("X", ROBOT_TARGET);
        }
        else
        {
            console.getTextWindow().output("X", ROBOT_RANDOM);
        }
    }

    public static void printCursorSymbol(Console console, char symbol)
    {
        console.getTextWindow().output("[" + symbol + "]", CURSOR);
    }

    private static TextAttributes getColor(char ch)
    {
        if (ch == '#' || ch == '/' || ch == '\\' || ch == '-')
        {
            return WALL;
        }
        if (ch == 'P')
        {
            return PLAYER;
        }
        if (ch == '@' || ch == 'o')
        {
            return FIREBALL;
        }
        if (ch == '?')
        {
            return QUESTION;
        }
        if (ch == 'A' || ch == 'B' || ch == 'C' || ch == 'D')
        {
            return UPPER_LOGIC;
        }
        if (ch == 'a' || ch == 'b' || ch == 'c' || ch == 'd')
        {
            return LOWER_LOGIC;
        }
        if (ch == '~' || ch == '^' || ch == 'v' || ch == '+' || ch == '>' || ch == '=')
        {
            return OPERATOR;
        }
        if (LogicSymbol.isValidSymbol(ch))
        {
            return LOGIC;
        }
        return NORMAL;
    }
}
