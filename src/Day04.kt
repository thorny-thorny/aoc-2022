fun main() {
    fun decodeLine(line: String) = line
        .split(',')
        .map {
            it
                .split('-')
                .map(String::toInt)
                .let { (start, endInclusive) -> start..endInclusive }
        }
        .let { (first, second) -> first to second }

    fun part1(input: List<String>): Int {
        return input.count {
            val (first, second) = decodeLine(it)
            first in second || second in first
        }
    }

    fun part2(input: List<String>): Int {
        return input.count {
            val (first, second) = decodeLine(it)
            first.first in second || first.last in second || second.first in first || second.last in first
        }
    }

    val testInput = readInput("Day04_test")
    check(part1(testInput) == 2)
    check(part2(testInput) == 4)

    val input = readInput("Day04")
    println(part1(input))
    println(part2(input))
}

operator fun IntRange.contains(other: IntRange) = this.first <= other.first && this.last >= other.last
