enum class Day21Operator {
    Plus,
    Minus,
    Times,
    Div,
}

sealed class Expression {
    data class Value(val value: Int): Expression()
    data class Operation(val op: Day21Operator, val left: String, val right: String): Expression()
}

fun main() {
    fun parseInput(input: List<String>): Map<String, Expression> {
        val nodes = hashMapOf<String, Expression>()
        input.forEach {
            val (id, value) = it.split(": ")

            val expressionMatch = Regex("""([a-z]+) (.) ([a-z]+)""").matchEntire(value)
            val node = when (val match = expressionMatch?.destructured) {
                null -> Expression.Value(value.toInt())
                else -> Expression.Operation(
                    when (match.component2()) {
                        "+" -> Day21Operator.Plus
                        "-" -> Day21Operator.Minus
                        "*" -> Day21Operator.Times
                        "/" -> Day21Operator.Div
                        else -> throw Exception("Unknown operator")
                    },
                    match.component1(),
                    match.component3(),
                )
            }

            nodes[id] = node
        }

        return nodes
    }

    fun calculateExpression(nodes: Map<String, Expression>, node: Expression): Long {
        return when (node) {
            is Expression.Value -> node.value.toLong()
            is Expression.Operation -> {
                val left = calculateExpression(nodes, nodes.getValue(node.left))
                val right = calculateExpression(nodes, nodes.getValue(node.right))

                when (node.op) {
                    Day21Operator.Plus -> left + right
                    Day21Operator.Minus -> left - right
                    Day21Operator.Times -> left * right
                    Day21Operator.Div -> left / right
                }
            }
        }
    }

    fun findValueNodePath(nodes: Map<String, Expression>, nodeId: String, targetId: String): List<String>? {
        return when (val node = nodes.getValue(nodeId)) {
            is Expression.Value -> when (nodeId) {
                targetId -> listOf(nodeId)
                else -> null
            }
            is Expression.Operation -> {
                val tail = when (val left = findValueNodePath(nodes, node.left, targetId)) {
                    null -> findValueNodePath(nodes, node.right, targetId)
                    else -> left
                }

                when (tail) {
                    null -> null
                    else -> tail + nodeId
                }
            }
        }
    }

    fun part1(input: List<String>): Long {
        val nodes = parseInput(input)
        return calculateExpression(nodes, nodes.getValue("root"))
    }

    fun part2(input: List<String>): Long {
        val nodes = parseInput(input)
        val path = findValueNodePath(nodes, "root", "humn") ?: throw Exception("Monkeys broken")

        val root = nodes["root"] as? Expression.Operation ?: throw Exception("No root monkey")
        val preRootId = path[path.lastIndex - 1]
        val rootValue = calculateExpression(
            nodes,
            when (preRootId) {
                root.left -> nodes.getValue(root.right)
                else -> nodes.getValue(root.left)
            },
        )

        fun reverseCalculateValue(index: Int): Long {
            val parent = nodes[path[index + 1]] as? Expression.Operation ?: throw Exception("Can't be value")
            val parentValue = when (index + 2) {
                path.lastIndex -> rootValue
                else -> reverseCalculateValue(index + 1)
            }

            val isLeft = parent.left == path[index]
            val siblingValue = calculateExpression(
                nodes,
                nodes.getValue(if (isLeft) parent.right else parent.left),
            )
            val v = when (parent.op) {
                Day21Operator.Plus -> parentValue - siblingValue
                Day21Operator.Minus -> when {
                    isLeft -> parentValue + siblingValue
                    else -> siblingValue - parentValue
                }
                Day21Operator.Times -> parentValue / siblingValue
                Day21Operator.Div -> when {
                    isLeft -> parentValue * siblingValue
                    else -> siblingValue / parentValue
                }
            }

            return v
        }

        return reverseCalculateValue(0)
    }

    val testInput = readInput("Day21_test")
    check(part1(testInput) == 152L)
    check(part2(testInput) == 301L)

    val input = readInput("Day21")
    println(part1(input))
    println(part2(input))
}
