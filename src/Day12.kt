enum class Direction(val dColumn: Int, val dRow: Int) {
    Up(0, -1),
    Down(0, 1),
    Left(-1, 0),
    Right(1, 0),
}

fun main() {
    fun findMinSteps(input: List<String>, stepsDelta: (Int, Int) -> Int): Int {
        var start = 0 to 0
        var end = 0 to 0

        var heightMap = input.mapIndexed { rowIndex, row ->
            row.mapIndexed { columnIndex, mapChar ->
                when (mapChar) {
                    'S' -> {
                        start = rowIndex to columnIndex
                        'a'
                    }
                    'E' -> {
                        end = rowIndex to columnIndex
                        'z'
                    }
                    else -> mapChar
                } - 'a'
            }
        }

        val rows = heightMap.size
        val columns = heightMap.first().size

        var stepsMap = List(rows) { MutableList(columns) { Int.MAX_VALUE } }
        val updated = ArrayDeque<Pair<Int, Int>>(rows * columns)

        stepsMap[start.first][start.second] = 0
        updated.addLast(start)

        while (updated.size > 0) {
            val position = updated.removeLast()
            val positionHeight = heightMap[position.first][position.second]
            Direction.values().forEach { direction ->
                val neighbour = Pair(position.first + direction.dRow, position.second + direction.dColumn)
                if (neighbour.first in 0 until rows && neighbour.second in 0 until columns) {
                    val nextSteps = stepsMap[position.first][position.second] + stepsDelta(positionHeight, heightMap[neighbour.first][neighbour.second])
                    val isReachable = heightMap[neighbour.first][neighbour.second] - positionHeight <= 1
                    val isOptimizible = stepsMap[neighbour.first][neighbour.second] > nextSteps
                    if (isReachable && isOptimizible) {
                        stepsMap[neighbour.first][neighbour.second] = nextSteps
                        updated.addLast(neighbour)
                    }
                }
            }
        }

        return stepsMap[end.first][end.second]
    }

    fun part1(input: List<String>): Int {
        return findMinSteps(input) { _, _ -> 1 }
    }

    fun part2(input: List<String>): Int {
        return findMinSteps(input) { a, b -> when (a to b) {
            (0 to 0) -> 0
            else -> 1
        }}
    }

    val testInput = readInput("Day12_test")
    check(part1(testInput) == 31)
    check(part2(testInput) == 29)

    val input = readInput("Day12")
    println(part1(input))
    println(part2(input))
}
