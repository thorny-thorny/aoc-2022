import kotlin.math.absoluteValue

class CPU(instructions: Sequence<Instruction>) {
    private val instructionsIterator = instructions.iterator()
    private var runningInstruction: Instruction? = null
    private var instructionCyclesLeft = 0
    var regX = 1
        private set
    var cycles = 0
        private set

    fun cycle() {
        if (runningInstruction == null) {
            val nextInstruction = instructionsIterator.next()
            runningInstruction = nextInstruction
            instructionCyclesLeft = nextInstruction.cycles // idk why smart cast doesn't work for runningInstruction.cycles
        }

        instructionCyclesLeft -= 1
        if (instructionCyclesLeft == 0) {
            when (val instruction = runningInstruction) {
                is Instruction.NoOp -> {}
                is Instruction.AddX -> regX += instruction.value
                null -> {}
            }
            runningInstruction = null
        }

        cycles += 1
    }

    fun cycleUntil(predicate: CPU.() -> Boolean) {
        while (!predicate()) {
            cycle()
        }
    }
}

class CRT() {
    val width = 40
    val height = 6
    private val pixels = List(height) { MutableList (width) { false } }

    fun drawPixel(index: Int, spriteX: Int) {
        val x = index % width
        pixels[index / width][x] = (x - spriteX).absoluteValue <= 1
    }

    override fun toString(): String {
        return pixels.joinToString("\n") { line ->
            line.joinToString("") {
                when (it) {
                    true -> "#"
                    false -> "."
                }
            }
        }
    }
}

class Device(instructions: Sequence<Instruction>) {
    private val cpu = CPU(instructions)
    private val crt = CRT()

    fun runProgram() {
        val pixels = crt.width * crt.height
        while (cpu.cycles < pixels) {
            crt.drawPixel(cpu.cycles, cpu.regX)
            cpu.cycle()
        }
    }

    override fun toString(): String {
        return crt.toString()
    }
}

sealed class Instruction private constructor (val cycles: Int) {
    class NoOp: Instruction(1)
    class AddX(val value: Int): Instruction(2)
}

fun main() {
    fun parseInstruction(line: String): Instruction {
        val parts = line.split(' ')
        return when (parts.first()) {
            "noop" -> Instruction.NoOp()
            "addx" -> Instruction.AddX(parts[1].toInt())
            else -> throw Exception("Unknown instruction")
        }
    }

    fun part1(input: List<String>): Int {
        val cpu = CPU(input.asSequence().map(::parseInstruction))
        val signalCycles = listOf(20, 60, 100, 140, 180, 220)
        return signalCycles.sumOf { signalCycle ->
            cpu.cycleUntil { cycles == signalCycle - 1 }
            signalCycle * cpu.regX
        }
    }

    fun part2(input: List<String>): String {
        val device = Device(input.asSequence().map(::parseInstruction))
        device.runProgram()
        return device.toString()
    }

    val testInput = readInput("Day10_test")
    check(part1(testInput) == 13140)

    val input = readInput("Day10")
    println(part1(input))
    println(part2(input))
}
