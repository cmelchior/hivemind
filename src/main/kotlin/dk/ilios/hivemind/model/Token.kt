package dk.ilios.hivemind.model

class Token(player: Player, type: BugType?) {
    var id: String = DEFAULT_ID // ID number = Number added to the board
    val player: Player
    val originalType: BugType
    private var mimics: BugType = BugType.UNKNOWN
    var hex: Hex? = null

    init {
        this.player = player
        this.originalType = if (type == null) BugType.UNKNOWN else type
    }

    val type: BugType
        get() = if (mimics != BugType.UNKNOWN) mimics else originalType

    /**
     * Set the Bug type this token currently mimics.
     * If above ground, it can only mimic a beetle
     */
    fun mimic(type: BugType?) {
        if (type == null) {
            mimics = BugType.UNKNOWN
        } else {
            mimics = type
        }
    }

    fun mimic(otherToken: Token?) {
        if (otherToken == null) {
            mimic(BugType.UNKNOWN)
        } else {
            mimic(otherToken.originalType)
        }
    }

    fun inSupply(): Boolean {
        return (hex == null || hex!!.q == Hex.SUPPLY)
    }

    override fun toString(): String {
        return player.name + "(" + id + "): " + originalType + (if (hex != null) " " + hex.toString() else " (SUPPLY)")
    }

    companion object {
        const val DEFAULT_ID: String = "?"
    }
}
