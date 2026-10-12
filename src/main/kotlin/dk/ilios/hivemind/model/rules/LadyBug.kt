package dk.ilios.hivemind.model.rules

import dk.ilios.hivemind.model.Board
import dk.ilios.hivemind.model.Hex
import dk.ilios.hivemind.model.Token


class LadyBug : Bug() {
    override fun isFreeToMove(token: Token, board: Board): Boolean {
        // As a ladybug starts by crawling on top of the hive, she can always move.
        return true
    }

    override fun getTargetHexes(token: Token, board: Board): List<Hex> {
        // Recursively find all squares you can slide to. Only squares at distance 3 count
        val result = recursiveVisit(token, 1, board, hashSetOf<Hex>())
        result.remove(token.hex) // Remove starting hex
        return result.toList()
    }

    private fun recursiveVisit(
        from: Token,
        distance: Int,
        board: Board,
        targetHexes: MutableSet<Hex>
    ): MutableSet<Hex> {
        // Crawl up on hive

        var distance = distance
        if (distance == 1) {
            val neighbors = board.getNeighborTokens(from.hex)
            distance++
            for (neighbor in neighbors) {
                val originalHex = checkNotNull(from.hex)
                val toHex = checkNotNull(neighbor.hex)
                if (Rules.instance.canCrawlUp(checkNotNull(from.hex), toHex, board)) {
                    board.moveToken(from, toHex.q, toHex.r)
                    recursiveVisit(from, distance, board, targetHexes)
                    board.moveToken(from, originalHex.q, originalHex.r)
                }
            }

            // Move around hive
        } else if (distance == 2) {
            val neighbors = board.getNeighborTokens(from.hex)
            distance++
            for (neighbor in neighbors) {
                val originalHex = checkNotNull(from.hex)
                val toHex = checkNotNull(neighbor.hex)
                if (Rules.instance.canSlideTo(originalHex, toHex, board) || Rules.instance
                        .canCrawlUp(originalHex, toHex, board)
                ) {
                    board.moveToken(from, toHex.q, toHex.r)
                    recursiveVisit(from, distance, board, targetHexes)
                    board.moveToken(from, originalHex.q, originalHex.r)
                }
            }

            // Move down from hive
        } else if (distance == 3) {
            val neighbors = board.getNeighborHexes(from.hex)
            for (neighbor in neighbors) {
                if (neighbor.height > 0) continue
                if (Rules.instance.canCrawlDown(checkNotNull(from.hex), neighbor, board)) {
                    targetHexes.add(neighbor)
                }
            }

            return targetHexes
        }


        return targetHexes
    }

    override fun canMimic(): Boolean = false

    override fun canMoveOthers(): Boolean = false
}
