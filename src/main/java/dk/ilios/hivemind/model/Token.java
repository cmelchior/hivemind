package dk.ilios.hivemind.model;

public class Token {

    public static final String DEFAULT_ID = "?";

    private String id = DEFAULT_ID; // ID number = Number added to the board
    private Player player;
    private BugType originalType;
    private BugType mimics = BugType.UNKNOWN;
    private Hex hex;

    public Token(Player player, BugType type) {
        this.player = player;
        this.originalType = (type == null) ? BugType.UNKNOWN : type;
    }

    public Player getPlayer() {
        return player;
    }

    public BugType getOriginalType() {
        return originalType;
    }

    public BugType getType() {
        return (mimics != BugType.UNKNOWN) ? mimics : originalType;
    }

    public Hex getHex() {
        return hex;
    }

    public void setHex(Hex hex) {
        this.hex = hex;
    }

    /**
     * Set the Bug type this token currently mimics.
     * If above ground, it can only mimic a beetle
     */
    public void mimic(BugType type) {
        if (type == null) {
            mimics = BugType.UNKNOWN;
        } else {
            mimics = type;
        }
    }

    public void mimic(Token otherToken) {
        if (otherToken == null) {
            mimic(BugType.UNKNOWN);
        } else {
            mimic(otherToken.getOriginalType());
        }
    }

    public boolean inSupply() {
        return (hex == null || hex.getQ() == Hex.SUPPLY);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return player.getName() + "("+id+"): " + originalType + (hex != null ? " " + hex.toString() : " (SUPPLY)");
    }
}
