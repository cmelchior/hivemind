package dk.ilios.hivemind.model;

import dk.ilios.hivemind.debug.HiveAsciiPrettyPrinter;

import java.util.*;

/**
 * Class representing a Hive hexagonal Board.
 *
 * As Hive doesn't have any "real" board, the board is defined from the starting piece with gets position (0,0).
 * The rest of the board is described by a 2 dimensional grid system.
 *
 * The hex grid uses a trapezoidal or axial coordinate system [2], like so:
 *
 *           _ _
 *         /     \
 *    _ _ /(0,-1) \ _ _
 *  /     \  -r   /     \
 * /(-1,0) \ _ _ /(1,-1) \
 * \  -q   /     \       /
 *  \ _ _ / (0,0) \ _ _ /
 *  /     \       /     \
 * /(-1,1) \ _ _ / (1,0) \
 * \       /     \  +q   /
 *  \ _ _ / (0,1) \ _ _ /
 *        \  +r   /
 *         \ _ _ /
 *
 *
 * If enabled, the board is maintained in "Standard Position" (SP), which is defined by the following properties:
 *  - White Queen Bee is always (0,0)
 *  - Black Queen coordinates must be min(R) <= 0 and 0 < Q < MAX_Q. Rotate the board clockwise until this is true.
 *    This is effectively a place on the Q axis or if not able to land on R = 0, the last rotation before crossing, so
 *    R > 0.
 *
 * Before both queens are placed, SP is defined slightly different. In that case we adopt a small variant of the SP
 * definition in Randell Ringersolls book [1]. The only difference is that we always use the 3rd / 4th token even though
 * they might not be queens (although it usually will be the queen anyway).
 *
 *  - 1st token (white) is placed at (0,0).
 *  - 2nd token (black) is placed at (1,0).
 *  - 3rd token (white) if R > 0, flip board around Q axis, so R < 0.
 *  - 4th token (black) if 3rd token has R = 0 and 4th has R > 0. Flip around Q axis so R < 0.
 *
 * Standard position uses the queens as "center points" as they are the most likely to be locked in place and they cannot
 * move very far. This minimizes the chance of origin changes or rotations. Analysis of Boardspace.net games has shown
 * that Queen Bee moves are only 4.8% of all moves.
 *
 * Note SP doesn't guarantee that all similar board positions have the same Zobrist Key [3]. Mirror or reflected boards
 * around the Q axis do not have the same Zobrist key. For now this is in acceptable inaccuracy as we still reduce
 * a board position from 12 different positions to 2.
 *
 * << Insert example here >>
 *
 * A Zobrist key is maintained for the board state. It will be recalculated for rotations/flips/center changes, so
 * the Zobrist key is always calculated on the SP board.
 *
 * Note the backing hashes for the Zobrist key have a pretty high memory requirement, due to the potential board size.
 * Currently about 51*51*7*2*8 bytes ~ 2.2 MB.
 *
 * @see [1] Randy Ingersoll: Play Hive like a champion
 * @see [2] http://www.redblobgames.com/grids/hexagons/
 * @see [3] http://en.wikipedia.org/wiki/Zobrist_hashing
 */
public class Board {

    private HiveAsciiPrettyPrinter printer = new HiveAsciiPrettyPrinter();

    private Map<String, Hex> hexes = new HashMap<String, Hex>(); // Key := (q,r), Value: hex. List of hexes visited in the game

    private Set<Token> tokens = new HashSet<Token>();
    private int[][] neighbors =  new int[][] {{0,-1},{+1,-1},{1,0},{0,1},{-1, +1},{-1,0}}; // From top and clockwise round.

    // Standard position variabels
    private Player whitePlayer;
    private Player blackPlayer;

    private boolean standardPosition = false; // If true, board is maintained in Standard Position
    private boolean spQFlip = false;          // Flip around the Q axis
    private int spRotation = 0;               // How many clockwise rotations are needed to achieve Standard Position
    private int[] spOrigin = new int[2];      // Displacement of origin
    private Token[] firstTokens = new Token[4]; // Keep track of the first 4 tokens placed on the board. 2 white and 2 black

    // The board state is hashed as a Zobrist key.
    long zobristKey = 0;

    // See [2] for details about storing hexagon maps.
    // Maximum size is 26 tokens in each directions that can be stacked 7 high.
    // This is not entirely true, but for simplicity we just use that as a first implementation
    // q, r, height, color, token type
    private long[][][][][] zobristHashes = new long[51][51][7][2][8];

