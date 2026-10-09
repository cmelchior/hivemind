package dk.ilios.hivemind.model

import dk.ilios.hivemind.debug.HiveAsciiPrettyPrinter
import java.util.*
import kotlin.math.abs

/**
 * Class representing a Hive hexagonal Board.
 *
 * As Hive doesn't have any "real" board, the board is defined from the starting piece with gets position (0,0).
 * The rest of the board is described by a 2 dimensional grid system.
 *
 * The hex grid uses a trapezoidal or axial coordinate system [2], like so:
 *
 * _ _
 * /     \
 * _ _ /(0,-1) \ _ _
 * /     \  -r   /     \
 * /(-1,0) \ _ _ /(1,-1) \
 * \  -q   /     \       /
 * \ _ _ / (0,0) \ _ _ /
 * /     \       /     \
 * /(-1,1) \ _ _ / (1,0) \
 * \       /     \  +q   /
 * \ _ _ / (0,1) \ _ _ /
 * \  +r   /
 * \ _ _ /
 *
 *
 * If enabled, the board is maintained in "Standard Position" (SP), which is defined by the following properties:
 * - White Queen Bee is always (0,0)
 * - Black Queen coordinates must be min(R) <= 0 and 0 < Q < MAX_Q. Rotate the board clockwise until this is true.
 * This is effectively a place on the Q axis or if not able to land on R = 0, the last rotation before crossing, so
 * R > 0.
 *
 * Before both queens are placed, SP is defined slightly different. In that case we adopt a small variant of the SP
 * definition in Randell Ringersolls book [1]. The only difference is that we always use the 3rd / 4th token even though
 * they might not be queens (although it usually will be the queen anyway).
 *
 * - 1st token (white) is placed at (0,0).
 * - 2nd token (black) is placed at (1,0).
 * - 3rd token (white) if R > 0, flip board around Q axis, so R < 0.
 * - 4th token (black) if 3rd token has R = 0 and 4th has R > 0. Flip around Q axis so R < 0.
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
 *
 * @see [2] http://www.redblobgames.com/grids/hexagons/
 *
 * @see [3] http://en.wikipedia.org/wiki/Zobrist_hashing
 */
class Board(white: Player, black: Player) {
    private val printer = HiveAsciiPrettyPrinter()

    private val hexes: MutableMap<String?, Hex> =
        HashMap<String?, Hex>() // Key := (q,r), Value: hex. List of hexes visited in the game
    private val tokens: MutableSet<Token> = HashSet<Token>()
    private val neighbors: Array<IntArray> = arrayOf(
        intArrayOf(0, -1),
        intArrayOf(+1, -1),
        intArrayOf(1, 0),
        intArrayOf(0, 1),
        intArrayOf(-1, +1),
        intArrayOf(-1, 0)
    ) // From top and clockwise round.

    // Standard position variabels
    private val whitePlayer: Player
    private val blackPlayer: Player

    private var standardPosition: StandardPositionMode? =
        StandardPositionMode.DISABLED // Type of Standard Position used
    private var spQFlip = false // Flip around the Q axis
    private var spRotation = 0 // How many clockwise rotations are needed to achieve Standard Position
    private var spOrigin = IntArray(2) // Displacement of origin
    private val firstTokens =
        arrayOfNulls<Token>(5) // Keep track of the first 4 tokens placed on the board. 2 white and 2 black

    /**
     * Returns the zobrist key for the given board layout.
     */
    // The board state is hashed as a Zobrist key.
    var zobristKey: Long = 0

    // See [2] for details about storing hexagon maps.
    // Maximum size is 26 tokens in each directions that can be stacked 7 high.
    // This is not entirely true, but for simplicity we just use that as a first implementation
    // q, r, height, color, token type
    private val zobristHashes: Array<Array<Array<Array<LongArray>>>> = Array<Array<Array<Array<LongArray>>>>(51) {
        Array<Array<Array<LongArray>>>(51) {
            Array<Array<LongArray>>(7) { Array<LongArray>(2) { LongArray(8) } }
        }
    }

    init {
        this.whitePlayer = white
        this.blackPlayer = black
    }

