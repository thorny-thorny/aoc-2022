class ItemsCounter {
    companion object {
        val distinctPriorities = itemPriority('Z') - itemPriority('a') + 1

        fun itemPriority(item: Char) = when (item) {
            in 'a'..'z' -> item - 'a' + 1
            in 'A'..'Z' -> item - 'A' + 27
            else -> throw Exception("Unknown item")
        }
    }

    private val itemsCount = ByteArray(distinctPriorities) { 0 }

    operator fun get(priority: Int) = itemsCount[priority - 1].toInt()

    operator fun set(priority: Int, count: Int) {
        itemsCount[priority - 1] = count.toByte()
    }
}

fun main() {
    fun part1(input: List<String>): Int {
        return input.sumOf { sack ->
            val counter = ItemsCounter()
            val compartmentSize = sack.length / 2
            (0 until compartmentSize).forEach {
                counter[ItemsCounter.itemPriority(sack[it])] += 1
            }

            (compartmentSize until sack.length).firstNotNullOf {
                val priority = ItemsCounter.itemPriority(sack[it])
                when (counter[priority]) {
                    0 -> null
                    else -> priority
                }
            }
        }
    }

    fun part2(input: List<String>): Int {
        return input
            .asSequence()
            .chunked(3)
            .map { sacks ->
                val counter = ItemsCounter()
                sacks.forEachIndexed { index, sack ->
                    sack.forEach {
                        val priority = ItemsCounter.itemPriority(it)
                        val count = counter[priority]
                        if (count == index) {
                            if (index == sacks.lastIndex) {
                                return@map priority
                            } else {
                                counter[priority] = count + 1
                            }
                        }
                    }
                }

                throw Exception("Badge not found")
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