    public Board(Player white, Player black) {
        this.whitePlayer = white;
        this.blackPlayer = black;
    }

    private void loadZobristHashes() {
        Random random = new Random();

        // Create the zobrist hashes needed for all edges
        // Is it possible to convert this to a recursive call?
        for (int i = 0; i < zobristHashes.length; i++) {
            long[][][][] d1 = zobristHashes[0];
            for (int j = 0; j < d1.length; j++) {
                long[][][] d2 = d1[j];
                for (int k = 0; k < d2.length; k++) {
                    long[][] d3 = d2[k];
                    for (int l = 0; l < d3.length; l++) {
                        long[] d4 = d3[l];
                        for(int m = 0; m < d4.length; m++) {
                            zobristHashes[i][j][k][l][m] = random.nextLong();
                        }
                    }
                }
            }
        }
    }

    /**
     * Add a token to the board.
     */
    public void addToken(Token token, int q, int r) {
        if (token == null) return;
        Hex hex = findOrCreateHex(q, r);
        hex.addToken(token);
        token.setHex(hex);
        token.getPlayer().removeFromSupply(token);
        tokens.add(token);

        updateZobristKey(token);
//        updateZobristKeyForPlayer(token.getPlayer());

        // Keep track of first 4 tokens
        if (tokens.size() < 5) {
            firstTokens[tokens.size() - 1] = token;
        }
        maintainStandardPosition(token);
    }

    private void maintainStandardPosition(Token token) {
        if (!standardPosition) return;
        boolean rebuildZobristKey;
        if (bothQueensPlaced()) {
            rebuildZobristKey = maintainSPForMidGame(token);
        } else {
            rebuildZobristKey = maintainSPForOpenings(token);
        }

        if (rebuildZobristKey) {
            rebuildZobristKey();
        }
    }

    private boolean bothQueensPlaced() {
        return !whitePlayer.getQueen().inSupply() && !blackPlayer.getQueen().inSupply();
    }

    // INVARIANT: All tokens are placed in a legal position
    private boolean maintainSPForOpenings(Token token) {
        int turn = whitePlayer.getMoves() + blackPlayer.getMoves();
        boolean rebuildZobristKey = false;
        if (firstTokens[3] != null) {
            // TURN 4+, after turn 4 pieces can move
            rebuildZobristKey = moveOrigin(firstTokens[0]) || rebuildZobristKey;
            rebuildZobristKey = rotateToStandardPosition(firstTokens[1]) || rebuildZobristKey;
            rebuildZobristKey = swapAxisIfNeededForThirdToken() || rebuildZobristKey;
            rebuildZobristKey = swapAxisIfNeededForFourthToken() || rebuildZobristKey;

        } else if (firstTokens[2] != null) {
            // TURN 3
            rebuildZobristKey = moveOrigin(firstTokens[0]) || rebuildZobristKey;
            rebuildZobristKey = rotateToStandardPosition(firstTokens[1]) || rebuildZobristKey;
            rebuildZobristKey = swapAxisIfNeededForThirdToken() || rebuildZobristKey;

        } else if (firstTokens[1] != null) {
            // TURN 2
            rebuildZobristKey = moveOrigin(firstTokens[0]) || rebuildZobristKey;
            rebuildZobristKey = rotateToStandardPosition(firstTokens[1]) || rebuildZobristKey;

        } else if (firstTokens[0] != null) {
            // TURN 1
            rebuildZobristKey = moveOrigin(token) || rebuildZobristKey;

        } else {
            throw new IllegalStateException("Board is empty");
        }

        return rebuildZobristKey;
    }

    private boolean swapAxisIfNeededForThirdToken() {
        int[] coords = getSPCoordinatesFor(firstTokens[2].getHex());
        boolean oldFlip = spQFlip;
        // R must be negative for 3rd token to be in SP
        if (coords[1] > 0) {
            spQFlip = true;
        } else {
            spQFlip = false;
        }

        return oldFlip != spQFlip;
    }

