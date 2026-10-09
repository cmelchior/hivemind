package dk.ilios.hivemind.ai

import dk.ilios.hivemind.ai.heuristics.BoardValueHeuristic
import dk.ilios.hivemind.ai.transpositiontable.TranspositionTable
import dk.ilios.hivemind.ai.transpositiontable.TranspositionTableEntry
import dk.ilios.hivemind.game.Game
import dk.ilios.hivemind.game.GameCommand
import dk.ilios.hivemind.model.Board
import dk.ilios.hivemind.utils.LimitedBuffer
import java.util.*

/**
 * Implementation of a MTD(f) algorithm search algorithm.
 * We use our best AlphaBeta algorithm as basis (TranspositionsTable, Killer Moves).
 *
 * @see http://askeplaat.wordpress.com/publications/mtdf-algorithm/
 */
class MTDFAI(name: String?, heuristicFunction: BoardValueHeuristic, depth: Int, maxTimeInMillis: Int) :
    AbstractMinMaxAI(name, heuristicFunction, depth, maxTimeInMillis) {
    private val random = Random()
    private val table = TranspositionTable()
    private val killerMoves = ArrayList<LimitedBuffer<GameCommand?>>()

    init {
        for (i in 0..<depth) {
            killerMoves.add(LimitedBuffer<GameCommand?>(2))
        }
    }

    override fun copy(): HiveAI {
        return KillerHeuristicTranspostionTableIDDFSAlphaBetaMiniMaxAI(name, heuristic, searchDepth, maxTimeInMillis)
    }

    override fun nextMove(state: Game, board: Board): GameCommand? {
        start = System.currentTimeMillis()
        maximizingPlayer = state.requireActivePlayer()

        // Clear previous killer moves
        for (buffer in killerMoves) {
            buffer.clear()
        }

        // Iterate depths, effectively a breath-first search, where top nodes get visited multiple times
        var depth = 1
        var bestValue = 0
        var bestCommand: GameCommand? = null

        var result: Array<Any?>
        while (depth <= searchDepth && System.currentTimeMillis() - start < maxTimeInMillis) {
            result = MTDF(state, bestValue, depth)
            bestValue = (result[0] as kotlin.Int?)!!
            bestCommand = result[1] as GameCommand?
            if (bestValue == HiveAI.MAX) {
                return bestCommand // Game winning move
            }

            depth++
        }

        return bestCommand // Best known move
    }

    private fun MTDF(state: Game, bestValue: Int, depth: Int): Array<Any?> {
        var value = bestValue
        var result = arrayOfNulls<Any>(2)

        var upperbound: Int = HiveAI.MAX
        var lowerbound: Int = HiveAI.MIN
        var beta: Int

        do {
            beta = if (value == lowerbound) value + 1 else value
            result = runAlphaBetaWithMemory(state, beta - 1, beta, depth, result)
            value = (result[0] as kotlin.Int?)!!
            if (value < beta) {
                upperbound = value
            } else {
                lowerbound = value
            }
        } while (lowerbound >= upperbound)

        return result
    }

    private fun runAlphaBetaWithMemory(
        state: Game,
        startAlpha: Int,
        startBeta: Int,
        depth: Int,
        result: Array<Any?>
    ): Array<Any?> {
        // Minimax traversal of game tree

        val moves = generateMoves(state)
        var bestValue = Int.MIN_VALUE
        var bestMove: GameCommand = GameCommand.PASS

        for (move in moves) {
            // Update game state and continue traversel
            applyMove(move, state)
            val value = alphaBetaWithMemory(state, bestValue, HiveAI.MAX, searchDepth - 1, false)
            if (value > bestValue || value == bestValue && random.nextBoolean()) {
                bestValue = value
                bestMove = move
            }
            undoMove(move, state)
        }

        result[0] = bestValue
        result[1] = bestMove
        return result
    }


    private fun alphaBetaWithMemory(state: Game, alpha: Int, beta: Int, depth: Int, maximizingPlayer: Boolean): Int {
        // Check transposition table and adjust values if needed or return result if possible

        var alpha = alpha
        var beta = beta
        val zobristKey = state.zobristKey
        val entry = table.getResult(zobristKey)
        if (entry != null && entry.depth >= depth) {
            aiStats.cacheHit()
            if (entry.type == TranspositionTableEntry.PV_NODE) {
                return entry.value // Shouldn't happen in zero-window searches (why?)
            } else if (entry.type == TranspositionTableEntry.CUT_NODE && entry.value > alpha) {
                alpha = entry.value
            } else if (entry.type == TranspositionTableEntry.ALL_NODE && entry.value < beta) {
                beta = entry.value
            }

            if (alpha >= beta) {
                return entry.value
            }
        }

        val originalAlpha = alpha
        val originalBeta = beta
        var bestMove: GameCommand = (if (entry != null) entry.move else null)!!

        // Run algorithm as usual
        var value: Int
        if (isGameOver(state, depth) || depth <= 0 || System.currentTimeMillis() - start > maxTimeInMillis) {
            value = value(state)
        } else {
            // Generate moves

            val killMoves = arrayOfNulls<GameCommand>(2)
            killerMoves.get(depth).toArray<GameCommand?>(killMoves)
            val moves = generateMoves(state, bestMove, killMoves[0], killMoves[1])
            var moveEvaluated = 0

            if (maximizingPlayer) {
                for (move in moves) {
                    moveEvaluated++
                    bestMove = move
                    applyMove(move, state)
                    value = alphabeta(state, depth - 1, alpha, beta, !maximizingPlayer)
                    if (value > alpha) {
                        alpha = value
                    }
                    undoMove(move, state)

                    // Beta cut-off
                    if (beta <= alpha) {
                        aiStats.cutOffAfter(moveEvaluated)
                        killerMoves.get(depth).add(move)
                        break
                    }
                }

                value = alpha
            } else {
                for (move in moves) {
                    moveEvaluated++
                    bestMove = move
                    applyMove(move, state)
                    value = alphabeta(state, depth - 1, alpha, beta, !maximizingPlayer)
                    if (value < beta) {
                        beta = value
                    }
                    undoMove(move, state)

                    // Alpha cut-off
                    if (beta <= alpha) {
                        aiStats.cutOffAfter(moveEvaluated)
                        killerMoves.get(depth).add(move)
                        break
                    }
                }

                value = beta
            }
        }

        // Update transposition table
        if (value <= originalAlpha) {
            table.addResult(zobristKey, value, depth, TranspositionTableEntry.CUT_NODE, bestMove)
        } else if (value >= originalBeta) {
            table.addResult(zobristKey, value, depth, TranspositionTableEntry.ALL_NODE, bestMove)
        } else {
            table.addResult(zobristKey, value, depth, TranspositionTableEntry.PV_NODE, bestMove)
        }

        return value
    }

    private fun alphabeta(state: Game, depth: Int, alpha: Int, beta: Int, maximizingPlayer: Boolean): Int {
        var alpha = alpha
        var beta = beta
        val originalAlpha = alpha
        val originalBeta = beta
        var bestMove: GameCommand? = null

        // Check transposition table and adjust values if needed or return result if possible
        val zobristKey = state.zobristKey
        val entry = table.getResult(zobristKey)
        if (entry != null && entry.depth >= depth) {
            aiStats.cacheHit()
            bestMove = entry.move
            if (entry.type == TranspositionTableEntry.PV_NODE) {
                return entry.value
            } else if (entry.type == TranspositionTableEntry.CUT_NODE && entry.value > alpha) {
                alpha = entry.value
            } else if (entry.type == TranspositionTableEntry.ALL_NODE && entry.value < beta) {
                beta = entry.value
            }

            if (alpha >= beta) {
                return entry.value // Lowerbound is better than upper bound
            }
        }

        // Run algorithm as usual
        var value: Int
        if (isGameOver(state, depth) || depth <= 0 || System.currentTimeMillis() - start > maxTimeInMillis) {
            value = value(state)
        } else {
            // Generate moves

            val killMoves = arrayOfNulls<GameCommand>(2)
            killerMoves.get(depth).toArray<GameCommand?>(killMoves)
            val moves = generateMoves(state, bestMove, killMoves[0], killMoves[1])
            var moveEvaluated = 0

            if (maximizingPlayer) {
                for (move in moves) {
                    moveEvaluated++
                    bestMove = move
                    applyMove(move, state)
                    value = alphabeta(state, depth - 1, alpha, beta, !maximizingPlayer)
                    if (value > alpha) {
                        alpha = value
                    }
                    undoMove(move, state)

                    // Beta cut-off
                    if (beta <= alpha) {
                        aiStats.cutOffAfter(moveEvaluated)
                        killerMoves.get(depth).add(move)
                        break
                    }
                }

                value = alpha
            } else {
                for (move in moves) {
                    moveEvaluated++
                    bestMove = move
                    applyMove(move, state)
                    value = alphabeta(state, depth - 1, alpha, beta, !maximizingPlayer)
                    if (value < beta) {
                        beta = value
                    }
                    undoMove(move, state)

                    // Alpha cut-off
                    if (beta <= alpha) {
                        aiStats.cutOffAfter(moveEvaluated)
                        killerMoves.get(depth).add(move)
                        break
                    }
                }

                value = beta
            }
        }

        // Update transposition table
        if (value <= originalAlpha) {
            table.addResult(zobristKey, value, depth, TranspositionTableEntry.CUT_NODE, bestMove)
        } else if (value >= originalBeta) {
            table.addResult(zobristKey, value, depth, TranspositionTableEntry.ALL_NODE, bestMove)
        } else {
            table.addResult(
                zobristKey,
                value,
                depth,
                TranspositionTableEntry.PV_NODE,
                bestMove
            ) // Shouldn't happen in zero-window searches (why?)
        }

        return value
    }

    override fun maintainsStandardPosition(): Boolean {
        return true
    }
}
