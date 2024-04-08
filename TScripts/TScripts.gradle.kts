/*
 * Copyright (c) 2019 Owain van Brakel <https://github.com/Owain94>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

version = "0.1.1-alpha"

project.extra["PluginName"] = "TScripts"
project.extra["PluginDescription"] = "Stuff and Things"

dependencies {
    annotationProcessor(group = "org.projectlombok", name = "lombok", version = "1.18.20")
    annotationProcessor(group = "org.pf4j", name = "pf4j", version = "3.6.0")
    compileOnly(group = "com.google.code.gson", name = "gson", version = "2.8.5")
    compileOnly(group = "com.squareup.okhttp3", name = "okhttp", version = "4.9.1")
    compileOnly(group = "net.java.dev.jna", name = "jna", version = "5.9.0")
    compileOnly(group = "net.java.dev.jna", name = "jna-platform", version = "5.9.0")
    implementation("io.netty:netty-all:5.0.0.Alpha2")
    implementation("com.github.vlsi.mxgraph:jgraphx:4.2.2")
    implementation("ch.obermuhlner:java-scriptengine:1.0.1")
//    implementation(group = "net.runelite", name = "fernflower", version = "07082019")
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
}