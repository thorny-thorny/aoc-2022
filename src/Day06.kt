fun main() {
    fun findPayloadIndex(buffer: String, markerLength: Int): Int {
        val marker = buffer.substring(0 until markerLength).toMutableList()
        return 1 + ((markerLength - 1)..buffer.lastIndex).first {
            marker[it % markerLength] = buffer[it]
            marker.distinct().size == markerLength
        }
    }

    fun part1(input: List<String>): Int {
        return findPayloadIndex(input.first(), 4)
    }

    fun part2(input: List<String>): Int {
        return findPayloadIndex(input.first(), 14)
    }

    val testInput = readInput("Day06_test")
    check(part1(testInput) == 7)
    check(part2(testInput) == 19)

    val input = readInput("Day06")
    println(part1(input))
    println(part2(input))
}
