package dk.ilios.hivemind.model;

import java.util.*;

/**
 * Class representing a Hive hexagonal Board.
 * As Hive doesn't have any "real" board, the board is defined from the starting piece with gets position (0,0).
 * The rest of the board is described by a 2 dimensional grid system.
 *
 * The hex grid uses a trapezoidal or axial coordinate system, like so:
 *
 *           _ _
 *         /     \
 *    _ _ /(0,-1) \ _ _
 *  /     \  -y   /     \
 * /(-1,0) \ _ _ /(1,-1) \
 * \  -x   /     \       /
 *  \ _ _ / (0,0) \ _ _ /
 *  /     \       /     \
 * /(-1,1) \ _ _ / (1,0) \
 * \       /     \  +x   /
 *  \ _ _ / (0,1) \ _ _ /
 *        \  +y   /
 *         \ _ _ /
 *
 * The board is also maintained as a graph, which is eg. is used to re
 *
 *
 */
public class BoardBackup {

    Map<String, Hex> hexes = new HashMap<String, Hex>(); // Key := (q,r), Value: hex. List of hexes visited in the game
    int[][] neighbors =  new int[][] {{0,-1},{+1,-1},{1,0},{0,1},{-1, +1},{-1,0}};

    // Canonical rotation, so the first 2 pieces always determine how the board is "rotated"
    // This means that boards always start with: W(0,0) and B(1,0).
    int canonicalRotationQ = 1;
    int canonicalRotationR = 1;

    int zobristKey = 0;


//    int[][][] edgeHash = new int[200][200][6][2]; // Zobrist dimensions: // q, r, type, color


    /**
     * Add a token to the board.
     */
    public void addToken(Token token, int q, int r) {
        if (token == null) return;
        Hex hex = findOrCreateHex(q, r);
        hex.addToken(token);
        token.setHex(hex);
        token.getPlayer().removeFromSupply(token);
    }


    /**
     * Removes a token from the board and puts in back into the players supply.
     */
    public void removeToken(int q, int r) {
        Hex hex = findOrCreateHex(q, r);
        Token token = hex.removeToken();
        token.setHex(null);
        token.getPlayer().addToSupply(token);
    }


    /**
     * Move the top creature from hex to another.
     */
    public void moveToken(int fromQ, int fromR, int toQ, int toR) {
        Hex fromHex = findHex(fromQ, fromR);
        if (fromHex == null) return;

        Token token = fromHex.removeToken();
        if (fromHex.isEmpty()) {
            hexes.remove(fromHex);
        }
        Hex toHex = findOrCreateHex(toQ, toR);
        toHex.addToken(token);
        token.setHex(toHex);
    }

    /**
     * Move token
     */
    public void moveToken(Token token, int toQ, int toR) {
        if (token.getHex() != null) {
            if (!token.getHex().getTopToken().equals(token)) {
                throw new IllegalStateException("Cannot move a token that is not on top of the stack.");
            }
            Hex hex = token.getHex();
            hex.removeToken();
            if (hex.isEmpty()) {
                hexes.remove(hex);
            }
        }

        Hex toHex = findOrCreateHex(toQ, toR);
        toHex.addToken(token);
        token.setHex(toHex);
     }

    /**
     * Returns a list of all hexes with 1 or more creatures
     */
    public List<Hex> getFilledHexes() {
        ArrayList<Hex> result = new ArrayList<Hex>();
        for (Hex hex : hexes.values()) {
            if (!hex.isEmpty()) {
                result.add(hex);
            }
        }

        return result;
    }

    /**
     * Returns the hex for the given position or create a new if it doesn't exists
     */
    private Hex findOrCreateHex(int q, int r) {

        // Look through all existing hexes
        Hex hex = findHex(q, r);
        if (hex != null) return hex;

        // Create new hex if needed
        hex = new Hex(q, r);
        hexes.put(getKey(q, r), hex);
        return hex;
    }

    private String getKey(int q, int r) {
        return ""+q+r;
    }

    /**
     * Returns the hex for the given position or null it no creatures reside there.
     */
    public Hex findHex(int q, int r) {
        Hex hex = hexes.get(getKey(q, r));
        if (hex == null) {
            return null;
        } else {
            return hex;
        }
    }

    /**
     * Returns the width of the board in hexes.
     */
    public int getWidth() {
        int minQ = Integer.MAX_VALUE;
        int maxQ = Integer.MIN_VALUE;

        for (Hex hex : hexes.values()) {
            if (hex.getTopToken() == null) continue;
            int q = hex.getQ();

            if (q < minQ) {
                minQ = q;
            }

            if (q > maxQ) {
                maxQ = q;
            }
        }

        return Math.abs(maxQ - minQ) + 1;
    }

    /**
     * Returns the heigh of the board in hexes.
     */
    public int getHeight() {
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (Hex hex : hexes.values()) {
            if (hex.getTopToken() == null) continue;
            int y = hex.getR();

            if (y < minY) {
                minY = y;
            }

            if (y > maxY) {
                maxY = y;
            }
        }

        return Math.abs(maxY - minY) + 1;
    }

    /**
     * Returns the top left hex or null if board is empty
     */
    public Hex getTopLeft() {
        if (hexes.isEmpty()) return null;

        Hex result = null;
        for (Hex hex : hexes.values()) {
            if (hex.getTopToken() == null) continue;
            if (result == null) {
                result = hex;
                continue;
            }

            if (hex.getQ() < result.getQ()) {
                result = hex;
                continue;

            } else if (hex.getR() < result.getR()) {
                result = hex;
            }
        }

        return result;
    }

