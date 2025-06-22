import com.diffplug.gradle.spotless.SpotlessPlugin
import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import com.vanniktech.maven.publish.SonatypeHost
import groovy.json.JsonSlurper
import xyz.jpenilla.runpaper.task.RunServer
import java.net.URI

plugins {
    java
    `java-library`
    signing

    alias(libs.plugins.shadow)
    alias(libs.plugins.spotless)
    alias(libs.plugins.grgit)
    alias(libs.plugins.publish)

    eclipse
    idea

    alias(libs.plugins.runPaper)
}

group = "com.intellectualsites.plotsquared"
version = "7.5.4-SNAPSHOT"

if (!File("$rootDir/.git").exists()) {
    logger.lifecycle("""
    **************************************************************************************
    You need to fork and clone this repository! Don't download a .zip file.
    If you need assistance, consult the GitHub docs: https://docs.github.com/get-started/quickstart/fork-a-repo
    **************************************************************************************
    """.trimIndent()
    ).also { kotlin.system.exitProcess(1) }
}

subprojects {
    group = rootProject.group
    version = rootProject.version

    repositories {
        mavenCentral()

        maven {
            name = "Jitpack"
            url = uri("https://jitpack.io")
            content {
                includeModule("com.github.MilkBowl", "VaultAPI")
            }
        }

        maven {
            name = "EngineHub"
            url = uri("https://maven.enginehub.org/repo/")
        }
    }

    apply {
        plugin<JavaPlugin>()
        plugin<JavaLibraryPlugin>()
        plugin<ShadowPlugin>()
        plugin<SpotlessPlugin>()
        plugin<SigningPlugin>()

        plugin<EclipsePlugin>()
        plugin<IdeaPlugin>()
    }

    dependencies {
        // Tests
        testImplementation("org.junit.jupiter:junit-jupiter:5.13.1")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.13.1")
    }

    plugins.withId("java") {
        the<JavaPluginExtension>().toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    tasks.compileJava.configure {
        options.release.set(17)
    }

    configurations.all {
        attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 21)
    }

    spotless {
        java {
            licenseHeaderFile(rootProject.file("HEADER.txt"))
            target("**/*.java")
            endWithNewline()
            trimTrailingWhitespace()
            removeUnusedImports()
        }
    }

    val javaComponent = components["java"] as AdhocComponentWithVariants
    javaComponent.withVariantsFromConfiguration(configurations["shadowRuntimeElements"]) {
        skip()
    }

    signing {
        if (!project.hasProperty("skip.signing") && !version.toString().endsWith("-SNAPSHOT")) {
            val signingKey: String? by project
            val signingPassword: String? by project
            useInMemoryPgpKeys(signingKey, signingPassword)
            signing.isRequired
            sign(publishing.publications)
        }
    }

    tasks {

        compileJava {
            options.compilerArgs.add("-parameters")
            options.encoding = "UTF-8"
        }

        shadowJar {
            this.archiveClassifier.set(null as String?)
            this.archiveFileName.set("${project.name}-${project.version}.${this.archiveExtension.getOrElse("jar")}")
        }

        named("build") {
            dependsOn(named("shadowJar"))
        }

        withType<AbstractArchiveTask>().configureEach {
            isPreserveFileTimestamps = false
            isReproducibleFileOrder = true
        }
    }
}

tasks.getByName<Jar>("jar") {
    enabled = false
}
