package dk.ilios.hivemind.parser.metric

import dk.ilios.hivemind.game.Game
import dk.ilios.hivemind.model.BugType
import dk.ilios.hivemind.parser.BoardspaceGameType

/**
 * List all games that fullfill the opening criteria given by Linael:
 *
 * Type I:
 * | = = = = = = = = = = = = = = = = = = = |
 * |    _ _                         _ _    |
 * |  /+ + +\                     /+ + +\  |
 * | /+ G1  +\ _ _               /+ S1  +\ |
 * | \+ -B- +/# # #\             \+ -B- +/ |
 * |  \+_+_+/#  Q  #\ _ _         \+_+_+/  |
 * |        \# -W- #/# # #\       /+ + +\  |
 * |         \#_#_#/# L1  #\ _ _ /+  Q  +\ |
 * |               \# -W- #/+ + +\+ -B- +/ |
 * |                \#_#_#/+ B1  +\+_+_+/  |
 * |                      \+ -B- +/        |
 * |                       \+_+_+/         |
 * |                                       |
 * | = = = = = = = = = = = = = = = = = = = |
 *
 * Type II:
 * | = = = = = = = = = = = = = = = = = = = |
 * |    _ _                         _ _    |
 * |  /+ + +\                     /+ + +\  |
 * | /+ A1  +\ _ _               /+ S1  +\ |
 * | \+ -B- +/# # #\             \+ -B- +/ |
 * |  \+_+_+/#  Q  #\ _ _         \+_+_+/  |
 * |        \# -W- #/# # #\       /+ + +\  |
 * |         \#_#_#/# L1  #\ _ _ /+  Q  +\ |
 * |               \# -W- #/+ + +\+ -B- +/ |
 * |                \#_#_#/+ B1  +\+_+_+/  |
 * |                      \+ -B- +/        |
 * |                       \+_+_+/         |
 * |                                       |
 * | = = = = = = = = = = = = = = = = = = = |
 *
 * Type III:
 * | = = = = = = = = = = = = = = =  |
 * |           _ _           _ _    |
 * |         /+ + +\       /+ + +\  |
 * |    _ _ /+ S1  +\     /+ S2  +\ |
 * |  /# # #\+ -B- +/     \+ -B- +/ |
 * | /#  Q  #\+_+_+/       \+_+_+/  |
 * | \# -W- #/# # #\       /+ + +\  |
 * |  \#_#_#/# L1  #\ _ _ /+  Q  +\ |
 * |        \# -W- #/+ + +\+ -B- +/ |
 * |         \#_#_#/+ B2  +\+_+_+/  |
 * |               \+ -B- +/        |
 * |                \+_+_+/         |
 * |                                |
 * | = = = = = = = = = = = = = = =  |
 *
 * Type I/II/III Relaxed:
 * | = = = = = = = = = = = = = = =  |
 * |    _ _                         |
 * |  /# # #\                       |
 * | /#  Q  #\ _ _           _ _    |
 * | \# -W- #/# # #\       /+ + +\  |
 * |  \#_#_#/# L1  #\ _ _ /+  Q  +\ |
 * |        \# -W- #/+ + +\+ -B- +/ |
 * |         \#_#_#/+ B1  +\+_+_+/  |
 * |               \+ -B- +/        |
 * |                \+_+_+/         |
 * |                                |
 * | = = = = = = = = = = = = = = =  |
 *
 */
class LinaelOpeningList : Metric() {
    private val results: MutableMap<String?, ArrayList<String?>?> = HashMap<String?, ArrayList<String?>?>()

    init {
        results.put(TYPE1, ArrayList<String?>())
        results.put(TYPE2, ArrayList<String?>())
        results.put(TYPE3, ArrayList<String?>())
        results.put(TYPE_RELAXED, ArrayList<String?>())
    }

    override fun analyzeGame(type: BoardspaceGameType, variant: String, game: Game) {
        if (!variant.contains("l")) return  // We only care about games with the ladybug

        if (game.getWhitePlayer().turns < 3 || game.getBlackPlayer()
                .turns < 3
        ) return  // Both players must have had 3 turns

        game.setReplayMode(true)
        val openingType = getOpeningType(game)
        if (openingType !== UNKNOWN) {
            println(openingType + ": " + game.name + "\n" + printer.toString(game.getBoard()))
            results.get(openingType)!!.add(game.name + "\n" + printer.toString(game.getBoard()))
        }
    }

    private fun getOpeningType(game: Game): String {
        // Fast forward 3 turns for each player

        for (i in 0..5) {
            game.forward()
        }

        if (isType1(game)) {
            return TYPE1
        } else if (isType2(game)) {
            return TYPE2
        } else if (isType3(game)) {
            return TYPE3
        } else if (isTypeRelaxed(game)) {
            return TYPE_RELAXED
        } else {
            return UNKNOWN
        }
    }

