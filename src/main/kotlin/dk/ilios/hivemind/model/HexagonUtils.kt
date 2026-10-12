package dk.ilios.hivemind.model

import kotlin.math.abs

object HexagonUtils {
    /**
     * Rotates the basic vectors counter clockwise.
     * [x, y, z] -> [-y, -z, -x]
     */
    fun rotateLeft(rotation: IntArray): IntArray {
        val newX = -rotation[1]
        val newY = -rotation[2]
        val newZ = -rotation[0]
        rotation[0] = newX
        rotation[1] = newY
        rotation[2] = newZ
        return rotation
    }

    /**
     * Rotates the basis vectors clockwise.
     * [x, y, z] -> [-z, -x, -y]
     */
    fun rotateRight(rotation: IntArray): IntArray {
        val newX = -rotation[2]
        val newY = -rotation[0]
        val newZ = -rotation[1]
        rotation[0] = newX
        rotation[1] = newY
        rotation[2] = newZ
        return rotation
    }

    fun convertToAxialCoordinates(x: Int, y: Int, z: Int): IntArray = intArrayOf(x, z)

    /**
     * Cube coordinates fulfill x + y + z = 0
     */
    fun convertToCubeCoordinates(q: Int, r: Int): IntArray {
        val x = q
        val z = r
        val y = -x - z
        return intArrayOf(x, y, z)
    }

    /**
     * Distance between two hexes. If neighbors the distance is 1.
     */
    fun distance(q1: Int, r1: Int, q2: Int, r2: Int): Int {
        val x1 = q1
        val z1 = r1
        val x2 = q2
        val z2 = r2
        val y1 = -(x1 + z1)
        val y2 = -(x2 + z2)
        return (abs(x1 - x2) + abs(y1 - y2) + abs(z1 - z2)) / 2
    }
}
