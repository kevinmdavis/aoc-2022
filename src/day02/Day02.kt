package aoc2022.day02

import aoc2022.readInput

enum class Outcome {
    Win, Lose, Draw;

    val value
        get() = when (this) {
            Lose -> 0
            Draw -> 3
            Win -> 6
        }

    companion object {
        fun parseFromStrategy(c: Char) = when (c) {
            'X' -> Lose
            'Y' -> Draw
            'Z' -> Win
            else -> throw IllegalArgumentException()
        }
    }
}

enum class Shape {
    Rock, Paper, Scissors;

    private val weakness
        get() = when (this) {
            Rock -> Paper
            Paper -> Scissors
            Scissors -> Rock
        }

    private val strength
        get() = when (this) {
            Rock -> Scissors
            Paper -> Rock
            Scissors -> Paper
        }

    fun outcome(opponent: Shape) = when (this) {
        opponent -> Outcome.Draw
        opponent.weakness -> Outcome.Win
        else -> Outcome.Lose
    }

    fun shapeForOutcome(outcome: Outcome) = when (outcome) {
        Outcome.Draw -> this
        Outcome.Win -> this.weakness
        Outcome.Lose -> this.strength
    }

    val value
        get() = when (this) {
            Rock -> 1
            Paper -> 2
            Scissors -> 3
        }

    companion object {
        fun parseOpponentShape(c: Char): Shape {
            return when (c) {
                'A' -> Rock
                'B' -> Paper
                'C' -> Scissors
                else -> throw IllegalArgumentException()
            }
        }

        fun parseMyShape(c: Char): Shape {
            return when (c) {
                'X' -> Rock
                'Y' -> Paper
                'Z' -> Scissors
                else -> throw IllegalArgumentException()
            }
        }
    }
}

fun part1(lines: List<String>): Int {
    return lines.sumOf {
        val opponentShape = Shape.parseOpponentShape(it[0])
        val myShape = Shape.parseMyShape(it[2])
        myShape.outcome(opponentShape).value + myShape.value
    }
}

fun part2(lines: List<String>): Int {
    return lines.sumOf {
        val opponentShape = Shape.parseOpponentShape(it[0])
        val outcome = Outcome.parseFromStrategy(it[2])
        opponentShape.shapeForOutcome(outcome).value + outcome.value
    }
}

fun main() {
    val lines = readInput()
    val result1 = part1(lines)
    val result2 = part2(lines)
    println("Part 1: $result1")
    println("Part 2: $result2")
}
