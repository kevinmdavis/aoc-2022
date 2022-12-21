package aoc2022.day19

import aoc2022.readInput
import kotlin.math.ceil

fun sumFirstInts(n: Int): Int = n * (n + 1) / 2

data class Blueprint(
    val oreRobot: Resources,
    val clayRobot: Resources,
    val obsidianRobot: Resources,
    val geodeRobot: Resources
) {
    fun optimalGeodes(
        minutes: Int,
        resources: Resources = Resources(),
        production: Resources = Resources(ore = 1),
        geodesToBeat: Int = 0
    ): Resources {
        if (minutes == 0 || stopEarly(resources, production, minutes, geodesToBeat)) {
            return resources
        }
        var best = geodesToBeat
        var choices = options().mapNotNull { (cost, gain) ->
            val minutesUntil = cost.minutesUntil(resources, production)
            minutesUntil?.let {
                val timePassed = minutesUntil + 1
                if (timePassed > minutes) {
                    null
                } else {
                    val nextResources = resources - cost + (production * timePassed)
                    val optimalResources = optimalGeodes(minutes - timePassed, nextResources, production + gain, best)
                    best = maxOf(best, optimalResources.geodes)
                    optimalResources
                }
            }
        }
        if (choices.isEmpty()) {
            // Do nothing but wait out the clock.
            choices = listOf(resources + production * (minutes - 1))
        }
        return choices.maxBy { it.geodes }
    }


    fun stopEarly(
        resources: Resources,
        production: Resources,
        minutesRemaining: Int,
        geodesToBeat: Int
    ): Boolean {
        // Don't produce more resources every turn than could possibly be consumed every turn
        val expenses = options()
        if (production.ore > expenses.maxOf { it.first.ore } ||
            production.clay > expenses.maxOf { it.first.clay } ||
            production.obsidian > expenses.maxOf { it.first.obsidian }) {
            return true
        }
        // Stop early if it's impossible to produce more geodes than the max (if a new geode robot was produced every
        // turn)
        val bestPossibleGeodes =
            resources.geodes + (sumFirstInts(production.geodes + minutesRemaining) - sumFirstInts(production.geodes - 1))
        return bestPossibleGeodes < geodesToBeat
    }

    fun options(): List<Pair<Resources, Resources>> = listOf(
        oreRobot to Resources(ore = 1),
        clayRobot to Resources(clay = 1),
        obsidianRobot to Resources(obsidian = 1),
        geodeRobot to Resources(geodes = 1),
    )
}

data class Resources(val ore: Int = 0, val clay: Int = 0, val obsidian: Int = 0, val geodes: Int = 0) {
    fun minutesUntil(resources: Resources, production: Resources): Int? {
        val parts = listOf(
            Triple(geodes, resources.geodes, production.geodes),
            Triple(obsidian, resources.obsidian, production.obsidian),
            Triple(clay, resources.clay, production.clay),
            Triple(ore, resources.ore, production.ore),
        )
        if (parts.all { it.second >= it.first }) {
            return 0
        }
        if (parts.any { it.first > 0 && it.third == 0 }) {
            return null
        }
        return parts.filter { it.third != 0 }.maxOf { ceil((it.first - it.second) / it.third.toDouble()).toInt() }
    }

    operator fun times(n: Int): Resources {
        return Resources(ore * n, clay * n, obsidian * n, geodes * n)
    }

    operator fun plus(other: Resources): Resources {
        return Resources(ore + other.ore, clay + other.clay, obsidian + other.obsidian, geodes + other.geodes)
    }

    operator fun minus(other: Resources): Resources {
        return Resources(ore - other.ore, clay - other.clay, obsidian - other.obsidian, geodes - other.geodes)
    }

    fun max(other: Resources): Resources {
        return Resources(
            maxOf(ore, other.ore),
            maxOf(clay, other.clay),
            maxOf(obsidian, other.obsidian),
            maxOf(geodes, other.geodes)
        )
    }
}

val regex = ("Blueprint (\\d+): " +
        "Each ore robot costs (\\d+) ore. " +
        "Each clay robot costs (\\d+) ore. " +
        "Each obsidian robot costs (\\d+) ore and (\\d+) clay. " +
        "Each geode robot costs (\\d+) ore and (\\d+) obsidian.").toRegex()

fun main() {
    val blueprints = readInput().map { line ->
        regex.matchEntire(line)!!.groupValues.drop(2).map { it.toInt() }
    }.map {
        Blueprint(
            oreRobot = Resources(ore = it[0]),
            clayRobot = Resources(ore = it[1]),
            obsidianRobot = Resources(ore = it[2], clay = it[3]),
            geodeRobot = Resources(ore = it[4], obsidian = it[5]),
        )
    }
    val result1 = blueprints.mapIndexed { idx, blueprint -> (idx + 1) * blueprint.optimalGeodes(24).geodes }.sum()
    val result2 = blueprints.take(3).map { it.optimalGeodes(32).geodes }.reduce { a, b -> a * b }
    println("Part 1: $result1")
    println("Part 2: $result2")
}