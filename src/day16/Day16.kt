package aoc2022.day16

import aoc2022.readInput
import java.util.Collections

data class Valve(val flow: Int, val adjacent: Set<String>)

typealias Graph = Map<String, Valve>

fun Graph.shortestPaths(): Map<Pair<String, String>, Int> {
    val distances = mutableMapOf<Pair<String, String>, Int>()
    keys.forEach { a ->
        keys.forEach { b ->
            distances[a to b] = Int.MAX_VALUE
        }
    }
    this.entries.forEach { (k, v) ->
        distances[k to k] = 0
        v.adjacent.forEach { adj ->
            distances[k to adj] = 1
            distances[adj to k] = 1
        }
    }
    keys.forEach { a ->
        keys.forEach { b ->
            keys.forEach { c ->
                val newPath: Long = distances[b to a]!!.toLong() + distances[a to c]!!.toLong()
                if (distances[b to c]!! > newPath) {
                    distances[b to c] = newPath.toInt()
                }
            }
        }
    }
    return distances
}

fun subsetPermutations(nodes: Set<String>) = sequence {
    val values = nodes.toList()
    for (i in 0 until (1 shl values.size)) {
        val a = mutableListOf<String>()
        val b = mutableListOf<String>()
        for (j in values.indices) {
            val present = (i and (1 shl j)) != 0
            if (present) {
                a.add(values[j])
            } else {
                b.add(values[j])
            }
        }
        yield(a to b)
    }
}

fun Graph.bestPressureRelease(
    start: String,
    timeRemaining: Int,
    remainingValves: MutableList<String>,
    distances: Map<Pair<String, String>, Int>,
): Int {
    val valve = get(start)!!
    val timeConsumed = if (valve.flow == 0) {
        0
    } else {
        1
    }
    val pressureReleased = valve.flow * (timeRemaining - 1)
    if (remainingValves.isEmpty()) {
        return pressureReleased
    }
    if (timeRemaining <= 0) {
        return 0
    }
    val bestCandidate = remainingValves.indices.map { idx ->
        val candidate = remainingValves[idx]
        Collections.swap(remainingValves, idx, remainingValves.size - 1)
        remainingValves.removeLast()
        val pressure = bestPressureRelease(
            candidate,
            timeRemaining - timeConsumed - distances[start to candidate]!!,
            remainingValves,
            distances
        )
        remainingValves.add(candidate)
        Collections.swap(remainingValves, idx, remainingValves.size - 1)
        pressure
    }
    return pressureReleased + bestCandidate.max()
}

val regex = "Valve (\\w\\w) has flow rate=(\\d+); tunnels? leads? to valves? (.*)".toRegex()

fun main() {
    val valves = readInput().associate { line ->
        val match: MatchResult = regex.matchEntire(line)!!
        val name = match.groups[1]!!.value
        val flow = match.groups[2]!!.value.toInt()
        val adjacent = match.groups[3]!!.value.split(", ")
        name to Valve(flow, adjacent.toSet())
    }
    val distances = valves.shortestPaths()
    val usableValves = valves.filterValues { it.flow > 0 }

    val result1 = valves.bestPressureRelease("AA", 30, usableValves.keys.toMutableList(), distances)
    val permutations = subsetPermutations(usableValves.keys).toList()
    val result2 = permutations.maxOf {(a, b) ->
        val me = valves.bestPressureRelease("AA", 26, a, distances)
        val elephant = valves.bestPressureRelease("AA", 26, b, distances)
        me + elephant
    }

    println("Part 1: $result1")
    println("Part 2: $result2")
}