    private fun loadZobristHashes() {
        val random = Random()

        // Create the zobrist hashes needed for all edges
        // Is it possible to convert this to a recursive call?
        for (i in zobristHashes.indices) {
            val d1 = zobristHashes[0]
            for (j in d1.indices) {
                val d2 = d1[j]
                for (k in d2.indices) {
                    val d3 = d2[k]
                    for (l in d3.indices) {
                        val d4 = d3[l]
                        for (m in d4.indices) {
                            zobristHashes[i][j][k][l][m] = random.nextLong()
                        }
                    }
                }
            }
        }
    }

    /**
     * Add a token to the board.
     */
    fun addToken(token: Token?, q: Int, r: Int) {
        if (token == null) return
        val hex = findOrCreateHex(q, r)
        hex.addToken(token)
        token.hex = hex
        token.player.removeFromSupply(token)
        tokens.add(token)

        updateZobristKey(token)

        // Keep track of first 4 tokens
        if (tokens.size < 6) {
            firstTokens[tokens.size - 1] = token
        }
        maintainStandardPosition(token)
    }

    private fun maintainStandardPosition(token: Token) {
        if (standardPosition == StandardPositionMode.DISABLED) return
        val rebuildZobristKey: Boolean
        if (bothQueensPlaced() && standardPosition == StandardPositionMode.ENABLED) {
            rebuildZobristKey = maintainSPForMidGame(token)
        } else {
            rebuildZobristKey = maintainSPForOpenings(token)
        }

        if (rebuildZobristKey) {
            rebuildZobristKey()
        }
    }

    private fun bothQueensPlaced(): Boolean {
        return !whitePlayer.queen!!.inSupply() && !blackPlayer.queen!!.inSupply()
    }

    // INVARIANT: All tokens are placed in a legal position
    private fun maintainSPForOpenings(token: Token): Boolean {
        var rebuildZobristKey = false

        if (firstTokens[4] != null) {
            // Should only be called if standardPostion = StandardPostionMode.LIMITED
            // This only acts as sentinel.
            return false
        }
        if (firstTokens[3] != null) {
            // TURN 4+, after turn 4 pieces can move
            rebuildZobristKey = moveOrigin(firstTokens[0]!!) || rebuildZobristKey
            rebuildZobristKey = rotateToStandardPosition(firstTokens[1]!!) || rebuildZobristKey
            rebuildZobristKey = swapAxisIfNeededForThirdToken() || rebuildZobristKey
            rebuildZobristKey = swapAxisIfNeededForFourthToken() || rebuildZobristKey
        } else if (firstTokens[2] != null) {
            // TURN 3
            rebuildZobristKey = moveOrigin(firstTokens[0]!!) || rebuildZobristKey
            rebuildZobristKey = rotateToStandardPosition(firstTokens[1]!!) || rebuildZobristKey
            rebuildZobristKey = swapAxisIfNeededForThirdToken() || rebuildZobristKey
        } else if (firstTokens[1] != null) {
            // TURN 2
            rebuildZobristKey = moveOrigin(firstTokens[0]!!) || rebuildZobristKey
            rebuildZobristKey = rotateToStandardPosition(firstTokens[1]!!) || rebuildZobristKey
        } else if (firstTokens[0] != null) {
            // TURN 1
            rebuildZobristKey = moveOrigin(token) || rebuildZobristKey
        } else {
            throw IllegalStateException("Board is empty")
        }

        return rebuildZobristKey
    }

    private fun swapAxisIfNeededForThirdToken(): Boolean {
        val coords = getRotatedSPCoordinatesFor(checkNotNull(firstTokens[2]!!.hex))
        val oldFlip = spQFlip
        // R must be negative for 3rd token to be in SP
        if (coords[1] > 0) {
            spQFlip = true
        } else {
            spQFlip = false
        }

        return oldFlip != spQFlip
    }

    private fun swapAxisIfNeededForFourthToken(): Boolean {
        var coords = getSPCoordinatesFor(checkNotNull(firstTokens[2]!!.hex))
        val oldFlip = spQFlip

        // If R == 0 for 3rd piece, R must be negative for 4th piece to be in SP
        if (coords[1] == 0) {
            // Only check rotation if 3rd is inline with the rest
            coords = getSPCoordinatesFor(checkNotNull(firstTokens[3]!!.hex))
            if (coords[1] > 0) {
                spQFlip = true
            } else {
                spQFlip = false
            }
        }

        return oldFlip != spQFlip
    }

