package aoc2022.day13

import aoc2022.TokenStream
import aoc2022.readInput

sealed class Packet : Comparable<Packet> {
    companion object {
        fun parse(tokens: TokenStream): Packet = if (tokens.peek() == '[') {
            ListPacket.parse(tokens)
        } else {
            IntPacket.parse(tokens)
        }

        fun distress(num: Int): Packet = ListPacket(ListPacket(num))
    }
}

data class IntPacket(val i: Int) : Packet(), Comparable<Packet> {
    override fun toString(): String {
        return i.toString()
    }

    override fun compareTo(other: Packet): Int {
        if (other is ListPacket) {
            return -other.compareTo(this)
        }
        check(other is IntPacket)
        return i.compareTo(other.i)
    }

    companion object {
        fun parse(tokens: TokenStream): IntPacket = IntPacket(tokens.consumeWhile { it.isDigit() }.toInt())
    }
}

data class ListPacket(val items: List<Packet>) : Packet(), Comparable<Packet> {
    constructor(num: Int) : this(IntPacket(num))
    constructor(msg: Packet) : this(listOf(msg))

    override fun toString(): String {
        return "[" + items.joinToString(",") { it.toString() } + "]"
    }

    override fun compareTo(other: Packet): Int {
        if (other is IntPacket) {
            return compareTo(ListPacket(listOf(other)))
        }
        check(other is ListPacket)
        items.zip(other.items).forEach { (a, b) ->
            val cmp = a.compareTo(b)
            if (cmp != 0) {
                return cmp
            }
        }
        return items.size.compareTo(other.items.size)
    }

    companion object {
        fun parse(tokens: TokenStream): ListPacket {
            tokens.consume("[")
            val items = mutableListOf<Packet>()
            while (tokens.peek() != ']') {
                items.add(Packet.parse(tokens))
                tokens.consume(",", optional = true)
            }
            tokens.consume("]")
            return ListPacket(items)
        }
    }
}

fun main() {
    val packets = readInput().filter { it.isNotBlank() }.map { Packet.parse(TokenStream(it)) }
    val result1 = packets.asSequence().chunked(2).map {
        it[0].compareTo(it[1])
    }.withIndex().filter { it.value == -1 }.sumOf { it.index + 1 }
    val a = Packet.distress(2)
    val b = Packet.distress(6)
    val result2 = (packets.count { it < a } + 1) * (packets.count { it < b } + 2)
    println("Part 1: $result1")
    println("Part 2: $result2")
}