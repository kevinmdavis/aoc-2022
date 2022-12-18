package aoc2022.day17

import aoc2022.Point
import aoc2022.readInput
import kotlin.IllegalStateException

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

class Chamber(private val width: Int = 7, private val height: Int = 100000) {
    private val grid: List<MutableList<Boolean>> = List(height) { MutableList(width) { false } }
    private var highestRock: Int = 0
    private var rockCount: Long = 0
    private var dirOffset = 0

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

    private fun newRock(): Rock {
        val shape = shapes[(rockCount % shapes.size).toInt()]
        rockCount++
        val p = Point(2, highestRock + 3)
        return Rock(shape, p)
    }

    private fun moveRock(rock: Rock, dir: Direction): Boolean {
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

    private fun addRock(rock: Rock) {
        highestRock = maxOf(highestRock, rock.position.y + rock.shape.spaces.size)
        rock.points().forEach {
            this[it] = true
        }
    }

    private fun dropRock(directions: List<Direction>) {
        val rock = newRock()
        var comeToRest = false
        while (!comeToRest) {
            val dir = directions[dirOffset]
            dirOffset = (dirOffset + 1) % directions.size
            comeToRest = moveRock(rock, dir)
        }
        addRock(rock)
    }

    fun simulate(count: Long, directions: List<Direction>): Long {
        val lookBack = 50
        val previousStates = mutableMapOf<Int, Pair<Int, Int>>()
        while (rockCount < count && highestRock < 2 * lookBack) {
            dropRock(directions)
        }
        if (rockCount == count) {
            return highestRock.toLong()
        }
        while (rockCount < count && !previousStates.contains(hashState(lookBack))) {
            previousStates[hashState(lookBack)] = rockCount.toInt() to highestRock
            dropRock(directions)
        }
        if (rockCount == count) {
            return highestRock.toLong()
        }
        val (prevRockCount, prevHeight) = previousStates[hashState(lookBack)]!!
        val cycle = (rockCount - prevRockCount)
        val skippedCycles = (count - rockCount) / cycle
        val correction = skippedCycles * (highestRock - prevHeight)
        rockCount += skippedCycles * (rockCount - prevRockCount)
        while (rockCount < count) {
            dropRock(directions)
        }
        return highestRock + correction
    }

    private fun hashState(rows: Int): Int {
        if (highestRock + 1 < rows) {
            throw IllegalStateException()
        }
        val gridState = grid.slice((highestRock - rows)..highestRock)
        return ((rockCount % shapes.size) to gridState).hashCode()
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
    val result1 = Chamber().simulate(2022, instructions)
    val result2 = Chamber().simulate(1000000000000L, instructions)
    println("Part 1: $result1")
    println("Part 2: $result2")
}