public class Expression {


    private Tree tree;

    public Expression(Tree tree) {
        this.tree = tree;
    }

    // Evaluate the expression for all 16 combinations of A, B, C, D.
    // Returns a boolean[16] where index i corresponds to the row where
    // A = bit3 of i, B = bit2, C = bit1, D = bit0.
    public boolean[] evaluateAllRows() {
        boolean[] results = new boolean[16];
        for (int i = 0; i < 16; i++) {
            boolean a = (i & 8) != 0;
            boolean b = (i & 4) != 0;
            boolean c = (i & 2) != 0;
            boolean d = (i & 1) != 0;
            results[i] = evaluateNode(tree.getRoot(), a, b, c, d);
        }
        return results;
    }

    // Recursively evaluate a node in the expression tree.
    private boolean evaluateNode(Tree.Node node, boolean a, boolean b, boolean c, boolean d) {
        if (node == null || node.symbol == ' ') {
            return false;
        }

        char s = node.symbol;

        // Variable or negated variable
        if (LogicSymbol.isVariableSymbol(s)) {
            if (s == LogicSymbol.VAR_A) { return a; }
            if (s == LogicSymbol.VAR_B) { return b; }
            if (s == LogicSymbol.VAR_C) { return c; }
            if (s == LogicSymbol.VAR_D) { return d; }
            if (s == LogicSymbol.NOT_A) { return !a; }
            if (s == LogicSymbol.NOT_B) { return !b; }
            if (s == LogicSymbol.NOT_C) { return !c; }
            if (s == LogicSymbol.NOT_D) { return !d; }
        }

        // Unary operator (~)
        if (LogicSymbol.isUnaryOperatorSymbol(s)) {
            boolean operand = evaluateNode(node.left, a, b, c, d);
            return LogicSymbol.evaluateUnary(s, operand);
        }

        // Binary operator
        if (LogicSymbol.isBinaryOperatorSymbol(s)) {
            boolean left  = evaluateNode(node.left,  a, b, c, d);
            boolean right = evaluateNode(node.right, a, b, c, d);
            return LogicSymbol.evaluateBinary(s, left, right);
        }

        return false;
    }

    // Format a single truth table row as a readable string.
    // row: 0-15, result: the evaluated value for that row.
    public static String formatRow(int row, boolean result) {
        int a;
        int b;
        int c;
        int d;
        String resultText;

        if ((row & 8) != 0) {
            a = 1;
        } else {
            a = 0;
        }

        if ((row & 4) != 0) {
            b = 1;
        } else {
            b = 0;
        }

        if ((row & 2) != 0) {
            c = 1;
        } else {
            c = 0;
        }

        if ((row & 1) != 0) {
            d = 1;
        } else {
            d = 0;
        }

        if (result) {
            resultText = "1";
        } else {
            resultText = "0";
        }

        return "" + a + b + c + d + " | " + resultText;
    }

 }
