package aoc2022.day20

import aoc2022.readInput

data class ListNode(val n: Long) {
    var next: ListNode? = null
        set(value) {
            value?.let { it.prev = this }
            field = value
        }

    var prev: ListNode? = null
        private set

    fun swapLeft() {
        val left = this.prev
        val right = this.next
        val tmp = left!!.prev!!
        left.next = right
        this.next = left
        tmp.next = this
    }

    fun swapRight() {
        val left = this.prev
        val right = this.next
        left!!.next = right
        val tmp = right!!.next
        right.next = this
        this.next = tmp
    }

    fun wrapTo(other: ListNode) {
        check(other.prev == null)
        check(this.next == null)
        this.next = other
    }

    fun take(n: Int): List<Long> {
        var current = this
        val result = mutableListOf<Long>()
        repeat(n) {
            result.add(current.n)
            current = current.next!!
        }
        return result
    }
}

fun mixFile(input: List<Int>, decryptionKey: Long = 1, times: Int = 1): List<Long> {
    val nodes = input.map { it * decryptionKey }.map { ListNode(it) }
    nodes.windowed(2).forEach { it[0].next = it[1] }
    nodes.last().wrapTo(nodes.first())
    repeat(times) {
        for (node in nodes) {
            val moveCount: Int = ((node.n) % (nodes.size - 1)).toInt()
            if (moveCount < 0) {
                repeat(-moveCount) {
                    node.swapLeft()
                }
            } else {
                repeat(moveCount) {
                    node.swapRight()
                }
            }
        }
    }
    return nodes.find { it.n == 0L }!!.take(input.size)
}

fun main() {
    val input = readInput().map { it.toInt() }
    val mixed = mixFile(input)
    val result1 = mixed[1000 % mixed.size] + mixed[2000 % mixed.size] + mixed[3000 % mixed.size]
    val mixed2 = mixFile(input, decryptionKey = 811589153, times = 10)
    val result2 = mixed2[1000 % mixed2.size] + mixed2[2000 % mixed2.size] + mixed2[3000 % mixed2.size]
    println("Part 1: $result1")
    println("Part 2: $result2")
}