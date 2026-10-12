package dk.ilios.hivemind.game

import dk.ilios.hivemind.model.Hex
import dk.ilios.hivemind.model.Token

class GameCommand(
    val fromQ: Int,
    val fromR: Int,
    val toQ: Int,
    val toR: Int,
    val token: Token?,
    val isMovedByPillbug: Boolean,
) {
    fun execute(game: Game) {
        val board = game.board

        if (PASS == this) {
            // Do nothing;
        } else if (fromQ != Hex.SUPPLY) {
            board.moveToken(fromQ, fromR, toQ, toR)
        } else {
            board.addToken(token, toQ, toR)
        }

        game.requireActivePlayer().movedToken()
        game.togglePlayer()
        game.updateZobristKey()
    }

    fun undo(game: Game) {
        val board = game.board

        if (PASS == this) {
            // Do nothing;
        } else if (fromQ != Hex.SUPPLY) {
            board.moveToken(toQ, toR, fromQ, fromR)
        } else {
            board.removeToken(toQ, toR)
        }

        // Make sure that the old active players gets it turn correctly modified
        // by toggling player first.
        game.togglePlayer()
        game.requireActivePlayer().undoTokenMoved()
        game.updateZobristKey()
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append(if (token != null) token.toString() else "null")
        sb.append(": ")
        if (fromQ == Hex.SUPPLY) {
            sb.append("SUPPLY")
        } else {
            sb.append("(" + fromQ + ", " + fromR + ")")
        }

        sb.append(" -> ")
        sb.append("(" + toQ + ", " + toR + ")")
        return sb.toString()
    }

    val targetSquareDesc: String
        get() {
            if (toQ == Hex.SUPPLY || toR == Hex.SUPPLY) {
                return "SUPPLY"
            } else {
                return "(" + toQ + ", " + toR + ")"
            }
        }

    override fun equals(other: Any?): Boolean {
        if (other !is GameCommand) return false
        val gc2 = other
        if (fromQ != gc2.fromQ) return false
        if (fromR != gc2.fromR) return false
        if (toQ != gc2.toQ) return false
        if (toR != gc2.toR) return false
        if (this.isMovedByPillbug != gc2.isMovedByPillbug) return false
        return (token == null && gc2.token == null) || token == gc2.token
    }

    companion object {
        val PASS: GameCommand = GameCommand(0, 0, 0, 0, null, false)

        /**
         * Helper constructor: Move a token
         */
        fun move(token: Token, toQ: Int, toR: Int): GameCommand =
            GameCommand(token.hex!!.q, token.hex!!.r, toQ, toR, token, false)

        /**
         * Helper constructor: Add to board from supply
         */
        fun addFromSupply(token: Token?, toQ: Int, toR: Int): GameCommand =
            GameCommand(Hex.SUPPLY, Hex.SUPPLY, toQ, toR, token, false)

        fun moveByPillbug(token: Token, toQ: Int, toR: Int): GameCommand =
            GameCommand(token.hex!!.q, token.hex!!.r, toQ, toR, token, true)
    }
}
