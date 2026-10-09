package dk.ilios.hivemind.model

import kotlin.math.abs

/**
 * Class representing a Hive hexagonal Board.
 * As Hive doesn't have any "real" board, the board is defined from the starting piece with gets position (0,0).
 * The rest of the board is described by a 2 dimensional grid system.
 *
 * The hex grid uses a trapezoidal or axial coordinate system, like so:
 *
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
class BoardBackup {
    var hexes: MutableMap<String?, Hex> =
        HashMap<String?, Hex>() // Key := (q,r), Value: hex. List of hexes visited in the game
    var neighbors: Array<IntArray> = arrayOf(
        intArrayOf(0, -1),
        intArrayOf(+1, -1),
        intArrayOf(1, 0),
        intArrayOf(0, 1),
        intArrayOf(-1, +1),
        intArrayOf(-1, 0)
    )

    // Canonical rotation, so the first 2 pieces always determine how the board is "rotated"
    // This means that boards always start with: W(0,0) and B(1,0).
    var canonicalRotationQ: Int = 1
    var canonicalRotationR: Int = 1

    @JvmField
    var zobristKey: Int = 0


    //    int[][][] edgeHash = new int[200][200][6][2]; // Zobrist dimensions: // q, r, type, color
    /**
     * Add a token to the board.
     */
    fun addToken(token: Token?, q: Int, r: Int) {
        if (token == null) return
        val hex = findOrCreateHex(q, r)
        hex.addToken(token)
        token.hex = hex
        token.player.removeFromSupply(token)
    }


    /**
     * Removes a token from the board and puts in back into the players supply.
     */
    fun removeToken(q: Int, r: Int) {
        val hex = findOrCreateHex(q, r)
        val token = hex.removeToken()
        token.hex = null
        token.player.addToSupply(token)
    }


    /**
     * Move the top creature from hex to another.
     */
    fun moveToken(fromQ: Int, fromR: Int, toQ: Int, toR: Int) {
        val fromHex = findHex(fromQ, fromR)
        if (fromHex == null) return

        val token = fromHex.removeToken()
        val toHex = findOrCreateHex(toQ, toR)
        toHex.addToken(token)
        token.hex = toHex
    }

    /**
     * Move token
     */
    fun moveToken(token: Token, toQ: Int, toR: Int) {
        val hex = token.hex
        if (hex != null) {
            check(hex.topToken == token) { "Cannot move a token that is not on top of the stack." }
            hex.removeToken()
        }

        val toHex = findOrCreateHex(toQ, toR)
        toHex.addToken(token)
        token.hex = toHex
    }

    val filledHexes: MutableList<Hex>
        /**
         * Returns a list of all hexes with 1 or more creatures
         */
        get() {
            val result =
                ArrayList<Hex>()
            for (hex in hexes.values) {
                if (!hex.isEmpty) {
                    result.add(hex)
                }
            }

            return result
        }

    /**
     * Returns the hex for the given position or create a new if it doesn't exists
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

    private fun getKey(q: Int, r: Int): String {
        return "" + q + r
    }

    /**
     * Returns the hex for the given position or null it no creatures reside there.
     */
    fun findHex(q: Int, r: Int): Hex? {
        val hex = hexes.get(getKey(q, r))
        if (hex == null) {
            return null
        } else {
            return hex
        }
    }

    val width: Int
        /**
         * Returns the width of the board in hexes.
         */
        get() {
            var minQ = Int.MAX_VALUE
            var maxQ = Int.MIN_VALUE

            for (hex in hexes.values) {
                if (hex.topToken == null) continue
                val q = hex.q

                if (q < minQ) {
                    minQ = q
                }

                if (q > maxQ) {
                    maxQ = q
                }
            }

            return abs(maxQ - minQ) + 1
        }

    val height: Int
        /**
         * Returns the heigh of the board in hexes.
         */
        get() {
            var minY = Int.MAX_VALUE
            var maxY = Int.MIN_VALUE

            for (hex in hexes.values) {
                if (hex.topToken == null) continue
                val y = hex.r

                if (y < minY) {
                    minY = y
                }

                if (y > maxY) {
                    maxY = y
                }
            }

            return abs(maxY - minY) + 1
        }

    val topLeft: Hex?
        /**
         * Returns the top left hex or null if board is empty
         */
        get() {
            if (hexes.isEmpty()) return null

            var result: Hex? = null
            for (hex in hexes.values) {
                if (hex.topToken == null) continue
                if (result == null) {
                    result = hex
                    continue
                }

                if (hex.q < result.q) {
                    result = hex
                    continue
                } else if (hex.r < result.r) {
                    result = hex
                }
            }

            return result
        }

    val minQ: Int
        /**
         * Return minimum x coordinate for all hexes.
         */
        get() {
            var result = Int.MAX_VALUE
            for (hex in hexes.values) {
                if (hex.topToken == null) continue
                val q = hex.q
                if (q < result) {
                    result = q
                }
            }

            return result
        }

    val minR: Int
        /**
         * Return minimum y coordinate for all hexes.
         */
        get() {
            var result = Int.MAX_VALUE
            for (hex in hexes.values) {
                if (hex.topToken == null) continue
                val r = hex.r
                if (r < result) {
                    result = r
                }
            }

            return result
        }

    val maxQ: Int
        get() {
            var result = Int.MIN_VALUE
            for (hex in hexes.values) {
                if (hex.topToken == null) continue
                val q = hex.q
                if (q > result) {
                    result = q
                }
            }

            return result
        }

    val maxR: Int
        get() {
            var result = Int.MIN_VALUE
            for (hex in hexes.values) {
                if (hex.topToken == null) continue
                val r = hex.r
                if (r > result) {
                    result = r
                }
            }

            return result
        }

    /**
     * Returns a list of neighbor tokens
     */
    fun getNeighborTokens(centerHex: Hex?): MutableList<Token> {
        if (centerHex == null) return mutableListOf<Token>()

        val result: MutableList<Token> = ArrayList<Token>()
        for (i in neighbors.indices) {
            val coordAdjustment = neighbors[i]
            val q = centerHex.q + coordAdjustment[0]
            val r = centerHex.r + coordAdjustment[1]
            val hex = findHex(q, r)
            hex?.topToken?.let { result.add(it) }
        }

        return result
    }

    /**
     * Returns a list of neighbor hexes from top and clockwise round.
     */
    fun getNeighborHexes(hex: Hex?): MutableList<Hex> {
        if (hex == null) return mutableListOf<Hex>()
        val result: MutableList<Hex> = ArrayList<Hex>()
        for (i in neighbors.indices) {
            val coordAdjustment = neighbors[i]
            val q = hex.q + coordAdjustment[0]
            val r = hex.r + coordAdjustment[1]
            result.add(findOrCreateHex(q, r))
        }

        return result
    }

    fun getHex(q: Int, r: Int): Hex {
        return findOrCreateHex(q, r)
    }

    /**
     * Return the hex that is the clockwise neighbor of the target hex when looking from the starting hex.
     * INVARIANT: From and To are neighbors.
     */
    fun getClockwiseHex(from: Hex, to: Hex, board: BoardBackup): Hex {
        val qDiff = to.q - from.q
        val rDiff = to.r - from.r

        var neighborIndex = 0
        for (i in neighbors.indices) {
            if (intArrayOf(qDiff, rDiff).contentEquals(neighbors[i])) {
                neighborIndex = i
                break
            }
        }

        val clockwiseNeighborCoordinates = neighbors[(neighborIndex + 1) % neighbors.size]
        return board.getHex(
            from.q + clockwiseNeighborCoordinates[0],
            from.r + clockwiseNeighborCoordinates[1]
        )
    }

    /**
     * Return the hex that is the counter clockwise neighbor of the target hex when looking from the starting hex.
     * INVARIANT: From and To are neighbors.
     */
    fun getCounterClockwiseHex(from: Hex, to: Hex, board: BoardBackup): Hex {
        val qDiff = to.q - from.q
        val rDiff = to.r - from.r

        var neighborIndex = 0
        for (i in neighbors.indices) {
            if (intArrayOf(qDiff, rDiff).contentEquals(neighbors[i])) {
                neighborIndex = i
                break
            }
        }

        val clockwiseNeighborCoordinates = neighbors[(neighborIndex + neighbors.size - 1) % neighbors.size]
        return board.getHex(
            from.q + clockwiseNeighborCoordinates[0],
            from.r + clockwiseNeighborCoordinates[1]
        )
    }

    /**
     * Get list of empty hexes around a hex.
     */
    fun getEmptyNeighborHexes(token: Token): MutableList<Hex> {
        val result: MutableList<Hex> = ArrayList<Hex>()
        for (hex in getNeighborHexes(token.hex)) {
            if (hex.isEmpty) {
                result.add(hex)
            }
        }

        return result
    }

    fun clear() {
        hexes.clear()
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
    fun getZobristKey(): Int {
        return hashCode()
    }
}
