package dk.ilios.hivemind.parser.metric;

import dk.ilios.hivemind.game.Game;
import dk.ilios.hivemind.model.Board;
import dk.ilios.hivemind.model.BugType;
import dk.ilios.hivemind.model.Token;
import dk.ilios.hivemind.parser.BoardspaceGameType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * List all games that fullfill the opening criteria given by Linael:
 *
 Type I:
 | = = = = = = = = = = = = = = = = = = = |
 |    _ _                         _ _    |
 |  /+ + +\                     /+ + +\  |
 | /+ G1  +\ _ _               /+ S1  +\ |
 | \+ -B- +/# # #\             \+ -B- +/ |
 |  \+_+_+/#  Q  #\ _ _         \+_+_+/  |
 |        \# -W- #/# # #\       /+ + +\  |
 |         \#_#_#/# L1  #\ _ _ /+  Q  +\ |
 |               \# -W- #/+ + +\+ -B- +/ |
 |                \#_#_#/+ B1  +\+_+_+/  |
 |                      \+ -B- +/        |
 |                       \+_+_+/         |
 |                                       |
 | = = = = = = = = = = = = = = = = = = = |

 Type II:
 | = = = = = = = = = = = = = = = = = = = |
 |    _ _                         _ _    |
 |  /+ + +\                     /+ + +\  |
 | /+ A1  +\ _ _               /+ S1  +\ |
 | \+ -B- +/# # #\             \+ -B- +/ |
 |  \+_+_+/#  Q  #\ _ _         \+_+_+/  |
 |        \# -W- #/# # #\       /+ + +\  |
 |         \#_#_#/# L1  #\ _ _ /+  Q  +\ |
 |               \# -W- #/+ + +\+ -B- +/ |
 |                \#_#_#/+ B1  +\+_+_+/  |
 |                      \+ -B- +/        |
 |                       \+_+_+/         |
 |                                       |
 | = = = = = = = = = = = = = = = = = = = |

 Type III:
 | = = = = = = = = = = = = = = =  |
 |           _ _           _ _    |
 |         /+ + +\       /+ + +\  |
 |    _ _ /+ S1  +\     /+ S2  +\ |
 |  /# # #\+ -B- +/     \+ -B- +/ |
 | /#  Q  #\+_+_+/       \+_+_+/  |
 | \# -W- #/# # #\       /+ + +\  |
 |  \#_#_#/# L1  #\ _ _ /+  Q  +\ |
 |        \# -W- #/+ + +\+ -B- +/ |
 |         \#_#_#/+ B2  +\+_+_+/  |
 |               \+ -B- +/        |
 |                \+_+_+/         |
 |                                |
 | = = = = = = = = = = = = = = =  |

 Type I/II/III Relaxed:
 | = = = = = = = = = = = = = = =  |
 |    _ _                         |
 |  /# # #\                       |
 | /#  Q  #\ _ _           _ _    |
 | \# -W- #/# # #\       /+ + +\  |
 |  \#_#_#/# L1  #\ _ _ /+  Q  +\ |
 |        \# -W- #/+ + +\+ -B- +/ |
 |         \#_#_#/+ B1  +\+_+_+/  |
 |               \+ -B- +/        |
 |                \+_+_+/         |
 |                                |
 | = = = = = = = = = = = = = = =  |
 *
 */

public class LinaelOpeningList extends Metric {

    public static final String FILE_NAME = "linael_openings_game_list.data";

    private static final String TYPE1 = "Type I";
    private static final String TYPE2 = "Type II";
    private static final String TYPE3 = "Type III";
    private static final String TYPE_RELAXED = "Type I/II/III Relaxed";
    private static final String UNKNOWN = "Unknown";

    private Map<String, ArrayList<String>> results = new HashMap<String, ArrayList<String>>();

    public LinaelOpeningList() {
        results.put(TYPE1, new ArrayList<String>());
        results.put(TYPE2, new ArrayList<String>());
        results.put(TYPE3, new ArrayList<String>());
        results.put(TYPE_RELAXED, new ArrayList<String>());
    }

    @Override
    public void analyzeGame(BoardspaceGameType type, String variant, Game game) {
        if (!variant.contains("l")) return; // We only care about games with the ladybug
        if (game.getWhitePlayer().getTurns() < 3 || game.getBlackPlayer().getTurns() < 3) return; // Both players must have had 3 turns
        game.setReplayMode(true);
        String openingType = getOpeningType(game);
        if (openingType != UNKNOWN) {
            System.out.println(openingType + ": " + game.getName() + "\n" + printer.toString(game.getBoard()));
            results.get(openingType).add(game.getName() + "\n" + printer.toString(game.getBoard()));
        }
    }

    private String getOpeningType(Game game) {

        // Fast forward 3 turns for each player
        for (int i = 0; i < 6; i++) {
            game.forward();
        }

        if (isType1(game)) {
            return TYPE1;
        } else if (isType2(game)) {
            return TYPE2;
        } else if (isType3(game)) {
            return TYPE3;
        } else if (isTypeRelaxed(game)) {
            return TYPE_RELAXED;
        } else {
            return UNKNOWN;
        }
  }

