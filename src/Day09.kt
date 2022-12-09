import kotlin.math.absoluteValue
import kotlin.math.max

data class Point(var x: Int, var y: Int) {
    companion object {
        fun zero() = Point(0, 0)
    }

    operator fun plusAssign(vector: Vector) {
        x += vector.dx
        y += vector.dy
    }

    operator fun minus(other: Point): Vector {
        return Vector(x - other.x, y - other.y)
    }
}

data class Vector(val dx: Int, val dy: Int) {
    fun abs(): Vector {
        return Vector(dx.absoluteValue, dy.absoluteValue)
    }

    operator fun div(other: Vector): Vector {
        return Vector(dx / max(other.dx, 1), dy / max(other.dy, 1))
    }
}

class Rope(knotsAmount: Int = 2) {
    private val knots = List(knotsAmount) { Point.zero() }
    private val head = knots[0]
    val tailTrack = mutableSetOf(Point.zero())

    fun moveHead(vector: Vector) {
        val abs = vector.abs()
        val steps = max(abs.dx, abs.dy)
        val step = vector / abs

        repeat(steps) {
            head += step
            (1..knots.lastIndex).forEach {
                val segmentHead = knots[it - 1]
                val segmentTail = knots[it]
                val segment = segmentHead - segmentTail
                val segmentAbs = segment.abs()
                val segmentStep = segment / segmentAbs

                val moveHorizontally = segmentAbs.dx > 1
                val moveVertically = segmentAbs.dy > 1

                segmentTail.x = when {
                    moveVertically && !moveHorizontally -> segmentHead.x
                    moveHorizontally -> segmentHead.x - segmentStep.dx
                    else -> segmentTail.x
                }

                segmentTail.y = when {
                    moveHorizontally && !moveVertically -> segmentHead.y
                    moveVertically -> segmentHead.y - segmentStep.dy
                    else -> segmentTail.y
                }
            }

            tailTrack.add(knots.last().copy())
        }
    }
}

fun main() {
    fun parseInstruction(line: String): Vector {
        val (code, stepsString) = line.split(' ')
        val steps = stepsString.toInt()
        return when (code.first()) {
            'U' -> Vector(0, -steps)
            'D' -> Vector(0, steps)
            'L' -> Vector(-steps, 0)
            'R' -> Vector(steps, 0)
            else -> throw Exception("Unknown instruction")
        }
    }

    fun part1(input: List<String>): Int {
        val rope = Rope()
        input.forEach { rope.moveHead(parseInstruction(it)) }

        return rope.tailTrack.size
    }

    fun part2(input: List<String>): Int {
        val rope = Rope(10)
        input.forEach { rope.moveHead(parseInstruction(it)) }

        return rope.tailTrack.size
    }

    val testInput = readInput("Day09_test")
    check(part1(testInput) == 13)
    check(part2(testInput) == 1)

    val input = readInput("Day09")
    println(part1(input))
    println(part2(input))
}
