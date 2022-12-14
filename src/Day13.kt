sealed class PacketNode: Comparable<PacketNode> {
    data class Number(val value: Int): PacketNode()
    data class List(val content: kotlin.collections.List<PacketNode>): PacketNode()

    override fun compareTo(other: PacketNode): Int {
        if (this is Number && other is Number) {
            return value.compareTo(other.value)
        }

        val (thisList, otherList) = listOf(this, other).map {
            when (it) {
                is List -> it
                else -> List(listOf(it))
            }
        }

        return (0..maxOf(thisList.content.lastIndex, otherList.content.lastIndex)).firstNotNullOfOrNull {
            val first = thisList.content.getOrNull(it) ?: return@firstNotNullOfOrNull -1
            val second = otherList.content.getOrNull(it) ?: return@firstNotNullOfOrNull 1

            when (val compare = first.compareTo(second)) {
                0 -> null
                else -> compare
            }
        } ?: 0
    }
}

fun main() {
    fun parsePacketNode(iterator: Iterator<Char>): Pair<PacketNode?, Char?> {
        var char = iterator.next()
        when (char) {
            '[' -> {
                val content = mutableListOf<PacketNode>()
                do {
                    val (node, carry) = parsePacketNode(iterator)
                    node?.let { content.add(it) }
                } while (carry != ']')

                val listCarry = if (iterator.hasNext()) iterator.next() else null

                return PacketNode.List(content.toList()) to listCarry
            }
            ']' -> return null to char
            else -> {
                val string = mutableListOf<Char>()
                while (char in '0'..'9') {
                    string.add(char)
                    char = iterator.next()
                }

                return PacketNode.Number(string.joinToString("").toInt()) to char
            }
        }
    }

    fun parsePacket(line: String): PacketNode {
        val (node, carry) = parsePacketNode(line.iterator())
        if (node == null || carry != null) {
            throw Exception("Bad packet format")
        }

        return node
    }

    fun part1(input: List<String>): Int {
        return input
            .asSequence()
            .filter(String::isNotEmpty)
            .map(::parsePacket)
            .chunked(2)
            .withIndex()
            .filter { it.value.component1() < it.value.component2() }
            .sumOf { it.index + 1 }
    }

    fun part2(input: List<String>): Int {
        val inputPackets = input
            .filter(String::isNotEmpty)
            .map(::parsePacket)

        val divider1 = PacketNode.List(listOf(PacketNode.List(listOf(PacketNode.Number(2)))))
        val divider2 = PacketNode.List(listOf(PacketNode.List(listOf(PacketNode.Number(6)))))

        val packets = (inputPackets + divider1 + divider2).sorted()

        return (packets.indexOf(divider1) + 1) * (packets.indexOf(divider2) + 1)
    }

    val testInput = readInput("Day13_test")
    check(part1(testInput) == 13)
    check(part2(testInput) == 140)

    val input = readInput("Day13")
    println(part1(input))
    println(part2(input))
}
