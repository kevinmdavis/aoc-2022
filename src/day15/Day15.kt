package aoc2022.day15

import aoc2022.Point
import aoc2022.readInput
import kotlin.math.abs

data class Interval(val start: Int, val end: Int) {
    val size get() = end - start
    fun contains(x: Int) = x in start until end
    fun overlaps(other: Interval) = other.start <= this.end && other.end >= this.start
    fun adjacent(other: Interval) = other.end == this.start || other.start == this.end
    fun merge(other: Interval) = Interval(minOf(this.start, other.start), maxOf(this.end, other.end))
}

fun List<Interval>.merge(): List<Interval> {
    val sorted = sortedBy { it.start }
    if (isEmpty()) {
        return listOf()
    }
    val result = mutableListOf<Interval>()
    var current = sorted.first()
    for (i in (1 until size)) {
        current = if (sorted[i].overlaps(current) || sorted[i].adjacent(current)) {
            current.merge(sorted[i])
        } else {
            result.add(current)
            sorted[i]
        }
    }
    result.add(current)
    return result
}

class Sensor(private val location: Point, val beacon: Point) {
    val radius = distanceTo(beacon)

    private fun distanceTo(other: Point) = abs(location.x - other.x) + abs(location.y - other.y)

    fun inRange(p: Point) = distanceTo(p) <= radius

    fun intervalWithoutBeacon(y: Int): Interval? {
        val remaining = radius - abs(y - location.y)
        if (remaining <= 0) {
            return null
        }
        var range = Interval(location.x - remaining, location.x + remaining + 1)
        if (beacon.x == range.start) {
            range = Interval(range.start + 1, range.end)
        } else if (beacon.x == range.end - 1) {
            range = Interval(range.start, range.end - 1)
        }
        return range
    }

    fun perimeter() = sequence<Point> {
        var p = location - Point(radius + 1, 0)
        while (p.x < location.x) {
            yield(p)
            p += Point(1, 1)
        }
        while (p.y > location.y) {
            yield(p)
            p += Point(1, -1)
        }
        while (p.x > location.x) {
            yield(p)
            p += Point(-1, -1)
        }
        while (p.y < location.y) {
            yield(p)
            p += Point(-1, 1)
        }
    }
}

fun part1(sensors: List<Sensor>, y: Int): Int {
    return sensors.mapNotNull { it.intervalWithoutBeacon(y) }.merge().sumOf { it.size }
}

fun part2(sensors: List<Sensor>, searchRange: Interval): Long {
    val point = sensors.asSequence().map { it.perimeter() }.flatten().filter { p ->
        searchRange.contains(p.x) && searchRange.contains(p.y)
    }.find { p -> sensors.all { !it.inRange(p) } }!!
    return point.x * 4000000L + point.y
}

fun main() {
    val row = readLine()!!.toInt()
    val searchRange = readLine()!!.toInt()
    val sensors = readInput().map { "-?\\d+".toRegex().findAll(it).map { num -> num.value.toInt() }.toList() }.map {
        Sensor(Point(it[0], it[1]), Point(it[2], it[3]))
    }
    val result1 = part1(sensors, row)
    val result2 = part2(sensors, Interval(0, searchRange))
    println("Part 1: $result1")
    println("Part 2: $result2")
}