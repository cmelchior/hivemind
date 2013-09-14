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
 *  - Black Queen coordinates must be R = 0 and 0 < Q < MAX_Q. Rotate the board clockwise until this is true.
 *
 * Before queens are placed, SP is defined the same way like in Randell Ringersolls book [1], expect we look at the 3rd/
 * 4th token instead of the queen (although it usually will be the queen anyway).
 *
 *  - 1st token (white) is placed at (0,0) -> Start
 *  - 2nd token (black) is placed at (1,0).
 *  - 3rd token (white) if R > 0, flip board around Q, so R < 0.
 *  - 4th token (black) if 3rd token has R = 0 and 4th has R > 0. Flip around Q so R < 0.
 *
 * Standard position uses the queens as "center points" as they are the most likely to be locked in place and they cannot
 * move very far. This minimizes the chance of origin changes or rotations. Analysis of Boardspace.net games has shown that
 * Queen Bee moves are only 4.8% of all moves.
 *
 * Note SP doesn't guarantee that all similar board positions have the same Zobrist Key [3]. Mirror or reflected boards
 * around the Q axis do not have the same Zobrist key. For now this is in acceptable inaccuracy.
 *
 * << Insert example here >>
 *
 * A Zobrist key is maintained for the board state. It will be recalculated for rotations/flips/center changes, so
 * the Zobrist key is always calculated on the SP board.
 *
 * Note the backing hashes for the Zobrist key have a pretty high memory requirement, due to the potential board size.
 * Currently about 800 kB.
 *
 * INVARIANT:
 *
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
    // Instead of using coordinates, which are in principal unlimited, we use token edges instead.
//    private int tokenId = 0;
//    private static final int COLORS = 2;
//    private static final int TOKEN_TYPES = 9; // Including all expansions + "empty" token
//    private static final int EDGES = 8; // 6 sides + up + down

//    Player zobristPlayer = null;
//    long[] nodeHash = new long[2*13];
//    long[][][][][] edgeHash = new long[COLORS][TOKEN_TYPES][EDGES][COLORS][TOKEN_TYPES];
//    long whiteMoved = 0;
//    long blackMoved = 0;
//    long zobristKey = 0;

//
    public Board(Player white, Player black) {
        this.whitePlayer = white;
        this.blackPlayer = black;
    }

