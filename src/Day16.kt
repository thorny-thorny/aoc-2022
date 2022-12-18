import kotlin.math.pow

data class Valve(val id: String, val rate: Int, val connectedTo: List<String>) {
    override fun equals(other: Any?): Boolean {
        return when (other) {
            is Valve -> id == other.id
            else -> false
        }
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return id
    }
}

fun main() {
    fun parseValve(line: String): Valve {
        val match = Regex("""Valve (\S+?) has flow rate=(\d+); tunnels? leads? to valves? (.+)""").matchEntire(line) ?: throw Exception("Unknown format")
        val (id, rate, connectedTo) = match.destructured
        return Valve(id, rate.toInt(), connectedTo.split(", "))
    }

    fun makeConnectionsMap(valves: List<Valve>): Map<Pair<Valve, Valve>, Int> {
        val size = valves.size
        val table = List(size) { from ->
            MutableList(size) { to ->
                when (from) {
                    to -> 0
                    else -> Int.MAX_VALUE
                }
            }
        }

        valves.forEachIndexed { index, valve ->
            valve.connectedTo.forEach { connectedId ->
                table[index][valves.indexOfFirst { it.id == connectedId }] = 1
            }
        }

        do {
            var updated = 0
            (0 until size).forEach { from ->
                (0 until size).forEach { to ->
                    val jumpsTo = table[from][to]
                    if (jumpsTo < Int.MAX_VALUE) {
                        (0 until size).forEach { bridge ->
                            val bridgeJumps = table[to][bridge]
                            if (bridgeJumps < Int.MAX_VALUE) {
                                val newJumps = jumpsTo + bridgeJumps
                                if (newJumps < table[from][bridge]) {
                                    table[from][bridge] = newJumps
                                    updated += 1
                                }
                            }
                        }
                    }
                }
            }
        } while (updated > 0)

        val map = hashMapOf<Pair<Valve, Valve>, Int>()
        (0 until size).forEach { from ->
            (0 until size).forEach { to ->
                val jumps = table[from][to]
                if (jumps < Int.MAX_VALUE) {
                    val fromValve = valves[from]
                    val toValve = valves[to]
                    map[fromValve to toValve] = jumps
                    map[toValve to fromValve] = jumps
                }
            }
        }

        return map.toMap()
    }

    fun getMaxPressure(targetValves: List<Valve>, tunnels: Map<Pair<Valve, Valve>, Int>, startingValve: Valve, track: List<Valve>, minutesLeft: Int): Int {
        if (minutesLeft == 0) {
            return 0
        }

        val current = track.lastOrNull() ?: startingValve
        val nextFunctioningValves = targetValves.filter {
            (it !in track) && tunnels[current to it] != null
        }

        return nextFunctioningValves.maxOfOrNull {
            when (val steps = tunnels[current to it]) {
                null -> 0
                else -> it.rate * maxOf(minutesLeft - steps - 1, 0) + getMaxPressure(targetValves, tunnels, startingValve, track + it, maxOf(minutesLeft - steps - 1, 0))
            }
        } ?: 0
    }

    fun part1(input: List<String>): Int {
        val valves = input.map(::parseValve)
        val allTunnels = makeConnectionsMap(valves)

        val startingValve = valves.find { it.id == "AA" } ?: throw Exception("I got lost D:")
        val functioningValves = valves.filter { it.rate > 0 }
        val involvedValves = functioningValves + startingValve
        val tunnels = allTunnels.filter {
            (it.key.first in involvedValves) && (it.key.second in involvedValves)
        }

        return getMaxPressure(functioningValves, tunnels, startingValve, listOf(), 30)
    }

    fun part2(input: List<String>): Int {
        val valves = input.map(::parseValve)
        val allTunnels = makeConnectionsMap(valves)

        val startingValve = valves.find { it.id == "AA" } ?: throw Exception("I got lost D:")
        val functioningValves = valves.filter { it.rate > 0 }
        val involvedValves = functioningValves + startingValve
        val tunnels = allTunnels.filter {
            (it.key.first in involvedValves) && (it.key.second in involvedValves)
        }

        return (0 until (2.0).pow(functioningValves.size).toInt()).maxOf {
            val meValves = mutableListOf<Valve>()
            val elValves = mutableListOf<Valve>()
            functioningValves.forEachIndexed { index, valve ->
                if ((1 shl index) and it > 0) {
                    meValves.add(valve)
                } else {
                    elValves.add(valve)
                }
            }

            val mePressure = getMaxPressure(meValves, tunnels, startingValve, listOf(), 26)
            val elPressure = getMaxPressure(elValves, tunnels, startingValve, listOf(), 26)

            mePressure + elPressure
        }
    }

    val testInput = readInput("Day16_test")
    check(part1(testInput) == 1651)
    check(part2(testInput) == 1707)

    val input = readInput("Day16")
    println(part1(input))
    println(part2(input))
}
