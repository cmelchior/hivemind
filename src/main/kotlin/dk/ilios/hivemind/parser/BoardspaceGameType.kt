package dk.ilios.hivemind.parser

enum class BoardspaceGameType(prefix: String) {
    ALL("a"),
    DUMBOT("d"),  // All games with Dumbot
    TOURNAMENT("t"),  // All tournament games between 2 human players
    PLAYER("p");

    val prefix: String? // All other games between human players

    init {
        this.prefix = prefix
    }
}
