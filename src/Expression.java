public class Expression
{

    private Tree tree;

    public Expression(Tree tree)
    {
        this.tree = tree;
    }

    public boolean[] evaluateAllRows()
    {
        boolean[] results = new boolean[16];
        for (int i = 0; i < 16; i++)
        {
            boolean a = (i & 8) != 0;
            boolean b = (i & 4) != 0;
            boolean c = (i & 2) != 0;
            boolean d = (i & 1) != 0;
            results[i] = evaluateNode(tree.getRoot(), a, b, c, d);
        }
        return results;
    }

    public String[] getColumnNames()
    {
        Tree.Node[] nodes = getColumnNodes();
        String[] names = new String[nodes.length];
        for (int i = 0; i < nodes.length; i++)
        {
            names[i] = tree.getInfix(nodes[i]);
        }
        return names;
    }

    public boolean[][] evaluateColumns()
    {
        Tree.Node[] nodes = getColumnNodes();
        boolean[][] results = new boolean[nodes.length][16];

        for (int column = 0; column < nodes.length; column++)
        {
            for (int row = 0; row < 16; row++)
            {
                boolean a = (row & 8) != 0;
                boolean b = (row & 4) != 0;
                boolean c = (row & 2) != 0;
                boolean d = (row & 1) != 0;
                results[column][row] = evaluateNode(nodes[column], a, b, c, d);
            }
        }
        return results;
    }

    private Tree.Node[] getColumnNodes()
    {
        Tree.Node[] allNodes = new Tree.Node[31];
        int count = collectColumnNodes(tree.getRoot(), allNodes, 0);
        Tree.Node[] result = new Tree.Node[count];
        for (int i = 0; i < count; i++)
        {
            result[i] = allNodes[i];
        }
        return result;
    }

    private int collectColumnNodes(Tree.Node node, Tree.Node[] nodes, int count)
    {
        if (node == null || node.symbol == ' ')
        {
            return count;
        }

        count = collectColumnNodes(node.left, nodes, count);
        count = collectColumnNodes(node.right, nodes, count);
        if (LogicSymbol.isUnaryOperatorSymbol(node.symbol)
                || LogicSymbol.isBinaryOperatorSymbol(node.symbol))
        {
            nodes[count] = node;
            count = count + 1;
        }
        return count;
    }

    private boolean evaluateNode(Tree.Node node, boolean a, boolean b, boolean c, boolean d)
    {
        if (node == null || node.symbol == ' ')
        {
            return false;
        }

        char s = node.symbol;

        if (LogicSymbol.isVariableSymbol(s))
        {
            if (s == LogicSymbol.VAR_A) { return a; }
            if (s == LogicSymbol.VAR_B) { return b; }
            if (s == LogicSymbol.VAR_C) { return c; }
            if (s == LogicSymbol.VAR_D) { return d; }
            if (s == LogicSymbol.NOT_A) { return !a; }
            if (s == LogicSymbol.NOT_B) { return !b; }
            if (s == LogicSymbol.NOT_C) { return !c; }
            if (s == LogicSymbol.NOT_D) { return !d; }
        }

        if (LogicSymbol.isUnaryOperatorSymbol(s))
        {
            boolean operand = evaluateNode(node.left, a, b, c, d);
            return LogicSymbol.evaluateUnary(s, operand);
        }

        if (LogicSymbol.isBinaryOperatorSymbol(s))
        {
            boolean left  = evaluateNode(node.left,  a, b, c, d);
            boolean right = evaluateNode(node.right, a, b, c, d);
            return LogicSymbol.evaluateBinary(s, left, right);
        }

        return false;
    }

    public static String formatRow(int row, boolean result)
    {
        return formatRow(row, result ? "1" : "0");
    }

    public static String formatRow(int row, String resultText)
    {
        int a;
        int b;
        int c;
        int d;

        if ((row & 8) != 0)
        {
            a = 1;
        }
        else
        {
            a = 0;
        }

        if ((row & 4) != 0)
        {
            b = 1;
        }
        else
        {
            b = 0;
        }

        if ((row & 2) != 0)
        {
            c = 1;
        }
        else
        {
            c = 0;
        }

        if ((row & 1) != 0)
        {
            d = 1;
        }
        else
        {
            d = 0;
        }

        return "" + a + b + c + d + " | " + resultText;
    }

 }
