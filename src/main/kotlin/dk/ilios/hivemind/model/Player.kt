package dk.ilios.hivemind.model

import dk.ilios.hivemind.game.CommandProvider
import java.util.*

/**
 * Describes a player
 */
class Player(name: String?, type: PlayerType?) {
    enum class PlayerType {
        UNKNOWN, BLACK, WHITE
    }

    var commandProvider: CommandProvider? = null

    val name: String?
    var ranking: Int = 0

    val type: PlayerType?
    var moves: Int = 0 // Number of moves made in the game
        private set
    val passes: Int = 0 // Number of passes made in the game

    var queen: Token? = null
        private set
    private val bugs: MutableMap<String?, Token?> = HashMap<String?, Token?>()
    private val supplyCreatureCounter: MutableMap<BugType?, Int?> =
        HashMap<BugType?, Int?>() // Easy lookup to determine number of tokens of a specific type in the supply.

    /**
     * Return available tokens
     */
    val supply: MutableSet<Token> = HashSet<Token>() // Tokens in the supply

    var playTime: Long = 0 // How much time has the player used so far
        private set

    init {
        this.name = name
        this.type = type
    }

    fun fillBaseSupply() {
        addToSupply(BugType.QUEEN_BEE, 1)
        addToSupply(BugType.SPIDER, 2)
        addToSupply(BugType.GRASSHOPPER, 3)
        addToSupply(BugType.SOLDIER_ANT, 3)
        addToSupply(BugType.BEETLE, 2)
    }

    fun useAllExpansions() {
        useMosquitoExpansion()
        useLadyBugExpansion()
        usePillBugExpansion()
    }

    fun useMosquitoExpansion() {
        addToSupply(BugType.MOSQUITO, 1)
    }

    fun useLadyBugExpansion() {
        addToSupply(BugType.LADY_BUG, 1)
    }

    fun usePillBugExpansion() {
        addToSupply(BugType.PILL_BUG, 1)
    }

    /**
     * Add a number of bugs to the supply.
     */
    fun addToSupply(type: BugType?, count: Int) {
        val currentCount: Int = (if (supplyCreatureCounter.containsKey(type)) supplyCreatureCounter.get(type) else 0)!!

        for (i in 0..<count) {
            val t = Token(this, type)
            t.id = t.originalType.generateId(currentCount + 1 + i) // ID scheme used by Boardspace.net
            supply.add(t)
            bugs.put(t.id, t)
        }

        supplyCreatureCounter.put(type, currentCount + count)

        // Set queen if needed
        if (queen == null && type == BugType.QUEEN_BEE) {
            this.queen = getFromSupply(BugType.QUEEN_BEE)
        }
    }


    fun movedToken() {
        moves++
    }

    fun undoTokenMoved() {
        moves--
    }

    /**
     * Helper function to explicitly set how many tokens the player has moved already.
     */
    fun setTokensMoved(tokensMoved: Int) {
        this.moves = tokensMoved
    }

    val isBlack: Boolean
        get() = type == PlayerType.BLACK

    val isWhitePlayer: Boolean
        get() = type == PlayerType.WHITE

    /**
     * Adds a token to the stash.
     */
    fun addToSupply(token: Token) {
        supply.add(token)
        val type = token.type
        if (supplyCreatureCounter.containsKey(type)) {
            supplyCreatureCounter.put(type, supplyCreatureCounter.get(type)!! + 1)
        } else {
            supplyCreatureCounter.put(type, 1)
        }
    }

    fun removeFromSupply(token: Token) {
        if (supply.contains(token)) {
            supply.remove(token)
            val count = supplyCreatureCounter.get(token.originalType)!! - 1
            supplyCreatureCounter.put(token.originalType, count)
        } else {
            throw IllegalStateException("Supply doesn't contain: " + token)
        }
    }

    /**
     * Fetches a token from the supply. It is not removed until removeFromSupply() is called.
     * An IllegalStateException will be thrown if no such is available.
     */
    fun getFromSupply(type: BugType?): Token {
        val count: Int = (if (supplyCreatureCounter.containsKey(type)) supplyCreatureCounter.get(type) else 0)!!
        if (count > 0) {
            var result: Token? = null
            for (token in supply) {
                if (token.type == type) {
                    result = token
                    break
                }
            }
            return checkNotNull(result) { "Supply count does not match tokens for $type" }
        }

        throw IllegalStateException("No more " + type + " + tokens available")
    }


    /**
     * Returns a reference to the bug with the given Id, no matter where it is.
     */
    fun get(bugId: String): Token? {
        return bugs.get(bugId.uppercase(Locale.getDefault()))
    }

    /**
     * Returns true if the stash contains more tokens of the given creature type
     * @param type
     * @return
     */
    fun haveMoreTokens(type: BugType?): Boolean {
        val count: Int = (if (supplyCreatureCounter.containsKey(type)) supplyCreatureCounter.get(type) else 0)!!
        return count > 0
    }

    fun hasPlacedQueen(): Boolean {
        return queen!!.hex != null
    }

    val turns: Int
        /**
         * Returns how many turns the player has had.
         */
        get() = moves + passes

    val noStartingBugs: Int
        /**
         * Return the number of bugs that make up each side
         */
        get() = bugs.size

    fun addPlayTime(timeInMs: Long) {
        playTime += timeInMs
    }
}
