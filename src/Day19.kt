import kotlin.math.ceil

data class Resources(val ore: Int = 0, val clay: Int = 0, val obsidian: Int = 0, val geode: Int = 0) {
    operator fun minus(other: Resources): Resources {
        return Resources(maxOf(ore - other.ore, 0), maxOf(clay - other.clay, 0), maxOf(obsidian - other.obsidian, 0), maxOf(geode - other.geode, 0))
    }

    operator fun plus(other: Resources): Resources {
        return Resources(ore + other.ore, clay + other.clay, obsidian + other.obsidian, geode + other.geode)
    }

    operator fun times(times: Int): Resources {
        return Resources(ore * times, clay * times, obsidian * times, geode * times)
    }
}

data class Miner(val rate: Resources) {
    fun mine(time: Int) = rate * time
}

data class MinerBlueprint(val cost: Resources, val rate: Resources) {
    fun build() = Miner(rate)
}

class Factory(val blueprints: List<MinerBlueprint>, val initialMiners: List<Miner>) {
    val initialMinersRate = initialMiners.fold(Resources()) { acc, it -> acc + it.rate }

    data class State(val resource: Resources, val miners: List<Miner>)

    private fun timeToBuild(resources: Resources, minersRate: Resources, blueprint: MinerBlueprint): Int? {
        val resourcesNeeded = blueprint.cost - resources

        if (
            (resourcesNeeded.ore > 0 && minersRate.ore == 0) ||
            (resourcesNeeded.clay > 0 && minersRate.clay == 0) ||
            (resourcesNeeded.obsidian > 0 && minersRate.obsidian == 0) ||
            (resourcesNeeded.geode > 0 && minersRate.geode == 0)
        ) {
            return null
        }

        return maxOf(
            ceil(resourcesNeeded.ore.toFloat() / maxOf(1, minersRate.ore)).toInt(),
            ceil(resourcesNeeded.clay.toFloat() / maxOf(1, minersRate.clay)).toInt(),
            ceil(resourcesNeeded.obsidian.toFloat() / maxOf(1, minersRate.obsidian)).toInt(),
            ceil(resourcesNeeded.geode.toFloat() / maxOf(1, minersRate.geode)).toInt(),
        )
    }

    fun optimizedGeodeProduction(timeLeft: Int, state: State = State(Resources(), initialMiners), minersRate: Resources = initialMinersRate): State {
        if (timeLeft == 0) {
            return state
        }

        return blueprints.maxOfWith({ first, second -> first.resource.geode - second.resource.geode }) { blueprint ->
            val timeToBuild = timeToBuild(state.resource, minersRate, blueprint)
            if (timeToBuild == null || timeToBuild > timeLeft) {
                val mined = minersRate * timeLeft
                state.copy(resource = state.resource + mined)
            } else {
                val mined = minersRate * timeToBuild
                val miner = blueprint.build()
                val newState = state.copy(resource = state.resource + mined - blueprint.cost, miners = state.miners + miner)
                optimizedGeodeProduction(timeLeft - timeToBuild, newState, minersRate + miner.rate)
            }
        }
    }
}

fun main() {
    fun part1(input: List<String>): Int {
        val oreRobotBlueprint = MinerBlueprint(Resources(ore = 4), Resources(ore = 1))
        val clayRobotBlueprint = MinerBlueprint(Resources(ore = 2), Resources(clay = 1))
        val obsidianRobotBlueprint = MinerBlueprint(Resources(ore = 3, clay = 14), Resources(obsidian = 1))
        val geodeRobotBlueprint = MinerBlueprint(Resources(ore = 2, obsidian = 7), Resources(geode = 1))
        val blueprints = listOf(oreRobotBlueprint, clayRobotBlueprint, obsidianRobotBlueprint, geodeRobotBlueprint)

        val factory = Factory(blueprints, listOf(oreRobotBlueprint.build()))
        val state = factory.optimizedGeodeProduction(17)
        println(state)

        return 0
    }

    fun part2(input: List<String>): Int {
        return 0
    }
}
