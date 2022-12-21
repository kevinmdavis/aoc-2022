package aoc2022.day18

import aoc2022.readInput

data class Point3(val x: Int, val y: Int, val z: Int) {
    fun adjacent() = listOf(
        this + Point3(1, 0, 0),
        this + Point3(-1, 0, 0),
        this + Point3(0, 1, 0),
        this + Point3(0, -1, 0),
        this + Point3(0, 0, 1),
        this + Point3(0, 0, -1),
    )

    operator fun plus(other: Point3): Point3 {
        return Point3(x + other.x, y + other.y, z + other.z)
    }

    operator fun minus(other: Point3): Point3 {
        return Point3(x - other.x, y - other.y, z - other.z)
    }
}

fun countExterior(points: Set<Point3>, adjacent: Set<Point3>): Int {
    val start = points.minBy { it.x } + Point3(-1, 0, 0)
    val q = ArrayDeque(listOf(start))
    val visited = mutableSetOf(start)
    var count = 0
    while (q.isNotEmpty()) {
        val current = q.removeFirst()
        val next = current.adjacent().filter { !points.contains(it) }
        val isAdjacent = adjacent.contains(current)
        if (isAdjacent) {
            count += current.adjacent().count { points.contains(it) }
        }
        next.filter { isAdjacent || adjacent.contains(it) }.filter {!visited.contains(it) }.forEach {
            visited.add(it)
            q.add(it)
        }
    }
    return count
}

fun main() {
    val points = readInput().map { line -> line.split(",").map { it.toInt() } }.map { Point3(it[0], it[1], it[2]) }.toSet()
    val adjacent = points.map { it.adjacent() }.flatten().filter { !points.contains(it) }
    val result1 = adjacent.size
    val result2 = countExterior(points, adjacent.toSet())
    println("Part 1: $result1")
    println("Part 2: $result2")
}
