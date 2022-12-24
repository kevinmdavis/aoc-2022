package aoc2022.day22

import aoc2022.Point
import aoc2022.TokenStream
import aoc2022.readInput
import org.jetbrains.kotlinx.multik.api.*
import org.jetbrains.kotlinx.multik.api.linalg.dot
import org.jetbrains.kotlinx.multik.ndarray.data.*
import org.jetbrains.kotlinx.multik.ndarray.operations.*
import kotlin.math.pow

const val NOTHING = 0
const val OPEN = 1
const val OBSTACLE = 2

operator fun <T> MultiArray<T, D2>.get(pos: Point): T {
    return this[pos.y, pos.x]
}

interface Movement {
    fun next(dir: Point): Pair<Point, Movement>

    val pos: Point
}

data class Wraparound(private val grid: MultiArray<Int, D2>, override val pos: Point) : Movement {
    override fun next(dir: Point): Pair<Point, Wraparound> {
        return if (grid[pos + dir] == NOTHING) {
            var current = pos + dir
            while (grid[current - dir] != NOTHING) {
                current -= dir
            }
            dir to Wraparound(grid, current)
        } else {
            dir to Wraparound(grid, pos + dir)
        }
    }
}

class Cube(val width: Int, val faces: Map<MultiArray<Int, D1>, Face>) {
    private val top: Face get() = faces[mk.ndarray(listOf(0, 1, 0))]!!
    private val center = (width - 1.0) / 2.0
    val faceCenter = mk.ndarray(listOf(center, 0.0, center))

    fun startingPoint(): CubeMovement {
        val x = top.data[0].indexOfFirst { it == OPEN }
        return CubeMovement(this, top, Point(x, 0))
    }

    companion object {
        val rotateLeft = mk.ndarray(mk[mk[0, -1, 0], mk[1, 0, 0], mk[0, 0, 1]])
        val rotateRight = rotateLeft.transpose()
        val rotateForward = mk.ndarray(mk[mk[1, 0, 0], mk[0, 0, 1], mk[0, -1, 0]])
        val rotateBackward = rotateForward.transpose()

        private fun rotations(cubeWidth: Int): List<Pair<Rotation, Point>> {
            return listOf(
                rotateLeft to Point(-cubeWidth, 0),
                rotateRight to Point(cubeWidth, 0),
                rotateForward to Point(0, -cubeWidth),
                rotateBackward to Point(0, cubeWidth),
            )
        }

        fun parse(input: D2Array<Int>): Cube {
            val cubeWidth = (input.flatten().count { it != NOTHING }.toDouble() / 6).pow(1 / 2.0).toInt()
            val x = input[1].indexOfFirst { it != NOTHING }
            val startPos = Point(x, 1)
            val startRotation = mk.identity<Int>(3)
            val startFace = Face(startRotation, startPos, input.getSquare(startPos, cubeWidth))
            val faces = mutableMapOf(startRotation.normal() to startFace)
            val visited = mutableSetOf(startRotation.normal())
            val q = ArrayDeque<Pair<Rotation, Point>>()
            q.add(startRotation to startPos)
            while (q.isNotEmpty()) {
                val (rot, pos) = q.removeFirst()
                for ((rotate, gridTranslate) in rotations(cubeWidth)) {
                    val next = pos + gridTranslate
                    val nextRotation = rot dot rotate
                    if (inBounds(input, next) && input[next] != NOTHING && !visited.contains(nextRotation.normal())) {
                        val f = Face(nextRotation, next, input.getSquare(next, cubeWidth))
                        faces[nextRotation.normal()] = f
                        visited.add(nextRotation.normal())
                        q.add(nextRotation to next)
                    }
                }
            }
            check(faces.size == 6)
            return Cube(cubeWidth, faces)
        }
    }

    data class Face(val rotation: D2Array<Int>, val pos: Point, val data: MultiArray<Int, D2>)
}

class CubeMovement(private val cube: Cube, private val face: Cube.Face, private val offset: Point) : Movement {

    override fun next(dir: Point): Pair<Point, CubeMovement> {
        val nextOffset = offset + dir
        val localRotation = if (nextOffset.x < 0) {
            Cube.rotateLeft
        } else if (nextOffset.x >= cube.width) {
            Cube.rotateRight
        } else if (nextOffset.y < 0) {
            Cube.rotateForward
        } else if (nextOffset.y >= cube.width) {
            Cube.rotateBackward
        } else {
            return dir to CubeMovement(cube, face, nextOffset)
        }
        val globalRotation = face.rotation dot localRotation
        val nextFace = cube.faces[globalRotation.normal()]!!
        val rotationCorrection = nextFace.rotation.transpose() dot globalRotation
        val wrappedPoint = Point((nextOffset.x + cube.width) % cube.width, (nextOffset.y + cube.width) % cube.width)
        val centeredPoint = wrappedPoint.doubleVec() - cube.faceCenter
        val mappedPoint =
            ((rotationCorrection.map { it.toDouble() } dot centeredPoint) + cube.faceCenter).map { it.toInt() }
        val mappedDir = rotationCorrection dot dir.vec()
        return mappedDir.point() to CubeMovement(cube, nextFace, mappedPoint.point())
    }

