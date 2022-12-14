sealed class Line(val topY: Int) {
    data class HorizontalInfinite(val y: Int): Line(y)
    data class Horizontal(val y: Int, val xMin: Int, val xMax: Int): Line(y)
    data class Vertical(val x: Int, val yMin: Int, val yMax: Int): Line(yMin)

    fun hasX(value: Int) = when (this) {
        is HorizontalInfinite -> true
        is Horizontal -> value in xMin..xMax
        is Vertical -> value == x
    }

    fun hasY(value: Int) = when (this) {
        is HorizontalInfinite -> value == y
        is Horizontal -> value == y
        is Vertical -> value in yMin..yMax
    }
}

class SandSimulator(lines: List<Line>) {
    private val spawn = 500 to 0
    private val lines = lines.sortedBy(Line::topY)
    private val piles = mutableMapOf<Pair<Line, Int>, Int>()
    var sandPiled = 0
        private set

    private fun getPileTop(line: Line, x: Int) = when (val pile = piles[line to x]) {
        null -> line.topY
        else -> pile
    }

    private fun addPileTop(line: Line, x: Int) {
        val pair = line to x
        piles[pair] = (piles[pair] ?: line.topY) - 1
    }

    fun dropSandUntilPiles() {
        while (dropSand()) {
        }
    }

    private fun dropSand(): Boolean {
        var (x, y) = spawn

        while (true) {
            val centerLine = lines.firstOrNull { it.hasX(x) && it.topY > y } ?: return false
            val centerPile = getPileTop(centerLine, x)

            if (centerPile == y) {
                return false
            }

            val leftLine =
                lines.firstOrNull { it.hasX(x - 1) && (it.hasY(centerPile) || it.topY >= centerPile) } ?: return false
            val leftPile = getPileTop(leftLine, x - 1)
            val rightLine =
                lines.firstOrNull { it.hasX(x + 1) && (it.hasY(centerPile) || it.topY >= centerPile) } ?: return false
            val rightPile = getPileTop(rightLine, x + 1)

            val canGoLeft = leftPile > centerPile
            val canGoRight = rightPile > centerPile

            if (!canGoLeft && !canGoRight) {
                addPileTop(centerLine, x)
                sandPiled += 1
                return true
            } else if (canGoLeft) {
                x -= 1
                y = leftPile - 1
            } else {
                x += 1
                y = rightPile - 1
            }
        }
    }
}

fun main() {
    fun parseLines(input: List<String>): List<Line> {
        val lines = mutableListOf<Line>()
        input.forEach {
            val parts = it
                .split(" -> ")
                .map { part ->
                    val (x, y) = part.split(',').map(String::toInt)
                    x to y
                }

            parts
                .zipWithNext()
                .forEach { pair ->
                    val (start, end) = pair
                    if (start.first == end.first) {
                        lines.add(Line.Vertical(start.first, minOf(start.second, end.second), maxOf(start.second, end.second)))
                    } else {
                        lines.add(Line.Horizontal(start.second, minOf(start.first, end.first), maxOf(start.first, end.first)))
                    }
                }
        }

        return lines.toList()
    }

    fun part1(input: List<String>): Int {
        val sandSimulator = SandSimulator(parseLines(input))
        sandSimulator.dropSandUntilPiles()

        return sandSimulator.sandPiled
    }

    fun part2(input: List<String>): Int {
        val lines = parseLines(input)
        val maxY = lines.maxOf {
            when (it) {
                is Line.HorizontalInfinite -> it.y
                is Line.Horizontal -> it.y
                is Line.Vertical -> it.yMax
            }
        }

        val sandSimulator = SandSimulator(lines + Line.HorizontalInfinite(maxY + 2))
        sandSimulator.dropSandUntilPiles()

        return sandSimulator.sandPiled
    }

    val testInput = readInput("Day14_test")
    check(part1(testInput) == 24)
    check(part2(testInput) == 93)

    val input = readInput("Day14")
    println(part1(input))
    println(part2(input))
}
