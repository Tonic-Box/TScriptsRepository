import org.jetbrains.kotlin.resolve.calls.model.ResolvedCallArgument.DefaultArgument.arguments

buildscript {
    repositories {
        gradlePluginPortal()
    }
}

plugins {
    checkstyle
    java
    kotlin("jvm") version "1.3.71"
    id("com.github.andygoossens.gradle-modernizer-plugin") version "1.6.1"
}

apply<BootstrapPlugin>()
apply<VersionPlugin>()

allprojects {
    group = "com.tonic"
    apply<MavenPublishPlugin>()
}

allprojects {
    apply<MavenPublishPlugin>()

    repositories {
        mavenLocal()
        mavenCentral()
    }
}

subprojects {
    group = "com.tonic.externals"

    project.extra["PluginProvider"] = "TonicBox"
    project.extra["ProjectUrl"] = "https://discord.gg/m6htemjY5j"
    project.extra["PluginLicense"] = "3-Clause BSD License"

    repositories {
        mavenCentral {
            content {
                excludeGroupByRegex("com\\.openosrs.*")
            }
        }

        exclusiveContent {
            forRepository {
                mavenLocal()
            }
            filter {
                includeGroupByRegex("com\\.openosrs.*")
            }
        }
    }

    apply<JavaPlugin>()
    apply(plugin = "kotlin")
    apply(plugin = "com.github.andygoossens.gradle-modernizer-plugin")

    dependencies {
        compileOnly(group = "net.unethicalite", name = "http-api", version = "1.0.20-EXPERIMENTAL")
        compileOnly(group = "net.unethicalite", name = "runelite-api", version = "1.0.20-EXPERIMENTAL")
        compileOnly(group = "net.unethicalite", name = "runelite-client", version = "1.0.20-EXPERIMENTAL")

        compileOnly(group = "org.apache.commons", name = "commons-text", version = "1.9")
        compileOnly(group = "com.google.guava", name = "guava", version = "30.1.1-jre") {
            exclude(group = "com.google.code.findbugs", module = "jsr305")
            exclude(group = "com.google.errorprone", module = "error_prone_annotations")
            exclude(group = "com.google.j2objc", module = "j2objc-annotations")
            exclude(group = "org.codehaus.mojo", module = "animal-sniffer-annotations")
        }
        compileOnly(group = "com.google.inject", name = "guice", version = "5.0.1")
        compileOnly(group = "org.projectlombok", name = "lombok", version = "1.18.4")
        compileOnly(group = "org.pf4j", name = "pf4j", version = "3.6.0")
        compileOnly(group = "io.reactivex.rxjava3", name = "rxjava", version = "3.1.1")
    }

    configure<PublishingExtension> {
        repositories {
            maven {
                url = uri("$buildDir/repo")
            }
        }
        publications {
            register("mavenJava", MavenPublication::class) {
                from(components["java"])
            }
        }
    }

    tasks {
        java {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }

        withType<AbstractArchiveTask> {
            isPreserveFileTimestamps = false
            isReproducibleFileOrder = true
            dirMode = 493
            fileMode = 420
        }

        withType<JavaCompile> {
            options.encoding = "UTF-8"
        }

        withType<Checkstyle> {
            group = "verification"
        }

//        compileKotlin {
//            kotlinOptions {
//                jvmTarget = "11"
//                freeCompilerArgs = listOf("-Xjvm-default=enable")
//            }
//            sourceCompatibility = "11"
//        }

        register<Copy>("copyDeps") {
            into("./build/deps/")
            from(configurations["runtimeClasspath"])
        }
    }
}