    private boolean swapAxisIfNeededForFourthToken() {
        int[] coords = getSPCoordinatesFor(firstTokens[2].getHex());
        boolean oldFlip = spQFlip;

        // If R == 0 for 3rd piece, R must be negative for 4th piece to be in SP
        if (coords[1] == 0) {
            // Only check rotation if 3rd is inline with the rest
            coords = getSPCoordinatesFor(firstTokens[3].getHex());
            if (coords[1] > 0) {
                spQFlip = true;
            } else {
                spQFlip = false;
            }
        }

        return oldFlip != spQFlip;
    }

    private boolean maintainSPForMidGame(Token token) {
        boolean oldFlip = spQFlip;
        boolean rebuildZobristKey = false;

        spQFlip = false;
        rebuildZobristKey = moveOrigin(whitePlayer.getQueen()) || rebuildZobristKey;
        rebuildZobristKey = rotateToStandardPosition(blackPlayer.getQueen()) || rebuildZobristKey;

        return rebuildZobristKey || oldFlip != spQFlip;
    }

    private boolean moveOrigin(Token token) {
        if (token.inSupply()) throw new IllegalStateException("Cannot recenter around token in supply: " + token);
        Hex hex = token.getHex();
        boolean rebuildZobristKey = spOrigin[0] != hex.getQ() || spOrigin[1] != hex.getR();
        spOrigin[0] = token.getHex().getQ();
        spOrigin[1] = token.getHex().getR();
        return rebuildZobristKey;
    }

    // Rebuild Zobrist key when Standard Position changes
    private void rebuildZobristKey() {
        zobristKey = 0;
        for (Token t: tokens) {
            updateZobristKey(t);
        }
    }

    // Rotate to last available space before crossing the positive Q axis.
    // Return true if rotation has changed so Zobrist key needs to be rebuild.
    private boolean rotateToStandardPosition(Token token) {
        if (token.getHex().getQ() == spOrigin[0] && token.getHex().getR() == spOrigin[1]) {
            // Very special case, can happen if a Beetle working as Blacks anchor point, move on top of White's origin.
            // Just keep current SP in that case.
            return false;
        }

        int oldRotation = spRotation;
        int[] sp = getSPCoordinatesFor(token.getHex());
        int maxRotations = 6;
        while(!(sp[0] >= 0 && sp[1] >= 1)) {  // q >= 0 && r >= 1
            if (maxRotations == 0) {
                printer.print(this);
                throw new IllegalStateException("Keeps rotating: Cannot find Standard Position");
            }
            rotateClockwise();
            sp = getSPCoordinatesFor(token.getHex());
            maxRotations--;
        }

        rotateCounterClockwise();
        return spRotation != oldRotation;
    }

    private void rotateClockwise() {
        spRotation = (spRotation + 1) % 6;
    }

    private void rotateCounterClockwise() {
        spRotation--;
        if (spRotation < 0) {
            spRotation = 5;
        }
    }

    public int[] getSPCoordinatesFor(Hex hex) {
        return getSPCoordinatesFor(hex.getQ(), hex.getR());
    }

    public int[] getSPCoordinatesFor(int q, int r) {
        int[] cubeCoords = HexagonUtils.convertToCubeCoordinates(q - spOrigin[0], r - spOrigin[1]);

        for (int i = 0; i < spRotation; i++) {
            cubeCoords = HexagonUtils.rotateRight(cubeCoords);
        }

        if (spQFlip) {
            int newX = -cubeCoords[1];
            int newY = -cubeCoords[0];
            int newZ = -cubeCoords[2];
            cubeCoords[0] = newX;
            cubeCoords[1] = newY;
            cubeCoords[2] = newZ;
        }

        return HexagonUtils.convertToAxialCoordinates(cubeCoords[0], cubeCoords[1], cubeCoords[2]);
    }

    /**
     * Returns the hex for the given Standard Position
     *
     * @param q Q coordinate in Standard Position
     * @param r R coordinate in Standard Position
     */
    public Hex getHexForStandardPosition(int q, int r) {
        int[] cubeCoords = HexagonUtils.convertToCubeCoordinates(q, r);

        if (spQFlip) {
            int newX = -cubeCoords[1];
            int newY = -cubeCoords[0];
            int newZ = -cubeCoords[2];
            cubeCoords[0] = newX;
            cubeCoords[1] = newY;
            cubeCoords[2] = newZ;
        }

        for (int i = 0; i < (6 - spRotation); i++) {
            cubeCoords = HexagonUtils.rotateRight(cubeCoords);
        }

        int[] originalCoords = HexagonUtils.convertToAxialCoordinates(cubeCoords[0], cubeCoords[1], cubeCoords[2]);
        return findOrCreateHex(originalCoords[0] + spOrigin[0], originalCoords[1] + spOrigin[1]);
    }

