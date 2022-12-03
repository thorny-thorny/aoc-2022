import java.util.PriorityQueue

fun main() {
    fun part1(input: List<String>): Int {
        return input
            .asSequence()
            .map(String::toIntOrNull)
            .sumGroups()
            .maxOrNull() ?: throw Exception("There's no food D:")
    }

    fun part2(input: List<String>): Int {
        return input
            .asSequence()
            .map(String::toIntOrNull)
            .sumGroups()
            .top(3)
            .sum()
    }

    val testInput = readInput("Day01_test")
    check(part1(testInput) == 24000)

    val input = readInput("Day01")
    println(part1(input))
    println(part2(input))
}

// Emits sums of groups of Int separated by nulls
fun Sequence<Int?>.sumGroups() = sequence {
    var acc: Int? = null
    val iterator = iterator()
    while (iterator.hasNext()) {
        acc = when (val next = iterator.next()) {
            null -> {
                acc?.let { yield(it) }
                null
            }
            else -> (acc ?: 0) + next
        }
    }

    acc?.let { yield(it) }
}

// Returns top n maximum Ints in a sequence
fun Sequence<Int>.top(n: Int): List<Int> {
    val top = PriorityQueue<Int>(n)
    for (value in this) {
        if (top.size < n || value > top.peek()) {
            top.add(value)
            if (top.size > n) {
                top.poll()
            }
        }
    }

    return top.toList()
}
