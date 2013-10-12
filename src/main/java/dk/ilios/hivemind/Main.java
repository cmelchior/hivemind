package dk.ilios.hivemind;

import dk.ilios.hivemind.ai.*;
import dk.ilios.hivemind.ai.controller.AIGameController;
import dk.ilios.hivemind.ai.heuristics.SimpleHeuristicV2;
import dk.ilios.hivemind.ai.heuristics.SimpleHeuristicV3;

public class Main {

    public static void main(String[] args) {
        AIGameController gameController = new AIGameController();

        // Opponents
        HiveAI opponentA = new RandomAI("RandomA");
        HiveAI opponentA2 = new RandomAI("RandomB");
        HiveAI opponentB = new SimpleMinMaxAI("SimpleMinMax", new SimpleHeuristicV2(), 2, 30000);
        HiveAI opponentC = new AlphaBetaMiniMaxAI("AlphaBeta", new SimpleHeuristicV2(), 3, 20000);
        HiveAI opponentCMark = new IDDFSAlphaBetaMiniMaxAI("Negamax", new SimpleHeuristicV2(), 3, 20000);
        HiveAI opponentD = new IDDFSAlphaBetaMiniMaxAI("IDDFS", new SimpleHeuristicV3(), 2, 20000);
        HiveAI opponentE = new MonteCarloTreeSearchAI("MCTS", 100, 30000);
        HiveAI opponentF = new UCTMonteCarloTreeSearchAI("MCTS-UCT", 70, 20000);
        HiveAI opponentG = new TranspostionTableIDDFSAlphaBetaMiniMaxAI("TranspositionTable-IDDFS", new SimpleHeuristicV3(), 2, 20000);
        HiveAI opponentH = new KillerHeuristicTranspostionTableIDDFSAlphaBetaMiniMaxAI("KillerMove-TT-IDDFS-AB", new SimpleHeuristicV3(), 3, 180000);
        HiveAI opponentI = new MTDFAI("MTD(f)", new SimpleHeuristicV3(), 3, 20000);

        gameController.addOpponent(opponentF);
//        gameController.addOpponent(opponentA2);

//        gameController.addOpponent(opponentB);
//        gameController.addOpponent(opponentC);
        gameController.addOpponent(opponentG);
//        gameController.addOpponent(opponentF);

        gameController.setTurnLimit(30);
        gameController.setNumberOfMatches(10);
//        gameController.start();
        gameController.startSingleGame(opponentA, opponentI, false);
        gameController.printLog(true);
    }
}
