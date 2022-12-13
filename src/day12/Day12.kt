package aoc2022.day12

import aoc2022.Grid
import aoc2022.Point
import aoc2022.readInput

fun Grid<Char>.elevation(p: Point) = when (val c = get(p)) {
    'S' -> 'a'
    'E' -> 'z'
    else -> c
}.code

fun solvePart1(grid: Grid<Char>, start: Point, end: Point): Int {
    val path = grid.Graph { from, to ->
        grid.elevation(to) - grid.elevation(from) <= 1
    }.bfs(start, end)
    return path!!.size - 1
}

fun solvePart2(grid: Grid<Char>, end: Point): Int {
    // Search backwards from the end point to the first point that is 'a'
    val path = grid.Graph { from, to ->
        grid.elevation(to) - grid.elevation(from) >= -1
    }.bfs(end) { grid.get(it) == 'a' }
    return path!!.size - 1
}

fun main() {
    val grid = Grid(readInput().map { it.toCharArray().toList() })
    val start = grid.find { it == 'S' }.single()
    val end = grid.find { it == 'E' }.single()
    val result1 = solvePart1(grid, start, end)
    val result2 = solvePart2(grid, end)
    println("Part 1: $result1")
    println("Part 2: $result2")
}
