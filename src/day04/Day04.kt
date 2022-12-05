package aoc2022.day04

import aoc2022.readInput

data class Range(val start: Int, val end: Int) {
    fun contains(other: Range) = start <= other.start && end >= other.end

    fun overlaps(other: Range) = other.start <= end && start <= other.end

    companion object {
        fun parse(s: String): Range {
            val (start, end) = s.split("-").map { it.toInt() }
            return Range(start, end)
        }
    }
}

fun part1(input: List<Pair<Range, Range>>): Int = input.count {
    it.first.contains(it.second) || it.second.contains(it.first)
}

fun part2(input: List<Pair<Range, Range>>): Int = input.count {
    it.first.overlaps(it.second)
}

fun main() {
    val input = readInput().map {
        val (a, b) = it.split(",").map { range ->
            Range.parse(range)
        }
        a to b
    }
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
