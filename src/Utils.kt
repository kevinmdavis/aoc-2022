package aoc2022

fun readInput(): List<String> = generateSequence(::readLine).toList()

data class Point(val x: Int, val y: Int) {
    operator fun plus(other: Point): Point {
        return Point(x + other.x, y + other.y)
    }

    operator fun minus(other: Point): Point {
        return Point(x - other.x, y - other.y)
    }

    companion object {
        val Zero = Point(0, 0)
        val Up = Point(0, 1)
        val Down = Point(0, -1)
        val Left = Point(-1, 0)
        val Right = Point(1, 0)
    }
}

class Grid<T>(private val data: List<List<T>>) : List<List<T>> by data {

    fun get(x: Int, y: Int): T {
        return data[y][x]
    }

    fun get(p: Point): T {
        return get(p.x, p.y)
    }

    private fun inBounds(p: Point): Boolean {
        return p.x >= 0 && p.x < data[0].size && p.y >= 0 && p.y < data.size
    }

    fun neighbors(p: Point): List<Point> = listOf(
        Point(p.x - 1, p.y),
        Point(p.x + 1, p.y),
        Point(p.x, p.y - 1),
        Point(p.x, p.y + 1),
    ).filter(::inBounds)

    fun find(predicate: (T) -> Boolean): List<Point> {
        return data.mapIndexed { y, row ->
            row.mapIndexed { x, v ->
                if (predicate(v)) {
                    Point(x, y)
                } else {
                    null
                }
            }
        }.flatten().filterNotNull()
    }

    inner class Graph(private val edgePredicate: (from: Point, to: Point) -> Boolean) : aoc2022.Graph<Point> {
        override fun neighbors(node: Point): List<Point> {
            return this@Grid.neighbors(node).filter { neighbor ->
                edgePredicate(node, neighbor)
            }
        }
    }
}

interface Graph<T> {
    fun neighbors(node: T): List<T>

    fun bfs(start: T, endPredicate: (T) -> Boolean): List<T>? {
        val parents = mutableMapOf<T, T?>(start to null)
        val q = ArrayDeque<T>()
        q.add(start)
        while (q.isNotEmpty()) {
            val current = q.removeFirst()
            if (endPredicate(current)) {
                val path = mutableListOf(current)
                var prev = current
                while (prev != start) {
                    prev = parents[prev]!!
                    path.add(prev)
                }
                return path.reversed()
            }
            neighbors(current).forEach {
                if (!parents.contains(it)) {
                    q.add(it)
                    parents[it] = current
                }
            }
        }
        return null
    }

    fun bfs(start: T, end: T) = bfs(start) { it == end }
}