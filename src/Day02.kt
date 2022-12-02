fun main() {
    fun decodeLine(line: String) = (line.first() - 'A' + 1) to (line.last() - 'X' + 1)

    fun roundScore(opponent: Int, me: Int) = me + ((4 + me - opponent) % 3) * 3

    fun part1(input: List<String>): Int {
        return input.sumOf {
            val (opponent, me) = decodeLine(it)
            roundScore(opponent, me)
        }
    }

    fun part2(input: List<String>): Int {
        return input.sumOf {
            val (opponent, strategy) = decodeLine(it)
            val me = when (strategy) {
                1 -> (opponent + 1) % 3 + 1
                2 -> opponent
                3 -> opponent % 3 + 1
                else -> throw Exception("Unknown strategy")
            }

            roundScore(opponent, me)
        }
    }

    val testInput = readInput("Day02_test")
    check(part1(testInput) == 15)
    check(part2(testInput) == 12)

    val input = readInput("Day02")
    println(part1(input))
    println(part2(input))
}
