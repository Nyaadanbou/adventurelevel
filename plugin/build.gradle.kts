import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    id("cc.mewcraft.deploy-conventions")
    alias(libs.plugins.pluginyml.paper)
}

project.ext.set("name", "AdventureLevel")

dependencies {
    // internal modules
    implementation(project(":adventurelevel:api"))
    implementation(project(":adventurelevel:hooks"))

    // internal
    implementation(libs.guice)
    implementation(libs.hikari)
    implementation(libs.evalex)
    implementation(project(":spatula:bukkit:command"))
    implementation(project(":spatula:bukkit:message"))
    implementation(project(":spatula:bukkit:utils"))
    implementation(project(":spatula:network"))

    // server
    compileOnly(libs.server.paper)

    // helper
    compileOnly(libs.helper)
    compileOnly(libs.helper.redis)
}

paper {
    main = "cc.mewcraft.adventurelevel.plugin.AdventureLevelPlugin"
    name = project.ext.get("name") as String
    version = "${project.version}"
    description = project.description
    apiVersion = "1.19"
    authors = listOf("Nailm")
    serverDependencies {
        register("helper") {
            required = true
            joinClasspath = true
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
        }
        register("LuckPerms") {
            required = false
            joinClasspath = true
            load = PaperPluginDescription.RelativeLoadOrder.OMIT
        }
        register("PlaceholderAPI") {
            required = false
            joinClasspath = true
            load = PaperPluginDescription.RelativeLoadOrder.OMIT
        }
        register("MiniPlaceholders") {
            required = false
            joinClasspath = true
            load = PaperPluginDescription.RelativeLoadOrder.OMIT
        }
    }
}
