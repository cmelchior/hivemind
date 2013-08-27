package dk.ilios.hivemind.game;

public enum GameStatus {
    RESULT_NOT_STARTED(1),
    RESULT_MATCH_IN_PROGRESS(2),
    RESULT_TURN_LIMIT_REACHED(3),
    RESULT_TIME_LIMIT_REACHED(4),
    RESULT_DRAW(5),
    RESULT_BLACK_WINS(6),
    RESULT_WHITE_WINS(7);

    private int value;

    public int getValue() {
        return value;
    }

    GameStatus(int value) {
        this.value = value;
    }

    public boolean winnerFound() {
        return this == RESULT_BLACK_WINS || this == RESULT_WHITE_WINS;
    }
}