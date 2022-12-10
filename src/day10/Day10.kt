package aoc2022.day10

import aoc2022.readInput

fun clockSignals(instructions: List<Instruction>): List<Pair<Int, Int>> {
    return instructions.runningFold(0 to 1) { (cycle, x), inst ->
        val newX = if (inst is Instruction.Add) {
            x + inst.x
        } else {
            x
        }
        cycle + 1 to newX
    }.dropLast(1)
}

sealed class Instruction {
    data class Add(val x: Int) : Instruction()
    object Noop : Instruction()

    companion object {
        fun parseLines(lines: List<String>): List<Instruction> {
            return lines.map {
                val parts = it.split(" ")
                if (parts[0] == "addx") {
                    listOf(Noop, Add(parts[1].toInt()))
                } else {
                    listOf(Noop)
                }
            }.flatten()
        }
    }
}

fun main() {
    val instructions = Instruction.parseLines(readInput())
    val signals = clockSignals(instructions)
    val result1 = listOf(20, 60, 100, 140, 180, 220).map { signals[it] }.sumOf { it.first * it.second }
    val result2 = signals.map { (cycle, x) ->
        val h = cycle % 40
        if (x >= h - 1 && x <= h + 1) {
            '#'
        } else {
            '.'
        }
    }.chunked(40).joinToString("\n") { it.joinToString("") }
    println("Part 1: $result1")
    println("Part 2:\n$result2")
}