    private boolean isType1(Game game) {
        Board b = game.getBoard();

        Token w1 = b.getHexForStandardPosition(1,0).getTopToken();
        Token w2 = b.getHexForStandardPosition(0,0).getTopToken();
        Token w3 = b.getHexForStandardPosition(-1,0).getTopToken();

        Token b1 = b.getHexForStandardPosition(2,0).getTopToken();
        Token b2 = b.getHexForStandardPosition(3,-1).getTopToken();
        Token b3 = b.getHexForStandardPosition(3,-2).getTopToken();

        return (w1 != null && w1.getPlayer().isWhitePlayer() && w1.getOriginalType() == BugType.LADY_BUG)
                && (w2 != null && w2.getPlayer().isWhitePlayer() && w2.getOriginalType() == BugType.QUEEN_BEE)
                && (w3 != null && w3.getPlayer().isWhitePlayer() && w3.getOriginalType() == BugType.GRASSHOPPER)
                && (b1 != null && b1.getPlayer().isBlack() && b1.getOriginalType() == BugType.BEETLE)
                && (b2 != null && b2.getPlayer().isBlack() && b2.getOriginalType() == BugType.QUEEN_BEE)
                && (b3 != null && b3.getPlayer().isBlack() && b3.getOriginalType() == BugType.SPIDER);
    }


    private boolean isType2(Game game) {
        Board b = game.getBoard();

        Token w1 = b.getHexForStandardPosition(1,0).getTopToken();
        Token w2 = b.getHexForStandardPosition(0,0).getTopToken();
        Token w3 = b.getHexForStandardPosition(-1,0).getTopToken();

        Token b1 = b.getHexForStandardPosition(2,0).getTopToken();
        Token b2 = b.getHexForStandardPosition(3,-1).getTopToken();
        Token b3 = b.getHexForStandardPosition(3,-2).getTopToken();

        return (w1 != null && w1.getPlayer().isWhitePlayer() && w1.getOriginalType() == BugType.LADY_BUG)
                && (w2 != null && w2.getPlayer().isWhitePlayer() && w2.getOriginalType() == BugType.QUEEN_BEE)
                && (w3 != null && w3.getPlayer().isWhitePlayer() && w3.getOriginalType() == BugType.SOLDIER_ANT)
                && (b1 != null && b1.getPlayer().isBlack() && b1.getOriginalType() == BugType.BEETLE)
                && (b2 != null && b2.getPlayer().isBlack() && b2.getOriginalType() == BugType.QUEEN_BEE)
                && (b3 != null && b3.getPlayer().isBlack() && b3.getOriginalType() == BugType.SPIDER);
    }

    private boolean isType3(Game game) {
        Board b = game.getBoard();

        Token w1 = b.getHexForStandardPosition(1,0).getTopToken();
        Token w2 = b.getHexForStandardPosition(0,0).getTopToken();
        Token w3 = b.getHexForStandardPosition(1,-1).getTopToken();

        Token b1 = b.getHexForStandardPosition(2,0).getTopToken();
        Token b2 = b.getHexForStandardPosition(3,-1).getTopToken();
        Token b3 = b.getHexForStandardPosition(3,-2).getTopToken();

        return (w1 != null && w1.getPlayer().isWhitePlayer() && w1.getOriginalType() == BugType.LADY_BUG)
                && (w2 != null && w2.getPlayer().isWhitePlayer() && w2.getOriginalType() == BugType.QUEEN_BEE)
                && (w3 != null && w3.getPlayer().isWhitePlayer() && w3.getOriginalType() == BugType.SPIDER)
                && (b1 != null && b1.getPlayer().isBlack() && b1.getOriginalType() == BugType.BEETLE)
                && (b2 != null && b2.getPlayer().isBlack() && b2.getOriginalType() == BugType.QUEEN_BEE)
                && (b3 != null && b3.getPlayer().isBlack() && b3.getOriginalType() == BugType.SPIDER);
    }

    private boolean isTypeRelaxed(Game game) {
        Board b = game.getBoard();

        Token w1 = b.getHexForStandardPosition(1,0).getTopToken();
        Token w2 = b.getHexForStandardPosition(0,0).getTopToken();

        Token b1 = b.getHexForStandardPosition(2,0).getTopToken();
        Token b2 = b.getHexForStandardPosition(3,-1).getTopToken();

        return (w1 != null && w1.getPlayer().isWhitePlayer() && w1.getOriginalType() == BugType.LADY_BUG)
                && (w2 != null && w2.getPlayer().isWhitePlayer() && w2.getOriginalType() == BugType.QUEEN_BEE)
                && (b1 != null && b1.getPlayer().isBlack() && b1.getOriginalType() == BugType.BEETLE)
                && (b2 != null && b2.getPlayer().isBlack() && b2.getOriginalType() == BugType.QUEEN_BEE);
    }


    @Override
    public void save() {
        StringBuilder sb = new StringBuilder();

        for (String key : results.keySet()) {
            sb.append(key + ":\n");
            ArrayList<String> results = new ArrayList<String>();
            for (String name : results) {
                sb.append(name + "\n\n");
            }
            sb.append("\n");
        }

        saveStringToDisc(FILE_NAME, sb.toString());
    }
}
