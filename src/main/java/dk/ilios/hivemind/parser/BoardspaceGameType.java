package dk.ilios.hivemind.parser;

public enum BoardspaceGameType {
    ALL("a"),
    DUMBOT("d"),         // All games with Dumbot
    TOURNAMENT("t"),     // All tournament games between 2 human players
    PLAYER("p");

    private final String prefix;         // All other games between human players

    private BoardspaceGameType(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }
}
