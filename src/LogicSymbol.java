public class LogicSymbol {
	public static final int TYPE_VARIABLE = 1;
	public static final int TYPE_UNARY_OPERATOR = 2;
	public static final int TYPE_BINARY_OPERATOR = 3;

	// Variables and their negated maze versions
	public static final char VAR_A = 'A';
	public static final char VAR_B = 'B';
	public static final char VAR_C = 'C';
	public static final char VAR_D = 'D';
	public static final char NOT_A = 'a';
	public static final char NOT_B = 'b';
	public static final char NOT_C = 'c';
	public static final char NOT_D = 'd';

	// Operators from the project document
	public static final char OP_NOT = '~';
	public static final char OP_AND = '^';
	public static final char OP_OR = 'v';
	public static final char OP_XOR = '+';
	public static final char OP_IMPLIES = '>';
	public static final char OP_IFF = '=';

	private final char symbol;
	private final int type;

	public LogicSymbol(char symbol) {
		if (!isValidSymbol(symbol)) {
			throw new IllegalArgumentException("Invalid logic symbol: " + symbol);
		}
		this.symbol = symbol;
		this.type = detectType(symbol);
	}

	public char getSymbol() {
		return symbol;
	}

	public int getType() {
		return type;
	}

	public boolean isVariable() {
		return type == TYPE_VARIABLE;
	}

	public boolean isUnaryOperator() {
		return type == TYPE_UNARY_OPERATOR;
	}

	public boolean isBinaryOperator() {
		return type == TYPE_BINARY_OPERATOR;
	}

	public static boolean isValidSymbol(char ch) {
		return isVariableSymbol(ch) || isUnaryOperatorSymbol(ch) || isBinaryOperatorSymbol(ch);
	}

	public static boolean isVariableSymbol(char ch) {
		return ch == VAR_A || ch == VAR_B || ch == VAR_C || ch == VAR_D
				|| ch == NOT_A || ch == NOT_B || ch == NOT_C || ch == NOT_D;
	}

	public static boolean isUnaryOperatorSymbol(char ch) {
		return ch == OP_NOT;
	}

	public static boolean isBinaryOperatorSymbol(char ch) {
		return ch == OP_AND || ch == OP_OR || ch == OP_XOR || ch == OP_IMPLIES || ch == OP_IFF;
	}

	public static boolean evaluateUnary(char op, boolean value) {
		if (op == OP_NOT) {
			return !value;
		}
		throw new IllegalArgumentException("Unknown unary operator: " + op);
	}

	public static boolean evaluateBinary(char op, boolean left, boolean right) {
		if (op == OP_AND) {
			return left && right;
		}
		if (op == OP_OR) {
			return left || right;
		}
		if (op == OP_XOR) {
			return left ^ right;
		}
		if (op == OP_IMPLIES) {
			return (!left) || right;
		}
		if (op == OP_IFF) {
			return left == right;
		}
		throw new IllegalArgumentException("Unknown binary operator: " + op);
	}

	private static int detectType(char ch) {
		if (isVariableSymbol(ch)) {
			return TYPE_VARIABLE;
		}
		if (isUnaryOperatorSymbol(ch)) {
			return TYPE_UNARY_OPERATOR;
		}
		return TYPE_BINARY_OPERATOR;
	}

	@Override
	public String toString() {
		return Character.toString(symbol);
	}
}
