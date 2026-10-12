package dk.ilios.hivemind.ai.statistics

import dk.ilios.hivemind.ai.RandomAI
import org.junit.Assert.*
import org.junit.Test

class AIStatisticsTest {
    @Test
    fun calculatesAggregatesWithoutReorderingRecordedHistory() {
        val stats = AIStatistics(RandomAI("Statistics"))
        assertTrue(stats.averagePositionsEvaluatedPrMove.isNaN())
        assertEquals(0, stats.meanPositionsEvaluatedPrMove)
        assertEquals(0, stats.maxPositionsEvaluatedPrMove)

        for (count in listOf(16, 1, 9, 4)) {
            stats.startCalculatingNextMove()
            stats.positionsEvaluated = count
            stats.moveCalculated()
        }

        assertEquals(listOf(16, 1, 9, 4), stats.positionsEvaluatedPrMove)
        assertEquals(7.5, stats.averagePositionsEvaluatedPrMove, 0.0)
        assertEquals(9, stats.meanPositionsEvaluatedPrMove)
        assertEquals(16, stats.maxPositionsEvaluatedPrMove)
    }
}