    private fun maintainSPForMidGame(token: Token?): Boolean {
        val oldFlip = spQFlip
        var rebuildZobristKey = false

        spQFlip = false
        rebuildZobristKey = moveOrigin(checkNotNull(whitePlayer.queen)) || rebuildZobristKey
        rebuildZobristKey = rotateToStandardPosition(checkNotNull(blackPlayer.queen)) || rebuildZobristKey

        return rebuildZobristKey || oldFlip != spQFlip
    }

    private fun moveOrigin(token: Token): Boolean {
        check(!token.inSupply()) { "Cannot recenter around token in supply: " + token }
        val hex = token.hex
        val rebuildZobristKey = spOrigin[0] != hex!!.q || spOrigin[1] != hex.r
        spOrigin[0] = token.hex!!.q
        spOrigin[1] = token.hex!!.r
        return rebuildZobristKey
    }

    // Rebuild Zobrist key when Standard Position changes
    private fun rebuildZobristKey() {
        zobristKey = 0
        for (t in tokens) {
            updateZobristKey(t)
        }
    }

    // Rotate to last available space before crossing the positive Q axis.
    // Return true if rotation has changed so Zobrist key needs to be rebuild.
    private fun rotateToStandardPosition(token: Token): Boolean {
        if (token.hex!!.q == spOrigin[0] && token.hex!!.r == spOrigin[1]) {
            // Very special case, can happen if a Beetle working as Blacks anchor point, move on top of White's origin.
            // Just keep current SP in that case.
            return false
        }

        val oldRotation = spRotation
        var sp = getSPCoordinatesFor(checkNotNull(token.hex))
        var maxRotations = 6
        while (!(sp[0] >= 0 && sp[1] >= 1)) {  // q >= 0 && r >= 1
            if (maxRotations == 0) {
                printer.print(this)
                throw IllegalStateException("Keeps rotating: Cannot find Standard Position")
            }
            rotateClockwise()
            sp = getRotatedSPCoordinatesFor(checkNotNull(token.hex))
            maxRotations--
        }

        rotateCounterClockwise()
        return spRotation != oldRotation
    }

    private fun rotateClockwise() {
        spRotation = (spRotation + 1) % 6
    }

    private fun rotateCounterClockwise() {
        spRotation--
        if (spRotation < 0) {
            spRotation = 5
        }
    }

    fun getSPCoordinatesFor(hex: Hex): IntArray {
        return getSPCoordinatesFor(hex.q, hex.r)
    }

    fun getSPCoordinatesFor(q: Int, r: Int): IntArray {
        var cubeCoords = HexagonUtils.convertToCubeCoordinates(q - spOrigin[0], r - spOrigin[1])

        for (i in 0..<spRotation) {
            cubeCoords = HexagonUtils.rotateRight(cubeCoords)
        }

        if (spQFlip) {
            val newX = -cubeCoords[1]
            val newY = -cubeCoords[0]
            val newZ = -cubeCoords[2]
            cubeCoords[0] = newX
            cubeCoords[1] = newY
            cubeCoords[2] = newZ
        }

        return HexagonUtils.convertToAxialCoordinates(cubeCoords[0], cubeCoords[1], cubeCoords[2])
    }

    /**
     * Returns the SP coordinates, only taking rotation into account
     */
    private fun getRotatedSPCoordinatesFor(hex: Hex): IntArray {
        val q = hex.q
        val r = hex.r
        var cubeCoords = HexagonUtils.convertToCubeCoordinates(q - spOrigin[0], r - spOrigin[1])
        for (i in 0..<spRotation) {
            cubeCoords = HexagonUtils.rotateRight(cubeCoords)
        }
        return HexagonUtils.convertToAxialCoordinates(cubeCoords[0], cubeCoords[1], cubeCoords[2])
    }


    /**
     * Returns the hex for the given Standard Position
     *
     * @param q Q coordinate in Standard Position
     * @param r R coordinate in Standard Position
     */
    fun getHexForStandardPosition(q: Int, r: Int): Hex {
        var cubeCoords = HexagonUtils.convertToCubeCoordinates(q, r)

        if (spQFlip) {
            val newX = -cubeCoords[1]
            val newY = -cubeCoords[0]
            val newZ = -cubeCoords[2]
            cubeCoords[0] = newX
            cubeCoords[1] = newY
            cubeCoords[2] = newZ
        }

        for (i in 0..<(6 - spRotation)) {
            cubeCoords = HexagonUtils.rotateRight(cubeCoords)
        }

        val originalCoords = HexagonUtils.convertToAxialCoordinates(cubeCoords[0], cubeCoords[1], cubeCoords[2])
        return findOrCreateHex(originalCoords[0] + spOrigin[0], originalCoords[1] + spOrigin[1])
    }

