package aoc2022.day14

import aoc2022.Point
import aoc2022.readInput
import kotlin.math.sign

data class Line(val start: Point, val end: Point) {
    val points: List<Point>
        get() {
            val delta = end - start
            val inc = if (delta.x == 0) {
                Point(0, delta.y.sign)
            } else {
                Point(delta.x.sign, 0)
            }
            val result = mutableListOf(start)
            var x = start
            while (x != end) {
                x += inc
                result.add(x)
            }
            return result
        }
}

fun parsePoint(s: String): Point {
    val parts = s.split(",").map { it.toInt() }
    return Point(parts[0], parts[1])
}

class Grid(lines: List<Line>, private val hasFloor: Boolean) {
    private val collision = lines.flatMap { it.points }.toMutableSet()
    private val bottom = lines.flatMap { listOf(it.start, it.end) }.maxOf { it.y } + if (hasFloor) {
        2
    } else {
        0
    }

    fun next(p: Point): Point? {
        val candidates = listOf(p + Point(0, 1), p + Point(-1, 1), p + Point(1, 1))
        return candidates.find { !collides(it) }
    }

    private fun collides(p: Point): Boolean {
        if (hasFloor && p.y == bottom) {
            return true
        }
        return collision.contains(p)
    }

    fun simulateSand(start: Point = Point(500, 0)): Int {
        val sand = mutableListOf(start)
        var sandCount = 0
        while (sand.isNotEmpty() && sand.last().y <= bottom) {
            val s = sand.last()
            val movement = next(s)
            if (movement == null) {
                collision.add(s)
                sandCount++
                sand.removeLast()
            } else {
                sand.add(movement)
            }
        }
        return sandCount
    }
}

fun main() {
    val lines = readInput().flatMap { line ->
        line.split(" -> ").map(::parsePoint).windowed(2).map { Line(it[0], it[1]) }
    }
    val result1 = Grid(lines, false).simulateSand()
    val result2 = Grid(lines, true).simulateSand()
    println("Part 1: $result1")
    println("Part 2: $result2")
}