plugins {
    id("cc.mewcraft.publishing-conventions")
}

version = "1.1.0"

dependencies {
    // server
    compileOnly(libs.server.paper)

    // helper
    compileOnly(libs.helper)
}
