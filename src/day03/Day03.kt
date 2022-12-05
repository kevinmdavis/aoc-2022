package aoc2022.day03

import aoc2022.readInput

typealias ItemSet = Set<Char>

fun String.compartments(): List<ItemSet> {
    val mid = length / 2
    val first = substring(0, mid).toCharArray().toSet()
    val second = substring(mid).toCharArray().toSet()
    return listOf(first, second)
}

fun List<String>.groups(): List<List<ItemSet>> {
    return chunked(3) { group ->
        group.map { it.toCharArray().toSet() }
    }
}

fun List<ItemSet>.overlap(): Char = reduce { a, b ->
    a.intersect(b)
}.single()

fun Char.priority(): Int {
    if (isLowerCase()) {
        return code - 'a'.code + 1
    }
    if (isUpperCase()) {
        return code - 'A'.code + 1 + 26
    }
    throw IllegalArgumentException()
}

fun main() {
    val lines = readInput()
    val result1 = lines.sumOf {
        it.compartments().overlap().priority()
    }
    val result2 = lines.groups().sumOf {
        it.overlap().priority()
    }
    println("Part 1: $result1")
    println("Part 2: $result2")
}