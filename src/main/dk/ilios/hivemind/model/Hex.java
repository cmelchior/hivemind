package dk.ilios.hivemind.model;

import java.util.Stack;

/**
 * Describe a Hex on the board
 */
public class Hex {

    public static final int SUPPLY = Integer.MAX_VALUE;

    private Stack<Token> tokens = new Stack<Token>();
    private int q = 0;
    private int r = 0;


    /**
     * Construct empty hex with no neighbors
     */
    public Hex(int x, int y) {
        this.q = x;
        this.r = y;
    }

    public boolean isEmpty() {
        return tokens.isEmpty();
    }

    public void addToken(Token token) {
        if (token == null) return;
        tokens.push(token);
    }

    public Token removeToken() {
        return tokens.pop();
    }

    public Token getTopToken() {
        if (tokens.isEmpty()) return null;
        return tokens.peek();
    }

    /**
     * Return token at given height.
     * @param height Height to find token. Ground level is 1;
     */
    public Token getTokenAt(int height) {
        if (tokens.size() < height) {
            return null;
        } else {
            return tokens.get(height - 1);
        }
    }

    public int getHeight() {
        return tokens.size();
    }

    public int getQ() {
        return q;
    }

    public int getR() {
        return r;
    }

//    @Override
//    public boolean equals(Object o) {
//        if (o == null || !(o instanceof Hex)) return false;
//        Hex otherHex = (Hex) o;
//        return q == otherHex.getQ() && r == otherHex.getR();
//    }

    @Override
    public String toString() {
        return "(" + q + ", " + r + ")";
    }
}
