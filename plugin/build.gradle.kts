import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    id("nyaadanbou-conventions.repositories")
    id("nyaadanbou-conventions.copy-jar")
    id("adventurelevel-conventions")
    alias(local.plugins.pluginyml.paper)
}

version = "1.2.0"
description = "Add adventure level to players"

dependencies {
    // internal
    implementation(project(":api"))
    implementation(project(":hooks"))
    implementation(local.guice) {
        exclude("com.google.guava", "guava")
    }
    implementation(local.hikaricp) {
        exclude("org.slf4j", "slf4j-api")
    }
    implementation(local.evalex)
    implementation(local.lang)
    implementation(local.nettowaku)
    implementation(platform(libs.bom.cloud.paper))

    // 3rd party
    compileOnly(local.paper)
    compileOnly(local.helper)
    compileOnly(local.helper.redis)
    compileOnly(libs.husksync.bukkit)
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
        register("helper-redis") {
            required = true
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
        }
        register("LuckPerms") {
            required = false
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
        }
        register("HuskSync") {
            required = false
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
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
