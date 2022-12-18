import kotlin.math.abs

data class Cube(val x: Int, val y: Int, val z: Int) {
    infix fun touches(other: Cube): Boolean {
        val touchesX = abs(x - other.x) == 1 && y == other.y && z == other.z
        val touchesY = abs(y - other.y) == 1 && x == other.x && z == other.z
        val touchesZ = abs(z - other.z) == 1 && x == other.x && y == other.y

        return touchesX || touchesY || touchesZ
    }

    fun min(other: Cube): Cube {
        val minX = minOf(x, other.x)
        val minY = minOf(y, other.y)
        val minZ = minOf(z, other.z)

        return Cube(minX, minY, minZ)
    }

    fun max(other: Cube): Cube {
        val maxX = maxOf(x, other.x)
        val maxY = maxOf(y, other.y)
        val maxZ = maxOf(z, other.z)

        return Cube(maxX, maxY, maxZ)
    }
}

enum class SpaceType {
    Unknown,
    Water,
    Lava,
}

fun main() {
    fun parseCubes(input: List<String>): List<Cube> {
        return input
            .map {
                val (x, y, z) = it.split(',').map(String::toInt)
                Cube(x, y, z)
            }
    }

    fun getCubesSurface(cubes: List<Cube>): Int {
        val countedCubes = mutableListOf<Cube>()

        return cubes.sumOf { cube ->
            val cubeCoveredSides = countedCubes.count { cube touches it }
            countedCubes.add(cube)

            6 - 2 * cubeCoveredSides
        }
    }


    fun part1(input: List<String>): Int {
        return getCubesSurface(parseCubes(input))
    }

    fun part2(input: List<String>): Int {
        val cubes = parseCubes(input)

        val totalSurface = getCubesSurface(cubes)

        var minCube = Cube(Int.MAX_VALUE, Int.MAX_VALUE, Int.MAX_VALUE)
        var maxCube = Cube(Int.MIN_VALUE, Int.MIN_VALUE, Int.MIN_VALUE)
        cubes.forEach {
            minCube = minCube.min(it)
            maxCube = maxCube.max(it)
        }

        val width = maxCube.x - minCube.x + 1
        val height = maxCube.y - minCube.y + 1
        val depth = maxCube.z - minCube.z + 1

        val space = List(width) { xIndex ->
            val x = minCube.x + xIndex
            List(height) { yIndex ->
                val y = minCube.y + yIndex
                MutableList(depth) { zIndex ->
                    val z = minCube.z + zIndex
                    val isBoundaryCube = x == minCube.x || x == maxCube.x || y == minCube.y || y == maxCube.y || z == minCube.z || z == maxCube.z
                    when {
                        isBoundaryCube -> when {
                            cubes.find { it.x == x && it.y == y && it.z == z } != null -> SpaceType.Lava
                            else -> SpaceType.Water
                        }
                        else -> SpaceType.Unknown
                    }
                }
            }
        }

        do {
            var updated = 0

            (minCube.x + 1 until maxCube.x).forEach { x ->
                val xIndex = x - minCube.x
                (minCube.y + 1 until maxCube.y).forEach { y ->
                    val yIndex = y - minCube.y
                    (minCube.z + 1 until maxCube.z).forEach { z ->
                        val zIndex = z - minCube.z
                        if (space[xIndex][yIndex][zIndex] == SpaceType.Unknown) {
                            if (cubes.find { it.x == x && it.y == y && it.z == z } != null) {
                                space[xIndex][yIndex][zIndex] = SpaceType.Lava
                                updated += 1
                            } else {
                                val touchesWater = (1..6).any { d ->
                                    val dx = if (d in 1..2) (d - 1) * 2 - 1 else 0
                                    val dy = if (d in 3..4) (d - 3) * 2 - 1 else 0
                                    val dz = if (d in 5..6) (d - 5) * 2 - 1 else 0
                                    space[xIndex + dx][yIndex + dy][zIndex + dz] == SpaceType.Water
                                }

                                if (touchesWater) {
                                    space[xIndex][yIndex][zIndex] = SpaceType.Water
                                    updated += 1
                                }
                            }
                        }
                    }
                }
            }
        } while (updated > 0)

        val holes = mutableListOf<Cube>()
        (minCube.x + 1 until maxCube.x).forEach { x ->
            val xIndex = x - minCube.x
            (minCube.y + 1 until maxCube.y).forEach { y ->
                val yIndex = y - minCube.y
                (minCube.z + 1 until maxCube.z).forEach { z ->
                    val zIndex = z - minCube.z
                    if (space[xIndex][yIndex][zIndex] == SpaceType.Unknown) {
                        holes.add(Cube(x, y, z))
                    }
                }
            }
        }

        val holesSurface = getCubesSurface(holes)

        return totalSurface - holesSurface
    }

    val testInput = readInput("Day18_test")
    check(part1(testInput) == 64)
    check(part2(testInput) == 58)

    val input = readInput("Day18")
    println(part1(input))
    println(part2(input))
}
