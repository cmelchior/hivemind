package dk.ilios.hivemind.model;

/**
 * Hive game pieces
 */
public enum BugType {
    UNKNOWN("?"),
    QUEEN_BEE("Q"),
    BEETLE("B"),
    GRASSHOPPER("G"),
    SPIDER("S"),
    SOLDIER_ANT("A"),
    MOSQUITO("M"),
    LADY_BUG("L"),
    PILL_BUG("P");

    private String boardspaceKey;

    private BugType(String boardspaceKey) {
        this.boardspaceKey = boardspaceKey;
    }




    /**
     * Generate a unique token ID that corrosponds the ID system used by Boardspace.net
     */
    public String generateId(int number) {
        if (this == QUEEN_BEE || this == PILL_BUG) {
            return boardspaceKey;
        } else {
            return boardspaceKey + Integer.toString(number);
        }
    }
}
