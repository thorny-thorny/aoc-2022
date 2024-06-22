data class PlanePosition(val x: Int, val y: Int) {
    operator fun plus(direction: PlaneDirection): PlanePosition {
        return PlanePosition(x + direction.dX, y + direction.dY)
    }
}

enum class PlaneDirection(val dX: Int, val dY: Int) {
    Up(0, -1),
    Right(1, 0),
    Down(0, 1),
    Left(-1, 0),
}

fun String.indexOfOrNull(char: Char): Int? {
    val index = indexOf(char)
    return when {
        index < 0 -> null
        else -> index
    }
}

fun main() {
    data class Blizzard(val origin: PlanePosition, val direction: PlaneDirection)

    class Field(
        val width: Int,
        val height: Int,
        val start: PlanePosition,
        val end: PlanePosition,
        blizzards: List<Blizzard>,
    ) {
        val verticalBlizzards = Array(width) { x ->
            blizzards
                .filter { (it.direction == PlaneDirection.Up || it.direction == PlaneDirection.Down) && it.origin.x == x }
                .toTypedArray()
        }
        val horizontalBlizzards = Array(height) { y ->
            blizzards
                .filter { (it.direction == PlaneDirection.Left || it.direction == PlaneDirection.Right) && it.origin.y == y }
                .toTypedArray()
        }

        val blizzardsWidth = width - 2
        val blizzardsHeight = height - 2
        val blizzardsCycle = blizzardsWidth * blizzardsHeight

        fun hasBlizzardAt(position: PlanePosition, blizzardStep: Int): Boolean {
            val horizontalStep = blizzardStep % blizzardsWidth
            val hasHorizontalBlizzard = horizontalBlizzards[position.y].any { blizzard ->
                position.x == 1 + (blizzard.origin.x - 1 + blizzardsWidth + blizzard.direction.dX * horizontalStep) % blizzardsWidth
            }

            if (hasHorizontalBlizzard) {
                return true
            }

            val verticalStep = blizzardStep % blizzardsHeight
            val hasVerticalBlizzard = verticalBlizzards[position.x].any { blizzard ->
                position.y == 1 + (blizzard.origin.y - 1 + blizzardsHeight + blizzard.direction.dY * verticalStep) % blizzardsHeight
            }

            return hasVerticalBlizzard
        }

        fun stepsToMove(from: PlanePosition, direction: PlaneDirection, step: Int): Int? {
            val to = from + direction
            if (to.x <= 0 || to.x >= width - 1 || to.y <= 0 || to.y >= height - 1) {
                if (to != start && to != end) {
                    return null
                }
            }

            val steps = (1..width * height).firstNotNullOf {
                val newStep = step + it
                val hasBlizzardOnFrom = hasBlizzardAt(from, newStep)
                val hasBlizzardOnTo = hasBlizzardAt(to, newStep)

                when {
                    !hasBlizzardOnTo -> it
                    hasBlizzardOnFrom -> -1
                    else -> null

                }
            }

            return when {
                steps < 0 -> null
                else -> steps
            }
        }

        fun findShortestPathSteps(): Int {
            val steps = Array(width) {
                Array(height) { HashMap<Int, Pair<Int, PlanePosition?>>() }
            }
            val updates = Array(width) {
                IntArray(height) { 0 }
            }

            var updateStep = 1
            (0 until blizzardsCycle).forEach { steps[start.x][start.y][it] = it to null }
            updates[start.x][start.y] = updateStep

            do {
                var updated = false

                println(updateStep)

                (0 until width).forEach { x ->
                    (0 until height).forEach { y ->
                        if (updates[x][y] == updateStep) {
                            val from = PlanePosition(x, y)
                            PlaneDirection.values().forEach { direction ->
                                val to = from + direction
                                if (to.x <= 0 || to.x >= width - 1 || to.y <= 0 || to.y >= height - 1) {
                                    if (to != start && to != end) {
                                        return@forEach
                                    }
                                }

                                val fromStepsMap = steps[from.x][from.y]
                                val toStepsMap = steps[to.x][to.y]

                                do {
                                    var localUpdated = false
                                    (0 until blizzardsCycle).forEach { blizzardStep ->
                                        val prevBlizzardStep = (blizzardStep + blizzardsCycle - 1) % blizzardsCycle
                                        val prevFromSteps = fromStepsMap[prevBlizzardStep]
                                        val prevToSteps = toStepsMap[prevBlizzardStep]

                                        if ((prevFromSteps != null || prevToSteps != null) && !hasBlizzardAt(to, blizzardStep)) {
                                            val newSteps = 1 + minOf(prevFromSteps?.first ?: Int.MAX_VALUE, prevToSteps?.first ?: Int.MAX_VALUE)
                                            val steps = toStepsMap[blizzardStep]
                                            if (steps == null || steps.first > newSteps) {
                                                toStepsMap[blizzardStep] = (newSteps to (if ((prevFromSteps?.first ?: Int.MAX_VALUE) < (prevToSteps?.first ?: Int.MAX_VALUE)) from else to))
                                                localUpdated = true
                                                updated = true
                                                updates[to.x][to.y] = updateStep + 1
                                            }
                                        }
                                    }
                                } while (localUpdated)
                            }
                        }
                    }
                }

                updateStep += 1
            } while (updated)

            var m = steps[end.x][end.y].minBy { it.value.first }
            var bs = m.key
            var value = m.value
            while (true) {
                println("${value.first} from ${value.second}")
                if (value.second != null) {
                    bs = (bs + blizzardsCycle - 1) % blizzardsCycle
                    value = steps[value.second!!.x][value.second!!.y][bs]!!
                } else {
                    break
                }
            }

            return 0
        }
    }

    fun parseField(input: List<String>): Field {
        val startX = input.first().indexOfOrNull('.') ?: throw Exception("Can't enter D:")
        val endX = input.last().indexOfOrNull('.') ?: throw Exception("Can't leave D:")

        val blizzards = mutableListOf<Blizzard>()
        (1 until input.lastIndex).forEach { rowIndex ->
            val row = input[rowIndex]
            row.forEachIndexed { columnIndex, char ->
                val direction = when (char) {
                    '^' -> PlaneDirection.Up
                    '>' -> PlaneDirection.Right
                    'V' -> PlaneDirection.Down
                    '<' -> PlaneDirection.Left
                    else -> null
                }
                direction?.let {
                    blizzards.add(Blizzard(PlanePosition(columnIndex, rowIndex), it))
                }
            }
        }

        return Field(
            input.first().length,
            input.size,
            PlanePosition(startX, 0),
            PlanePosition(endX, input.lastIndex),
            blizzards.toList(),
        )
    }

    fun part1(input: List<String>): Int {
        val field = parseField(input)
        println("${field.width} ${field.height}")
        return field.findShortestPathSteps()
    }

    fun part2(input: List<String>): Int {
        return 0
    }

    // val testInput = readInput("Day24_test")
    // part1(testInput)

    val input = readInput("Day24")
    println(part1(input))
    println(part2(input))
}
