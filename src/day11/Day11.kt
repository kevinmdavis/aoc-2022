package aoc2022.day11

import aoc2022.readInput

typealias Operation = (Long) -> Long

class Monkey(initialItems: List<Long>, val op: Operation, val mod: Long, private val trueMonkey: Int, private val falseMonkey: Int) {
    private val items: MutableList<Long> = mutableListOf()
    var inspectionCount: Long = 0L

    init {
        items.addAll(initialItems)
    }

    fun handle(monkeys: List<Monkey>, reduceWorry: Boolean, combinedMod: Long) {
        if (items.isEmpty()) {
            return
        }
        items.forEach {
            inspectionCount++
            var worry = op(it) % combinedMod
            if (reduceWorry) {
                worry /= 3
            }
            val nextMonkey = if (worry % this.mod == 0L) {
                monkeys[trueMonkey]
            } else {
                monkeys[falseMonkey]
            }
            nextMonkey.items.add(worry)
        }
        items.clear()
    }

    companion object {
        fun parse(lines: List<String>): Monkey {
            val (itemLine, opLine, testLine, trueLine, falseLine) = lines.drop(1)
            val items = itemLine.trim().split(" ").drop(2).map { it.removeSuffix(",").toLong() }
            val operation: Operation = opLine.let { line ->
                val parts = line.trim().split(" ")
                val op = when (parts[4]) {
                    "*" -> { a: Long, b: Long -> a * b }
                    "/" -> { a: Long, b: Long -> a / b }
                    "+" -> { a: Long, b: Long -> a + b }
                    "-" -> { a: Long, b: Long -> a - b }
                    else -> throw IllegalArgumentException("Unknown operator: ${parts[4]}")
                }
                val resolveOperand = { it: Long ->
                    if (parts[5] == "old") {
                        it
                    } else {
                        parts[5].toLong()
                    }
                }
                { op(it, resolveOperand(it)) }
            }
            val mod = testLine.trim().split(" ")[3].toLong()
            val trueMonkey = trueLine.trim().split(" ")[5].toInt()
            val falseMonkey = falseLine.trim().split(" ")[5].toInt()
            return Monkey(items, operation, mod, trueMonkey, falseMonkey)
        }
    }
}

fun determineMonkeyBusiness(lines: List<String>, rounds: Int, reduceWorry: Boolean): Long {
    val monkeys: MutableList<Monkey> = mutableListOf()
    lines.chunked(7).map { chunk -> chunk.filter { it.isNotBlank() } }.map {
        Monkey.parse(it)
    }.forEach {
        monkeys.add(it)
    }
    val mod = monkeys.map { it.mod }.reduce { a, b -> a * b }
    repeat(rounds) {
        monkeys.forEach { it.handle(monkeys, reduceWorry, mod) }
    }
    val (monkey1, monkey2) = monkeys.sortedBy { it.inspectionCount }.takeLast(2)
    return monkey1.inspectionCount * monkey2.inspectionCount
}

fun main() {
    val lines = readInput()
    val result1 = determineMonkeyBusiness(lines, 20, true)
    val result2 = determineMonkeyBusiness(lines, 10000, false)
    println("Part 1: $result1")
    println("Part 2: $result2")
}