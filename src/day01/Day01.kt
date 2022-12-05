package aoc2022.day01

import aoc2022.readInput

fun parseCounts(lines: List<String>): List<List<Int>> {
    val result: MutableList<List<Int>> = mutableListOf()
    var current: MutableList<Int> = mutableListOf()
    for (line in lines) {
        if (line.isBlank()) {
            result.add(current)
            current = mutableListOf()
        } else {
            current.add(line.toInt())
        }
    }
    result.add(current)
    return result
}

fun part1(counts: List<List<Int>>): Int {
    return counts.maxOf { it.sum() }
}

fun part2(counts: List<List<Int>>): Int {
    return counts.map { it.sum() }.sorted().takeLast(3).sum()
}

fun main() {
    val counts = parseCounts(readInput())
    val result1 = part1(counts)
    val result2 = part2(counts)
    println("Part 1: $result1")
    println("Part 2: $result2")
}
