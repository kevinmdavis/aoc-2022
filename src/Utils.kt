package aoc2022

fun readInput(): List<String> =generateSequence(::readLine).toList()

data class Point(val x: Int, val y: Int) {
    operator fun plus(other: Point): Point {
        return Point(x + other.x, y + other.y)
    }

    operator fun minus(other: Point): Point {
        return Point(x - other.x, y - other.y)
    }

    companion object {
        val Zero = Point(0, 0)
        val Up = Point(0, 1)
        val Down = Point(0, -1)
        val Left = Point(-1, 0)
        val Right = Point(1, 0)
    }
}