//    private static int EMPTY_HEX = 0;
//    private static int BOTTOM_EDGE = 6;
//    private static int TOP_EDGE = 7;

    private void loadZobristHashes() {
//        Random random = new Random();
//
//        // Create the zobrist hashes needed for all edges
//        // Is it possible to convert this to a recursive call?
//        for (int i = 0; i < edgeHash.length; i++) {
//            long[][][][] d1 = edgeHash[0];
//            for (int j = 0; j < d1.length; j++) {
//                long[][][] d2 = d1[j];
//                for (int k = 0; k < d2.length; k++) {
//                    long[][] d3 = d2[k];
//                    for (int l = 0; l < d3.length; l++) {
//                        long[] d4 = d3[l];
//                        for(int m = 0; m < d4.length; m++) {
//                            edgeHash[i][j][k][l][m] = random.nextLong();
//                        }
//                    }
//                }
//            }
//        }
//
//        // Create the zobrist hashes for nodes
//        // Only used for the 1st node, after this, everything is described by edges
//        for (int i = 0; i < nodeHash.length; i++) {
//            nodeHash[i] = random.nextLong();
//        }
//
//        whiteMoved = random.nextLong();
//        blackMoved = random.nextLong();
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

//        updateZobristKey(token);
//        updateZobristKeyForPlayer(token.getPlayer());

        // Keep track of first 4 tokens
        if (tokens.size() < 5) {
            firstTokens[tokens.size() - 1] = token;
        }
        maintainStandardPosition(token);
    }

    private void maintainStandardPosition(Token token) {
        if (!standardPosition) return;
        if (bothQueensPlaced()) {
            maintainSPForMidGame(token);
        } else {
            maintainSPForOpenings(token);
        }
    }

    private boolean bothQueensPlaced() {
        return !whitePlayer.getQueen().inSupply() && !blackPlayer.getQueen().inSupply();
    }

    // INVARIANT: All tokens are placed in a legal position
    private void maintainSPForOpenings(Token token) {
        int turn = whitePlayer.getMoves() + blackPlayer.getMoves();

        if (firstTokens[3] != null) {
            // TURN 4
            moveOrigin(firstTokens[0]);
            rotateSecondTokenToStandardPosition();
            swapAxisIfNeededForThirdToken();
            swapAxisIfNeededForFourthToken();

        } else if (firstTokens[2] != null) {
            // TURN 3
            moveOrigin(firstTokens[0]);
            rotateSecondTokenToStandardPosition();
            swapAxisIfNeededForThirdToken();

        } else if (firstTokens[1] != null) {
            // TURN 2
            moveOrigin(firstTokens[0]);
            rotateSecondTokenToStandardPosition();

        } else if (firstTokens[0] != null) {
            // TURN 1
            moveOrigin(token);

        } else {
            throw new IllegalStateException("Board is empty");
        }

//            if (turn == 1) {
//            // First token must be (0,0)
//            moveOrigin(token);
//
//        } else if (turn == 2) {
//            // Rotate 1st black token to (1,0)
//            int[] spCoords = getSPCoordinatesFor(token.getHex());
//            int i = 6;
//            while(!(spCoords[0] == 1 && spCoords[1] == 0)) {
//                if (i == 0) {
//                    printer.print(this);
//                    throw new IllegalStateException("Keep rotating: " + Arrays.toString(spCoords));
//                }
//                rotateClockwise();
//                spCoords = getSPCoordinatesFor(token.getHex());
//                i--;
//            }
//
//        } else if (turn == 3) {
//            turn3Token = token;
//            if (token.getType() == BugType.QUEEN_BEE) {
//                moveOrigin(token);
//            }
//
//            // Swap around q axis if R is positive (we want token in upper left corner)
//            int[] spCoords = getSPCoordinatesFor(token.getHex());
//            if (spCoords[0] < 0 && spCoords[1] > 0) {
//                spQFlip = true;
//            } else {
//                spQFlip = false;
//            }
//
//        } else if (turn == 4) {
//            // Swap around q axis if R is positive (we want token in upper left corner)
//            Token previousToken = turn3Token;
//            Token currentToken = token;
//            int[] previousSPCoords = getSPCoordinatesFor(previousToken.getHex());
//            int[] currentSPCoords = getSPCoordinatesFor(currentToken.getHex());
//            if (previousSPCoords[1] == 0 && currentSPCoords[0] >= 0 && currentSPCoords[1] >= 0) {
//                spQFlip = true;
//            } else {
//                spQFlip = false;
//            }
//        } else {
//            // Just maintain current flip / rotation
//        }
    }

    private void swapAxisIfNeededForThirdToken() {
        int[] coords = getSPCoordinatesFor(firstTokens[2].getHex());
        if (coords[1] > 0) {
            spQFlip = true;
        } else {
            spQFlip = false;
        }
    }

    private void swapAxisIfNeededForFourthToken() {
        int[] coords = getSPCoordinatesFor(firstTokens[2].getHex());
        if (coords[1] == 0) {
            // Only check rotation if 3rd is inline with the rest
            coords = getSPCoordinatesFor(firstTokens[3].getHex());
            if (coords[1] > 0) {
                spQFlip = true;
            } else {
                spQFlip = false;
            }
        }
    }

    private void rotateSecondTokenToStandardPosition() {
        int i = 6;
        int[] spCoords = getSPCoordinatesFor(firstTokens[1].getHex());
        while(!(spCoords[0] == 1 && spCoords[1] == 0)) {
            if (i == 0) {
                printer.print(this);
                throw new IllegalStateException("Keep rotating: " + Arrays.toString(spCoords));
            }
            rotateClockwise();
            spCoords = getSPCoordinatesFor(firstTokens[1].getHex());
            i--;
        }
    }

    private void maintainSPForMidGame(Token token) {
        spQFlip = false;
        moveOrigin(whitePlayer.getQueen());
        rotateToStandardPosition(token);
    }

    private void moveOrigin(Token token) {
        if (token.inSupply()) throw new IllegalStateException("Cannot recenter around token in supply: " + token);
        spOrigin[0] = token.getHex().getQ();
        spOrigin[1] = token.getHex().getR();
    }

    // Rotate to last available space before crossing the positive Q axis.
    private void rotateToStandardPosition(Token token) {
        int[] sp = getSPCoordinatesFor(token.getHex());

        int maxRotations = 6;
        while(!(sp[0] >= 0 && sp[1] >= 0)) {
            if (maxRotations == 0) {
                throw new IllegalStateException("Keep rotating");
            }
            rotateClockwise();
            sp = getSPCoordinatesFor(token.getHex());
            maxRotations--;
        }

        rotateCounterClockwise();
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

    /**
     * Rotates the basis vectors clockwise.
     * [x, y, z] -> [-z, -x, -y]
     */
    private int[] rotateRight(int[] rotation) {
        int newX = -rotation[2];
        int newY = -rotation[0];
        int newZ = -rotation[1];
        rotation[0] = newX;
        rotation[1] = newY;
        rotation[2] = newZ;
        return rotation;
    }

    public int[] getSPCoordinatesFor(Hex hex) {
        return getSPCoordinatesFor(hex.getQ(), hex.getR());
    }

    public int[] getSPCoordinatesFor(int q, int r) {
        int[] cubeCoords = convertToCubeCoordinates(q - spOrigin[0], r - spOrigin[1]);

        for (int i = 0; i < spRotation; i++) {
            cubeCoords = rotateRight(cubeCoords);
        }

        if (spQFlip) {
            int newX = -cubeCoords[1];
            int newY = -cubeCoords[0];
            int newZ = -cubeCoords[2];
            cubeCoords[0] = newX;
            cubeCoords[1] = newY;
            cubeCoords[2] = newZ;
        }

        return convertToAxialCoordinates(cubeCoords[0], cubeCoords[1], cubeCoords[2]);
    }

    private int[] convertToAxialCoordinates(int x, int y, int z) {
        return new int[] { x, z };
    }

    // Cube coordinates fulfill x + y + z = 0
    private int[] convertToCubeCoordinates(int q, int r) {
        int x = q;
        int z = r;
        int y = -x - z;
        return new int[] { x, y, z };
    }

    /**
     * Enable standard position for the board. Can only be enabled when the board is empty.
     * @param enabled
     */
    public void setStandardPositionMode(boolean enabled) {
        if (hexes.size() > 0 && enabled) throw new IllegalStateException("Cannot enable Standard Position for non-empty board");
        this.standardPosition = enabled;
        if (enabled) {
            spOrigin = new int[2];
            spRotation = 0;
            spQFlip = false;
        }
    }

    // Encode the player who just moved into the key
    private void updateZobristKeyForPlayer(Player player) {
//        if (zobristPlayer == null) {
//            // Starting player
//            zobristKey = zobristKey ^ (player.isWhitePlayer() ? whiteMoved : blackMoved);
//            zobristPlayer = player;
//        } else {
//            // Toggle player if needed
//            if (player == zobristPlayer) return;
//            zobristKey = zobristKey ^ blackMoved;
//            zobristKey = zobristKey ^ whiteMoved;
//            zobristPlayer = player;
//        }
    }

    /**
     * Updating the Zobrist key works by XOR, so calling it once, will insert a token, calling it twice will remove it.
     */
    private void updateZobristKey(Token token) {
//        if (token == null || token.getHex() == null) return;
//
//        Hex tokenHex = token.getHex();
//        int tokenId = token.getId();
//        int color = getColorIndex(token.getPlayer());
//
//        // Update zobrist key
//        List<Hex> border = getNeighborHexes(tokenHex);
//        for (int i = 0; i < border.size(); i++) {
//            int oppositeDir = (i + 3) % 6;
//
//            Hex neighborHex = border.get(i);
//            if (neighborHex.isEmpty()) {
//                zobristKey = zobristKey ^ edgeHash[color][tokenId][i][color][EMPTY_HEX]; // Create edge from node to empty
//                zobristKey = zobristKey ^ edgeHash[color][EMPTY_HEX][oppositeDir][color][tokenId]; // Create edge in other direction from empty
//            } else {
//
//                // Cancel empty edges
//                int neighborColor = getColorIndex(neighborHex.getTopToken().getPlayer());
//                zobristKey = zobristKey ^ edgeHash[color][EMPTY_HEX][i][neighborColor][neighborHex.getTopToken().getId()]; // remove old edge (this -> neighbor)
//                zobristKey = zobristKey ^ edgeHash[neighborColor][neighborHex.getTopToken().getId()][oppositeDir][color][EMPTY_HEX]; // remove old edge (neighbor - > this)
//                zobristKey = zobristKey ^ edgeHash[color][tokenId][i][neighborColor][neighborHex.getTopToken().getId()]; // add new edge (this -> neighbor)
//                zobristKey = zobristKey ^ edgeHash[neighborColor][neighborHex.getTopToken().getId()][oppositeDir][color][tokenId]; // add new edge ( neighbor -> this)
//            }
//        }
//
//        // Update top/bottom
//        // Token is always on top
//        // Bottom might need to be updated
//        zobristKey = zobristKey ^ edgeHash[color][tokenId][TOP_EDGE][color][EMPTY_HEX];
//        zobristKey = zobristKey ^ edgeHash[color][EMPTY_HEX][BOTTOM_EDGE][color][tokenId];
//
//        if (token.getHex().getHeight() == 1) {
//            zobristKey = zobristKey ^ edgeHash[color][tokenId][BOTTOM_EDGE][color][EMPTY_HEX];
//        } else {
//
//            // Update lower token
//            Token tokenBelow = tokenHex.getTokenAt(tokenHex.getHeight() - 1);
//            zobristKey = zobristKey ^ edgeHash[color][tokenBelow.getId()][TOP_EDGE][color][tokenId];
//
//            // add bottom edge
//            zobristKey = zobristKey ^ edgeHash[color][tokenId][BOTTOM_EDGE][color][tokenBelow.getId()];
//        }
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
        updateZobristKeyForPlayer(hex.getTopToken().getPlayer());
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
        updateZobristKey(token);
        updateZobristKeyForPlayer(token.getPlayer());
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
     * Due to the nature of Hive boards, which move freely and can be expanded in all directions, this makes it
     * hard to make zobrist keys that also identify identical shifted or rotated positions.
     *
     * However as the search depth is never very big, it is not possible to shift or rotate a hive to another location
     * within the given search time. At least not for any interesting sized hives.
     *
     * Having zobrist keys that z(b) = z(rotate(b)) or z(b) = z(shift(b)) would however be an interesting optimization.
     *
     */
//    public long getZobristKey() {
//        return zobristKey;
//    }
//
//    /**
//     * Returns the next token id.
//     */
//    public int getTokenId() {
//        tokenId++;
//        return tokenId;
//    }
//
//    public void setReplayMode(boolean enabled) {
//        this.replayMode = enabled;
//    }

    public boolean isUsingStandardPosition() {
        return standardPosition;
    }
}