    /**
     * Return minimum x coordinate for all hexes.
     */
    public int getMinQ() {
        int result = Integer.MAX_VALUE;
        for (Hex hex : hexes.values()) {
            if (hex.getTopToken() == null) continue;
            int q = hex.getQ();
            if (q < result) {
                result = q;
            }
        }

        return result;
    }

    /**
     * Return minimum y coordinate for all hexes.
     */
    public int getMinR() {
        int result = Integer.MAX_VALUE;
        for (Hex hex : hexes.values()) {
            if (hex.getTopToken() == null) continue;
            int r = hex.getR();
            if (r < result) {
                result = r;
            }
        }

        return result;
    }

    public int getMaxQ() {
        int result = Integer.MIN_VALUE;
        for (Hex hex : hexes.values()) {
            if (hex.getTopToken() == null) continue;
            int q = hex.getQ();
            if (q > result) {
                result = q;
            }
        }

        return result;
    }

    public int getMaxR() {
        int result = Integer.MIN_VALUE;
        for (Hex hex : hexes.values()) {
            if (hex.getTopToken() == null) continue;
            int r = hex.getR();
            if (r > result) {
                result = r;
            }
        }

        return result;
    }

    /**
     * Returns a list of neighbor tokens
     */
    public List<Token> getNeighborTokens(Hex centerHex) {
        if (centerHex == null) return Collections.emptyList();

        List<Token> result = new ArrayList<Token>();
        for (int i = 0; i < neighbors.length; i++) {
            int[] coordAdjustment = neighbors[i];
            int q = centerHex.getQ() + coordAdjustment[0];
            int r = centerHex.getR() + coordAdjustment[1];
            Hex hex = findHex(q, r);
            if (hex != null && hex.getTopToken() != null) {
                result.add(hex.getTopToken());
            }
        }

        return result;
    }

    /**
     * Returns a list of neighbor hexes from top and clockwise round.
     */
    public List<Hex> getNeighborHexes(Hex hex) {
        if (hex == null) return Collections.emptyList();
        List<Hex> result = new ArrayList<Hex>();
        for (int i = 0; i < neighbors.length; i++) {
            int[] coordAdjustment = neighbors[i];
            int q = hex.getQ() + coordAdjustment[0];
            int r = hex.getR() + coordAdjustment[1];
            result.add(findOrCreateHex(q, r));
        }

        return result;
    }

    public Hex getHex(int q, int r) {
        return findOrCreateHex(q, r);
    }

    /**
     * Return the hex that is the clockwise neighbor of the target hex when looking from the starting hex.
     * INVARIANT: From and To are neighbors.
     */
    public Hex getClockwiseHex(Hex from, Hex to, BoardBackup board) {
        int qDiff = to.getQ() - from.getQ();
        int rDiff = to.getR() - from.getR();

        int neighborIndex = 0;
        for (int i = 0; i < neighbors.length; i++) {
            if (Arrays.equals(new int[] {qDiff, rDiff}, neighbors[i])) {
                neighborIndex = i;
                break;
            }
        }

        int[] clockwiseNeighborCoordinates = neighbors[(neighborIndex + 1) % neighbors.length];
        return board.getHex(from.getQ() + clockwiseNeighborCoordinates[0], from.getR() + clockwiseNeighborCoordinates[1]);
    }

    /**
     * Return the hex that is the counter clockwise neighbor of the target hex when looking from the starting hex.
     * INVARIANT: From and To are neighbors.
     */
    public Hex getCounterClockwiseHex(Hex from, Hex to, BoardBackup board) {
        int qDiff = to.getQ() - from.getQ();
        int rDiff = to.getR() - from.getR();

        int neighborIndex = 0;
        for (int i = 0; i < neighbors.length; i++) {
            if (Arrays.equals(new int[] {qDiff, rDiff}, neighbors[i])) {
                neighborIndex = i;
                break;
            }
        }

        int[] clockwiseNeighborCoordinates = neighbors[(neighborIndex + neighbors.length - 1) % neighbors.length];
        return board.getHex(from.getQ() + clockwiseNeighborCoordinates[0], from.getR() + clockwiseNeighborCoordinates[1]);
    }

    /**
     * Get list of empty hexes around a hex.
     */
    public List<Hex> getEmptyNeighborHexes(Token token) {
        List<Hex> result = new ArrayList<Hex>();
        for (Hex hex : getNeighborHexes(token.getHex())) {
            if (hex.isEmpty()) {
                result.add(hex);
            }
        }

        return result;
    }

    public void clear() {
        hexes.clear();
    }

    /**
     * Returns the zobrist key for the given board layout.
     * Due to the nature of Hive boards, which move freely and can be expanded in all directions, this makes it
     * hard to make zobrist keys that also identify identical shifted or rotated positions.
     *
     * However as the search depth is never very big, it is not possible to shift or rotate a hive to another location
     * within the given search time. At least not for any interesting sized hives.
     *
     * Having zobrist keys that z(b) = z(rotate(b)) or z(b) = z(shift(b)) would however be an interesting optimization.
     *
     */
    public int getZobristKey() {
        return hashCode();
    }
}
