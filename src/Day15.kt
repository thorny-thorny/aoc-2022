import kotlin.math.abs

data class Beacon(val x: Int, val y: Int)
data class Sensor(val x: Int, val y: Int, val beacon: Beacon) {
    fun coverRangeAtY(rangeY: Int): IntRange {
        val distance = abs(x - beacon.x) + abs(y - beacon.y)
        val yCorrection = abs(y - rangeY)
        val left = x - distance + yCorrection
        val right = x + distance - yCorrection

        return left..right
    }
}

fun main() {
    fun parseLine(line: String): Sensor {
        val match = Regex("""Sensor at x=(-?\d+), y=(-?\d+): closest beacon is at x=(-?\d+), y=(-?\d+)""").matchEntire(line) ?: throw Exception("Unknown format")
        val (sensorX, sensorY, beaconX, beaconY) = match.destructured

        val beacon = Beacon(beaconX.toInt(), beaconY.toInt())
        return Sensor(sensorX.toInt(), sensorY.toInt(), beacon)
    }

    fun part1(input: List<String>, y: Int): Int {
        val sensors = input
            .map {
                val sensor = parseLine(it)
                val range = sensor.coverRangeAtY(y)
                sensor to range
            }
            .filter {
                !it.second.isEmpty()
            }

        val left = sensors.minOf { it.second.first }
        val right = sensors.maxOf { it.second.last }

        return (left..right).count { x ->
            val inRange = sensors.any { x in it.second }
            when {
                inRange -> {
                    val isSensor = sensors.any { it.first.x == x && it.first.y == y }
                    val isBeacon = sensors.any { it.first.beacon.x == x && it.first.beacon.y == y }
                    !isSensor && !isBeacon
                }
                else -> false
            }
        }
    }

    fun part2(input: List<String>, searchRange: IntRange): Long {
        val sensors = input.map(::parseLine)

        return searchRange.firstNotNullOf { y ->
            var x = searchRange.first
            while (x <= searchRange.last) {
                val range = sensors.firstNotNullOfOrNull {
                    val range = it.coverRangeAtY(y)
                    when {
                        range.isEmpty() || x !in range -> null
                        else -> range
                    }
                }

                when (range) {
                    null -> return@firstNotNullOf x.toLong() * 4000000L + y.toLong()
                    else -> x = range.last + 1
                }
            }

            null
        }
    }

    val testInput = readInput("Day15_test")
    check(part1(testInput, 10) == 26)
    check(part2(testInput, 0..20) == 56000011L)

    val input = readInput("Day15")
    println(part1(input, 2000000))
    println(part2(input, 0..4000000))
}
