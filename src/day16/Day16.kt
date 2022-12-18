package aoc2022.day16

import aoc2022.readInput
import java.util.Collections

data class Valve(val name: String, val flow: Int)

data class Path(val valves: List<Int>, val pressure: Int) {
    private val valveSet: Int

    init {
        var x = 0
        for (i in valves) {
            x = x or (1 shl i)
        }
        valveSet = x
    }

    fun disjoint(other: Path): Boolean = (valveSet and other.valveSet) == 1
}

data class Graph(val valves: List<Valve>, val distances: List<List<Int>>) {

    fun pathPermutations(remainingTime: Int, remainingOptions: List<Int>, start: Int = 0): List<Path> {
        val result = mutableListOf<Path>()
        pathPermutations(result, mutableListOf(start), remainingTime, remainingOptions.toMutableList(), 0)
        return result
    }

    private fun pathPermutations(
        result: MutableList<Path>,
        currentPath: MutableList<Int>,
        remainingTime: Int,
        remainingOptions: MutableList<Int>,
        currentPressure: Int
    ) {
        val current = valves[currentPath.last()]
        val pressure = currentPressure + remainingTime * current.flow
        result.add(Path(currentPath.toList(), pressure))
        for (i in remainingOptions.indices) {
            val candidate = remainingOptions[i]
            val distance = distances[currentPath.last()][candidate] + 1
            if (distance >= remainingTime) {
                continue
            }
            currentPath.add(candidate)
            Collections.swap(remainingOptions, i, remainingOptions.size - 1)
            remainingOptions.removeLast()
            pathPermutations(result, currentPath, remainingTime - distance, remainingOptions, pressure)
            remainingOptions.add(candidate)
            Collections.swap(remainingOptions, i, remainingOptions.size - 1)
            currentPath.removeLast()
        }
    }

    companion object {
        private fun create(valves: Map<String, Valve>, distances: Map<Pair<String, String>, Int>): Graph {
            val sortedValves = valves.filter { (k, v) -> k == "AA" || v.flow > 0 }.toSortedMap()
            val filteredDistances = sortedValves.map { (from, _) ->
                sortedValves.map { (to, _) ->
                    distances[from to to]!!
                }
            }
            return Graph(sortedValves.map { it.value }, filteredDistances)
        }

        fun parse(lines: List<String>): Graph {
            val nodes = lines.associate { line ->
                val match: MatchResult = regex.matchEntire(line)!!
                val name = match.groups[1]!!.value
                val adjacent = match.groups[3]!!.value.split(", ")
                name to (adjacent to match.groups[2]!!.value.toInt())
            }
            val distances = shortestPaths(nodes.mapValues { it.value.first })
            return create(nodes.map { it.key to Valve(it.key, it.value.second) }.toMap(), distances)
        }
    }
}

fun shortestPaths(nodes: Map<String, List<String>>): Map<Pair<String, String>, Int> {
    val distances = mutableMapOf<Pair<String, String>, Int>()
    nodes.keys.forEach { a ->
        nodes.keys.forEach { b ->
            distances[a to b] = Int.MAX_VALUE
        }
    }
    nodes.entries.forEach { (k, v) ->
        distances[k to k] = 0
        v.forEach { adj ->
            distances[k to adj] = 1
            distances[adj to k] = 1
        }
    }
    nodes.keys.forEach { a ->
        nodes.keys.forEach { b ->
            nodes.keys.forEach { c ->
                val newPath: Long = distances[b to a]!!.toLong() + distances[a to c]!!.toLong()
                if (distances[b to c]!! > newPath) {
                    distances[b to c] = newPath.toInt()
                }
            }
        }
    }
    return distances
}

val regex = "Valve (\\w\\w) has flow rate=(\\d+); tunnels? leads? to valves? (.*)".toRegex()

fun part1(graph: Graph): Int {
    val permutations = graph.pathPermutations(30, (1 until graph.valves.size).toList(), 0)
    return permutations.maxOf { it.pressure }
}

fun part2(graph: Graph): Int {
    val permutations = graph.pathPermutations(26, (1 until graph.valves.size).toList(), 0)
    val paths = permutations.sortedBy { it.pressure }.reversed()
    var best = 0
    for (i in paths.indices) {
        val me = paths[i]
        for (j in i + 1 until paths.size) {
            val elephant = paths[j]
            if (!me.disjoint(elephant)) {
                continue
            }
            val pressure = me.pressure + elephant.pressure
            best = maxOf(best, pressure)
            if (pressure < best) {
                break
            }
        }
    }
    return best
}

fun main() {
    val graph = Graph.parse(readInput())
    val result1 = part1(graph)
    val result2 = part2(graph)

    println("Part 1: $result1")
    println("Part 2: $result2")
}