import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    id("nyaadanbou-conventions.repositories")
    id("nyaadanbou-conventions.copy-jar")
    alias(local.plugins.pluginyml.paper)
}

version = "1.1.1"
description = "Add adventure level to players"

dependencies {
    // internal
    implementation(project(":api"))
    implementation(project(":hooks"))
    implementation(local.guice)
    implementation(local.hikaricp)
    implementation(local.evalex)

    // 3rd party
    compileOnly(local.paper)
    compileOnly(local.helper)
    compileOnly(local.helper.redis)
}

tasks {
    copyJar {
        environment = "paper"
        jarFileName = "adventurelevel-${project.version}.jar"
    }
}

paper {
    main = "cc.mewcraft.adventurelevel.plugin.AdventureLevelPlugin"
    name = "AdventureLevel"
    version = "${project.version}"
    description = project.description
    apiVersion = "1.21"
    authors = listOf("Nailm")
    serverDependencies {
        register("helper") {
            required = true
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
        }
        register("LuckPerms") {
            required = false
            load = PaperPluginDescription.RelativeLoadOrder.OMIT
        }
        register("PlaceholderAPI") {
            required = false
            load = PaperPluginDescription.RelativeLoadOrder.OMIT
        }
        register("MiniPlaceholders") {
            required = false
            load = PaperPluginDescription.RelativeLoadOrder.OMIT
        }
    }
}
