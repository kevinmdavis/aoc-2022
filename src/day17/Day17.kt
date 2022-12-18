package aoc2022.day17

import aoc2022.Point
import aoc2022.readInput

class Shape(val spaces: List<List<Boolean>>)

val shapes: List<Shape> = listOf(
    Shape(
        listOf(
            listOf(true, true, true, true)
        )
    ),
    Shape(
        listOf(
            listOf(false, true, false),
            listOf(true, true, true),
            listOf(false, true, false),
        )
    ),
    Shape(
        listOf(
            listOf(true, true, true),
            listOf(false, false, true),
            listOf(false, false, true),
        )
    ),
    Shape(
        listOf(
            listOf(true),
            listOf(true),
            listOf(true),
            listOf(true),
        )
    ),
    Shape(
        listOf(
            listOf(true, true),
            listOf(true, true),
        )
    ),
)

class Rock(val shape: Shape, var position: Point) {
    fun points(offset: Point = Point(0, 0)): List<Point> {
        return shape.spaces.mapIndexed { dy, row ->
            row.mapIndexedNotNull { dx, filled ->
                if (filled) {
                    offset + position + Point(dx, dy)
                } else {
                    null
                }
            }
        }.flatten()
    }
}

enum class Direction {
    LEFT, RIGHT;

    val delta
        get() = when (this) {
            LEFT -> Point(-1, 0)
            RIGHT -> Point(1, 0)
        }
}

class Chamber(val width: Int, val height: Int) {
    val grid: List<MutableList<Boolean>> = List<MutableList<Boolean>>(height) { MutableList<Boolean>(width) { false } }
    var highestRock: Int = 0
    var rockCount = 0

    operator fun get(p: Point): Boolean {
        return grid[p.y][p.x]
    }

    operator fun set(p: Point, value: Boolean) {
        grid[p.y][p.x] = value
    }

    private fun checkCollision(rock: Rock, move: Point): Boolean {
        val points = rock.points(move)
        val outOfBounds = !points.all { it.x in 0 until width && it.y in 0 until height }
        if (outOfBounds) {
            return false
        }
        return points.all { !this[it] }
    }

    fun newRock(): Rock {
        val shape = shapes[rockCount % shapes.size]
        rockCount++
        val p = Point(2, highestRock + 3)
        return Rock(shape, p)
    }

    fun move(rock: Rock, dir: Direction): Boolean {
        val canMove = checkCollision(rock, dir.delta)
        if (canMove) {
            rock.position += dir.delta
        }
        val canFall = checkCollision(rock, Point(0, -1))
        if (!canFall) {
            return true
        }
        rock.position += Point(0, -1)
        return false
    }

    fun add(rock: Rock) {
        highestRock = maxOf(highestRock, rock.position.y + rock.shape.spaces.size)
        rock.points().forEach {
            this[it] = true
        }
    }

    fun process(directions: List<Direction>) {
        var dirOffset = 0
        repeat(2022) {
            val rock = newRock()
            var comeToRest = false
            while (!comeToRest) {
                val dir = directions[dirOffset]
                dirOffset = (dirOffset + 1) % directions.size
                comeToRest = move(rock, dir)
            }
            add(rock)
        }
    }

    override fun toString(): String {
        val filteredGrid = grid.filter { row -> row.any { it } }.reversed()
        return filteredGrid.joinToString("\n") { row ->
            row.joinToString("") {
                if (it) {
                    "#"
                } else {
                    "."
                }
            }
        }
    }
}

fun main() {
    val instructions = readInput()[0].toCharArray().map {
        when (it) {
            '>' -> Direction.RIGHT
            '<' -> Direction.LEFT
            else -> throw IllegalArgumentException()
        }
    }
    val chamber = Chamber(7, 2022 * 5)
    chamber.process(instructions)
    println("Part 1: ${chamber.highestRock}")
}