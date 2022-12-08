package aoc2022.day06

import aoc2022.readInput

fun firstMarker(msg: String, size: Int): Int {
    return msg.windowed(size, 1, false).indexOfFirst { it.toSet().size == size } + size
}

fun main() {
    val input = readInput()[0]
    println("Part 1: ${firstMarker(input, 4)}")
    println("Part 2: ${firstMarker(input, 14)}")
}