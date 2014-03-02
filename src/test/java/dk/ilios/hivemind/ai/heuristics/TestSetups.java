package dk.ilios.hivemind.ai.heuristics;

import dk.ilios.hivemind.game.Game;
import dk.ilios.hivemind.model.Board;
import dk.ilios.hivemind.model.BugType;
import dk.ilios.hivemind.model.Player;

/**
 * Utility class for some setups that are interesting to run heuristic checks on or use them as basis for AI checks.
 */
public class TestSetups {

    /**
     * Blacks have 2 pieces (HOP/SPI) that can be moved to ensure the win.
     * The returned state has black as the active player.
     *
     * | = = = = = = = = = = = = = = =  |
     * |                  _ _           |
     * |                /# # #\         |
     * |               /# ANT #\        |
     * |               \# -W- #/        |
     * |                \#_#_#/         |
     * |                /# # #\         |
     * |           _ _ /# BTL #\ _ _    |
     * |         /+ + +\# -W- #/     \  |
     * |        /+ ANT +\#_#_#/  WIN  \ |
     * |        \+ -B- +/# # #\  -B-  / |
     * |         \+_+_+/# QBE #\ _ _ /  |
     * |         /+ + +\# -W- #/# # #\  |
     * |    _ _ /+ QBE +\#_#_#/# ANT #\ |
     * |  /+ + +\+ -B- +/+ + +\# -W- #/ |
     * | /+ HOP +\+_+_+/+ ANT +\#_#_#/  |
     * | \+ -B- +/+ + +\+ -B- +/+ + +\  |
     * |  \+_+_+/+ SPI +\+_+_+/+ SPI +\ |
     * |        \+ -B- +/# # #\+ -B- +/ |
     * |         \+_+_+/# HOP #\+_+_+/  |
     * |               \# -W- #/        |
     * |                \#_#_#/         |
     * |                /# # #\         |
     * |               /# HOP #\        |
     * |               \# -W- #/        |
     * |                \#_#_#/         |
     * |                                |
     * | = = = = = = = = = = = = = = =  |
     *
     * @param state Game state with players ready.
     * @return Game state forwarded to the given board setup. Move count might not be accurate.
     *
     */
    public static Game sureWinInOneTurn(Game state) {
        Board board = state.getBoard();
        Player white = state.getWhitePlayer();
        Player black = state.getBlackPlayer();

        // Black setup
        board.addToken(white.getFromSupply(BugType.SOLDIER_ANT), 0, 0);
        board.addToken(white.getFromSupply(BugType.BEETLE), 0, 1);
        board.addToken(white.getFromSupply(BugType.QUEEN_BEE), 0, 2);
        board.addToken(white.getFromSupply(BugType.GRASSHOPPER), 0, 4);
        board.addToken(white.getFromSupply(BugType.GRASSHOPPER), 0, 5);
        board.addToken(white.getFromSupply(BugType.SOLDIER_ANT), 1, 2);
        black.setTokensMoved(6);

        // White setup
        board.addToken(black.getFromSupply(BugType.GRASSHOPPER), -2, 4);
        board.addToken(black.getFromSupply(BugType.SOLDIER_ANT), -1, 2);
        board.addToken(black.getFromSupply(BugType.QUEEN_BEE), -1, 3);
        board.addToken(black.getFromSupply(BugType.SPIDER), -1, 4);
        board.addToken(black.getFromSupply(BugType.SOLDIER_ANT), 0, 3);
        board.addToken(black.getFromSupply(BugType.SPIDER), 1, 3);
        white.setTokensMoved(6);

        state.setActivePlayer(black);
        return state;
    }

    /**
     * Blacks have 4 pieces that can be moved to ensure the win in 2 turns.
     * The returned state has black as the active player.
     *
     * 1) Black: Move either B1 to T2 or A1/A2 to T1
     * 2) White: Block one piece
     * 3) Black: Use either Beetle, Ant or Hopper to win
     *
     * | = = = = = = = = = = = = = = = = = = = |
     * |                         _ _           |
     * |                       /# # #\         |
     * |                  _ _ /# ANT #\        |
     * |                /# # #\# -W- #/        |
     * |               /# ANT #\#_#_#/         |
     * |               \# -W- #/               |
     * |                \#_#_#/                |
     * |                /# # #\                |
     * |           _ _ /# SPI #\ _ _           |
     * |         /     \# -W- #/     \         |
     * |    _ _ /  T2   \#_#_#/  T1   \ _ _    |
     * |  /+ + +\       /# # #\       /# # #\  |
     * | /+ B1  +\ _ _ /# QBE #\ _ _ /# ANT #\ |
     * | \+ -B- +/+ + +\# -W- #/# # #\# -W- #/ |
     * |  \+_+_+/+ QBE +\#_#_#/# SPI #\#_#_#/  |
     * |  /+ + +\+ -B- +/+ + +\# -W- #/+ + +\  |
     * | /+ A1  +\+_+_+/+ ANT +\#_#_#/+ HOP +\ |
     * | \+ -B- +/+ + +\+ -B- +/     \+ -B- +/ |
     * |  \+_+_+/+ A2  +\+_+_+/       \+_+_+/  |
     * |        \+ -B- +/                      |
     * |         \+_+_+/                       |
     * |                                       |
     * | = = = = = = = = = = = = = = = = = = = |
     *
     */
    public static Game sureWinInTwoTurns(Game state) {
        Board board = state.getBoard();
        Player white = state.getWhitePlayer();
        Player black = state.getBlackPlayer();

        // Black setup
        board.addToken(black.getFromSupply(BugType.QUEEN_BEE), 0, 3);
        board.addToken(black.getFromSupply(BugType.SOLDIER_ANT), 0, 4);
        board.addToken(black.getFromSupply(BugType.SOLDIER_ANT), -1, 4);
        board.addToken(black.getFromSupply(BugType.BEETLE), -1, 3);
        board.addToken(black.getFromSupply(BugType.SOLDIER_ANT), 1, 3);
        board.addToken(black.getFromSupply(BugType.GRASSHOPPER), 3, 2);
        black.setTokensMoved(6);

        // White setup
        board.addToken(white.getFromSupply(BugType.SOLDIER_ANT), 1, 0);
        board.addToken(white.getFromSupply(BugType.SPIDER), 1, 1);
        board.addToken(white.getFromSupply(BugType.QUEEN_BEE), 1, 2);
        board.addToken(white.getFromSupply(BugType.SOLDIER_ANT), 2, -1);
        board.addToken(white.getFromSupply(BugType.SPIDER), 2, 2);
        board.addToken(white.getFromSupply(BugType.SOLDIER_ANT), 3, 1);
        black.setTokensMoved(6);

        state.setActivePlayer(black);
        return state;
    }
}
