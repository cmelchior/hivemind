package dk.ilios.hivemind.model

class Token(val player: Player, type: BugType?) {
    var id: String = DEFAULT_ID // ID number = Number added to the board
    val originalType: BugType = type ?: BugType.UNKNOWN
    private var mimics: BugType = BugType.UNKNOWN
    var hex: Hex? = null

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
        return hex?.q?.let { it == Hex.SUPPLY } ?: true
    }

    override fun toString(): String =
        player.name + "(" + id + "): " + originalType + (if (hex != null) " " + hex.toString() else " (SUPPLY)")

    companion object {
        const val DEFAULT_ID: String = "?"
    }
}
