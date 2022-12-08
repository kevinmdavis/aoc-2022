package aoc2022.day07

import aoc2022.readInput

sealed class Command {
    companion object {
        fun canParse(line: String) = line.startsWith("$")

        fun parse(line: String): Command {
            val parts = line.split(" ")
            if (parts.size == 3 && parts[1] == "cd") {
                return ChangeDirectory(parts[2])
            } else if (parts.size == 2 && parts[1] == "ls") {
                return List
            }
            throw IllegalArgumentException("Bad input: $line")
        }
    }

    object List : Command()
    data class ChangeDirectory(val target: String) : Command()
}

sealed class ListResult {
    companion object {
        fun parse(pwd: Directory, line: String): ListResult {
            val (left, right) = line.split(" ")
            if (left == "dir") {
                return Directory(pwd, right)
            }
            return File(pwd, right, left.toInt())
        }
    }
}

data class Directory(val parent: Directory?, val name: String) : ListResult() {
    companion object {
        val ROOT = Directory(null, "/")
    }
}

data class File(val parent: Directory, val name: String, val size: Int) : ListResult()


fun part1(lines: List<String>): Int {
    val totalSizes = mutableMapOf<Directory, Int>()
    var pwd = Directory.ROOT
    lines.forEach { line ->
        if (Command.canParse(line)) {
            val cmd = Command.parse(line)
            if (cmd is Command.ChangeDirectory) {
                if (cmd.target == "..") {
                    totalSizes.merge(pwd.parent!!, totalSizes.getOrDefault(pwd, 0), Int::plus)
                    pwd = pwd.parent!!
                } else if (cmd.target == "/") {
                    pwd = Directory.ROOT
                } else {
                    pwd = Directory(pwd, cmd.target)
                }
            }
        } else {
            when (val listResult = ListResult.parse(pwd, line)) {
                is File -> totalSizes.merge(pwd, listResult.size, Int::plus)
                else -> {}
            }
        }
    }
    while (pwd != Directory.ROOT) {
        totalSizes.merge(pwd.parent!!, totalSizes.getOrDefault(pwd, 0), Int::plus)
        pwd = pwd.parent!!
    }
    return totalSizes.values.filter { it <= 100000 }.sum()
}

fun main() {
    val lines = readInput()
    val result1 = part1(lines)
    println("Part 1: $result1")
}