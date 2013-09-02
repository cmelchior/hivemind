package dk.ilios.hivemind.model.rules;

import dk.ilios.hivemind.model.*;

import java.util.*;

public class Rules {

    private static Rules instance;
    private final PillBug pillbug;

    private final QueenBee bee;
    private final Beetle beetle;
    private final SoldierAnt ant;
    private final Spider spider;
    private final Grasshopper grasshopper;
    private final LadyBug ladyBug;
    private final Mosquito mosquito;
    private final UnknownBug unknownBug;

    public static Rules getInstance() {
        if (instance == null) {
            instance = new Rules();
        }

        return instance;
    }

    private Rules() {
        ant = new SoldierAnt();
        bee = new QueenBee();
        beetle = new Beetle();
        spider = new Spider();
        grasshopper = new Grasshopper();
        ladyBug = new LadyBug();
        mosquito = new Mosquito();
        pillbug = new PillBug();
        unknownBug = new UnknownBug();
    }

    public boolean isFreeToMove(Token token, Board board) {

        // 1. Pick a random neighbor and mark it.
        // 2. Visit all neighbors recursively (ignoring startGame) and mark them
        // 3. When marking is done. Counht marked. Must be equal to hexes - 1 to be a consistent hive
        List<Token> neighbors = board.getNeighborTokens(token.getHex());
        if (neighbors.size() == 0) return false;

        Token startToken = neighbors.get(0);
        Set<Token> marked = mark(startToken, token, board, new HashSet<Token>());
        if (marked.size() != board.getFilledHexes().size() - 1) {
            return false;  // Marked set doesn't match whole board -> Disconnected hive
        }

        // Futher restrict based on type (Mosquito mimics other types)
        Bug bug = getBugSpecificRules(token);
        return bug.isFreeToMove(token, board);
     }

    /**
     * Recursively mark all tokens connected to a token
     * @param startToken
     * @param ignore
     * @param board
     * @param marked
     * @return The set of all tokens connected
     */
    private Set<Token> mark(Token startToken, Token ignore, Board board, HashSet<Token> marked) {
        marked.add(startToken);

        List<Token> neighbors = board.getNeighborTokens(startToken.getHex());
        for (Token t : neighbors) {
            if (t.equals(ignore)) continue;
            if (marked.contains(t)) continue;
            mark(t, ignore, board, marked);
        }

        return marked;
    }

    public List<Hex> getTargetHexes(Token token, Board board) {
        Bug bug = getBugSpecificRules(token);
        return bug.getTargetHexes(token, board);
    }

    /**
     * Get list of hexes a new pieces can be added to.
     *
     * @param player
     * @param board
     */
    public List<Hex> getStartHexes(Player player, Board board)  {

        // Start
        if (board.getFilledHexes
                ().isEmpty()) {
            return Arrays.asList(board.getHex(0, 0));
        }

        // Start for 2nd player
        if (board.getFilledHexes().size() == 1) {
            return board.getNeighborHexes(board.getFilledHexes().get(0));
        }

        List<Hex> result = new ArrayList<Hex>();
        Set<Hex> targets = new HashSet<Hex>();

        // 1. Get all empty neighbor hexes to player tokens.
        List<Hex> hexes = board.getFilledHexes();
        for (Hex hex : hexes) {
            if (hex.getTopToken().getPlayer().equals(player)) {
                List<Hex> neighbors = board.getNeighborHexes(hex);
                for (Hex h : neighbors) {
                    if (h.isEmpty()) {
                        targets.add(h);
                    }
                }
            }
        }

        // 2. For each empty hex, check colors of neighbor hexes
        // 3. If only 1 color, add to startGame hex set.
        for (Hex hex : targets) {
            List<Hex> neighbors = board.getNeighborHexes(hex);
            boolean isTarget = true;
            for (Hex h : neighbors) {
                if (h.isEmpty()) continue;
                Player p = h.getTopToken().getPlayer();
                if (p != player) {
                    isTarget = false;
                    break; // different token colors, ignore
                }
            }

            if (isTarget) {
                result.add(hex);
            }
        }
        return result;

    }

    /**
     * Returns true if a token can slide between the two hexes.
     * IF the "One Hive" rule must be maintained during the slide,
     * either the left or right hex must be filled, but if both are filled
     * they block instead.
     *
     * INVARIANT: Hexes are assumed to be neighbors.
     *
     * @param from Hex to move from.
     * @param to Hex to move to.
     * @return True if a token can slide to the new hex.
     */
    public boolean canSlideTo(Hex from, Hex to, Board board) {
        Hex left = board.getClockwiseHex(from, to, board);
        Hex right = board.getCounterClockwiseHex(from, to, board);

        if (from.getHeight() == 1 && to.isEmpty()) {
            // Slide at ground level
            boolean followRightSide = left.isEmpty() && !right.isEmpty();
            boolean followLeftSide = !left.isEmpty() && right.isEmpty();
            return followLeftSide || followRightSide;

        } else if (from.getHeight() > 1 && to.getHeight() < from.getHeight()) {
            // Slide on top of hive
            if (left.getHeight() > from.getHeight() && right.getHeight() > from.getHeight()) {
                return false;
            } else {
                return true;
            }

        } else {
            return false;
        }
    }

