package dk.ilios.hivemind.model.rules

import dk.ilios.hivemind.model.*


class Rules private constructor() {
    private val pillbug: PillBug

    private val bee: QueenBee
    private val beetle: Beetle
    private val ant: SoldierAnt
    private val spider: Spider
    private val grasshopper: Grasshopper
    private val ladyBug: LadyBug
    private val mosquito: Mosquito
    private val unknownBug: UnknownBug

    init {
        ant = SoldierAnt()
        bee = QueenBee()
        beetle = Beetle()
        spider = Spider()
        grasshopper = Grasshopper()
        ladyBug = LadyBug()
        mosquito = Mosquito()
        pillbug = PillBug()
        unknownBug = UnknownBug()
    }

    fun isFreeToMove(token: Token, board: Board): Boolean {
        // 1. Pick a random neighbor and mark it.
        // 2. Visit all neighbors recursively (ignoring startGame) and mark them
        // 3. When marking is done. Count marked. Must be equal to hexes - 1 to be a consistent hive

        val neighbors = board.getNeighborTokens(token.hex)
        if (neighbors.isEmpty()) return false

        val startToken = neighbors[0]
        val marked = mark(startToken, token, board, hashSetOf<Token>())
        if (marked.size != board.filledHexes.size - 1) {
            return false // Marked set doesn't match whole board -> Disconnected hive
        }

        // Futher restrict based on type (Mosquito mimics other types)
        val bug = getBugSpecificRules(token)
        return bug.isFreeToMove(token, board)
    }

    /**
     * Recursively mark all tokens connected to a token
     * @return The set of all tokens connected
     */
    private fun mark(startToken: Token, ignore: Token?, board: Board, marked: MutableSet<Token>): Set<Token> {
        marked.add(startToken)

        val neighbors = board.getNeighborTokens(startToken.hex)
        for (t in neighbors) {
            if (t == ignore) continue
            if (marked.contains(t)) continue
            mark(t, ignore, board, marked)
        }

        return marked
    }

    fun getTargetHexes(token: Token, board: Board): List<Hex> {
        val bug = getBugSpecificRules(token)
        return bug.getTargetHexes(token, board)
    }

    /**
     * Get list of hexes a new pieces can be added to.
     *
     * @param player
     * @param board
     */
    fun getStartHexes(player: Player?, board: Board): List<Hex> {
        // Start

        if (board.filledHexes.isEmpty()) {
            return listOf(board.getHex(0, 0))
        }

        // Start for 2nd player
        if (board.filledHexes.size == 1) {
            return board.getNeighborHexes(board.filledHexes[0])
        }

        val result: MutableList<Hex> = mutableListOf<Hex>()
        val targets: MutableSet<Hex> = hashSetOf<Hex>()

        // 1. Get all empty neighbor hexes to player tokens.
        val hexes = board.filledHexes
        for (hex in hexes) {
            if (hex.topToken!!.player == player) {
                val neighbors = board.getNeighborHexes(hex)
                for (h in neighbors) {
                    if (h.isEmpty) {
                        targets.add(h)
                    }
                }
            }
        }

        // 2. For each empty hex, check colors of neighbor hexes
        // 3. If only 1 color, add to startGame hex set.
        for (hex in targets) {
            val neighbors = board.getNeighborHexes(hex)
            var isTarget = true
            for (h in neighbors) {
                if (h.isEmpty) continue
                val p = h.topToken!!.player
                if (p !== player) {
                    isTarget = false
                    break // different token colors, ignore
                }
            }

            if (isTarget) {
                result.add(hex)
            }
        }
        return result
    }

    /**
     * Returns true if a token can slide between the two hexes.
     * IF the "One Hive" rule must be maintained during the slide,
     * either the left or right hex must be filled, but if both are filled
     * they block instead.
     *
     * INVARIANT: Hexes are assumed to be neighbors.
     *
     * @param from Hex to move from.
     * @param to Hex to move to.
     * @return True if a token can slide to the new hex.
     */
    fun canSlideTo(from: Hex, to: Hex, board: Board): Boolean {
        val left = board.getClockwiseHex(from, to, board)
        val right = board.getCounterClockwiseHex(from, to, board)

        if (from.height == 1 && to.isEmpty) {
            // Slide at ground level
            val followRightSide = left.isEmpty && !right.isEmpty
            val followLeftSide = !left.isEmpty && right.isEmpty
            return followLeftSide || followRightSide
        } else if (from.height > 1 && to.height < from.height) {
            // Slide on top of hive
            if (left.height > from.height && right.height > from.height) {
                return false
            } else {
                return true
            }
        } else {
            return false
        }
    }

