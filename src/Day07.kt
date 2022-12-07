sealed class FileSystemEntry private constructor (val name: String) {
    var parent: Directory? = null
        private set

    protected fun Directory.makeParentOf(entry: FileSystemEntry) {
        entry.parent = this
    }

    open fun printlnStructured(indent: Int = 0) {
        repeat(indent) { print("  ") }
        println(this)
    }
}

class File(name: String, val size: Int): FileSystemEntry(name) {
    override fun toString(): String {
        return "$name (file, size=$size)"
    }
}

class Directory(name: String): FileSystemEntry(name) {
    var content = listOf<FileSystemEntry>()
        private set

    var contentSize = 0
        private set

    private fun increaseSizeRecursively(extraSize: Int) {
        var dir: Directory? = this
        while (dir != null) {
            dir.contentSize += extraSize
            dir = dir.parent
        }
    }

    fun addEntry(entry: FileSystemEntry) {
        val extraSize = when (entry) {
            is File -> entry.size
            is Directory -> entry.contentSize
        }

        content += entry
        makeParentOf(entry)
        increaseSizeRecursively(extraSize)
    }

    override fun printlnStructured(indent: Int) {
        super.printlnStructured(indent)
        content.forEach { it.printlnStructured(indent + 1) }
    }

    override fun toString(): String {
        return "$name (dir, contentSize=$contentSize)"
    }

    private fun iterate(action: (FileSystemEntry) -> Unit) {
        action(this)
        content.forEach {
            when (it) {
                is Directory -> it.iterate(action)
                else -> action(it)
            }
        }
    }

    fun flat(): List<FileSystemEntry> {
        val list = mutableListOf<FileSystemEntry>()
        iterate { list.add(it) }

        return list.toList()
    }
}

sealed class ExecutedCommand {
    class ChangeDirectory(val to: String): ExecutedCommand()
    class ListContent(val output: List<String>): ExecutedCommand()
}

fun main() {
    fun Sequence<String>.parseCommands() = sequence {
        var command: String? = null
        val output = mutableListOf<String>()

        suspend fun SequenceScope<ExecutedCommand>.emitCommand() {
            when (command) {
                null -> {}
                "ls" -> yield(ExecutedCommand.ListContent(output.toList()))
                else -> {
                    val cdMatch = command?.let { Regex("""cd (.+)""").matchEntire(it) }
                    if (cdMatch != null) {
                        yield(ExecutedCommand.ChangeDirectory(cdMatch.destructured.component1()))
                    } else {
                        throw Exception("Unknown command")
                    }
                }
            }

            command = null
            output.clear()
        }

        forEach { line ->
            when (val match = Regex("""\$ (.+)""").matchEntire(line)) {
                null -> output.add(line)
                else -> {
                    emitCommand()
                    command = match.destructured.component1()
                }
            }
        }

        emitCommand()
    }

    fun buildFileSystem(commands: List<String>): Directory {
        val root = Directory("/")
        var cwd = root

        commands
            .asSequence()
            .parseCommands()
            .forEach { command ->
                when (command) {
                    is ExecutedCommand.ChangeDirectory -> cwd = when (val to = command.to) {
                        "/" -> root
                        ".." -> cwd.parent ?: throw Exception("Can't go back")
                        else -> cwd.content.firstNotNullOf {
                            when (it is Directory && it.name == to) {
                                true -> it
                                else -> null
                            }
                        }
                    }
                    is ExecutedCommand.ListContent -> {
                        command.output.forEach {
                            val (size, name) = it.split(" ")
                            when (size) {
                                "dir" -> cwd.addEntry(Directory(name))
                                else -> cwd.addEntry(File(name, size.toInt()))
                            }
                        }
                    }
                }
            }

        return root
    }

    fun part1(input: List<String>): Int {
        val root = buildFileSystem(input)
        return root
            .flat()
            .filterIsInstance<Directory>()
            .map(Directory::contentSize)
            .filter { it <= 100000 }
            .sum()
    }

    fun part2(input: List<String>): Int {
        val root = buildFileSystem(input)
        val sizeToFree = 30000000 - (70000000 - root.contentSize)

        return sizeToFree + root
            .flat()
            .filterIsInstance<Directory>()
            .map(Directory::contentSize)
            .map { it - sizeToFree }
            .filter { it >= 0 }
            .min()
    }

    val testInput = readInput("Day07_test")
    check(part1(testInput) == 95437)
    check(part2(testInput) == 24933642)

    val input = readInput("Day07")
    println(part1(input))
    println(part2(input))
}
