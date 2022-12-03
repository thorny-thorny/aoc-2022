class PriorityFlags {
    companion object {
        private fun itemPriority(item: Char) = when (item) {
            in 'a'..'z' -> item - 'a' + 1
            in 'A'..'Z' -> item - 'A' + 27
            else -> throw Exception("Unknown item")
        }

        fun allRaised() = PriorityFlags().apply { value = ULong.MAX_VALUE }
    }

    private var value: ULong = 0UL

    fun priorityIfSet(item: Char): Int? {
        val priority = itemPriority(item)
        return when (value and (1UL shl (priority - 1))) {
            0UL -> null
            else -> priority
        }
    }

    fun set(item: Char) {
        value = value or (1UL shl (itemPriority(item) - 1))
    }

    operator fun times(other: PriorityFlags): PriorityFlags {
        return PriorityFlags().apply {
            value = value and other.value
        }
    }

    operator fun timesAssign(other: PriorityFlags) {
        value = value and other.value
    }
}

fun main() {
    fun part1(input: List<String>): Int {
        return input.sumOf { sack ->
            val compartmentSize = sack.length / 2
            val firstCompartmentFlags = PriorityFlags().apply {
                (0 until compartmentSize).forEach { set(sack[it]) }
            }

            (compartmentSize until sack.length).firstNotNullOf {
                firstCompartmentFlags.priorityIfSet(sack[it])
            }
        }
    }

    fun part2(input: List<String>): Int {
        return input
            .asSequence()
            .chunked(3)
            .map { sacks ->
                val flags = PriorityFlags.allRaised()
                (0 until sacks.lastIndex).forEach {index ->
                    flags *= PriorityFlags().apply {
                        sacks[index].forEach { set(it) }
                    }
                }

                sacks.last().firstNotNullOf { flags.priorityIfSet(it) }
            }
            .sum()
    }

    val testInput = readInput("Day03_test")
    check(part1(testInput) == 157)
    check(part2(testInput) == 70)

    val input = readInput("Day03")
    println(part1(input))
    println(part2(input))
}
