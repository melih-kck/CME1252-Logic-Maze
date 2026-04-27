import enigma.console.Console;

public class Tree {

    class Node {
        char symbol;
        Node left, right, parent;
        int id;

        Node(int id, Node parent) {
            this.id = id;
            this.parent = parent;
            this.symbol = ' ';
        }
    }

    private Node root;
    private Node cursor;

    public Tree() {
        root = buildTemplate(1, null);
        cursor = root;
    }

    private Node buildTemplate(int id, Node parent) {
        if (id > 31) return null;
        Node node = new Node(id, parent);
        node.left = buildTemplate(id * 2, node);
        node.right = buildTemplate(id * 2 + 1, node);
        return node;
    }

    public void moveUp() { if (cursor.parent != null) cursor = cursor.parent; }
    public void moveLeft() { if (cursor.left != null) cursor = cursor.left; }
    public void moveRight() { if (cursor.right != null) cursor = cursor.right; }

    public void placeSymbol(char s) {
        cursor.symbol = s;
    }

    public char removeSymbol() {
        char temp = cursor.symbol;
        cursor.symbol = ' ';
        return temp;
    }

    public void drawTree(Console console) {
        console.getTextWindow().setCursorPosition(24, 1);
        console.getTextWindow().output("--- EXPRESSION TREE ---");

        renderNode(console, root, 35, 4, 16);

    }

    private void renderNode(Console console, Node node, int x, int y, int xOffset) {
        if (node == null || y > 18) return;

        console.getTextWindow().setCursorPosition(x, y);

        if (node == cursor) {
            String content;
            if (node.symbol == ' ') {
                content = ".";
            } else {
                content = String.valueOf(node.symbol);
            }
            console.getTextWindow().output("[" + content + "]");
        } else {
            String content;
            if (node.symbol == ' ') {
                content = ".";
            } else {
                content = String.valueOf(node.symbol);
            }
            console.getTextWindow().output(" " + content + " ");
        }

        if (node.left != null) {
            console.getTextWindow().setCursorPosition(x - (xOffset/2), y + 1);
            console.getTextWindow().output("/");
            renderNode(console, node.left, x - xOffset, y + 2, xOffset / 2);
        }
        if (node.right != null) {
            console.getTextWindow().setCursorPosition(x + (xOffset/2) + 2, y + 1);
            console.getTextWindow().output("\\");
            renderNode(console, node.right, x + xOffset, y + 2, xOffset / 2);
        }
    }

    public String getInfix(Node node) {
        if (node == null || node.symbol == ' ') return "";
        if (LogicSymbol.isVariableSymbol(node.symbol)) return String.valueOf(node.symbol);

        if (LogicSymbol.isUnaryOperatorSymbol(node.symbol))
            return "(" + node.symbol + getInfix(node.left) + ")";

        return "(" + getInfix(node.left) + " " + node.symbol + " " + getInfix(node.right) + ")";
    }

    public String getPostfix(Node node) {
        if (node == null || node.symbol == ' ') return "";
        if (LogicSymbol.isVariableSymbol(node.symbol)) return String.valueOf(node.symbol);

        if (LogicSymbol.isUnaryOperatorSymbol(node.symbol))
            return getPostfix(node.left) + " " + node.symbol;

        return getPostfix(node.left) + " " + getPostfix(node.right) + " " + node.symbol;
    }

    public String getFullInfix() { return getInfix(root); }
    public String getFullPostfix() { return getPostfix(root); }

    public boolean checkSyntax() {
        if (countVariables(root) < 3) return false;
        if (getDepth(root) < 3) return false;
        return validateNode(root);
    }

    private boolean validateNode(Node node) {
        if (node == null || node.symbol == ' ') return true;
        char s = node.symbol;

        if (LogicSymbol.isVariableSymbol(s)) {
            return (node.left == null || node.left.symbol == ' ') && (node.right == null || node.right.symbol == ' ');
        }
        if (LogicSymbol.isUnaryOperatorSymbol(s)) {
            return (node.left != null && node.left.symbol != ' ') && (node.right == null || node.right.symbol == ' ') && validateNode(node.left);
        }
        if (LogicSymbol.isBinaryOperatorSymbol(s)) {
            return (node.left != null && node.left.symbol != ' ') && (node.right != null && node.right.symbol != ' ') && validateNode(node.left) && validateNode(node.right);
        }
        return false;
    }

    private int countVariables(Node node) {
        if (node == null || node.symbol == ' ') return 0;
        int count = LogicSymbol.isVariableSymbol(node.symbol) ? 1 : 0;
        return count + countVariables(node.left) + countVariables(node.right);
    }

    private int getDepth(Node node) {
        if (node == null || node.symbol == ' ') return 0;
        return 1 + Math.max(getDepth(node.left), getDepth(node.right));
    }

    public int countTotalNodes(Node node) {
        if (node == null || node.symbol == ' ') return 0;
        return 1 + countTotalNodes(node.left) + countTotalNodes(node.right);
    }

    public Node getRoot() { return root; }

    public void moveToNextEmpty() {

        for (int i = 1; i <= 31; i++) {
            Node target = findNodeById(root, i);
            if (target != null && target.symbol == ' ') {
                cursor = target;
                return;
            }
        }
    }

    private Node findNodeById(Node node, int targetId) {
        if (node == null) return null;
        if (node.id == targetId) return node;

        Node left = findNodeById(node.left, targetId);
        if (left != null) return left;

        return findNodeById(node.right, targetId);
    }

    public char getCursorSymbol() {
        return cursor.symbol;
    }

}