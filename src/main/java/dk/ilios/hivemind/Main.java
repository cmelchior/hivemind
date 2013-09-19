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
        HiveAI opponentC = new AlphaBetaMiniMaxAI("AlphaBeta", new SimpleHeuristicV2(), 3, 30000);
        HiveAI opponentCMark = new IDDFSAlphaBetaMiniMaxAI("Negamax", new SimpleHeuristicV2(), 3, 20000);
        HiveAI opponentD = new IDDFSAlphaBetaMiniMaxAI("IDDFS", new SimpleHeuristicV3(), 2, 20000);
        HiveAI opponentE = new MonteCarloTreeSearchAI("MCTS", 100, 30000);
        HiveAI opponentF = new UCTMonteCarloTreeSearchAI("MCTS-UCT", 100, 30000);
        HiveAI opponentG = new TranspostionTableIDDFSAlphaBetaMiniMaxAI("TranspositioNTable-IDDFS", new SimpleHeuristicV3(), 2, 20000);

        gameController.addOpponent(opponentA);
//        gameController.addOpponent(opponentA2);

//        gameController.addOpponent(opponentB);
//        gameController.addOpponent(opponentC);
        gameController.addOpponent(opponentD);
//        gameController.addOpponent(opponentF);

        gameController.setTurnLimit(30);
        gameController.setNumberOfMatches(10);
        gameController.start();
//        gameController.startSingleGame(opponentA, opponentA2, true);
        gameController.printLog(false);
    }
}