    /**
     * Returns true if a token can crawl up on a target token.
     * @param from Hex to move from.
     * @param to Hex to move to.
     * @return True if a token can crawl up on the new hex.
     */
    fun canCrawlUp(from: Hex, to: Hex, board: Board): Boolean {
        val left = board.getClockwiseHex(from, to, board)
        val right = board.getCounterClockwiseHex(from, to, board)

        val isBlocked =
            (from.height - 1) < left.height && (from.height - 1) < right.height && to.height < left.height && to.height < right.height

        return !isBlocked
    }

    /**
     * Returns true if a token can crawl down from top the hive to ground level.
     * @param from Hex to move from.
     * @param to Hex to move to.
     * @return True if a token can crawl up on the new hex.
     */
    fun canCrawlDown(from: Hex, to: Hex, board: Board): Boolean {
        if (from.height < 2 || to.height > 0) return false

        val left = board.getClockwiseHex(from, to, board)
        val right = board.getCounterClockwiseHex(from, to, board)

        return left.height < from.height || right.height < from.height
    }


    /**
     * Returns all tokens free to move for the given player
     */
    fun getFreeTokens(p: Player?, board: Board): Set<Token> {
        val result: MutableSet<Token> = hashSetOf<Token>()

        val hexes = board.filledHexes
        for (hex in hexes) {
            if (hex.isEmpty) continue
            val token = checkNotNull(hex.topToken)
            if (token.player == p) {
                if (isFreeToMove(token, board)) {
                    result.add(token)
                }
            }
        }

        return result
    }

    /**
     * Returns true if moving a hex from A to B maintains the "One Hive" rule.
     * Ie. by induction this is always true if any neighbor have a token.
     */
    fun isOneHiveIntact(a: Hex?, b: Hex, board: Board): Boolean {
        var i = b.height // Crawling on top of of the hive still maintains the "One Hive" rule
        val neighbors = board.getNeighborTokens(b)
        for (t in neighbors) {
            if (t.hex != a) {
                i++
            }
        }

        return i > 0
    }

    fun isQueenSurrounded(player: Player, board: Board): Boolean {
        val queen = player.queen
        val hex = queen!!.hex
        if (hex != null) {
            return board.getNeighborTokens(hex).size == 6
        } else {
            return false
        }
    }

    /**
     * A bug tries to mimic the movement characteristics of another bug.
     * Returns true, if successful, false otherwise.
     */
    fun mimicBug(copyTo: Token, copyFrom: Token, board: Board): Boolean {
        val bug = getBugSpecificRules(copyTo)
        check(bug.canMimic()) { copyTo.originalType.toString() + " cannot mimic other bugs." }

        // Always mimic BEETLE when atop the hive
        if (copyTo.hex!!.height > 1) {
            copyTo.mimic(BugType.BEETLE)
            return true
        } else {
            val targets = getMimicList(copyTo, board)
            if (!targets.contains(copyFrom)) return false // Only allowed to copy from legal bugs

            copyTo.mimic(copyFrom.originalType)
            return true
        }
    }

    /**
     * Returns a list of possible targets for mimicing.
     * When atop the hive, it cannot copy anything (it is forced to be a beetle), and this returns no result
     *
     * @param token Token that wants to mimic nearby bugs.
     * @param board Current board
     */
    fun getMimicList(token: Token, board: Board): List<Token> {
        if (token.hex!!.height > 1) {
            return emptyList()
        } else {
            return board.getNeighborTokens(token.hex)
        }
    }

    fun getBugSpecificRules(token: Token): Bug {
        val type = token.type
        when (type) {
            BugType.UNKNOWN -> return unknownBug
            BugType.PILL_BUG -> return pillbug
            BugType.MOSQUITO -> return mosquito
            BugType.LADY_BUG -> return ladyBug
            BugType.GRASSHOPPER -> return grasshopper
            BugType.SPIDER -> return spider
            BugType.SOLDIER_ANT -> return ant
            BugType.QUEEN_BEE -> return bee
            BugType.BEETLE -> return beetle
        }

        return unknownBug
    }

    companion object {
        val instance: Rules by lazy { Rules() }
    }
}
