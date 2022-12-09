fun main() {
    fun part1(input: List<String>): Int {
        var visibleTreesInside = (1 until input.lastIndex).sumOf { rowIndex ->
            val row = input[rowIndex]
            (1 until row.lastIndex).count { columnIndex ->
                val tree = row[columnIndex]
                when {
                    (0 until columnIndex).all { tree > row[it] } -> true
                    ((columnIndex + 1)..row.lastIndex).all { tree > row[it] } -> true
                    (0 until rowIndex).all { tree > input[it][columnIndex] } -> true
                    ((rowIndex + 1)..input.lastIndex).all { tree > input[it][columnIndex] } -> true
                    else -> false
                }
            }
        }

        return visibleTreesInside + (input.size + input.first().length - 2) * 2
    }

    fun part2(input: List<String>): Int {
        val insideWidth = input.size - 2
        val insideHeight = input.first().length - 2
        val totalTreesInside = insideWidth * insideHeight
        return (0 until totalTreesInside).maxOf { index ->
            val row = index / insideWidth + 1
            val column = index % insideWidth + 1
            val tree = input[row][column]

            val leftTrees = (1..column).firstNotNullOfOrNull {
                if (input[row][column - it] >= tree) it else null
            } ?: column
            val rightTrees = (1 until (input.first().length - column)).firstNotNullOfOrNull {
                if (input[row][column + it] >= tree) it else null
            } ?: (input.first().length - column - 1)
            val topTrees = (1..row).firstNotNullOfOrNull {
                if (input[row - it][column] >= tree) it else null
            } ?: row
            val bottomTrees = (1 until (input.size - row)).firstNotNullOfOrNull {
                if (input[row + it][column] >= tree) it else null
            } ?: (input.size - row - 1)

//            println("$row:$column $leftTrees, $rightTrees, $topTrees, $bottomTrees")
//            println(leftTrees * rightTrees * topTrees * bottomTrees)

            leftTrees * rightTrees * topTrees * bottomTrees
        }
    }

    val testInput = readInput("Day08_test")
    check(part1(testInput) == 21)
    check(part2(testInput) == 8)

    val input = readInput("Day08")
    println(part1(input))
    println(part2(input))
}
