public class Stack {
    private char[] data;
    private int top;

    public Stack(int capacity) {
        data = new char[capacity];
        top = -1;
    }

    public boolean push(char value) {
        if (isFull()) {
            return false;
        }
        top = top + 1;
        data[top] = value;
        return true;
    }

    public char pop() {
        if (isEmpty()) {
            return '\0';
        }
        char value = data[top];
        top = top - 1;
        return value;
    }

    public char peek() {
        if (isEmpty()) {
            return '\0';
        }
        return data[top];
    }

    public boolean isEmpty() {
        return top == -1;
    }

    public boolean isFull() {
        return top == data.length - 1;
    }

    public int size() {
        return top + 1;
    }

    public int capacity() {
        return data.length;
    }

    public char[] snapshot() {
        char[] out = new char[size()];
        for (int i = 0; i < size(); i = i + 1) {
            out[i] = data[i];
        }
        return out;
    }
}