    private fun isType1(game: Game): Boolean {
        val b = game.getBoard()

        val w1 = b.getHexForStandardPosition(1, 0).topToken
        val w2 = b.getHexForStandardPosition(0, 0).topToken
        val w3 = b.getHexForStandardPosition(-1, 0).topToken

        val b1 = b.getHexForStandardPosition(2, 0).topToken
        val b2 = b.getHexForStandardPosition(3, -1).topToken
        val b3 = b.getHexForStandardPosition(3, -2).topToken

        return (w1 != null && w1.player.isWhitePlayer && w1.originalType == BugType.LADY_BUG)
                && (w2 != null && w2.player.isWhitePlayer && w2.originalType == BugType.QUEEN_BEE)
                && (w3 != null && w3.player.isWhitePlayer && w3.originalType == BugType.GRASSHOPPER)
                && (b1 != null && b1.player.isBlack && b1.originalType == BugType.BEETLE)
                && (b2 != null && b2.player.isBlack && b2.originalType == BugType.QUEEN_BEE)
                && (b3 != null && b3.player.isBlack && b3.originalType == BugType.SPIDER)
    }


    private fun isType2(game: Game): Boolean {
        val b = game.getBoard()

        val w1 = b.getHexForStandardPosition(1, 0).topToken
        val w2 = b.getHexForStandardPosition(0, 0).topToken
        val w3 = b.getHexForStandardPosition(-1, 0).topToken

        val b1 = b.getHexForStandardPosition(2, 0).topToken
        val b2 = b.getHexForStandardPosition(3, -1).topToken
        val b3 = b.getHexForStandardPosition(3, -2).topToken

        return (w1 != null && w1.player.isWhitePlayer && w1.originalType == BugType.LADY_BUG)
                && (w2 != null && w2.player.isWhitePlayer && w2.originalType == BugType.QUEEN_BEE)
                && (w3 != null && w3.player.isWhitePlayer && w3.originalType == BugType.SOLDIER_ANT)
                && (b1 != null && b1.player.isBlack && b1.originalType == BugType.BEETLE)
                && (b2 != null && b2.player.isBlack && b2.originalType == BugType.QUEEN_BEE)
                && (b3 != null && b3.player.isBlack && b3.originalType == BugType.SPIDER)
    }

    private fun isType3(game: Game): Boolean {
        val b = game.getBoard()

        val w1 = b.getHexForStandardPosition(1, 0).topToken
        val w2 = b.getHexForStandardPosition(0, 0).topToken
        val w3 = b.getHexForStandardPosition(1, -1).topToken

        val b1 = b.getHexForStandardPosition(2, 0).topToken
        val b2 = b.getHexForStandardPosition(3, -1).topToken
        val b3 = b.getHexForStandardPosition(3, -2).topToken

        return (w1 != null && w1.player.isWhitePlayer && w1.originalType == BugType.LADY_BUG)
                && (w2 != null && w2.player.isWhitePlayer && w2.originalType == BugType.QUEEN_BEE)
                && (w3 != null && w3.player.isWhitePlayer && w3.originalType == BugType.SPIDER)
                && (b1 != null && b1.player.isBlack && b1.originalType == BugType.BEETLE)
                && (b2 != null && b2.player.isBlack && b2.originalType == BugType.QUEEN_BEE)
                && (b3 != null && b3.player.isBlack && b3.originalType == BugType.SPIDER)
    }

    private fun isTypeRelaxed(game: Game): Boolean {
        val b = game.getBoard()

        val w1 = b.getHexForStandardPosition(1, 0).topToken
        val w2 = b.getHexForStandardPosition(0, 0).topToken

        val b1 = b.getHexForStandardPosition(2, 0).topToken
        val b2 = b.getHexForStandardPosition(3, -1).topToken

        return (w1 != null && w1.player.isWhitePlayer && w1.originalType == BugType.LADY_BUG)
                && (w2 != null && w2.player.isWhitePlayer && w2.originalType == BugType.QUEEN_BEE)
                && (b1 != null && b1.player.isBlack && b1.originalType == BugType.BEETLE)
                && (b2 != null && b2.player.isBlack && b2.originalType == BugType.QUEEN_BEE)
    }


    override fun save() {
        val sb = StringBuilder()

        for (key in results.keys) {
            sb.append(key + ":\n")
            val results = ArrayList<String?>()
            for (name in results) {
                sb.append(name + "\n\n")
            }
            sb.append("\n")
        }

        saveStringToDisc(FILE_NAME, sb.toString())
    }

    companion object {
        const val FILE_NAME: String = "linael_openings_game_list.data"

        private const val TYPE1 = "Type I"
        private const val TYPE2 = "Type II"
        private const val TYPE3 = "Type III"
        private const val TYPE_RELAXED = "Type I/II/III Relaxed"
        private const val UNKNOWN = "Unknown"
    }
}