    override val pos: Point
        get() = face.pos + offset

}

fun startingPoint(grid: MultiArray<Int, D2>): Wraparound {
    return Wraparound(grid, Point(grid[1].indexOfFirst { it == OPEN }, 1))
}

fun Point.turnRight() = Point(-this.y, this.x)
fun Point.turnLeft() = Point(this.y, -this.x)
fun Point.doubleVec() = mk.ndarray(listOf(this.x.toDouble(), 0.0, this.y.toDouble()))
fun Point.vec() = mk.ndarray(listOf(this.x, 0, this.y))

fun D1Array<Int>.point() = Point(this[0], this[2])

fun executeInstructions(grid: MultiArray<Int, D2>, start: Movement, instructions: List<Instruction>): Int {
    var current = start
    var dir = Point.Right
    for (instruction in instructions) {
        when (instruction) {
            is Instruction.Move -> {
                var i = 0
                val movement = current.next(dir)
                var nextDir = movement.first
                var next = movement.second
                while (i < instruction.n && grid[next.pos] == OPEN) {
                    current = next
                    dir = nextDir
                    val nextMovement = current.next(dir)
                    nextDir = nextMovement.first
                    next = nextMovement.second
                    i++
                }
            }

            is Instruction.Turn -> {
                dir = instruction.rotate(dir)
            }
        }
    }
    val dirScore = when (dir) {
        Point.Right -> 0
        Point.Up -> 1
        Point.Left -> 2
        Point.Down -> 3
        else -> throw IllegalArgumentException()
    }
    return 1000 * current.pos.y + 4 * current.pos.x + dirScore
}

sealed class Instruction {
    class Move(val n: Int) : Instruction()
    class Turn(private val turn: Char) : Instruction() {
        fun rotate(dir: Point): Point {
            return when (this.turn) {
                'L' -> dir.turnLeft()
                'R' -> dir.turnRight()
                else -> throw IllegalArgumentException()
            }
        }
    }
}

fun parseInstructions(line: String): List<Instruction> {
    val tokens = TokenStream(line)
    val result = mutableListOf<Instruction>()
    while (tokens.remainingLength > 0) {
        result.add(Instruction.Move(tokens.consumeWhile { it.isDigit() }.toInt()))
        if (tokens.remainingLength == 0) {
            break
        }
        result.add(Instruction.Turn(tokens.consume().first()))
    }
    return result
}


fun inBounds(grid: MultiArray<Int, D2>, pos: Point): Boolean {
    return pos.x >= 0 && pos.x < grid.shape[1] && pos.y >= 0 && pos.y < grid.shape[0]
}

fun <T> D2Array<T>.normal() = this[0..3, 1].copy()

typealias Rotation = D2Array<Int>

fun <T> D2Array<T>.getSquare(pos: Point, size: Int): MultiArray<T, D2> {
    return this[pos.y..pos.y + size, pos.x..pos.x + size]
}

fun toString(grid: MultiArray<Int, D2>, pos: Point, dir: Point) {
    println(grid.toListD2().mapIndexed { y, row ->
        row.mapIndexed() { x, v ->
            if (x == pos.x && y == pos.y) {
                when (dir) {
                    Point.Up -> 'v'
                    Point.Down -> '^'
                    Point.Left -> '<'
                    Point.Right -> '>'
                    else -> throw IllegalArgumentException()
                }
            } else {
                when (v) {
                    NOTHING -> ' '
                    OPEN -> '.'
                    OBSTACLE -> '#'
                    else -> throw IllegalArgumentException()
                }
            }
        }.joinToString("")
    }.joinToString("\n"))
}

fun main() {
    val lines = readInput()
    val map = lines.takeWhile { it.isNotBlank() }
    val width = map.maxOf { it.length }
    val height = map.size
    val grid = mk.d2arrayIndices(height + 2, width + 2) { y, x ->
        if (y == 0 || y == height + 1 || x == 0 || x == width + 1) {
            NOTHING
        } else {
            val row = lines[y - 1]
            if (x - 1 >= row.length) {
                NOTHING
            } else {
                when (lines[y - 1][x - 1]) {
                    '.' -> OPEN
                    '#' -> OBSTACLE
                    ' ' -> NOTHING
                    else -> throw IllegalArgumentException("Unknown value ${lines[y - 1][x - 1]}")
                }
            }
        }
    }
    val instructions = parseInstructions(lines.last())
    val start = startingPoint(grid)
    val result1 = executeInstructions(grid, start, instructions)
    val cube = Cube.parse(grid)
    val result2 = executeInstructions(grid, cube.startingPoint(), instructions)
    println("Part 1: $result1")
    println("Part 2: $result2")
}