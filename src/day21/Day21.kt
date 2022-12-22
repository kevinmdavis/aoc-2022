package aoc2022.day21

import aoc2022.readInput
import kotlin.math.abs

data class Expression(val factor: Double = 0.0, val constant: Double = 0.0) {
    operator fun plus(other: Expression): Expression {
        return Expression(factor + other.factor, constant + other.constant)
    }

    operator fun minus(other: Expression): Expression {
        return Expression(factor - other.factor, constant - other.constant)
    }

    operator fun times(other: Expression): Expression {
        if (abs(factor * other.factor) > 0.000001) {
            throw IllegalArgumentException("Polynomial expressions are unsupported")
        }
        return Expression(factor * other.constant + other.factor * constant, constant * other.constant)
    }

    operator fun div(other: Expression): Expression {
        if (abs(other.factor) > 0.000001) {
            throw IllegalArgumentException("Dividing by non-constant expression is unsupported")
        }
        return Expression(factor / other.constant, constant / other.constant)
    }
}

sealed class Monkey {
    abstract fun getExpression(): Expression
    class Constant(private val expr: Expression) : Monkey() {
        override fun getExpression() = expr
    }

    class Operation(
        private val monkeys: Map<String, Monkey>,
        private val left: String,
        private val right: String,
        private val op: (Expression, Expression) -> Expression
    ) : Monkey() {
        val lhs get() = monkeys[left]!!.getExpression()
        val rhs get() = monkeys[right]!!.getExpression()
        override fun getExpression(): Expression {
            return op(lhs, rhs)
        }
    }
}

fun solve(lhs: Expression, rhs: Expression): Long {
    val combined = lhs - rhs
    return (-combined.constant / combined.factor).toLong()
}

fun main() {
    val monkeys = mutableMapOf<String, Monkey>()
    readInput().map { line ->
        val (name, instructions) = line.split(":")
        val parts = instructions.split(" ").drop(1)
        val monkey = if (parts.size == 1) {
            Monkey.Constant(Expression(constant = parts[0].toDouble()))
        } else if (parts.size == 3) {
            val op = when (parts[1]) {
                "*" -> Expression::times
                "/" -> Expression::div
                "-" -> Expression::minus
                "+" -> Expression::plus
                else -> throw IllegalArgumentException()
            }
            Monkey.Operation(monkeys, parts[0], parts[2], op)
        } else {
            throw IllegalArgumentException("Invalid line: $parts")
        }
        name to monkey
    }.forEach { monkeys[it.first] = it.second }
    val result1 = monkeys["root"]!!.getExpression().constant.toLong()
    monkeys["humn"] = Monkey.Constant(Expression(factor = 1.0))
    val root = (monkeys["root"]!! as Monkey.Operation)
    val result2 = solve(root.lhs, root.rhs)
    println("Part 1: $result1")
    println("Part 2: $result2")
}