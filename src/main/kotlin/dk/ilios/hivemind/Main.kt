package dk.ilios.hivemind

import dk.ilios.hivemind.ai.*
import dk.ilios.hivemind.ai.controller.AIGameController
import dk.ilios.hivemind.ai.heuristics.SimpleHeuristicV2
import dk.ilios.hivemind.ai.heuristics.SimpleHeuristicV3

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val gameController = AIGameController()

        // Opponents
        val opponentA: HiveAI = RandomAI("RandomA")
        val opponentA2: HiveAI = RandomAI("RandomB")
        val opponentB: HiveAI = SimpleMinMaxAI("SimpleMinMax", SimpleHeuristicV2(), 2, 30000)
        val opponentC: HiveAI = AlphaBetaMiniMaxAI("AlphaBeta", SimpleHeuristicV2(), 3, 20000)
        val opponentCMark: HiveAI = IDDFSAlphaBetaMiniMaxAI("Negamax", SimpleHeuristicV2(), 3, 20000)
        val opponentD: HiveAI = IDDFSAlphaBetaMiniMaxAI("IDDFS", SimpleHeuristicV3(), 2, 20000)
        val opponentE: HiveAI = MonteCarloTreeSearchAI("MCTS", 100, 30000)
        val opponentF: HiveAI = UCTMonteCarloTreeSearchAI("MCTS-UCT", 70, 20000)
        val opponentG: HiveAI =
            TranspostionTableIDDFSAlphaBetaMiniMaxAI("TranspositionTable-IDDFS", SimpleHeuristicV3(), 2, 20000)
        val opponentH: HiveAI = KillerHeuristicTranspostionTableIDDFSAlphaBetaMiniMaxAI(
            "KillerMove-TT-IDDFS-AB",
            SimpleHeuristicV3(),
            3,
            180000
        )
        val opponentI: HiveAI = MTDFAI("MTD(f)", SimpleHeuristicV3(), 3, 20000)

        gameController.addOpponent(opponentF)

        //        gameController.addOpponent(opponentA2);

//        gameController.addOpponent(opponentB);
//        gameController.addOpponent(opponentC);
        gameController.addOpponent(opponentG)

        //        gameController.addOpponent(opponentF);
        gameController.setTurnLimit(30)
        gameController.setNumberOfMatches(10)
        //        gameController.start();
        gameController.startSingleGame(opponentA, opponentI, false)
        gameController.printLog(true)
    }
}
