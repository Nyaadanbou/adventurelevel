plugins {
    id("cc.mewcraft.publishing-conventions")
}

dependencies {
    // api module
    implementation(project(":adventurelevel:api"))

    // server api
    compileOnly(libs.server.paper)

    // libs that present as other plugins
    compileOnly(libs.helper) { isTransitive = false }
    compileOnly(libs.luckperms) // for LuckPerms context support
    compileOnly(libs.papi) { isTransitive = false } // for placeholders support
    compileOnly(libs.minipapi) { isTransitive = false } // for placeholder support
    compileOnly(libs.mmoitems) // for hooking to player main level
    compileOnly(libs.mythiclib) // same as above
}
