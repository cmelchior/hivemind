package dk.ilios.hivemind.model

import dk.ilios.hivemind.game.CommandProvider
import java.util.Locale

/**
 * Describes a player
 */
class Player(val name: String?, val type: PlayerType?) {
    enum class PlayerType {
        UNKNOWN, BLACK, WHITE
    }

    var commandProvider: CommandProvider? = null

    var ranking: Int = 0
    var moves: Int = 0 // Number of moves made in the game
        private set
    val passes: Int = 0 // Number of passes made in the game

    var queen: Token? = null
        private set
    private val bugs: MutableMap<String, Token> = hashMapOf<String, Token>()
    private val supplyCreatureCounter: MutableMap<BugType?, Int> =
        hashMapOf<BugType?, Int>() // Easy lookup to determine number of tokens of a specific type in the supply.

    private val mutableSupply = hashSetOf<Token>()

    /**
     * Return available tokens
     */
    val supply: Set<Token>
        get() = mutableSupply // Tokens in the supply

    var playTime: Long = 0 // How much time has the player used so far
        private set

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
        val currentCount: Int = supplyCreatureCounter[type] ?: 0

        for (i in 0..<count) {
            val t = Token(this, type)
            t.id = t.originalType.generateId(currentCount + 1 + i) // ID scheme used by Boardspace.net
            mutableSupply.add(t)
            bugs[t.id] = t
        }

        supplyCreatureCounter[type] = currentCount + count

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
        mutableSupply.add(token)
        val type = token.type
        supplyCreatureCounter[type] = (supplyCreatureCounter[type] ?: 0) + 1
    }

    fun removeFromSupply(token: Token) {
        if (supply.contains(token)) {
            mutableSupply.remove(token)
            val count = supplyCreatureCounter[token.originalType]!! - 1
            supplyCreatureCounter[token.originalType] = count
        } else {
            throw IllegalStateException("Supply doesn't contain: " + token)
        }
    }

    /**
     * Fetches a token from the supply. It is not removed until removeFromSupply() is called.
     * An IllegalStateException will be thrown if no such is available.
     */
    fun getFromSupply(type: BugType?): Token {
        val count: Int = supplyCreatureCounter[type] ?: 0
        if (count > 0) {
            return checkNotNull(supply.firstOrNull { it.type == type }) {
                "Supply count does not match tokens for $type"
            }
        }

        throw IllegalStateException("No more " + type + " + tokens available")
    }


    /**
     * Returns a reference to the bug with the given Id, no matter where it is.
     */
    operator fun get(bugId: String): Token? = bugs[bugId.uppercase(Locale.getDefault())]

    /**
     * Returns true if the stash contains more tokens of the given creature type
     * @param type
     * @return
     */
    fun haveMoreTokens(type: BugType?): Boolean {
        val count: Int = supplyCreatureCounter[type] ?: 0
        return count > 0
    }

    fun hasPlacedQueen(): Boolean = queen!!.hex != null

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
