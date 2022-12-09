package aoc2022.day08

import aoc2022.readInput

fun visibleHeights(heights: List<Int>): List<Int> {
    return heights.runningFold(-1) { a, b -> maxOf(a, b) }.dropLast(1)
}

// Reverses the input and output of the provided function
fun reverse(op: (List<Int>) -> List<Int>): (List<Int>) -> List<Int> = { it -> op(it.asReversed()).asReversed() }

fun part1(grid: List<List<Int>>): Int {
    val thresholds = List(grid.size) { MutableList(grid[it].size) { 10 } }
    grid.forEachIndexed { y, row ->
        visibleHeights(row).forEachIndexed { x, h ->
            thresholds[y][x] = minOf(thresholds[y][x], h)
        }
        reverse(::visibleHeights)(row).forEachIndexed { x, h ->
            thresholds[y][x] = minOf(thresholds[y][x], h)
        }
    }
    grid[0].indices.forEach { x ->
        val col = grid.map { it[x] }
        visibleHeights(col).forEachIndexed { y, h ->
            thresholds[y][x] = minOf(thresholds[y][x], h)
        }
        reverse(::visibleHeights)(col).forEachIndexed { y, h ->
            thresholds[y][x] = minOf(thresholds[y][x], h)
        }
    }
    return grid.zip(thresholds).map { (row, visibility) -> row.zip(visibility) }
        .flatten().count { (treeHeight, visibleHeight) ->
            treeHeight > visibleHeight
        }
}

fun viewingDistances(heights: List<Int>): List<Int> {
    val distances = MutableList(10) { 0 }
    return heights.mapIndexed { i, n ->
        val distance = i - distances[n]
        for (j in 0..n) {
            distances[j] = i
        }
        distance
    }
}

fun part2(grid: List<List<Int>>): Int {
    val scores = List(grid.size) { MutableList(grid[it].size) { 1 } }
    grid.forEachIndexed { y, row ->
        viewingDistances(row).forEachIndexed { x, dist ->
            scores[y][x] *= dist
        }
        reverse(::viewingDistances)(row).forEachIndexed { x, dist ->
            scores[y][x] *= dist
        }
    }
    grid[0].indices.forEach { x ->
        val col = grid.map { it[x] }
        viewingDistances(col).forEachIndexed { y, dist ->
            scores[y][x] *= dist
        }
        viewingDistances(col.asReversed()).asReversed().forEachIndexed { y, dist ->
            scores[y][x] *= dist
        }

    }
    return scores.flatten().max()
}

fun main() {
    val grid = readInput().map { line -> line.toCharArray().map { it.digitToInt() } }
    val result1 = part1(grid)
    val result2 = part2(grid)
    println("Part 1: $result1")
    println("Part 2: $result2")
}
