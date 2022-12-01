fun main() {
    fun part1(input: List<String>): Int {
        return input
            .asSequence()
            .map { it.toIntOrNull() }
            .sumIntGroups()
            .maxOrNull() ?: throw Exception("There's no food D:")
    }

    fun part2(input: List<String>): Int {
        return input
            .asSequence()
            .map { it.toIntOrNull() }
            .sumIntGroups()
            .topInts(3)
            .sum()
    }

    val testInput = readInput("Day01_test")
    check(part1(testInput) == 24000)

    val input = readInput("Day01")
    println(part1(input))
    println(part2(input))
}

// Emits sums of groups of Int separated by nulls
fun Sequence<Int?>.sumIntGroups(): Sequence<Int> {
    return sequence {
        var acc: Int? = null
        val iterator = iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            when (next) {
                null -> {
                    if (acc != null) {
                        yield(acc)
                        acc = null
                    }
                }
                else -> acc = (acc ?: 0) + next
            }
        }
        
        if (acc != null) {
            yield(acc)
        }
    }
}

// Returns top n maximum Ints in a sequence
fun Sequence<Int>.topInts(n: Int): List<Int> {
    var topInts = MutableList(n, { 0 })
    for (value in this) {
        if (value > topInts.last()) {
            topInts.add(value)
            topInts.sortDescending()
            topInts.removeLast()
        }
    }

    return topInts
}
