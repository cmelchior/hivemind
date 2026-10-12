package dk.ilios.hivemind.model

/**
 * Hive game pieces
 */
enum class BugType(private val boardspaceKey: String) {
    UNKNOWN("?"),
    QUEEN_BEE("Q"),
    BEETLE("B"),
    GRASSHOPPER("G"),
    SPIDER("S"),
    SOLDIER_ANT("A"),
    MOSQUITO("M"),
    LADY_BUG("L"),
    PILL_BUG("P");

    /**
     * Generate a unique token ID that corrosponds the ID system used by Boardspace.net
     */
    fun generateId(number: Int): String {
        if (this == QUEEN_BEE || this == PILL_BUG) {
            return boardspaceKey
        } else {
            return boardspaceKey + number.toString()
        }
    }
}
