package dk.ilios.hivemind.model

/**
 * Class representing a Hive hexagonal Board.
 * The board is represented as a graph, with the following properties:
 *
 * - Each node represent a hex.
 * - A node can either be empty or filled. Empty hexes are legal placements for new tokens. Filled contains tokens.
 * - Start graph is 1 empty node.
 * - Adding a token will automaticall expand the graph with new nodes.
 *
 * but can be mapped to a hex grid with the top-left node at (0,0) using the
 * following trapezoidal coordinate system.
 * _ _
 * /     \
 * _ _ /(0,-1) \ _ _
 * /     \  -y   /     \
 * /(-1,0) \ _ _ /(1,-1) \
 * \  -x   /     \       /
 * \ _ _ / (0,0) \ _ _ /
 * /     \       /     \
 * /(-1,1) \ _ _ / (1,0) \
 * \       /     \  +x   /
 * \ _ _ / (0,1) \ _ _ /
 * \  +y   /
 * \ _ _ /
 *
 */
class GraphBoard {
    //    Map<String, Hex> border = new HashMap<String, Hex>(); // List of hexes that make up the border of the hive.
    var hexes: MutableMap<String?, Hex?> =
        HashMap<String?, Hex?>() // Key := (q,r), Value: hex. List of hexes visited in the game

    var neighbors: Array<IntArray?> = arrayOf(
        intArrayOf(0, -1),
        intArrayOf(+1, -1),
        intArrayOf(1, 0),
        intArrayOf(0, 1),
        intArrayOf(-1, +1),
        intArrayOf(-1, 0)
    )

    //    Map<Token, Node> hexes = new HashMap<Token, Node>(); // Maps a token to its graph node
    private var border: MutableSet<Node?> = HashSet<Node?>() // Maps a hex to its graph node.
    private var nodes: MutableMap<Hex?, Node?> = HashMap<Hex?, Node?>() // Maps a hex to its graph node.


    init {
        val start: Node = Node()
        start.node = Hex(0, 0)
        border.add(start)
    }

    /**
     * Only possible to add token to existing nodes
     */
    private fun addToken(token: Token?, node: Node) {
        if (token == null) return
        val hex = node.node!!
        hex.addToken(token)
        token.hex = hex
        token.player.removeFromSupply(token)
        //        nodeFilled(node);
    }

    fun addToken(token: Token?, q: Int, r: Int) {
        if (token == null) return
        val hex = findOrCreateHex(q, r)
        hex.addToken(token)
        token.hex = hex
        token.player.removeFromSupply(token)
    }


    /**
     * Input node has been filled. Create new border hexes as needed.
     *
     * @param node Node to expand from.
     */
    private fun expandGraphFrom(node: Node) {
        for (i in node.edges.indices) {
            val neighbor = node.edges[i]
            if (neighbor == null) {
                // Add new empty node (expanding the graph)
                node.edges[i] = Node()
            }
        }
    }


    //    /**
    //     * Removes a token from the board and puts in back into the players supply.
    //     */
    //    public void removeToken(int q, int r) {
    //        Hex hex = findOrCreateHex(q, r);
    //        Token token = hex.removeToken();
    //        token.setHex(null);
    //        token.getPlayer().addToSupply(token);
    //    }
    //
    //
    //    /**
    //     * Move the top creature from hex to another.
    //     */
    //    public void moveToken(int fromQ, int fromR, int toQ, int toR) {
    //        Hex fromHex = findHex(fromQ, fromR);
    //        if (fromHex == null) return;
    //
    //        Token token = fromHex.removeToken();
    //        if (fromHex.isEmpty()) {
    //            hexes.remove(fromHex);
    //        }
    //        Hex toHex = findOrCreateHex(toQ, toR);
    //        toHex.addToken(token);
    //        token.setHex(toHex);
    //    }
    //
    //    /**
    //     * Move token
    //     */
    //    public void moveToken(Token token, int toQ, int toR) {
    //        if (token.getHex() != null) {
    //            if (!token.getHex().getTopToken().equals(token)) {
    //                throw new IllegalStateException("Cannot move a token that is not on top of the stack.");
    //            }
    //            Hex hex = token.getHex();
    //            hex.removeToken();
    //            if (hex.isEmpty()) {
    //                hexes.remove(hex);
    //            }
    //        }
    //
    //        Hex toHex = findOrCreateHex(toQ, toR);
    //        toHex.addToken(token);
    //        token.setHex(toHex);
    //     }
    //
    //    /**
    //     * Returns a list of all hexes with 1 or more creatures
    //     */
    //    public List<Hex> getFilledHexes() {
    //        ArrayList<Hex> result = new ArrayList<Hex>();
    //        for (Hex hex : hexes.values()) {
    //            if (!hex.isEmpty()) {
    //                result.add(hex);
    //            }
    //        }
    //
    //        return result;
    //    }
    //
    /**
     * Returns the hex for the given position or create a new if it doesn't exists.
     */
    private fun findOrCreateHex(q: Int, r: Int): Hex {
        // Look through all existing hexes
        var hex = findHex(q, r)
        if (hex != null) return hex

        // Create new hex if needed
        hex = Hex(q, r)
        hexes.put(getKey(q, r), hex)
        return hex
    }

