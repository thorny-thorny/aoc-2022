sealed class Operand {
    object CurrentValue: Operand()
    class Number(val value: Int): Operand()
}

enum class Operator {
    Add,
    Multiply,
}

class Operation(val operand1: Operand, val operator: Operator, val operand2: Operand) {
    fun execute(currentValue: Long): Long {
        val (value1, value2) = listOf(operand1, operand2).map {
            when (it) {
                is Operand.CurrentValue -> currentValue
                is Operand.Number -> it.value.toLong()
            }
        }

        return when (operator) {
            Operator.Add -> value1 + value2
            Operator.Multiply -> value1 * value2
        }
    }
}

sealed class Test {
    class DivisibleBy(val op: Int): Test()

    fun run(value: Long) = when (this) {
        is DivisibleBy -> value % op == 0L
    }
}

class Monkey(
    startingItems: List<Long>,
    val operation: Operation,
    val test: Test,
    val throwTo: Map<Boolean, Int>,
) {
    fun interface OnThrowItemCallback {
        fun throwItem(item: Long, to: Int)
    }

    private val items = startingItems.toMutableList()
    var itemsInspected = 0L
        private set

    fun playTurn(relief: (Long) -> Long, onThrowItem: OnThrowItemCallback) {
        items.forEach {
            val item = relief(operation.execute(it))
            itemsInspected += 1
            onThrowItem.throwItem(item, throwTo.getValue(test.run(item)))
        }

        items.clear()
    }

    fun catch(item: Long) {
        items.add(item)
    }
}

fun main() {
    fun parseOperation(string: String): Operation {
        val (op1String, operationString, op2String) = string.split(' ')
        val (op1, op2) = listOf(op1String, op2String).map {
            when (it) {
                "old" -> Operand.CurrentValue
                else -> Operand.Number(it.toInt())
            }
        }
        val operator = when (operationString) {
            "+" -> Operator.Add
            "*" -> Operator.Multiply
            else -> throw Exception("Unknown operator")
        }

        return Operation(op1, operator, op2)
    }

    fun parseTest(string: String): Test {
        val match = Regex("""divisible by (\d+)""").matchEntire(string) ?: throw Exception("Unknown test")
        return Test.DivisibleBy(match.destructured.component1().toInt())
    }

    fun parseThrowTo(string: String): Pair<Boolean, Int> {
        val match = Regex("""If (true|false): throw to monkey (\d+)""").matchEntire(string) ?: throw Exception("Unknown throw to")
        val (condition, monkey) = match.destructured

        return condition.toBooleanStrict() to monkey.toInt()
    }

    fun parseMonkey(iterator: Iterator<String>): Monkey {
        Regex("""Monkey (\d+):""").matchEntire(iterator.next()) ?: throw Exception("Unknown header format")

        val startingItemsMatch = Regex("""Starting items: (.+)""").matchEntire(iterator.next().trim()) ?: throw Exception("Unknown starting items format")
        val startingItems = startingItemsMatch.destructured.component1().split(", ").map(String::toLong)

        val operationMatch = Regex("""Operation: new = (.+)""").matchEntire(iterator.next().trim()) ?: throw Exception("Unknown operation format")
        val operation = parseOperation(operationMatch.destructured.component1())

        val testMatch = Regex("""Test: (.+)""").matchEntire(iterator.next().trim()) ?: throw Exception("Unknown test format")
        val test = parseTest(testMatch.destructured.component1())

        val throw1 = iterator.next()
        val throw2 = iterator.next()
        val throwTo = listOf(throw1, throw2).map(String::trim).map(::parseThrowTo).associate { it }

        return Monkey(startingItems, operation, test, throwTo)
    }

    fun parseMonkeys(iterator: Iterator<String>): List<Monkey> {
        val monkeys = mutableListOf<Monkey>()
        while (iterator.hasNext()) {
            monkeys.add(parseMonkey(iterator))

            if (iterator.hasNext()) {
                if (iterator.next().isNotEmpty()) {
                    throw Exception("Unknown delimiter")
                }
            }
        }

        return monkeys.toList()
    }

    fun simulateMonkeys(monkeys: List<Monkey>, times: Int, relief: (Long) -> Long) {
        repeat(times) {
            monkeys.forEach {
                it.playTurn(relief) { item, to ->
                    if (item < 0) {
                        println(item)
                    }
                    monkeys[to].catch(item)
                }
            }
        }
    }

    fun part1(input: List<String>): Long {
        val monkeys = parseMonkeys(input.iterator())

        simulateMonkeys(monkeys, 20) { it / 3 }

        return monkeys
            .map(Monkey::itemsInspected)
            .sortedDescending()
            .take(2)
            .fold(1) { a, b -> a * b }
    }

    fun part2(input: List<String>): Long {
        val monkeys = parseMonkeys(input.iterator())

        val testProduct = monkeys.fold(1L) { it, monkey ->
            it * when (val test = monkey.test) {
                is Test.DivisibleBy -> test.op
                else -> throw Exception("Can't solve it for large numbers this way")
            }
        }

        simulateMonkeys(monkeys, 10000) { it % testProduct }

        return monkeys
            .map(Monkey::itemsInspected)
            .sortedDescending()
            .take(2)
            .fold(1) { a, b -> a * b }
    }

    val testInput = readInput("Day11_test")
    check(part1(testInput) == 10605L)
    check(part2(testInput) == 2713310158L)

    val input = readInput("Day11")
    println(part1(input))
    println(part2(input))
}
