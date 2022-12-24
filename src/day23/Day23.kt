package aoc2022.day23

import aoc2022.Point
import aoc2022.readInput

data class Movement(val direction: Point, val check: List<Point>)

val allAdjacent = listOf(
    Point(-1, -1),
    Point(0, -1),
    Point(1, -1),
    Point(-1, 0),
    Point(1, 0),
    Point(-1, 1),
    Point(0, 1),
    Point(1, 1),
)

val movements = listOf(
    Movement(Point(0, -1), listOf(Point(-1, -1), Point(0, -1), Point(1, -1))),
    Movement(Point(0, 1), listOf(Point(-1, 1), Point(0, 1), Point(1, 1))),
    Movement(Point(-1, 0), listOf(Point(-1, -1), Point(-1, 0), Point(-1, 1))),
    Movement(Point(1, 0), listOf(Point(1, -1), Point(1, 0), Point(1, 1))),
)

fun Set<Point>.progress(offset: Int): Pair<Set<Point>, Boolean> {
    var anyMoved = false
    return this.groupBy {
        var dir = Point(0, 0)
        if (!allAdjacent.all { toCheck -> !this.contains(it + toCheck) }) {
            for (i in 0..4) {
                val movement = movements[(offset + i) % 4]
                val clear = movement.check.all { toCheck -> !this.contains(it + toCheck) }
                if (clear) {
                    dir = movement.direction
                    break
                }
            }
        }
        it + dir
    }.flatMap { (k, v) ->
        if (v.size > 1) {
            v
        } else {
            anyMoved = anyMoved || k != v.first()
            listOf(k)
        }
    }.toSet() to anyMoved
}

fun Set<Point>.boundingRectangle(): Pair<Point, Point> {
    val minX = this.minOf { it.x }
    val maxX = this.maxOf { it.x }
    val minY = this.minOf { it.y }
    val maxY = this.maxOf { it.y }
    return Point(minX, minY) to Point(maxX, maxY)
}

fun Pair<Point, Point>.area(): Int {
    return (this.second.x - this.first.x + 1) * (this.second.y - this.first.y + 1)
}

fun Set<Point>.gridString(): String {
    val rect = boundingRectangle()
    return (rect.first.y..rect.second.y).map { y ->
        (rect.first.x..rect.second.y).map { x ->
            if (this.contains(Point(x, y))) {
                '#'
            } else {
                '.'
            }
        }.joinToString("")
    }.joinToString("\n") + "\n"
}

fun main() {
    var start = readInput().flatMapIndexed { y, row ->
        row.mapIndexed { x, value ->
            if (value == '#') {
                Point(x, y)
            } else {
                null
            }
        }
    }.filterNotNull().toSet()
    var elves = start
    var changed = true
    var i = 0
    while (changed) {
        val tick = elves.progress(i % 4)
        elves = tick.first
        changed = tick.second
        if (i == 9) {
            val result1 = elves.boundingRectangle().area() - elves.size
            println("Part 1: $result1")
        }
        i++
    }
    println("Part 2: $i")
}