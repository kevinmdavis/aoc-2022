package aoc2022.day05

import aoc2022.readInput
import java.util.Stack

data class Instruction(val count: Int, val from: Int, val to: Int) {
    fun executePart1(stacks: List<Stack<Char>>) {
        repeat(count) {
            stacks[to].push(stacks[from].pop())
        }
    }

    fun executePart2(stacks: List<Stack<Char>>) {
        (1..count).map { stacks[from].pop() }.reversed().forEach {
            stacks[to].push(it)
        }
    }

    companion object {
        fun parse(s: String): Instruction {
            val parts = s.split(" ")
            return Instruction(parts[1].toInt(), parts[3].toInt() - 1, parts[5].toInt() - 1)
        }
    }
}

fun parseInput(lines: List<String>): Pair<List<Stack<Char>>, List<Instruction>> {
    val split = lines.indexOfFirst { it.isBlank() }
    val stacks = mutableListOf<Stack<Char>>()
    for (x in 1 until lines[split - 1].length step 4) {
        val stack = Stack<Char>()
        var y = split - 1
        while (y - 1 >= 0 && x < lines[y - 1].length && lines[y - 1][x] != ' ') {
            y--
            stack.push(lines[y][x])
        }
        stacks.add(stack)
    }
    val instructions = (split + 1 until lines.size).map { Instruction.parse(lines[it]) }
    return stacks to instructions
}

fun solve(input: List<String>, execute: Instruction.(List<Stack<Char>>) -> Unit): String {
    val (stacks, instructions) = parseInput(input)
    instructions.forEach { execute(it, stacks) }
    return stacks.map { it.peek() }.joinToString("")
}

fun main() {
    val input = readInput()
    val result1 = solve(input, Instruction::executePart1)
    val result2 = solve(input, Instruction::executePart2)
    println("Part 1: $result1")
    println("Part 2: $result2")
}