    /**
     * Returns true if a token can crawl up on a target token.
     * @param from Hex to move from.
     * @param to Hex to move to.
     * @return True if a token can crawl up on the new hex.
     */
    public boolean canCrawlUp(Hex from, Hex to, Board board) {
        Hex left = board.getClockwiseHex(from, to, board);
        Hex right = board.getCounterClockwiseHex(from, to, board);

        boolean isBlocked = (from.getHeight() - 1) < left.getHeight()
                && (from.getHeight() - 1) < right.getHeight()
                && to.getHeight() < left.getHeight()
                && to.getHeight() < right.getHeight();

        return !isBlocked;
    }

    /**
     * Returns true if a token can crawl down from top the hive to ground level.
     * @param from Hex to move from.
     * @param to Hex to move to.
     * @return True if a token can crawl up on the new hex.
     */
    public boolean canCrawlDown(Hex from, Hex to, Board board) {
        if (from.getHeight() < 2 || to.getHeight() > 0) return false;

        Hex left = board.getClockwiseHex(from, to, board);
        Hex right = board.getCounterClockwiseHex(from, to, board);

        return left.getHeight() < from.getHeight() || right.getHeight() < from.getHeight();
    }


    /**
     * Returns all tokens free to move for the given player
     */
    public Set<Token> getFreeTokens(Player p, Board board) {
        Set<Token> result = new HashSet<Token>();

        List<Hex> hexes = board.getFilledHexes();
        for (Hex hex : hexes) {
            if (hex.getTopToken() == null) continue;
            Token token = hex.getTopToken();
            if (token.getPlayer().equals(p)) {
                if (isFreeToMove(token, board)) {
                    result.add(token);
                }
            }
        }

        return result;
    }

    /**
     * Returns true if moving a hex from A to B maintains the "One Hive" rule.
     * Ie. by induction this is always true if any neighbor have a token.
     */
    public boolean isOneHiveIntact(Hex a, Hex b, Board board) {
        int i = b.getHeight(); // Crawling on top of of the hive still maintains the "One Hive" rule
        List<Token> neighbors = board.getNeighborTokens(b);
        for (Token t : neighbors) {
           if (!t.getHex().equals(a)) {
               i++;
           }
        }

        return i > 0;
    }

    public boolean isQueenSurrounded(Player player, Board board) {
        Token queen = player.getQueen();
        Hex hex = queen.getHex();
        if (hex != null) {
            return board.getNeighborTokens(hex).size() == 6;
        } else {
            return false;
        }
    }

    /**
     * A bug tries to mimic the movement characteristics of another bug.
     * Returns true, if successful, false otherwise.
     */
    public boolean mimicBug(Token copyTo, Token copyFrom, Board board) {
        Bug bug = getBugSpecificRules(copyTo);
        if (!bug.canMimic()) {
            throw new IllegalStateException(copyTo.getOriginalType() + " cannot mimic other bugs.");
        }

        // Always mimic BEETLE when atop the hive
        if (copyTo.getHex().getHeight() > 1) {
            copyTo.mimic(BugType.BEETLE);
            return true;

        } else {
            List<Token> targets = getMimicList(copyTo, board);
            if (!targets.contains(copyFrom)) return false; // Only allowed to copy from legal bugs
            copyTo.mimic(copyFrom.getOriginalType());
            return true;
        }
    }

    /**
     * Returns a list of possible targets for mimicing.
     * When atop the hive, it cannot copy anything (it is forced to be a beetle), and this returns no result
     *
     * @param token Token that wants to mimic nearby bugs.
     * @param board Current board
     */
    public List<Token> getMimicList(Token token, Board board) {
        if (token.getHex().getHeight() > 1) {
            return Collections.emptyList();
        } else {
            return board.getNeighborTokens(token.getHex());
        }
    }

    public Bug getBugSpecificRules(Token token) {
        BugType type = token.getType();
        switch (type) {
            case UNKNOWN: return unknownBug;
            case PILL_BUG: return pillbug;
            case MOSQUITO: return mosquito;
            case LADY_BUG: return ladyBug;
            case GRASSHOPPER: return grasshopper;
            case SPIDER: return spider;
            case SOLDIER_ANT: return ant;
            case QUEEN_BEE: return bee;
            case BEETLE: return beetle;
        }

        return unknownBug;
    }
}