    //
    private fun getKey(q: Int, r: Int): String {
        return "" + q + r
    }
    //
    /**
     * Returns the hex for the given position or null if it doesn't exist or is a border hex.
     */
    fun findHex(q: Int, r: Int): Hex? {
        val hex = hexes.get(getKey(q, r))
        if (hex == null) {
            return null
        } else {
            return hex
        }
    }

    //
    //    /**
    //     * Returns the width of the board in hexes.
    //     */
    //    public int getWidth() {
    //        int minQ = Integer.MAX_VALUE;
    //        int maxQ = Integer.MIN_VALUE;
    //
    //        for (Hex hex : hexes.values()) {
    //            if (hex.getTopToken() == null) continue;
    //            int q = hex.getQ();
    //
    //            if (q < minQ) {
    //                minQ = q;
    //            }
    //
    //            if (q > maxQ) {
    //                maxQ = q;
    //            }
    //        }
    //
    //        return Math.abs(maxQ - minQ) + 1;
    //    }
    //
    //    /**
    //     * Returns the heigh of the board in hexes.
    //     */
    //    public int getHeight() {
    //        int minY = Integer.MAX_VALUE;
    //        int maxY = Integer.MIN_VALUE;
    //
    //        for (Hex hex : hexes.values()) {
    //            if (hex.getTopToken() == null) continue;
    //            int y = hex.getR();
    //
    //            if (y < minY) {
    //                minY = y;
    //            }
    //
    //            if (y > maxY) {
    //                maxY = y;
    //            }
    //        }
    //
    //        return Math.abs(maxY - minY) + 1;
    //    }
    //
    //    /**
    //     * Returns the top left hex or null if board is empty
    //     */
    //    public Hex getTopLeft() {
    //        if (hexes.isEmpty()) return null;
    //
    //        Hex result = null;
    //        for (Hex hex : hexes.values()) {
    //            if (hex.getTopToken() == null) continue;
    //            if (result == null) {
    //                result = hex;
    //                continue;
    //            }
    //
    //            if (hex.getQ() < result.getQ()) {
    //                result = hex;
    //                continue;
    //
    //            } else if (hex.getR() < result.getR()) {
    //                result = hex;
    //            }
    //        }
    //
    //        return result;
    //    }
    //
    //    /**
    //     * Return minimum x coordinate for all hexes.
    //     */
    //    public int getMinQ() {
    //        int result = Integer.MAX_VALUE;
    //        for (Hex hex : hexes.values()) {
    //            if (hex.getTopToken() == null) continue;
    //            int q = hex.getQ();
    //            if (q < result) {
    //                result = q;
    //            }
    //        }
    //
    //        return result;
    //    }
    //
    //    /**
    //     * Return minimum y coordinate for all hexes.
    //     */
    //    public int getMinR() {
    //        int result = Integer.MAX_VALUE;
    //        for (Hex hex : hexes.values()) {
    //            if (hex.getTopToken() == null) continue;
    //            int r = hex.getR();
    //            if (r < result) {
    //                result = r;
    //            }
    //        }
    //
    //        return result;
    //    }
    //
    //    public int getMaxQ() {
    //        int result = Integer.MIN_VALUE;
    //        for (Hex hex : hexes.values()) {
    //            if (hex.getTopToken() == null) continue;
    //            int q = hex.getQ();
    //            if (q > result) {
    //                result = q;
    //            }
    //        }
    //
    //        return result;
    //    }
    //
    //    public int getMaxR() {
    //        int result = Integer.MIN_VALUE;
    //        for (Hex hex : hexes.values()) {
    //            if (hex.getTopToken() == null) continue;
    //            int r = hex.getR();
    //            if (r > result) {
    //                result = r;
    //            }
    //        }
    //
    //        return result;
    //    }
    //
    //    /**
    //     * Returns a list of neighbor tokens
    //     */
    //    public List<Token> getNeighborTokens(Hex centerHex) {
    //        if (centerHex == null) return Collections.emptyList();
    //
    //        List<Token> result = new ArrayList<Token>();
    //        for (int i = 0; i < neighbors.length; i++) {
    //            int[] coordAdjustment = neighbors[i];
    //            int q = centerHex.getQ() + coordAdjustment[0];
    //            int r = centerHex.getR() + coordAdjustment[1];
    //            Hex hex = findHex(q, r);
    //            if (hex != null && hex.getTopToken() != null) {
    //                result.add(hex.getTopToken());
    //            }
    //        }
    //
    //        return result;
    //    }
    //
    //    /**
    //     * Returns a list of neighbor hexes from top and clockwise round.
    //     */
    //    public List<Hex> getNeighborHexes(Hex hex) {
    //        if (hex == null) return Collections.emptyList();
    //        List<Hex> result = new ArrayList<Hex>();
    //        for (int i = 0; i < neighbors.length; i++) {
    //            int[] coordAdjustment = neighbors[i];
    //            int q = hex.getQ() + coordAdjustment[0];
    //            int r = hex.getR() + coordAdjustment[1];
    //            result.add(findOrCreateHex(q, r));
    //        }
    //
    //        return result;
    //    }
    //
    //    public Hex getHex(int q, int r) {
    //        return findOrCreateHex(q, r);
    //    }
    //
    //    /**
    //     * Return the hex that is the clockwise neighbor of the target hex when looking from the starting hex.
    //     * INVARIANT: From and To are neighbors.
    //     */
    //    public Hex getClockwiseHex(Hex from, Hex to, GraphBoard board) {
    //        int qDiff = to.getQ() - from.getQ();
    //        int rDiff = to.getR() - from.getR();
    //
    //        int neighborIndex = 0;
    //        for (int i = 0; i < neighbors.length; i++) {
    //            if (Arrays.equals(new int[] {qDiff, rDiff}, neighbors[i])) {
    //                neighborIndex = i;
    //                break;
    //            }
    //        }
    //
    //        int[] clockwiseNeighborCoordinates = neighbors[(neighborIndex + 1) % neighbors.length];
    //        return board.getHex(from.getQ() + clockwiseNeighborCoordinates[0], from.getR() + clockwiseNeighborCoordinates[1]);
    //    }
    //
    //    /**
    //     * Return the hex that is the counter clockwise neighbor of the target hex when looking from the starting hex.
    //     * INVARIANT: From and To are neighbors.
    //     */
    //    public Hex getCounterClockwiseHex(Hex from, Hex to, GraphBoard board) {
    //        int qDiff = to.getQ() - from.getQ();
    //        int rDiff = to.getR() - from.getR();
    //
    //        int neighborIndex = 0;
    //        for (int i = 0; i < neighbors.length; i++) {
    //            if (Arrays.equals(new int[] {qDiff, rDiff}, neighbors[i])) {
    //                neighborIndex = i;
    //                break;
    //            }
    //        }
    //
    //        int[] clockwiseNeighborCoordinates = neighbors[(neighborIndex + neighbors.length - 1) % neighbors.length];
    //        return board.getHex(from.getQ() + clockwiseNeighborCoordinates[0], from.getR() + clockwiseNeighborCoordinates[1]);
    //    }
    //
    //    /**
    //     * Get list of empty hexes around a hex.
    //     */
    //    public List<Hex> getEmptyNeighborHexes(Token token) {
    //        List<Hex> result = new ArrayList<Hex>();
    //        for (Hex hex : getNeighborHexes(token.getHex())) {
    //            if (hex.isEmpty()) {
    //                result.add(hex);
    //            }
    //        }
    //
    //        return result;
    //    }
    //
    //    public void clear() {
    //        hexes.clear();
    //    }
    //
    //    /**
    //     * Returns the zobrist key for the given board layout.
    //     * Due to the nature of Hive boards, which move freely and can be expanded in all directions, this makes it
    //     * hard to make zobrist keys that also identify identical shifted or rotated positions.
    //     *
    //     * However as the search depth is never very big, it is not possible to shift or rotate a hive to another location
    //     * within the given search time. At least not for any interesting sized hives.
    //     *
    //     * Having zobrist keys that z(b) = z(rotate(b)) or z(b) = z(shift(b)) would however be an interesting optimization.
    //     *
    //     */
    //    public int getZobristKey() {
    //        return hashCode();
    //    }
    /**
     * Class mapping
     */
    private inner class Node {
        var node: Hex? = null
        var edges: Array<Node?> = arrayOfNulls<Node>(6)
    }

    /**
     * Bidirectional edge
     */
    private inner class Edge {
        var node1: Node? = null
        var node2: Node? = null //        @Override
        //        public boolean equals(Object o) {
        //            if (!(o instanceof Edge)) return false;
        //            Edge otherEdge = (Edge) o;
        //            return (node1.equals(otherEdge.node1) && node2.equals(otherEd))
        //
        //        }
    }
}
