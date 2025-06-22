import java.time.format.DateTimeFormatter

dependencies {
    // Expected everywhere.
    compileOnlyApi(libs.checkerqual)

    // Minecraft expectations
    compileOnlyApi(libs.gson)
    compileOnly(libs.guava)

    // Platform expectations
    compileOnlyApi(libs.snakeyaml)

    // Adventure
    api(libs.adventureApi)
    api(libs.adventureMiniMessage)

    // Guice
    api(libs.guice) {
        exclude(group = "com.google.guava")
    }
    api(libs.guiceassistedinject) {
        exclude("com.google.inject", "guice")
    }
    api(libs.spotbugs)

    // Plugins
    compileOnly(libs.worldeditCore) {
        exclude(group = "bukkit-classloader-check")
        exclude(group = "mockito-core")
        exclude(group = "dummypermscompat")
    }
    testImplementation(libs.worldeditCore)
    compileOnly(libs.faweBukkit) { isTransitive = false }
    testImplementation(libs.faweCore) { isTransitive = false }

    // Logging
    compileOnlyApi(libs.log4j)

    // Other libraries
    api(libs.prtree)
    api(libs.aopalliance)
    api(libs.cloudServices)
    api(libs.arkitektonika)
    api(libs.paster)
    api(libs.informativeAnnotations)
}

tasks.processResources {
    filesMatching("plugin.properties") {
        expand(
                "version" to project.version.toString(),
                "commit" to rootProject.grgit.head().abbreviatedId,
                "date" to rootProject.grgit.head().dateTime.format(DateTimeFormatter.ofPattern("yy.MM.dd"))
        )
    }

    doLast {
        copy {
            from(layout.buildDirectory.file("$rootDir/LICENSE"))
            into(layout.buildDirectory.dir("resources/main"))
        }
    }
}
