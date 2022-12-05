class Cargo(stacksAmount: Int) {
    private val stacks = MutableList(stacksAmount) { listOf<Char>() }

    fun pushItem(item: Char, index: Int) {
        stacks[index] = stacks[index] + item
    }

    fun runInstruction(instruction: CargoInstruction, oneItemAtTime: Boolean) {
        when (instruction) {
            is CargoInstruction.Move -> {
                val stack = stacks[instruction.from - 1]
                var chunk = stack.slice((stack.lastIndex - instruction.amount + 1)..stack.lastIndex)
                if (oneItemAtTime) {
                    chunk = chunk.reversed()
                }

                stacks[instruction.from - 1] = stack.dropLast(instruction.amount)
                stacks[instruction.to - 1] = stacks[instruction.to - 1] + chunk
            }
        }
    }

    fun topItems() = stacks
        .map { it.last() }
        .joinToString("")
}

sealed class CargoInstruction {
    class Move(val amount: Int, val from: Int, val to: Int): CargoInstruction()
}

fun main() {
    // Returns cargo and index of next line
    fun parseCargo(lines: List<String>): Pair<Cargo, Int> {
        val separatorIndex = lines.indexOf("")
        val cargo = Cargo((lines[separatorIndex - 1].length + 2) / 4)
        ((separatorIndex - 2) downTo 0).forEach { index ->
            lines[index]
                .windowed(3, 4)
                .map { it[1] }
                .forEachIndexed { itemIndex, item ->
                    if (!item.isWhitespace()) {
                        cargo.pushItem(item, itemIndex)
                    }
                }
        }

        return cargo to (separatorIndex + 1)
    }

    fun parseInstruction(line: String): CargoInstruction {
        val parts = line.split(' ', limit = 2)
        return when (parts.first()) {
            "move" -> {
                val match = Regex("""(\d+) from (\d+) to (\d+)""").matchEntire(parts.last())
                val (amount, from, to) = match?.destructured ?: throw Exception("Bad instruction arguments")
                CargoInstruction.Move(amount.toInt(), from.toInt(), to.toInt())
            }
            else -> throw Exception("Unknown instruction")
        }
    }

    fun part1(input: List<String>): String {
        val (cargo, instructionsIndex) = parseCargo(input)
        (instructionsIndex..input.lastIndex).forEach { index ->
            cargo.runInstruction(parseInstruction(input[index]), true)
        }

        return cargo.topItems()
    }

    fun part2(input: List<String>): String {
        val (cargo, instructionsIndex) = parseCargo(input)
        (instructionsIndex..input.lastIndex).forEach { index ->
            cargo.runInstruction(parseInstruction(input[index]), false)
        }

        return cargo.topItems()
    }

    val testInput = readInput("Day05_test")
    check(part1(testInput) == "CMZ")
    check(part2(testInput) == "MCD")

    val input = readInput("Day05")
    println(part1(input))
    println(part2(input))
}
