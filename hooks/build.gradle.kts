plugins {
    id("nyaadanbou-conventions.repositories")
    id("adventurelevel-conventions")
}

dependencies {
    // internal
    compileOnly(project(":api"))
    compileOnly(local.paper)
    compileOnly(local.helper)

    // hooks
    compileOnly(local.luckperms)
    compileOnly(local.placeholderapi) { isTransitive = false }
    compileOnly(local.miniplaceholders) { isTransitive = false }
}
