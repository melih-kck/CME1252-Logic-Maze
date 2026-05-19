// Recursive descent parser and evaluator for boolean expressions.
public final class StringEvaluator
{
    private StringEvaluator()
    {
    }

    // Evaluates the expression for all 16 truth table rows (A,B,C,D combinations).
    public static boolean[] evaluate(String expression)
    {
        ExprNode root = parse(expression);
        if (root == null)
        {
            return null;
        }

        boolean[] results = new boolean[16];
        for (int rowIndex = 0; rowIndex < 16; rowIndex++)
        {
            boolean valueA = (rowIndex & 8) != 0;
            boolean valueB = (rowIndex & 4) != 0;
            boolean valueC = (rowIndex & 2) != 0;
            boolean valueD = (rowIndex & 1) != 0;
            results[rowIndex] = root.evaluate(valueA, valueB, valueC, valueD);
        }
        return results;
    }



    private static ExprNode parse(String expression)
    {
        if (expression == null || expression.trim().isEmpty())
        {
            return null;
        }

        Parser parser = new Parser(expression);
        ExprNode root = parser.parseExpression();
        if (root == null || !parser.isAtEnd())
        {
            return null;
        }
        return root;
    }

    private static final class Parser
    {
        private final String text;
        private int index;

        Parser(String text)
        {
            this.text = text;
            this.index = 0;
        }

        ExprNode parseExpression()
        {
            return parseOr();
        }

        private ExprNode parseOr()
        {
            ExprNode left = parseXor();
            if (left == null)
            {
                return null;
            }

            while (true)
            {
                if (match('v'))
                {
                    ExprNode right = parseXor();
                    if (right == null)
                    {
                        return null;
                    }
                    left = ExprNode.binary('O', left, right);
                }
                else
                {
                    return left;
                }
            }
        }

        private ExprNode parseXor()
        {
            ExprNode left = parseAnd();
            if (left == null)
            {
                return null;
            }

            while (true)
            {
                if (match('+'))
                {
                    ExprNode right = parseAnd();
                    if (right == null)
                    {
                        return null;
                    }
                    left = ExprNode.binary('X', left, right);
                }
                else
                {
                    return left;
                }
            }
        }

        private ExprNode parseAnd()
        {
            ExprNode left = parseUnary();
            if (left == null)
            {
                return null;
            }

            while (true)
            {
                skipSpaces();
                if (match('^'))
                {
                    ExprNode right = parseUnary();
                    if (right == null)
                    {
                        return null;
                    }
                    left = ExprNode.binary('A', left, right);
                }
                else if (startsFactor(peek()))
                {
                    ExprNode right = parseUnary();
                    if (right == null)
                    {
                        return null;
                    }
                    left = ExprNode.binary('A', left, right);
                }
                else
                {
                    return left;
                }
            }
        }

        private ExprNode parseUnary()
        {
            skipSpaces();
            if (match('~'))
            {
                ExprNode operand = parseUnary();
                if (operand == null)
                {
                    return null;
                }
                return ExprNode.not(operand);
            }

            ExprNode node = parsePrimary();
            if (node == null)
            {
                return null;
            }

            while (match('\''))
            {
                node = ExprNode.not(node);
            }
            return node;
        }

        private ExprNode parsePrimary()
        {
            skipSpaces();
            char currentChar = peek();

            if (currentChar == '(')
            {
                index++;
                ExprNode node = parseExpression();
                if (node == null || !match(')'))
                {
                    return null;
                }
                return node;
            }

            if (currentChar == '0' || currentChar == '1')
            {
                index++;
                return ExprNode.constant(currentChar == '1');
            }

            if (currentChar >= 'A' && currentChar <= 'D')
            {
                index++;
                return ExprNode.variable(currentChar);
            }

            if (currentChar >= 'a' && currentChar <= 'd')
            {
                index++;
                return ExprNode.not(ExprNode.variable((char) (currentChar - 'a' + 'A')));
            }

            return null;
        }

        private boolean startsFactor(char currentChar)
        {
            return currentChar == '~' || currentChar == '(' || currentChar == '0' || currentChar == '1'
            || (currentChar >= 'A' && currentChar <= 'D') || (currentChar >= 'a' && currentChar <= 'd');
        }

        private boolean match(char expected)
        {
            skipSpaces();
            if (index < text.length() && text.charAt(index) == expected)
            {
                index++;
                return true;
            }
            return false;
        }

        private char peek()
        {
            if (index >= text.length())
            {
                return '\0';
            }
            return text.charAt(index);
        }

        private void skipSpaces()
        {
            while (index < text.length() && Character.isWhitespace(text.charAt(index)))
            {
                index++;
            }
        }

        boolean isAtEnd()
        {
            skipSpaces();
            return index == text.length();
        }
    }

    private static final class ExprNode
    {
        private char type;
        private char variable;
        private boolean constant;
        private ExprNode left;
        private ExprNode right;

        static ExprNode constant(boolean value)
        {
            ExprNode node = new ExprNode();
            node.type = 'C';
            node.constant = value;
            return node;
        }

        static ExprNode variable(char variable)
        {
            ExprNode node = new ExprNode();
            node.type = 'V';
            node.variable = variable;
            return node;
        }

        static ExprNode not(ExprNode child)
        {
            ExprNode node = new ExprNode();
            node.type = 'N';
            node.left = child;
            return node;
        }

        static ExprNode binary(char type, ExprNode left, ExprNode right)
        {
            ExprNode node = new ExprNode();
            node.type = type;
            node.left = left;
            node.right = right;
            return node;
        }

        boolean evaluate(boolean valueA, boolean valueB, boolean valueC, boolean valueD)
        {
            if (type == 'C')
            {
                return constant;
            }
            if (type == 'V')
            {
                if (variable == 'A')
                {
                    return valueA;
                }
                if (variable == 'B')
                {
                    return valueB;
                }
                if (variable == 'C')
                {
                    return valueC;
                }
                return valueD;
            }
            if (type == 'N')
            {
                return !left.evaluate(valueA, valueB, valueC, valueD);
            }
            if (type == 'A')
            {
                return left.evaluate(valueA, valueB, valueC, valueD)
                        && right.evaluate(valueA, valueB, valueC, valueD);
            }
            if (type == 'X')
            {
                return left.evaluate(valueA, valueB, valueC, valueD)
                        ^ right.evaluate(valueA, valueB, valueC, valueD);
            }
            return left.evaluate(valueA, valueB, valueC, valueD)
                    || right.evaluate(valueA, valueB, valueC, valueD);
        }
    }
}
