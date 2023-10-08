plugins {
    id("cc.mewcraft.publishing-conventions")
}

dependencies {
    // internal modules
    compileOnly(project(":adventurelevel:api"))

    // server
    compileOnly(libs.server.paper)

    // helper
    compileOnly(libs.helper)

    // standalone plugins
    compileOnly(libs.luckperms) // for LuckPerms context support
    compileOnly(libs.papi) { isTransitive = false } // for PlaceholderAPI support
    compileOnly(libs.minipapi) { isTransitive = false } // for MiniPlaceholders support
    compileOnly(libs.mmoitems) // for hooking to player main level of MMOItems
    compileOnly(libs.mythiclib) // same as above
}
