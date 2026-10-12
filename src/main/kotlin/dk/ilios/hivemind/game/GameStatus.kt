package dk.ilios.hivemind.game

enum class GameStatus(value: Int) {
    RESULT_NOT_STARTED(1),
    RESULT_MATCH_IN_PROGRESS(2),
    RESULT_TURN_LIMIT_REACHED(3),
    RESULT_TIME_LIMIT_REACHED(4),
    RESULT_DRAW(5),
    RESULT_BLACK_WINS(6),
    RESULT_WHITE_WINS(7);

    val value: Int

    init {
        this.value = value
    }

    fun winnerFound(): Boolean = this == RESULT_BLACK_WINS || this == RESULT_WHITE_WINS
}