    /**
     * Enable standard position for the board. Can only be enabled when the board is empty.
     * @param mode
     */
    fun setStandardPositionMode(mode: StandardPositionMode?) {
        check(!(tokens.size > 0 && mode != StandardPositionMode.DISABLED)) { "Cannot enable Standard Position for non-empty board" }
        this.standardPosition = mode
        if (mode != StandardPositionMode.DISABLED) {
            loadZobristHashes()
            spOrigin = IntArray(2)
            spRotation = 0
            spQFlip = false
        }
    }

    /**
     * Updating the Zobrist key works by XOR, so calling it once, will insert a token, calling it twice will remove it.
     */
    private fun updateZobristKey(token: Token?) {
        if (token == null || token.hex == null) return

        val tokenHex = checkNotNull(token.hex)
        val typeIndex: Int
        when (token.originalType) {
            BugType.QUEEN_BEE -> typeIndex = 0
            BugType.BEETLE -> typeIndex = 1
            BugType.GRASSHOPPER -> typeIndex = 2
            BugType.SPIDER -> typeIndex = 3
            BugType.SOLDIER_ANT -> typeIndex = 4
            BugType.MOSQUITO -> typeIndex = 5
            BugType.LADY_BUG -> typeIndex = 6
            BugType.PILL_BUG -> typeIndex = 7
            else -> throw IllegalStateException("Unknown token: " + token)
        }

        val coords = getSPCoordinatesFor(tokenHex)
        zobristKey =
            zobristKey xor zobristHashes[coords[0] + 25][coords[1] + 25][tokenHex.height - 1][getColorIndex(token.player)][typeIndex]
    }

    private fun getColorIndex(player: Player): Int {
        return if (player.isWhitePlayer) 0 else 1
    }

    /**
     * Removes a token from the board and puts in back into the players supply.
     *
     * INVARIANT: Can only be removed in the order they where added.
     */
    fun removeToken(q: Int, r: Int) {
        val hex = findOrCreateHex(q, r)
        updateZobristKey(hex.topToken)
        val token = hex.removeToken()
        token.hex = null
        token.player.addToSupply(token)
        tokens.remove(token)

        // Maintain tracking of first 4 tokens.
        if (tokens.size < 5) {
            firstTokens[tokens.size] = null
        }

        if (tokens.size == 0) {
            spOrigin[0] = 0
            spOrigin[1] = 0
        }
    }


    /**
     * Move token
     */
    fun moveToken(token: Token, toQ: Int, toR: Int) {
        val hex = token.hex
        if (hex != null) {
            check(hex.topToken == token) { "Cannot move a token that is not on top of the stack." }
            updateZobristKey(token) // Remove current position
            hex.removeToken()
        }

        val toHex = findOrCreateHex(toQ, toR)
        toHex.addToken(token)
        token.hex = toHex
        updateZobristKey(token) // Add new position
    }


    /**
     * Move the top creature from hex to another.
     */
    fun moveToken(fromQ: Int, fromR: Int, toQ: Int, toR: Int) {
        val fromHex = findHex(fromQ, fromR)
        if (fromHex == null) return

        updateZobristKey(fromHex.topToken) // Remove current position
        val token = fromHex.removeToken()
        val toHex = findOrCreateHex(toQ, toR)
        toHex.addToken(token)
        token.hex = toHex
        maintainStandardPosition(token)
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
                val q = if (this.isUsingStandardPosition) getSPCoordinatesFor(hex)[0] else hex.q
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
                val r = if (this.isUsingStandardPosition) getSPCoordinatesFor(hex)[1] else hex.r
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
                val q = if (this.isUsingStandardPosition) getSPCoordinatesFor(hex)[0] else hex.q
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
                val r = if (this.isUsingStandardPosition) getSPCoordinatesFor(hex)[1] else hex.r
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
    fun getClockwiseHex(from: Hex, to: Hex, board: Board): Hex {
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
    fun getCounterClockwiseHex(from: Hex, to: Hex, board: Board): Hex {
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

    val isUsingStandardPosition: Boolean
        get() = (standardPosition != StandardPositionMode.DISABLED)
}
