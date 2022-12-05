import java.io.ByteArrayOutputStream

plugins {
    kotlin("jvm") version "1.7.22"
}

repositories {
    mavenCentral()
}

tasks {
    sourceSets {
        main {
            java.srcDirs("src")
        }
    }

    wrapper {
        gradleVersion = "7.6"
    }
}

customAdventOfCodeTasks()

fun customAdventOfCodeTasks() {
    val testTask = tasks["test"]
    val solveTask = tasks.register("solve").get()
    fileTree("src") {
        include("day*/*.in")
    }.map {
        val day = it.parent.split("/").last().removePrefix("day")
        val inputName = it.name.removeSuffix(".in")
        day to inputName
    }.groupBy { it.first }.map { entry ->
        entry.key to entry.value.map { it.second }
    }.forEach { (day, inputs) ->
        val solveDayTask = tasks.register("solveDay$day").get()
        solveTask.dependsOn(solveDayTask)
        val testDayTask = tasks.register("testDay$day").get()
        testTask.dependsOn(testDayTask)
        inputs.forEach { inputName ->
            val inputFile = File("src/day$day/$inputName.in")
            val outputFile = layout.buildDirectory.file("answers/day$day/$inputName.out").get().asFile
            val expectedOutputFile = File("src/day$day/$inputName.out")
            val executeTask = tasks.register<JavaExec>("solveDay$day-$inputName") {
                description = "Calculates the solution for day $day for input '$inputName'"
                dependsOn("compileKotlin")
                this.inputs.file(inputFile)
                this.outputs.file(outputFile)
                // Don't cache response, always run.
                this.outputs.upToDateWhen { false }

                classpath(sourceSets["main"].runtimeClasspath)
                mainClass.set("aoc2022/day$day/Day" + day + "Kt")
                standardInput = inputFile.inputStream()
                standardOutput = ByteArrayOutputStream()

                doLast {
                    val result = (standardOutput as ByteArrayOutputStream).toString()
                    outputFile.parentFile.mkdirs()
                    if (!outputFile.exists()) {
                        outputFile.createNewFile()
                    }
                    val fout = outputFile.outputStream()
                    fout.write(result.toByteArray())
                    fout.close()
                    print(result)
                }
            }.get()
            solveDayTask.dependsOn(executeTask)
            if (expectedOutputFile.exists()) {
                val testDayInputTask = tasks.register<Exec>("testDay$day-$inputName") {
                    description = "Tests the solution for day $day using input/output for '$inputName'"
                    dependsOn(executeTask)
                    this.inputs.files(expectedOutputFile, outputFile)

                    commandLine = listOf("diff", expectedOutputFile.path, outputFile.path)
                }.get()
                testDayTask.dependsOn(testDayInputTask)
            }
        }
    }
}
