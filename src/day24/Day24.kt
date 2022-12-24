package aoc2022.day24

import aoc2022.Graph
import aoc2022.Point
import aoc2022.readInput

data class Snowflake(val direction: Point)
data class Blizzard(val gridSize: Point, val snowflakes: Map<Point, List<Snowflake>>, val iteration: Long = 0) {

    private fun nextPoint(pos: Point, dir: Point): Point {
        val next = pos + dir
        return if (next.x == gridSize.x - 1) {
            Point(1, next.y)
        } else if (next.x == 0) {
            Point(gridSize.x - 2, next.y)
        } else if (next.y == gridSize.y - 1) {
            Point(next.x, 1)
        } else if (next.y == 0) {
            Point(next.x, gridSize.y - 2)
        } else {
            next
        }
    }

    fun next(): Blizzard {
        val nextSnowflakes = snowflakes.flatMap { (k, v) ->
            v.map { snowflake ->
                nextPoint(k, snowflake.direction) to snowflake
            }
        }.groupBy({ it.first }, { it.second })
        return Blizzard(gridSize, nextSnowflakes, iteration + 1)
    }

    fun isOpen(position: Point) = !snowflakes.contains(position)

    override fun toString(): String {
        return (0 until gridSize.y).joinToString("\n") { y ->
            (0 until gridSize.x).map { x ->
                if (snowflakes.contains(Point(x, y))) {
                    '*'
                } else {
                    '.'
                }
            }.joinToString("")
        } + "\n"
    }
}

fun Point.nextMoves(gridSize: Point): List<Point> {
    return listOf(
        Point(-1, 0),
        Point(1, 0),
        Point(0, 0),
        Point(0, 1),
        Point(0, -1),
    ).map { this + it }.filter {
        if (it.x == gridSize.x - 2 && it.y == gridSize.y - 1) {
            true
        } else if (it == Point(1, 0)) {
            true
        } else {
            it.x in 1 until gridSize.x - 1 && it.y in 1 until gridSize.y - 1
        }
    }
}

data class TemporalPoint(val iteration: Long, val position: Point)

class BlizzardGraph(var blizzard: Blizzard) : Graph<TemporalPoint> {
    override fun neighbors(node: TemporalPoint): List<TemporalPoint> {
        val iteration = node.iteration + 1
        check(iteration == blizzard.iteration || iteration == blizzard.iteration + 1)
        if (iteration == blizzard.iteration + 1) {
            blizzard = blizzard.next()
        }
        return node.position.nextMoves(blizzard.gridSize).filter { blizzard.isOpen(it) }.map {
            TemporalPoint(iteration, it)
        }
    }
}

class WaypointGraph<T>(val graph: Graph<T>, val waypoints: List<(T) -> Boolean>) : Graph<Waypoint<T>> {
    override fun neighbors(node: Waypoint<T>): List<Waypoint<T>> {
        return graph.neighbors(node.current).map {
            if (waypoints[node.waypoint](node.current)) {
                Waypoint(it, node.waypoint + 1)
            } else {
                Waypoint(it, node.waypoint)
            }
        }
    }

    fun bfs(start: T): List<Waypoint<T>>? {
        return bfs(Waypoint(start)) {
            it.waypoint == waypoints.size - 1 && waypoints.last()(it.current)
        }
    }
}

data class Waypoint<T>(val current: T, val waypoint: Int = 0)

fun main() {
    val lines = readInput()
    val snowflakes = lines.flatMapIndexed { y, row ->
        row.mapIndexed { x, value ->
            Point(x, y) to when (value) {
                '>' -> Point(1, 0)
                '<' -> Point(-1, 0)
                'v' -> Point(0, 1)
                '^' -> Point(0, -1)
                else -> null
            }?.let { Snowflake(it) }
        }
    }.filter { it.second != null }.associate { it.first to listOf(it.second!!) }
    val gridSize = Point(lines[0].length, lines.size)
    val blizzard = Blizzard(gridSize, snowflakes)
    val graph = BlizzardGraph(blizzard)
    val start = Point(1, 0)
    val end = gridSize + Point(-2, -1)
    val result1 = graph.bfs(TemporalPoint(0, Point(1, 0))) {
        it.position == end
    }!!.size - 1
    val isStart = { it: TemporalPoint -> it.position == start }
    val isEnd = { it: TemporalPoint -> it.position == end }
    val waypointGraph = WaypointGraph(BlizzardGraph(blizzard), listOf(isEnd, isStart, isEnd))
    val result2 = waypointGraph.bfs(TemporalPoint(0, start))!!.size - 1
    println("Part 1: $result1")
    println("Part 2: $result2")
}