    /**
     * Enable standard position for the board. Can only be enabled when the board is empty.
     * @param enabled
     */
    public void setStandardPositionMode(boolean enabled) {
        if (hexes.size() > 0 && enabled) throw new IllegalStateException("Cannot enable Standard Position for non-empty board");
        this.standardPosition = enabled;
        if (enabled) {
            loadZobristHashes();
            spOrigin = new int[2];
            spRotation = 0;
            spQFlip = false;
        }
    }

    /**
     * Updating the Zobrist key works by XOR, so calling it once, will insert a token, calling it twice will remove it.
     */
    private void updateZobristKey(Token token) {
        if (token == null || token.getHex() == null) return;

        Hex tokenHex = token.getHex();
        int typeIndex;
        switch(token.getOriginalType()) {
            case QUEEN_BEE: typeIndex = 0; break;
            case BEETLE: typeIndex = 1; break;
            case GRASSHOPPER: typeIndex = 2; break;
            case SPIDER: typeIndex = 3; break;
            case SOLDIER_ANT: typeIndex = 4; break;
            case MOSQUITO: typeIndex = 5; break;
            case LADY_BUG: typeIndex = 6; break;
            case PILL_BUG: typeIndex = 7; break;
            default:
                throw new IllegalStateException("Unknown token: " + token);
        }

        int[] coords = getSPCoordinatesFor(tokenHex);
        zobristKey = zobristKey ^ zobristHashes[coords[0] + 25][coords[1] + 25][tokenHex.getHeight() - 1][getColorIndex(token.getPlayer())][typeIndex];
    }

    private int getColorIndex(Player player) {
        return player.isWhitePlayer() ? 0 : 1;
    }

    /**
     * Removes a token from the board and puts in back into the players supply.
     *
     * INVARIANT: Can only be removed in the order they where added.
     */
    public void removeToken(int q, int r) {
        Hex hex = findOrCreateHex(q, r);
        updateZobristKey(hex.getTopToken());
        Token token = hex.removeToken();
        token.setHex(null);
        token.getPlayer().addToSupply(token);
        tokens.remove(token);

        // Maintain tracking of first 4 tokens.
        if (tokens.size() < 4) {
            firstTokens[tokens.size()] = null;
        }
    }


    /**
     * Move token
     */
    public void moveToken(Token token, int toQ, int toR) {
        if (token.getHex() != null) {
            if (!token.getHex().getTopToken().equals(token)) {
                throw new IllegalStateException("Cannot move a token that is not on top of the stack.");
            }
            updateZobristKey(token); // Remove current position
            Hex hex = token.getHex();
            hex.removeToken();
            if (hex.isEmpty()) {
                hexes.remove(hex);
            }
        }

        Hex toHex = findOrCreateHex(toQ, toR);
        toHex.addToken(token);
        token.setHex(toHex);
        updateZobristKey(token); // Add new position
    }


    /**
     * Move the top creature from hex to another.
     */
    public void moveToken(int fromQ, int fromR, int toQ, int toR) {
        Hex fromHex = findHex(fromQ, fromR);
        if (fromHex == null) return;

        updateZobristKey(fromHex.getTopToken()); // Remove current position
        Token token = fromHex.removeToken();
        Hex toHex = findOrCreateHex(toQ, toR);
        toHex.addToken(token);
        token.setHex(toHex);
//        updateZobristKey(token); // Insert new position
//        updateZobristKeyForPlayer(token.getPlayer());
        maintainStandardPosition(token);
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
            int q = standardPosition ? getSPCoordinatesFor(hex)[0] : hex.getQ();
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
            int r = standardPosition ? getSPCoordinatesFor(hex)[1] : hex.getR();
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
            int q = standardPosition ? getSPCoordinatesFor(hex)[0] : hex.getQ();
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
            int r = standardPosition ? getSPCoordinatesFor(hex)[1] : hex.getR();
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
    public Hex getClockwiseHex(Hex from, Hex to, Board board) {
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
    public Hex getCounterClockwiseHex(Hex from, Hex to, Board board) {
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
     */
    public long getZobristKey() {
        return zobristKey;
    }

    public boolean isUsingStandardPosition() {
        return standardPosition;
    }
}
