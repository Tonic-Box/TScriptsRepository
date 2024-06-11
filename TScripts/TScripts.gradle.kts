import org.gradle.kotlin.dsl.dependencies

//Major Release - Feature Addition - Bug Fix
version = "2.15.15"

plugins {
    id("java")
}

project.extra["PluginName"] = "TScripts"
project.extra["PluginDescription"] = "Stuff and Things"

dependencies {
    val kotlinVersion = "1.2.71"
    annotationProcessor(group = "org.projectlombok", name = "lombok", version = "1.18.20")
    annotationProcessor(group = "org.pf4j", name = "pf4j", version = "3.6.0")
    compileOnly(group = "com.google.code.gson", name = "gson", version = "2.8.5")
    compileOnly(group = "com.squareup.okhttp3", name = "okhttp", version = "4.9.1")
    compileOnly(group = "net.java.dev.jna", name = "jna", version = "5.9.0")
    compileOnly(group = "net.java.dev.jna", name = "jna-platform", version = "5.9.0")
    implementation("io.netty:netty-all:5.0.0.Alpha2")
    implementation("com.github.vlsi.mxgraph:jgraphx:4.2.2")
    implementation("ch.obermuhlner:java-scriptengine:1.0.1")
    implementation("org.antlr:antlr4:4.13.1")
    implementation("it.unimi.dsi:fastutil:8.5.11")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-script-util:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-script-runtime:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:$kotlinVersion")

    compileOnly(group = "com.fifesoft", name = "rsyntaxtextarea", version = "3.1.2")
    compileOnly(group = "com.fifesoft", name = "autocomplete", version = "3.1.1")
}



tasks {
    jar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        from(configurations.runtimeClasspath.get()
                .map { if (it.isDirectory) it else zipTree(it) })

        manifest {
            attributes(mapOf(
                    "Plugin-Version" to project.version,
                    "Plugin-Id" to nameToId(project.extra["PluginName"] as String),
                    "Plugin-Provider" to project.extra["PluginProvider"],
                    "Plugin-Description" to project.extra["PluginDescription"],
                    "Plugin-License" to project.extra["PluginLicense"]
            ))
        }
    }
    register("CompileTScriptsGrammar", Exec::class) {
        setWorkingDir("src\\main\\antlr\\")
        commandLine("cmd", "/c", "java -jar antlr.jar TScript.g4"
                + " -o ..\\java\\net\\runelite\\client\\plugins\\tscripts\\adapter\\lexer"
                + " -visitor -no-listener"
        )
        doLast {
            println("Executed!")
        }
    }
}