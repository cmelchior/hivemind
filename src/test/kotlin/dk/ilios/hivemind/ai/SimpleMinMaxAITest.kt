package dk.ilios.hivemind.ai

import dk.ilios.hivemind.ai.heuristics.BoardValueHeuristic
import dk.ilios.hivemind.ai.heuristics.SimpleHeuristicV1
import dk.ilios.hivemind.ai.heuristics.TestSetups
import dk.ilios.hivemind.debug.HiveAsciiPrettyPrinter
import dk.ilios.hivemind.game.CommandProvider
import dk.ilios.hivemind.game.Game
import dk.ilios.hivemind.game.GameCommand
import dk.ilios.hivemind.model.Board
import dk.ilios.hivemind.model.BugType
import dk.ilios.hivemind.model.Hex
import dk.ilios.hivemind.model.Player
import dk.ilios.hivemind.model.Player.PlayerType
import org.junit.Assert
import org.junit.Before
import org.junit.Test

/**
 * Created by cme on 7/30/13.
 */
class SimpleMinMaxAITest {
    lateinit var printer: HiveAsciiPrettyPrinter
    lateinit var p1: Player
    lateinit var p2: Player

    @Before
    fun setup() {
        printer = HiveAsciiPrettyPrinter()
        p1 = Player("White", PlayerType.WHITE)
        p2 = Player("Black", PlayerType.BLACK)

        // Very simplistic game
        p1.addToSupply(BugType.QUEEN_BEE, 1)
        p1.addToSupply(BugType.SPIDER, 1)
        p2.addToSupply(BugType.QUEEN_BEE, 1)
        p2.addToSupply(BugType.SPIDER, 1)
    }

    @Test
    fun testOneDepth() {
        val ai: HiveAI = SimpleMinMaxAI("OneDepth", object : BoardValueHeuristic {
            override fun calculateBoardValue(state: Game): Int {
                // Give queen at 0,1 max priority for player 2
                val queenHex = checkNotNull(p2.queen).hex
                if (queenHex != null && queenHex.q == 0 && queenHex.r == 1) {
                    return -100
                } else {
                    return 0
                }
            }
        }, 1, 30000)

        p1.commandProvider = object : CommandProvider {
            override fun getCommand(currentState: Game, board: Board): GameCommand {
                return GameCommand(
                    Hex.SUPPLY,
                    Hex.SUPPLY,
                    0,
                    0,
                    p1.getFromSupply(BugType.QUEEN_BEE),
                    false
                )
            }
        }

        p2.commandProvider = object : CommandProvider {
            override fun getCommand(currentState: Game, board: Board): GameCommand? {
                return ai.nextMove(currentState, board)
            }
        }

        // Test placement of first bug once another bug has been placed
        val game = Game()
        game.setManualStepping(true)
        game.addPlayers(p1, p2)
        game.turnLimit = 1
        game.start()
        game.continueGame() // Turn 1 (White)
        val command = checkNotNull(ai.nextMove(game, game.getBoard())) // Turn 1 (Black)

        Assert.assertEquals(p2.queen, command.token)
        Assert.assertEquals(0, command.toQ.toLong())
        Assert.assertEquals(1, command.toR.toLong())
    }

    @Test
    fun testTwoDepth() {
        val ai: HiveAI = SimpleMinMaxAI("TwoDepth", object : BoardValueHeuristic {
            override fun calculateBoardValue(state: Game): Int {
                // Black really wants token at (0,1).
                if (state.requireActivePlayer().isBlack) {
                    val hex = state.getBoard().getHex(0, 1)
                    val t = hex.topToken
                    return if (t != null && t.player.isBlack) -100 else -25
                } else {
                    return 50
                }
            }
        }, 2, 30000)

        p1.commandProvider = object : CommandProvider {
            override fun getCommand(currentState: Game, board: Board): GameCommand {
                val turn = currentState.requireActivePlayer().turns
                if (turn == 0) {
                    return GameCommand(
                        Hex.SUPPLY,
                        Hex.SUPPLY,
                        0,
                        0,
                        p1.getFromSupply(BugType.QUEEN_BEE),
                        false
                    )
                } else {
                    return GameCommand.PASS
                }
            }
        }

        p2.commandProvider = object : CommandProvider {
            override fun getCommand(currentState: Game, board: Board): GameCommand? {
                return ai.nextMove(currentState, board)
            }
        }

        // Test placement of first bug once another bug has been placed
        val game = Game()
        game.addPlayers(p1, p2)
        game.setManualStepping(true)
        game.turnLimit = 2
        game.start()
        game.continueGame() // Turn 1 (White)

        val command = checkNotNull(ai.nextMove(game, game.getBoard())) // Turn 1 (Black)

        Assert.assertEquals(0, command.toQ.toLong())
        Assert.assertEquals(1, command.toR.toLong())
    }

    @Test
    fun testCanDetectOneTurnWin() {
        val ai: HiveAI = SimpleMinMaxAI("OneDepth", SimpleHeuristicV1(), 1, 30000)

        var game = Game()
        val p1 = Player("White", PlayerType.WHITE)
        p1.fillBaseSupply()
        val p2 = Player("Black", PlayerType.BLACK)
        p2.fillBaseSupply()

        game.addPlayers(p1, p2)
        game.turnLimit = 10
        game = TestSetups.sureWinInOneTurn(game)

        val command = checkNotNull(ai.nextMove(game, game.getBoard()))
        Assert.assertEquals(1, command.toQ.toLong())
        Assert.assertEquals(1, command.toR.toLong())
    }

    @Test
    fun testCanDetectWinTurnOne() {
        val ai: HiveAI = SimpleMinMaxAI("SimpleMinMax", SimpleHeuristicV1(), 1, 30000)

        var game = Game()
        val p1 = Player("White", PlayerType.WHITE)
        p1.fillBaseSupply()
        val p2 = Player("Black", PlayerType.BLACK)
        p2.fillBaseSupply()

        game.addPlayers(p1, p2)
        game.turnLimit = 10
        game = TestSetups.sureWinInOneTurn(game)

        val command = checkNotNull(ai.nextMove(game, game.getBoard()))

        Assert.assertEquals(1, command.toQ.toLong())
        Assert.assertEquals(1, command.toR.toLong())
    }


    @Test
    fun testCanDetectWinTurnTwo() {
        val ai: HiveAI = SimpleMinMaxAI("SimpleMinMax", SimpleHeuristicV1(), 3, 30000)

        var game = Game()
        val p1 = Player("White", PlayerType.WHITE)
        p1.fillBaseSupply()
        val p2 = Player("Black", PlayerType.BLACK)
        p2.fillBaseSupply()

        game.addPlayers(p1, p2)
        game.turnLimit = 10
        game = TestSetups.sureWinInTwoTurns(game)

        val command = checkNotNull(ai.nextMove(game, game.getBoard()))
        Assert.assertEquals(2, command.toQ.toLong())
        Assert.assertEquals(1, command.toR.toLong())
    }
}
