package aoc2022.day09

import aoc2022.Point
import aoc2022.readInput
import kotlin.math.abs
import kotlin.math.sign

fun Point.adjacent(other: Point) = abs(x - other.x) <= 1 && abs(y - other.y) <= 1

fun Point.towards(other: Point): Point {
    val relative = other - this
    return this +  Point(relative.x.sign, relative.y.sign)
}

class Rope(count: Int) {
    private val parts: MutableList<Point>

    init {
        parts = MutableList(count) { Point.Zero }
    }

    private fun move(dir: Point) {
        parts[0] = parts[0] + dir
        for ((i, j) in parts.indices.windowed(2)) {
            if (!parts[i].adjacent(parts[j])) {
                parts[j] = parts[j].towards(parts[i])
            }
        }
    }

    fun simulate(instructions: List<Instruction>): Int {
        val places = mutableSetOf<Point>()
        instructions.forEach { inst ->
            repeat(inst.count) {
                move(inst.dir)
                places.add(parts.last())
            }
        }
        return places.size
    }
}

data class Instruction(val dir: Point, val count: Int) {
    companion object {
        fun parse(line: String): Instruction {
            val (dirChar, count) = line.split(" ")
            val dir = when (dirChar) {
                "L" -> Point.Left
                "R" -> Point.Right
                "U" -> Point.Up
                "D" -> Point.Down
                else -> throw IllegalArgumentException("Invalid direction: $dirChar")
            }
            return Instruction(dir, count.toInt())
        }
    }
}

fun main() {
    val instructions = readInput().map { Instruction.parse(it) }
    val result1 = Rope(2).simulate(instructions)
    val result2 = Rope(10).simulate(instructions)
    println("Part 1: $result1")
    println("Part 2: $result2")
}